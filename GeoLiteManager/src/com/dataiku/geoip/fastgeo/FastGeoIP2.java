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
	public FastGeoIP2(File file) throws IOException {
		
		FileInputStream fis = new FileInputStream(file);
		BufferedInputStream bis = new BufferedInputStream(fis);
		DataInputStream dis = new DataInputStream(bis);

		UniqueDB db = null;
		
		try {
			db = UniqueDB.loadFromStream(dis);
			
		} finally {
			dis.close();
		}
		
		initialize(db);
		
	}

	// Find an IPv4 address in the database
	// Return null if the IP has not been found, or a Result object
	public Result find(String addr) {

		if (addr == null || addr.isEmpty()) {
			return null;
		}

		long ip = 0;
		long blockVal = 0;
		int blockNum = 0;

		for (int i = 0; i < addr.length(); i++) {
			
			char c = addr.charAt(i);

			if (c >= '0' && c <= '9') {
				blockVal = blockVal * 10 + c - '0';
			}

			if (c == '.' || i == addr.length() - 1) {

				if (blockVal < 0 || blockVal > 255) {
					return null;
				}

				ip += blockVal << (24 - (8 * blockNum));
				blockVal = 0;
				blockNum++;
			}

			if (blockNum > 4 || (c != '.' && (c < '0' || c > '9'))) {
				return null;
			}
		}

		if (blockNum != 4) {
			return null;
		}

		int index = findIndex(ip);

		Node data = dataTable.getNode(index);

		if (data != null) {
			return new Result(data);
		}

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
			minIdx = Math.max(minIdx - 1, 0);
			foundIP = ipTable.getInteger(minIdx) & 0xFFFFFFFFL;
		}

		return minIdx;
	}


	// Construct a FastGeoIP2 using an already loaded UniqueDB
	FastGeoIP2(UniqueDB db) throws IOException {
		initialize(db);
	}
	
	private void initialize(UniqueDB db) throws IOException {
		this.db = db;
		this.dataTable = db.getNode(1);
		this.ipTable = db.getNode(2);
		checkVersion();
	}
	
	private void checkVersion() throws IOException {
		if(db.getInteger(0) != VERSION_ID) {
			throw new IOException("Cannot load FastGeoIP2 database (incompatible version or corrupted)");
		}
	}

	private UniqueDB db;
	private Node dataTable;
	private Node ipTable;
	
	static final int VERSION_ID = 9988432;

}
