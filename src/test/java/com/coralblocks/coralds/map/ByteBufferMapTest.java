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

import java.nio.ByteBuffer;

import org.junit.Before;
import org.junit.Test;

public class ByteBufferMapTest {
    
    private ByteBufferMap<String> map;
    private static final int INITIAL_CAPACITY = 16;
    private static final float LOAD_FACTOR = 0.75f;
    private static final short MAX_KEY_LENGTH = 32;
    
    @Before
    public void setUp() {
        map = new ByteBufferMap<>(INITIAL_CAPACITY, MAX_KEY_LENGTH, LOAD_FACTOR);
    }
    
    @Test
    public void testBasicOperations() {
        byte[] key1 = "a".getBytes();
        byte[] key2 = "longkey".getBytes();
        byte[] key3 = "verylongkey123".getBytes();
        
        // Test put and get with different key lengths
        assertNull(map.put(key1, "Short"));
        assertEquals("Short", map.get(key1));
        assertEquals(1, map.size());
        
        // Test update existing key
        assertEquals("Short", map.put(key1, "Updated"));
        assertEquals("Updated", map.get(key1));
        assertEquals(1, map.size());
        
        // Test multiple entries with varying lengths
        assertNull(map.put(key2, "Medium"));
        assertNull(map.put(key3, "Long"));
        assertEquals(3, map.size());
        assertEquals("Medium", map.get(key2));
        assertEquals("Long", map.get(key3));
    }
    
    @Test
    public void testByteBufferOperations() {
        ByteBuffer key1 = ByteBuffer.wrap("x".getBytes());
        ByteBuffer key2 = ByteBuffer.wrap("mediumkey".getBytes());
        ByteBuffer key3 = ByteBuffer.wrap("verylongkey12345".getBytes());
        
        // Test put and get with ByteBuffer of different lengths
        assertNull(map.put(key1, "One"));
        assertEquals("One", map.get(key1));
        
        // Test multiple entries
        assertNull(map.put(key2, "Eight"));
        assertNull(map.put(key3, "Fifteen"));
        assertEquals("Eight", map.get(key2));
        assertEquals("Fifteen", map.get(key3));
    }
    
    @Test
    public void testPartialKeyOperations() {
        byte[] key = "abcdefghijklmnop".getBytes();
        
        // Test put and get with different length partial keys
        assertNull(map.put(key, 0, 3, "ABC"));
        assertEquals("ABC", map.get(key, 0, 3));
        
        // Test different slices of varying lengths
        assertNull(map.put(key, 3, 5, "DEFGH"));
        assertNull(map.put(key, 8, 8, "IJKLMNOP"));
        assertEquals("DEFGH", map.get(key, 3, 5));
        assertEquals("IJKLMNOP", map.get(key, 8, 8));
    }
    
    @Test
    public void testRemoveOperations() {
        byte[] key1 = "tiny".getBytes();
        byte[] key2 = "mediumsized".getBytes();
        byte[] key3 = "verylongsizedkey".getBytes();
        
        map.put(key1, "Small");
        map.put(key2, "Medium");
        map.put(key3, "Large");
        
        // Test remove with different key lengths
        assertEquals("Small", map.remove(key1));
        assertNull(map.get(key1));
        assertEquals(2, map.size());
        
        // Test remove with ByteBuffer
        ByteBuffer bbKey = ByteBuffer.wrap(key3);
        assertEquals("Large", map.remove(bbKey));
        assertEquals(1, map.size());
        
        assertEquals("Medium", map.remove(key2));
        assertEquals(0, map.size());
    }
    
    @Test
    public void testRehashing() {
        // Fill map with keys of varying lengths
        for (int i = 0; i < 20; i++) {
            byte[] key = ("key" + "x".repeat(i)).getBytes(); // Each key has different length
            map.put(key, "value" + i);
        }
        
        // Verify all entries are still accessible
        for (int i = 0; i < 20; i++) {
            byte[] key = ("key" + "x".repeat(i)).getBytes();
            assertEquals("value" + i, map.get(key));
        }
    }
    
    @Test
    public void testIterator() {
        byte[] key1 = "a".getBytes();
        byte[] key2 = "mediumkey".getBytes();
        byte[] key3 = "verylongkey123".getBytes();
        
        map.put(key1, "Small");
        map.put(key2, "Medium");
        map.put(key3, "Large");
        
        int count = 0;
        for (String value : map) {
            assertTrue(value.equals("Small") || value.equals("Medium") || value.equals("Large"));
            count++;
        }
        assertEquals(3, count);
    }
    
