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
 * A container class that holds an optional long value. This class provides a way to represent
 * a nullable long primitive without using the Long wrapper class.
 */
public final class LongHolder {

    private boolean isPresent = false;
    
    private long value = 0;

    /**
     * Constructs an empty LongHolder with no value present.
     */
    public LongHolder() {
    	
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
     * Retrieves the stored long value.
     *
     * @return the stored long value
     * @throws RuntimeException if no value is present when this method is called
     */
    public final long getValue() {
        if (!isPresent) throw new RuntimeException("Tried to get a long value that is not present!");
        return value;
    }

    /**
     * Clears the stored value, marking this holder as empty.
     * 
     * @return the instance of this LongHolder
     */
    public final LongHolder clear() {
        isPresent = false;
        return this;
    }

    /**
     * Stores a long value in this holder.
     *
     * @param value the long value to store
     * @return the instance of this LongHolder
     */
    public final LongHolder set(long value) {
        isPresent = true;
        this.value = value;
        return this;
    }
}