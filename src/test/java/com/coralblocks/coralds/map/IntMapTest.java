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

import org.junit.Before;
import org.junit.Test;

public class IntMapTest {
    
    private IntMap<String> mapRegular;
    private IntMap<String> mapPowerOfTwo;
    
    @Before
    public void setUp() {
        mapRegular = new IntMap<>(3);
        mapPowerOfTwo = new IntMap<>(4);
    }
    
    @Test
    public void testInitialState1() {
        assertEquals(0, mapRegular.size());
        assertTrue(mapRegular.isEmpty());
    }
    
    @Test
    public void testInitialState2() {
        assertEquals(0, mapPowerOfTwo.size());
        assertTrue(mapPowerOfTwo.isEmpty());
    }
    
    @Test
    public void testPutAndGet1() {
        assertNull(mapRegular.put(1, "one"));
        assertEquals("one", mapRegular.get(1));
        assertEquals(1, mapRegular.size());
        assertFalse(mapRegular.isEmpty());
        
        // Test overwriting existing key
        assertEquals("one", mapRegular.put(1, "new-one"));
        assertEquals("new-one", mapRegular.get(1));
        assertEquals(1, mapRegular.size());
    }
    
    @Test
    public void testPutAndGet2() {
        assertNull(mapPowerOfTwo.put(1, "one"));
        assertEquals("one", mapPowerOfTwo.get(1));
        assertEquals(1, mapPowerOfTwo.size());
        assertFalse(mapPowerOfTwo.isEmpty());
        
        // Test overwriting existing key
        assertEquals("one", mapPowerOfTwo.put(1, "new-one"));
        assertEquals("new-one", mapPowerOfTwo.get(1));
        assertEquals(1, mapPowerOfTwo.size());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testPutNullValue1() {
        mapRegular.put(1, null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testPutNullValue2() {
    	mapPowerOfTwo.put(1, null);
    }
    
    @Test
    public void testContainsKey1() {
        mapRegular.put(1, "one");
        mapRegular.put(2, "two");
        
        assertTrue(mapRegular.containsKey(1));
        assertTrue(mapRegular.containsKey(2));
        assertFalse(mapRegular.containsKey(3));
    }
    
    @Test
    public void testContainsKey2() {
    	mapPowerOfTwo.put(1, "one");
    	mapPowerOfTwo.put(2, "two");
        
        assertTrue(mapPowerOfTwo.containsKey(1));
        assertTrue(mapPowerOfTwo.containsKey(2));
        assertFalse(mapPowerOfTwo.containsKey(3));
    }
    
    @Test
    public void testRemove1() {
        mapRegular.put(1, "one");
        mapRegular.put(2, "two");
        
        assertEquals("one", mapRegular.remove(1));
        assertEquals(1, mapRegular.size());
        assertNull(mapRegular.get(1));
        assertEquals("two", mapRegular.get(2));
        
        // Test removing non-existent key
        assertNull(mapRegular.remove(3));
    }
    
    @Test
    public void testRemove2() {
    	mapPowerOfTwo.put(1, "one");
    	mapPowerOfTwo.put(2, "two");
        
        assertEquals("one", mapPowerOfTwo.remove(1));
        assertEquals(1, mapPowerOfTwo.size());
        assertNull(mapPowerOfTwo.get(1));
        assertEquals("two", mapPowerOfTwo.get(2));
        
        // Test removing non-existent key
        assertNull(mapPowerOfTwo.remove(3));
    }
    
    @Test
    public void testClear1() {
        mapRegular.put(1, "one");
        mapRegular.put(2, "two");
        
        mapRegular.clear();
        assertEquals(0, mapRegular.size());
        assertTrue(mapRegular.isEmpty());
        assertNull(mapRegular.get(1));
        assertNull(mapRegular.get(2));
    }
    
    @Test
    public void testClear2() {
    	mapPowerOfTwo.put(1, "one");
    	mapPowerOfTwo.put(2, "two");
        
    	mapPowerOfTwo.clear();
        assertEquals(0, mapPowerOfTwo.size());
        assertTrue(mapPowerOfTwo.isEmpty());
        assertNull(mapPowerOfTwo.get(1));
        assertNull(mapPowerOfTwo.get(2));
    }
    
    @Test
    public void testRehashing1() {
        // Fill beyond the default threshold to trigger rehashing
        for (int i = 0; i < 200; i++) {
            mapRegular.put(i, "value" + i);
        }
        
        // Verify all entries are still accessible
        for (int i = 0; i < 200; i++) {
            assertEquals("value" + i, mapRegular.get(i));
        }
        assertEquals(200, mapRegular.size());
    }
    
    @Test
    public void testRehashing2() {
        // Fill beyond the default threshold to trigger rehashing
        for (int i = 0; i < 200; i++) {
        	mapPowerOfTwo.put(i, "value" + i);
        }
        
        // Verify all entries are still accessible
        for (int i = 0; i < 200; i++) {
            assertEquals("value" + i, mapPowerOfTwo.get(i));
        }
        assertEquals(200, mapPowerOfTwo.size());
    }
    
    @Test
    public void testIterator1() {
        mapRegular.put(1, "one");
        mapRegular.put(2, "two");
        mapRegular.put(3, "three");
        
        Iterator<String> iter = mapRegular.iterator();
        int count = 0;
        
        while (iter.hasNext()) {
            String value = iter.next();
            int key = mapRegular.getCurrIteratorKey();
            assertEquals(mapRegular.get(key), value);
            count++;
        }
        
        assertEquals(3, count);
    }
    
    @Test
    public void testIterator2() {
    	mapPowerOfTwo.put(1, "one");
    	mapPowerOfTwo.put(2, "two");
    	mapPowerOfTwo.put(3, "three");
        
        Iterator<String> iter = mapPowerOfTwo.iterator();
        int count = 0;
        
        while (iter.hasNext()) {
            String value = iter.next();
            int key = mapPowerOfTwo.getCurrIteratorKey();
            assertEquals(mapPowerOfTwo.get(key), value);
            count++;
        }
        
        assertEquals(3, count);
    }    
    
    @Test(expected = NoSuchElementException.class)
    public void testIteratorNoMoreElements1() {
        Iterator<String> iter = mapRegular.iterator();
        assertFalse(iter.hasNext());
        iter.next(); // Should throw exception when empty
    }
    
    @Test(expected = NoSuchElementException.class)
    public void testIteratorNoMoreElements2() {
        Iterator<String> iter = mapPowerOfTwo.iterator();
        assertFalse(iter.hasNext());
        iter.next(); // Should throw exception when empty
    }
    
    @Test
    public void testIteratorRemove1() {
        mapRegular.put(1, "one");
        mapRegular.put(2, "two");
        
        Iterator<String> iter = mapRegular.iterator();
        assertTrue(iter.hasNext());
        String toRemove = iter.next();
        iter.remove();
        
        assertEquals(1, mapRegular.size());
        
        iter = mapRegular.iterator();
        assertTrue(iter.hasNext());
        String element = iter.next();
        
        if (toRemove.equals("one")) {
        	assertEquals("two", element);
        } else {
        	assertEquals("one", element);
        }
        
        iter.remove();
        
        assertEquals(0, mapRegular.size());
    }
    
    @Test
    public void testIteratorRemove2() {
    	mapPowerOfTwo.put(1, "one");
    	mapPowerOfTwo.put(2, "two");
        
        Iterator<String> iter = mapPowerOfTwo.iterator();
        assertTrue(iter.hasNext());
        String toRemove = iter.next();
        iter.remove();
        
        assertEquals(1, mapPowerOfTwo.size());
        
        iter = mapPowerOfTwo.iterator();
        assertTrue(iter.hasNext());
        String element = iter.next();
        
        if (toRemove.equals("one")) {
        	assertEquals("two", element);
        } else {
        	assertEquals("one", element);
        }
        
        iter.remove();
        
        assertEquals(0, mapPowerOfTwo.size());
    }
    
    @Test(expected = NoSuchElementException.class)
    public void testIteratorIllegalRemove1() {
        mapRegular.put(1, "one");
        Iterator<String> iter = mapRegular.iterator();
        iter.remove(); // Should throw exception without calling next() first
    }
    
    @Test(expected = NoSuchElementException.class)
    public void testIteratorIllegalRemove2() {
    	mapPowerOfTwo.put(1, "one");
        Iterator<String> iter = mapPowerOfTwo.iterator();
        iter.remove(); // Should throw exception without calling next() first
    }
    
    @Test
    public void testCustomInitialCapacity1() {
        IntMap<String> customMap = new IntMap<>(210);
        for (int i = 0; i < 200; i++) {
            customMap.put(i, "value" + i);
        }
        assertEquals(200, customMap.size());
    }
    
    @Test
    public void testCustomInitialCapacity2() {
        IntMap<String> customMap = new IntMap<>(256);
        for (int i = 0; i < 200; i++) {
            customMap.put(i, "value" + i);
        }
        assertEquals(200, customMap.size());
    }
    
    @Test
    public void testGetCurrentIteratorKey1() {
        mapRegular.put(1, "one");
        mapRegular.put(2, "two");
        
        Iterator<String> iter = mapRegular.iterator();
        iter.next();
        int key = mapRegular.getCurrIteratorKey();
        assertEquals(mapRegular.get(key), "one");
    }
    
    @Test
    public void testGetCurrentIteratorKey2() {
    	mapPowerOfTwo.put(1, "one");
    	mapPowerOfTwo.put(2, "two");
        
        Iterator<String> iter = mapPowerOfTwo.iterator();
        iter.next();
        int key = mapPowerOfTwo.getCurrIteratorKey();
        assertEquals(mapPowerOfTwo.get(key), "one");
    }
    
    @Test
    public void testCollisions1() {
        // Create keys that will have the same hash
        int key1 = 1;
        int key2 = key1 + mapRegular.getCurrentArrayLength();  // This should generate a collision
        
        mapRegular.put(key1, "first");
        mapRegular.put(key2, "second");
        
        assertEquals("first", mapRegular.get(key1));
        assertEquals("second", mapRegular.get(key2));
        assertEquals(2, mapRegular.size());
    }
    
    @Test
    public void testCollisions2() {
        // Create keys that will have the same hash
        int key1 = 1;
        int key2 = key1 + mapPowerOfTwo.getCurrentArrayLength();  // This should generate a collision
        
        mapPowerOfTwo.put(key1, "first");
        mapPowerOfTwo.put(key2, "second");
        
        assertEquals("first", mapPowerOfTwo.get(key1));
        assertEquals("second", mapPowerOfTwo.get(key2));
        assertEquals(2, mapPowerOfTwo.size());
    }
}