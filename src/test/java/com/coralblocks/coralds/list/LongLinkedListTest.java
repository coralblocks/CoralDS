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
package com.coralblocks.coralds.list;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

public class LongLinkedListTest {

    private LongLinkedList list;

    @Before
    public void setUp() {
        // We can start with an initial capacity of 10 (or any other number)
        list = new LongLinkedList(10);
    }

    @Test
    public void testIsEmptyAndSizeOnNewList() {
        assertTrue("List should be empty initially", list.isEmpty());
        assertEquals("Size should be 0 initially", 0, list.size());
    }

    @Test
    public void testAddFirstAndRemoveFirst_singleElement() {
        list.addFirst(100L);

        // Verify that the list has one element
        assertFalse("List should not be empty after adding one element", list.isEmpty());
        assertEquals("Size should be 1 after adding one element", 1, list.size());

        // Check the first element
        LongLinkedList.NullableLong nullableLong = list.first();
        assertFalse("NullableLong should not be null", nullableLong.isNull());
        assertEquals("First element should be 100", 100L, nullableLong.getValue());

        // Remove it
        LongLinkedList.NullableLong removed = list.removeFirst();
        assertFalse("Removed NullableLong should not be null", removed.isNull());
        assertEquals("Removed element should be 100", 100L, removed.getValue());

        // Check that the list is empty again
        assertTrue("List should be empty after removing the only element", list.isEmpty());
        assertEquals("Size should be 0", 0, list.size());
    }

    @Test
    public void testAddLastAndRemoveLast_singleElement() {
        list.addLast(200L);

        // Verify that the list has one element
        assertFalse("List should not be empty after adding one element", list.isEmpty());
        assertEquals("Size should be 1 after adding one element", 1, list.size());

        // Check the last element
        LongLinkedList.NullableLong nullableLong = list.last();
        assertFalse("NullableLong should not be null", nullableLong.isNull());
        assertEquals("Last element should be 200", 200L, nullableLong.getValue());

        // Remove it
        LongLinkedList.NullableLong removed = list.removeLast();
        assertFalse("Removed NullableLong should not be null", removed.isNull());
        assertEquals("Removed element should be 200", 200L, removed.getValue());

        // Check that the list is empty again
        assertTrue("List should be empty after removing the only element", list.isEmpty());
        assertEquals("Size should be 0", 0, list.size());
    }

    @Test
    public void testAddFirstAndLast_multipleElements() {
        list.addFirst(1L);
        list.addFirst(2L);
        list.addLast(3L);
        list.addLast(4L);

        // Now the list should have 4 elements in order: 2, 1, 3, 4
        assertEquals("Size should be 4", 4, list.size());

        // Check first element
        LongLinkedList.NullableLong first = list.first();
        assertFalse(first.isNull());
        assertEquals("First element should be 2", 2L, first.getValue());

        // Check last element
        LongLinkedList.NullableLong last = list.last();
        assertFalse(last.isNull());
        assertEquals("Last element should be 4", 4L, last.getValue());

        // Remove them one by one, checking order
        assertEquals(2L, list.removeFirst().getValue()); // now 1,3,4
        assertEquals(4L, list.removeLast().getValue());  // now 1,3
        assertEquals(1L, list.removeFirst().getValue()); // now 3
        assertEquals(3L, list.removeLast().getValue());  // now empty

        assertTrue("List should be empty after removing all elements", list.isEmpty());
    }

    @Test
    public void testClear() {
        list.addFirst(10L);
        list.addLast(20L);
        list.addLast(30L);

        assertFalse("List should not be empty before clear", list.isEmpty());
        assertEquals("Size should be 3 before clear", 3, list.size());

        list.clear();

        assertTrue("List should be empty after clear", list.isEmpty());
        assertEquals("Size should be 0 after clear", 0, list.size());

        // Ensure subsequent calls still see it as empty
        assertTrue("List should remain empty", list.isEmpty());
        assertEquals("Size should remain 0", 0, list.size());
    }

    @Test
    public void testRemoveFirstFromEmptyList() {
        LongLinkedList.NullableLong removed = list.removeFirst();
        assertTrue("Removed NullableLong should be null if list is empty", removed.isNull());
    }

    @Test
    public void testRemoveLastFromEmptyList() {
        LongLinkedList.NullableLong removed = list.removeLast();
        assertTrue("Removed NullableLong should be null if list is empty", removed.isNull());
    }

