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

import org.junit.Test;

public class IdentitySetTest {

	@Test
	public void testDefaultConstructor() {
		IdentitySet<Integer> set = new IdentitySet<>();
		assertNotNull(set);
		assertTrue(set.isEmpty());
		assertEquals(0, set.size());
	}

	@Test
	public void testConstructorWithCapacity() {
		IdentitySet<String> set = new IdentitySet<>(10);
		assertNotNull(set);
		assertTrue(set.isEmpty());
		assertEquals(0, set.size());
	}

	@Test
	public void testConstructorWithCapacityAndLoadFactor() {
		IdentitySet<Double> set = new IdentitySet<>(10, 0.75f);
		assertNotNull(set);
		assertTrue(set.isEmpty());
		assertEquals(0, set.size());
	}

	@Test
	public void testAddAndContains() {
		IdentitySet<String> set = new IdentitySet<>();
		String s1 = new String("test");
		String s2 = new String("test");
		// Add first instance
		assertTrue(set.add(s1));
		assertTrue(set.contains(s1));
		// Different instance with the same content should not be recognized as present
		assertFalse(set.contains(s2));
		// Add second distinct instance
		assertTrue(set.add(s2));
		assertTrue(set.contains(s2));
		// Adding the same instance again should return false
		assertFalse(set.add(s1));
		assertEquals(2, set.size());
	}

	@Test
	public void testRemove() {
		IdentitySet<String> set = new IdentitySet<>();
		String s1 = new String("remove");
		set.add(s1);
		assertTrue(set.contains(s1));
		assertEquals(1, set.size());
		// Remove the element and verify removal
		assertTrue(set.remove(s1));
		assertFalse(set.contains(s1));
		assertEquals(0, set.size());
		// Removing an element that is not present should return false
		assertFalse(set.remove(s1));
	}

	@Test
	public void testClear() {
		IdentitySet<Integer> set = new IdentitySet<>();
		set.add(1);
		set.add(2);
		set.add(3);
		assertEquals(3, set.size());
		set.clear();
		assertEquals(0, set.size());
		assertTrue(set.isEmpty());
	}

	@Test
	public void testIterator() {
		IdentitySet<String> set = new IdentitySet<>();
		String s1 = "one";
		String s2 = "two";
		String s3 = "three";
		set.add(s1);
		set.add(s2);
		set.add(s3);
		Iterator<String> it = set.iterator();
		int count = 0;
		while (it.hasNext()) {
			String element = it.next();
			assertTrue(set.contains(element));
			count++;
		}
		assertEquals(set.size(), count);
	}

	@Test
	public void testIteratorRemove() {
		IdentitySet<String> set = new IdentitySet<>();
		String s1 = "a";
		String s2 = "b";
		set.add(s1);
		set.add(s2);
		Iterator<String> it = set.iterator();
		while (it.hasNext()) {
			String element = it.next();
			if (element.equals("a")) {
				it.remove();
			}
		}
		assertFalse(set.contains(s1));
		assertTrue(set.contains(s2));
		assertEquals(1, set.size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddNull() {
		IdentitySet<String> set = new IdentitySet<>();
		set.add(null);
	}

	@Test
	public void testEdgeCases() {
		IdentitySet<Integer> set = new IdentitySet<>();
		// Removing from an empty set should return false.
		assertFalse(set.remove(100));
		// Clearing an empty set should leave it empty.
		set.clear();
		assertTrue(set.isEmpty());
		// Add duplicate elements (using the same instance).
		Integer value = 5;
		assertTrue(set.add(value));
		assertFalse(set.add(value));
		assertEquals(1, set.size());
	}
}