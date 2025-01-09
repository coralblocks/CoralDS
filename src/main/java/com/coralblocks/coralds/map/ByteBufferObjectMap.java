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
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.coralblocks.coralds.LinkedObjectList;
import com.coralblocks.coralds.util.MathUtils;
import com.coralblocks.coralpool.ArrayObjectPool;
import com.coralblocks.coralpool.ObjectPool;
import com.coralblocks.coralpool.util.Builder;

/**
 * A hash map implementation that uses {@link ByteBuffer ByteBuffers} and/or byte arrays as keys
 * and objects of type {@code E} as values. This map handles keys with variable lengths up to a
 * specified maximum and employs techniques such as soft references and object pooling to optimize
 * memory usage and performance.
 *
 * <p>The map supports various operations including insertion, retrieval, removal, and iteration
 * over the values. It also manages internal resizing based on a configurable load factor to
 * maintain efficient access times.</p>
 * 
 *  <p><b>NOTE:</b> This data structure is designed on purpose to be used by <b>single-threaded systems</b>, in other words, 
 *  it will break if used concurrently by multiple threads.</p>
 *
 * @param <E> the type of mapped values
 */
public class ByteBufferObjectMap<E> implements Iterable<E> {

	/** The default initial capacity of the map. */
	public static final int DEFAULT_INITIAL_CAPACITY = 128;
	
	/** The default load factor for the map. */
	public static final float DEFAULT_LOAD_FACTOR = 0.80f;
	
	/** The default maximum key length in bytes. */
	public static final short DEFAULT_MAX_KEY_LENGTH = 256;
	
	private static final int SOFT_REFERENCE_LINKED_LIST_INITIAL_SIZE = 32;
	
	static class Entry<T> {
		final ByteBuffer keyByteBuffer;
		int keyLength;
		int hash;
		T value;
		Entry<T> next;
		
		Entry(int maxKeyLength) {
			this.keyByteBuffer = ByteBuffer.allocateDirect(maxKeyLength);
		}
	}

	private Entry<E>[] data;
	private int lengthMinusOne;
	private int length;
	private int count;
	private int threshold;
	private float loadFactor;
	private ByteBuffer currIteratorKey = null;
	private final boolean isPowerOfTwo;
	private final int maxKeyLength;
	
	private final ObjectPool<Entry<E>> entryPool;

	private ReusableIterator reusableIter = new ReusableIterator();
	
	private final LinkedObjectList<SoftReference<Entry<E>[]>> oldArrays = new LinkedObjectList<>(SOFT_REFERENCE_LINKED_LIST_INITIAL_SIZE);
	
    /**
     * Constructs a new {@code ByteBufferObjectMap} with default initial capacity, maximum key length,
     * and load factor.
     */
	public ByteBufferObjectMap() {
		this(DEFAULT_INITIAL_CAPACITY, DEFAULT_MAX_KEY_LENGTH, DEFAULT_LOAD_FACTOR);
	}

    /**
     * Constructs a new {@code ByteBufferObjectMap} with the specified initial capacity and default
     * maximum key length and load factor.
     *
     * @param initialCapacity the initial capacity of the map
     */
	public ByteBufferObjectMap(int initialCapacity) {
		this(initialCapacity, DEFAULT_MAX_KEY_LENGTH, DEFAULT_LOAD_FACTOR);
	}
	
    /**
     * Constructs a new {@code ByteBufferObjectMap} with the specified initial capacity, maximum
     * key length, and default load factor.
     *
     * @param initialCapacity the initial capacity of the map
     * @param maxKeyLength    the maximum allowed length for keys
     */
	public ByteBufferObjectMap(int initialCapacity, short maxKeyLength) {
		this(initialCapacity, maxKeyLength, DEFAULT_LOAD_FACTOR);
	}
	
    /**
     * Constructs a new {@code ByteBufferObjectMap} with the specified initial capacity, maximum key
     * length, and load factor.
     *
     * @param initialCapacity the initial capacity of the map
     * @param loadFactor      the load factor for resizing the map
     */
	public ByteBufferObjectMap(int initialCapacity, float loadFactor) {
		this(initialCapacity, DEFAULT_MAX_KEY_LENGTH, loadFactor);
	}
	
