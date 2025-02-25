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
import com.coralblocks.coralds.map.IdentityMap;

/**
 * A set implementation that uses identity equality (==) for comparing its elements.
 * This set is backed by an {@link com.coralblocks.coralds.map.IdentityMap}.
 *
 * @param <E> the type of elements maintained by this set
 */
public class IdentitySet<E> implements Iterable<E> {

	private final IdentityMap<E,E> map;

	/**
	 * Constructs a new <code>IdentitySet</code> with the specified initial capacity and load factor.
	 *
	 * @param initialCapacity the initial capacity of the underlying map
	 * @param loadFactor the load factor for the underlying map
	 */
	public IdentitySet(int initialCapacity, float loadFactor) {
		this.map = new IdentityMap<E,E>(initialCapacity, loadFactor);
	}

	/**
	 * Constructs a new <code>IdentitySet</code> with the specified initial capacity and a default load factor.
	 *
	 * @param initialCapacity the initial capacity of the underlying map
	 */
	public IdentitySet(int initialCapacity) {
		this.map = new IdentityMap<E,E>(initialCapacity);
	}

	/**
	 * Constructs a new <code>IdentitySet</code> with default initial capacity and load factor.
	 */
	public IdentitySet() {
		this.map = new IdentityMap<E,E>();
	}

	/**
	 * Returns the number of elements in this set.
	 *
	 * @return the number of elements in the set
	 */
	public int size() {
		return map.size();
	}

	/**
	 * Returns {@code true} if this set contains no elements.
	 *
	 * @return {@code true} if this set contains no elements, {@code false} otherwise
	 */
	public boolean isEmpty() {
		return map.isEmpty();
	}

	/**
	 * Returns {@code true} if this set contains the specified element.
	 *
	 * @param value the element whose presence in this set is to be tested
	 * @return {@code true} if this set contains the specified element
	 */
	public boolean contains(E value) {
		return map.containsKey(value);
	}

	/**
	 * Adds the specified element to this set if it is not already present.
	 *
	 * @param key the element to be added to this set
	 * @return {@code true} if the set did not already contain the specified element
	 */
	public boolean add(E key) {
		return map.put(key, key) == null;
	}
	
	/**
	 * Removes the specified element from this set if it is present.
	 *
	 * @param key the element to be removed from this set, if present
	 * @return {@code true} if the element was present and removed
	 */
	public boolean remove(E key) {
		return map.remove(key) != null;
	}

	/**
	 * Removes all of the elements from this set.
	 */
	public void clear() {
		map.clear();
	}

	/**
	 * Returns an iterator over the elements in this set.
	 * Note that the same instance of the iterator is returned every time to avoid creating garbage for the GC.
	 *
	 * @return an iterator over the elements in this set
	 */
	@Override
	public Iterator<E> iterator() {
		return map.iterator();
	}
}