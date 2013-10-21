package com.dataiku.geoip.fastgeo;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import com.dataiku.geoip.mmdb.Reader;
import com.dataiku.geoip.uniquedb.UniqueDB;
import com.dataiku.geoip.uniquedb.UniqueDBBuilder;
import com.dataiku.geoip.uniquedb.WritableArray;
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

        dbBuilder = new UniqueDBBuilder();
        dataTable = dbBuilder.newArray();
        ipTable = dbBuilder.newArray();
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

            dbBuilder.addArray(dataTable);
            dbBuilder.addArray(ipTable);

            UniqueDB udb = dbBuilder.constructDatabase();
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

        WritableArray ipDetails = null;

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

            WritableArray regions = dbBuilder.newArray();

            JsonNode subdivisions = node.path("subdivisions");

            for (int i = 0; i < subdivisions.size(); i++) {

                String name = subdivisions.get(i).path("names").path("en")
                        .asText();
                String code = subdivisions.get(i).path("iso_code").asText();

                regions.addArray(dbBuilder.newArray().addString(name)
                        .addString(code));
            }

            // If you modify this tree, don't forget to update the getters in
            // FastGeoIP2 !
            ipDetails = dbBuilder
                    .newArray()
                    .addString(latitude)
                    .addString(longitude)
                    .addString(postalcode)
                    .addString(city)
                    .addArray(
                            dbBuilder
                                    .newArray()
                                    .addArray(regions)
                                    .addString(country)
                                    .addString(countrycode)
                                    .addString(timezone)
                                    .addArray(
                                            dbBuilder.newArray()
                                            .addString(continent)
                                                    .addString(continentcode)));
        }

        dataTable.addArray(ipDetails);
        ipTable.addInteger(ipAddress);

    }

    private UniqueDBBuilder dbBuilder;
    private WritableArray dataTable;
    private WritableArray ipTable;
    private File geoliteFile;

}
