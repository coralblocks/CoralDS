package com.coralblocks.coralds;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import com.coralblocks.coralds.list.ArrayLinkedList;
import com.coralblocks.coralds.list.ArrayList;
import com.coralblocks.coralds.list.IntArrayList;
import com.coralblocks.coralds.list.IntLinkedList;
import com.coralblocks.coralds.list.LinkedList;
import com.coralblocks.coralds.list.LongArrayList;
import com.coralblocks.coralds.list.LongLinkedList;
import com.coralblocks.coralds.map.ByteBufferMap;
import com.coralblocks.coralds.map.CharSequenceMap;
import com.coralblocks.coralds.map.IdentityMap;
import com.coralblocks.coralds.map.IntMap;
import com.coralblocks.coralds.map.LinkedMap;
import com.coralblocks.coralds.map.LongMap;
import com.coralblocks.coralds.map.Map;
import com.coralblocks.coralds.set.IdentitySet;
import com.coralblocks.coralds.set.IntSet;
import com.coralblocks.coralds.set.LinkedSet;
import com.coralblocks.coralds.set.LongSet;
import com.coralblocks.coralds.set.Set;

public class ConstructorConfigurationTest {

	private static final int INITIAL_CAPACITY = 17;
	private static final short MAX_KEY_LENGTH = 31;
	private static final float GROWTH_FACTOR = 1.9f;
	private static final float LOAD_FACTOR = 0.65f;

	@Test
	public void exposesListConstructorConfiguration() {
		ArrayLinkedList<Object> arrayLinkedList = new ArrayLinkedList<>(INITIAL_CAPACITY);
		assertEquals(INITIAL_CAPACITY, arrayLinkedList.getArraySize());

		ArrayList<Object> arrayList = new ArrayList<>(INITIAL_CAPACITY, GROWTH_FACTOR);
		assertEquals(INITIAL_CAPACITY, arrayList.getInitialCapacity());
		assertEquals(GROWTH_FACTOR, arrayList.getGrowthFactor(), 0f);

		IntArrayList intArrayList = new IntArrayList(INITIAL_CAPACITY, GROWTH_FACTOR);
		assertEquals(INITIAL_CAPACITY, intArrayList.getInitialCapacity());
		assertEquals(GROWTH_FACTOR, intArrayList.getGrowthFactor(), 0f);

		LongArrayList longArrayList = new LongArrayList(INITIAL_CAPACITY, GROWTH_FACTOR);
		assertEquals(INITIAL_CAPACITY, longArrayList.getInitialCapacity());
		assertEquals(GROWTH_FACTOR, longArrayList.getGrowthFactor(), 0f);

		assertEquals(INITIAL_CAPACITY, new LinkedList<Object>(INITIAL_CAPACITY).getInitialCapacity());
		assertEquals(INITIAL_CAPACITY, new IntLinkedList(INITIAL_CAPACITY).getInitialCapacity());
		assertEquals(INITIAL_CAPACITY, new LongLinkedList(INITIAL_CAPACITY).getInitialCapacity());
	}

	@Test
	public void exposesMapConstructorConfiguration() {
		Map<Object, Object> map = new Map<>(INITIAL_CAPACITY, LOAD_FACTOR);
		assertMapConfiguration(map.getInitialCapacity(), map.getLoadFactor());

		IdentityMap<Object, Object> identityMap = new IdentityMap<>(INITIAL_CAPACITY, LOAD_FACTOR);
		assertMapConfiguration(identityMap.getInitialCapacity(), identityMap.getLoadFactor());

		LinkedMap<Object, Object> linkedMap = new LinkedMap<>(INITIAL_CAPACITY, LOAD_FACTOR);
		assertMapConfiguration(linkedMap.getInitialCapacity(), linkedMap.getLoadFactor());

		IntMap<Object> intMap = new IntMap<>(INITIAL_CAPACITY, LOAD_FACTOR);
		assertMapConfiguration(intMap.getInitialCapacity(), intMap.getLoadFactor());

		LongMap<Object> longMap = new LongMap<>(INITIAL_CAPACITY, LOAD_FACTOR);
		assertMapConfiguration(longMap.getInitialCapacity(), longMap.getLoadFactor());

		CharSequenceMap<Object> charSequenceMap =
				new CharSequenceMap<>(INITIAL_CAPACITY, MAX_KEY_LENGTH, LOAD_FACTOR);
		assertMapConfiguration(charSequenceMap.getInitialCapacity(), charSequenceMap.getLoadFactor());
		assertEquals(MAX_KEY_LENGTH, charSequenceMap.getMaxKeyLength());

		ByteBufferMap<Object> byteBufferMap =
				new ByteBufferMap<>(INITIAL_CAPACITY, MAX_KEY_LENGTH, LOAD_FACTOR, false);
		assertMapConfiguration(byteBufferMap.getInitialCapacity(), byteBufferMap.getLoadFactor());
		assertEquals(MAX_KEY_LENGTH, byteBufferMap.getMaxKeyLength());
		assertFalse(byteBufferMap.isDirectBuffer());
	}

