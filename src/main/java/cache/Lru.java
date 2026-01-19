package cache;


import static java.time.LocalDateTime.now;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Least Recently Used*/
public class Lru {
    // TDOO concurrentHAshMap ? is it enough ? as long as sorting is based on timestamp and not the order ot use the LinkedHashmap
    private Map<String, String> cache = Collections.synchronizedMap(new LinkedHashMap<>());
    private Map<String, LocalDateTime> index = Collections.synchronizedMap(new HashMap<>());
    private int max = 10;
    public synchronized void add(String key, String value) {
        if(index.size()>=max && !cache.containsKey(key)) { //
            // remove least recently used;
            String lruKey = Collections.min(index.entrySet(), Entry.comparingByValue()).getKey();
            cache.remove(lruKey);
            index.remove(lruKey);
        }
        cache.put(key, value);
        index.put(key, now());
    }
    public synchronized String get(String key) {
        String value = cache.get(key);
        if(value == null) {
            return null;
        }
        index.put(key, now());
        return  value;
    }

    public Map<String, String> getCache() {
        return cache;
    }
}
