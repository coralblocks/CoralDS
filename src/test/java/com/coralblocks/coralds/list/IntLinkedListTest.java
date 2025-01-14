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

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.Iterator;

public class IntLinkedListTest {

	@Test
	public void testConstructorAndIsEmpty() {
		IntLinkedList list = new IntLinkedList(10);
		assertTrue("New list should be empty", list.isEmpty());
		assertEquals("Size of new list should be 0", 0, list.size());
	}

	@Test
	public void testAddFirstAndFirst() {
		IntLinkedList list = new IntLinkedList(10);
		list.addFirst(100);
		assertFalse("List should not be empty after addFirst", list.isEmpty());
		assertEquals("List size should be 1 after addFirst", 1, list.size());
		IntLinkedList.NullableInt firstValue = list.first();
		assertFalse("first() should not be null after adding an element", firstValue.isNull());
		assertEquals("The value returned by first() should match the added value", 100, firstValue.getValue());
	}

	@Test
	public void testAddLastAndLast() {
		IntLinkedList list = new IntLinkedList(10);
		list.addLast(200);
		assertFalse("List should not be empty after addLast", list.isEmpty());
		assertEquals("List size should be 1 after addLast", 1, list.size());
		IntLinkedList.NullableInt lastValue = list.last();
		assertFalse("last() should not be null after adding an element", lastValue.isNull());
		assertEquals("The value returned by last() should match the added value", 200, lastValue.getValue());
	}

	@Test
	public void testAddFirstThenAddLast() {
		IntLinkedList list = new IntLinkedList(10);
		list.addFirst(1);
		list.addLast(2);
		list.addLast(3);

		assertEquals("Size should be 3 after adding 3 elements", 3, list.size());
		assertEquals("First element should be 1", 1, list.first().getValue());
		assertEquals("Last element should be 3", 3, list.last().getValue());
	}

	@Test
	public void testRemoveFirst() {
		IntLinkedList list = new IntLinkedList(10);
		// Remove from an empty list
		IntLinkedList.NullableInt removed = list.removeFirst();
		assertTrue("Removing from empty list should result in a nullified NullableInt", removed.isNull());

		list.addFirst(10);
		list.addFirst(20);
		assertEquals("Size should be 2 after adding two elements", 2, list.size());

		removed = list.removeFirst();
		assertFalse("Removed element should not be null", removed.isNull());
		assertEquals("Removed element should be 20 (LIFO from head)", 20, removed.getValue());
		assertEquals("Size should decrease to 1 after removeFirst", 1, list.size());

		removed = list.removeFirst();
		assertFalse("Removed element should not be null", removed.isNull());
		assertEquals("Removed element should be 10", 10, removed.getValue());
		assertEquals("Size should be 0 after removing both elements", 0, list.size());

		removed = list.removeFirst();
		assertTrue("No more elements, removeFirst should return nullified NullableInt", removed.isNull());
	}

	@Test
	public void testRemoveLast() {
		IntLinkedList list = new IntLinkedList(10);
		// Remove from an empty list
		IntLinkedList.NullableInt removed = list.removeLast();
		assertTrue("Removing from empty list should result in a nullified NullableInt", removed.isNull());

		list.addLast(100);
		list.addLast(200);
		assertEquals("Size should be 2 after adding two elements", 2, list.size());

		removed = list.removeLast();
		assertFalse("Removed element should not be null", removed.isNull());
		assertEquals("Removed element should be 200", 200, removed.getValue());
		assertEquals("Size should decrease to 1 after removeLast", 1, list.size());

		removed = list.removeLast();
		assertFalse("Removed element should not be null", removed.isNull());
		assertEquals("Removed element should be 100", 100, removed.getValue());
		assertEquals("Size should be 0 after removing both elements", 0, list.size());

		removed = list.removeLast();
		assertTrue("No more elements, removeLast should return nullified NullableInt", removed.isNull());
	}

