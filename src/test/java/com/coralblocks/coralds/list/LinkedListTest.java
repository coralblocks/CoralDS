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

public class LinkedListTest {
    
    private LinkedList<String> list;
    private static final int INITIAL_CAPACITY = 10;
    
    @Before
    public void setUp() {
        list = new LinkedList<>(INITIAL_CAPACITY);
    }
    
    @Test(expected = NoSuchElementException.class)
    public void testNewListIsEmptyFirst() {
        assertTrue("New list should be empty", list.isEmpty());
        assertEquals("New list should have size 0", 0, list.size());
        list.first();
    }
    
    @Test(expected = NoSuchElementException.class)
    public void testNewListIsEmptyLast() {
        assertTrue("New list should be empty", list.isEmpty());
        assertEquals("New list should have size 0", 0, list.size());
        list.last();
    }
    
    @Test
    public void testAddFirst() {
        list.addFirst("A");
        assertEquals("Size should be 1", 1, list.size());
        assertEquals("First element should be A", "A", list.first());
        assertEquals("Last element should be A", "A", list.last());
        
        list.addFirst("B");
        assertEquals("Size should be 2", 2, list.size());
        assertEquals("First element should be B", "B", list.first());
        assertEquals("Last element should be A", "A", list.last());
    }
    
    @Test
    public void testAddLast() {
        list.addLast("A");
        assertEquals("Size should be 1", 1, list.size());
        assertEquals("First element should be A", "A", list.first());
        assertEquals("Last element should be A", "A", list.last());
        
        list.addLast("B");
        assertEquals("Size should be 2", 2, list.size());
        assertEquals("First element should be A", "A", list.first());
        assertEquals("Last element should be B", "B", list.last());
    }
    
    @Test
    public void testRemoveFirst() {
        
        list.addFirst("A");
        list.addFirst("B");
        list.addFirst("C");
        
        assertEquals("Should remove C", "C", list.removeFirst());
        assertEquals("Size should be 2", 2, list.size());
        assertEquals("First should now be B", "B", list.first());
        
        assertEquals("Should remove B", "B", list.removeFirst());
        assertEquals("Size should be 1", 1, list.size());
        assertEquals("First should now be A", "A", list.first());
        
        assertEquals("Should remove A", "A", list.removeFirst());
        assertTrue("List should be empty", list.isEmpty());
    }
    
    @Test
    public void testRemoveLast() {
        
        list.addLast("A");
        list.addLast("B");
        list.addLast("C");
        
        assertEquals("Should remove C", "C", list.removeLast());
        assertEquals("Size should be 2", 2, list.size());
        assertEquals("Last should now be B", "B", list.last());
        
        assertEquals("Should remove B", "B", list.removeLast());
        assertEquals("Size should be 1", 1, list.size());
        assertEquals("Last should now be A", "A", list.last());
        
        assertEquals("Should remove A", "A", list.removeLast());
        assertTrue("List should be empty", list.isEmpty());
    }
    
    @Test
    public void testClear() {
        list.addLast("A");
        list.addLast("B");
        list.addLast("C");
        
        list.clear();
        assertTrue("List should be empty after clear", list.isEmpty());
        assertEquals("Size should be 0 after clear", 0, list.size());
    }
    
    @Test
    public void testIterator() {
        list.addLast("A");
        list.addLast("B");
        list.addLast("C");
        
        Iterator<String> iter = list.iterator();
        assertTrue("Iterator should have next", iter.hasNext());
        assertEquals("First element should be A", "A", iter.next());
        assertEquals("Second element should be B", "B", iter.next());
        assertEquals("Third element should be C", "C", iter.next());
        assertFalse("Iterator should not have next", iter.hasNext());
    }
    
    @Test
    public void testIteratorRemove() {
        list.addLast("A");
        list.addLast("B");
        list.addLast("C");
        
        Iterator<String> iter = list.iterator();
        
        // Remove first element
        iter.next();
        iter.remove();
        assertEquals("Size should be 2 after remove", 2, list.size());
        assertEquals("First element should be B", "B", list.first());
        
        // Remove middle element
        iter.next();
        iter.remove();
        assertEquals("Size should be 1 after remove", 1, list.size());
        assertEquals("First element should be C", "C", list.first());
        assertEquals("Last element should be C", "C", list.last());
        
        // Remove last element
        iter.next();
        iter.remove();
        assertTrue("List should be empty", list.isEmpty());
    }
    
    @Test
    public void testReusableIterator() {
        list.addLast("A");
        list.addLast("B");
        
        Iterator<String> iter1 = list.iterator();
        iter1.next();
        iter1.next();
        
        Iterator<String> iter2 = list.iterator();
        assertTrue("New iterator should start from beginning", iter2.hasNext());
        assertEquals("First element should be A", "A", iter2.next());
    }
    
    @Test
    public void testObjectPooling() {
        // Test that the pool is being used by adding and removing elements
        for (int i = 0; i < INITIAL_CAPACITY * 2; i++) {
            list.addLast("Test" + i);
        }
        
        for (int i = 0; i < INITIAL_CAPACITY * 2; i++) {
            list.removeLast();
        }
        
        // Add elements again - should reuse pooled entries
        for (int i = 0; i < INITIAL_CAPACITY; i++) {
            list.addLast("Test" + i);
        }
        
        assertEquals("Size should match number of elements added", INITIAL_CAPACITY, list.size());
    }
}