// This product includes GeoLite2 data created by MaxMind, available from
// <a href="http://www.maxmind.com">http://www.maxmind.com</a>.
//
// Cloned from https://github.com/maxmind/MaxMind-DB-Reader-java
// Added : dump to text file and IPs extraction
package com.dataiku.geoip.mmdb;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Instances of this class provide a reader for the MaxMind DB format. IP
 * addresses can be looked up using the <code>get</code> method.
 */
public final class Reader implements Closeable {
    private static final int DATA_SECTION_SEPARATOR_SIZE = 16;
    private static final byte[] METADATA_START_MARKER = { (byte) 0xAB,
            (byte) 0xCD, (byte) 0xEF, 'M', 'a', 'x', 'M', 'i', 'n', 'd', '.',
            'c', 'o', 'm' };

    private int ipV4Start;
    private final Decoder decoder;
    private final Metadata metadata;
    private final ThreadBuffer threadBuffer;

    /**
     * The file mode to use when opening a MaxMind DB.
     */
    public enum FileMode {
        /**
         * The default file mode. This maps the database to virtual memory. This
         * often provides similar performance to loading the database into real
         * memory without the overhead.
         */
        MEMORY_MAPPED,
        /**
         * Loads the database into memory when the reader is constructed.
         */
        MEMORY
    }

    /**
     * Constructs a Reader for the MaxMind DB format. The file passed to it must
     * be a valid MaxMind DB file such as a GeoIP2 database file.
     * 
     * @param database
     *            the MaxMind DB file to use.
     * @throws IOException
     *             if there is an error opening or reading from the file.
     */
    public Reader(File database) throws IOException {
        this(database, FileMode.MEMORY_MAPPED);
    }

    /**
     * Constructs a Reader for the MaxMind DB format. The file passed to it must
     * be a valid MaxMind DB file such as a GeoIP2 database file.
     * 
     * @param database
     *            the MaxMind DB file to use.
     * @param fileMode
     *            the mode to open the file with.
     * @throws IOException
     *             if there is an error opening or reading from the file.
     */
    public Reader(File database, FileMode fileMode) throws IOException {
        this.threadBuffer = new ThreadBuffer(database, fileMode);
        int start = this.findMetadataStart(database.getName());

        Decoder metadataDecoder = new Decoder(this.threadBuffer, start);
        this.metadata = new Metadata(metadataDecoder.decode(start).getNode());
        this.decoder = new Decoder(this.threadBuffer,
                this.metadata.searchTreeSize + DATA_SECTION_SEPARATOR_SIZE);
    }

    /**
     * Looks up the <code>address</code> in the MaxMind DB.
     * 
     * @param ipAddress
     *            the IP address to look up.
     * @return the record for the IP address.
     * @throws IOException
     *             if a file I/O error occurs.
     */
    public JsonNode get(InetAddress ipAddress) throws IOException {
        int pointer = this.findAddressInTree(ipAddress);
        if (pointer == 0) {
            return null;
        }
        return this.resolveDataPointer(pointer);
    }
    
    int previousRecord;
    

    // Get the splits between IPv4 ranges 
    // Warning : this method doesn't exist in the official MaxMind DB reader
    public List<InetAddress> getRanges(int bitLength) {

        try {

            List<InetAddress> ips = new ArrayList<InetAddress>();

            int record = startNode(bitLength);
            System.out.println("Start="+record);
            previousRecord = -1;
            visitorHelper(record, 0, 0, new byte[bitLength/8], ips);
            visitorHelper(record, 1, 0, new byte[bitLength/8], ips);
            
            
            return ips;

        } catch (Exception e) {

            return null;
        }

    }
    
    // Recursive helper for getRanges()
    // Warning : this method doesn't exist in the official MaxMind DB reader
    private void visitorHelper(int record, int bit, int depth, byte ip[],
            List<InetAddress> ips) throws InvalidDatabaseException,
            UnknownHostException {

        
        record = this.readNode(record, bit);
        if (record < this.metadata.nodeCount) {
            
            ip[depth/8] |= (bit<<(7-depth%8));
            visitorHelper(record, 0, depth + 1, ip, ips);
            visitorHelper(record, 1, depth + 1, ip, ips);
            ip[depth/8] &= ~(bit<<(7-depth%8));
            
        } else {
            
            if (record != previousRecord) {
                ips.add(Inet6Address.getByAddress(ip));
            }
            previousRecord = record;
        }

    }
    
    
    
    

    // Dump the database to a text file
    // Warning : this method doesn't exist in the official MaxMind DB reader
    public void dump(File output) throws FileNotFoundException {

        System.out.println("Dumping GeoIP database...");

        FileOutputStream ofs = new FileOutputStream(output);
        PrintStream stro = new PrintStream(ofs);
        List<InetAddress> addresses = getRanges(32);
        int ok = 0;
        int bad = 0;
        for (InetAddress addr : addresses) {
            try {
                JsonNode node = get(addr);
                if (node != null) {
                    stro.print(addr.toString() + "\t" + node.toString() + "\n");
                    ok++;
                } else {
                    stro.print(addr.toString() + "\tNULL\n");
                    bad++;
                }
            } catch (Exception e) {
                System.out.println("This should NEVER happen !");
            }
            

            if ((ok+bad) % 10000 == 0) {
                System.out.println("Processed " + (ok+bad) + "/" + addresses.size()+ " ("+bad+" null records)");
            }
        }
        stro.close();
        System.out.println("Dump success!");
    }

