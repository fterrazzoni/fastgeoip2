package com.dataiku.geoip.uniquedb.builder;

import java.util.HashMap;

import com.dataiku.geoip.uniquedb.UniqueDB;

// UniqueDBBuilder : UniqueDB construction
public final class UniqueDBBuilder {

    StringBuilder data;
    GrowableIntArray meta;
    final  HashMap<Object, Integer> map;
     
	// Create a new builder
	public UniqueDBBuilder() {
	    
	    this.data = new StringBuilder();
	    this.meta = new GrowableIntArray();
	    this.map = new HashMap<Object,Integer>();
	    meta.add(-1);
	    
		root = NodeBuilder.newArray(this);
		
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