	@Test
	public void testClear() {
		IntLinkedList list = new IntLinkedList(10);
		list.addFirst(10);
		list.addLast(20);
		list.addLast(30);
		assertEquals("Size should be 3 before clear", 3, list.size());

		list.clear();
		assertEquals("Size should be 0 after clear", 0, list.size());
		assertTrue("List should be empty after clear", list.isEmpty());

		// Ensure we can still add after clear
		list.addFirst(40);
		assertEquals("Size should be 1 after adding an element post-clear", 1, list.size());
		assertFalse("List should not be empty", list.isEmpty());
	}

	@Test
	public void testSizeAndIsEmpty() {
		IntLinkedList list = new IntLinkedList(10);
		assertTrue("New list should be empty", list.isEmpty());
		assertEquals("New list size should be 0", 0, list.size());

		list.addFirst(1);
		list.addLast(2);
		assertFalse("List should not be empty after additions", list.isEmpty());
		assertEquals("List size should be 2", 2, list.size());

		list.removeFirst();
		assertEquals("List size should be 1 after removeFirst", 1, list.size());
		list.removeLast();
		assertEquals("List size should be 0 after removeLast", 0, list.size());
		assertTrue("List should be empty again", list.isEmpty());
	}

	@Test
	public void testFirstAndLastOnEmptyList() {
		IntLinkedList list = new IntLinkedList(10);

		IntLinkedList.NullableInt first = list.first();
		assertTrue("first() on empty list should return a nullified NullableInt", first.isNull());

		IntLinkedList.NullableInt last = list.last();
		assertTrue("last() on empty list should return a nullified NullableInt", last.isNull());
	}

	@Test
	public void testIteratorBasic() {
		IntLinkedList list = new IntLinkedList(10);
		list.addLast(10);
		list.addLast(20);
		list.addLast(30);

		Iterator<IntLinkedList.IntHolder> it = list.iterator();
		assertTrue("Iterator should have next (first call)", it.hasNext());
		assertEquals("First element should be 10", 10, it.next().get());
		assertTrue("Iterator should have next (second call)", it.hasNext());
		assertEquals("Second element should be 20", 20, it.next().get());
		assertTrue("Iterator should have next (third call)", it.hasNext());
		assertEquals("Third element should be 30", 30, it.next().get());
		assertFalse("Iterator should not have next after last element", it.hasNext());
	}

	@Test
	public void testIteratorRemove() {
		IntLinkedList list = new IntLinkedList(10);
		list.addLast(10);
		list.addLast(20);
		list.addLast(30);

		Iterator<IntLinkedList.IntHolder> it = list.iterator();
		assertTrue(it.hasNext());
		assertEquals(10, it.next().get());
		it.remove(); // remove 10
		assertEquals("Size should be 2 after removing first element via iterator", 2, list.size());

		assertTrue(it.hasNext());
		assertEquals(20, it.next().get());
		it.remove(); // remove 20
		assertEquals("Size should be 1 after removing second element via iterator", 1, list.size());

		assertTrue(it.hasNext());
		assertEquals(30, it.next().get());
		it.remove(); // remove 30
		assertEquals("Size should be 0 after removing last element via iterator", 0, list.size());

		assertFalse("Iterator should have no more elements", it.hasNext());
	}

	@Test
	public void testIteratorRemoveHeadAndTail() {
		// This specifically tests removing head/tail when iterating, ensuring internal link updates
		IntLinkedList list = new IntLinkedList(10);
		list.addLast(1);
		list.addLast(2);
		list.addLast(3);

		Iterator<IntLinkedList.IntHolder> it = list.iterator();
		// Remove head
		assertEquals(1, it.next().get());
		it.remove(); // remove 1 (head)
		assertEquals("Size after removing head via iterator", 2, list.size());

		// Move next
		assertTrue(it.hasNext());
		assertEquals(2, it.next().get());
		assertTrue(it.hasNext());
		assertEquals(3, it.next().get());
		// Now at the end, remove tail
		it.remove(); // remove 3 (tail)
		assertEquals("Size after removing tail via iterator", 1, list.size());

		// Only middle remains
		assertEquals("Remaining element should be 2 as both head(1) and tail(3) are removed", 2, list.first().getValue());
		assertEquals("Remaining element should also be 2 as it's both head and tail", 2, list.last().getValue());
	}
}
