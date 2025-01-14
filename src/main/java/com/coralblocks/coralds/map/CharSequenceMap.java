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
 * A hash map implementation that uses {@link CharSequence CharSequences} as keys
 * and objects of type {@code E} as values. This map handles keys with variable lengths
 * up to a specified maximum and employs techniques such as soft references and object
 * pooling to optimize memory usage and performance.
 *
 * <p>This map supports various operations including insertion, retrieval, removal, and iteration
 * over the values. It also manages internal resizing based on a configurable load factor to
 * maintain efficient access times. The initial capacity can be any size; however, using a
 * power-of-two size may improve performance by allowing bitwise operations instead of the
 * modulus operator (%). When the map reaches its load factor threshold, its capacity is doubled,
 * preserving the power-of-two property if the initial capacity was a power of two.</p>
 *
 * <p><b>NOTE:</b> This data structure is designed on purpose to be used by
 * <b>single-threaded systems</b>. In other words, it will break if used concurrently by multiple
 * threads.</p>
 *
 * @param <E> the type of mapped values
 */
public class CharSequenceMap<E> implements Iterable<E> {

	/** The default initial capacity of the map. */
	public static final int DEFAULT_INITIAL_CAPACITY = 128;
	
	/** The default load factor for the map. */
	public static final float DEFAULT_LOAD_FACTOR = 0.80f;
	
	/** The default maximum key length in bytes. */
	public static final short DEFAULT_MAX_KEY_LENGTH = 256;
	
	private static final int SOFT_REFERENCE_LINKED_LIST_INITIAL_SIZE = 32;
	
	static class Entry<T> {
		final StringBuilder keyStringBuilder;
		int hash;
		T value;
		Entry<T> next;
		
		Entry(int maxKeyLength) {
			this.keyStringBuilder = new StringBuilder(maxKeyLength);
		}
	}

	private Entry<E>[] data;
	private int lengthMinusOne;
	private int length;
	private int count;
	private int threshold;
	private float loadFactor;
	private CharSequence currIteratorKey = null;
	private final boolean isPowerOfTwo;
	private final int maxKeyLength;
	
	private final ObjectPool<Entry<E>> entryPool;

	private ReusableIterator reusableIter = new ReusableIterator();
	
	private final LinkedList<SoftReference<Entry<E>[]>> oldArrays = new LinkedList<>(SOFT_REFERENCE_LINKED_LIST_INITIAL_SIZE);
	
    /**
     * Constructs a new {@code CharSequenceMap} with default initial capacity, maximum key length,
     * and load factor.
     */
	public CharSequenceMap() {
		this(DEFAULT_INITIAL_CAPACITY, DEFAULT_MAX_KEY_LENGTH, DEFAULT_LOAD_FACTOR);
	}

    /**
     * Constructs a new {@code CharSequenceMap} with the specified initial capacity and default
     * maximum key length and load factor.
     *
     * @param initialCapacity the initial capacity of the map
     */
	public CharSequenceMap(int initialCapacity) {
		this(initialCapacity, DEFAULT_MAX_KEY_LENGTH, DEFAULT_LOAD_FACTOR);
	}
	
    /**
     * Constructs a new {@code CharSequenceMap} with the specified initial capacity, maximum
     * key length, and default load factor.
     *
     * @param initialCapacity the initial capacity of the map
     * @param maxKeyLength    the maximum allowed length for keys
     */
	public CharSequenceMap(int initialCapacity, short maxKeyLength) {
		this(initialCapacity, maxKeyLength, DEFAULT_LOAD_FACTOR);
	}
	
    /**
     * Constructs a new {@code CharSequenceMap} with the specified initial capacity, maximum key
     * length, and load factor.
     *
     * @param initialCapacity the initial capacity of the map
     * @param loadFactor      the load factor for resizing the map
     */
	public CharSequenceMap(int initialCapacity, float loadFactor) {
		this(initialCapacity, DEFAULT_MAX_KEY_LENGTH, loadFactor);
	}
	
    /**
     * Constructs a new {@code CharSequenceMap} with the specified maximum key length and default
     * initial capacity and load factor.
     *
     * @param maxKeyLength the maximum allowed length for keys
     */
	public CharSequenceMap(short maxKeyLength) {
		this(DEFAULT_INITIAL_CAPACITY, maxKeyLength, DEFAULT_LOAD_FACTOR);
	}
	
