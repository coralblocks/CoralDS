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
package com.coralblocks.coralds;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

public class ArrayBagTest {

    private ArrayBag<String> bag;

    @Before
    public void setUp() {
        bag = new ArrayBag<>(5);
    }

    @Test
    public void testAddIncreasesSize() {
        bag.add("A");
        assertEquals(1, bag.size());
        bag.add("B");
        assertEquals(2, bag.size());
    }

    @Test
    public void testAddAndGet() {
        bag.add("Hello");
        bag.add("World");
        assertEquals("Hello", bag.get("Hello"));
        assertEquals("World", bag.get("World"));
        assertNull(bag.get("NotInBag"));
    }

    @Test
    public void testContains() {
        bag.add("A");
        bag.add("B");
        assertTrue(bag.contains("A"));
        assertTrue(bag.contains("B"));
        assertFalse(bag.contains("C"));
    }

    @Test
    public void testRemoveExistingElement() {
        bag.add("A");
        bag.add("B");
        bag.add("C");
        assertEquals("B", bag.remove("B"));
        assertFalse(bag.contains("B"));
        assertEquals(2, bag.size());
        
        Iterator<String> iter = bag.iterator();
        assertTrue(iter.hasNext());
        assertEquals("A", iter.next());
        assertTrue(iter.hasNext());
        assertEquals("C", iter.next());
        assertFalse(iter.hasNext());
    }
    
    @Test
    public void testRemoveFirstElement() {
        bag.add("A");
        bag.add("B");
        bag.add("C");
        assertEquals("A", bag.remove("A"));
        assertFalse(bag.contains("A"));
        assertEquals(2, bag.size());
        
        Iterator<String> iter = bag.iterator();
        assertTrue(iter.hasNext());
        assertEquals("C", iter.next());
        assertTrue(iter.hasNext());
        assertEquals("B", iter.next());
        assertFalse(iter.hasNext());
    }
    
    @Test
    public void testRemoveThenInsert() {
        bag.add("A");
        bag.add("B");
        bag.add("C");
        bag.add("D");
        assertEquals(4, bag.size());
        
        bag.remove("B");
        bag.add("E");
        bag.remove("C");
        bag.add("F");
        
        StringBuilder sb = new StringBuilder();
        for(String s : bag) sb.append(s);
        assertEquals("ADEF", sb.toString());
        
        bag.remove("A");

        sb.setLength(0);
        for(String s : bag) sb.append(s);
        assertEquals("FDE", sb.toString());
    }
    
    @Test
    public void testDuplicates() {
        bag.add("A");
        bag.add("A");
        bag.add("B");
        bag.add("B");
        bag.add("C");
        bag.add("D");
        assertEquals(6, bag.size());
        
        bag.remove("A");
        StringBuilder sb = new StringBuilder();
        for(String s : bag) sb.append(s);
        assertEquals("DABBC", sb.toString());
        
        bag.remove("A");
        sb.setLength(0);
        for(String s : bag) sb.append(s);
        assertEquals("DCBB", sb.toString());
        
        bag.remove("B");
        sb.setLength(0);
        for(String s : bag) sb.append(s);
        assertEquals("DCB", sb.toString());
    }
    
    @Test
    public void testRemoveFirstElementThroughIterator() {
        bag.add("A");
        bag.add("B");
        bag.add("C");
        assertEquals(3, bag.size());
        
        Iterator<String> iter = bag.iterator();
        assertTrue(iter.hasNext());
        assertEquals("A", iter.next());
        iter.remove();
        assertTrue(iter.hasNext());
        assertEquals("C", iter.next());
        assertTrue(iter.hasNext());
        assertEquals("B", iter.next());
        assertFalse(iter.hasNext());
    }
    
    @Test
    public void testRemoveFirstElementThroughIteratorWithoutHasNext() {
        bag.add("A");
        bag.add("B");
        bag.add("C");
        assertEquals(3, bag.size());
        
        Iterator<String> iter = bag.iterator();
        assertEquals("A", iter.next());
        iter.remove();
        assertEquals("C", iter.next());
        assertEquals("B", iter.next());
        assertFalse(iter.hasNext());
    }

