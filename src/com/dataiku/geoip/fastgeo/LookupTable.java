package com.dataiku.geoip.fastgeo;

import com.dataiku.geoip.uniquedb.Array;

// 
public class LookupTable extends Array {

   public Array findIndex(int key) {
        
        int minIdx = 0;
        int maxIdx = keyTable.size()-1;
        
        if(maxIdx<minIdx) {
            return null;
        }
        
        while(minIdx <= maxIdx) {
            
            int midIdx = (minIdx+maxIdx) >>> 1;
            int currentIP = keyTable.getInteger(midIdx);
            
            if(currentIP<key) {
                minIdx = midIdx+1;
            } else if(currentIP>key) {
                maxIdx = midIdx-1;
            } else {
                return nodeTable.getNode(midIdx);
            }
        }

        return nodeTable.getNode(minIdx-1);
    }
    
    
    private final Array keyTable;
    private final Array nodeTable;
    
    private LookupTable(Array n) {
        
        super(n);
        
        keyTable = n.getNode(0);
        nodeTable = n.getNode(1);
        
    }
    
    static public LookupTable readFromNode(Array n, int index) {
        return new LookupTable(n.getNode(index));
    }
    
}
