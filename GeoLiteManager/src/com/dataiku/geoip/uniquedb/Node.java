package com.dataiku.geoip.uniquedb;

public class Node {

	// Get the integer at the position 'index' of the current node
	// If the element is not an integer or if the index doesn't exist
	// => undefined behavior
	final public int getInteger(int index) {
		return meta[offset + index];
	}
	
	// Get the node at the position 'index' of the current node
	// If the element is not an array or if the index doesn't exist
	// => undefined behavior
	final public Node getNode(int index) {
		int identifier = meta[offset + index];
		if (identifier == -1) {
			return null;
		} else {
			return new Node(meta, data, identifier);
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
	
	// Get the number of elements in the current node
	// If the current node is not an array node => undefined behavior
	final public int size() {
		return meta[offset - 1];
	}

	Node(int meta[], String data, int offset) {
		this.meta = meta;
		this.data = data;
		this.offset = offset;
	}

	final protected int[] meta;
	final protected String data;
	final private int offset;

}