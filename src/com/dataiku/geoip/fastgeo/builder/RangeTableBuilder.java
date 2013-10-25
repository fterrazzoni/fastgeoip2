package com.dataiku.geoip.fastgeo.builder;


import com.dataiku.geoip.uniquedb.builder.GrowableIntArray;
import com.dataiku.geoip.uniquedb.builder.NodeBuilder;
import com.dataiku.geoip.uniquedb.builder.Buildable;
import com.dataiku.geoip.uniquedb.builder.UniqueDBBuilder;

public class RangeTableBuilder extends Buildable {

    NodeBuilder nodeTable;
    Buildable lastInsert;
    GrowableIntArray keyTempTable;
    
    public RangeTableBuilder orderedAdd(int key, Buildable node) {
        
        
        keyTempTable.add(key);
        nodeTable.add(node);
        lastInsert=node;
        
        return this;
    }
    
    
    public RangeTableBuilder(UniqueDBBuilder db) {
    	
        super(db);
        
        keyTempTable = new GrowableIntArray();
        nodeTable = new NodeBuilder(db);
    }
    
    @Override
    public NodeBuilder build() {
        
        
        if(keyTempTable.size()!=1) {
            // We work on a copy because the build() operation 
            // should not mutate the current object
        	NodeBuilder table  = nodeTable.clone();
        	
        	for(int i = 0 ; i < keyTempTable.size(); i++) {
        	    table.add(keyTempTable.get(i));
        	}
        	return  table;
        	
        } else {
            NodeBuilder table  = nodeTable.clone();
            table.add(lastInsert.build().setFlag(true));
            return table;
            
        }
        
    }
    
}
