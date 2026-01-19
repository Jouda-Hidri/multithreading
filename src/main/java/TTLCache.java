import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class TTLCache<K, V> {
    private static class Entry<V> {
        final V value;
        final long timestamp;

        Entry(V value, long timestamp) {
            this.value = value;
            this.timestamp = timestamp;
        }
    }

    private final long ttl;
    private final ConcurrentHashMap<K, Entry<V>> cache = new ConcurrentHashMap<>();
    private final Deque<K> order = new ConcurrentLinkedDeque<>(); // Double-ended (FIFO or LIFO)

    public TTLCache(long ttlMillis) {
        this.ttl = ttlMillis;
    }

    public synchronized void put(K key, V value) {
        long now = System.currentTimeMillis();
        cleanup(now);
        cache.put(key, new Entry<>(value, now));
        // add last for a queue
        order.addLast(key);
    }

    public synchronized V get(K key) {
        long now = System.currentTimeMillis();
        Entry<V> entry = cache.get(key);
        if (entry != null && (now - entry.timestamp) <= ttl) {
            return entry.value;
        }
        cache.remove(key);
        return null;
    }

    private void cleanup(long now) {
        while (!order.isEmpty()) {
            K key = order.peekFirst();
            Entry<V> entry = cache.get(key);
            if (entry != null && (now - entry.timestamp) > ttl) {
                cache.remove(key);
                // remove first for a stack
                order.pollFirst();
            } else {
                break;
            }
        }
    }
}

