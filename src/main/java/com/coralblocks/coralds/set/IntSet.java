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

import com.coralblocks.coralds.map.IntMap;
import com.coralblocks.coralds.util.IntHolder;

/**
 * Represents a set of integers backed by an {@link IntMap}.
 */
public class IntSet implements Iterable<IntHolder> {
	
	static class IntHolderImpl implements IntHolder {
		
		int value = 0;
		
		final IntHolder set(int value) {
			this.value = value;
			return this;
		}
		
		@Override
		public int get() {
			return value;
		}
	}
	
	/**
	 * The default initial capacity used by this set.
	 */
	public static int DEFAULT_INITIAL_CAPACITY = IntMap.DEFAULT_INITIAL_CAPACITY;
	
	/**
	 * The default load factor used by this set.
	 */
	public static float DEFAULT_LOAD_FACTOR = IntMap.DEFAULT_LOAD_FACTOR;
	
	private static final Object FILLER = new Object();
	
	private final IntMap<Object> map;
	
	/**
	 * Creates an IntSet with the default initial capacity and load factor.
	 */
	public IntSet() {
		this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
	}

	/**
	 * Creates an IntSet with the specified initial capacity and default load factor.
	 *
	 * @param initialCapacity the initial capacity for the internal map
	 */
	public IntSet(int initialCapacity) {
		this(initialCapacity, DEFAULT_LOAD_FACTOR);
	}

	/**
	 * Creates an IntSet with the specified initial capacity and load factor.
	 *
	 * @param initialCapacity the initial capacity for the internal map
	 * @param loadFactor the load factor for the internal map
	 */
	public IntSet(int initialCapacity, float loadFactor) {
		this.map = new IntMap<Object>(initialCapacity, loadFactor);
	}
	
	/**
	 * Adds the specified value to the set if it is not already present.
	 *
	 * @param value the value to add
	 * @return true if the set did not already contain the specified value
	 */
	public boolean add(int value) {
		Object oldValue = map.put(value, FILLER);
		return oldValue == null;
	}
	
	/**
	 * Returns whether the set contains the specified value.
	 *
	 * @param value the value to check
	 * @return true if the set contains the specified value, false otherwise
	 */
	public boolean contains(int value) {
		return map.containsKey(value);
	}
	
	/**
	 * Removes the specified value from the set if it is present.
	 *
	 * @param value the value to remove
	 * @return true if the set contained the specified value, false otherwise
	 */
	public boolean remove(int value) {
		Object removed = map.remove(value);
		return removed != null;
	}
	
	/**
	 * Removes all elements from this set.
	 */
	public void clear() {
		map.clear();
	}
	
	/**
	 * Returns the number of elements in this set.
	 *
	 * @return the size of this set
	 */
	public int size() {
		return map.size();
	}
	
	/**
	 * Returns whether the set is empty.
	 *
	 * @return true if the set is empty, false otherwise
	 */
	public boolean isEmpty() {
		return map.isEmpty();
	}
	
	/**
	 * Clears soft references in the underlying map.
	 */
	public void clearSoftReferences() {
		map.clearSoftReferences();
	}
	
	private final ReusableIterator reusableIter = new ReusableIterator();
	
	private class ReusableIterator implements Iterator<IntHolder> {
		
		Iterator<Object> iter;
		final IntHolderImpl intHolderImpl = new IntHolderImpl();
		
		void reset() {
			iter = map.iterator();
		}

		@Override
		public boolean hasNext() {
			return iter.hasNext();
		}

		@Override
		public IntHolder next() {
			iter.next();
			return intHolderImpl.set(map.getCurrIteratorKey());
		}

		@Override
		public void remove() {
			iter.remove();
		}
	}

	/**
	 * Returns an iterator over the elements in this set.
	 *
	 * @return an Iterator of IntHolder elements
	 */
	@Override
	public Iterator<IntHolder> iterator() {
		reusableIter.reset();
		return reusableIter;
	}
}
