package com.dataiku.geoip.fastgeo;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import com.dataiku.geoip.mmdb.Reader;
import com.dataiku.geoip.uniquedb.UniqueDB;
import com.dataiku.geoip.uniquedb.UniqueDBBuilder;
import com.dataiku.geoip.uniquedb.WritableArray;
import com.fasterxml.jackson.databind.JsonNode;

// Construct a FastGeoIP2 database from the GeoLite2 MMDB file
public class FastGeoIP2Builder implements Closeable {

	ArrayList<Integer> ipAddresses = new ArrayList<Integer>();
	UniqueDBBuilder dbBuilder = new UniqueDBBuilder();
	WritableArray dataTable = dbBuilder.newArray();
	WritableArray ipTable = dbBuilder.newArray();

	private Reader geoliteDatabase;

	FastGeoIP2Builder(File mmdbFile) throws IOException {
		geoliteDatabase = new Reader(mmdbFile);
	}

	private void insert(InetAddress address, JsonNode node) {

		WritableArray ipDetails = null;
		int ipAddress = (int) FastGeoIP2.inetAddressToLong(address);

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
				regions.addString(subdivisions.get(i).path("names").path("en")
						.asText());
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
									.addArray(

											dbBuilder
													.newArray()
													.addString(country)
													.addString(countrycode)
													.addString(timezone)
													.addArray(
															dbBuilder
																	.newArray()

																	.addString(
																			continent)
																	.addString(
																			continentcode))));
		}

		dataTable.addArray(ipDetails);
		ipTable.addInteger(ipAddress);

	}

	static public interface Listener {
		public void progress(int processed, int total);
	}

	FastGeoIP2 build(Listener listener) throws IOException {

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

	}

	@Override
	public void close() throws IOException {
		if (geoliteDatabase != null) {
			geoliteDatabase.close();
			geoliteDatabase = null;
			dbBuilder = null;
			ipAddresses = null;
		}
	}

}
