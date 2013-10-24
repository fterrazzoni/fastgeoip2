package com.dataiku.geoip.fastgeo;

import com.dataiku.geoip.uniquedb.Node;

// 
public class RangeTable {

	public Node lookup(int key) {

		int minIdx = 0;
		int maxIdx = keyTable.size() - 1;
		
		if (maxIdx < minIdx) {
			return null;
		}

		while (minIdx <= maxIdx) {

			int midIdx = (minIdx + maxIdx) >>> 1;
			int currentIP = keyTable.getInteger(midIdx);

			if (currentIP < key) {
				minIdx = midIdx + 1;
			} else if (currentIP > key) {
				maxIdx = midIdx - 1;
			} else {
				return nodeTable.getNode(midIdx);
			}
		}
		if(minIdx>1) {
			return nodeTable.getNode(minIdx - 1);
		} else {
			return null;
		}
	}

	private final Node keyTable;
	private final Node nodeTable;

	public RangeTable(Node n) {

		keyTable = n.getNode(0);
		nodeTable = n.getNode(1);

	}

}
