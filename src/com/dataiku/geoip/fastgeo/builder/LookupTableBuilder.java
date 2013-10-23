package com.dataiku.geoip.fastgeo.builder;

import com.dataiku.geoip.uniquedb.builder.NodeBuilder;
import com.dataiku.geoip.uniquedb.builder.Buildable;
import com.dataiku.geoip.uniquedb.builder.UniqueDBBuilder;

public class LookupTableBuilder extends Buildable {

    NodeBuilder keyTable;
    NodeBuilder nodeTable;
    
    public void orderedAdd(int key, Buildable node) {
        keyTable.add(key);
        nodeTable.add(node);
    }
    
    private LookupTableBuilder(UniqueDBBuilder db) {
        
        super(db);
        
        keyTable = NodeBuilder.newArray(db);
        nodeTable = NodeBuilder.newArray(db);
    }
    
    static LookupTableBuilder newLookupTable(UniqueDBBuilder db) {
        return new LookupTableBuilder(db);
    }
    
    @Override
    public NodeBuilder build() {
        return NodeBuilder.newArray(getDB()).add(keyTable).add(nodeTable);
    }
    
}
