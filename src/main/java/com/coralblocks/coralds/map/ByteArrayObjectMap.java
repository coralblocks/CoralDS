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
import com.coralblocks.coralds.holder.ByteArrayHolder;
import com.coralblocks.coralds.util.MathUtils;
import com.coralblocks.coralpool.ArrayObjectPool;
import com.coralblocks.coralpool.ObjectPool;
import com.coralblocks.coralpool.util.Builder;

public class ByteArrayObjectMap<E> implements Iterable<E> {

	public static final int DEFAULT_INITIAL_CAPACITY = 128;
	public static final float DEFAULT_LOAD_FACTOR = 0.80f;
	public static final short DEFAULT_MAX_KEY_LENGTH = 256;
	private static final int SOFT_REFERENCE_LINKED_LIST_INITIAL_SIZE = 32;
	
	static class Entry<T> {
		final byte[] key;
		int keyLength;
		int hash;
		T value;
		Entry<T> next;
		
		Entry(int maxKeyLength) {
			this.key = new byte[maxKeyLength];
		}
	}

	private Entry<E>[] data;
	private int lengthMinusOne;
	private int length;
	private int count;
	private int threshold;
	private float loadFactor;
	private final ByteArrayHolder currIteratorKey = new ByteArrayHolder();
	private final boolean isPowerOfTwo;
	private final int maxKeyLength;
	private final ByteArrayHolder byteArrayHolder = new ByteArrayHolder();
	
	private final ObjectPool<Entry<E>> entryPool;

	private ReusableIterator reusableIter = new ReusableIterator();
	
	private final LinkedObjectList<SoftReference<Entry<E>[]>> oldArrays = new LinkedObjectList<>(SOFT_REFERENCE_LINKED_LIST_INITIAL_SIZE);
	
	public ByteArrayObjectMap() {
		this(DEFAULT_INITIAL_CAPACITY, DEFAULT_MAX_KEY_LENGTH, DEFAULT_LOAD_FACTOR);
	}

	public ByteArrayObjectMap(int initialCapacity) {
		this(initialCapacity, DEFAULT_MAX_KEY_LENGTH, DEFAULT_LOAD_FACTOR);
	}
	
	public ByteArrayObjectMap(int initialCapacity, short maxKeyLength) {
		this(initialCapacity, maxKeyLength, DEFAULT_LOAD_FACTOR);
	}
	
	public ByteArrayObjectMap(int initialCapacity, float loadFactor) {
		this(initialCapacity, DEFAULT_MAX_KEY_LENGTH, loadFactor);
	}
	
	public ByteArrayObjectMap(short maxKeyLength) {
		this(DEFAULT_INITIAL_CAPACITY, maxKeyLength, DEFAULT_LOAD_FACTOR);
	}
	
	public ByteArrayObjectMap(short maxKeyLength, float loadFactor) {
		this(DEFAULT_INITIAL_CAPACITY, maxKeyLength, loadFactor);
	}
	
	public ByteArrayObjectMap(float loadFactor) {
		this(DEFAULT_INITIAL_CAPACITY, DEFAULT_MAX_KEY_LENGTH, loadFactor);
	}
	
