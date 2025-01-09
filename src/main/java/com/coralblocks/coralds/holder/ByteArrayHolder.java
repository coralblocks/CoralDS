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
package com.coralblocks.coralds.holder;

/**
 * A container class that holds an optional byte array value with its length.
 */
public final class ByteArrayHolder {

    private boolean isPresent = false;
    
    private byte[] value = null;
    private int length = 0;

    /**
     * Constructs an empty ByteArrayHolder with no value present.
     */
    public ByteArrayHolder() {

    }

    /**
     * Checks if a value is present in this holder.
     *
     * @return true if a value has been set, false otherwise
     */
    public final boolean isPresent() {
        return isPresent;
    }

    /**
     * Retrieves the stored byte array value.
     *
     * @return the stored byte array value
     * @throws RuntimeException if no value is present when this method is called
     */
    public final byte[] getValue() {
        if (!isPresent) throw new RuntimeException("Tried to get a byte array value that is not present!");
        return value;
    }
    
    /**
     * Retrieves the stored byte array length.
     *
     * @return the stored byte array length
     * @throws RuntimeException if no value is present when this method is called
     */
    public final int getLength() {
    	if (!isPresent) throw new RuntimeException("Tried to get a byte array length that is not present!");
    	return length;
    }

    /**
     * Clears the stored value, marking this holder as empty.
     * 
     * @return the instance of this ByteArrayHolder
     */
    public final ByteArrayHolder clear() {
        isPresent = false;
        this.value = null;
        return this;
    }

    /**
     * Stores a byte value in this holder, together with its length.
     *
     * @param value the byte array reference to store
     * @param length the byte array length to store
     * @return the instance of this ByteArrayHolder
     */
    public final ByteArrayHolder set(byte[] value,int length) {
        isPresent = true;
        this.value = value;
        this.length = length;
        return this;
    }
}