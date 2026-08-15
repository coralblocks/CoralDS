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

public class LinkedSetTest {

	private static class Value {
		private final int id;
		private final int hash;

		private Value(int id, int hash) {
			this.id = id;
			this.hash = hash;
		}

		@Override
		public int hashCode() {
			return hash;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Value)) return false;
			return id == ((Value) obj).id;
		}
	}

	@Test
	public void testInsertionOrderWithCollisionsAndDuplicate() {
		LinkedSet<Value> set = new LinkedSet<>(4);
		Value a = new Value(1, 0);
		Value b = new Value(2, 0);
		Value c = new Value(3, 0);

		assertTrue(set.add(a));
		assertTrue(set.add(b));
		assertFalse(set.add(a));
		assertTrue(set.add(c));

		Iterator<Value> iter = set.iterator();
		assertSame(a, iter.next());
		assertSame(b, iter.next());
		assertSame(c, iter.next());
		assertFalse(iter.hasNext());
	}

	@Test
	public void testRehashPreservesInsertionOrder() {
		LinkedSet<Value> set = new LinkedSet<>(2, 0.5f);
		Value[] values = new Value[20];

		for(int i = 0; i < values.length; i++) {
			values[i] = new Value(i, i % 3);
			assertTrue(set.add(values[i]));
		}

		Iterator<Value> iter = set.iterator();
		for(int i = 0; i < values.length; i++) {
			assertTrue(iter.hasNext());
			assertSame(values[i], iter.next());
		}
		assertFalse(iter.hasNext());

		set.clearSoftReferences();
	}

	@Test
	public void testRemoveAndReinsertMovesToEnd() {
		LinkedSet<Value> set = new LinkedSet<>(4);
		Value a = new Value(1, 0);
		Value b = new Value(2, 0);
		Value c = new Value(3, 0);

		set.add(a);
		set.add(b);
		set.add(c);
		assertTrue(set.remove(b));
		assertFalse(set.remove(b));
		assertTrue(set.add(b));

		Iterator<Value> iter = set.iterator();
		assertSame(a, iter.next());
		assertSame(c, iter.next());
		assertSame(b, iter.next());
		assertFalse(iter.hasNext());
	}

	@Test
	public void testIteratorRemove() {
		LinkedSet<Value> set = new LinkedSet<>(4);
		Value a = new Value(1, 0);
		Value b = new Value(2, 0);
		Value c = new Value(3, 0);

		set.add(a);
		set.add(b);
		set.add(c);

		Iterator<Value> iter = set.iterator();
		assertSame(a, iter.next());
		iter.remove();
		assertSame(b, iter.next());
		assertSame(c, iter.next());
		iter.remove();

		assertFalse(iter.hasNext());
		assertEquals(1, set.size());
		assertFalse(set.contains(a));
		assertTrue(set.contains(b));
		assertFalse(set.contains(c));
	}

	@Test
	public void testClearAndReusableIterator() {
		LinkedSet<Value> set = new LinkedSet<>();
		Value a = new Value(1, 1);

		set.add(a);
		Iterator<Value> iter = set.iterator();
		assertSame(iter, set.iterator());

		set.clear();
		assertTrue(set.isEmpty());
		assertFalse(set.contains(a));
		assertFalse(set.iterator().hasNext());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNull() {
		new LinkedSet<Value>().add(null);
	}
}