	@Test
	public void exposesSetConstructorConfiguration() {
		Set<Object> set = new Set<>(INITIAL_CAPACITY, LOAD_FACTOR);
		assertMapConfiguration(set.getInitialCapacity(), set.getLoadFactor());

		IdentitySet<Object> identitySet = new IdentitySet<>(INITIAL_CAPACITY, LOAD_FACTOR);
		assertMapConfiguration(identitySet.getInitialCapacity(), identitySet.getLoadFactor());

		LinkedSet<Object> linkedSet = new LinkedSet<>(INITIAL_CAPACITY, LOAD_FACTOR);
		assertMapConfiguration(linkedSet.getInitialCapacity(), linkedSet.getLoadFactor());

		IntSet intSet = new IntSet(INITIAL_CAPACITY, LOAD_FACTOR);
		assertMapConfiguration(intSet.getInitialCapacity(), intSet.getLoadFactor());

		LongSet longSet = new LongSet(INITIAL_CAPACITY, LOAD_FACTOR);
		assertMapConfiguration(longSet.getInitialCapacity(), longSet.getLoadFactor());
	}

	@Test
	public void exposesDefaultConstructorConfiguration() {
		assertEquals(ArrayList.DEFAULT_INITIAL_CAPACITY, new ArrayList<>().getInitialCapacity());
		assertEquals(ArrayList.DEFAULT_GROWTH_FACTOR, new ArrayList<>().getGrowthFactor(), 0f);
		assertEquals(IntArrayList.DEFAULT_INITIAL_CAPACITY, new IntArrayList().getInitialCapacity());
		assertEquals(IntArrayList.DEFAULT_GROWTH_FACTOR, new IntArrayList().getGrowthFactor(), 0f);
		assertEquals(LongArrayList.DEFAULT_INITIAL_CAPACITY, new LongArrayList().getInitialCapacity());
		assertEquals(LongArrayList.DEFAULT_GROWTH_FACTOR, new LongArrayList().getGrowthFactor(), 0f);

		Map<Object, Object> map = new Map<>();
		assertEquals(Map.DEFAULT_INITIAL_CAPACITY, map.getInitialCapacity());
		assertEquals(Map.DEFAULT_LOAD_FACTOR, map.getLoadFactor(), 0f);

		CharSequenceMap<Object> charSequenceMap = new CharSequenceMap<>();
		assertEquals(CharSequenceMap.DEFAULT_INITIAL_CAPACITY, charSequenceMap.getInitialCapacity());
		assertEquals(CharSequenceMap.DEFAULT_MAX_KEY_LENGTH, charSequenceMap.getMaxKeyLength());
		assertEquals(CharSequenceMap.DEFAULT_LOAD_FACTOR, charSequenceMap.getLoadFactor(), 0f);

		ByteBufferMap<Object> byteBufferMap = new ByteBufferMap<>();
		assertEquals(ByteBufferMap.DEFAULT_INITIAL_CAPACITY, byteBufferMap.getInitialCapacity());
		assertEquals(ByteBufferMap.DEFAULT_MAX_KEY_LENGTH, byteBufferMap.getMaxKeyLength());
		assertEquals(ByteBufferMap.DEFAULT_LOAD_FACTOR, byteBufferMap.getLoadFactor(), 0f);
		assertEquals(ByteBufferMap.DEFAULT_IS_DIRECT_BUFFER, byteBufferMap.isDirectBuffer());
	}

	@Test
	public void retainsOriginalConfigurationAfterGrowth() {
		ArrayList<Object> list = new ArrayList<>(1, 2f);
		list.add(new Object());
		list.add(new Object());
		assertEquals(1, list.getInitialCapacity());
		assertEquals(2f, list.getGrowthFactor(), 0f);

		Map<Integer, Object> map = new Map<>(1, 0.5f);
		map.put(1, new Object());
		map.put(2, new Object());
		assertEquals(1, map.getInitialCapacity());
		assertEquals(0.5f, map.getLoadFactor(), 0f);
	}

	private static void assertMapConfiguration(int initialCapacity, float loadFactor) {
		assertEquals(INITIAL_CAPACITY, initialCapacity);
		assertEquals(LOAD_FACTOR, loadFactor, 0f);
	}
}
