package com.coralblocks.coralds;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;

import org.junit.Test;

import com.coralblocks.coralds.list.ArrayLinkedList;
import com.coralblocks.coralds.list.ArrayList;
import com.coralblocks.coralds.list.IntArrayList;
import com.coralblocks.coralds.list.IntLinkedList;
import com.coralblocks.coralds.list.LinkedList;
import com.coralblocks.coralds.list.LongArrayList;
import com.coralblocks.coralds.list.LongLinkedList;
import com.coralblocks.coralds.map.ByteBufferMap;
import com.coralblocks.coralds.map.ByteMap;
import com.coralblocks.coralds.map.CharMap;
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

public class ValueSemanticsTest {

	private static final Class<?>[] DATA_STRUCTURES = {
			ArrayLinkedList.class,
			ArrayList.class,
			IntArrayList.class,
			IntLinkedList.class,
			LinkedList.class,
			LongArrayList.class,
			LongLinkedList.class,
			ByteBufferMap.class,
			ByteMap.class,
			CharMap.class,
			CharSequenceMap.class,
			IdentityMap.class,
			IntMap.class,
			LinkedMap.class,
			LongMap.class,
			Map.class,
			IdentitySet.class,
			IntSet.class,
			LinkedSet.class,
			LongSet.class,
			Set.class
	};

	@Test
	public void allDataStructuresDeclareValueMethods() throws Exception {
		for (Class<?> type : DATA_STRUCTURES) {
			assertEquals(type, type.getDeclaredMethod("equals", Object.class).getDeclaringClass());
			assertEquals(type, type.getDeclaredMethod("hashCode").getDeclaringClass());
			assertEquals(type, type.getDeclaredMethod("toString").getDeclaringClass());
		}
	}

	@Test
	public void objectListsCompareByValueAndOrder() {
		ArrayList<String> arrayList = new ArrayList<>(2);
		arrayList.add("A");
		arrayList.add("B");
		ArrayList<String> equalArrayList = new ArrayList<>(8, 2f);
		equalArrayList.add("A");
		equalArrayList.add("B");
		assertEqualContents(arrayList, equalArrayList, "[A, B]");
		equalArrayList.removeLast();
		equalArrayList.add("C");
		assertNotEquals(arrayList, equalArrayList);

		ArrayLinkedList<String> arrayLinkedList = new ArrayLinkedList<>(1);
		arrayLinkedList.addLast("A");
		arrayLinkedList.addLast("B");
		ArrayLinkedList<String> equalArrayLinkedList = new ArrayLinkedList<>(4);
		equalArrayLinkedList.addLast("A");
		equalArrayLinkedList.addLast("B");
		assertEqualContents(arrayLinkedList, equalArrayLinkedList, "[A, B]");
		equalArrayLinkedList.removeLast();
		equalArrayLinkedList.addLast("C");
		assertNotEquals(arrayLinkedList, equalArrayLinkedList);

		LinkedList<String> linkedList = new LinkedList<>(2);
		linkedList.add("A");
		linkedList.add("B");
		LinkedList<String> equalLinkedList = new LinkedList<>(8);
		equalLinkedList.add("A");
		equalLinkedList.add("B");
		assertEqualContents(linkedList, equalLinkedList, "[A, B]");
		equalLinkedList.removeLast();
		equalLinkedList.add("C");
		assertNotEquals(linkedList, equalLinkedList);
	}

	@Test
	public void primitiveListsCompareByValueAndOrder() {
		IntArrayList intArrayList = new IntArrayList(2);
		intArrayList.add(1);
		intArrayList.add(2);
		IntArrayList equalIntArrayList = new IntArrayList(8, 2f);
		equalIntArrayList.add(1);
		equalIntArrayList.add(2);
		assertEqualContents(intArrayList, equalIntArrayList, "[1, 2]");
		equalIntArrayList.removeLast();
		equalIntArrayList.add(3);
		assertNotEquals(intArrayList, equalIntArrayList);

		LongArrayList longArrayList = new LongArrayList(2);
		longArrayList.add(1L);
		longArrayList.add(2L);
		LongArrayList equalLongArrayList = new LongArrayList(8, 2f);
		equalLongArrayList.add(1L);
		equalLongArrayList.add(2L);
		assertEqualContents(longArrayList, equalLongArrayList, "[1, 2]");
		equalLongArrayList.removeLast();
		equalLongArrayList.add(3L);
		assertNotEquals(longArrayList, equalLongArrayList);

		IntLinkedList intLinkedList = new IntLinkedList(2);
		intLinkedList.add(1);
		intLinkedList.add(2);
		IntLinkedList equalIntLinkedList = new IntLinkedList(8);
		equalIntLinkedList.add(1);
		equalIntLinkedList.add(2);
		assertEqualContents(intLinkedList, equalIntLinkedList, "[1, 2]");
		equalIntLinkedList.removeLast();
		equalIntLinkedList.add(3);
		assertNotEquals(intLinkedList, equalIntLinkedList);

		LongLinkedList longLinkedList = new LongLinkedList(2);
		longLinkedList.add(1L);
		longLinkedList.add(2L);
		LongLinkedList equalLongLinkedList = new LongLinkedList(8);
		equalLongLinkedList.add(1L);
		equalLongLinkedList.add(2L);
		assertEqualContents(longLinkedList, equalLongLinkedList, "[1, 2]");
		equalLongLinkedList.removeLast();
		equalLongLinkedList.add(3L);
		assertNotEquals(longLinkedList, equalLongLinkedList);
	}

