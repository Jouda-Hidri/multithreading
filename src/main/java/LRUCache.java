package main.java;

import java.util.*;


/**
 * Thread-safe LRU Cache
 * */
public class LRUCache<K, V> {
    private final int maxSize;
    private final Map<K, V> cache;
    private final Map<K, Long> timestamps = Collections.synchronizedMap(new HashMap<>());

    public LRUCache(int maxSize) {
        this.maxSize = maxSize;
        // synchronisedMap, LinkedHashMap
        this.cache = Collections.synchronizedMap(new LinkedHashMap<>(maxSize, 0.75f, true));
    }

    public synchronized void put(K key, V value) {
        if (cache.size() >= maxSize && !cache.containsKey(key)) {
            // min of a list
            K lruKey = Collections.min(timestamps.entrySet(), Comparator.comparingLong(Map.Entry::getValue)).getKey();
            cache.remove(lruKey);
            timestamps.remove(lruKey);
        }
        cache.put(key, value);
        timestamps.put(key, System.currentTimeMillis());
    }

    public synchronized V get(K key) {
        if (!cache.containsKey(key)) return null;
        timestamps.put(key, System.currentTimeMillis());
        return cache.get(key);
    }

}

