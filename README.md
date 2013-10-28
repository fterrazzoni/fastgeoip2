FastGeoIP2
==========

GeoIP2 java API is much slower than the original GeoIP1 API. 

The MaxMind database format stores IP prefixes in a trie where each leaf points to a record, and where each record is a "binary JSON"-like datastucture.
The database is incredibly space efficient but unfortunately it is slow: I used a profiler and it turns out that it wastes most of the CPU time decoding strings ! 

The code we provide here converts the GeoLite2 database into another format. Differences are :
- We only kept the subset of fields we needed for our project
- IP ranges are stored in a contiguous array
- There is only one large string, and it is entirely decoded into RAM when the file is loaded
- The DB is less compact (40MB vs 26MB for GeoLite2)
- Shorter lookup times (20x faster)

This product includes GeoLite2 data created by MaxMind, available from
<a href="http://www.maxmind.com">http://www.maxmind.com</a>.