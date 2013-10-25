    package com.dataiku.geoip.fastgeo.builder;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import com.dataiku.geoip.fastgeo.FastGeoIP2;
import com.dataiku.geoip.mmdb.Reader;
import com.dataiku.geoip.uniquedb.InvalidDatabaseException;
import com.dataiku.geoip.uniquedb.UniqueDB;
import com.dataiku.geoip.uniquedb.builder.Buildable;
import com.dataiku.geoip.uniquedb.builder.NodeBuilder;
import com.dataiku.geoip.uniquedb.builder.UniqueDBBuilder;
import com.fasterxml.jackson.databind.JsonNode;

// Convert a GeoLite2 MMDB file to a FastGeoIP2 database
public class FastGeoIP2Builder {

    // Progress listener (the conversion process takes some time!)
    static public interface Listener {
        public void progress(int processed, int total);
    }
    
    private RangeTableBuilder ipv6Tables[] = new RangeTableBuilder[4];
    private int lastIPv6[];
    

    // Construct the builder with a GeoLite2 database
    FastGeoIP2Builder(File mmdbFile) {
        geoliteFile = mmdbFile;
    }

    // Start the conversion procedure
    FastGeoIP2 build(Listener listener) throws IOException {
        
        db = new UniqueDBBuilder();
        
        try (Reader geoliteDatabase = new Reader(geoliteFile)) {
            
            List<InetAddress> ranges = geoliteDatabase.getRanges(128);

            for (int i = 0; i < ranges.size(); i++) {
            	
                InetAddress addr = ranges.get(i);
                JsonNode node = geoliteDatabase.get(addr);
                
                insert(addr, node);
                
                if (listener != null) {
                    listener.progress(1 + i, ranges.size());
                }
                	
            }
            
            db.root().add(FastGeoIP2.FGDB_MARKER);
            db.root().add(FastGeoIP2.VERSION_ID);
            
            ipv6Tables[2].orderedAdd(lastIPv6[2], ipv6Tables[3]);
            ipv6Tables[1].orderedAdd(lastIPv6[1], ipv6Tables[2]);
            ipv6Tables[0].orderedAdd(lastIPv6[0], ipv6Tables[1]);
            
            
            db.root().add(ipv6Tables[0]);

            UniqueDB udb = db.constructDatabase();
            return new FastGeoIP2(udb);

        } catch (InvalidDatabaseException e) {
            throw new RuntimeException("This is a bug: the FastGeoIP2Builder generated an invalid database",e);
        }
    }

    static public int[] inet2ints(InetAddress addr) {
        byte[] bytes = addr.getAddress();
        int[] out = new int[bytes.length/4];
        
        for(int i = 0 ; i < bytes.length; i+=4) {
            
            out[i/4] = (int) (
                    (((bytes[i] & 0xFFL) << 24) 
                  | ((bytes[i+1] & 0xFFL) << 16) 
                  | ((bytes[i+2] & 0xFFL) << 8) 
                  |  (bytes[i+3] & 0xFFL))
                  + Integer.MIN_VALUE);
        }
        
        return out;
    }
    
    
    // Insert an IP record using the data contained in a JsonNode
    private void insert(InetAddress address, JsonNode node) {

        int[] words = inet2ints(address);
        
        NodeBuilder ipDetails = null;

        if (node != null) {

            String latitude = node.path("location").path("latitude").asText();
            String longitude = node.path("location").path("longitude").asText();
            String city = node.path("city").path("names").path("en").asText();
            String postalcode = node.path("postal").path("code").asText();
            String countrycode = node.path("country").path("iso_code").asText();
            String country = node.path("country").path("names").path("en").asText();
            String continentcode = node.path("continent").path("iso_code").asText();
            String continent = node.path("continent").path("names").path("en").asText();
            String timezone = node.path("location").path("time_zone").asText();

            NodeBuilder regions = new NodeBuilder(db);
            JsonNode subdivisions = node.path("subdivisions");

            for (int i = 0; i < subdivisions.size(); i++) {

                String name = subdivisions.get(i).path("names").path("en").asText();
                String code = subdivisions.get(i).path("iso_code").asText();

                regions.add(new NodeBuilder(db).storeSize(false).add(name).add(code));
            }

            // If you modify this tree, don't forget to update the getters in
            // FastGeoIP2 !
            ipDetails =  new NodeBuilder(db).storeSize(false)

                    .add(latitude)
                    .add(longitude)
                    .add(postalcode)
                    .add(city)
                    .add(
                    		 new NodeBuilder(db).storeSize(false)
                            .add(regions)
                            .add(country)
                            .add(countrycode)
                            .add(timezone)
                            .add(
                            		 new NodeBuilder(db).storeSize(false)
                                    .add(continent)
                                    .add(continentcode)
                             )
                     );

        }
        
        
        if(words.length == 4) {
        	
            if(lastIPv6==null) {
            	
                lastIPv6 = words;
                for(int i = 0 ; i < 4 ; i++) {
                    ipv6Tables[i] = new RangeTableBuilder(db);
                }
            }
            
            for(int i = 0 ; i < 4 ; i++) {
                if(words[i] != lastIPv6[i]) {
                	
                    for(int j = 3; j > i; j--) {
                    	
                        ipv6Tables[j-1].orderedAdd(lastIPv6[j-1],ipv6Tables[j]);
                        ipv6Tables[j] = new RangeTableBuilder(db);
                    }
                    
                    break;
                }
            }
            
            ipv6Tables[3].orderedAdd(words[3],ipDetails);
            lastIPv6 = words;
        }
        
    }

    private UniqueDBBuilder db;
    private File geoliteFile;

}
