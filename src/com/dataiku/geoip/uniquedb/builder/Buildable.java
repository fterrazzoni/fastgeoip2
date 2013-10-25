package com.dataiku.geoip.uniquedb.builder;


public abstract class Buildable {
    
    private UniqueDBBuilder database;
    
    protected Buildable(UniqueDBBuilder database) {
        this.database = database;
    }
    protected UniqueDBBuilder getDatabase() {
        return database;
    }
    
    protected abstract NodeBuilder build();
    
}
