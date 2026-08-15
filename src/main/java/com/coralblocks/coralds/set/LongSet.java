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

import com.coralblocks.coralds.map.LongMap;
import com.coralblocks.coralds.util.LongHolder;

/**
 * Represents a set of longs backed by an {@link LongMap}.
 * 
 * <p><b>Null handling:</b> This set stores primitive values, so null elements are not permitted.</p>
 *
 * <p><b>NOTE:</b> This data structure is designed on purpose to be used by <b>single-threaded systems</b>. In other
 * words, it will break if used concurrently by multiple threads.</p>
 */
public class LongSet implements Iterable<LongHolder> {
	
	static class LongHolderImpl implements LongHolder {
		
		long value = 0;
		
		final LongHolder set(long value) {
			this.value = value;
			return this;
		}
		
		@Override
		public long get() {
			return value;
		}
	}
	
	/**
	 * The default initial capacity used by this set.
	 */
	public static int DEFAULT_INITIAL_CAPACITY = LongMap.DEFAULT_INITIAL_CAPACITY;
	
	/**
	 * The default load factor used by this set.
	 */
	public static float DEFAULT_LOAD_FACTOR = LongMap.DEFAULT_LOAD_FACTOR;
	
	private static final Object FILLER = new Object();
	
	private final LongMap<Object> map;
	
	/**
	 * Creates an LongSet with the default initial capacity and load factor.
	 */
	public LongSet() {
		this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
	}

	/**
	 * Creates an LongSet with the specified initial capacity and default load factor.
	 *
	 * @param initialCapacity the initial capacity for the internal map
	 */
	public LongSet(int initialCapacity) {
		this(initialCapacity, DEFAULT_LOAD_FACTOR);
	}

	/**
	 * Creates an LongSet with the specified initial capacity and load factor.
	 *
	 * @param initialCapacity the initial capacity for the internal map
	 * @param loadFactor the load factor for the internal map
	 */
	public LongSet(int initialCapacity, float loadFactor) {
		this.map = new LongMap<Object>(initialCapacity, loadFactor);
	}

	/**
	 * Returns the initial capacity supplied when this set was constructed.
	 *
	 * @return the original initial capacity
	 */
	public int getInitialCapacity() {
		return map.getInitialCapacity();
	}

	/**
	 * Returns the load factor supplied when this set was constructed.
	 *
	 * @return the configured load factor
	 */
	public float getLoadFactor() {
		return map.getLoadFactor();
	}
	
	/**
	 * Adds the specified value to the set if it is not already present.
	 *
	 * @param value the value to add
	 * @return true if the set did not already contain the specified value
	 */
	public boolean add(long value) {
		Object oldValue = map.put(value, FILLER);
		return oldValue == null;
	}
	
	/**
	 * Returns whether the set contains the specified value.
	 *
	 * @param value the value to check
	 * @return true if the set contains the specified value, false otherwise
	 */
	public boolean contains(long value) {
		return map.containsKey(value);
	}
	
	/**
	 * Removes the specified value from the set if it is present.
	 *
	 * @param value the value to remove
	 * @return true if the set contained the specified value, false otherwise
	 */
	public boolean remove(long value) {
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
	 * Compares this set with another LongSet by value.
	 *
	 * @param o the object to compare with this set
	 * @return true when both sets contain the same values
	 */
	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof LongSet)) return false;

		LongSet other = (LongSet) o;
		if (size() != other.size()) return false;

		Iterator<LongHolder> iter = iterator();
		while(iter.hasNext()) {
			if (!other.contains(iter.next().get())) return false;
		}
		return true;
	}

	/**
	 * Returns an order-independent hash code for this set's values.
	 *
	 * @return this set's hash code
	 */
	@Override
	public int hashCode() {
		int hash = 0;
		Iterator<LongHolder> iter = iterator();
		while(iter.hasNext()) {
			hash += Long.hashCode(iter.next().get());
		}
		return hash;
	}

	/**
	 * Returns this set's values.
	 *
	 * @return a readable representation of this set
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		Iterator<LongHolder> iter = iterator();
		boolean first = true;
		while(iter.hasNext()) {
			if (!first) sb.append(", ");
			first = false;
			sb.append(iter.next().get());
		}
		sb.append(']');
		return sb.toString();
	}
	
	/**
	 * Clears soft references in the underlying map.
	 */
	public void clearSoftReferences() {
		map.clearSoftReferences();
	}
	
	private final ReusableIterator reusableIter = new ReusableIterator();
	
	private class ReusableIterator implements Iterator<LongHolder> {
		
		Iterator<Object> iter;
		final LongHolderImpl longHolderImpl = new LongHolderImpl();
		
		void reset() {
			iter = map.iterator();
		}

		@Override
		public boolean hasNext() {
			return iter.hasNext();
		}

		@Override
		public LongHolder next() {
			iter.next();
			return longHolderImpl.set(map.getCurrIteratorKey());
		}

		@Override
		public void remove() {
			iter.remove();
		}
	}

	/**
	 * Returns an iterator over the elements in this set.
	 *
	 * @return an Iterator of LongHolder elements
	 */
	@Override
	public Iterator<LongHolder> iterator() {
		reusableIter.reset();
		return reusableIter;
	}
}
