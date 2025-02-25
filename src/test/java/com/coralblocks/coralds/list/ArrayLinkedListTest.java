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

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Iterator;

public class ArrayLinkedListTest {

	@Test
	public void testEmptyList() {
		ArrayLinkedList<Integer> list = new ArrayLinkedList<>(3);
		assertEquals(0, list.size());
		assertNull(list.removeLast());
		Iterator<Integer> it = list.iterator();
		assertFalse(it.hasNext());
	}

	@Test
	public void testAddWithinArray() {
		ArrayLinkedList<String> list = new ArrayLinkedList<>(3);
		list.addLast("a");
		list.addLast("b");
		assertEquals(2, list.size());
		Iterator<String> it = list.iterator();
		assertTrue(it.hasNext());
		assertEquals("a", it.next());
		assertEquals("b", it.next());
		assertFalse(it.hasNext());
		// Remove elements from the array portion
		assertEquals("b", list.removeLast());
		assertEquals(1, list.size());
		assertEquals("a", list.removeLast());
		assertEquals(0, list.size());
		assertNull(list.removeLast());
	}

	@Test
	public void testOverflowToLinkedList() {
		ArrayLinkedList<String> list = new ArrayLinkedList<>(3);
		// Add more elements than the array capacity (3)
		list.addLast("a"); // stored in array[0]
		list.addLast("b"); // stored in array[1]
		list.addLast("c"); // stored in array[2]
		list.addLast("d"); // stored in linkedList
		list.addLast("e"); // stored in linkedList
		assertEquals(5, list.size());
		
		// Verify iterator traverses all elements in insertion order
		Iterator<String> it = list.iterator();
		assertTrue(it.hasNext());
		assertEquals("a", it.next());
		assertEquals("b", it.next());
		assertEquals("c", it.next());
		assertEquals("d", it.next());
		assertEquals("e", it.next());
		assertFalse(it.hasNext());
		
		// Test removal order (should be LIFO)
		assertEquals("e", list.removeLast()); // from linkedList
		assertEquals("d", list.removeLast()); // from linkedList
		assertEquals("c", list.removeLast()); // from array
		assertEquals("b", list.removeLast());
		assertEquals("a", list.removeLast());
		assertNull(list.removeLast());
	}

	@Test
	public void testClearWithNullify() {
		ArrayLinkedList<Integer> list = new ArrayLinkedList<>(3);
		list.addLast(1);
		list.addLast(2);
		list.addLast(3);
		list.addLast(4); // overflow to linkedList
		assertEquals(4, list.size());
		list.clear(true);
		assertEquals(0, list.size());
		// After clear, removeLast should return null
		assertNull(list.removeLast());
		// Iterator should not return any elements
		Iterator<Integer> it = list.iterator();
		assertFalse(it.hasNext());
	}

	@Test
	public void testClearWithoutNullify() {
		ArrayLinkedList<Integer> list = new ArrayLinkedList<>(3);
		list.addLast(10);
		list.addLast(20);
		list.addLast(30);
		list.addLast(40); // overflow to linkedList
		assertEquals(4, list.size());
		list.clear(false);
		assertEquals(0, list.size());
		assertNull(list.removeLast());
	}

	@Test
	public void testIteratorMultipleCalls() {
		ArrayLinkedList<String> list = new ArrayLinkedList<>(3);
		list.addLast("x");
		list.addLast("y");
		list.addLast("z");
		// First iterator call
		Iterator<String> it1 = list.iterator();
		assertTrue(it1.hasNext());
		assertEquals("x", it1.next());
		assertEquals("y", it1.next());
		assertEquals("z", it1.next());
		assertFalse(it1.hasNext());
		// Second iterator call should reset the iterator
		Iterator<String> it2 = list.iterator();
		assertTrue(it2.hasNext());
		assertEquals("x", it2.next());
		assertEquals("y", it2.next());
		assertEquals("z", it2.next());
		assertFalse(it2.hasNext());
		// Verify that the same iterator instance is returned
		assertSame(it2, list.iterator());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testIteratorRemoveUnsupported() {
		ArrayLinkedList<Integer> list = new ArrayLinkedList<>(3);
		list.addLast(100);
		Iterator<Integer> it = list.iterator();
		// Attempt to remove via iterator should throw exception
		it.remove();
	}

	@Test
	public void testAddRemoveEdgeCases() {
		ArrayLinkedList<String> list = new ArrayLinkedList<>(2);
		// Removing from an empty list
		assertNull(list.removeLast());
		
		// Add one element and remove it
		list.addLast("only");
		assertEquals(1, list.size());
		assertEquals("only", list.removeLast());
		assertEquals(0, list.size());
		
		// Add and remove in mixed order
		list.addLast("first");
		list.addLast("second");
		assertEquals(2, list.size());
		assertEquals("second", list.removeLast());
		list.addLast("third");
		assertEquals(2, list.size());
		// Iterator should return "first" then "third"
		Iterator<String> it = list.iterator();
		assertEquals("first", it.next());
		assertEquals("third", it.next());
		assertFalse(it.hasNext());
	}
}