    /**
     * Constructs a new {@code ByteBufferObjectMap} with the specified maximum key length and default
     * initial capacity and load factor.
     *
     * @param maxKeyLength the maximum allowed length for keys
     */
	public ByteBufferObjectMap(short maxKeyLength) {
		this(DEFAULT_INITIAL_CAPACITY, maxKeyLength, DEFAULT_LOAD_FACTOR);
	}
	
    /**
     * Constructs a new {@code ByteBufferObjectMap} with the specified maximum key length and load
     * factor, using the default initial capacity.
     *
     * @param maxKeyLength the maximum allowed length for keys
     * @param loadFactor    the load factor for resizing the map
     */
	public ByteBufferObjectMap(short maxKeyLength, float loadFactor) {
		this(DEFAULT_INITIAL_CAPACITY, maxKeyLength, loadFactor);
	}
	
    /**
     * Constructs a new {@code ByteBufferObjectMap} with the specified load factor and default
     * initial capacity and maximum key length.
     *
     * @param loadFactor the load factor for resizing the map
     */
	public ByteBufferObjectMap(float loadFactor) {
		this(DEFAULT_INITIAL_CAPACITY, DEFAULT_MAX_KEY_LENGTH, loadFactor);
	}
	
    /**
     * Constructs a new {@code ByteBufferObjectMap} with the specified initial capacity, maximum key
     * length, and load factor.
     *
     * @param initialCapacity the initial capacity of the map
     * @param maxKeyLength    the maximum allowed length for keys
     * @param loadFactor      the load factor for resizing the map
     */
	@SuppressWarnings("unchecked")
	public ByteBufferObjectMap(int initialCapacity, short maxKeyLength, float loadFactor) {
		
		this.isPowerOfTwo = MathUtils.isPowerOfTwo(initialCapacity);
		this.data = new Entry[initialCapacity];
		this.lengthMinusOne = initialCapacity - 1;
		this.length = initialCapacity;
		this.loadFactor = loadFactor;
		this.threshold = Math.round(initialCapacity * loadFactor);
		this.maxKeyLength = maxKeyLength;
		
		Builder<Entry<E>> builder = new Builder<Entry<E>>() {
			@Override
			public Entry<E> newInstance() {
				return new Entry<E>(maxKeyLength);
			}
		};
		
		this.entryPool = new ArrayObjectPool<Entry<E>>(threshold, builder, 2f);
	}
	
	private static final int hashCode(byte[] src) {
		return hashCode(src, 0, src.length);
	}
	
	private static final int hashCode(byte[] src, int start, int len) {
		int hash = 0;
		for (int i = 0; i < len; i++) {
			hash = 31 * hash + src[start++];
		}
		return hash & 0x7FFFFFFF;
	}
	
	private static final int hashCode(ByteBuffer src) {
		final int pos = src.position();
		final int len = src.remaining();
		int hash = 0;
		for (int i = 0; i < len; i++) {
			hash = 31 * hash + src.get();
		}
		src.position(pos);
		return hash & 0x7FFFFFFF;
	}
	
	private static final boolean equals(ByteBuffer internalKey, 
										int internalKeyLength, 
										byte[] externalKey, 
										int externalKeyStart, 
										int externalKeyLength) {
		
		if (internalKeyLength != externalKeyLength) return false;
		
		internalKey.limit(internalKeyLength).position(0);
		
		for(int i = 0; i < internalKeyLength; i++) {
			if (internalKey.get(i) != externalKey[externalKeyStart + i]) return false;
		}
		
		return true;
	}
	
