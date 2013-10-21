package com.dataiku.geoip.uniquedb;

import java.util.HashMap;

public class NodeBuilder {

	// Add a string
	// Notice: the string is inserted immediately
	// (and it'll be stored forever even if the current array is never added!)
	final public NodeBuilder add(String string) {

		if (string == null) {
			storage.add(-1);
		} else {
			Integer index = map.get(string);
			if (index == null) {
				index = meta.size();
				meta.add(data.length());
				data.append(string);
				meta.add(data.length());
				map.put(string, index);
			}

			storage.add(index);
		}

		return this;
	}

	// Add a node (either array or struct)
	// Notice: the node content is copied immediately
	// Consequences:
	// - It'll be stored forever even if the current node is never added,
	// resulting in a leak
	// - Further modifications of this node won't be reflected in the database
	final public NodeBuilder add(NodeBuilder node) {

		if (node == null) {
			storage.add(-1);
		} else {
			Integer index = map.get(node.storage);
			if (index == null) {
				if (node.writeSize) {
					meta.add(node.storage.size());
				}
				index = meta.size();
				for (int i = 0; i < node.storage.size(); i++) {
					meta.add(node.storage.get(i));
				}
				map.put(node.storage.clone(), index);
			}
			storage.add(index);
		}

		return this;
	}

	// Add an integer to this node
	final public NodeBuilder add(int value) {
		storage.add(value);
		return this;
	}
	
	// Create a new array node
	// Notice : the newly created array cannot be used on another builder!
	final public NodeBuilder newArray() {
		return new NodeBuilder(data, meta, map, true);
	}

	// Create a new struct node
	// The only difference between a struct and an array is that the structure
	// doesn't store its own size
	// Notice : the newly created array cannot be used on another builder!
	final public NodeBuilder newStruct() {
		return new NodeBuilder(data, meta, map, false);
	}


	NodeBuilder(StringBuilder data, GrowableIntArray meta,
			HashMap<Object, Integer> map, boolean writeSize) {
		this.data = data;
		this.meta = meta;
		this.map = map;
		this.writeSize = writeSize;
	}

	protected StringBuilder data;
	protected GrowableIntArray meta;
	protected HashMap<Object, Integer> map;
	protected GrowableIntArray storage = new GrowableIntArray();
	private boolean writeSize;
}