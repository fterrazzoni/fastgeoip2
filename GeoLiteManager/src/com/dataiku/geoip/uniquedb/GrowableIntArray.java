package com.dataiku.geoip.uniquedb;

import java.util.Arrays;

// Less overhead compared to ArrayList
class GrowableIntArray {

	int data[] = new int[8];
	int size = 0;

	public void add(int value) {
		if (size == data.length) {
			data = Arrays.copyOf(data, size * 2);
		}
		data[size] = value;
		size++;
	}

	public int get(int index) {
		return data[index];
	}
	
	public int size() {
		return size;
	}
	
	public void set(int index, int value) {
		data[index] = value;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof GrowableIntArray)) {
			return false;
		}
		GrowableIntArray arr = (GrowableIntArray) obj;
		if (arr.size == size && Arrays.equals(arr.data, data)) {
			return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(data)*31+size;
	}
	
	@Override
	protected GrowableIntArray clone() {
		GrowableIntArray arr = new GrowableIntArray();
		arr.size = size;
		arr.data = data.clone();
		return arr;
	}

}
