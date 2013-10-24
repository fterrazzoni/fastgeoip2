package com.dataiku.geoip.fastgeo.builder;

import com.dataiku.geoip.uniquedb.builder.NodeBuilder;
import com.dataiku.geoip.uniquedb.builder.Buildable;
import com.dataiku.geoip.uniquedb.builder.UniqueDBBuilder;

public class RangeTableBuilder extends Buildable {

    NodeBuilder keyTable;
    NodeBuilder nodeTable;
    
    public RangeTableBuilder orderedAdd(int key, Buildable node) {
    	
        keyTable.add(key);
        nodeTable.add(node);
        
        return this;
    }
    
    private RangeTableBuilder(UniqueDBBuilder db) {
    	
        super(db);
        
        keyTable = new NodeBuilder(db);
        nodeTable = new NodeBuilder(db).setStoreSize(false);
    }
    
    static RangeTableBuilder newLookupTable(UniqueDBBuilder db) {
        return new RangeTableBuilder(db);
    }
    
    @Override
    public NodeBuilder build() {
    	
        return  new NodeBuilder(getDB()).setStoreSize(false).add(keyTable).add(nodeTable);
    }
    
}
