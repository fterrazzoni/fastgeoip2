package com.dataiku.geoip.fastgeo.builder;


import com.dataiku.geoip.uniquedb.builder.GrowableIntArray;
import com.dataiku.geoip.uniquedb.builder.NodeBuilder;
import com.dataiku.geoip.uniquedb.builder.Buildable;
import com.dataiku.geoip.uniquedb.builder.UniqueDBBuilder;

public class RangeTableBuilder extends Buildable {

    NodeBuilder nodeTable;
    NodeBuilder keyTable;
    int keySize;
    
    
    public RangeTableBuilder orderedAdd(int keys[], Buildable node) {
    	
        for(int i = 0 ; i < keySize ; i++ ){
            keyTable.add(keys[i]);
        }
        
        nodeTable.add(node);
        
        return this;
    }
    public int size() {
        return nodeTable.size();
    }
    
    public RangeTableBuilder(UniqueDBBuilder db, int keySize) {
    	
        super(db);
        this.keySize = keySize;
        this.keyTable = new NodeBuilder(db);
        this.nodeTable = new NodeBuilder(db);
    }
    
    @Override
    public NodeBuilder build() {
        
        return new NodeBuilder(getDatabase())
                    .add(keySize)
                    .add(keyTable)
                    .add(nodeTable);
        
    }
    
}
