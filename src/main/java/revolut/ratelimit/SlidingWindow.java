package revolut.ratelimit;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

public class SlidingWindow {

    private final Deque<Long> requests = new ArrayDeque<>();
    private static final long WINDOW = TimeUnit.SECONDS.toNanos(5);

    public synchronized boolean allow() {
        long now = System.nanoTime();

        while (!requests.isEmpty() && now - requests.peekFirst() > WINDOW) {
            requests.removeFirst();
        }

        if (requests.size() >= 10) {
            return false;
        }

        requests.addLast(now);
        return true;
    }

}
