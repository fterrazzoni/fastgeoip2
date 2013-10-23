package com.dataiku.geoip.uniquedb;

public class Array  {

    final private UniqueDB db;
    final private int offset;
    
    public UniqueDB getDB() {
        return db;
    }
    
	// Get the integer at the position 'index' of the current node (either array or struct)
	// If the element is not an integer or if the index doesn't exist
	// => undefined behavior
	public int getInteger(int index) {
		return db.meta[offset + index];
	}

	// Get the node at the position 'index' of the current node
	// If the element is not an array or if the index doesn't exist
	// => undefined behavior
	public Array getNode(int index) {
			int identifier = db.meta[offset + index];
			if (identifier == -1) {
				return null;
			} else {
				return new Array(db, identifier);
			}
	}

	// Get the string at the position 'index' of the current array
	// If the element is not a string or if the index doesn't exist
	// => undefined behavior
	public String getString(int index) {
		
		int identifier = db.meta[offset + index];
		if (identifier == -1)
			return null;
		
		int from = db.meta[identifier];
		int to = db.meta[identifier + 1];
		return db.data.substring(from, to);
	}

	// Get the number of elements in the current node
	// If the current node is not an array node => undefined behavior
	public int size() {
		return db.meta[offset - 1];
	}

	Array(UniqueDB db, int offset) {
        this.db = db;
        this.offset = offset;
	}
	
	protected Array(Array array) {
	    db = array.db;
        offset = array.offset;
	}

}