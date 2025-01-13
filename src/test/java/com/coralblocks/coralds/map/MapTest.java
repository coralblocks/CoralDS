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
package com.coralblocks.coralds.map;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Test;

/**
 * JUnit 4 test cases for the {@link Map} class.
 */
public class MapTest {

	/**
	 * Tests construction with default values and verifies the map is initially empty.
	 */
	@Test
	public void testDefaultConstructor() {
		Map<String, Integer> map = new Map<>();
		assertTrue("New map should be empty", map.isEmpty());
		assertEquals("New map should have size 0", 0, map.size());
	}

	/**
	 * Tests the constructor with a specified initial capacity and verifies the map is empty.
	 */
	@Test
	public void testConstructorWithInitialCapacity() {
		Map<String, Integer> map = new Map<>(64);
		assertTrue("New map should be empty", map.isEmpty());
		assertEquals("New map should have size 0", 0, map.size());
	}

	/**
	 * Tests the constructor with both initial capacity and load factor.
	 */
	@Test
	public void testConstructorWithCapacityAndLoadFactor() {
		Map<String, Integer> map = new Map<>(64, 0.75f);
		assertTrue("New map should be empty", map.isEmpty());
		assertEquals("New map should have size 0", 0, map.size());
	}

	/**
	 * Tests putting entries into the map and retrieving them.
	 */
	@Test
	public void testPutAndGet() {
		Map<String, Integer> map = new Map<>();

		assertNull("Putting a new key should return null", map.put("One", 1));
		assertEquals("Stored value should be retrieved", Integer.valueOf(1), map.get("One"));
		assertEquals("Map size should be 1 after one insertion", 1, map.size());

		assertNull("Putting another new key should return null", map.put("Two", 2));
		assertEquals("Stored value should be retrieved", Integer.valueOf(2), map.get("Two"));
		assertEquals("Map size should be 2 after two insertions", 2, map.size());

		// Overwrite an existing key
		Integer oldValue = map.put("One", 11);
		assertEquals("Overwriting an existing key should return the old value", Integer.valueOf(1), oldValue);
		assertEquals("New value should be accessible", Integer.valueOf(11), map.get("One"));
		assertEquals("Map size should still be 2", 2, map.size());
	}

	/**
	 * Tests behavior when attempting to use null keys or values.
	 */
	@Test
	public void testNullKeysAndValues() {
		Map<String, Integer> map = new Map<>();

		try {
			map.get(null);
			fail("Expected IllegalArgumentException when key is null for get()");
		} catch (IllegalArgumentException e) {
			// Expected
		}

		try {
			map.put(null, 1);
			fail("Expected IllegalArgumentException when key is null for put()");
		} catch (IllegalArgumentException e) {
			// Expected
		}

		try {
			map.put("Key", null);
			fail("Expected IllegalArgumentException when value is null for put()");
		} catch (IllegalArgumentException e) {
			// Expected
		}

		try {
			map.remove(null);
			fail("Expected IllegalArgumentException when key is null for remove()");
		} catch (IllegalArgumentException e) {
			// Expected
		}
	}

	/**
	 * Tests that containsKey() correctly identifies existing or non-existing keys.
	 */
	@Test
	public void testContainsKey() {
		Map<String, Integer> map = new Map<>();
		map.put("A", 10);
		map.put("B", 20);

		assertTrue("Map should contain key 'A'", map.containsKey("A"));
		assertTrue("Map should contain key 'B'", map.containsKey("B"));
		assertFalse("Map should not contain key 'C'", map.containsKey("C"));
	}

	/**
	 * Tests removing entries from the map.
	 */
	@Test
	public void testRemove() {
		Map<String, Integer> map = new Map<>();
		map.put("Alpha", 100);
		map.put("Beta", 200);

		assertEquals("Map size should be 2", 2, map.size());

		Integer removed = map.remove("Alpha");
		assertEquals("Removing 'Alpha' should return 100", Integer.valueOf(100), removed);
		assertNull("Getting 'Alpha' should now return null", map.get("Alpha"));
		assertEquals("Map size should be 1", 1, map.size());

		removed = map.remove("Beta");
		assertEquals("Removing 'Beta' should return 200", Integer.valueOf(200), removed);
		assertEquals("Map size should be 0 after removing both", 0, map.size());

		// Removing a non-existing key
		assertNull("Removing a non-existing key should return null", map.remove("Gamma"));
	}

