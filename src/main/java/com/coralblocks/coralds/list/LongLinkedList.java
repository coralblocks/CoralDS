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

import java.util.Iterator;

import com.coralblocks.coralds.list.LongLinkedList.LongHolder;
import com.coralblocks.coralpool.ObjectBuilder;
import com.coralblocks.coralpool.ObjectPool;
import com.coralblocks.coralpool.TieredObjectPool;

/**
 * A fast and garbage-free double-linked list of long primitives.
 * 
 * <p><b>NOTE:</b> This data structure is designed on purpose to be used by <b>single-threaded systems</b>, 
 * it will break if used concurrently by multiple threads.</p>
 */
public class LongLinkedList implements Iterable<LongHolder> {
	
	/**
	 * A holder for a long value (primitive) that will be used by the iterator.
	 */
	public static class LongHolder {
		
		long value = 0;
		
		final LongHolder set(long value) {
			this.value = value;
			return this;
		}
		
		/**
		 * Return the long value
		 * 
		 * @return the long value
		 */
		public final long get() {
			return value;
		}
	}
	
	/**
	 * Represents a long value that may be null.
	 */
	public static class NullableLong {
		
		boolean isNull;
		long value;
		
		/**
		 * Returns whether this NullableLong is null.
		 *
		 * @return true if this NullableLong is null; false otherwise
		 */
		public final boolean isNull() {
			return isNull;
		}
		
		/**
		 * Returns the value of this NullableLong if it is not null;
		 * throws NullPointerException otherwise.
		 *
		 * @return the long value
		 * @throws NullPointerException if this NullableLong is null
		 */
		public final long getValue() {
			if (isNull) throw new NullPointerException();
			return value;
		}
		
		final NullableLong set(long value) {
			this.isNull = false;
			this.value = value;
			return this;
		}
		
		final NullableLong nullify() {
			this.isNull = true;
			return this;
		}
	}

	private static class Entry {
		long value = 0;
		Entry next = null;
		Entry prev = null;
	}
	
	private final ObjectPool<Entry> entryPool;
	
	private Entry head = null;
	private Entry tail = null;
	private int size = 0;
	private final NullableLong nullableLong = new NullableLong();

	/**
	 * Creates a LongLinkedList.
	 * 
	 * @param initialCapacity the initial number of preallocated internal entries
	 */
	public LongLinkedList(int initialCapacity) {
		
		ObjectBuilder<Entry> builder = new ObjectBuilder<Entry>() {
			@Override
			public Entry newInstance() {
				return new Entry();
			}
		};

		this.entryPool = new TieredObjectPool<Entry>(initialCapacity, builder);
	}
	
	private Entry getEntryFromPool() {
		return entryPool.get();
	}
	
	private void releaseEntryBackToPool(Entry entry) {
		entry.prev = null;
		entry.next = null;
		entryPool.release(entry);
	}
	
	/**
	 * Clears this list. The list will be empty after this operation.
	 */
	public void clear() {
		while(head != null) {
			Entry saveNext = head.next;
			releaseEntryBackToPool(head);
			head = saveNext;
		}
		tail = null;
		size = 0;
	}
	
	/**
	 * Adds an element to the head of the list.
	 * 
	 * @param value the value to be added
	 */
	public void addFirst(long value) {
		
		Entry entry = getEntryFromPool();
		entry.value = value;
		if (head == null) {
			head = entry;
			tail = entry;
		} else {
			entry.next = head;
			head.prev = entry;
			head = entry;
		}
		size++;
	}
	
	/**
	 * Adds an element to the tail of the list.  (same as {@link #addLast(long)})
	 * 
	 * @param value the value to be added
	 */
	public void add(long value) {
		addLast(value);
	}
	
	/**
	 * Adds an element to the tail of the list.
	 * 
	 * @param value the value to be added
	 */
	public void addLast(long value) {
		
		Entry entry = getEntryFromPool();
		entry.value = value;
		if (tail == null) {
			tail = entry;
			head = entry;
		} else {
			entry.prev = tail;
			tail.next = entry;
			tail = entry;
		}
		size++;
	}
	
