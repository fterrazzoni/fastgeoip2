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
import java.util.ArrayList;
import java.util.List;

import com.dataiku.geoip.uniquedb.ReadableArray;
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
	public FastGeoIP2(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		BufferedInputStream bis = new BufferedInputStream(fis);
		DataInputStream dis = new DataInputStream(bis);

		try {
			this.db = UniqueDB.loadFromStream(dis);
			this.dataTable = db.getArray(0);
			this.ipTable = db.getArray(1);
		} finally {
			dis.close();
		}
	}
	
	// Construct a FastGeoIP2 using an already loaded UniqueDB
	public FastGeoIP2(UniqueDB db) {
		this.db = db;
		this.dataTable = db.getArray(0);
		this.ipTable = db.getArray(1);
	}

	// Find an IPv4 address in the database
	// Return null if the IP has not been found, or a Result object
	public Result find(InetAddress addr) {

		byte[] bytes = addr.getAddress();
		
		if (bytes.length != 4)
			return null;
		
		long ip = ((bytes[0] & 0xFFL) << 24)
				| ((bytes[1] & 0xFFL) << 16) | ((bytes[2] & 0xFFL) << 8)
				| (bytes[3]) & 0xFFL;
		
		int index = findIndex(ip);

		ReadableArray data = dataTable.getArray(index);

		if (data != null)
			return new Result(data);

		return null;
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

		ReadableArray root;

		private Result(ReadableArray root) {
			this.root = root;
		}

		public String getCity() {
			return root.getString(3);
		}

		public String getPostalCode() {
			return root.getString(2);
		}

		public String getCountryCode() {
			return root.getArray(4).getString(2);
		}

		public String getTimezone() {
			return root.getArray(4).getString(3);
		}

		public String getCountry() {
			return root.getArray(4).getString(1);
		}

		public String getContinent() {
			return root.getArray(4).getArray(4).getString(0);
		}

		public String getContinentCode() {
			return root.getArray(4).getArray(4).getString(1);
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
            ReadableArray arr = root.getArray(4).getArray(0);
            ArrayList<Subdivision> list = new ArrayList<Subdivision>();
            for (int i = 0; i < arr.size(); i++) {
                Subdivision sub = new Subdivision();
                sub.name = arr.getArray(i).getString(0);
                sub.code = arr.getArray(i).getString(1);
                list.add(sub);
            }
            return list;
        }
        

	}
	


	private int findIndex(long queryIP) {

		int minIdx = 0;
		int maxIdx = ipTable.size() - 1;
		int midIdx = -1;

		while (maxIdx > minIdx) {

			midIdx = (minIdx + maxIdx) / 2;

			long midIP = ipTable.getInteger(midIdx) & 0xFFFFFFFFL;

			if (midIP > queryIP) {
				maxIdx = Math.max(0, midIdx - 1);
			} else if (midIP < queryIP) {
				minIdx = Math.min(midIdx + 1, ipTable.size() - 1);
			} else {
				return midIdx;
			}
		}

		long foundIP = ipTable.getInteger(minIdx) & 0xFFFFFFFFL;

		if (foundIP > queryIP) {
			minIdx--;
			foundIP = ipTable.getInteger(minIdx) & 0xFFFFFFFFL;
		}

		return minIdx;
	}

	

	private UniqueDB db;
	private ReadableArray dataTable;
	private ReadableArray ipTable;

}
