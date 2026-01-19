package cache;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

class TtlTest {

    @Test
    public void shouldKeepNewElements() throws InterruptedException {
        Ttl ttl = new Ttl();
        ExecutorService executorService = Executors.newFixedThreadPool(50);
        for (int i = 0; i < 20; i++) {
            int finalI = i;
            executorService.submit(() -> {
                ttl.add(finalI + "", finalI + "");
            });
            Thread.sleep(125); // ttl =1000; wait 1000/8 => 8 elements in cache
        }
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);
        assertThat(ttl.getCache().size()).isEqualTo(8);
    }

    @Test
    public void shouldKeepAll() throws InterruptedException {
        Ttl ttl = new Ttl();
        ExecutorService executorService = Executors.newFixedThreadPool(50);
        for (int i = 0; i < 20; i++) {
            int finalI = i;
            executorService.submit(() -> {
                ttl.add(finalI + "", finalI + "");
            });
        }
        Thread.sleep(2000);
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);
        assertThat(ttl.getCache().size()).isEqualTo(20);
    }

    @Test
    public void shouldKeepNothing() throws InterruptedException {
        Ttl ttl = new Ttl();
        ExecutorService executorService = Executors.newFixedThreadPool(50);
        for (int i = 0; i < 20; i++) {
            int finalI = i;
            executorService.submit(() -> {
                ttl.add(finalI + "", finalI + "");
            });
        }
        Thread.sleep(2000);
        ttl.get(0+"");
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);
        assertThat(ttl.getCache().size()).isEqualTo(0);
    }

}