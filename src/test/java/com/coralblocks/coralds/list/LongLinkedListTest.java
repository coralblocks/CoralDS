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
import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Test;

import com.coralblocks.coralds.util.LongHolder;

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
        assertEquals("First element should be 100", 100L, list.first());

        // Remove it
        assertEquals("Removed element should be 100", 100L, list.removeFirst());

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
        assertEquals("Last element should be 200", 200L, list.last());

        // Remove it
        assertEquals("Removed element should be 200", 200L, list.removeLast());

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
        assertEquals("First element should be 2", 2L, list.first());

        // Check last element
        assertEquals("Last element should be 4", 4L, list.last());

        // Remove them one by one, checking order
        assertEquals(2L, list.removeFirst()); // now 1,3,4
        assertEquals(4L, list.removeLast());  // now 1,3
        assertEquals(1L, list.removeFirst()); // now 3
        assertEquals(3L, list.removeLast());  // now empty

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

    @Test(expected = NoSuchElementException.class)
    public void testRemoveFirstFromEmptyList() {
        list.removeFirst();
    }

    @Test(expected = NoSuchElementException.class)
    public void testRemoveLastFromEmptyList() {
        list.removeLast();
    }

    @Test(expected = NoSuchElementException.class)
    public void testFirstFromEmptyList() {
        list.first();
    }
    
    @Test(expected = NoSuchElementException.class)
    public void testLastFromEmptyList() {
        list.last();
    }

    @Test
    public void testIterator_basicIteration() {
        // Populate list: 10, 20, 30
        list.addLast(10L);
        list.addLast(20L);
        list.addLast(30L);

        Iterator<LongHolder> it = list.iterator();
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
        Iterator<LongHolder> it = list.iterator();

        // Move to first element
        assertTrue(it.hasNext());
        LongHolder first = it.next();
        assertEquals(10L, first.get());

        // Move to second element
        assertTrue(it.hasNext());
        LongHolder second = it.next();
        assertEquals(20L, second.get());

        // Remove the second element
        it.remove();
        assertEquals("Size should now be 2 after removing one element", 2, list.size());

        // Now the list should be 10 -> 30
        // Check the third element in iteration
        assertTrue(it.hasNext());
        LongHolder third = it.next();
        assertEquals(30L, third.get());

        // No more elements
        assertFalse(it.hasNext());

        // Double-check the remaining list order
        // First element should be 10, last should be 30
        assertEquals(10L, list.first());
        assertEquals(30L, list.last());
    }

    @Test
    public void testIteratorRemoveFirstAndLastViaIterator() {
        // Populate list: 100, 200, 300
        list.addLast(100L);
        list.addLast(200L);
        list.addLast(300L);

        Iterator<LongHolder> it = list.iterator();

        // Remove the first (100) via iterator
        assertTrue(it.hasNext());
        LongHolder first = it.next();
        assertEquals(100L, first.get());
        it.remove(); 
        assertEquals("Size should now be 2", 2, list.size());

        // Now the list is 200 -> 300
        // Remove the middle (which is also the new first in iteration)
        assertTrue(it.hasNext());
        LongHolder second = it.next();
        assertEquals(200L, second.get());
        it.remove();
        assertEquals("Size should now be 1", 1, list.size());

        // Now the list is 300
        // Remove the last (300) via iterator
        assertTrue(it.hasNext());
        LongHolder third = it.next();
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

        assertEquals(5L, list.first());
        assertEquals(10L, list.last());
    }
}