	@Test
	public void primitiveKeyMapsCompareMappingsWithoutOrder() {
		ByteMap<String> byteMap = new ByteMap<>();
		byteMap.put((byte) -1, "A");
		byteMap.put((byte) 2, "B");
		ByteMap<String> equalByteMap = new ByteMap<>();
		equalByteMap.put((byte) 2, "B");
		equalByteMap.put((byte) -1, new String("A"));
		assertEqualMappings(byteMap, equalByteMap, "-1=A", "2=B");

		CharMap<String> charMap = new CharMap<>();
		charMap.put('A', "one");
		charMap.put('B', "two");
		CharMap<String> equalCharMap = new CharMap<>();
		equalCharMap.put('B', "two");
		equalCharMap.put('A', new String("one"));
		assertEqualMappings(charMap, equalCharMap, "A=one", "B=two");

		IntMap<String> intMap = new IntMap<>(2);
		intMap.put(-1, "A");
		intMap.put(2, "B");
		IntMap<String> equalIntMap = new IntMap<>(7);
		equalIntMap.put(2, "B");
		equalIntMap.put(-1, new String("A"));
		assertEqualMappings(intMap, equalIntMap, "-1=A", "2=B");

		LongMap<String> longMap = new LongMap<>(2);
		longMap.put(-1L, "A");
		longMap.put(2L, "B");
		LongMap<String> equalLongMap = new LongMap<>(7);
		equalLongMap.put(2L, "B");
		equalLongMap.put(-1L, new String("A"));
		assertEqualMappings(longMap, equalLongMap, "-1=A", "2=B");

		equalByteMap.put((byte) 3, "C");
		equalCharMap.put('C', "three");
		equalIntMap.put(3, "C");
		equalLongMap.put(3L, "C");
		assertNotEquals(byteMap, equalByteMap);
		assertNotEquals(charMap, equalCharMap);
		assertNotEquals(intMap, equalIntMap);
		assertNotEquals(longMap, equalLongMap);
	}

	@Test
	public void objectKeyMapsCompareMappingsWithoutOrder() {
		Map<String, Integer> map = new Map<>(2);
		map.put("one", 1);
		map.put("two", 2);
		Map<String, Integer> equalMap = new Map<>(7);
		equalMap.put("two", 2);
		equalMap.put(new String("one"), 1);
		assertEqualMappings(map, equalMap, "one=1", "two=2");

		LinkedMap<String, Integer> linkedMap = new LinkedMap<>(2);
		linkedMap.put("one", 1);
		linkedMap.put("two", 2);
		LinkedMap<String, Integer> equalLinkedMap = new LinkedMap<>(7);
		equalLinkedMap.put("two", 2);
		equalLinkedMap.put(new String("one"), 1);
		assertEqualMappings(linkedMap, equalLinkedMap, "one=1", "two=2");
		assertEquals("{one=1, two=2}", linkedMap.toString());

		CharSequenceMap<Integer> charSequenceMap = new CharSequenceMap<>(2);
		charSequenceMap.put("one", 1);
		charSequenceMap.put("two", 2);
		CharSequenceMap<Integer> equalCharSequenceMap = new CharSequenceMap<>(7);
		equalCharSequenceMap.put(new StringBuilder("two"), 2);
		equalCharSequenceMap.put(new StringBuilder("one"), 1);
		assertEqualMappings(charSequenceMap, equalCharSequenceMap, "one=1", "two=2");

		ByteBufferMap<String> byteBufferMap = new ByteBufferMap<>(2, (short) 8, false);
		byteBufferMap.put(new byte[] { 1, 2 }, "A");
		byteBufferMap.put(new byte[] { 3 }, "B");
		ByteBufferMap<String> equalByteBufferMap = new ByteBufferMap<>(7, (short) 8, false);
		equalByteBufferMap.put(ByteBuffer.wrap(new byte[] { 3 }), "B");
		equalByteBufferMap.put(ByteBuffer.wrap(new byte[] { 1, 2 }), new String("A"));
		assertEqualMappings(byteBufferMap, equalByteBufferMap, "[1, 2]=A", "[3]=B");

		equalMap.put("three", 3);
		equalLinkedMap.put("three", 3);
		equalCharSequenceMap.put("three", 3);
		equalByteBufferMap.put(new byte[] { 4 }, "C");
		assertNotEquals(map, equalMap);
		assertNotEquals(linkedMap, equalLinkedMap);
		assertNotEquals(charSequenceMap, equalCharSequenceMap);
		assertNotEquals(byteBufferMap, equalByteBufferMap);
	}

