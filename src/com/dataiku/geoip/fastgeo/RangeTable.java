package com.dataiku.geoip.fastgeo;

import com.dataiku.geoip.uniquedb.Node;


public class RangeTable {

    // Load a Node as a RangeTable
    // For convenience, 'node' can be null (in this case, lookup() will always return null)
    public RangeTable(Node node) {
        this.mainTable = node;
    }
    
    // Find the 'key' in the table using a simple binary search 
	public Node lookup(int key) {

	    if(mainTable == null) {
	        return null;
	    }
	    
	    if(mainTable.flag()) {
	        return mainTable;
	    }
	    
	    if(mainTable.size()==0)
	        return null;
	    
	    int tableSize = mainTable.size()/2;
		int minIdx = 0;
		int maxIdx = tableSize-1;
		
		while (minIdx <= maxIdx) {
		    
			int midIdx = (minIdx + maxIdx) >>> 1;
			int currentKey = mainTable.getInteger(tableSize+midIdx);
			
			if (currentKey < key) {
				minIdx = midIdx + 1;
			} else if (currentKey > key) {
				maxIdx = midIdx - 1;
			} else {
				return mainTable.getNode(midIdx);
			}
		}
		
		if(minIdx>0) {
			return mainTable.getNode(minIdx-1);
		} else {
			return null;
		}
	}

	private final Node mainTable;

}
