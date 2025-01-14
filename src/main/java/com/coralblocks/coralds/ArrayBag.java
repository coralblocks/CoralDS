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

import com.coralblocks.coralds.list.LinkedList;

/**
 * A generic container that stores elements in an array and allows dynamic resizing. 
 * It provides efficient addition, removal, and iteration over elements. <b>The order of the elements is not guaranteed.</b>
 * There can be duplicates and <code>null</code> values are not permitted.
 * 
 * <p><b>NOTE:</b> This data structure is designed on purpose to be used by <b>single-threaded systems</b>, in other words, 
 *  it will break if used concurrently by multiple threads.</p>
 *
 * @param <E> the type of elements stored in the ArrayBag
 */
public class ArrayBag<E> implements Iterable<E> {
	
	/** The default growth factor */
	public static float DEFAULT_GROWTH_FACTOR = 1.75f;
	
	/* Our LinkedList does not produce any garbage, not even when it grows */
	private final static int SOFT_REFERENCE_LINKED_LIST_INITIAL_SIZE = 32;
	
	private E[] array;
	private LinkedList<SoftReference<E[]>> oldArrays = new LinkedList<>(SOFT_REFERENCE_LINKED_LIST_INITIAL_SIZE);
	private int count = 0;
	private final float growthFactor;
	
    /**
     * Constructs an <code>ArrayBag</code> with a specified initial size and growth factor.
     *
     * @param initialSize the initial size of the internal array
     * @param growthFactor the factor by which the array size increases when full
     */
	@SuppressWarnings("unchecked")
	public ArrayBag(int initialSize, float growthFactor) {
		if (growthFactor <= 1) throw new IllegalArgumentException("growthFactor must be greater than one: " + growthFactor);
		if (initialSize <= 0) throw new IllegalArgumentException("initialSize must be greater than zero: " + initialSize);
		this.array = (E[]) new Object[initialSize];
		this.growthFactor = growthFactor;
	}
	
    /**
     * Constructs an <code>ArrayBag</code> with a specified initial size and the default growth factor.
     *
     * @param initialSize the initial size of the internal array
     */
	public ArrayBag(int initialSize) {
		this(initialSize, DEFAULT_GROWTH_FACTOR);
	}
	
    /**
     * Clears all elements from the <code>ArrayBag</code>. Elements are set to <code>null</code>.
     */
	public final void clear() {
		clear(true);
	}
	
	/**
     * Clears all elements from the <code>ArrayBag</code>. Allows control over whether to nullify the internal array.
     *
     * @param nullify true to nullify all the elements currently in the internal array
     */
	public final void clear(boolean nullify) {
		if (nullify) for(int i = 0; i < count; i++) {
			this.array[i] = null;
		}
		count = 0;
	}
	
	@SuppressWarnings("unchecked")
	private final E[] grow() {
		int newSize = (int) (growthFactor * this.array.length);
		if (newSize == this.array.length) newSize++; 
		E[] oldArray = this.array;
		E[] newArray = (E[]) new Object[newSize];
		System.arraycopy(oldArray, 0, newArray, 0, oldArray.length);
		for(int i = 0; i < oldArray.length; i++) oldArray[i] = null;
		oldArrays.addLast(new SoftReference<E[]>(oldArray));
		return newArray;
	}
	
	/**
     * Clears all soft references to old arrays to free memory.
     */
	public final void clearSoftReferences() {
		oldArrays.clear();
	}
	
    /**
     * Adds an element to the <code>ArrayBag</code>. Duplicates can be added.
     *
     * @param value the element to add
     */
	public final void add(E value) {
		ensureNotNull(value);
		if (count == this.array.length) {
			this.array = grow();
		}
		this.array[count++] = value;
	}
	
    /**
     * Removes the first occurrence of the specified element from the <code>ArrayBag</code>, if it exists.
     *
     * @param value the element to remove
     * @return the removed element, or <code>null</code> if the element was not found
     */
	public final E remove(E value) {
		ensureNotNull(value);
		for(int i = 0; i < count; i++) {
			if (this.array[i].equals(value)) {
				return remove(i);
			}
		}
		return null;
	}
	
    /**
     * Retrieves an element from the <code>ArrayBag</code> that matches the specified value.
     *
     * @param value the element to find
     * @return the matching element, or <code>null</code> if not found
     */
	public final E get(E value) {
		ensureNotNull(value);
		for(int i = 0; i < count; i++) {
			if (this.array[i].equals(value)) {
				return this.array[i];
			}
		}
		return null;
	}
	
    /**
     * Checks if the <code>ArrayBag</code> contains a specified element.
     *
     * @param value the element to check
     * @return true if the element exists in the ArrayBag, false otherwise
     */
	public final boolean contains(E value) {
		return get(value) != null;
	}
	
    /**
     * Returns the number of elements currently stored in the <code>ArrayBag</code>.
     *
     * @return the number of elements in the ArrayBag
     */
	public int size() {
		return count;
	}
	
    /**
     * Checks if the <code>ArrayBag</code> is empty.
     *
     * @return true if the ArrayBag contains no elements, false otherwise
     */
	public boolean isEmpty() {
		return size() == 0;
	}
	
	private final void ensureNotNull(E value) {
		if (value == null) throw new IllegalArgumentException("Method cannot receive null value!");
	}
	
	private final E remove(int index) {
		E toRemove = this.array[index];
		int lastIndex = --count;
		if (index != lastIndex) {
			this.array[index] = this.array[lastIndex];
		}
		this.array[lastIndex] = null;
		return toRemove;
	}
	
	Object[] getArray() {
		return array;
	}
	
	private class ReusableIterator implements Iterator<E> {

		int counter;
		
		public void reset() {
			counter = 0;
		}

		@Override
		public final boolean hasNext() {
			return counter < count;
		}

		@Override
		public final E next() {
			return array[counter++];
		}

		@Override
		public final void remove() {
			ArrayBag.this.remove(--counter);
		}
	}
	
	private ReusableIterator reusableIter = new ReusableIterator();
	
    /**
     * Returns an iterator over the elements in this <code>ArrayBag</code>.
     * <b>The same iterator instance is returned every time.</b>
     * The iterator allows traversal and removal of elements.
     *
     * @return an iterator over the elements in this ArrayBag
     */
	@Override
	public Iterator<E> iterator() {
		reusableIter.reset();
		return reusableIter;
	}
}