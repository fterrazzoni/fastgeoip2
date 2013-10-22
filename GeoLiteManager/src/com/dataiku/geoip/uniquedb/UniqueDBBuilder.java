package com.dataiku.geoip.uniquedb;

import java.util.HashMap;

// UniqueDBBuilder : UniqueDB construction
public final class UniqueDBBuilder extends NodeBuilder {

	// Create a new builder
	public UniqueDBBuilder() {
		super(new StringBuilder(), new GrowableIntArray(),
				new HashMap<Object, Integer>(),true);
		meta.add(-1);
	}



	// Build an immutable UniqueDB from the current builder state
	public UniqueDB constructDatabase() {
	    
	    if(writeSize) {
	        meta.add(storage.size());
	    }
		int index = meta.size();
		for (int i = 0; i < storage.size(); i++) {
			meta.add(storage.get(i));
		}
		meta.set(0, index);

		String nativeData = this.data.toString();
		int[] nativeMeta = new int[meta.size()];
		for (int i = 0; i < nativeMeta.length; i++) {
			nativeMeta[i] = meta.get(i);
		}

		return new UniqueDB(nativeData, nativeMeta);
	}
	


}