    @Test
    public void testFirstAndLastFromEmptyList() {
        LongLinkedList.NullableLong first = list.first();
        LongLinkedList.NullableLong last = list.last();
        assertTrue("First NullableLong should be null if list is empty", first.isNull());
        assertTrue("Last NullableLong should be null if list is empty", last.isNull());
    }

    @Test(expected = NullPointerException.class)
    public void testNullableLongThrowsNPEWhenNull() {
        // If the list is empty, the shared NullableLong is always nullified.
        // Trying to getValue() on that should throw NPE.
        list.first().getValue();
    }

    @Test
    public void testIterator_basicIteration() {
        // Populate list: 10, 20, 30
        list.addLast(10L);
        list.addLast(20L);
        list.addLast(30L);

        Iterator<LongLinkedList.LongHolder> it = list.iterator();
        assertTrue("Iterator should have next initially", it.hasNext());
        assertEquals(10L, it.next().get());

        assertTrue("Iterator should have next after first element", it.hasNext());
        assertEquals(20L, it.next().get());

        assertTrue("Iterator should have next after second element", it.hasNext());
        assertEquals(30L, it.next().get());

        assertFalse("Iterator should not have next after last element", it.hasNext());
    }

    @Test
    public void testIteratorRemove() {
        // Populate list: 10, 20, 30
        list.addLast(10L);
        list.addLast(20L);
        list.addLast(30L);

        // We'll remove the middle element (20) via iterator
        Iterator<LongLinkedList.LongHolder> it = list.iterator();

        // Move to first element
        assertTrue(it.hasNext());
        LongLinkedList.LongHolder first = it.next();
        assertEquals(10L, first.get());

        // Move to second element
        assertTrue(it.hasNext());
        LongLinkedList.LongHolder second = it.next();
        assertEquals(20L, second.get());

        // Remove the second element
        it.remove();
        assertEquals("Size should now be 2 after removing one element", 2, list.size());

        // Now the list should be 10 -> 30
        // Check the third element in iteration
        assertTrue(it.hasNext());
        LongLinkedList.LongHolder third = it.next();
        assertEquals(30L, third.get());

        // No more elements
        assertFalse(it.hasNext());

        // Double-check the remaining list order
        // First element should be 10, last should be 30
        assertEquals(10L, list.first().getValue());
        assertEquals(30L, list.last().getValue());
    }

    @Test
    public void testIteratorRemoveFirstAndLastViaIterator() {
        // Populate list: 100, 200, 300
        list.addLast(100L);
        list.addLast(200L);
        list.addLast(300L);

        Iterator<LongLinkedList.LongHolder> it = list.iterator();

        // Remove the first (100) via iterator
        assertTrue(it.hasNext());
        LongLinkedList.LongHolder first = it.next();
        assertEquals(100L, first.get());
        it.remove(); 
        assertEquals("Size should now be 2", 2, list.size());

        // Now the list is 200 -> 300
        // Remove the middle (which is also the new first in iteration)
        assertTrue(it.hasNext());
        LongLinkedList.LongHolder second = it.next();
        assertEquals(200L, second.get());
        it.remove();
        assertEquals("Size should now be 1", 1, list.size());

        // Now the list is 300
        // Remove the last (300) via iterator
        assertTrue(it.hasNext());
        LongLinkedList.LongHolder third = it.next();
        assertEquals(300L, third.get());
        it.remove();
        assertEquals("Size should now be 0", 0, list.size());

        // Ensure the list is indeed empty
        assertTrue("List should be empty", list.isEmpty());
    }

    @Test
    public void testSharedNullableLongOverwrittenBehavior() {
        // This is a more advanced test to illustrate that first() and last()
        // return a shared NullableLong object.

        list.addLast(5L);
        list.addLast(10L);

        LongLinkedList.NullableLong first = list.first();   // shared object
        assertFalse(first.isNull());
        assertEquals(5L, first.getValue());

        LongLinkedList.NullableLong last = list.last();     // overwrites the same shared object
        assertFalse(last.isNull());
        assertEquals(10L, last.getValue());

        // Now 'first' is referencing the same shared object as 'last'
        // The last call set the shared object's value to 10
        // So if we call getValue() on 'first' again, it should show 10 (overwritten)
        assertEquals("Should have been overwritten to 10", 10L, first.getValue());
    }
}