	private static final boolean equals(ByteBuffer internalKey, 
										int internalKeyLength, 
										ByteBuffer externalKey) { 

		if (internalKeyLength != externalKey.remaining()) return false;
		
		internalKey.limit(internalKeyLength).position(0);
		
		final int pos = externalKey.position();

		for(int i = 0; i < internalKeyLength; i++) {
			if (internalKey.get(i) != externalKey.get(pos + i)) return false;
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
     * @return a {@code ByteBuffer} containing the current key
     */
	public final ByteBuffer getCurrIteratorKey() {
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
     * @param key the key to search for
     * @return {@code true} if the key exists in the map, {@code false} otherwise
     */
	public final boolean containsKey(byte[] key) {
		
		return get(key) != null;
	}
	
    /**
     * Checks if the map contains the specified key.
     *
     * @param key the {@code ByteBuffer} key to search for
     * @return {@code true} if the key exists in the map, {@code false} otherwise
     */
	public final boolean containsKey(ByteBuffer key) {
		
		return get(key) != null;
	}

    /**
     * Checks if the map contains the specified key segment.
     *
     * @param key   the byte array containing the key
     * @param start the starting index of the key segment
     * @param len   the length of the key segment
     * @return {@code true} if the key segment exists in the map, {@code false} otherwise
     */
	public final boolean containsKey(byte[] key, int start, int len) {

		return get(key, start, len) != null;
	}
	
	private final void ensureNotNull(E value) {
		if (value == null) throw new IllegalArgumentException("Method cannot receive null value!");
	}
	
	private final void ensureMaxKeyLength(int len) {
		if (len > maxKeyLength) throw new IllegalArgumentException("Key is too big: " + len + " (max=" + maxKeyLength + ")");
	}

    /**
     * Retrieves the value associated with the specified key.
     *
     * @param key the key to search for
     * @return the associated value, or {@code null} if the key does not exist
     * @throws IllegalArgumentException if the key length exceeds the maximum allowed
     */
	public final E get(byte[] key) {
		
		ensureMaxKeyLength(key.length);

		int hash = hashCode(key);
		
		int index = toArrayIndex(hash);

		Entry<E> e = data[index];

		while(e != null) {

			if (e.hash == hash && equals(e.keyByteBuffer, e.keyLength, key, 0, key.length)) {

				return e.value;
			}

			e = e.next;
		}
		
		return null;
	}
	
    /**
     * Retrieves the value associated with the specified {@link ByteBuffer} key.
     *
     * @param key the {@code ByteBuffer} key to search for
     * @return the associated value, or {@code null} if the key does not exist
     * @throws IllegalArgumentException if the key length exceeds the maximum allowed
     */
	public final E get(ByteBuffer key) {
		
		ensureMaxKeyLength(key.remaining());

		int hash = hashCode(key);
		
		int index = toArrayIndex(hash);

		Entry<E> e = data[index];

		while(e != null) {

			if (e.hash == hash && equals(e.keyByteBuffer, e.keyLength, key)) {

				return e.value;
			}

			e = e.next;
		}
		
		return null;
	}

    /**
     * Retrieves the value associated with the specified key segment.
     *
     * @param key   the byte array containing the key
     * @param start the starting index of the key segment
     * @param len   the length of the key segment
     * @return the associated value, or {@code null} if the key segment does not exist
     * @throws IllegalArgumentException if the key length exceeds the maximum allowed
     */
	public final E get(byte[] key, int start, int len) {
		
		ensureMaxKeyLength(len);

		int hash = hashCode(key, start, len);
		
		int index = toArrayIndex(hash);

		Entry<E> e = data[index];

		while(e != null) {

			if (e.hash == hash && equals(e.keyByteBuffer, e.keyLength, key, start, len)) {

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
     * Associates the specified value with the specified key in the map. If the map previously
     * contained a mapping for the key, the old value is replaced.
     *
     * @param key   the key with which the specified value is to be associated
     * @param value the value to be associated with the specified key
     * @return the previous value associated with the key, or {@code null} if there was no mapping
     * @throws IllegalArgumentException if the key length exceeds the maximum allowed or the value is {@code null}
     */
	public final E put(byte[] key, E value) {
		
		ensureMaxKeyLength(key.length);

		ensureNotNull(value);
		
		int hash = hashCode(key);

		int index = toArrayIndex(hash);

		Entry<E> e = data[index];

		while(e != null) {

			if (e.hash == hash && equals(e.keyByteBuffer, e.keyLength, key, 0, key.length)) {

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

		newEntry.keyLength = key.length;
		newEntry.keyByteBuffer.clear();
		newEntry.keyByteBuffer.put(key);
		newEntry.keyByteBuffer.flip();
		newEntry.hash = hash;
		newEntry.value = value;
		newEntry.next = data[index];
		
		data[index] = newEntry;
		
		count++;

		return null;
	}
	
    /**
     * Associates the specified value with the specified {@link ByteBuffer} key in the map. If the
     * map previously contained a mapping for the key, the old value is replaced.
     *
     * @param key   the {@code ByteBuffer} key with which the specified value is to be associated
     * @param value the value to be associated with the specified key
     * @return the previous value associated with the key, or {@code null} if there was no mapping
     * @throws IllegalArgumentException if the key length exceeds the maximum allowed or the value is {@code null}
     */
	public final E put(ByteBuffer key, E value) {
		
		ensureMaxKeyLength(key.remaining());

		ensureNotNull(value);
		
		int hash = hashCode(key);

		int index = toArrayIndex(hash);

		Entry<E> e = data[index];

		while(e != null) {

			if (e.hash == hash && equals(e.keyByteBuffer, e.keyLength, key)) {

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

		newEntry.keyLength = key.remaining();
		final int pos = key.position();
		newEntry.keyByteBuffer.clear();
		newEntry.keyByteBuffer.put(key);
		newEntry.keyByteBuffer.flip();
		key.position(pos);
		newEntry.hash = hash;
		newEntry.value = value;
		newEntry.next = data[index];
		
		data[index] = newEntry;
		
		count++;

		return null;
	}
	
    /**
     * Associates the specified value with a segment of the specified byte array key in the map.
     * If the map previously contained a mapping for the key segment, the old value is replaced.
     *
     * @param key   the byte array containing the key
     * @param start the starting index of the key segment
     * @param len   the length of the key segment
     * @param value the value to be associated with the specified key segment
     * @return the previous value associated with the key segment, or {@code null} if there was no mapping
     * @throws IllegalArgumentException if the key length exceeds the maximum allowed or the value is {@code null}
     */
	public final E put(byte[] key, int start, int len, E value) {
		
		ensureMaxKeyLength(len);

		ensureNotNull(value);

		int hash = hashCode(key, start, len);

		int index = toArrayIndex(hash);

		Entry<E> e = data[index];

		while(e != null) {

			if (e.hash == hash && equals(e.keyByteBuffer, e.keyLength, key, start, len)) {

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

		newEntry.keyLength = len;
		newEntry.keyByteBuffer.clear();
		newEntry.keyByteBuffer.put(key, start, len);
		newEntry.keyByteBuffer.flip();
		newEntry.hash = hash;
		newEntry.value = value;
		newEntry.next = data[index];
		
		data[index] = newEntry;
		
		count++;

		return null;
	}

    /**
     * Removes the mapping for the specified {@link ByteBuffer} key from the map if present.
     *
     * @param key the {@code ByteBuffer} key whose mapping is to be removed
     * @return the previous value associated with the key, or {@code null} if there was no mapping
     * @throws IllegalArgumentException if the key length exceeds the maximum allowed
     */
	public final E remove(ByteBuffer key) {
		
		ensureMaxKeyLength(key.remaining());

		int hash = hashCode(key);

		int index = toArrayIndex(hash);

		Entry<E> e = data[index];
		Entry<E> prev = null;

		while(e != null) {

			if (e.hash == hash && equals(e.keyByteBuffer, e.keyLength, key)) {

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
     * Removes the mapping for the specified byte array key from the map if present.
     *
     * @param key the byte array key whose mapping is to be removed
     * @return the previous value associated with the key, or {@code null} if there was no mapping
     * @throws IllegalArgumentException if the key length exceeds the maximum allowed
     */
	public final E remove(byte[] key) {
		
		ensureMaxKeyLength(key.length);

		int hash = hashCode(key);

		int index = toArrayIndex(hash);

		Entry<E> e = data[index];
		Entry<E> prev = null;

		while(e != null) {

			if (e.hash == hash && equals(e.keyByteBuffer, e.keyLength, key, 0, key.length)) {

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
     * Removes the mapping for the specified key segment from the map if present.
     *
     * @param key   the byte array containing the key
     * @param start the starting index of the key segment
     * @param len   the length of the key segment
     * @return the previous value associated with the key segment, or {@code null} if there was no mapping
     * @throws IllegalArgumentException if the key length exceeds the maximum allowed
     */
	public final E remove(byte[] key, int start, int len) {
		
		ensureMaxKeyLength(len);

		int hash = hashCode(key, start, len);

		int index = toArrayIndex(hash);

		Entry<E> e = data[index];
		Entry<E> prev = null;

		while(e != null) {

			if (e.hash == hash && equals(e.keyByteBuffer, e.keyLength, key, start, len)) {

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
		public final void reset() {
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

			entry.keyByteBuffer.limit(entry.keyLength).position(0);
			currIteratorKey = entry.keyByteBuffer;

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
