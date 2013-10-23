package com.dataiku.geoip.uniquedb.builder;



public class NodeBuilder extends Buildable {
    
    final protected boolean storeSize;
    final protected GrowableIntArray storage;
    
    public NodeBuilder add(NodeBuilder array) {
        
        Integer index = getDB().map.get(array.storage);
        if (index == null) {
            if(array.storeSize) {
                getDB().meta.add(array.storage.size());
            }
            index = getDB().meta.size();
            for(int i = 0 ; i < array.storage.size() ;i++) {
                getDB().meta.add(array.storage.get(i));
            }
            getDB().map.put(array.storage.clone(),index);
        }
        storage.add(index);
        
        return this;
    }

    
    final public NodeBuilder add(Buildable b) {
        if(b!=null) {
            add(b.build());
        } else {
            storage.add(-1);
        }
        return this;
    }

	final public NodeBuilder add(String string) {

		if (string == null) {
			storage.add(-1);
		} else {
			Integer index = getDB().map.get(string);
			if (index == null) {
				index = getDB().meta.size();
				getDB().meta.add(getDB().data.length());
				getDB().data.append(string);
				getDB().meta.add(getDB().data.length());
				getDB().map.put(string, index);
			}
			
			storage.add(index);
		}
		
		return this;
	}

	final public NodeBuilder add(int value) {
		storage.add(value);
		return this;
	}
	
	private NodeBuilder(UniqueDBBuilder db, boolean storeSize) {
	    super(db);
        this.storeSize = storeSize;
        this.storage = new GrowableIntArray();
	}
	
	public static NodeBuilder newArray(UniqueDBBuilder db) {
	    return new NodeBuilder(db,true);
	}
	
	public static NodeBuilder newStruct(UniqueDBBuilder db) {
	    return new NodeBuilder(db,false);
	}

    @Override
    protected NodeBuilder build() {
        return this;
    }
  
}