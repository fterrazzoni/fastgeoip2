package com.dataiku.geoip.fastgeo;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import com.dataiku.geoip.mmdb.Reader;
import com.dataiku.geoip.uniquedb.UniqueDB;
import com.dataiku.geoip.uniquedb.UniqueDBBuilder;
import com.dataiku.geoip.uniquedb.NodeBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

// Convert a GeoLite2 MMDB file to a FastGeoIP2 database
public class FastGeoIP2Builder {

    // Progress listener (the conversion process takes some time!)
    static public interface Listener {
        public void progress(int processed, int total);
    }
    private NodeBuilder schemaTable;

    // Construct the builder with a GeoLite2 database
    FastGeoIP2Builder(File mmdbFile) {
        geoliteFile = mmdbFile;
    }

    // Start the conversion procedure
    FastGeoIP2 build(Listener listener) throws IOException {

        db = new UniqueDBBuilder();
        dataTable = db.array();
        ipTable = db.array();
        schemaTable = db.array();
        
        Reader geoliteDatabase = new Reader(geoliteFile);
        
        try {
            List<InetAddress> ranges = geoliteDatabase.getRanges();

            for (int i = 0; i < ranges.size(); i++) {
                InetAddress addr = ranges.get(i);
                JsonNode node = geoliteDatabase.get(addr);
                insert(addr, node);

                if (listener != null) {
                    listener.progress(1 + i, ranges.size());
                }
            }

            db.add(dataTable);
            db.add(ipTable);
            

            UniqueDB udb = db.constructDatabase();
            return new FastGeoIP2(udb);

        } finally {

            geoliteDatabase.close();

        }
    }
 

    // Insert an IP record using the data contained in a JsonNode
    private void insert(InetAddress address, JsonNode node) {

        byte[] bytes = address.getAddress();

        if (bytes.length != 4) {
            throw new IllegalArgumentException("FastGeoIP2 supports IPv4 only");
        }

        int ipAddress = (int) (((bytes[0] & 0xFFL) << 24)
                | ((bytes[1] & 0xFFL) << 16) | ((bytes[2] & 0xFFL) << 8) | (bytes[3]) & 0xFFL);

        NodeBuilder ipDetails = null;

        if (node != null) {

            String latitude = node.path("location").path("latitude").asText();
            String longitude = node.path("location").path("longitude").asText();
            String city = node.path("city").path("names").path("en").asText();
            String postalcode = node.path("postal").path("code").asText();
            String countrycode = node.path("country").path("iso_code").asText();
            String country = node.path("country").path("names").path("en")
                    .asText();
            String continentcode = node.path("continent").path("iso_code")
                    .asText();
            String continent = node.path("continent").path("names").path("en")
                    .asText();
            String timezone = node.path("location").path("time_zone").asText();

            NodeBuilder regions = db.array();

            JsonNode subdivisions = node.path("subdivisions");

            for (int i = 0; i < subdivisions.size(); i++) {

                String name = subdivisions.get(i).path("names").path("en")
                        .asText();
                String code = subdivisions.get(i).path("iso_code").asText();

                regions.add(db.struct().add(name)
                        .add(code));
            }

            // If you modify this tree, don't forget to update the getters in
            // FastGeoIP2 !
            
            
            ipDetails = db
                    .struct()
                    .add(latitude)
                    .add(longitude)
                    .add(postalcode)
                    .add(city)
                    .add(
                            db
                                    .struct()
                                    .add(regions)
                                    .add(country)
                                    .add(countrycode)
                                    .add(timezone)
                                    .add(
                                            db.struct()
                                            .add(continent)
                                                    .add(continentcode)));
            
        }
        
        dataTable.add(ipDetails);
        ipTable.add(ipAddress);
        

    }

    private UniqueDBBuilder db;
    private NodeBuilder dataTable;
    private NodeBuilder ipTable;
    private File geoliteFile;

}
