package com.dataiku.geoip.uniquedb.builder;


public final class NodeBuilder extends Buildable {
    
    protected GrowableIntArray storage = new GrowableIntArray();
    private boolean deduplicate=true;
    private boolean storeSize=true;
	private boolean flag=false;
	
    
    public NodeBuilder setFlag(boolean v) {
        flag = v;
        return this;
    }
    
    public NodeBuilder add(NodeBuilder array) {
        
        if(array.getDatabase() != getDatabase()) {
            throw new IllegalArgumentException("Cannot add a node from another UniqueDBBuilder");
        }
        
        Integer index = null;
        
        if(deduplicate) {
        	index = getDatabase().map.get(array.storage);
        }
        
        if (index == null) {
            if(array.storeSize) {
                
                int m = array.storage.size();
                getDatabase().meta.add(m);
            }
            index = getDatabase().meta.size();
            for(int i = 0 ; i < array.storage.size() ;i++) {
                getDatabase().meta.add(array.storage.get(i));
            }
            
            if(deduplicate) {
            	getDatabase().map.put(array.storage.clone(),index);
            }
        }
        if(array.flag) {
            index = index | (1<<31);
        } else {
            index = index & ~(1<<31);
        }
        storage.add(index);
        
        return this;
    }
    
	public NodeBuilder storeSize(boolean v) {
		storeSize=v;
		return this;
	}
	
	public NodeBuilder setDeduplication(boolean v) {
		deduplicate=v;
		return this;
	}
    
    public NodeBuilder add(Buildable b) {
        if(b!=null) {
            if(b.getDatabase() != getDatabase()) {
                throw new IllegalArgumentException("Cannot add a node from another UniqueDBBuilder");
            }
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
			Integer index = getDatabase().map.get(string);
			if (index == null) {
				index = getDatabase().meta.size();
				getDatabase().meta.add(getDatabase().data.length());
				getDatabase().data.append(string);
				getDatabase().meta.add(getDatabase().data.length());
				getDatabase().map.put(string, index);
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
    public NodeBuilder build() {
        return this;
    }
    
    @Override
    public NodeBuilder clone()  {
        return new NodeBuilder(getDatabase(),storage.clone(),storeSize,deduplicate);
    }
    
    private NodeBuilder(UniqueDBBuilder db, GrowableIntArray storage, boolean storeSize, boolean deduplicate) {
        super(db);
        this.storage = storage;
        this.storeSize = storeSize;
        this.deduplicate = deduplicate;
    }
    
    public int getInteger(int i) {
        return storage.get(i);
    }
    
}