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

import com.coralblocks.coralds.util.IntHolder;

public class IntSetTest {

	private IntSet set;

	@Before
	public void setUp() {
		set = new IntSet();
	}

	@Test
	public void testDefaultConstructor() {
		// Just ensure that the default constructor doesn't blow up and the set is empty
		assertTrue("\tSet should be empty initially", set.isEmpty());
		assertEquals("\tSet size should be 0 initially", 0, set.size());
	}

	@Test
	public void testConstructorWithInitialCapacity() {
		IntSet customSet = new IntSet(128);
		assertTrue("\tSet should be empty initially", customSet.isEmpty());
		assertEquals("\tSet size should be 0 initially", 0, customSet.size());
	}

	@Test
	public void testConstructorWithInitialCapacityAndLoadFactor() {
		IntSet customSet = new IntSet(256, 0.95f);
		assertTrue("\tSet should be empty initially", customSet.isEmpty());
		assertEquals("\tSet size should be 0 initially", 0, customSet.size());
	}

	@Test
	public void testAddAndContains() {
		assertFalse("\tSet should not contain 10 initially", set.contains(10));
		assertTrue("\tAdding 10 should return true (new insertion)", set.add(10));
		assertTrue("\tNow the set should contain 10", set.contains(10));

		// Duplicates
		assertFalse("\tAdding 10 again should return false (already in set)", set.add(10));
		assertTrue("\tSet still contains 10", set.contains(10));

		assertTrue("\tAdding -1 should return true (new insertion)", set.add(-1));
		assertTrue("\tSet should contain -1", set.contains(-1));
		assertTrue("\tAdding 0 should return true (new insertion)", set.add(0));
		assertTrue("\tSet should contain 0", set.contains(0));

		// Edge values
		int largeValue = Integer.MAX_VALUE;
		assertTrue("\tAdding Integer.MAX_VALUE should succeed", set.add(largeValue));
		assertTrue("\tSet should contain Integer.MAX_VALUE", set.contains(largeValue));

		int smallValue = Integer.MIN_VALUE;
		assertTrue("\tAdding Integer.MIN_VALUE should succeed", set.add(smallValue));
		assertTrue("\tSet should contain Integer.MIN_VALUE", set.contains(smallValue));
	}

	@Test
	public void testRemove() {
		assertTrue("\tAdding 5 should return true", set.add(5));
		assertTrue("\tSet should contain 5", set.contains(5));

		assertTrue("\tRemoving 5 should return true", set.remove(5));
		assertFalse("\tSet should not contain 5 after removal", set.contains(5));

		// Removing non-existent element
		assertFalse("\tRemoving 100 should return false (not in set)", set.remove(100));
	}

	@Test
	public void testSizeAndIsEmpty() {
		assertTrue("\tSet should be empty at start", set.isEmpty());
		assertEquals("\tSize should be 0 at start", 0, set.size());

		set.add(1);
		set.add(2);
		set.add(3);

		assertFalse("\tSet should not be empty now", set.isEmpty());
		assertEquals("\tSize should be 3 after adding three items", 3, set.size());

		set.remove(2);
		assertEquals("\tSize should be 2 after removing one item", 2, set.size());
	}

	@Test
	public void testClear() {
		set.add(10);
		set.add(20);
		set.add(30);
		assertEquals("\tSize should be 3 after adding items", 3, set.size());

		set.clear();
		assertTrue("\tSet should be empty after clear", set.isEmpty());
		assertEquals("\tSize should be 0 after clear", 0, set.size());
	}

	@Test
	public void testIteratorBasic() {
		set.add(10);
		set.add(20);
		set.add(30);

		// We expect exactly these three values
		int foundCount = 0;
		for (IntHolder holder : set) {
			int val = holder.get();
			assertTrue("\tIterator returned value that wasn't in the set", set.contains(val));
			foundCount++;
		}
		assertEquals("\tIterator should find 3 elements", 3, foundCount);
	}

	@Test
	public void testIteratorRemove() {
		set.add(1);
		set.add(2);
		set.add(3);

		Iterator<IntHolder> it = set.iterator();
		while (it.hasNext()) {
			int val = it.next().get();
			if (val == 2) {
				it.remove();  // remove 2 from the set
			}
		}

		assertFalse("\t2 should have been removed from the set", set.contains(2));
		assertEquals("\tSet size should now be 2", 2, set.size());
	}

	@Test
	public void testMultipleIteration() {
		set.add(10);
		set.add(20);
		set.add(30);

		// First iteration
		int foundCount1 = 0;
		for (IntHolder holder : set) {
			assertTrue("\tFirst iteration: value should be in the set", set.contains(holder.get()));
			foundCount1++;
		}
		assertEquals("\tFirst iteration should find 3 elements", 3, foundCount1);

		// Second iteration
		int foundCount2 = 0;
		for (IntHolder holder : set) {
			assertTrue("\tSecond iteration: value should still be in the set", set.contains(holder.get()));
			foundCount2++;
		}
		assertEquals("\tSecond iteration should also find 3 elements", 3, foundCount2);
	}

	@Test
	public void testEdgeCases() {
		// Test adding negative numbers
		assertTrue("\tAdding negative number -100", set.add(-100));
		assertTrue("\tSet should contain -100", set.contains(-100));

		// Test removing negative numbers
		assertTrue("\tRemoving -100 should return true", set.remove(-100));
		assertFalse("\t-100 should not be in the set anymore", set.contains(-100));

		// Adding zero
		assertTrue("\tAdding 0", set.add(0));
		assertTrue("\tSet should contain 0", set.contains(0));

		// Removing zero
		assertTrue("\tRemoving 0", set.remove(0));
		assertFalse("\tSet should not contain 0 after removal", set.contains(0));
	}
}
