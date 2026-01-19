package cache;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

class TtlMapTest {

    TtlMap cache;

    @BeforeEach
    public void reset() {
        cache = new TtlMap();
    }

    @RepeatedTest(20)
//    @Test
    public void shouldAddConcurently() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 100; i++) {
            int finalI = i;
            executorService.submit(() -> {
                try {
                    cache.add(String.valueOf(finalI), String.valueOf(finalI));
                    cache.add(String.valueOf(finalI + 500), String.valueOf(finalI * 2));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        Thread.sleep(1000); // After TTL

        for (int i = 100; i < 200; i++) {
            int finalI = i;
            executorService.submit(() -> {
                try {
                    cache.add(String.valueOf(finalI), String.valueOf(finalI));
                    cache.add(String.valueOf(finalI + 500), String.valueOf(finalI * 2));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);
        assertThat(cache.history).hasSize(200);
    }

}