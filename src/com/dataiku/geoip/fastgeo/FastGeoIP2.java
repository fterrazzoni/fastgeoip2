package com.dataiku.geoip.fastgeo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import com.dataiku.geoip.uniquedb.InvalidUniqueDBException;
import com.dataiku.geoip.uniquedb.Node;
import com.dataiku.geoip.uniquedb.UniqueDB;

// FastGeoIP2 tries to solve a major drawback of GeoLite2 : a very slow API
//
// This implementation uses another file format which is less space-efficient but
// easier to lookup quickly. The whole file is stored in RAM for efficiency.
//
// Features: 
// - Localize any IPv4 address
// - Provide only a subset of the fields available in GeoLite2 (but it's easy to add more...)
// - Much faster than the GeoLite2 Java API (30x)
public class FastGeoIP2 {


	// Instantiate a new FastGeoIP2 from a file
    public FastGeoIP2(File file) throws InvalidFastGeoIP2DatabaseException {

        try (

                FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);
                DataInputStream dis = new DataInputStream(bis)) 
        {
            
            initialize(UniqueDB.loadFromStream(dis));

        } catch (IOException e) {

            throw new InvalidFastGeoIP2DatabaseException("Unable to open FastGeoIP2 database (I/O error)", e);

        } catch (InvalidUniqueDBException e) {

            throw new InvalidFastGeoIP2DatabaseException("Invalid FastGeoIP2 database (corrupted UniqueDB)", e);

        }

    }
    

    // Find an IPv4 address in the database (anything else will throw an InvalidIPAddress!)
    // Return null if the IP has not been found, or a Result object
    public Result find(String addr) throws InvalidIPAddress {

        int ip = IPAddressParser.parseIPv4(addr);
 
        int index = findIndex(ip);

        Node data = dataTable.getNode(index);

        if (data != null) {

            return new Result(data);

        } else {

            return null;
        }
    }

    // Save the FastGeoIP2 database to a file
    public void saveToFile(File file) throws IOException {

        FileOutputStream fos = new FileOutputStream(file);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        DataOutputStream dos = new DataOutputStream(bos);
        try {
            db.writeToStream(dos);
        } finally {
            dos.close();
        }
    }

    static public class Result {

        private Node root;

        Result(Node root) {
            this.root = root;
        }

        final public String getCity() {
            return root.getString(3);
        }

        final public String getPostalCode() {
            return root.getString(2);
        }

        final public String getCountryCode() {
            return root.getNode(4).getString(2);
        }

        final public String getTimezone() {
            return root.getNode(4).getString(3);
        }

        final public String getCountry() {
            return root.getNode(4).getString(1);
        }

        final public String getContinent() {
            return root.getNode(4).getNode(4).getString(0);
        }

        final public String getContinentCode() {
            return root.getNode(4).getNode(4).getString(1);
        }

        final public String getLatitude() {
            return root.getString(0);
        }

        final public String getLongitude() {
            return root.getString(1);
        }

        final static public class Subdivision {
            public String name;
            public String code;
        }

        final public List<Subdivision> getSubdivisions() {
            Node arr = root.getNode(4).getNode(0);
            ArrayList<Subdivision> list = new ArrayList<Subdivision>();
            for (int i = 0; i < arr.size(); i++) {
                Subdivision sub = new Subdivision();
                sub.name = arr.getNode(i).getString(0);
                sub.code = arr.getNode(i).getString(1);
                list.add(sub);
            }
            return list;
        }

    }

    // Find the record index of an IP address in the lookup table (binary search)
    private int findIndex(int queryIP) {
    	
    	int minIdx = 0;
    	int maxIdx = ipTable.size()-1;
    	
    	while(minIdx <= maxIdx) {
    		
    		int midIdx = (minIdx+maxIdx) >>> 1;
    		int currentIP = ipTable.getInteger(midIdx);
    		
    		if(currentIP<queryIP) {
    			minIdx = midIdx+1;
    		} else if(currentIP>queryIP) {
    			maxIdx = midIdx-1;
    		} else {
    			return midIdx;
    		}
    	}

    	return minIdx-1;
    }

    // Construct a FastGeoIP2 using an already loaded UniqueDB
    public FastGeoIP2(UniqueDB db) throws InvalidFastGeoIP2DatabaseException {
        initialize(db);
    }
    
    public Result find(InetAddress addr) {
    	
		IntBuffer intBuf = ByteBuffer
				.wrap(addr.getAddress())
				.order(ByteOrder.BIG_ENDIAN)
				.asIntBuffer();
		
		int[] words = new int[intBuf.remaining()];
		intBuf.get(words);
		
		for(int i = 0 ; i < words.length; i++) {
			words[i] += Integer.MIN_VALUE;
		}
    	
    	if(words.length!=4)
    		return null;

    	Node node = db.root().getNode(2);

    	for(int i = 0 ; i < 4 && node != null; i++) {
    		node = new RangeTable(node).lookup(words[i]);
    	}
    	
    	if(node!=null) {
	    	return new Result(node);
    	} 
    	
    	return null;
    	
    }
    
    private void initialize(UniqueDB db) throws InvalidFastGeoIP2DatabaseException {
        this.db = db;
        
        if(db.root().size() != 3 || db.root().getInteger(0) != FGDB_MARKER || db.root().getInteger(1) != VERSION_ID) {
        	throw new InvalidFastGeoIP2DatabaseException("Cannot load FastGeoIP2 database (invalid database or incompatible version)");
        }
    }


    private UniqueDB db;
    private Node dataTable;
    private Node ipTable;

	public static final int VERSION_ID = 2;
	public static final int FGDB_MARKER = 1181889348;

}
