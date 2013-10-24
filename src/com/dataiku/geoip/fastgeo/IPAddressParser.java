package com.dataiku.geoip.fastgeo;

public class IPAddressParser {

    // Parse an IPv4 and store it into an integer
    static public int parseIPv4(String addr) throws InvalidIPAddress {
    	
        if (addr == null || addr.isEmpty()) {
            throw new InvalidIPAddress("Invalid IP address (error : cannot be null or empty)");
        }
        
        int ip = Integer.MIN_VALUE;
        int blockVal = 0;
        int blockSize = 0;
        int blockNum = 0;

        for (int i = 0; i < addr.length(); i++) {

            char c = addr.charAt(i);

            if (c >= '0' && c <= '9') {
                blockVal = blockVal * 10 + c - '0';
                blockSize++;
            }

            if (c == '.' || i == addr.length() - 1) {

                if (blockVal < 0 || blockVal > 255) {
                    throw new InvalidIPAddress("Invalid IP address (error : value is not in [0, 255])");
                }
                
                if(blockSize==0) {
                    throw new InvalidIPAddress("Invalid IP address (error : missing part value)");
                }

                ip += (blockVal << (24 - (8 * blockNum)));
                blockVal = 0;
                blockSize = 0;
                blockNum++;
            }

            if (blockNum > 4) {
                throw new InvalidIPAddress("Invalid IP address (error : number of parts cannot be > 4)");
            }
            if(c != '.' && (c < '0' || c > '9')) {
                throw new InvalidIPAddress("Invalid IP address (error : invalid character '"+c+"')");
            }
        }

        if (blockNum < 4) {
            throw new InvalidIPAddress("Invalid IP address (error : number of parts cannot be < 4)");
        }
        

        return ip;
    }
    
    static public int[] parseIPv6(String addr) throws InvalidIPAddress {
		
    	return null;
    	
    	
    	
    	
    }
    
    
}