	@Test
	public void identityMapUsesIdentityForKeys() {
		String firstKey = new String("key");
		String equalButDistinctKey = new String("key");

		IdentityMap<String, String> map = new IdentityMap<>();
		map.put(firstKey, "value");
		IdentityMap<String, String> equalMap = new IdentityMap<>();
		equalMap.put(firstKey, new String("value"));
		assertEqualMappings(map, equalMap, "key=value");

		IdentityMap<String, String> differentMap = new IdentityMap<>();
		differentMap.put(equalButDistinctKey, "value");
		assertNotEquals(map, differentMap);
	}

	@Test
	public void setsCompareContentsWithoutOrder() {
		Set<String> set = new Set<>(2);
		set.add("A");
		set.add("B");
		Set<String> equalSet = new Set<>(7);
		equalSet.add("B");
		equalSet.add(new String("A"));
		assertEqualElements(set, equalSet, "A", "B");

		LinkedSet<String> linkedSet = new LinkedSet<>(2);
		linkedSet.add("A");
		linkedSet.add("B");
		LinkedSet<String> equalLinkedSet = new LinkedSet<>(7);
		equalLinkedSet.add("B");
		equalLinkedSet.add(new String("A"));
		assertEqualElements(linkedSet, equalLinkedSet, "A", "B");
		assertEquals("[A, B]", linkedSet.toString());

		IntSet intSet = new IntSet(2);
		intSet.add(1);
		intSet.add(2);
		IntSet equalIntSet = new IntSet(7);
		equalIntSet.add(2);
		equalIntSet.add(1);
		assertEqualElements(intSet, equalIntSet, "1", "2");

		LongSet longSet = new LongSet(2);
		longSet.add(1L);
		longSet.add(2L);
		LongSet equalLongSet = new LongSet(7);
		equalLongSet.add(2L);
		equalLongSet.add(1L);
		assertEqualElements(longSet, equalLongSet, "1", "2");

		equalSet.add("C");
		equalLinkedSet.add("C");
		equalIntSet.add(3);
		equalLongSet.add(3L);
		assertNotEquals(set, equalSet);
		assertNotEquals(linkedSet, equalLinkedSet);
		assertNotEquals(intSet, equalIntSet);
		assertNotEquals(longSet, equalLongSet);
	}

	@Test
	public void identitySetUsesIdentityForElements() {
		String firstElement = new String("element");
		String equalButDistinctElement = new String("element");

		IdentitySet<String> set = new IdentitySet<>();
		set.add(firstElement);
		IdentitySet<String> equalSet = new IdentitySet<>();
		equalSet.add(firstElement);
		assertEqualElements(set, equalSet, "element");

		IdentitySet<String> differentSet = new IdentitySet<>();
		differentSet.add(equalButDistinctElement);
		assertNotEquals(set, differentSet);
	}

	private static void assertEqualContents(Object first, Object second, String expectedString) {
		assertEquals(first, second);
		assertEquals(second, first);
		assertEquals(first.hashCode(), second.hashCode());
		assertEquals(expectedString, first.toString());
	}

	private static void assertEqualMappings(Object first, Object second, String... expectedMappings) {
		assertEquals(first, second);
		assertEquals(second, first);
		assertEquals(first.hashCode(), second.hashCode());
		assertDelimitedContents(first.toString(), '{', '}', expectedMappings);
	}

	private static void assertEqualElements(Object first, Object second, String... expectedElements) {
		assertEquals(first, second);
		assertEquals(second, first);
		assertEquals(first.hashCode(), second.hashCode());
		assertDelimitedContents(first.toString(), '[', ']', expectedElements);
	}

	private static void assertDelimitedContents(String value, char start, char end, String... contents) {
		assertFalse(value.isEmpty());
		assertEquals(start, value.charAt(0));
		assertEquals(end, value.charAt(value.length() - 1));
		for (String content : contents) {
			assertTrue(value.contains(content));
		}
	}
}
