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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.coralblocks.coralds.util.LongHolder;

public class LongSetTest {

	@Test
	public void testDefaultConstructor() {
		// Verify that an empty set is created by default
		LongSet longSet = new LongSet();
		assertTrue("Newly created set should be empty", longSet.isEmpty());
		assertEquals("Newly created set should have size 0", 0, longSet.size());
	}

	@Test
	public void testConstructorWithInitialCapacity() {
		// Create a set with a specified initial capacity
		int initialCapacity = 128;
		LongSet longSet = new LongSet(initialCapacity);
		assertTrue("Newly created set should be empty", longSet.isEmpty());
		assertEquals("Newly created set should have size 0", 0, longSet.size());
	}

	@Test
	public void testConstructorWithInitialCapacityAndLoadFactor() {
		// Create a set with a specified initial capacity and load factor
		int initialCapacity = 256;
		float loadFactor = 0.75f;
		LongSet longSet = new LongSet(initialCapacity, loadFactor);
		assertTrue("Newly created set should be empty", longSet.isEmpty());
		assertEquals("Newly created set should have size 0", 0, longSet.size());
	}

	@Test
	public void testAddAndContains() {
		LongSet longSet = new LongSet();
		
		assertFalse("Set should not contain 10 before adding", longSet.contains(10L));
		
		// Add an element
		boolean added = longSet.add(10L);
		assertTrue("Set should report that 10 was successfully added", added);
		assertTrue("Set should contain 10 after adding it", longSet.contains(10L));
		
		// Add the same element again
		added = longSet.add(10L);
		assertFalse("Set should report false when adding a duplicate element", added);
		assertTrue("Set should still contain 10", longSet.contains(10L));
		
		// Add some negative and zero values
		longSet.add(-1L);
		longSet.add(0L);
		
		assertTrue("Set should contain -1 after adding it", longSet.contains(-1L));
		assertTrue("Set should contain 0 after adding it", longSet.contains(0L));
	}

	@Test
	public void testSize() {
		LongSet longSet = new LongSet();
		assertEquals("Size should be 0 initially", 0, longSet.size());
		
		longSet.add(1L);
		longSet.add(2L);
		longSet.add(3L);
		assertEquals("Size should be 3 after adding three elements", 3, longSet.size());
		
		// Adding duplicates should not affect the size
		longSet.add(1L);
		longSet.add(2L);
		assertEquals("Size should remain 3 after adding duplicates", 3, longSet.size());
	}

	@Test
	public void testIsEmpty() {
		LongSet longSet = new LongSet();
		assertTrue("Newly created set should be empty", longSet.isEmpty());
		
		longSet.add(42L);
		assertFalse("Set should not be empty after adding an element", longSet.isEmpty());
		
		longSet.remove(42L);
		assertTrue("Set should be empty after removing all elements", longSet.isEmpty());
	}

	@Test
	public void testRemove() {
		LongSet longSet = new LongSet();
		longSet.add(10L);
		longSet.add(20L);
		
		assertTrue("Set should contain 10", longSet.contains(10L));
		assertTrue("Set should contain 20", longSet.contains(20L));
		assertEquals("Size should be 2", 2, longSet.size());
		
		// Remove an element that is present
		boolean removed = longSet.remove(10L);
		assertTrue("remove(10) should return true when 10 is present", removed);
		assertFalse("Set should not contain 10 after removal", longSet.contains(10L));
		assertEquals("Size should be 1 after removing 10", 1, longSet.size());
		
		// Remove an element that is not present
		removed = longSet.remove(999L);
		assertFalse("remove(999) should return false when 999 is not present", removed);
		assertEquals("Size should remain 1", 1, longSet.size());
		
		// Remove last element
		longSet.remove(20L);
		assertFalse("Set should no longer contain 20", longSet.contains(20L));
		assertTrue("Set should be empty now", longSet.isEmpty());
	}

	@Test
	public void testClear() {
		LongSet longSet = new LongSet();
		longSet.add(1L);
		longSet.add(2L);
		longSet.add(3L);
		
		assertFalse("Set should not be empty", longSet.isEmpty());
		assertEquals("Set size should be 3", 3, longSet.size());
		
		// Clear the set
		longSet.clear();
		assertTrue("Set should be empty after clear()", longSet.isEmpty());
		assertEquals("Set size should be 0 after clear()", 0, longSet.size());
		
		// Ensure that re-adding works after clear
		longSet.add(5L);
		assertEquals("Set size should be 1 after re-adding an element", 1, longSet.size());
		assertTrue("Set should contain the re-added element (5)", longSet.contains(5L));
	}

	@Test
	public void testIterator() {
		LongSet longSet = new LongSet();
		longSet.add(10L);
		longSet.add(20L);
		longSet.add(30L);
		
		// Use a HashSet to track iterated values
		Set<Long> seenValues = new HashSet<Long>();
		Iterator<LongHolder> it = longSet.iterator();
		
		while (it.hasNext()) {
			LongHolder holder = it.next();
			seenValues.add(holder.get());
		}
		
		assertEquals("We should have iterated over exactly 3 unique elements", 3, seenValues.size());
		assertTrue("Iterator should have contained 10", seenValues.contains(10L));
		assertTrue("Iterator should have contained 20", seenValues.contains(20L));
		assertTrue("Iterator should have contained 30", seenValues.contains(30L));
	}

	@Test
	public void testIteratorRemove() {
		LongSet longSet = new LongSet();
		longSet.add(100L);
		longSet.add(200L);
		longSet.add(300L);
		
		Iterator<LongHolder> it = longSet.iterator();
		List<Long> removed = new ArrayList<>();
		
		while (it.hasNext()) {
			LongHolder holder = it.next();
			if (holder.get() == 200L || holder.get() == 100L) {
				it.remove();
				removed.add(holder.get());
			}
		}
		
		assertEquals("We removed 2 elements via iterator", 2, removed.size());
		assertFalse("Set should not contain 100 anymore", longSet.contains(100L));
		assertFalse("Set should not contain 200 anymore", longSet.contains(200L));
		assertTrue("Set should still contain 300", longSet.contains(300L));
		assertEquals("Size should now be 1", 1, longSet.size());
	}

	@Test
	public void testAddEdgeValues() {
		LongSet longSet = new LongSet();
		
		longSet.add(Long.MIN_VALUE);
		longSet.add(Long.MAX_VALUE);
		longSet.add(0L);
		
		assertTrue("Set should contain Long.MIN_VALUE", longSet.contains(Long.MIN_VALUE));
		assertTrue("Set should contain Long.MAX_VALUE", longSet.contains(Long.MAX_VALUE));
		assertTrue("Set should contain 0", longSet.contains(0L));
		assertEquals("Set size should be 3 after adding these edge values", 3, longSet.size());
	}
}