    /**
     * Constructs a new {@code CharSequenceMap} with the specified maximum key length and load
     * factor, using the default initial capacity.
     *
     * @param maxKeyLength the maximum allowed length for keys
     * @param loadFactor    the load factor for resizing the map
     */
	public CharSequenceMap(short maxKeyLength, float loadFactor) {
		this(DEFAULT_INITIAL_CAPACITY, maxKeyLength, loadFactor);
	}
	
    /**
     * Constructs a new {@code CharSequenceMap} with the specified load factor and default
     * initial capacity and maximum key length.
     *
     * @param loadFactor the load factor for resizing the map
     */
	public CharSequenceMap(float loadFactor) {
		this(DEFAULT_INITIAL_CAPACITY, DEFAULT_MAX_KEY_LENGTH, loadFactor);
	}
	
    /**
     * Constructs a new {@code CharSequenceMap} with the specified initial capacity, maximum key
     * length, and load factor.
     *
     * @param initialCapacity the initial capacity of the map
     * @param maxKeyLength    the maximum allowed length for keys
     * @param loadFactor      the load factor for resizing the map
     */
	@SuppressWarnings("unchecked")
	public CharSequenceMap(int initialCapacity, short maxKeyLength, float loadFactor) {
		
		this.isPowerOfTwo = MathUtils.isPowerOfTwo(initialCapacity);
		this.data = new Entry[initialCapacity];
		this.lengthMinusOne = initialCapacity - 1;
		this.length = initialCapacity;
		this.loadFactor = loadFactor;
		this.threshold = Math.round(initialCapacity * loadFactor);
		this.maxKeyLength = maxKeyLength;
		
		ObjectBuilder<Entry<E>> builder = new ObjectBuilder<Entry<E>>() {
			@Override
			public Entry<E> newInstance() {
				return new Entry<E>(maxKeyLength);
			}
		};
		
		this.entryPool = new ArrayObjectPool<Entry<E>>(threshold, builder, 2f);
	}
	
	private static final int hashCode(CharSequence src) {
		final int len = src.length();
		int hash = 0;
		for (int i = 0; i < len; i++) {
			hash = 31 * hash + src.charAt(i);
		}
		return hash & 0x7FFFFFFF;
	}
	
	private static final boolean equals(CharSequence internalKey, 
										CharSequence externalKey) { 

		if (internalKey.length() != externalKey.length()) return false;
		
		final int len = internalKey.length();
		
		for(int i = 0; i < len; i++) {
			if (internalKey.charAt(i) != externalKey.charAt(i)) return false;
		}

		return true;
	}
	
	private final int toArrayIndex(int hash) {
		if (isPowerOfTwo) {
			return (hash & 0x7FFFFFFF) & lengthMinusOne;
		} else {
			return (hash & 0x7FFFFFFF) % length;
		}
	}
	
	private final void releaseEntry(Entry<E> entry) {
		entry.value = null;
		entry.next = null;
		entryPool.release(entry);
	}
	
    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of key-value mappings
     */
	public final int size() {
		return count;
	}

    /**
     * Returns the current key from the iterator. This is useful when iterating over the map
     * to access the key corresponding to the current value.
     *
     * @return a {@code CharSequence} containing the current key
     */
	public final CharSequence getCurrIteratorKey() {
		return currIteratorKey;
	}

    /**
     * Checks if the map is empty.
     *
     * @return {@code true} if the map contains no key-value mappings, {@code false} otherwise
     */
	public final boolean isEmpty() {
		return size() == 0;
	}

    /**
     * Checks if the map contains the specified key.
     *
     * @param key the {@code CharSequence} key to search for
     * @return {@code true} if the key exists in the map, {@code false} otherwise
     */
	public final boolean containsKey(CharSequence key) {
		
		return get(key) != null;
	}

	private final void ensureNotNull(E value) {
		if (value == null) throw new IllegalArgumentException("Method cannot receive null value!");
	}
	
	private final void ensureMaxKeyLength(int len) {
		if (len > maxKeyLength) throw new IllegalArgumentException("Key is too big: " + len + " (max=" + maxKeyLength + ")");
	}

