package com.dataiku.geoip.fastgeo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.dataiku.geoip.uniquedb.InvalidDatabaseException;
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
    public FastGeoIP2(File file) throws InvalidDatabaseException {

        try (

        FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);
                DataInputStream dis = new DataInputStream(bis)) {

            initialize(UniqueDB.loadFromStream(dis));

        } catch (IOException e) {

            throw new InvalidDatabaseException("Unable to open FastGeoIP2 database (I/O error)", e);

        } catch (InvalidDatabaseException e) {

            throw new InvalidDatabaseException("Invalid FastGeoIP2 database (corrupted UniqueDB)", e);

        }

    }

    // Save the FastGeoIP2 database to a file
    public void saveToFile(File file) throws IOException {

        FileOutputStream fos = new FileOutputStream(file);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        DataOutputStream dos = new DataOutputStream(bos);
        try {
            database.writeToStream(dos);
        } finally {
            dos.close();
        }
    }

    static final public class Result {

        private Node root;

        Result(Node root) {
            this.root = root;
        }

        public String getCity() {
            return root.getString(3);
        }

        public String getPostalCode() {
            return root.getString(2);
        }

        public String getCountryCode() {
            return root.getNode(4).getString(2);
        }

        public String getTimezone() {
            return root.getNode(4).getString(3);
        }

        public String getCountry() {
            return root.getNode(4).getString(1);
        }

        public String getContinent() {
            return root.getNode(4).getNode(4).getString(0);
        }

        public String getContinentCode() {
            return root.getNode(4).getNode(4).getString(1);
        }

        public String getLatitude() {
            return root.getString(0);
        }

        public String getLongitude() {
            return root.getString(1);
        }

        static public class Subdivision {
            public String name;
            public String code;
        }

        public List<Subdivision> getSubdivisions() {
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

    // Construct a FastGeoIP2 using an already loaded UniqueDB
    public FastGeoIP2(UniqueDB db) throws InvalidDatabaseException {
        initialize(db);
    }

    public Result find(IPv6Address addr) {

        int ip[] = addr.internalRepresentation();
        
        Node node = null;
        
        if(addr.isIPv4()) {
            
            RangeTable table = ipv4Table;
            node = table.lookup(ip[3]);
            
        } else {
        
            RangeTable table = ipv6Table;
            
            table = new RangeTable(table.lookup(ip[0]));
            table = new RangeTable(table.lookup(ip[1]));
            table = new RangeTable(table.lookup(ip[2]));
            
            node = table.lookup(ip[3]);
        
        } 
        
        if(node != null) {
            return new Result(node);
        } 
        
        return null;

    }

    private void initialize(UniqueDB db) throws InvalidDatabaseException {
        
        // save db
        this.database = db;
        
        // check version
        if (db.root().size() != 3 
                || db.root().getInteger(0) != FGDB_MARKER 
                || db.root().getInteger(1) != VERSION_ID) {
            throw new InvalidDatabaseException("Cannot load FastGeoIP2 database (invalid database or incompatible version)");
        }
        
        // Load IPv6 table
        ipv6Table = new RangeTable(db.root().getNode(2));
        
        // Load IPv4 table
        ipv4Table = new RangeTable(ipv6Table.lookup(Integer.MIN_VALUE));
        ipv4Table = new RangeTable(ipv4Table.lookup(Integer.MIN_VALUE));
        ipv4Table = new RangeTable(ipv4Table.lookup(Integer.MIN_VALUE+0x0000FFFF));
        
    }

    private RangeTable ipv6Table;
    private RangeTable ipv4Table;
    private UniqueDB database;
    
    public static final int VERSION_ID = 2;
    public static final int FGDB_MARKER = 1181889348;

}
