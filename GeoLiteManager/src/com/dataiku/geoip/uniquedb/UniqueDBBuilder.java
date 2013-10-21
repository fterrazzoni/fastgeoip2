package com.dataiku.geoip.uniquedb;

import java.util.ArrayList;
import java.util.HashMap;

// UniqueDBBuilder : UniqueDB construction
public final class UniqueDBBuilder extends NodeBuilder {

	// Create a new builder
	public UniqueDBBuilder() {
		super(new StringBuilder(), new ArrayList<Integer>(),
				new HashMap<Object, Integer>(),false);
		meta.add(-1);
	}



	// Build an immutable UniqueDB from the current builder state
	public UniqueDB constructDatabase() {

		int index = meta.size();
		meta.addAll(storage);
		meta.set(0, index);

		String nativeData = this.data.toString();
		int[] nativeMeta = new int[meta.size()];
		for (int i = 0; i < nativeMeta.length; i++) {
			nativeMeta[i] = meta.get(i);
		}

		return new UniqueDB(nativeData, nativeMeta);
	}

}
