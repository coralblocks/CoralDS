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
import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Test;

import com.coralblocks.coralds.holder.ByteHolder;

public class ByteObjectMapTest {
    
    private ByteObjectMap<String> map;
    
    @Before
    public void setUp() {
        map = new ByteObjectMap<>();
    }
    

    @Test
    public void testEmptyMapProperties() {
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
        assertNull(map.get((byte) 1));
    }

    @Test
    public void testPutAndGet() {
        assertNull(map.put((byte) 1, "value1"));
        assertEquals("value1", map.get((byte) 1));
        assertEquals(1, map.size());
        assertFalse(map.isEmpty());
    }

    @Test
    public void testPutReplace() {
        map.put((byte) 1, "value1");
        assertEquals("value1", map.put((byte) 1, "value2"));
        assertEquals("value2", map.get((byte) 1));
        assertEquals(1, map.size());
    }

    @Test
    public void testRemove() {
        map.put((byte) 1, "value1");
        assertEquals("value1", map.remove((byte) 1));
        assertNull(map.get((byte) 1));
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
    }

    @Test
    public void testContainsKey() {
        assertFalse(map.containsKey((byte) 1));
        map.put((byte) 1, "value1");
        assertTrue(map.containsKey((byte) 1));
    }

    @Test
    public void testContains() {
        map.put((byte) 1, "value1");
        ByteHolder holder = map.contains("value1");
        assertTrue(holder.isPresent());
        assertEquals(1, holder.getValue());
        
        holder = map.contains("nonexistent");
        assertFalse(holder.isPresent());
    }

    @Test
    public void testClear() {
        map.put((byte) 1, "value1");
        map.put((byte) 2, "value2");
        map.clear();
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
        assertNull(map.get((byte) 1));
        assertNull(map.get((byte) 2));
    }

    @Test
    public void testIterator() {
        map.put((byte) 1, "value1");
        map.put((byte) 2, "value2");
        
        Iterator<String> iterator = map.iterator();
        assertTrue(iterator.hasNext());
        String value = iterator.next();
        assertTrue(value.equals("value1") || value.equals("value2"));
        assertEquals(map.getCurrIteratorKey(), value.equals("value1") ? (byte) 1 : (byte) 2);
        
        assertTrue(iterator.hasNext());
        value = iterator.next();
        assertTrue(value.equals("value1") || value.equals("value2"));
        assertEquals(map.getCurrIteratorKey(), value.equals("value1") ? (byte) 1 : (byte) 2);
        
        assertFalse(iterator.hasNext());
    }

    @Test(expected = NoSuchElementException.class)
    public void testIteratorNoMoreElements() {
        Iterator<String> iterator = map.iterator();
        iterator.next();
    }

    @Test
    public void testIteratorWithEmptyMap() {
        Iterator<String> iterator = map.iterator();
        assertFalse("Iterator should have no elements for empty map", iterator.hasNext());
    }
    
    @Test(expected = NoSuchElementException.class)
    public void testIteratorNextOnEmptyMap() {
        Iterator<String> iterator = map.iterator();
        iterator.next(); // should throw NoSuchElementException
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testPutNull() {
        map.put((byte) 1, null);
    }
    
    @Test
    public void testIteratorWithSingleElement() {
        map.put((byte) 1, "One");
        Iterator<String> iterator = map.iterator();
        
        assertTrue("Iterator should have one element", iterator.hasNext());
        assertEquals("One", iterator.next());
        assertEquals("Current iterator key should be 1", (byte) 1, map.getCurrIteratorKey());
        assertFalse("Iterator should have no more elements", iterator.hasNext());
    }
    
    @Test
    public void testIteratorWithMultipleElements() {
        // Add elements with non-sequential keys
        map.put((byte) 1, "One");
        map.put((byte) 5, "Five");
        map.put((byte) 10, "Ten");
        
        Iterator<String> iterator = map.iterator();
        
        assertTrue(iterator.hasNext());
        assertEquals("One", iterator.next());
        assertEquals((byte) 1, map.getCurrIteratorKey());
        
        assertTrue(iterator.hasNext());
        assertEquals("Five", iterator.next());
        assertEquals((byte) 5, map.getCurrIteratorKey());
        
        assertTrue(iterator.hasNext());
        assertEquals("Ten", iterator.next());
        assertEquals((byte) 10, map.getCurrIteratorKey());
        
        assertFalse(iterator.hasNext());
    }
    
    @Test
    public void testIteratorWithNonSequentialKeys() {
        // Add elements with gaps in between
        map.put((byte) 0, "Zero");
        map.put((byte) 255, "Max");
        
        Iterator<String> iterator = map.iterator();
        
        assertTrue(iterator.hasNext());
        assertEquals("Zero", iterator.next());
        assertEquals((byte) 0, map.getCurrIteratorKey());
        
        assertTrue(iterator.hasNext());
        assertEquals("Max", iterator.next());
        assertEquals((byte) 255, map.getCurrIteratorKey());
        
        assertFalse(iterator.hasNext());
    }
    
    @Test
    public void testIteratorRemove() {
        map.put((byte) 1, "One");
        Iterator<String> iterator = map.iterator();
        
        assertEquals("One", iterator.next());
        iterator.remove();
        assertEquals(0, map.size());
        assertNull(map.get((byte) 1));
    }
    
    @Test(expected = NoSuchElementException.class)
    public void testIteratorRemoveWithoutNext() {
        map.put((byte) 1, "One");
        Iterator<String> iterator = map.iterator();
        iterator.remove(); // should throw NoSuchElementException
    }
    
    @Test
    public void testIteratorReuse() {
        map.put((byte) 1, "One");
        map.put((byte) 2, "Two");
        
        Iterator<String> iterator = map.iterator();
        
        // First iteration
        assertTrue(iterator.hasNext());
        assertEquals("One", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("Two", iterator.next());
        assertFalse(iterator.hasNext());
        
        // Reuse the same iterator
        iterator = map.iterator();
        
        // Second iteration
        assertTrue(iterator.hasNext());
        assertEquals("One", iterator.next());
        assertEquals((byte) 1, map.getCurrIteratorKey());
        assertTrue(iterator.hasNext());
        assertEquals("Two", iterator.next());
        assertEquals((byte) 2, map.getCurrIteratorKey());
        assertFalse(iterator.hasNext());
    }
    
    @Test
    public void testGetCurrIteratorKeyAfterRemoval() {
        map.put((byte) 1, "One");
        map.put((byte) 2, "Two");
        
        Iterator<String> iterator = map.iterator();
        assertEquals("One", iterator.next());
        assertEquals((byte) 1, map.getCurrIteratorKey());
        
        iterator.remove();
        assertEquals((byte) 1, map.getCurrIteratorKey()); // Key should remain the same after removal
        
        assertEquals("Two", iterator.next());
        assertEquals((byte) 2, map.getCurrIteratorKey());
    }
}