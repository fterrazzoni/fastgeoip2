package com.dataiku.geoip.uniquedb;

public class InvalidUniqueDBException extends Exception {

    private static final long serialVersionUID = 2461422826557215943L;

    public InvalidUniqueDBException(String m) {
        super(m);
    }

    public InvalidUniqueDBException(String m, Throwable t) {
        super(m, t);
    }
}
