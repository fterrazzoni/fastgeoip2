package com.dataiku.geoip.uniquedb.builder;


public abstract class Buildable {
    
    private UniqueDBBuilder db;
    
    protected Buildable(UniqueDBBuilder db) {
        this.db = db;
    }
    protected UniqueDBBuilder getDB() {
        return db;
    }
    
    protected abstract NodeBuilder build();
    
}
