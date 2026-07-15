package revolut.ratelimit;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RateLimiter implements AutoCloseable {

    private static final int CAPACITY = 10;

    private final BlockingQueue<Token> tokens =
            new LinkedBlockingQueue<>(CAPACITY);

    private final ScheduledExecutorService scheduler;

    static class Token {
    }

    public RateLimiter() {
        // TODO learn that offer unlike add, doesnt throw exception

        for (int i = 0; i < CAPACITY; i++) {
            tokens.offer(new Token());
        }

        scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(
                () -> tokens.offer(new Token()),
                1,
                1,
                TimeUnit.SECONDS);
    }

    AtomicInteger sharedResource = new AtomicInteger(0);

    public boolean handle(String request) {
        if (allow()) {
            sharedResource.incrementAndGet();
            return true;
        }
        return false;
    }

    private boolean allow() {
        return tokens.poll() != null;
    }

    @Override
    public void close() throws InterruptedException {
        scheduler.shutdown();
        scheduler.awaitTermination(5, TimeUnit.SECONDS);
    }
}