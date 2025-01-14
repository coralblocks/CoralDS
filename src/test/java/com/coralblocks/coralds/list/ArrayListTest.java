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
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ArrayListTest {

	private ArrayList<String> list;

	@Before
	public void setUp() {
		list = new ArrayList<>();
	}

	@Test
	public void testDefaultConstructor() {
		assertTrue(list.isEmpty());
		assertEquals(0, list.size());
	}

	@Test
	public void testConstructorWithInitialCapacity() {
		ArrayList<Integer> customList = new ArrayList<>(128);
		assertTrue(customList.isEmpty());
		assertEquals(0, customList.size());
	}

	@Test
	public void testConstructorWithGrowthFactor() {
		ArrayList<Integer> customList = new ArrayList<>(2.0f);
		assertTrue(customList.isEmpty());
		assertEquals(0, customList.size());
	}

	@Test
	public void testConstructorWithBothParams() {
		ArrayList<Integer> customList = new ArrayList<>(32, 1.8f);
		assertTrue(customList.isEmpty());
		assertEquals(0, customList.size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorWithInvalidCapacity() {
		new ArrayList<>(0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorWithInvalidGrowthFactor() {
		new ArrayList<>(64, 1.0f);
	}

	@Test
	public void testAddAndGet() {
		list.add("A");
		list.add("B");
		list.add("C");

		assertEquals(3, list.size());
		assertFalse(list.isEmpty());
		assertEquals("A", list.get(0));
		assertEquals("B", list.get(1));
		assertEquals("C", list.get(2));
	}

	@Test
	public void testAddLast() {
		list.addLast("X");
		list.addLast("Y");
		assertEquals(2, list.size());
		assertEquals("X", list.get(0));
		assertEquals("Y", list.get(1));
	}

	@Test
	public void testAddFirst() {
		list.add("B");
		list.add("C");
		list.addFirst("A");

		assertEquals(3, list.size());
		assertEquals("A", list.get(0));
		assertEquals("B", list.get(1));
		assertEquals("C", list.get(2));
	}

	@Test
	public void testRemoveFirst() {
		list.add("A");
		list.add("B");
		list.add("C");

		String removed = list.removeFirst();
		assertEquals("A", removed);
		assertEquals(2, list.size());
		assertEquals("B", list.get(0));
		assertEquals("C", list.get(1));
	}

	@Test(expected = NoSuchElementException.class)
	public void testRemoveFirstFromEmptyList() {
		list.removeFirst();
	}

	@Test
	public void testRemoveLast() {
		list.add("A");
		list.add("B");
		list.add("C");

		String removed = list.removeLast();
		assertEquals("C", removed);
		assertEquals(2, list.size());
		assertEquals("A", list.get(0));
		assertEquals("B", list.get(1));
	}

	@Test(expected = NoSuchElementException.class)
	public void testRemoveLastFromEmptyList() {
		list.removeLast();
	}

	@Test
	public void testInsertAtHead() {
		list.add("B");
		list.add("C");
		list.insert(0, "A");

		assertEquals(3, list.size());
		assertEquals("A", list.get(0));
		assertEquals("B", list.get(1));
		assertEquals("C", list.get(2));
	}

	@Test
	public void testInsertInTheMiddle() {
		list.add("A");
		list.add("C");
		list.insert(1, "B");

		assertEquals(3, list.size());
		assertEquals("A", list.get(0));
		assertEquals("B", list.get(1));
		assertEquals("C", list.get(2));
	}

	@Test
	public void testInsertAtTail() {
		list.add("A");
		list.add("B");
		list.insert(1, "C");

		assertEquals(3, list.size());
		assertEquals("A", list.get(0));
		assertEquals("C", list.get(1));
		assertEquals("B", list.get(2));
	}
	
	@Test
	public void testInsertAtSize() {
		list.add("A");
		list.add("B");
		list.insert(2, "C");

		assertEquals(3, list.size());
		assertEquals("A", list.get(0));
		assertEquals("B", list.get(1));
		assertEquals("C", list.get(2));
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void testInsertOutOfBounds() {
		list.add("A");
		list.add("B");
		list.insert(3, "X");  // index == size is valid only with add(), not insert()
	}

	@Test
	public void testRemoveFromHead() {
		list.add("A");
		list.add("B");
		list.add("C");

		String removed = list.remove(0);
		assertEquals("A", removed);
		assertEquals(2, list.size());
		assertEquals("B", list.get(0));
		assertEquals("C", list.get(1));
	}

	@Test
	public void testRemoveFromTail() {
		list.add("A");
		list.add("B");
		list.add("C");

		String removed = list.remove(list.size() - 1);
		assertEquals("C", removed);
		assertEquals(2, list.size());
		assertEquals("A", list.get(0));
		assertEquals("B", list.get(1));
	}

	@Test
	public void testRemoveFromMiddle() {
		list.add("A");
		list.add("B");
		list.add("C");
		list.add("D");

		String removed = list.remove(1);
		assertEquals("B", removed);
		assertEquals(3, list.size());
		assertEquals("A", list.get(0));
		assertEquals("C", list.get(1));
		assertEquals("D", list.get(2));
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void testRemoveOutOfBounds() {
		list.add("A");
		list.add("B");
		list.remove(2);
	}

	@Test
	public void testGrowBeyondInitialCapacity() {
		// default initial capacity is 64, so let's add 65 elements
		for(int i = 0; i < 65; i++) {
			list.add("Element-" + i);
		}
		assertEquals(65, list.size());
	}

	@Test
	public void testIteratorHasNextAndNext() {
		list.add("A");
		list.add("B");
		list.add("C");

		Iterator<String> it = list.iterator();

		assertTrue(it.hasNext());
		assertEquals("A", it.next());
		assertTrue(it.hasNext());
		assertEquals("B", it.next());
		assertTrue(it.hasNext());
		assertEquals("C", it.next());
		assertFalse(it.hasNext());
	}

	@Test(expected = NoSuchElementException.class)
	public void testIteratorNextBeyondSize() {
		list.add("A");

		Iterator<String> it = list.iterator();
		assertTrue(it.hasNext());
		assertEquals("A", it.next());

		// This should throw NoSuchElementException
		it.next();
	}

	@Test
	public void testIteratorRemoveFirst() {
		list.add("A");
		list.add("B");
		list.add("C");

		Iterator<String> it = list.iterator();
		assertTrue(it.hasNext());
		assertEquals("A", it.next());

		it.remove(); // remove "A"

		assertEquals(2, list.size());
		assertEquals("B", list.get(0));
		assertEquals("C", list.get(1));
		
		assertTrue(it.hasNext());
		assertEquals("B", it.next());
		assertTrue(it.hasNext());
		assertEquals("C", it.next());
		assertFalse(it.hasNext());
	}

	@Test
	public void testIteratorRemoveAfterNextCalls() {
		list.add("A");
		list.add("B");
		list.add("C");

		Iterator<String> it = list.iterator();
		it.next();  // skip A
		it.next();  // skip B
		it.remove(); // remove "B"

		assertEquals(2, list.size());
		assertEquals("A", list.get(0));
		assertEquals("C", list.get(1));
		
		assertTrue(it.hasNext());
		assertEquals("C", it.next());
		assertFalse(it.hasNext());
		
		it = list.iterator();
		assertTrue(it.hasNext());
		assertEquals("A", it.next());
		assertTrue(it.hasNext());
		assertEquals("C", it.next());
		assertFalse(it.hasNext());
	}
	
	@Test
	public void testIteratorRemoveLast() {
		list.add("A");
		list.add("B");
		list.add("C");

		Iterator<String> it = list.iterator();
		it.next();  // skip A
		it.next();  // skip B
		it.next();  // skip C
		it.remove(); // remove "C"

		assertEquals(2, list.size());
		assertEquals("A", list.get(0));
		assertEquals("B", list.get(1));
		
		assertFalse(it.hasNext());
		
		it = list.iterator();
		assertTrue(it.hasNext());
		assertEquals("A", it.next());
		assertTrue(it.hasNext());
		assertEquals("B", it.next());
		assertFalse(it.hasNext());
	}

	@Test
	public void testIsEmptyOnNewList() {
		assertTrue(list.isEmpty());
	}

	@Test
	public void testIsEmptyAfterAddsAndRemoves() {
		list.add("A");
		list.add("B");
		assertFalse(list.isEmpty());

		list.removeLast();
		list.removeLast();
		assertTrue(list.isEmpty());
	}

	@Test
	public void testSizeWithMultipleOps() {
		assertEquals(0, list.size());

		list.add("A");
		assertEquals(1, list.size());

		list.remove(0);
		assertEquals(0, list.size());
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void testGetOutOfBounds() {
		list.get(0);
	}
	
	@Test
	public void testMultipleIteratorCallsReuseTheSameIterator() {
		list.add("A");
		list.add("B");

		Iterator<String> it1 = list.iterator();
		assertTrue(it1.hasNext());
		assertEquals("A", it1.next());
		assertTrue(it1.hasNext());
		assertEquals("B", it1.next());
		assertFalse(it1.hasNext());

		// calling iterator() again returns the same underlying ReusableIterator
		Iterator<String> it2 = list.iterator();
		assertTrue(it2.hasNext());
		assertEquals("A", it2.next());
		assertTrue(it2.hasNext());
		assertEquals("B", it2.next());
		assertFalse(it2.hasNext());
	}
}
