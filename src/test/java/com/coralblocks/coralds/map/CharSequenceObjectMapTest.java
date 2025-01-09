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
import org.junit.Before;
import org.junit.Test;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.HashSet;
import java.util.Set;

public class CharSequenceObjectMapTest {
    
    private CharSequenceObjectMap<String> map;
    
    @Before
    public void setUp() {
        map = new CharSequenceObjectMap<>();
    }
    
    @Test
    public void testBasicOperations() {
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
        
        assertNull(map.put("key1", "value1"));
        assertEquals(1, map.size());
        assertFalse(map.isEmpty());
        
        assertEquals("value1", map.get("key1"));
        assertTrue(map.containsKey("key1"));
        
        assertEquals("value1", map.put("key1", "value2"));
        assertEquals("value2", map.get("key1"));
        
        assertEquals("value2", map.remove("key1"));
        assertNull(map.get("key1"));
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNullValueNotAllowed() {
        map.put("key", null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testKeyLengthExceedsMaximum() {
        StringBuilder longKey = new StringBuilder();
        for (int i = 0; i < 257; i++) {
            longKey.append('a');
        }
        map.put(longKey, "value");
    }
    
    @Test
    public void testCustomCapacityAndLoadFactor() {
        map = new CharSequenceObjectMap<>(64, 0.5f);
        
        // Add enough entries to trigger rehash
        for (int i = 0; i < 33; i++) {
            map.put("key" + i, "value" + i);
        }
        
        // Verify all entries are still accessible
        for (int i = 0; i < 33; i++) {
            assertEquals("value" + i, map.get("key" + i));
        }
    }
    
    @Test
    public void testClear() {
        map.put("key1", "value1");
        map.put("key2", "value2");
        
        assertFalse(map.isEmpty());
        map.clear();
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
        assertNull(map.get("key1"));
        assertNull(map.get("key2"));
    }
    
    @Test
    public void testIterator() {
        // Add some entries
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.put("key3", "value3");
        
        Set<String> values = new HashSet<>();
        Set<String> keys = new HashSet<>();
        
        Iterator<String> iterator = map.iterator();
        while (iterator.hasNext()) {
            String value = iterator.next();
            values.add(value);
            keys.add(map.getCurrIteratorKey().toString());
        }
        
        assertEquals(3, values.size());
        assertTrue(values.contains("value1"));
        assertTrue(values.contains("value2"));
        assertTrue(values.contains("value3"));
        
        assertEquals(3, keys.size());
        assertTrue(keys.contains("key1"));
        assertTrue(keys.contains("key2"));
        assertTrue(keys.contains("key3"));
    }
    
    @Test
    public void testIteratorRemove() {
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.put("key3", "value3");
        
        Iterator<String> iterator = map.iterator();
        while (iterator.hasNext()) {
            String value = iterator.next();
            if (value.equals("value2")) {
                iterator.remove();
            }
        }
        
        assertEquals(2, map.size());
        assertTrue(map.containsKey("key1"));
        assertFalse(map.containsKey("key2"));
        assertTrue(map.containsKey("key3"));
    }
    
    @Test(expected = NoSuchElementException.class)
    public void testIteratorNextBeyondEnd() {
        map.put("key1", "value1");
        Iterator<String> iterator = map.iterator();
        iterator.next(); // First element
        iterator.next(); // Should throw NoSuchElementException
    }
    
    @Test(expected = NoSuchElementException.class)
    public void testIteratorRemoveWithoutNext() {
        map.put("key1", "value1");
        Iterator<String> iterator = map.iterator();
        iterator.remove(); // Should throw NoSuchElementException
    }
    
    @Test(expected = NoSuchElementException.class)
    public void testIteratorDoubleRemove() {
        map.put("key1", "value1");
        Iterator<String> iterator = map.iterator();
        iterator.next();
        iterator.remove();
        iterator.remove(); // Should throw NoSuchElementException
    }
    
    @Test
    public void testDifferentConstructors() {
        // Test all constructor variants
        new CharSequenceObjectMap<String>();
        new CharSequenceObjectMap<String>(256);
        new CharSequenceObjectMap<String>(256, (short)128);
        new CharSequenceObjectMap<String>(256, 0.75f);
        new CharSequenceObjectMap<String>((short)128);
        new CharSequenceObjectMap<String>((short)128, 0.75f);
        new CharSequenceObjectMap<String>(0.75f);
        new CharSequenceObjectMap<String>(256, (short)128, 0.75f);
        
        // Verify they all work as expected
        CharSequenceObjectMap<String> customMap = 
            new CharSequenceObjectMap<>(32, (short)64, 0.5f);
        customMap.put("test", "value");
        assertEquals("value", customMap.get("test"));
    }
    
    @Test
    public void testHashCollisions() {
        // Create strings that might have the same hash code
        map.put("Aa", "value1");
        map.put("BB", "value2"); // 'BB' has same hash code as 'Aa'
        
        assertEquals("value1", map.get("Aa"));
        assertEquals("value2", map.get("BB"));
        assertEquals(2, map.size());
    }
    
    @Test
    public void testClearSoftReferences() {
        // Add enough entries to trigger rehashing
        for (int i = 0; i < 1000; i++) {
            map.put("key" + i, "value" + i);
        }
        
        map.clearSoftReferences();
        
        // Verify the map still works after clearing soft references
        assertEquals("value1", map.get("key1"));
        assertEquals("value999", map.get("key999"));
    }
    
    @Test
    public void testVariableKeyLengths() {
        map.put("", "empty");
        map.put("a", "short");
        map.put("medium_key", "medium");
        map.put("very_long_key_but_within_limits", "long");
        
        assertEquals("empty", map.get(""));
        assertEquals("short", map.get("a"));
        assertEquals("medium", map.get("medium_key"));
        assertEquals("long", map.get("very_long_key_but_within_limits"));
    }
}