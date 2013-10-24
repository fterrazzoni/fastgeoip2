package com.dataiku.geoip.fastgeo;

public class IPAddress {
    

    
    
    // Parse an IPv4 and store it into an integer
    static private int[] parseIPv4(String addr) {
    	
        if (addr == null || addr.isEmpty()) {
            return null;
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
                    return null;
                }
                
                if(blockSize==0) {
                    return null;
                }

                ip += (blockVal << (24 - (8 * blockNum)));
                blockVal = 0;
                blockSize = 0;
                blockNum++;
            }

            if (blockNum > 4) {
                return null;
            }
            if(c != '.' && (c < '0' || c > '9')) {
                return null;
            }
        }

        if (blockNum < 4) {
            return null;
        }

        int output[] = new int[4];
        output[0] = Integer.MIN_VALUE;
        output[1] = Integer.MIN_VALUE;
        output[2] = Integer.MIN_VALUE;
        output[3] = ip ;
        
        return output;
    }

    
    
    // TODO : safety checks
    static private int[] parseIPv6(String addr) {
		
        
        int blocks[] = new int[8];
        int blockNum = 0;
        int blockVal = 0;
        int magic = -1;
        
        for(int i = 0 ; i < addr.length() ; i++) {
            
            char c = addr.charAt(i);
            
            if(c>='0' && c<='9') {
                blockVal = (blockVal << 4)  | (c-'0');   
            }
            else if(c>='a' && c <= 'f') {
                blockVal = (blockVal << 4)  | (c-'a'+10);  
            }
            else if(c == ':') {
                
                blocks[blockNum] = blockVal;
                blockVal = 0;
                blockNum++;
                if(blockNum==8) {
                    return null;
                }
                if(i!=addr.length()-1 && addr.charAt(i+1)==':') {
                    if(magic>=0) {
                        return null;
                    }
                    magic = blockNum;
                    i++;
                }
            }
        }
        blocks[blockNum] = blockVal;
        
        if(magic>=0) {
            // Not implemented!!!!
            // TODO
        }
        
        int[] output = new int[4];
        for(int i = 0 ; i < 4; i++) {
            output[i] = Integer.MIN_VALUE + ((blocks[i*2] << 16) + blocks[i*2+1]);
        } 
        
        return output;
    	
    }
    
    public int[] getIntRepresentation() {
        return storage;
    }
    
    private int storage[];
    
    public IPAddress(String ipStr) throws InvalidIPAddress {
        
        if(ipStr.contains(":")) {
            storage = parseIPv6(ipStr);
        } else {
            storage = parseIPv4(ipStr);
        }
        
        if(storage == null) {
            throw new InvalidIPAddress("Unable to parse IP address : "+ipStr);
        }
        
    }

}
