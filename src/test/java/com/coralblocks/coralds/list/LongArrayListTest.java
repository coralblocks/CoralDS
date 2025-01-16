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

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Test;

import com.coralblocks.coralds.util.LongHolder;

public class LongArrayListTest {
	
	@Test
	public void testDefaultConstructor() {
		LongArrayList list = new LongArrayList();
		assertTrue("New list should be empty", list.isEmpty());
		assertEquals("New list should have size 0", 0, list.size());
	}
	
	@Test
	public void testConstructorWithInitialCapacity() {
		LongArrayList list = new LongArrayList(10);
		assertTrue("New list with initial capacity should be empty", list.isEmpty());
		assertEquals("New list should have size 0", 0, list.size());
	}
	
	@Test
	public void testConstructorWithGrowthFactor() {
		LongArrayList list = new LongArrayList(2.0f);
		assertTrue("New list with growth factor only should be empty", list.isEmpty());
		assertEquals("New list should have size 0", 0, list.size());
	}
	
	@Test
	public void testConstructorWithCapacityAndGrowthFactor() {
		LongArrayList list = new LongArrayList(10, 2.0f);
		assertTrue("New list should be empty", list.isEmpty());
		assertEquals("New list should have size 0", 0, list.size());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorInvalidInitialCapacity() {
		new LongArrayList(0);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorInvalidGrowthFactor() {
		new LongArrayList(10, 1.0f);
	}
	
	@Test
	public void testAddAndGet() {
		LongArrayList list = new LongArrayList();
		list.add(100);
		list.add(200);
		list.add(300);
		
		assertEquals("Size should be 3 after adding 3 elements", 3, list.size());
		assertEquals("First element should be 100", 100, list.get(0));
		assertEquals("Second element should be 200", 200, list.get(1));
		assertEquals("Third element should be 300", 300, list.get(2));
	}
	
	@Test
	public void testInsert() {
		LongArrayList list = new LongArrayList();
		list.add(10);
		list.add(20);
		list.add(30);
		list.insert(1, 15); // Insert at index 1
		
		assertEquals("Size should be 4 after inserting", 4, list.size());
		assertEquals("Element at index 0 should be 10", 10, list.get(0));
		assertEquals("Element at index 1 should be 15", 15, list.get(1));
		assertEquals("Element at index 2 should be 20", 20, list.get(2));
		assertEquals("Element at index 3 should be 30", 30, list.get(3));
	}
	
	@Test
	public void testInsertAtEnd() {
		LongArrayList list = new LongArrayList();
		list.add(10);
		list.add(20);
		
		list.insert(list.size(), 30); // Insert at end
		
		assertEquals("Size should be 3", 3, list.size());
		assertEquals("Element at end should be 30", 30, list.get(2));
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void testInsertAtInvalidIndex() {
		LongArrayList list = new LongArrayList();
		list.add(10);
		list.insert(2, 20); // index 2 is out of bounds (size is 1)
	}
	
	@Test
	public void testRemove() {
		LongArrayList list = new LongArrayList();
		list.add(10);
		list.add(20);
		list.add(30);
		
		long removed = list.remove(1); // Remove element at index 1 (20)
		
		assertEquals("Removed element should be 20", 20, removed);
		assertEquals("Size should be 2", 2, list.size());
		assertEquals("First element should be 10", 10, list.get(0));
		assertEquals("Second element should be 30", 30, list.get(1));
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void testRemoveInvalidIndex() {
		LongArrayList list = new LongArrayList();
		list.add(10);
		list.remove(1); // Index 1 is out of bounds (size is 1)
	}
	
	@Test
	public void testAddFirstAndRemoveFirst() {
		LongArrayList list = new LongArrayList();
		list.addFirst(10);
		list.addFirst(20);
		assertEquals("Size should be 2", 2, list.size());
		assertEquals("First element should be 20", 20, list.get(0));
		assertEquals("Second element should be 10", 10, list.get(1));
		
		long removed = list.removeFirst();
		assertEquals("Removed first element should be 20", 20, removed);
		assertEquals("Size should now be 1", 1, list.size());
	}
	
	@Test(expected = NoSuchElementException.class)
	public void testRemoveFirstOnEmpty() {
		LongArrayList list = new LongArrayList();
		list.removeFirst();
	}
	
	@Test
	public void testAddLastAndRemoveLast() {
		LongArrayList list = new LongArrayList();
		list.addLast(10);
		list.addLast(20);
		assertEquals("Size should be 2", 2, list.size());
		assertEquals("First element should be 10", 10, list.get(0));
		assertEquals("Second element should be 20", 20, list.get(1));
		
		long removed = list.removeLast();
		assertEquals("Removed last element should be 20", 20, removed);
		assertEquals("Size should now be 1", 1, list.size());
	}
	
	@Test(expected = NoSuchElementException.class)
	public void testRemoveLastOnEmpty() {
		LongArrayList list = new LongArrayList();
		list.removeLast();
	}
	
	@Test
	public void testIsEmpty() {
		LongArrayList list = new LongArrayList();
		assertTrue("New list should be empty", list.isEmpty());
		list.add(10);
		assertFalse("List should not be empty after adding an element", list.isEmpty());
	}
	
	@Test
	public void testGrowingBeyondInitialCapacity() {
		LongArrayList list = new LongArrayList(2, 1.5f);
		// Add more elements than initial capacity to force growth
		list.add(10);
		list.add(20);
		list.add(30);
		
		assertEquals("Size should be 3 after adding 3 elements", 3, list.size());
		assertEquals("Element at index 2 should be 30", 30, list.get(2));
	}
	
	@Test
	public void testClearSoftReferences() {
		LongArrayList list = new LongArrayList(2, 1.5f);
		list.add(10);
		list.add(20);
		list.add(30); // This triggers at least one array growth
		
		// Clearing soft references to old arrays (cannot directly test GC, but we can call it)
		list.clearSoftReferences();
		
		// Just ensure the list still works after clearing
		assertEquals("Size should remain unchanged", 3, list.size());
		assertEquals("Element at index 2 should still be 30", 30, list.get(2));
	}
	
	@Test
	public void testIteratorHasNextAndNext() {
		LongArrayList list = new LongArrayList();
		list.add(10);
		list.add(20);
		list.add(30);
		
		Iterator<LongHolder> it = list.iterator();
		assertTrue("Iterator should have next element", it.hasNext());
		assertEquals("Next element should be 10", 10, it.next().get());
		assertTrue("Iterator should still have next", it.hasNext());
		assertEquals("Next element should be 20", 20, it.next().get());
		assertTrue("Iterator should still have next", it.hasNext());
		assertEquals("Next element should be 30", 30, it.next().get());
		assertFalse("Iterator should not have next after last element", it.hasNext());
	}
	
	@Test(expected = NoSuchElementException.class)
	public void testIteratorNextBeyondEnd() {
		LongArrayList list = new LongArrayList();
		list.add(10);
		
		Iterator<LongHolder> it = list.iterator();
		it.next(); // 10
		it.next(); // Should throw NoSuchElementException
	}
	
	@Test
	public void testIteratorRemove() {
		LongArrayList list = new LongArrayList();
		list.add(10);
		list.add(20);
		list.add(30);
		
		Iterator<LongHolder> it = list.iterator();
		
		// Remove the first element (10) via iterator
		assertEquals("Next element should be 10", 10, it.next().get());
		it.remove();
		
		assertEquals("After removal, size should be 2", 2, list.size());
		assertEquals("First element should now be 20", 20, list.get(0));
		assertEquals("Second element should now be 30", 30, list.get(1));
	}
	
	@Test(expected = NoSuchElementException.class)
	public void testIteratorRemoveWithoutNext() {
		LongArrayList list = new LongArrayList();
		list.add(10);
		
		Iterator<LongHolder> it = list.iterator();
		// Removing before a successful next() should throw an exception
		it.remove();
	}
}