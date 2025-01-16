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

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

public class SetTest {

	private Set<String> set;

	@Before
	public void setUp() {
		set = new Set<>();
	}

	@Test
	public void testDefaultConstructor() {
		assertNotNull("Set should be initialized", set);
		assertTrue("New set should be empty", set.isEmpty());
		assertEquals("New set should have size 0", 0, set.size());
	}

	@Test
	public void testConstructorWithInitialCapacity() {
		Set<Integer> intSet = new Set<>(256);
		assertNotNull("Set with specific initial capacity should be initialized", intSet);
		assertTrue("New set should be empty", intSet.isEmpty());
		assertEquals("New set should have size 0", 0, intSet.size());
	}

	@Test
	public void testConstructorWithInitialCapacityAndLoadFactor() {
		Set<Double> doubleSet = new Set<>(256, 0.5f);
		assertNotNull("Set with capacity and loadFactor should be initialized", doubleSet);
		assertTrue("New set should be empty", doubleSet.isEmpty());
		assertEquals("New set should have size 0", 0, doubleSet.size());
	}

	@Test
	public void testAddAndContains() {
		assertTrue("Should add a new element for the first time", set.add("A"));
		assertTrue("Set should contain the added element", set.contains("A"));
		
		assertFalse("Adding the same element again should return false", set.add("A"));
		assertTrue("Set should still contain element A", set.contains("A"));

		assertTrue("Should add a different element", set.add("B"));
		assertTrue("Set should contain element B", set.contains("B"));
	}

	@Test
	public void testSize() {
		assertEquals("New set should have size 0", 0, set.size());
		set.add("A");
		assertEquals("Size should be 1 after adding one element", 1, set.size());
		set.add("B");
		assertEquals("Size should be 2 after adding two elements", 2, set.size());
		set.add("A"); // duplicate, shouldn't increase size
		assertEquals("Size should remain 2 after trying to add duplicate", 2, set.size());
	}

	@Test
	public void testIsEmpty() {
		assertTrue("New set should be empty", set.isEmpty());
		set.add("A");
		assertFalse("Set should not be empty after adding an element", set.isEmpty());
	}

	@Test
	public void testRemove() {
		set.add("A");
		set.add("B");
		assertTrue("Set should contain A before removal", set.contains("A"));
		assertTrue("Set should contain B before removal", set.contains("B"));

		assertTrue("Removing A should return true", set.remove("A"));
		assertFalse("Set should no longer contain A", set.contains("A"));
		assertEquals("Size should be 1 after removing A", 1, set.size());

		assertFalse("Removing an element not in the set should return false", set.remove("C"));
		assertEquals("Size should still be 1", 1, set.size());
	}

	@Test
	public void testClear() {
		set.add("A");
		set.add("B");
		assertEquals("Size should be 2 before clearing", 2, set.size());
		assertFalse("Set should not be empty before clearing", set.isEmpty());

		set.clear();
		assertEquals("Size should be 0 after clearing", 0, set.size());
		assertTrue("Set should be empty after clearing", set.isEmpty());
	}

	@Test
	public void testIterator() {
		set.add("A");
		set.add("B");
		set.add("C");

		Iterator<String> it = set.iterator();
		assertNotNull("Iterator should not be null", it);

		int count = 0;
		while (it.hasNext()) {
			String val = it.next();
			assertNotNull("Iterator should not return null elements", val);
			count++;
		}
		assertEquals("Iterator should iterate through all added elements", 3, count);
	}

	@Test
	public void testIteratorRemove() {
		set.add("A");
		set.add("B");

		Iterator<String> it = set.iterator();
		assertTrue("Iterator should have next initially", it.hasNext());
		String firstElement = it.next();
		it.remove(); // remove the first element

		assertEquals("Size should now be 1 after removing one element", 1, set.size());
		assertFalse("Removed element should not be present", set.contains(firstElement));

		assertTrue("Iterator should still have a next after removing one", it.hasNext());
		String secondElement = it.next();
		it.remove(); // remove the second element

		assertEquals("Size should now be 0 after removing second element", 0, set.size());
		assertFalse("Removed element should not be present", set.contains(secondElement));
	}

	@Test
	public void testEdgeCases() {
		// Removing from an empty set
		assertFalse("Removing from an empty set should return false", set.remove("X"));

		// Contains on an empty set
		assertFalse("Empty set should not contain any element", set.contains("X"));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testNull() {
		set.add(null);
	}
}
