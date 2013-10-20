package com.dataiku.geoip.uniquedb;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


// UniqueDB : In-memory hierarchical string datastore optimized for highly redundant data
// - Supported types are 'integer', 'string' and 'array' (the root node is an array)
// - Arrays handle mixed element types
// - No runtime type checking / bound checking
// - Redundant subtrees are stored once
// - Very simple memory layout (1 UniqueDB = 1 array + 1 string)
public final class UniqueDB extends ReadableArray {

	public static final int VERSION_ID = 7429138;
	
	public UniqueDB(String data, int[] meta) {
		super(meta, data, meta[0]);
	}

	public int getApproxSizeInBytes() {
		return 4*3+meta.length*4 + data.length()*2;
	}

	public void writeToStream(DataOutputStream dos) throws IOException {
		
		byte dataBytes[] = data.getBytes("UTF-8");
		dos.writeInt(VERSION_ID);
		dos.writeInt(dataBytes.length);
		dos.write(dataBytes);
		dos.writeInt(VERSION_ID);
		dos.writeInt(meta.length);
		for (int i = 0; i < meta.length; i++)
			dos.writeInt(meta[i]);
		dos.writeInt(VERSION_ID);
		
	}

	static private void checkVersion(DataInputStream dis) throws IOException {
		if(dis.readInt() != VERSION_ID)
			throw new IOException("Cannot load database (invalid version or corrupted file)");
	}
	
	static public UniqueDB loadFromStream(DataInputStream dis)
			throws IOException {
		
		checkVersion(dis);
		int dataLength = dis.readInt();
		byte[] dataBytes = new byte[dataLength];
		dis.readFully(dataBytes);
		checkVersion(dis);
		String data = new String(dataBytes,"UTF-8");
		dataBytes = null;
		int metaLength = dis.readInt();
		int meta[] = new int[metaLength];
		for (int i = 0; i < meta.length; i++)
			meta[i] = dis.readInt();
		checkVersion(dis);
		return new UniqueDB(data, meta);
	}
}
