package com.dataiku.geoip.uniquedb;

public class ReadableArray {

	final public int getInteger(int index) {
		return meta[offset + index];
	}

	final public ReadableArray getArray(int index) {
		int identifier = meta[offset + index];
		if (identifier == -1) {
			return null;
		} else {
			return new ReadableArray(meta, data, identifier);
		}
	}

	final public String getString(int index) {
		int identifier = meta[offset + index];
		if (identifier == -1)
			return null;
		int from = meta[identifier];
		int to = meta[identifier + 1];
		return data.substring(from, to);
	}

	ReadableArray(int meta[], String data, int offset) {
		this.meta = meta;
		this.data = data;
		this.offset = offset + 1;
	}

	final public int size() {
		return meta[offset - 1];
	}

	final protected int[] meta;
	final protected String data;
	final private int offset;

}