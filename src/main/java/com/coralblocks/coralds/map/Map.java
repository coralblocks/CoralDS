/* 
 * Copyright 2015-2024 (c) CoralBlocks LLC - http://www.coralblocks.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.coralblocks.coralds.map;

import java.lang.ref.SoftReference;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.coralblocks.coralds.list.LinkedList;
import com.coralblocks.coralds.util.MathUtils;
import com.coralblocks.coralpool.ArrayObjectPool;
import com.coralblocks.coralpool.ObjectBuilder;
import com.coralblocks.coralpool.ObjectPool;

/**
 * A hash-based map implementation that stores key-value pairs.
 * <p>
 * This map supports a configurable load factor and uses an internal array of entries
 * to store the data. The initial capacity can be any size. However, if you choose a
 * power-of-two size, the map can take advantage of faster bitwise operations instead
 * of the modulus operator (%). When the load factor threshold is exceeded, the internal
 * array is rehashed, and its capacity is always doubled. Thus, if the initial capacity
 * is a power of two, it remains a power of two after rehashing. An internal pool of
 * entry objects is also employed for memory reuse (garbage-free).
 *
 * <p><b>NOTE:</b> This data structure is designed on purpose to be used by <b>single-threaded systems</b>. In other
 * words, it will break if used concurrently by multiple threads.</p>
 * 
 * @param <K> the type of keys
 * @param <E> the type of values
 */
public class Map<K, E> implements Iterable<E> {
	
	/**
	 * The default initial capacity if not provided in the constructor.
	 */
	public static int DEFAULT_INITIAL_CAPACITY = 128;
	
	/**
	 * The default load factor if not provided in the constructor.
	 */
	public static float DEFAULT_LOAD_FACTOR = 0.80f;
	
	private static final int SOFT_REFERENCE_LINKED_LIST_INITIAL_SIZE = 32;
	
	private static class Entry<K, E> {
		K key;
		int hash;
		E value;
		Entry<K, E> next;
	}
	
	private Entry<K, E>[] data;

	private int lengthMinusOne;
	private int length;
	private final boolean isPowerOfTwo;

	private int count;

	private int threshold;

	private float loadFactor;

	private final ObjectPool<Entry<K, E>> entryPool;
	
	private final LinkedList<SoftReference<Entry<K, E>[]>> oldArrays = new LinkedList<>(SOFT_REFERENCE_LINKED_LIST_INITIAL_SIZE);

	private final ReusableIterator reusableIter = new ReusableIterator();

	private K currIteratorKey;
	
	/**
	 * Creates a <code>Map</code> with the default initial capacity and load factor.
	 */
	public Map() {
		this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
	}

	/**
	 * Creates a <code>Map</code> with the default load factor.
	 *
	 * @param initialCapacity the desired initial capacity
	 */
	public Map(int initialCapacity) {
		this(initialCapacity, DEFAULT_LOAD_FACTOR);
	}

	/**
	 * Creates a <code>Map</code> with the specified initial capacity and load factor.
	 *
	 * @param initialCapacity the desired initial capacity
	 * @param loadFactor the desired load factor
	 */
	@SuppressWarnings("unchecked")
	public Map(int initialCapacity, float loadFactor) {
		this.isPowerOfTwo = MathUtils.isPowerOfTwo(initialCapacity);
		this.data = new Entry[initialCapacity];
		this.lengthMinusOne = initialCapacity - 1;
		this.length = initialCapacity;
		this.loadFactor = loadFactor;
		this.threshold = Math.round(initialCapacity * loadFactor);
		
		ObjectBuilder<Entry<K, E>> builder = new ObjectBuilder<Entry<K, E>>() {
			@Override
			public Entry<K, E> newInstance() {
				return new Entry<K, E>();
			}
		};
		
		this.entryPool = new ArrayObjectPool<Entry<K, E>>(threshold, builder, 2f);
	}
	
	int getCurrentArrayLength() {
		return data.length;
	}
	
	private Entry<K, E> getEntryFromPool(K key, int hash, E value, Entry<K, E> next) {
		
		Entry<K, E> newEntry = entryPool.get();

		newEntry.key = key;
		newEntry.hash = hash;
		newEntry.value = value;
		newEntry.next = next;

		return newEntry;
	}

	private void releaseEntryBackToPool(Entry<K, E> entry) {
		entry.key = null;
		entry.value = null;
		entry.next = null;
		entryPool.release(entry);
	}
	
	/**
	 * When using the Iterator for this <code>Map</code>, this method will return the current key 
	 * of the last element returned by Iterator.next().
	 * 
	 * @return the current key of the last iterated element
	 */
	public final K getCurrIteratorKey() {
		return currIteratorKey;
	}

	/**
	 * Returns the number of elements in this map.
	 *
	 * @return the current size of the map
	 */
	public int size() {
		return count;
	}

	/**
	 * Indicates whether this map is empty.
	 *
	 * @return true if the map is empty, false otherwise
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	private final int toArrayIndex(int hash) {
		if (isPowerOfTwo) {
			return (hash & 0x7FFFFFFF) & lengthMinusOne;
		} else {
			return (hash & 0x7FFFFFFF) % length;
		}
	}

	/**
	 * Checks if the map contains the given key.
	 *
	 * @param key the key to check
	 * @return true if this map contains the specified key, false otherwise
	 * @throws IllegalArgumentException if the key is null
	 */
	public boolean containsKey(K key) {
		
		ensureNotNull(key);
		
		int hash = key.hashCode();

		Entry<K, E> e = data[toArrayIndex(hash)];

		while(e != null) {

			if (e.hash == hash && e.key.equals(key)) {
				return true;
			}

			e = e.next;
		}
		
		return false;
	}

