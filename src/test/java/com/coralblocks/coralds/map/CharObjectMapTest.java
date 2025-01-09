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

public class CharObjectMapTest {
    
    private CharObjectMap<String> map;
    
    @Before
    public void setUp() {
        map = new CharObjectMap<>();
    }
    
    @Test
    public void testInitialState() {
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
    }
    
    @Test
    public void testPutAndGet() {
        assertNull(map.put('a', "value-a"));
        assertEquals("value-a", map.get('a'));
        assertEquals(1, map.size());
        
        String oldValue = map.put('a', "new-value-a");
        assertEquals("value-a", oldValue);
        assertEquals("new-value-a", map.get('a'));
        assertEquals(1, map.size());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testPutNull() {
        map.put('a', null);
    }
    
    @Test
    public void testContainsKey() {
        assertFalse(map.containsKey('a'));
        map.put('a', "value-a");
        assertTrue(map.containsKey('a'));
    }
    
    @Test
    public void testRemove() {
        assertNull(map.remove('a'));
        
        map.put('a', "value-a");
        assertEquals("value-a", map.remove('a'));
        assertNull(map.get('a'));
        assertEquals(0, map.size());
        
        // Remove again should return null
        assertNull(map.remove('a'));
    }
    
    @Test
    public void testClear() {
        map.put('a', "value-a");
        map.put('b', "value-b");
        assertFalse(map.isEmpty());
        
        map.clear();
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
        assertNull(map.get('a'));
        assertNull(map.get('b'));
    }
    
    @Test
    public void testIterator() {
        map.put('a', "value-a");
        map.put('b', "value-b");
        map.put('c', "value-c");
        
        Iterator<String> iterator = map.iterator();
        int count = 0;
        
        while (iterator.hasNext()) {
            String value = iterator.next();
            char key = map.getCurrIteratorKey();
            assertEquals(map.get(key), value);
            count++;
        }
        
        assertEquals(3, count);
    }
    
    @Test(expected = NoSuchElementException.class)
    public void testIteratorNoSuchElement() {
        Iterator<String> iterator = map.iterator();
        iterator.next(); // Should throw NoSuchElementException
    }
    
    @Test
    public void testIteratorRemove() {
        map.put('a', "value-a");
        Iterator<String> iterator = map.iterator();
        iterator.next();
        iterator.remove();
        
        assertEquals(0, map.size());
        assertNull(map.get('a'));
    }
    
    @Test(expected = NoSuchElementException.class)
    public void testIteratorRemoveWithoutNext() {
        map.put('a', "value-a");
        Iterator<String> iterator = map.iterator();
        iterator.remove(); // Should throw NoSuchElementException
    }
    
    @Test
    public void testFullMapCapacity() {
        // Test adding 128 elements (full capacity)
        for (int i = 0; i < 128; i++) {
            map.put((char) i, "value-" + i);
        }
        
        assertEquals(128, map.size());
        
        // Verify all values are correctly stored
        for (int i = 0; i < 128; i++) {
            assertEquals("value-" + i, map.get((char) i));
        }
    }
    
    @Test
    public void testIteratorReuse() {
        map.put('a', "value-a");
        map.put('b', "value-b");
        
        Iterator<String> iterator1 = map.iterator();
        iterator1.next();
        iterator1.next();
        assertFalse(iterator1.hasNext());
        
        // Get iterator again (should be reset)
        Iterator<String> iterator2 = map.iterator();
        assertTrue(iterator2.hasNext());
        assertEquals(iterator1, iterator2); // Should be same instance
    }
}