    private int findAddressInTree(InetAddress address)
            throws InvalidDatabaseException {
        byte[] rawAddress = address.getAddress();

        int bitLength = rawAddress.length * 8;
        int record = this.startNode(bitLength);
        
        for (int i = 0; i < bitLength; i++) {
            if (record >= this.metadata.nodeCount) {
                break;
            }
            int b = 0xFF & rawAddress[i / 8];
            int bit = 1 & (b >> 7 - (i % 8));
            record = this.readNode(record, bit);
        }
        if (record == this.metadata.nodeCount) {
            // record is empty
            return 0;
        } else if (record > this.metadata.nodeCount) {
            // record is a data pointer
            return record;
        }
        throw new InvalidDatabaseException("Something bad happened");
    }

    private int startNode(int bitLength) throws InvalidDatabaseException {
        // Check if we are looking up an IPv4 address in an IPv6 tree. If this
        // is the case, we can skip over the first 96 nodes.
        if (this.metadata.ipVersion == 6 && bitLength == 32) {
            return this.ipV4StartNode();
        }
        // The first node of the tree is always node 0, at the beginning of the
        // value
        return 0;
    }

    private int ipV4StartNode() throws InvalidDatabaseException {
        // This is a defensive check. There is no reason to call this when you
        // have an IPv4 tree.
        if (this.metadata.ipVersion == 4) {
            return 0;
        }

        if (this.ipV4Start != 0) {
            return this.ipV4Start;
        }
        int node = 0;
        for (int i = 0; i < 96 && node < this.metadata.nodeCount; i++) {
            node = this.readNode(node, 0);
        }
        this.ipV4Start = node;
        return node;
    }

    private int readNode(int nodeNumber, int index)
            throws InvalidDatabaseException {
        ByteBuffer buffer = this.threadBuffer.get();
        int baseOffset = nodeNumber * this.metadata.nodeByteSize;

        switch (this.metadata.recordSize) {
        case 24:
            buffer.position(baseOffset + index * 3);
            return Decoder.decodeInteger(buffer, 0, 3);
        case 28:
            int middle = buffer.get(baseOffset + 3);

            if (index == 0) {
                middle = (0xF0 & middle) >>> 4;
            } else {
                middle = 0x0F & middle;
            }
            buffer.position(baseOffset + index * 4);
            return Decoder.decodeInteger(buffer, middle, 3);
        case 32:
            buffer.position(baseOffset + index * 4);
            return Decoder.decodeInteger(buffer, 0, 4);
        default:
            throw new InvalidDatabaseException("Unknown record size: "
                    + this.metadata.recordSize);
        }
    }

    private JsonNode resolveDataPointer(int pointer) throws IOException {
        int resolved = (pointer - this.metadata.nodeCount)
                + this.metadata.searchTreeSize;

        if (resolved >= this.threadBuffer.get().capacity()) {
            throw new InvalidDatabaseException(
                    "The MaxMind DB file's search tree is corrupt: "
                            + "contains pointer larger than the database.");
        }

        // We only want the data from the decoder, not the offset where it was
        // found.
        return this.decoder.decode(resolved).getNode();
    }

    /*
     * Apparently searching a file for a sequence is not a solved problem in
     * Java. This searches from the end of the file for metadata start.
     * 
     * This is an extremely naive but reasonably readable implementation. There
     * are much faster algorithms (e.g., Boyer-Moore) for this if speed is ever
     * an issue, but I suspect it won't be.
     */
    private int findMetadataStart(String databaseName)
            throws InvalidDatabaseException {
        ByteBuffer buffer = this.threadBuffer.get();
        int fileSize = buffer.capacity();

        FILE: for (int i = 0; i < fileSize - METADATA_START_MARKER.length + 1; i++) {
            for (int j = 0; j < METADATA_START_MARKER.length; j++) {
                byte b = buffer.get(fileSize - i - j - 1);
                if (b != METADATA_START_MARKER[METADATA_START_MARKER.length - j
                        - 1]) {
                    continue FILE;
                }
            }
            return fileSize - i;
        }
        throw new InvalidDatabaseException(
                "Could not find a MaxMind DB metadata marker in this file ("
                        + databaseName + "). Is this a valid MaxMind DB file?");
    }

    Metadata getMetadata() {
        return this.metadata;
    }

    /**
     * Closes the MaxMind DB and returns resources to the system.
     * 
     * @throws IOException
     *             if an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        this.threadBuffer.close();
    }
}
