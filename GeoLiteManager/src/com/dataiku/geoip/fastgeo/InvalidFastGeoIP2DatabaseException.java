package com.dataiku.geoip.fastgeo;

public class InvalidFastGeoIP2DatabaseException extends Exception {

    private static final long serialVersionUID = -8325017694610021887L;

    public InvalidFastGeoIP2DatabaseException(String m) {
        super(m);
    }

    public InvalidFastGeoIP2DatabaseException(String m, Throwable t) {
        super(m, t);
    }
}
