package com.dataiku.geoip.uniquedb;

public class Node  {

    final private UniqueDB db;
    final private int offset;
    final private boolean flag;
    
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
	public Node getNode(int index) {
	        
			int pointer = db.meta[offset + index];
			boolean ptrflag = (pointer & (1<<31))!=0;
			pointer &= ~(1<<31);
			
			if (pointer == -1) {
				return null;
			} else {
				return new Node(db, pointer,ptrflag);
			}
	}

	// Get the string at the position 'index' of the current array
	// If the element is not a string or if the index doesn't exist
	// => undefined behavior
	public String getString(int index) {
		
		int identifier = db.meta[offset + index] & ~(1<<31);
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
	
	public boolean flag() {
	    return flag;
	}

	protected Node(UniqueDB db, int offset,boolean flag) {
        this.db = db;
        this.offset = offset;
        this.flag = flag;
	}


}