package com.dataiku.geoip.fastgeo;

// Store an IPv6 address 
public class IPv6Address {

	// Parse an IPv4 and store it as an IPv4-mapped IPv6
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

				if (blockSize == 0) {
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
			if (c != '.' && (c < '0' || c > '9')) {
				return null;
			}
		}

		if (blockNum < 4) {
			return null;
		}

		int output[] = new int[4];
		output[0] = Integer.MIN_VALUE;
		output[1] = Integer.MIN_VALUE;
		output[2] = 0x0000FFFF + Integer.MIN_VALUE;
		output[3] = ip;

		return output;
	}

	// Parse an IPv6 and store it into an int[4]
	static private int[] parseIPv6(String addr) {

		int blocks[] = new int[8];
		int blockNum = 0;
		int blockVal = 0;
		int blockSize = 0;
		int magic = -1;

		for (int i = 0; i < addr.length(); i++) {

			char c = addr.charAt(i);

			if (c >= '0' && c <= '9') {
				blockVal = (blockVal << 4) | (c - '0');
				blockSize++;
			} else if (c >= 'a' && c <= 'f') {
				blockVal = (blockVal << 4) | (c - 'a' + 10);
				blockSize++;
			} else if (c >= 'A' && c <= 'F') {
				blockVal = (blockVal << 4) | (c - 'A' + 10);
				blockSize++;
			} else if (c == ':') {

				if (i == addr.length() - 1 || blockNum > 6) {
					// - got a single ending ':'
					// - cannot have more than 8 blocks
					return null;
				}

				blocks[blockNum] = blockVal;
				blockVal = 0;
				blockSize = 0;
				blockNum++;

				if (addr.charAt(i + 1) == ':') {

					if (magic >= 0) {
						// only one '::' is allowed 
						return null;
					}
					
					if(i+2 < addr.length() && addr.charAt(i+2)==':') {
						// forbid more than 2 ":"
						return null;
					}
					
					
					magic = blockNum;
					i++;

				}

			} else {
				// unrecognized character
				return null;
			}

			// each block = max 4 characters
			if (blockSize > 4) {
				return null;
			}

		}
		blocks[blockNum] = blockVal;

		// expand the optional '::'
		if (magic >= 0) {
			int nbMissingBlocks = 7 - blockNum;

			if (nbMissingBlocks == 0)
				return null;

			for (int i = blockNum - magic; i >= 0; i--) {
				int from = magic + i;
				int to = from + nbMissingBlocks;
				blocks[to] = blocks[from];
				blocks[from] = 0;
			}
		} else if(blockNum!=7) {
			return null;
		}

		int[] output = new int[4];
		for (int i = 0; i < 4; i++) {
			output[i] = Integer.MIN_VALUE + ((blocks[i * 2] << 16) + blocks[i * 2 + 1]);
		}

		return output;

	}
	
	public int[] parseIP(String ipStr) {
	    
	    int lastColonPosition = ipStr.lastIndexOf(":");
	    
	    // IPv6 format
	    if (lastColonPosition!=-1) {
	        
	        // IPv4-mapped IPv6 "mixed notation"
	        if(ipStr.indexOf(".")!=-1) {
	            
	            // Check the IPv6 part
	            int storage[] = parseIPv6(ipStr.substring(0,lastColonPosition)+":0");
	           
	            if(storage != null 
	                    && storage[0] == Integer.MIN_VALUE 
	                    && storage[1] == Integer.MIN_VALUE
	                    && (storage[2] == Integer.MIN_VALUE + 0x0000FFFF 
	                        || storage[2] == Integer.MIN_VALUE)
	                    && storage[3] == Integer.MIN_VALUE) {
	                
	                // IPv6 part is valid, parse the IPv4 part
	                return parseIPv4(ipStr.substring(lastColonPosition+1));
	                
	            } else {
	                // Invalid IPv6 part
	                return null;
	            }
	        } 
	        // Standard IPv6 notation
	        else {
	            return parseIPv6(ipStr);
	        }
	        
        } else {
            return parseIPv4(ipStr);
        }

	    
	}
	
	// Useful for debugging
	@Override
	public String toString() {
		String out = "";
		for (int i = 0; i < 4; i++) {
			int v = storage[i] - Integer.MIN_VALUE;
			String hStr = Integer.toString((v & 0xFFFF0000) >>> 16, 16);
			String lStr = Integer.toString(v & 0x0000FFFF, 16);
			while (hStr.length() < 4) {
				hStr = "0" + hStr;
			}
			while (lStr.length() < 4) {
				lStr = "0" + lStr;
			}
			out += hStr + ":" + lStr + (i != 3 ? ":" : "");
		}
		return out;
	}

	

	private int storage[];

	public IPv6Address(String ipStr) throws IllegalArgumentException {

		storage = parseIP(ipStr);

		if (storage == null) {
			throw new IllegalArgumentException("Unable to parse IP address \"" + ipStr+"\"");
		}

	}

	public IPv6Address(int ip[]) throws IllegalArgumentException {
		if (ip == null || ip.length != 4) {
			throw new IllegalArgumentException("'ip' must be encoded as int[4]");
		}
		storage = ip;
	}
	
	// Return the internal int[4] storing the IP
	public int[] internalRepresentation() {
        return storage;
    }
	
	// It is an IPv4-mapped IPv6 address ?
	public boolean isIPv4() {
	    return storage[0] == Integer.MIN_VALUE 
	            && storage[1] == Integer.MIN_VALUE 
	            && (storage[2] == Integer.MIN_VALUE+0x0000FFFF || storage[2] == Integer.MIN_VALUE);
	    
	}

}
