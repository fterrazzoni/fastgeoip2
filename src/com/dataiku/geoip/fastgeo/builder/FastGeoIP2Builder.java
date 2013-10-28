package com.dataiku.geoip.fastgeo.builder;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import com.dataiku.geoip.fastgeo.FastGeoIP2;
import com.dataiku.geoip.mmdb.Reader;
import com.dataiku.geoip.uniquedb.InvalidDatabaseException;
import com.dataiku.geoip.uniquedb.UniqueDB;
import com.dataiku.geoip.uniquedb.builder.NodeBuilder;
import com.dataiku.geoip.uniquedb.builder.UniqueDBBuilder;
import com.fasterxml.jackson.databind.JsonNode;

// Convert a GeoLite2 MMDB file to a FastGeoIP2 database
public class FastGeoIP2Builder {

    // Progress listener (the conversion process takes some time!)
    static public interface Listener {
        public void progress(int processed, int total);
    }

    // Construct the builder with a GeoLite2 database
    FastGeoIP2Builder(File mmdbFile) {
        geoliteFile = mmdbFile;
    }

    // Start the conversion procedure
    FastGeoIP2 build(Listener listener) throws IOException {

        db = new UniqueDBBuilder();

        try (Reader geoliteDatabase = new Reader(geoliteFile)) {

            List<InetAddress> ranges = geoliteDatabase.getRanges(128);

            ipv4Table = new RangeTableBuilder(db, 1);
            ipv6Table = new RangeTableBuilder(db, 2);

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
            
            db.root().add(ipv4Table);
            db.root().add(ipv6Table);

            UniqueDB udb = db.constructDatabase();
            return new FastGeoIP2(udb);

        } catch (InvalidDatabaseException e) {
            throw new RuntimeException("This is a bug: the FastGeoIP2Builder generated an invalid database", e);
        }
    }
    
    // Insert an IP record using the data contained in a JsonNode
    private void insert(InetAddress address, JsonNode node) {

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

                regions.add(new NodeBuilder(db).withSize(false).add(name).add(code));
            }

            // If you modify this tree, don't forget to update the getters in
            // FastGeoIP2 !
            ipDetails = new NodeBuilder(db)
                    .withSize(false)

                    .add(latitude)
                    .add(longitude)
                    .add(postalcode)
                    .add(city)
                    .add(new NodeBuilder(db).withSize(false).add(regions).add(country).add(countrycode).add(timezone)
                            .add(new NodeBuilder(db).withSize(false).add(continent).add(continentcode)));

        }


        int ip[] = FastGeoIP2.inetToInteger(address);

        if (ip[0] == Integer.MIN_VALUE 
				&& ip[1] == Integer.MIN_VALUE 
				&& ip[2] == Integer.MIN_VALUE+0x0000FFFF) {
            ipv4Table.orderedAdd(new int[] { ip[3] }, ipDetails);
            
        } else if (ip[0] == Integer.MIN_VALUE 
				&& ip[1] == Integer.MIN_VALUE 
				&& ip[2] == Integer.MIN_VALUE) {
        	// explicitely ignored
        }
        else
        {
            ipv6Table.orderedAdd(new int[] { ip[0], ip[1] }, ipDetails);
            
        }

    }

    private RangeTableBuilder ipv4Table;
    private RangeTableBuilder ipv6Table;

    private UniqueDBBuilder db;
    private File geoliteFile;

}