    @Test
    public void testIteratorRemove() {
        byte[] key1 = "x".getBytes();
        byte[] key2 = "mediumkey".getBytes();
        byte[] key3 = "verylongkey123".getBytes();
        
        map.put(key1, "Small");
        map.put(key2, "Medium");
        map.put(key3, "Large");
        
        java.util.Iterator<String> iter = map.iterator();
        while (iter.hasNext()) {
            String value = iter.next();
            if (value.equals("Medium")) {
                iter.remove();
            }
        }
        
        assertEquals(2, map.size());
        assertEquals("Small", map.get(key1));
        assertNull(map.get(key2));
        assertEquals("Large", map.get(key3));
    }
    
    private void assertArrayEqualsByteBuffer(byte[] expected, ByteBuffer bb) {
    	assertEquals(expected.length, bb.remaining());
        for (int i = 0; i < expected.length; i++) {
            assertEquals("Mismatch at index " + i, expected[i], bb.get(i));
        }
    }
    
    @Test
    public void testGetCurrIteratorKey() {
        byte[] key1 = "a".getBytes();
        byte[] key2 = "mediumkey".getBytes();
        byte[] key3 = "verylongkey123".getBytes();
        
        map.put(key1, "Short");
        map.put(key2, "Medium");
        map.put(key3, "Long");
        
        // Track found keys to verify we see all of them
        boolean foundKey1 = false;
        boolean foundKey2 = false;
        boolean foundKey3 = false;
        
        for (String value : map) {
            ByteBuffer currKey = map.getCurrIteratorKey();
            
            if (value.equals("Short")) {
                assertArrayEqualsByteBuffer(key1, currKey);
                assertEquals(key1.length, currKey.limit());
                foundKey1 = true;
            } else if (value.equals("Medium")) {
                assertArrayEqualsByteBuffer(key2, currKey);
                assertEquals(key2.length, currKey.limit());
                foundKey2 = true;
            } else if (value.equals("Long")) {
                assertArrayEqualsByteBuffer(key3, currKey);
                assertEquals(key3.length, currKey.limit());
                foundKey3 = true;
            }
        }
        
        assertTrue("Should have found key1", foundKey1);
        assertTrue("Should have found key2", foundKey2);
        assertTrue("Should have found key3", foundKey3);
    }
    
    @Test
    public void testIteratorKeyWithRemoval() {
        byte[] key1 = "short".getBytes();
        byte[] key2 = "mediumsized".getBytes();
        
        map.put(key1, "Value1");
        map.put(key2, "Value2");
        
        java.util.Iterator<String> iter = map.iterator();
        while (iter.hasNext()) {
            String value = iter.next();
            ByteBuffer currKey = map.getCurrIteratorKey();
            
            if (value.equals("Value1")) {
                assertArrayEqualsByteBuffer(key1, currKey);
                assertEquals(key1.length, currKey.limit());
                iter.remove();
                
                // Key should still be available after removal
                assertArrayEqualsByteBuffer(key1, currKey);
                assertEquals(key1.length, currKey.limit());
            }
        }
        
        assertEquals(1, map.size());
        assertNull(map.get(key1));
        assertEquals("Value2", map.get(key2));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testMaxKeyLengthExceeded() {
        byte[] key = new byte[MAX_KEY_LENGTH + 1];
        map.put(key, "value");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNullValueNotAllowed() {
        map.put("key".getBytes(), null);
    }
    
    @Test
    public void testEmptyMap() {
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
        assertNull(map.get("nonexistent".getBytes()));
    }
    
    @Test
    public void testCollisions() {
        // Create keys that might have the same hash code but different lengths
        byte[] key1 = {1, 2, 3};
        byte[] key2 = {1, 2, 3, 4};
        byte[] key3 = {1, 2, 3, 4, 5};
        
        map.put(key1, "Three");
        map.put(key2, "Four");
        map.put(key3, "Five");
        
        assertEquals("Three", map.get(key1));
        assertEquals("Four", map.get(key2));
        assertEquals("Five", map.get(key3));
        assertEquals(3, map.size());
    }
}