package com.dataiku.geoip.uniquedb.builder;

import java.util.HashMap;

import com.dataiku.geoip.uniquedb.UniqueDB;

// UniqueDBBuilder : UniqueDB construction
public final class UniqueDBBuilder {

    protected StringBuilder data;
    protected GrowableIntArray meta;
    protected HashMap<Object, Integer> map;
     
	// Create a new builder
	public UniqueDBBuilder() {
	    
		meta = new GrowableIntArray();
	    meta.add(-1);
	    
	    data = new StringBuilder();
	    map = new HashMap<Object,Integer>();
		root = new NodeBuilder(this);
		
	}

	// Build an immutable UniqueDB from the current builder state
	public UniqueDB constructDatabase() {
	    
	    meta.add(root.storage.size());
		int index = meta.size();
		for (int i = 0; i < root.storage.size(); i++) {
			meta.add(root.storage.get(i));
		}
		meta.set(0, index);
		String nativeData = this.data.toString();
		int[] nativeMeta = new int[meta.size()];
		for (int i = 0; i < nativeMeta.length; i++) {
			nativeMeta[i] = meta.get(i);
		}
		return new UniqueDB(nativeData, nativeMeta);
	}
	
	public NodeBuilder root() {
	    return root;
	}
	
	private NodeBuilder root;

}
