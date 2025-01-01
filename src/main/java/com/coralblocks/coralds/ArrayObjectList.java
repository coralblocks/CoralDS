package com.coralblocks.coralds;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

public class ArrayObjectList<E> {
	
	public static float DEFAULT_GROWTH_FACTOR = 1.75f;
	
	private E[] array;
	private List<SoftReference<E[]>> oldArrays = new ArrayList<>(64);
	private int count = 0;
	private final float growthFactor;
	
	@SuppressWarnings("unchecked")
	public ArrayObjectList(int initialSize, float growthFactor) {
		this.array = (E[]) new Object[initialSize];
		this.growthFactor = growthFactor;
	}
	
	public ArrayObjectList(int initialSize) {
		this(initialSize, DEFAULT_GROWTH_FACTOR);
	}
	
	public final void clear() {
		for(int i = 0; i < count; i++) {
			this.array[i] = null;
		}
		count = 0;
	}
	
	@SuppressWarnings("unchecked")
	private final E[] grow() {
		int newSize = (int) (growthFactor * this.array.length);
		E[] oldArray = this.array;
		E[] newArray = (E[]) new Object[newSize];
		System.arraycopy(oldArray, 0, newArray, 0, oldArray.length);
		for(int i = 0; i < oldArray.length; i++) oldArray[i] = null;
		oldArrays.add(new SoftReference<E[]>(oldArray));
		return newArray;
	}
	
	private final void ensureNotNull(E value) {
		if (value == null) throw new IllegalArgumentException("Cannot insert null value!");
	}
	
	public final void add(E value) {
		ensureNotNull(value);
		if (count == this.array.length) {
			this.array = grow();
		}
		this.array[count++] = value;
	}
	
	public final E removeLast() {
		if (count == 0) return null;
		count--;
		E toReturn = this.array[count];
		this.array[count] = null;
		return toReturn;
	}
	
	public final E remove(int index) {
		if (index >= count) throw new ArrayIndexOutOfBoundsException("Bad index " + index + " for size " + count);
		E toRemove = this.array[index];
		int lastIndex = count-- - 1;
		if (index == lastIndex) {
			this.array[lastIndex] = null;
			return toRemove;
		} else {
			this.array[index] = this.array[lastIndex];
			this.array[lastIndex] = null;
			return toRemove;
		}
	}
	
	
}