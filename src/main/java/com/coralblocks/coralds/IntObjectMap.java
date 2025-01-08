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
package com.coralblocks.coralds;

import java.lang.ref.SoftReference;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.coralblocks.coralds.util.MathUtils;
import com.coralblocks.coralpool.ArrayObjectPool;
import com.coralblocks.coralpool.ObjectPool;
import com.coralblocks.coralpool.util.Builder;

/**
 * <p>A fast hash map that uses int primitives as keys.</p>
 * 
 * <p>It produces <b>ZERO garbage.</b>.</p>
 *  
 * <p>Its default initial capacity (before a rehash is needed) is 128. The default load factor is 80%.</p>
 *  
 * <p>Initial capacity must be a <b>power of two</b> or an IllegalArgumentException will be thrown by the constructor. That's for <b>bitwise fast hashing</b>.</p>
 *  
 * <p>You should choose the initial capacity wisely, according to your needs, in order to avoid a rehash which of course produces garbage.</p>
 *  
 * <p>It re-uses the same iterator instance not to produce garbage.</p>
 *  
 * <p><b>NOTE:</b> This data structure is designed on purpose to be used by <b>single-threaded systems</b>, in other words, 
 *  it will break if used concurrently by multiple threads.</p>
 * 
 * @param <E> the entry type this hash map will hold
 */
public class IntObjectMap<E> implements Iterable<E> {
	
	/** The default initial capacity if not provided in the constructor */
	public static int DEFAULT_INITIAL_CAPACITY = 128;
	
	/** The default load factor if not provided in the constructor */
	public static float DEFAULT_LOAD_FACTOR = 0.80f;
	
	/* Our LinkedObjectList does not produce any garbage, not even when it grows */
	private final static int SOFT_REFERENCE_LINKED_LIST_INITIAL_SIZE = 32;
	
	private static class Entry<T> {
		int key;
		T value;
		Entry<T> next;
	}
	
	private Entry<E>[] data;

	private int lengthMinusOne;
	private int length;
	private final boolean isPowerOfTwo;

	private int count;

	private int threshold;

	private float loadFactor;

	private final ObjectPool<Entry<E>> entryPool;
	
	private final LinkedObjectList<SoftReference<Entry<E>[]>> oldArrays = new LinkedObjectList<>(SOFT_REFERENCE_LINKED_LIST_INITIAL_SIZE);

	private final ReusableIterator reusableIter = new ReusableIterator();

	private int currIteratorKey;

	/**
	 * Creates a <code>IntObjectMap</code> with the default initial capacity and load factor.
	 */
	public IntObjectMap() {
		this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
	}

	/**
	 * Creates a <code>IntObjectMap</code> with the default load factor.
	 * 
	 * @param initialCapacity the desired initial capacity
	 */
	public IntObjectMap(int initialCapacity) {
		this(initialCapacity, DEFAULT_LOAD_FACTOR);
	}

	/**
	 * Creates a <code>IntObjectMap</code>.
	 * 
	 * @param initialCapacity the desired initial capacity
	 * @param loadFactor the desired load factor
	 */
	@SuppressWarnings("unchecked")
	public IntObjectMap(int initialCapacity, float loadFactor) {
		this.isPowerOfTwo = MathUtils.isPowerOfTwo(initialCapacity);
		this.data = new Entry[initialCapacity];
		this.lengthMinusOne = initialCapacity - 1;
		this.length = initialCapacity;
		this.loadFactor = loadFactor;
		this.threshold = Math.round(initialCapacity * loadFactor);
		
		Builder<Entry<E>> builder = new Builder<Entry<E>>() {
			@Override
			public Entry<E> newInstance() {
				return new Entry<E>();
			}
		};
		
		this.entryPool = new ArrayObjectPool<Entry<E>>(threshold, builder, 2f);
	}
	
	/*
	 * For testing
	 */
	int getCurrentArrayLength() {
		return data.length;
	}
	
	private Entry<E> getEntryFromPool(int key, E value, Entry<E> next) {
		
		Entry<E> newEntry = entryPool.get();

		newEntry.key = key;
		newEntry.value = value;
		newEntry.next = next;

		return newEntry;
	}

	private void releaseEntryBackToPool(Entry<E> entry) {
		entry.value = null;
		entry.next = null;
		entryPool.release(entry);
	}
	
	/**
	 * When using the Iterator for this <code>IntObjectMap</code>, this method will return the current key of the last 
	 * element returned by Iterator.next().
	 * 
	 * @return the current key of the last iterated element
	 */
	public final int getCurrIteratorKey() {
		return currIteratorKey;
	}

