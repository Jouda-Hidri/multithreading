package cache;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Ttl { // TODO test when element is updated, would timestamp be updated also ?
    private ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();
    private ConcurrentLinkedDeque<OrderEntry> index = new ConcurrentLinkedDeque<>();
    private final int ttl = 1; // SECONDS

    public synchronized void add(String key, String value) {
        updateTime();
        cache.put(key, value);
        index.addLast(new OrderEntry(key));
    }

    public synchronized String get(String key) {
        updateTime();
        return cache.get(key);
    }

    private void updateTime() { // thread safe because called from sync method
        while (!index.isEmpty() && index.peekFirst().isOutdated()) {
            cache.remove(index.peekFirst().key);
            index.pollFirst();
        }
    }

    Map<String, String> getCache() {
        return cache;
    }

    public class OrderEntry {
        private final String key;
        private final LocalDateTime time; // could be also timestamp

        public OrderEntry(String key) {
            this.key = key;
            this.time = LocalDateTime.now();
        }

        public boolean isOutdated() {
            return time.plusSeconds(ttl).isBefore(LocalDateTime.now());
        }
    }
}
