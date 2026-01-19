package cache;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class TtlMap {

    final Map<String, String> cache = new ConcurrentHashMap<>();
    Map<String, Long> history = Collections.synchronizedMap(new LinkedHashMap<>(1, 0.75f, true));
    final long delayMs = 1000;
    final AtomicInteger killed = new AtomicInteger(0);

    public void add(final String key, final String value) throws InterruptedException {
        CompletableFuture.runAsync(this::kill); // TODO compare performance with and without (211ms repeast 20)
//        kill();
        cache.put(key, value);
        history.put(key, System.currentTimeMillis()); // TODO regardless of boolean accessOrder ?
    }

    public String get(final String key) {
        if (shouldKill(key)) {
            return null;
        }
        synchronized (history) {
            history.put(key, System.currentTimeMillis()); // TODO regardless of boolean accessOrder ?
        }
        return cache.get(key);
    }

    private synchronized void kill() {
        while (!history.isEmpty()) {
            if (!shouldKill(history.keySet().iterator().next())) {
                System.out.println(killed.get());
                return;
            }
        }
    }

    private boolean shouldKill(final String key) {
        boolean isOld = history.get(key) + delayMs <= System.currentTimeMillis();
        if (isOld) {
            killed.incrementAndGet();
            cache.remove(key);
            history.remove(key);
        }
        return isOld;
    }
}