	/**
	 * Returns the element on the head of the list.
	 * <p>This method returns a shared NullableLong object, which will be overwritten by subsequent calls.</p>
	 * <p>If the list is empty, the returned NullableLong will have isNull() == true.</p>
	 *
	 * @return a shared NullableLong representing the first element in the list, or a nullified NullableLong if empty
	 */
	public NullableLong first() {
		if (head == null) return nullableLong.nullify();
		return nullableLong.set(head.value);
	}
	
	/**
	 * Removes the element from the head of the list.
	 * <p>This method returns a shared NullableLong object, which will be overwritten by subsequent calls.</p>
	 * <p>If the list is empty, the returned NullableLong will have isNull() == true.</p>
	 *
	 * @return a shared NullableLong representing the removed element, or a nullified NullableLong if empty
	 */
	public NullableLong removeFirst() {
		if (head == null) return nullableLong.nullify();
		Entry entry = head;
		head = head.next;
		if (head != null) head.prev = null;
		long toReturn = entry.value;
		releaseEntryBackToPool(entry);
		if (--size == 0) tail = null;
		return nullableLong.set(toReturn);
	}
	
	/**
	 * Returns the element on the tail of the list.
	 * <p>This method returns a shared NullableLong object, which will be overwritten by subsequent calls.</p>
	 * <p>If the list is empty, the returned NullableLong will have isNull() == true.</p>
	 *
	 * @return a shared NullableLong representing the last element in the list, or a nullified NullableLong if empty
	 */
	public NullableLong last() {
		if (tail == null) return nullableLong.nullify();
		return nullableLong.set(tail.value);
	}
	
	/**
	 * Removes the element from the tail of the list.
	 * <p>This method returns a shared NullableLong object, which will be overwritten by subsequent calls.</p>
	 * <p>If the list is empty, the returned NullableLong will have isNull() == true.</p>
	 *
	 * @return a shared NullableLong representing the removed element, or a nullified NullableLong if empty
	 */
	public NullableLong removeLast() {
		if (tail == null) return nullableLong.nullify();
		Entry entry = tail;
		tail = tail.prev;
		if (tail != null) tail.next = null;
		long toReturn = entry.value;
		releaseEntryBackToPool(entry);
		if (--size == 0) head = null;
		return nullableLong.set(toReturn);
	}
	
	/**
	 * Indicates whether this list is empty (size == 0).
	 * 
	 * @return true if this list is empty, false otherwise
	 */
	public boolean isEmpty() {
		return size == 0;
	}
	
	/**
	 * Returns the number of elements currently present in the list.
	 * 
	 * @return the current size of the list
	 */
	public int size() {
		return size;
	}
	
	private class ReusableIterator implements Iterator<LongHolder> {

		Entry start;
		Entry curr;
		final LongHolder longHolder = new LongHolder();

		void reset() {
			this.start = head;
			this.curr = null;
		}

		@Override
		public final boolean hasNext() {
			return start != null;
		}

		/**
		 * Return the next long
		 * <p>This method returns a shared LongHolder object, which will be overwritten by subsequent calls.</p>
		 */
		@Override
		public final LongHolder next() {

			this.curr = start;
			
			long toReturn = start.value;
			
			start = start.next;

			return longHolder.set(toReturn);
		}

		@Override
		public final void remove() {
			
			boolean isTail = curr == tail;
			boolean isHead = curr == head;

			if (isTail) {
				removeLast();
			} else if (isHead) {
				removeFirst();
			} else {
				curr.prev.next = curr.next;
				curr.next.prev = curr.prev;
				releaseEntryBackToPool(curr);
				size--;
			}
			curr = null;
		}
	}
	
	private ReusableIterator reusableIter = new ReusableIterator();
	
	/**
	 * Returns the same iterator instance (garbage-free operation).
	 * 
	 * @return the same iterator instance for iterating through the list
	 */
	@Override
	public Iterator<LongHolder> iterator() {
		reusableIter.reset();
		return reusableIter;
	}
}