    @Test
    public void testRemoveNonExistingElementReturnsNull() {
        bag.add("A");
        assertNull(bag.remove("Z"));
        assertEquals(1, bag.size());
    }

    @Test
    public void testClearWithNullify() {
        bag.add("A");
        bag.add("B");
        bag.clear(); // default is clear(true)
        assertEquals(0, bag.size());
        assertTrue(bag.isEmpty());
        for(Object s : bag.getArray()) {
        	assertNull(s);
        }
    }

    @Test
    public void testClearWithoutNullify() {
        bag.add("A");
        bag.add("B");
        bag.clear(false);
        assertEquals(0, bag.size());
        assertTrue(bag.isEmpty());
        Object[] array = bag.getArray();
        for(int i = 0; i < 2; i++) {
        	assertNotNull(array[i]);
        }
        for(int i = 2; i < array.length; i++) {
        	assertNull(array[i]);
        }
    }

    @Test
    public void testBagGrows() {
        for (int i = 0; i < 10; i++) {
            bag.add("Element" + i);
        }
        assertEquals(10, bag.size());
        for (int i = 0; i < 10; i++) {
            assertTrue(bag.contains("Element" + i));
        }
    }

    @Test
    public void testIteratorOnEmptyBag() {
        Iterator<String> it = bag.iterator();
        assertFalse(it.hasNext());
    }

    @Test
    public void testIteratorHasNextAndNext() {
        bag.add("A");
        bag.add("B");
        bag.add("C");

        Iterator<String> it = bag.iterator();
        for(int i = 0; i < 3; i++) assertTrue(it.hasNext());
        assertEquals("A", it.next());
        assertTrue(it.hasNext());
        assertEquals("B", it.next());
        assertTrue(it.hasNext());
        assertEquals("C", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testIteratorRemoveFirstElement() {
        bag.add("A");
        bag.add("B");
        bag.add("C");

        Iterator<String> it = bag.iterator();
        assertEquals("A", it.next());
        it.remove(); 
        assertFalse(bag.contains("A"));
        assertEquals(2, bag.size());

        assertEquals("C", it.next());
        assertEquals("B", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testIteratorRemoveMiddleElement() {
        bag.add("A");
        bag.add("B");
        bag.add("C");
        bag.add("D");

        Iterator<String> it = bag.iterator();
        assertEquals("A", it.next());
        assertEquals("B", it.next());
        it.remove();
        assertFalse(bag.contains("B"));
        assertEquals(3, bag.size());

        assertEquals("D", it.next());
        assertEquals("C", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testIteratorRemoveLastElement() {
        bag.add("A");
        bag.add("B");
        bag.add("C");

        Iterator<String> it = bag.iterator();
        assertEquals("A", it.next());
        assertEquals("B", it.next());
        assertEquals("C", it.next());

        it.remove();
        assertFalse(bag.contains("C"));
        assertEquals(2, bag.size());

        assertFalse(it.hasNext());
    }
    
    @Test
    public void testIteratorRemoveSecondLastElement() {
        bag.add("A");
        bag.add("B");
        bag.add("C");

        Iterator<String> it = bag.iterator();
        assertEquals("A", it.next());
        assertEquals("B", it.next());
        it.remove();
        assertFalse(bag.contains("B"));
        assertTrue(bag.contains("C"));
        assertEquals(2, bag.size());
        assertTrue(it.hasNext());
        assertEquals("C", it.next());
    }

    @Test
    public void testIteratorRemoveAllElements() {
        bag.add("A");
        bag.add("B");
        bag.add("C");

        Iterator<String> it = bag.iterator();
        while (it.hasNext()) {
            it.next();
            it.remove();
        }
        assertTrue(bag.isEmpty());
    }
}
