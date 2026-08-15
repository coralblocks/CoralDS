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

public class LinkedMapTest {

	private static class Key {
		private final int id;
		private final int hash;

		private Key(int id, int hash) {
			this.id = id;
			this.hash = hash;
		}

		@Override
		public int hashCode() {
			return hash;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Key)) return false;
			return id == ((Key) obj).id;
		}
	}

	@Test
	public void testDefaultConstructorAndCrud() {
		LinkedMap<String, String> map = new LinkedMap<>();

		assertTrue(map.isEmpty());
		assertEquals(0, map.size());
		assertNull(map.put("A", "one"));
		assertNull(map.put("B", "two"));
		assertEquals("one", map.get("A"));
		assertTrue(map.containsKey("B"));
		assertFalse(map.containsKey("C"));
		assertEquals("one", map.remove("A"));
		assertNull(map.remove("A"));
		assertEquals(1, map.size());
	}

	@Test
	public void testInsertionOrderWithCollisions() {
		LinkedMap<Key, String> map = new LinkedMap<>(4);
		Key a = new Key(1, 0);
		Key b = new Key(2, 0);
		Key c = new Key(3, 0);

		map.put(a, "A");
		map.put(b, "B");
		map.put(c, "C");

		Iterator<String> iter = map.iterator();
		assertEquals("A", iter.next());
		assertSame(a, map.getCurrIteratorKey());
		assertEquals("B", iter.next());
		assertSame(b, map.getCurrIteratorKey());
		assertEquals("C", iter.next());
		assertSame(c, map.getCurrIteratorKey());
		assertFalse(iter.hasNext());
	}

	@Test
	public void testRehashPreservesInsertionOrder() {
		LinkedMap<Key, String> map = new LinkedMap<>(2, 0.5f);
		Key[] keys = new Key[20];
		String[] values = new String[20];

		for(int i = 0; i < keys.length; i++) {
			keys[i] = new Key(i, i % 3);
			values[i] = "value-" + i;
			map.put(keys[i], values[i]);
		}

		assertTrue(map.getCurrentArrayLength() > 2);

		Iterator<String> iter = map.iterator();
		for(int i = 0; i < keys.length; i++) {
			assertTrue(iter.hasNext());
			assertSame(values[i], iter.next());
			assertSame(keys[i], map.getCurrIteratorKey());
			assertSame(values[i], map.get(keys[i]));
		}
		assertFalse(iter.hasNext());

		map.clearSoftReferences();
	}

	@Test
	public void testReplaceDoesNotMoveAndReinsertMovesToEnd() {
		LinkedMap<Key, String> map = new LinkedMap<>(4);
		Key a = new Key(1, 0);
		Key b = new Key(2, 0);
		Key c = new Key(3, 0);

		map.put(a, "A");
		map.put(b, "B");
		map.put(c, "C");
		assertEquals("B", map.put(b, "B2"));

		Iterator<String> iter = map.iterator();
		assertEquals("A", iter.next());
		assertEquals("B2", iter.next());
		assertEquals("C", iter.next());

		assertEquals("B2", map.remove(b));
		assertNull(map.put(b, "B3"));

		iter = map.iterator();
		assertEquals("A", iter.next());
		assertEquals("C", iter.next());
		assertEquals("B3", iter.next());
		assertFalse(iter.hasNext());
	}

	@Test
	public void testIteratorRemoveMaintainsBothStructures() {
		LinkedMap<Key, String> map = new LinkedMap<>(4);
		Key a = new Key(1, 0);
		Key b = new Key(2, 0);
		Key c = new Key(3, 0);
		Key d = new Key(4, 0);

		map.put(a, "A");
		map.put(b, "B");
		map.put(c, "C");
		map.put(d, "D");

		Iterator<String> iter = map.iterator();
		assertEquals("A", iter.next());
		iter.remove();
		assertEquals("B", iter.next());
		assertEquals("C", iter.next());
		iter.remove();
		assertEquals("D", iter.next());
		iter.remove();

		assertFalse(iter.hasNext());
		assertEquals(1, map.size());
		assertFalse(map.containsKey(a));
		assertTrue(map.containsKey(b));
		assertFalse(map.containsKey(c));
		assertFalse(map.containsKey(d));

		iter = map.iterator();
		assertEquals("B", iter.next());
		iter.remove();
		assertTrue(map.isEmpty());
	}

	@Test
	public void testClearAndSubsequentInsertions() {
		LinkedMap<Key, String> map = new LinkedMap<>(4);
		Key a = new Key(1, 1);
		Key b = new Key(2, 2);
		Key c = new Key(3, 3);

		map.put(a, "A");
		map.put(b, "B");
		map.clear();

		assertTrue(map.isEmpty());
		assertFalse(map.iterator().hasNext());

		map.put(c, "C");
		map.put(a, "A2");
		Iterator<String> iter = map.iterator();
		assertEquals("C", iter.next());
		assertEquals("A2", iter.next());
		assertFalse(iter.hasNext());
	}

	@Test
	public void testIteratorIsReused() {
		LinkedMap<String, String> map = new LinkedMap<>();
		assertSame(map.iterator(), map.iterator());
	}

	@Test(expected = NoSuchElementException.class)
	public void testIteratorRemoveBeforeNext() {
		LinkedMap<String, String> map = new LinkedMap<>();
		map.put("A", "one");
		map.iterator().remove();
	}

	@Test(expected = NoSuchElementException.class)
	public void testIteratorNextWhenEmpty() {
		new LinkedMap<String, String>().iterator().next();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullKey() {
		new LinkedMap<String, String>().put(null, "value");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullValue() {
		new LinkedMap<String, String>().put("key", null);
	}
}
