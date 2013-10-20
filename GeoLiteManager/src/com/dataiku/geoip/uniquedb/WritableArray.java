package com.dataiku.geoip.uniquedb;

import java.util.ArrayList;
import java.util.HashMap;

public class WritableArray {

	// Add a string
	// Notice: the string is inserted immediately 
	// (and it'll be stored forever even if the current array is never added!)
	final public WritableArray addString(String string) {

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
	
	// Add an array
	// Notice: the array is inserted immediately 
	// (and it'll be stored forever even if the current array is never added!)
	final public WritableArray addArray(WritableArray array) {

		if (array == null) {

			storage.add(-1);

		} else {

			Integer index = map.get(array.storage);
			if (index == null) {
				index = meta.size();
				meta.add(array.storage.size());
				meta.addAll(array.storage);
				map.put(array.storage.clone(), index);
			}
			storage.add(index);
		}

		return this;
	}
	
	// Add an integer
	final public WritableArray addInteger(int value) {
		storage.add(value);
		return this;
	}

	public WritableArray(StringBuilder data, ArrayList<Integer> meta,
			HashMap<Object, Integer> map) {
		this.data = data;
		this.meta = meta;
		this.map = map;
	}

	protected StringBuilder data;
	protected ArrayList<Integer> meta;
	protected HashMap<Object, Integer> map;
	protected ArrayList<Integer> storage = new ArrayList<Integer>();
}