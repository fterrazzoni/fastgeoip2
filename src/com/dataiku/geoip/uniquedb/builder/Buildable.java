package com.dataiku.geoip.uniquedb.builder;


public abstract class Buildable {
    
    private UniqueDBBuilder database;
    
    protected Buildable(UniqueDBBuilder database) {
        this.database = database;
    }
    
    public UniqueDBBuilder getDatabase() {
        return database;
    }
    
    public abstract NodeBuilder build();
    
}