	/**
	 * Tests clearing the map.
	 */
	@Test
	public void testClear() {
		Map<String, Integer> map = new Map<>();
		map.put("One", 1);
		map.put("Two", 2);
		assertEquals("Map size should be 2", 2, map.size());

		map.clear();
		assertTrue("Map should be empty after clear()", map.isEmpty());
		assertNull("Getting any key should return null after clear()", map.get("One"));
	}

	/**
	 * Tests the iterator functionality, including hasNext() and next().
	 */
	@Test
	public void testIterator() {
		Map<String, Integer> map = new Map<>();
		map.put("K1", 1);
		map.put("K2", 2);
		map.put("K3", 3);

		Iterator<Integer> it = map.iterator();
		int count = 0;
		while(it.hasNext()) {
			Integer value = it.next();
			assertNotNull("Iterator should return non-null values", value);
			count++;
		}
		assertEquals("Iterator should have iterated through 3 values", 3, count);
	}

	/**
	 * Tests getCurrIteratorKey() while iterating over the map.
	 */
	@Test
	public void testGetCurrIteratorKey() {
		Map<String, Integer> map = new Map<>();
		map.put("Key1", 111);
		map.put("Key2", 222);

		Iterator<Integer> it = map.iterator();

		assertTrue("There should be a next element", it.hasNext());
		Integer value1 = it.next();
		assertTrue("Value should be either 111 or 222", value1.equals(111) || value1.equals(222));
		String currKey1 = map.getCurrIteratorKey();
		assertNotNull("Current iterator key should not be null", currKey1);
		assertTrue("Current key should be 'Key1' or 'Key2'", currKey1.equals("Key1") || currKey1.equals("Key2"));

		assertTrue("There should be a next element", it.hasNext());
		Integer value2 = it.next();
		assertTrue("Value should be the other one", value2.equals(111) || value2.equals(222));
		String currKey2 = map.getCurrIteratorKey();
		assertNotNull("Current iterator key should not be null", currKey2);
		assertNotEquals("The key should have changed", currKey1, currKey2);
	}

	/**
	 * Tests removing elements using the iterator's remove() method.
	 */
	@Test
	public void testIteratorRemove() {
		Map<String, Integer> map = new Map<>();
		map.put("K1", 1);
		map.put("K2", 2);
		map.put("K3", 3);

		Iterator<Integer> it = map.iterator();

		assertTrue("Iterator should have a next element", it.hasNext());
		assertEquals(Integer.valueOf(1), it.next());
		it.remove();  // Removes the element "K1"
		assertFalse("Map should not contain 'K1' after iterator.remove()", map.containsKey("K1"));
		assertEquals("Map size should be 2 now", 2, map.size());

		assertTrue("Iterator should have a next element", it.hasNext());
		assertEquals(Integer.valueOf(2), it.next());
		it.remove();  // Removes the element "K2"
		assertFalse("Map should not contain 'K2' after iterator.remove()", map.containsKey("K2"));
		assertEquals("Map size should be 1 now", 1, map.size());

		assertTrue("Iterator should have a next element", it.hasNext());
		assertEquals(Integer.valueOf(3), it.next());
		it.remove();  // Removes the element "K3"
		assertFalse("Map should not contain 'K3' after iterator.remove()", map.containsKey("K3"));
		assertEquals("Map size should be 0 now", 0, map.size());

		assertFalse("Iterator should have no more elements", it.hasNext());
	}

	/**
	 * Tests that remove() throws an exception if called before next() or after the element has been removed.
	 */
	@Test(expected = NoSuchElementException.class)
	public void testIteratorRemoveException() {
		Map<String, Integer> map = new Map<>();
		map.put("A", 1);

		Iterator<Integer> it = map.iterator();
		it.remove();  // remove() before next() should throw NoSuchElementException
	}
}
