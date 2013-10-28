package com.dataiku.geoip.fastgeo;

import com.dataiku.geoip.uniquedb.Node;

public class RangeTable {

    // Load a Node as a RangeTable
    // For convenience, 'node' can be null (in this case, lookup() will always
    // return null)
    public RangeTable(Node node) {
        this.size = node.getInteger(0);
        this.keyTable = node.getNode(1);
        this.nodeTable = node.getNode(2);
    }

    private int compare(int a[], int b[]) {

        for (int i = 0; i < size; i++) {
            if (a[i] < b[i]) {
                return -1;
            } else if (a[i] > b[i]) {
                return 1;
            }
        }
        return 0;

    }

    // Find the 'key' in the table using a simple binary search
    public Node lookup(int keys[]) {

        if (nodeTable.size() == 0) {
            return null;
        }

        int minIdx = 0;
        int maxIdx = nodeTable.size() - 1;
        int currentKey[] = new int[size];

        while (minIdx <= maxIdx) {

            int midIdx = (minIdx + maxIdx) >>> 1;

            for (int i = 0; i < size; i++) {
                currentKey[i] = keyTable.getInteger(midIdx * size + i);
            }
            int cmp = compare(currentKey, keys);

            if (cmp < 0) {
                minIdx = midIdx + 1;
            } else if (cmp > 0) {
                maxIdx = midIdx - 1;
            } else {

                return nodeTable.getNode(midIdx);
            }
        }

        if (minIdx > 0) {
            return nodeTable.getNode(minIdx - 1);
        } else {
            return null;
        }
    }

    Node nodeTable;
    Node keyTable;
    int size;

}
