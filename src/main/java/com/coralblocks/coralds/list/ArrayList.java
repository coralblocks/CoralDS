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
package com.coralblocks.coralds.list;

import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A resizable, array-based implementation of a list data structure.
 * This implementation automatically grows when needed and
 * maintains references to old arrays using soft references to delay garbage collection.
 * 
 * <p><b>NOTE:</b> This data structure is designed on purpose to be used by <b>single-threaded systems</b>, 
 * it will break if used concurrently by multiple threads.</p>
 *
 * @param <E> the type of elements in this list
 */
public class ArrayList<E> implements Iterable<E> {
	
	/**
	 * The default initial capacity for this list.
	 */
	public static final int DEFAULT_INITIAL_CAPACITY = 64;
	
	/**
	 * The default growth factor for array expansion.
	 */
	public static final float DEFAULT_GROWTH_FACTOR = 1.75f;
	
	private static final int SOFT_REFERENCE_LINKED_LIST_INITIAL_SIZE = 32;
	
	private E[] array;
	private int size = 0;
	private final float growthFactor;
	private final LinkedList<SoftReference<E[]>> oldArrays = new LinkedList<>(SOFT_REFERENCE_LINKED_LIST_INITIAL_SIZE);
	
	/**
	 * Constructs an ArrayList with the default initial capacity and growth factor.
	 */
	public ArrayList() {
		this(DEFAULT_INITIAL_CAPACITY, DEFAULT_GROWTH_FACTOR);
	}
	
	/**
	 * Constructs an ArrayList with the specified initial capacity
	 * and the default growth factor.
	 *
	 * @param initialCapacity the initial capacity
	 * @throws IllegalArgumentException if {@code initialCapacity} is not positive
	 */
	public ArrayList(int initialCapacity) {
		this(initialCapacity, DEFAULT_GROWTH_FACTOR);
	}
	
	/**
	 * Constructs an ArrayList with the default initial capacity
	 * and the specified growth factor.
	 *
	 * @param growthFactor the factor by which the array grows
	 * @throws IllegalArgumentException if {@code growthFactor} is not greater than 1
	 */
	public ArrayList(float growthFactor) {
		this(DEFAULT_INITIAL_CAPACITY, growthFactor);
	}

	/**
	 * Constructs an ArrayList with the specified initial capacity
	 * and growth factor.
	 *
	 * @param initialCapacity the initial capacity
	 * @param growthFactor    the factor by which the array grows
	 * @throws IllegalArgumentException if {@code initialCapacity} is not positive
	 *                                  or {@code growthFactor} is not greater than 1
	 */
	@SuppressWarnings("unchecked")
	public ArrayList(int initialCapacity, float growthFactor) {
		
		if (initialCapacity <= 0) throw new IllegalArgumentException("Bad initial capacity: " + initialCapacity);
		if (growthFactor <= 1f) throw new IllegalArgumentException("Bad growth factor: " + growthFactor);
		
		this.array = (E[]) new Object[initialCapacity];
		this.growthFactor = growthFactor;
	}
	