    /**
     * Retrieves the value associated with the specified {@link CharSequence} key.
     *
     * @param key the {@code CharSequence} key to search for
     * @return the associated value, or {@code null} if the key does not exist
     * @throws IllegalArgumentException if the key length exceeds the maximum allowed
     */
	public final E get(CharSequence key) {
		
		if (key.length() > maxKeyLength) return null;

		int hash = hashCode(key);
		
		int index = toArrayIndex(hash);

		Entry<E> e = data[index];

		while(e != null) {

			if (e.hash == hash && equals(e.keyStringBuilder, key)) {

				return e.value;
			}

			e = e.next;
		}
		
		return null;
	}

	@SuppressWarnings("unchecked")
	private final void rehash() {

		int oldCapacity = data.length;

		Entry<E> oldData[] = data;

		int newCapacity = oldCapacity * 2;

		data = new Entry[newCapacity];
		lengthMinusOne = newCapacity - 1;
		length = newCapacity;

		threshold = Math.round(newCapacity * loadFactor);

		for(int i = oldCapacity - 1; i >= 0; i--) {

			Entry<E> old = oldData[i];

			while(old != null) {

				Entry<E> e = old;

				old = old.next;

				int index = toArrayIndex(e.hash);

				e.next = data[index];

				data[index] = e;
			}
			
			oldData[i] = null; // nullify not to hold entry objects
		}
		
		oldArrays.addLast(new SoftReference<Entry<E>[]>(oldData));
	}
	
    /**
     * Clears all soft references to old data arrays, allowing them to be garbage collected
     * and freeing memory.
     */
	public void clearSoftReferences() {
		oldArrays.clear();
	}

    /**
     * Associates the specified value with the specified {@link CharSequence} key in the map. If the
     * map previously contained a mapping for the key, the old value is replaced.
     *
     * @param key   the {@code CharSequence} key with which the specified value is to be associated
     * @param value the value to be associated with the specified key
     * @return the previous value associated with the key, or {@code null} if there was no mapping
     * @throws IllegalArgumentException if the key length exceeds the maximum allowed or the value is {@code null}
     */
	public final E put(CharSequence key, E value) {
		
		ensureMaxKeyLength(key.length());

		ensureNotNull(value);
		
		int hash = hashCode(key);

		int index = toArrayIndex(hash);

		Entry<E> e = data[index];

		while(e != null) {

			if (e.hash == hash && equals(e.keyStringBuilder, key)) {

				E old = e.value;

				e.value = value;

				return old;
			}

			e = e.next;
		}

		if (count >= threshold) {

			rehash();

			index = toArrayIndex(hash); // array size changed!
		}

		Entry<E> newEntry = entryPool.get();

		newEntry.keyStringBuilder.setLength(0);
		newEntry.keyStringBuilder.append(key);
		newEntry.hash = hash;
		newEntry.value = value;
		newEntry.next = data[index];
		
		data[index] = newEntry;
		
		count++;

		return null;
	}
	
    /**
     * Removes the mapping for the specified {@link CharSequence} key from the map if present.
     *
     * @param key the {@code CharSequence} key whose mapping is to be removed
     * @return the previous value associated with the key, or {@code null} if there was no mapping
     * @throws IllegalArgumentException if the key length exceeds the maximum allowed
     */
	public final E remove(CharSequence key) {
		
		if (key.length() > maxKeyLength) return null;

		int hash = hashCode(key);

		int index = toArrayIndex(hash);

		Entry<E> e = data[index];
		Entry<E> prev = null;

		while(e != null) {

			if (e.hash == hash && equals(e.keyStringBuilder, key)) {

				if (prev != null) {

					prev.next = e.next;

				} else {

					data[index] = e.next;
				}

				E oldValue = e.value;

				releaseEntry(e);

				count--;

				return oldValue;
			}

			prev = e;
			e = e.next;
		}

		return null;
	}
	
    /**
     * Removes all mappings from the map. The map will be empty after this call returns.
     */
	public final void clear() {

		for(int index = data.length - 1; index >= 0; index--) {

			while(data[index] != null) {

				Entry<E> next = data[index].next;

				releaseEntry(data[index]);

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
		
        /**
         * Resets the iterator to the beginning of the map.
         */
		final void reset() {
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

			currIteratorKey = entry.keyStringBuilder;

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

			releaseEntry(entry);

			entry = null;

			count--;
		}
	}
	
    /**
     * Returns an iterator over the values in the map. The same instance of the iterator is returned every time.
     *
     * @return an iterator over the map's values
     */
	@Override
	public Iterator<E> iterator() {
		reusableIter.reset();
		return reusableIter;
	}
}