	@SuppressWarnings("unchecked")
	public ByteArrayObjectMap(int initialCapacity, short maxKeyLength, float loadFactor) {
		
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
	
	private static final boolean equals(byte[] internalKey, 
										int internalKeyLength, 
										byte[] externalKey, 
										int externalKeyStart, 
										int externalKeyLength) {
		
		if (internalKeyLength != externalKeyLength) return false;
		
		for(int i = 0; i < internalKeyLength; i++) {
			if (internalKey[i] != externalKey[externalKeyStart + i]) return false;
		}
		
		return true;
	}
	
	private static final boolean equals(byte[] internalKey, 
										int internalKeyLength, 
										ByteBuffer externalKey) { 

		if (internalKeyLength != externalKey.remaining()) return false;
		
		final int pos = externalKey.position();

		for(int i = 0; i < internalKeyLength; i++) {
			if (internalKey[i] != externalKey.get(pos + i)) return false;
		}

		return true;
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
	
	public final int size() {
		return count;
	}

	public final ByteArrayHolder getCurrIteratorKey() {
		return currIteratorKey;
	}

	public final boolean isEmpty() {
		return size() == 0;
	}

	public final ByteArrayHolder contains(E value) {

		ensureNotNull(value);

		for(int i = data.length - 1; i >= 0; i--) {

			Entry<E> e = data[i];

			while(e != null) {

				if (e.value.equals(value)) {

					return byteArrayHolder.set(e.key, e.keyLength);
				}

				e = e.next;
			}
		}
		
		return byteArrayHolder.clear();
	}

	public final boolean containsKey(byte[] key) {
		
		return get(key) != null;
	}
	
	public final boolean containsKey(ByteBuffer key) {
		
		return get(key) != null;
	}

	public final boolean containsKey(byte[] key, int start, int len) {

		return get(key, start, len) != null;
	}
	
	private final void ensureNotNull(E value) {
		if (value == null) throw new IllegalArgumentException("Method cannot receive null value!");
	}
	
	private final void ensureMaxKeyLength(int len) {
		if (len > maxKeyLength) throw new IllegalArgumentException("Key is too big: " + len + " (max=" + maxKeyLength + ")");
	}

	public final E get(byte[] key) {
		
		ensureMaxKeyLength(key.length);

		int hash = hashCode(key);
		
		int index = toArrayIndex(hash);

		Entry<E> e = data[index];

		while(e != null) {

			if (e.hash == hash && equals(e.key, e.keyLength, key, 0, key.length)) {

				return e.value;
			}

			e = e.next;
		}
		
		return null;
	}
	
	public final E get(ByteBuffer key) {
		
		ensureMaxKeyLength(key.remaining());

		int hash = hashCode(key);
		
		int index = toArrayIndex(hash);

		Entry<E> e = data[index];

		while(e != null) {

			if (e.hash == hash && equals(e.key, e.keyLength, key)) {

				return e.value;
			}

			e = e.next;
		}
		
		return null;
	}

	public final E get(byte[] key, int start, int len) {
		
		ensureMaxKeyLength(len);

		int hash = hashCode(key, start, len);
		
		int index = toArrayIndex(hash);

		Entry<E> e = data[index];

		while(e != null) {

			if (e.hash == hash && equals(e.key, e.keyLength, key, start, len)) {

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

	public final E put(byte[] key, E value) {
		
		ensureMaxKeyLength(key.length);

		ensureNotNull(value);
		
		int hash = hashCode(key);

		int index = toArrayIndex(hash);

		Entry<E> e = data[index];

		while(e != null) {

			if (e.hash == hash && equals(e.key, e.keyLength, key, 0, key.length)) {

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
		System.arraycopy(key, 0, newEntry.key, 0, key.length);
		newEntry.hash = hash;
		newEntry.value = value;
		newEntry.next = data[index];
		
		data[index] = newEntry;
		
		count++;

		return null;
	}
	
	public final E put(ByteBuffer key, E value) {
		
		ensureMaxKeyLength(key.remaining());

		ensureNotNull(value);
		
		int hash = hashCode(key);

		int index = toArrayIndex(hash);

		Entry<E> e = data[index];

		while(e != null) {

			if (e.hash == hash && equals(e.key, e.keyLength, key)) {

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
		key.get(newEntry.key, 0, newEntry.keyLength);
		key.position(pos);
		newEntry.hash = hash;
		newEntry.value = value;
		newEntry.next = data[index];
		
		data[index] = newEntry;
		
		count++;

		return null;
	}
	
	public final E put(byte[] key, int start, int len, E value) {
		
		ensureMaxKeyLength(len);

		ensureNotNull(value);

		int hash = hashCode(key, start, len);

		int index = toArrayIndex(hash);

		Entry<E> e = data[index];

		while(e != null) {

			if (e.hash == hash && equals(e.key, e.keyLength, key, start, len)) {

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
		System.arraycopy(key, start, newEntry.key, 0, len);
		newEntry.hash = hash;
		newEntry.value = value;
		newEntry.next = data[index];
		
		data[index] = newEntry;
		
		count++;

		return null;
	}

	public final E remove(ByteBuffer key) {
		
		ensureMaxKeyLength(key.remaining());

		int hash = hashCode(key);

		int index = toArrayIndex(hash);

		Entry<E> e = data[index];
		Entry<E> prev = null;

		while(e != null) {

			if (e.hash == hash && equals(e.key, e.keyLength, key)) {

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
	
	public final E remove(byte[] key) {
		
		ensureMaxKeyLength(key.length);

		int hash = hashCode(key);

		int index = toArrayIndex(hash);

		Entry<E> e = data[index];
		Entry<E> prev = null;

		while(e != null) {

			if (e.hash == hash && equals(e.key, e.keyLength, key, 0, key.length)) {

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

	public final E remove(byte[] key, int start, int len) {
		
		ensureMaxKeyLength(len);

		int hash = hashCode(key, start, len);

		int index = toArrayIndex(hash);

		Entry<E> e = data[index];
		Entry<E> prev = null;

		while(e != null) {

			if (e.hash == hash && e.key.length == len && equals(e.key, e.keyLength, key, start, len)) {

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

			currIteratorKey.set(entry.key, entry.keyLength);

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

	@Override
	public Iterator<E> iterator() {
		reusableIter.reset();
		return reusableIter;
	}
}