	/**
	 * Retrieves the value associated with the given key in this map.
	 *
	 * @param key the key whose associated value is to be returned
	 * @return the value associated with the key, or null if the key is not found
	 * @throws IllegalArgumentException if the key is null
	 */
	public E get(K key) {
		
		ensureNotNull(key);
		
		int hash = key.hashCode();

		Entry<K, E> e = data[toArrayIndex(hash)];

		while(e != null) {

			if (e.hash == hash && e.key.equals(key)) {
				return e.value;
			}

			e = e.next;
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	private void rehash() {

		int oldCapacity = data.length;

		Entry<K, E> oldData[] = data;

		int newCapacity = oldCapacity * 2; // keep power of two if originally so

		data = new Entry[newCapacity];
		lengthMinusOne = newCapacity - 1;
		length = newCapacity;

		threshold = Math.round(newCapacity * loadFactor);

		for(int i = oldCapacity - 1; i >= 0; i--) {

			Entry<K, E> old = oldData[i];
			
			while(old != null) {

				Entry<K, E> e = old;

				old = old.next;

				int index = toArrayIndex(e.key.hashCode());

				e.next = data[index];

				data[index] = e;
			}
			
			oldData[i] = null; // nullify to avoid holding entry objects
		}
		
		oldArrays.addLast(new SoftReference<Entry<K, E>[]>(oldData));
	}
	
	/**
	 * Clears all soft references to old arrays, potentially freeing memory 
	 * used by those old arrays.
	 */
	public void clearSoftReferences() {
		oldArrays.clear();
	}
	
	/**
	 * Associates the specified value with the specified key in this map.
	 * If the key is already present, the old value is replaced.
	 *
	 * @param key the key with which the specified value is to be associated
	 * @param value the value to be associated with the specified key
	 * @return the previous value associated with the key, or null if there was no mapping
	 * @throws IllegalArgumentException if the key or value is null
	 */
	public E put(K key, E value) {
		
		ensureNotNull(key);
		ensureNotNull(value);
		
		int hash = key.hashCode();
		
		int index = toArrayIndex(hash);
		
		Entry<K, E> e = data[index];

		while(e != null) {

			if (e.hash == hash && e.key.equals(key)) {

				E old = e.value;
				e.value = value;
				return old;
			}

			e = e.next;
		}

		if (count >= threshold) {
			
			rehash();

			index = toArrayIndex(hash); // array length has changed!
		}
			
		data[index] = getEntryFromPool(key, hash, value, data[index]);

		count++;

		return null;
	}
	
	private final void ensureNotNull(Object value) {
		if (value == null) throw new IllegalArgumentException("Method cannot receive null!");
	}

	/**
	 * Removes the mapping for the specified key from this map if present.
	 *
	 * @param key the key whose mapping is to be removed
	 * @return the previous value associated with the specified key, or null if there was no mapping
	 * @throws IllegalArgumentException if the key is null
	 */
	public E remove(K key) {
		
		ensureNotNull(key);
		
		int hash = key.hashCode();

		int index = toArrayIndex(hash);

		Entry<K, E> e = data[index];
		Entry<K, E> prev = null;

		while(e != null) {

			if (e.hash == hash && e.key.equals(key)) {

				if (prev != null) {

					prev.next = e.next;

				} else {

					data[index] = e.next;
				}

				E oldValue = e.value;

				releaseEntryBackToPool(e);

				count--;

				return oldValue;
			}

			prev = e;
			e = e.next;
		}

		return null;
	}

	/**
	 * Removes all mappings from this map, leaving it empty.
	 */
	public void clear() {

		for(int index = data.length - 1; index >= 0; index--) {

			while(data[index] != null) {

				Entry<K, E> next = data[index].next;

				releaseEntryBackToPool(data[index]);

				data[index] = next;
			}
		}

		count = 0;
	}

	private class ReusableIterator implements Iterator<E> {

		int size = count;
		int index = 0;
		int dataIndex = 0;
		Entry<K, E> prev = null;
		Entry<K, E> next = null;
		Entry<K, E> entry = null;
		boolean wasRemoved = false;

		/**
		 * Resets the iterator to the initial position.
		 */
		public void reset() {
			this.size = count;
			this.index = 0;
			this.dataIndex = 0;
			this.prev = null;
			this.next = data[0];
			this.entry = null;
			this.wasRemoved = false;
		}

		@Override
		public final boolean hasNext() {
			return index < size;
		}

		@Override
		public final E next() {

			if (index >= size) throw new NoSuchElementException();

			if (!wasRemoved) prev = entry;
			
			wasRemoved = false;

			entry = next;

			if (entry == null) {
				while(entry == null) {
					dataIndex++;
					entry = data[dataIndex];
				}
				prev = null;
			}

			index++;
			
			E o = entry.value;

			currIteratorKey = entry.key;

			next = entry.next;

			return o;
		}

		@Override
		public final void remove() {

			if (wasRemoved || entry == null) {
				throw new NoSuchElementException();
			}
			
			wasRemoved = true;

			if (prev == null) {
				data[dataIndex] = next;
			} else {
				prev.next = next;
			}

			releaseEntryBackToPool(entry);

			entry = null;

			count--;
		}

	}

	/**
	 * Returns an iterator that reuses the same instance (garbage-free).
	 *
	 * @return an iterator over the elements in this map
	 */
	@Override
	public Iterator<E> iterator() {
		reusableIter.reset();
		return reusableIter;
	}
}

