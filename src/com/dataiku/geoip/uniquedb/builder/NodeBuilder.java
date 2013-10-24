package com.dataiku.geoip.uniquedb.builder;


public class NodeBuilder extends Buildable {
    
    final protected GrowableIntArray storage = new GrowableIntArray();
	private boolean deduplicate=true;
	private boolean storeSize=true;
    
    public NodeBuilder add(NodeBuilder array) {
        
        Integer index = null;
        
        if(deduplicate) {
        	index = getDB().map.get(array.storage);
        }
        
        if (index == null) {
            if(array.storeSize) {
                getDB().meta.add(array.storage.size());
            }
            index = getDB().meta.size();
            for(int i = 0 ; i < array.storage.size() ;i++) {
                getDB().meta.add(array.storage.get(i));
            }
            
            if(deduplicate) {
            	getDB().map.put(array.storage.clone(),index);
            }
        }
        storage.add(index);
        
        return this;
    }
    
	public NodeBuilder setStoreSize(boolean v) {
		storeSize=v;
		return this;
	}
	
	public NodeBuilder setDeduplication(boolean v) {
		deduplicate=v;
		return this;
	}
    
    public NodeBuilder add(Buildable b) {
        if(b!=null) {
            add(b.build());
        } else {
            storage.add(-1);
        }
        return this;
    }
    
    public int size() {
    	return storage.size();
    }

	public NodeBuilder add(String string) {

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

	public NodeBuilder add(int value) {
		storage.add(value);
		return this;
	}


	public NodeBuilder(UniqueDBBuilder db) {
	    super(db);
	}

    @Override
    protected NodeBuilder build() {
        return this;
    }
  
}