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

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.coralblocks.coralds.holder.CharHolder;

/**
 * A map implementation that uses 1-byte chars as keys and is backed by a fixed-size array of 256 elements,
 * providing constant-time access performance. This implementation does not accept <code>null</code> values
 * but can store any non-null object type specified by the generic parameter.
 * 
 * @param <E> the entry type this hash map will hold
 */
public final class CharObjectMap<E> implements Iterable<E> {

	@SuppressWarnings("unchecked")
	private final E[] data = (E[]) new Object[256];
	
	private final ReusableIterator iter = new ReusableIterator();
	
	private int count = 0;
	
	private char currIteratorKey;
	
	private final CharHolder charHolder = new CharHolder();
	
    /**
     * Constructs an empty <code>CharObjectMap</code>.
     */
	public CharObjectMap() {
		
	}

	private final int convert(char key) {
		return ((byte) key) & 0xff;
	}
	
	/**
	 * Does the map contain the given value?
	 * 
	 * @param value the value to be checked
	 * @return a CharHolder that might contain the key for the given value or nothing to indicate that the value is not present
	 */
	public final CharHolder contains(E value) {
		
		ensureNotNull(value);
		
		for(int i = 0; i < data.length; i++) {
			if (value.equals(data[i])) return charHolder.set((char) i);
		}
		
		return charHolder.clear();
	}

    /**
     * Checks if the map contains a mapping for the specified key.
     *
     * @param key the char key whose presence in this map is to be tested
     * @return true if this map contains a mapping for the specified key
     */
	public final boolean containsKey(char key) {
		return data[convert(key)] != null;
	}

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for the key, the old value
     * is replaced and returned.
     *
     * @param key the char key with which the specified value is to be associated
     * @param value the value to be associated with the specified key (must not be null)
     * @return the previous value associated with the key, or null if there was no mapping for the key
     * @throws NullPointerException if the specified value is null
     */
	public final E put(char key, E value) {

		ensureNotNull(value);
		
		int index = convert(key);
		E old = data[index];
		data[index] = value;
		if (old == null) {
			// not replacing...
			count++;
		}
		return old;
	}
	
	private final void ensureNotNull(E value) {
		if (value == null) throw new IllegalArgumentException("Method cannot receive null value!");
	}

    /**
     * Returns the value to which the specified key is mapped.
     *
     * @param key the char key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or null if this map contains no mapping for the key
     */
	public final E get(char key) {
		return data[convert(key)];
	}

    /**
     * Removes the mapping for the specified key if present.
     *
     * @param key the char key whose mapping is to be removed from the map
     * @return the previous value associated with the key, or null if there was no mapping for the key
     */
	public final E remove(char key) {
		int index = convert(key);
		E old = data[index];
		data[index] = null;
		if (old != null) {
			// really removing something...
			count--;
		}
		return old;
	}
	
	/**
	 * When using the Iterator for this <code>CharObjectMap</code>, this method will return the current key of the last 
	 * element returned by Iterator.next().
	 * 
	 * @return the current key of the last iterated element
	 */
	public final char getCurrIteratorKey() {
		return currIteratorKey;
	}

	private class ReusableIterator implements Iterator<E> {

		int index = 0;
		int position = 0;
		int size;

		public void reset() {
			this.index = 0;
			this.position = 0;
			this.size = count;
		}

		@Override
		public final boolean hasNext() {
			return position < size;
		}

		@Override
		public final E next() {
			
			if (position >= size) {
				throw new NoSuchElementException();
			}
			
			E e = null;
			while(e == null) {
				e = data[index];
				index++;
			}
			currIteratorKey = (char) (index - 1);
			position++;
			return e;
		}

		@Override
		public void remove() {
			if (index == 0 || data[index - 1] == null) {
				throw new NoSuchElementException();
			}
			data[index - 1] = null;
			count--;
		}
	}
	
	/**
	 * Returns the same instance of the iterator (garbage-free)
	 * 
	 * @return the same instance of the iterator
	 */
	@Override
	public final Iterator<E> iterator() {
		iter.reset();
		return iter;
	}

    /**
     * Returns true if this map contains no key-value mappings.
     *
     * @return true if this map contains no key-value mappings,
     */
	public final boolean isEmpty() {
		return count == 0;
	}

    /**
     * Removes all mappings from this map, leaving it empty.
     */
	public final void clear() {
		for (int i = 0; i < data.length; i++) {
			data[i] = null;
		}
		count = 0;
	}

    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of key-value mappings in this map
     */
	public final int size() {
		return count;
	}
}
