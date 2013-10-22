package com.dataiku.geoip.uniquedb;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.zip.Adler32;

// UniqueDB : In-memory hierarchical string datastore optimized for highly redundant data and fast access
// - Supported types are 'integer', 'string', 'array' and 'struct' (the root node is an array)
// - Arrays handle mixed element types
// - No runtime type checking / bound checking / any checking
// - Redundant subtrees are stored once (=> the UniqueDB is actually a DAG)
// - Very simple memory layout from a JVM POV (1 UniqueDB = 1 array + 1 string)
public final class UniqueDB extends Node {

	// Write the UniqueDB to a DataOutputStream
	public void writeToStream(DataOutputStream dos) throws IOException {

		Adler32 checksum = new Adler32();

		// headers
		dos.writeInt(UDB_MARKER);
		dos.writeInt(VERSION_ID);

		// write strings
		byte dataBytes[] = data.getBytes("UTF-8");
		checksum.update(dataBytes);
		dos.writeInt(dataBytes.length);
		dos.write(dataBytes);

		// write metadata
		dos.writeInt(meta.length);
		for (int i = 0; i < meta.length; i++) {
			int v = meta[i];
			checksum.update(v);
			dos.writeInt(v);
		}

		// write checksum
		dos.writeLong(checksum.getValue());

	}

	// Write the UniqueDB to a DataOutputStream
	static public UniqueDB loadFromStream(DataInputStream dis) throws InvalidUniqueDBException {

		try {

			Adler32 checksum = new Adler32();

			// reader & check headers
			if (dis.readInt() != UDB_MARKER) {
				throw new InvalidUniqueDBException("Unknown file format");
			}

			if (dis.readInt() != VERSION_ID) {
				throw new InvalidUniqueDBException("Incompatible UniqueDB version");
			}

			// read strings
			int dataLength = dis.readInt();
			byte[] dataBytes = new byte[dataLength];
			dis.readFully(dataBytes);
			checksum.update(dataBytes);
			String data = new String(dataBytes, "UTF-8");
			dataBytes = null;

			// read metadata
			int metaLength = dis.readInt();
			int meta[] = new int[metaLength];
			for (int i = 0; i < meta.length; i++) {
				int v = dis.readInt();
				meta[i] = v;
				checksum.update(v);
			}

			// verify checksum
			if (checksum.getValue() != dis.readLong()) {
				throw new InvalidUniqueDBException("Cannot load UniqueDB (invalid checksum)");
			}

			return new UniqueDB(data, meta);

		} catch (IOException e) {
			
			throw new InvalidUniqueDBException("Cannot load UniqueDB (I/O error)", e);
		}

	}

	UniqueDB(String data, int[] meta) {
		super(meta, data, meta[0]);
	}

	private static final int VERSION_ID = 1;
	private static final int UDB_MARKER = 1433486402;

}
