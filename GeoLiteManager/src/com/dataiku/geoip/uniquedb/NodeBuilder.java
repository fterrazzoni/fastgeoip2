package com.dataiku.geoip.uniquedb;

import java.util.ArrayList;
import java.util.HashMap;

public class NodeBuilder {

    // Add a string
    // Notice: the string is inserted immediately
    // (and it'll be stored forever even if the current array is never added!)
    final public NodeBuilder add(String string) {

        if (string == null) {
            storage.add(-1);
        } else {
            Integer index = map.get(string);
            if (index == null) {
                index = meta.size();
                meta.add(data.length());
                data.append(string);
                meta.add(data.length());
                map.put(string, index);
            }

            storage.add(index);
        }

        return this;
    }

    // Add a node (either array or struct)
    // Notice: the node content is copied immediately
    // Consequences:
    // - It'll be stored forever even if the current node is never added, resulting in a leak
    // - Further modifications of this node won't be reflected in the database
    final public NodeBuilder add(NodeBuilder node) {

        if (node == null) {
            storage.add(-1);
        } else {
            Integer index = map.get(node.storage);
            if (index == null) {
                
                if (node.isArray) {
                    meta.add(node.storage.size());
                }
                index = meta.size();
                meta.addAll(node.storage);
                map.put(node.storage.clone(), index);
            }
            storage.add(index);
        }

        return this;
    }

    // Add an integer to this node
    final public NodeBuilder add(int value) {
        storage.add(value);
        return this;
    }
    
    // Create a new array node
    // Notice : the newly created array cannot be used on another builder!
    final public NodeBuilder array() {
        return new NodeBuilder(data, meta, map,true);
    }
    
    // Create a new struct node
    // The only difference between a struct and an array is that the structure doesn't store its own size
    // Notice : the newly created array cannot be used on another builder!
    final public NodeBuilder struct() {
        return new NodeBuilder(data, meta, map,false);
    }

    public NodeBuilder(StringBuilder data, ArrayList<Integer> meta, HashMap<Object, Integer> map, boolean isArray) {
        this.data = data;
        this.meta = meta;
        this.map = map;
        this.isArray = isArray;
    }

    protected StringBuilder data;
    protected ArrayList<Integer> meta;
    protected HashMap<Object, Integer> map;
    protected ArrayList<Integer> storage = new ArrayList<Integer>();
    private boolean isArray;
}