	/**
	 * Returns the size of this map
	 * 
	 * @return the size of this map
	 */
	public int size() {
		return count;
	}

	/**
	 * Is this map empty? (size == 0)
	 * 
	 * @return true if empty
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * Does the map contain the given value?
	 * 
	 * @param value the value to be checked
	 * @return true if the map contains this value
	 */
	public boolean contains(E value) {
		
		ensureNotNull(value);

		for(int i = data.length - 1; i >= 0; i--) {

			Entry<E> e = data[i];

			while(e != null) {

				if (e.value.equals(value)) {

					return true;
				}

				e = e.next;
			}
		}
		
		return false;
	}

	private final int toArrayIndex(int key) {
		if (isPowerOfTwo) {
			return (key & 0x7FFFFFFF) & lengthMinusOne;
		} else {
			return (key & 0x7FFFFFFF) % length;
		}
	}

	/**
	 * Does the map contain the given key?
	 * 
	 * @param key the key to check
	 * @return true if the map contains the given key
	 */
	public boolean containsKey(int key) {

		Entry<E> e = data[toArrayIndex(key)];

		while(e != null) {

			if (e.key == key) {

				return true;
			}

			e = e.next;
		}
		
		return false;
	}

	/**
	 * Returns the value associated with the given key in this map
	 * 
	 * @param key the key to get the value
	 * @return the value associated with the given key
	 */
	public E get(int key) {

		Entry<E> e = data[toArrayIndex(key)];

		while(e != null) {

			if (e.key == key) {

				return e.value;
			}

			e = e.next;
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	private void rehash() {

		int oldCapacity = data.length;

		Entry<E> oldData[] = data;

		int newCapacity = oldCapacity * 2; // always so power of two remains power of two

		data = new Entry[newCapacity];
		lengthMinusOne = newCapacity - 1;
		length = newCapacity;

		threshold = Math.round(newCapacity * loadFactor);

		for(int i = oldCapacity - 1; i >= 0; i--) {

			Entry<E> old = oldData[i];
			
			while(old != null) {

				Entry<E> e = old;

				old = old.next;

				int index = toArrayIndex(e.key);

				e.next = data[index];

				data[index] = e;
			}
			
			oldData[i] = null; // nullify not to hold entry objects
		}
		
		oldArrays.addLast(new SoftReference<Entry<E>[]>(oldData));
	}
	
	/**
     * Clears all soft references to old arrays to free memory.
     */
	public void clearSoftReferences() {
		oldArrays.clear();
	}
	
	/**
	 * Adds a value for the given key in this map
	 * 
	 * @param key the key to add
	 * @param value the value to add
	 * @return any previous value associated with the given key or null if there was not a value associated with this key
	 */
	public E put(int key, E value) {

		ensureNotNull(value);
		
		int index = toArrayIndex(key);
		
		Entry<E> e = data[index];

		while(e != null) {

			if (e.key == key) {

				E old = e.value;

				e.value = value;
				
				return old;
			}

			e = e.next;
		}

		if (count >= threshold) {
			
			rehash();

			index = toArrayIndex(key); // lengthMinusOne has changed!

			data[index] = getEntryFromPool(key, value, data[index]);

		} else {
			
			data[index] = getEntryFromPool(key, value, data[index]);
		}

		count++;

		return null;
	}
	
	private final void ensureNotNull(E value) {
		if (value == null) throw new IllegalArgumentException("Method cannot receive null value!");
	}

	/**
	 * Removes and returns the value associated with the given key in the map
	 * 
	 * @param key the key to remove
	 * @return the value for the removed key
	 */
	public E remove(int key) {

		int index = toArrayIndex(key);

		Entry<E> e = data[index];
		Entry<E> prev = null;

		while(e != null) {

			if (e.key == key) {

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
	 * Clears the map. The map will be empty (size == 0) after this operation.
	 */
	public void clear() {

		for(int index = data.length - 1; index >= 0; index--) {

			while(data[index] != null) {

				Entry<E> next = data[index].next;

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
		Entry<E> prev = null;
		Entry<E> next = null;
		Entry<E> entry = null;
		boolean wasRemoved = false;

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
	 * Returns the same instance of the iterator (garbage-free)
	 * 
	 * @return the same instance of the iterator
	 */
	@Override
	public Iterator<E> iterator() {
		reusableIter.reset();
		return reusableIter;
	}
	
}
