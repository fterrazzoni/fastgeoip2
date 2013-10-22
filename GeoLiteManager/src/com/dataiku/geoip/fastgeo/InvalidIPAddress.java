package com.dataiku.geoip.fastgeo;

public class InvalidIPAddress extends Exception {

    private static final long serialVersionUID = 36970936991328748L;
    
    public InvalidIPAddress(String message) {
        super(message);
    }
    
    public InvalidIPAddress(String message,Throwable throwable) {
        super(message,throwable);
    }

}
