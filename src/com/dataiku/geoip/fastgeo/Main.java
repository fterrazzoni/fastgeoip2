package com.dataiku.geoip.fastgeo;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.dataiku.geoip.fastgeo.FastGeoIP2.Result;
import com.dataiku.geoip.fastgeo.FastGeoIP2.Result.Subdivision;
import com.dataiku.geoip.fastgeo.FastGeoIP2Builder.Listener;
import com.dataiku.geoip.mmdb.Reader;
import com.fasterxml.jackson.databind.JsonNode;

public class Main {

	// 1st argument : path to the GeoLite2 DB
	// 2nd argument : path to the output FastGeoIP2 DB
	public static void main(String[] args) throws IOException, InvalidFastGeoIP2DatabaseException, InvalidIPAddress {

		convert(args[0], args[1]);
		bench(args[0], args[1]);

	}

	public static void convert(String mmdbInputFilename,
			String fgdbOutputFilename) throws IOException {

		// Convert the DB
		System.out.println("Convert GeoLite2 MMDB -> FastGeoIP2 FGDB...");
		System.out.println("MaxMind GeoLite database : " + mmdbInputFilename);
		System.out.println("Output FastGeoIP database : " + fgdbOutputFilename);
		File inputMMDB = new File(mmdbInputFilename);
		File outputFGDB = new File(fgdbOutputFilename);

		FastGeoIP2Builder dbBuilder = new FastGeoIP2Builder(inputMMDB);
		FastGeoIP2 inMemoryDB = dbBuilder.build(new Listener() {

			@Override
			public void progress(int processed, int total) {
				if (processed % 150000 == 0) {
					System.out.println("Building FastGeoIP2... " + 100
							* processed / total + "%   (" + processed
							+ " IP ranges processed)");
				}
			}
		});

		inMemoryDB.saveToFile(outputFGDB);
		System.out.println("FastGeoIP2 database built successfully !");
		System.out.println("Database size (on disk) : " + outputFGDB.length()
				/ 1024 + "KB");

	}

	// Run a perf benchmark FastGeoIP2 VS MaxMind GeoIP2 API
	// Check that they both produces the same output!
	static public void bench(String mmdbFilename, String fgdbFilename)
			throws  InvalidFastGeoIP2DatabaseException, IOException, InvalidIPAddress {

		// Benchmark the generated DB
		System.out.println("Run benchmark...");

		FastGeoIP2 fgdb = new FastGeoIP2(new File(fgdbFilename));
		Reader mmdb = new Reader(new File(mmdbFilename));

		// Take all the split addresses to make sure we test everything
		List<InetAddress> addressesInet = mmdb.getRanges();
		
		
		
		// Add some "pathological" addresses to make sure they work as expected
		addressesInet.add(InetAddress.getByName("0.0.0.0"));
		addressesInet.add(InetAddress.getByName("255.255.255.255"));

		// Generate random IP addresses
		int nbRandomIP = 100000;
		Random rd = new Random(123);
		for (int k = 0; k < nbRandomIP; k++) {
			String ip = rd.nextInt(256) + "." + rd.nextInt(256) + "."
					+ rd.nextInt(256) + "." + rd.nextInt(256);
			addressesInet.add(InetAddress.getByName(ip));
		}
		
		List<String> addressesString = new ArrayList<String>();
		
		for(InetAddress addr : addressesInet) {
			addressesString.add(addr.getHostAddress());
		}

		System.out.println("Benchmark size : " + addressesInet.size() + " IPs");

		int nbPasses = 5;
		for (int k = 0; k < nbPasses; k++) {

			int hashFGDB = 0;
			int hashMMDB = 0;

			long T1 = System.currentTimeMillis();

			for (String addr : addressesString) {
				
				Result res = fgdb.find(addr);
				
				if (res != null) {

					hashFGDB = 31 * hashFGDB + res.getLatitude().hashCode();
					hashFGDB = 31 * hashFGDB + res.getLongitude().hashCode();
					hashFGDB = 31 * hashFGDB + res.getTimezone().hashCode();
					hashFGDB = 31 * hashFGDB + res.getCity().hashCode();
					hashFGDB = 31 * hashFGDB + res.getCountry().hashCode();
					hashFGDB = 31 * hashFGDB + res.getCountryCode().hashCode();
					hashFGDB = 31 * hashFGDB + res.getContinent().hashCode();
					hashFGDB = 31 * hashFGDB
							+ res.getContinentCode().hashCode();
					hashFGDB = 31 * hashFGDB + res.getPostalCode().hashCode();

					for (Subdivision region : res.getSubdivisions()) {
					    hashFGDB = 31 * hashFGDB + region.name.hashCode();
					    hashFGDB = 31 * hashFGDB + region.code.hashCode();
					}
                    
				}

			}

			long T2 = System.currentTimeMillis();

			for (InetAddress addr : addressesInet) {

				JsonNode n = mmdb.get(addr);
				
				if (n != null) {
					
					hashMMDB = 31
							* hashMMDB
							+ n.path("location").path("latitude").asText()
									.hashCode();

					hashMMDB = 31
							* hashMMDB
							+ n.path("location").path("longitude").asText()
									.hashCode();

					hashMMDB = 31
							* hashMMDB
							+ n.path("location").path("time_zone").asText()
									.hashCode();

					hashMMDB = 31
							* hashMMDB
							+ n.path("city").path("names").path("en").asText()
									.hashCode();

					hashMMDB = 31
							* hashMMDB
							+ n.path("country").path("names").path("en")
									.asText().hashCode();
					hashMMDB = 31
							* hashMMDB
							+ n.path("country").path("iso_code").asText()
									.hashCode();

					hashMMDB = 31
							* hashMMDB
							+ n.path("continent").path("names").path("en")
									.asText().hashCode();

					hashMMDB = 31
							* hashMMDB
							+ n.path("continent").path("iso_code").asText()
									.hashCode();
					hashMMDB = 31 * hashMMDB
							+ n.path("postal").path("code").asText().hashCode();

					JsonNode subdivisions = n.path("subdivisions");
					for (int i = 0; i < subdivisions.size(); i++) {

						hashMMDB = 31
								* hashMMDB
								+ subdivisions.get(i).path("names").path("en")
										.asText().hashCode();
						
						hashMMDB = 31
                                * hashMMDB
                                + subdivisions.get(i).path("iso_code")
                                        .asText().hashCode();

					}

				}
			}

			long T3 = System.currentTimeMillis();
			System.out.println("Test " + (1 + k) + "/" + nbPasses);
			System.out.println("  FGDB : " + (T2 - T1) + "ms");
			System.out.println("  MMDB : " + (T3 - T2) + "ms");

			if (hashFGDB == hashMMDB) {
				System.out.println("  Coherence check : OK");
			} else {
				System.out.println("  Coherence check : FAILED");
			}

		}

		mmdb.close();

	}

}
