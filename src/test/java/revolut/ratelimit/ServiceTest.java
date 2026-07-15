package revolut.ratelimit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RateLimiterConcurrencyTest {

    private RateLimiter limiter;

    @AfterEach
    void cleanup() throws InterruptedException {
        if (limiter != null) {
            limiter.close();
        }
    }


    @Test
    void shouldAllowInitialBucketCapacity() throws Exception {

        limiter = new RateLimiter();

        int succeeded = 0;

        for (int i = 0; i < 20; i++) {
            if (limiter.handle("user")) {
                succeeded++;
            }
        }

        assertEquals(10, succeeded);
        assertEquals(10, limiter.sharedResource.get());
    }


    @Test
    void shouldRejectWhenBucketIsEmpty() {

        limiter = new RateLimiter();

        for (int i = 0; i < 10; i++) {
            assertTrue(limiter.handle("user"));
        }

        assertFalse(limiter.handle("user"));
    }


    @Test
    void shouldRefillOneTokenPerSecond() throws Exception {

        limiter = new RateLimiter();

        for (int i = 0; i < 10; i++) {
            assertTrue(limiter.handle("user"));
        }

        assertFalse(limiter.handle("user"));

        Thread.sleep(1100);

        assertTrue(limiter.handle("user"));

        assertFalse(limiter.handle("user"));
    }


    @Test
    void shouldNotExceedCapacityAfterLongIdlePeriod() throws Exception {

        limiter = new RateLimiter();

        Thread.sleep(15000);

        int succeeded = 0;

        for (int i = 0; i < 100; i++) {
            if (limiter.handle("user")) {
                succeeded++;
            }
        }

        // Bucket capacity is still only 10
        assertEquals(10, succeeded);
    }


    @Test
    void shouldBeThreadSafeUnderConcurrentAccess() throws Exception {

        limiter = new RateLimiter();

        int threads = 100;

        ExecutorService executor =
                Executors.newFixedThreadPool(threads);

        CyclicBarrier barrier =
                new CyclicBarrier(threads);

        AtomicInteger succeeded =
                new AtomicInteger();

        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threads; i++) {

            futures.add(executor.submit(() -> {

                barrier.await();

                if (limiter.handle("same-user")) {
                    succeeded.incrementAndGet();
                }

                return null;
            }));
        }

        for (Future<?> future : futures) {
            future.get();
        }

        executor.shutdown();

        assertEquals(10, succeeded.get());
        assertEquals(10, limiter.sharedResource.get());
    }


    @Test
    void shouldNotLoseRequestsUnderHeavyConcurrency() throws Exception {

        limiter = new RateLimiter();

        int threads = 1000;

        ExecutorService executor =
                Executors.newFixedThreadPool(100);

        CountDownLatch latch =
                new CountDownLatch(threads);

        AtomicInteger completed =
                new AtomicInteger();

        for (int i = 0; i < threads; i++) {

            executor.submit(() -> {
                try {
                    limiter.handle("stress");
                } finally {
                    completed.incrementAndGet();
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS));

        executor.shutdown();

        assertEquals(threads, completed.get());
    }


    @Test
    void shouldRemainConsistentDuringRepeatedBursts() throws Exception {

        limiter = new RateLimiter();

        ExecutorService executor =
                Executors.newFixedThreadPool(50);

        for (int round = 0; round < 10; round++) {

            CountDownLatch latch =
                    new CountDownLatch(50);

            for (int i = 0; i < 50; i++) {

                executor.submit(() -> {
                    try {
                        limiter.handle("burst");
                    } finally {
                        latch.countDown();
                    }
                });
            }

            assertTrue(latch.await(5, TimeUnit.SECONDS));

            Thread.sleep(1100);
        }

        executor.shutdown();

        assertTrue(executor.awaitTermination(
                5,
                TimeUnit.SECONDS));
    }
}