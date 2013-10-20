package com.dataiku.geoip.uniquedb;

public class ReadableArray {

	// Get the integer at the position 'index' of the current array
	// If the element is not an integer or if the index doesn't exist
	// => undefined behavior
	final public int getInteger(int index) {
		return meta[offset + index];
	}
	
	// Get the array at the position 'index' of the current array
	// If the element is not an array or if the index doesn't exist
	// => undefined behavior
	final public ReadableArray getArray(int index) {
		int identifier = meta[offset + index];
		if (identifier == -1) {
			return null;
		} else {
			return new ReadableArray(meta, data, identifier);
		}
	}

	// Get the string at the position 'index' of the current array
	// If the element is not a string or if the index doesn't exist
	// => undefined behavior
	final public String getString(int index) {
		int identifier = meta[offset + index];
		if (identifier == -1)
			return null;
		int from = meta[identifier];
		int to = meta[identifier + 1];
		return data.substring(from, to);
	}
	
	// Get the size of the current array
	final public int size() {
		return meta[offset - 1];
	}

	ReadableArray(int meta[], String data, int offset) {
		this.meta = meta;
		this.data = data;
		this.offset = offset + 1;
	}

	final protected int[] meta;
	final protected String data;
	final private int offset;

}