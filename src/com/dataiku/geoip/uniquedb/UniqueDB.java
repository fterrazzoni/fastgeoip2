package com.dataiku.geoip.uniquedb;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.zip.Adler32;


public final class UniqueDB {

    public Node root() {
        return root;
    }
	
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
	static public UniqueDB loadFromStream(DataInputStream dis) throws InvalidDatabaseException {

		try {

			Adler32 checksum = new Adler32();

			// reader & check headers
			if (dis.readInt() != UDB_MARKER) {
				throw new InvalidDatabaseException("Unknown file format");
			}

			if (dis.readInt() != VERSION_ID) {
				throw new InvalidDatabaseException("Incompatible UniqueDB version");
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
				throw new InvalidDatabaseException("Cannot load UniqueDB (invalid checksum)");
			}

			return new UniqueDB(data, meta);

		} catch (IOException e) {
			
			throw new InvalidDatabaseException("Cannot load UniqueDB (I/O error)", e);
		}

	}

	public UniqueDB(String data, int[] meta) {
	    this.root = new Node(this, meta[0]);
	    this.data = data;
	    this.meta = meta;
	}
	
	final private Node root;
	final protected String data;
    final protected int[] meta;
    
	private static final int VERSION_ID = 1;
	private static final int UDB_MARKER = 1433486402;

}
