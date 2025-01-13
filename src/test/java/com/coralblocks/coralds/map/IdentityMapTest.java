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
 * JUnit 4 test cases for the {@link IdentityMap} class, which uses
 * identity (==) comparison for keys.
 */
public class IdentityMapTest {

	/**
	 * Tests that a default-constructed IdentityMap is initially empty.
	 */
	@Test
	public void testDefaultConstructor() {
		IdentityMap<Object, Object> map = new IdentityMap<>();
		assertTrue("New map should be empty", map.isEmpty());
		assertEquals("New map should have size 0", 0, map.size());
	}

	/**
	 * Tests the constructor that accepts an initial capacity
	 * and verifies the map is empty.
	 */
	@Test
	public void testConstructorWithInitialCapacity() {
		IdentityMap<Object, Object> map = new IdentityMap<>(64);
		assertTrue("New map should be empty", map.isEmpty());
		assertEquals("New map should have size 0", 0, map.size());
	}

	/**
	 * Tests the constructor that accepts both initial capacity
	 * and load factor, and verifies the map is empty.
	 */
	@Test
	public void testConstructorWithCapacityAndLoadFactor() {
		IdentityMap<Object, Object> map = new IdentityMap<>(64, 0.75f);
		assertTrue("New map should be empty", map.isEmpty());
		assertEquals("New map should have size 0", 0, map.size());
	}

	/**
	 * Tests putting entries into the map and retrieving them
	 * using identity comparison for keys.
	 */
	@Test
	public void testPutAndGet() {
		IdentityMap<Object, Integer> map = new IdentityMap<>();

		Object key1 = new Object();
		Object key2 = new Object();

		assertNull("Putting a new key should return null", map.put(key1, 1));
		assertEquals("Stored value should be retrieved", Integer.valueOf(1), map.get(key1));
		assertEquals("Map size should be 1 after one insertion", 1, map.size());

		assertNull("Putting another new key should return null", map.put(key2, 2));
		assertEquals("Stored value should be retrieved", Integer.valueOf(2), map.get(key2));
		assertEquals("Map size should be 2 after two insertions", 2, map.size());

		// Overwrite an existing key
		Integer oldValue = map.put(key1, 11);
		assertEquals("Overwriting an existing key should return the old value", Integer.valueOf(1), oldValue);
		assertEquals("New value should be accessible", Integer.valueOf(11), map.get(key1));
		assertEquals("Map size should still be 2", 2, map.size());
	}

	/**
	 * Tests that the IdentityMap truly uses identity (==) instead of equals().
	 * We create two different String objects with the same content and confirm
	 * they are treated as different keys.
	 */
	@Test
	public void testIdentityBehavior() {
		IdentityMap<String, Integer> map = new IdentityMap<>();

		String s1 = new String("Hello");
		String s2 = new String("Hello");

		assertNotSame("s1 and s2 should be different object references", s1, s2);

		map.put(s1, 100);
		map.put(s2, 200);

		assertEquals("Value for s1 should be 100", Integer.valueOf(100), map.get(s1));
		assertEquals("Value for s2 should be 200", Integer.valueOf(200), map.get(s2));
		assertNotEquals("They should be considered different keys", map.get(s1), map.get(s2));
	}

	/**
	 * Tests that null keys and values throw IllegalArgumentException.
	 */
	@Test
	public void testNullKeysAndValues() {
		IdentityMap<Object, Integer> map = new IdentityMap<>();

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
			map.put(new Object(), null);
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
	 * Tests the containsKey() method, ensuring identity comparison is used.
	 */
	@Test
	public void testContainsKey() {
		IdentityMap<Object, Integer> map = new IdentityMap<>();
		Object k1 = new Object();
		Object k2 = new Object();

		map.put(k1, 10);

		assertTrue("Map should contain k1", map.containsKey(k1));
		assertFalse("Map should not contain k2", map.containsKey(k2));
	}

	/**
	 * Tests removing entries from the map.
	 */
	@Test
	public void testRemove() {
		IdentityMap<Object, Integer> map = new IdentityMap<>();
		Object alpha = new Object();
		Object beta = new Object();

		map.put(alpha, 100);
		map.put(beta, 200);

		assertEquals("Map size should be 2", 2, map.size());

		Integer removed = map.remove(alpha);
		assertEquals("Removing alpha should return 100", Integer.valueOf(100), removed);
		assertNull("Getting alpha should now return null", map.get(alpha));
		assertEquals("Map size should be 1", 1, map.size());

		removed = map.remove(beta);
		assertEquals("Removing beta should return 200", Integer.valueOf(200), removed);
		assertEquals("Map size should be 0 after removing both", 0, map.size());

		// Removing a non-existing key
		assertNull("Removing a non-existing key should return null", map.remove("Gamma"));
	}

	/**
	 * Tests clearing the map.
	 */
	@Test
	public void testClear() {
		IdentityMap<String, Integer> map = new IdentityMap<>();
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
		IdentityMap<String, Integer> map = new IdentityMap<>();
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
		IdentityMap<String, Integer> map = new IdentityMap<>();
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
		IdentityMap<String, Integer> map = new IdentityMap<>();
		map.put("K1", 1);
		map.put("K2", 2);
		map.put("K3", 3);

		Iterator<Integer> it = map.iterator();

		assertTrue("Iterator should have a next element", it.hasNext());
		assertEquals(Integer.valueOf(1), it.next());
		it.remove();  // Removes the element with key "K1"
		assertFalse("Map should not contain 'K1' after iterator.remove()", map.containsKey("K1"));
		assertEquals("Map size should be 2 now", 2, map.size());

		assertTrue("Iterator should have a next element", it.hasNext());
		assertEquals(Integer.valueOf(2), it.next());
		it.remove();  // Removes the element with key "K2"
		assertFalse("Map should not contain 'K2' after iterator.remove()", map.containsKey("K2"));
		assertEquals("Map size should be 1 now", 1, map.size());

		assertTrue("Iterator should have a next element", it.hasNext());
		assertEquals(Integer.valueOf(3), it.next());
		it.remove();  // Removes the element with key "K3"
		assertFalse("Map should not contain 'K3' after iterator.remove()", map.containsKey("K3"));
		assertEquals("Map size should be 0 now", 0, map.size());

		assertFalse("Iterator should have no more elements", it.hasNext());
	}

	/**
	 * Tests that remove() throws an exception if called before next()
	 * or if the element has already been removed.
	 */
	@Test(expected = NoSuchElementException.class)
	public void testIteratorRemoveException() {
		IdentityMap<String, Integer> map = new IdentityMap<>();
		map.put("A", 1);

		Iterator<Integer> it = map.iterator();
		it.remove();  // remove() before next() should throw NoSuchElementException
	}
}
