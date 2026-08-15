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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.coralblocks.coralds.map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assume.assumeTrue;

import java.lang.management.ManagementFactory;
import java.util.Iterator;

import org.junit.Test;

/**
 * Allocation test for the steady-state operations of {@link Map}.
 * <p>
 * Waiting for a garbage collection would be nondeterministic, so this test uses
 * the JVM's per-thread allocation counter instead. The map, its pooled entries,
 * and all test data are created before measurement. Construction, rehashing, and
 * pool growth are therefore deliberately outside this test's scope.
 */
public class MapGarbageFreeTest {

	private static final int ENTRY_COUNT = 64;
	private static final int WARMUP_CYCLES = 10_000;
	private static final int MEASURED_CYCLES = 1_000;

	private static final class Key {

		private final int id;

		private Key(int id) {
			this.id = id;
		}

		@Override
		public int hashCode() {
			return id;
		}

		@Override
		public boolean equals(Object other) {
			return other instanceof Key && ((Key) other).id == id;
		}
	}

	private static final class Value {

		private final int id;

		private Value(int id) {
			this.id = id;
		}
	}

	@Test
	public void testSteadyStateOperationsDoNotAllocate() {
		java.lang.management.ThreadMXBean standardBean = ManagementFactory.getThreadMXBean();
		assumeTrue("This JVM does not expose per-thread allocation counters",
				standardBean instanceof com.sun.management.ThreadMXBean);

		com.sun.management.ThreadMXBean allocationBean =
				(com.sun.management.ThreadMXBean) standardBean;
		assumeTrue("This JVM does not support per-thread allocation counters",
				allocationBean.isThreadAllocatedMemorySupported());

		boolean allocationTrackingWasEnabled = allocationBean.isThreadAllocatedMemoryEnabled();
		if (!allocationTrackingWasEnabled) {
			allocationBean.setThreadAllocatedMemoryEnabled(true);
		}

		try {
			Map<Key, Value> map = new Map<>(128);
			Key[] keys = new Key[ENTRY_COUNT];
			Value[] initialValues = new Value[ENTRY_COUNT];
			Value[] updatedValues = new Value[ENTRY_COUNT];

			for(int i = 0; i < ENTRY_COUNT; i++) {
				keys[i] = new Key(i);
				initialValues[i] = new Value(i);
				updatedValues[i] = new Value(ENTRY_COUNT + i);
			}

			long checksum = 0;
			for(int i = 0; i < WARMUP_CYCLES; i++) {
				checksum += exerciseMap(map, keys, initialValues, updatedValues);
			}

			long allocatedBefore = allocationBean.getCurrentThreadAllocatedBytes();

			for(int i = 0; i < MEASURED_CYCLES; i++) {
				checksum += exerciseMap(map, keys, initialValues, updatedValues);
			}

			long allocatedAfter = allocationBean.getCurrentThreadAllocatedBytes();

			assertEquals("Map operations allocated bytes after warmup",
					0, allocatedAfter - allocatedBefore);
			assertEquals("Each operation cycle should leave the map empty", 0, map.size());
			assertNotEquals("The operation result must be observed", 0, checksum);
		} finally {
			if (!allocationTrackingWasEnabled) {
				allocationBean.setThreadAllocatedMemoryEnabled(false);
			}
		}
	}

	private static long exerciseMap(Map<Key, Value> map, Key[] keys,
			Value[] initialValues, Value[] updatedValues) {
		long checksum = 0;

		for(int i = 0; i < ENTRY_COUNT; i++) {
			map.put(keys[i], initialValues[i]);
		}

		for(int i = 0; i < ENTRY_COUNT; i += 3) {
			checksum += map.put(keys[i], updatedValues[i]).id;
		}

		for(int i = 1; i < ENTRY_COUNT; i += 3) {
			checksum += map.remove(keys[i]).id;
		}

		for(int i = 1; i < ENTRY_COUNT; i += 3) {
			map.put(keys[i], updatedValues[i]);
		}

		Iterator<Value> iterator = map.iterator();
		while(iterator.hasNext()) {
			Value value = iterator.next();
			Key key = map.getCurrIteratorKey();
			checksum += value.id + key.id;

			if ((key.id & 1) == 0) {
				iterator.remove();
			}
		}

		map.clear();
		return checksum;
	}
}