	private final void checkBounds(int index) {
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
		}
	}
	
	private final void grow() {
		
		E[] oldArray = this.array;
		int oldCapacity = oldArray.length;
		int newCapacity = Math.round(oldCapacity * growthFactor);
		if (newCapacity == oldCapacity) newCapacity++;

		@SuppressWarnings("unchecked")
		E[] newArray = (E[]) new Object[newCapacity];

		// Copy existing elements into the new, larger array
		System.arraycopy(oldArray, 0, newArray, 0, size);
		
		Arrays.fill(oldArray, null); // nullify old array
		oldArrays.addLast(new SoftReference<E[]>(oldArray)); // delay gc
		
		this.array = newArray;
	}
	
	/**
	 * Clears all soft references to old arrays, which may help garbage collection.
	 * Does not affect the currently used array.
	 */
	public void clearSoftReferences() {
		oldArrays.clear();
	}
	
	/**
	 * Remove all elements from this list, making its size equal to zero.
	 */
	public void clear() {
		
		for(int i = 0; i < size; i++) {
			array[i] = null;
		}
		
		size = 0;
	}

	/**
	 * Adds an element at the end of the list. (same as {@link #addLast(Object)})
	 *
	 * @param element the element to be added
	 */
	public void add(E element) {
		
		if (size == array.length) {
			grow();
		}
		
		array[size++] = element;
	}
	
	/**
	 * Adds an element at the end of the list.
	 *
	 * @param element the element to be added
	 */
	public void addLast(E element) {
		add(element);
	}
	
	/**
	 * Inserts an element at the beginning of the list.
	 *
	 * @param element the element to be added
	 */
	public void addFirst(E element) {
		insert(0, element);
	}
	
	/**
	 * Removes the first element of the list.
	 *
	 * @return the removed element
	 * @throws NoSuchElementException if the list is empty
	 */
	public E removeFirst() {
		
		if (size == 0) {
			throw new NoSuchElementException();
		}
		
		return remove(0);
	}
	
	/**
	 * Removes the last element of the list.
	 *
	 * @return the removed element
	 * @throws NoSuchElementException if the list is empty
	 */
	public E removeLast() {
		
		if (size == 0) {
			throw new NoSuchElementException();
		}
		
		E toReturn = array[--size];
		
		array[size] = null;
		
		return toReturn;
	}

	/**
	 * Inserts an element at the specified index. If the specified index equals
	 * the current size, the element is appended to the end. Otherwise the element
	 * is inserted at index and the other elements that come after are shifted to
	 * the right.
	 *
	 * @param index   the position at which to insert the new element
	 * @param element the element to be inserted
	 * @throws IndexOutOfBoundsException if {@code index} is out of range (index &lt; 0 || index &gt; size)
	 */
	public void insert(int index, E element) {
		
		if (index == size) {
			addLast(element);
			return;
		}
		
		checkBounds(index);
		
		if (size == array.length) {
			grow();
		}

		// shift to the right
		System.arraycopy(array, index, array, index + 1, size - index);

		array[index] = element;
		size++;
	}
	
	/**
	 * Retrieves the element at the specified index.
	 *
	 * @param index the index of the element to return
	 * @return the element at the specified index
	 * @throws IndexOutOfBoundsException if {@code index} is out of range (index &lt; 0 || index &ge; size)
	 */
	public E get(int index) {

		checkBounds(index);
		
		return array[index];
	}

	/**
	 * Removes the element at the specified index. Any elements after the removed
	 * element are shifted to the left.
	 *
	 * @param index the index of the element to remove
	 * @return the element previously at the specified index
	 * @throws IndexOutOfBoundsException if {@code index} is out of range (index &lt; 0 || index &ge; size)
	 */
	public E remove(int index) {
		
		checkBounds(index);

		E oldValue = array[index];

		int toShiftLeft = size - index - 1;
		
		if (toShiftLeft > 0) {
			// shift to the left
			System.arraycopy(array, index + 1, array, index, toShiftLeft);
		}
		
		array[--size] = null;

		return oldValue;
	}

	/**
	 * Returns the number of elements in this list.
	 *
	 * @return the size of the list
	 */
	public int size() {
		return size;
	}

	/**
	 * Returns whether this list is empty.
	 *
	 * @return true if the list contains no elements, false otherwise
	 */
	public boolean isEmpty() {
		return size == 0;
	}
	
	private class ReusableIterator implements Iterator<E> {

		int currIndex;
		
		void reset() {
			currIndex = 0;
		}

		@Override
		public final boolean hasNext() {
			return currIndex < size;
		}

		@Override
		public final E next() {
			if (currIndex == size) throw new NoSuchElementException();
			return array[currIndex++];
		}

		@Override
		public final void remove() {
			if (currIndex == 0) throw new NoSuchElementException();
			ArrayList.this.remove(--currIndex);
		}
	}
	
	private ReusableIterator reusableIter = new ReusableIterator();

	/**
	 * Returns an iterator over the elements in this list in proper sequence.
	 * Reuses the same underlying iterator instance, not to create any garbage.
	 *
	 * @return an iterator over the elements in this list
	 */
	@Override
	public Iterator<E> iterator() {
		reusableIter.reset();
		return reusableIter;
	}
}
