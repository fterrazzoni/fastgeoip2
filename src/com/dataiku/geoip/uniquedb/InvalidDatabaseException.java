package com.dataiku.geoip.uniquedb;

public class InvalidDatabaseException extends Exception {

    private static final long serialVersionUID = 2461422826557215943L;

    public InvalidDatabaseException(String m) {
        super(m);
    }

    public InvalidDatabaseException(String m, Throwable t) {
        super(m, t);
    }
}
