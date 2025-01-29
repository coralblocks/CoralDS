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
package com.coralblocks.coralds.set;

import java.util.Iterator;

import com.coralblocks.coralds.map.Map;

/**
 * A set implementation built on top of a {@link Map} for storing unique elements.
 *
 * @param <E> the type of elements maintained by this Set
 */
public class Set<E> implements Iterable<E> {
	
	/**
	 * The default initial capacity for the Set.
	 */
	public static int DEFAULT_INITIAL_CAPACITY = Map.DEFAULT_INITIAL_CAPACITY;
	
	/**
	 * The default load factor for the Set.
	 */
	public static float DEFAULT_LOAD_FACTOR = Map.DEFAULT_LOAD_FACTOR;
	
	private static final Object FILLER = new Object();
	
	private final Map<E, Object> map;
	
	/**
	 * Constructs a Set with the default initial capacity and load factor.
	 */
	public Set() {
		this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
	}

	/**
	 * Constructs a Set with the specified initial capacity and the default load factor.
	 *
	 * @param initialCapacity the initial capacity for the Set
	 */
	public Set(int initialCapacity) {
		this(initialCapacity, DEFAULT_LOAD_FACTOR);
	}

	/**
	 * Constructs a Set with the specified initial capacity and load factor.
	 *
	 * @param initialCapacity the initial capacity for the Set
	 * @param loadFactor the load factor for the Set
	 */
	public Set(int initialCapacity, float loadFactor) {
		this.map = new Map<E, Object>(initialCapacity, loadFactor);
	}
	
	/**
	 * Adds the specified value to this Set if it is not already present.
	 *
	 * @param value the value to be added
	 * @return true if this Set did not already contain the specified value
	 */
	public boolean add(E value) {
		Object oldValue = map.put(value, FILLER);
		return oldValue == null;
	}
	
	/**
	 * Returns true if this Set contains the specified value.
	 *
	 * @param value the value to be checked
	 * @return true if this Set contains the value; false otherwise
	 */
	public boolean contains(E value) {
		return map.containsKey(value);
	}
	
	/**
	 * Removes the specified value from this Set if it is present.
	 *
	 * @param value the value to be removed
	 * @return true if the value was removed
	 */
	public boolean remove(E value) {
		Object removed = map.remove(value);
		return removed != null;
	}
	
	/**
	 * Removes all of the elements from this Set.
	 */
	public void clear() {
		map.clear();
	}
	
	/**
	 * Returns the number of elements in this Set.
	 *
	 * @return the number of elements in this Set
	 */
	public int size() {
		return map.size();
	}
	
	/**
	 * Returns true if this Set contains no elements.
	 *
	 * @return true if this Set is empty; false otherwise
	 */
	public boolean isEmpty() {
		return map.isEmpty();
	}
	
	/**
	 * Clears any soft references from the underlying {@link Map} structure.
	 */
	public void clearSoftReferences() {
		map.clearSoftReferences();
	}
	
	private final ReusableIterator reusableIter = new ReusableIterator();
	
	private class ReusableIterator implements Iterator<E> {
		
		Iterator<Object> iter;
		
		void reset() {
			iter = map.iterator();
		}

		@Override
		public boolean hasNext() {
			return iter.hasNext();
		}

		@Override
		public E next() {
			iter.next();
			return map.getCurrIteratorKey();
		}

		@Override
		public void remove() {
			iter.remove();
		}
	}

	/**
	 * Returns an iterator over the elements in this Set.
	 *
	 * @return an iterator over the elements in this Set
	 */
	@Override
	public Iterator<E> iterator() {
		reusableIter.reset();
		return reusableIter;
	}
}
