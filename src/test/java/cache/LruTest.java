package cache;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

class LruTest {

    @Test
    public void shouldLru() throws InterruptedException {
        Lru lru = new Lru();
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        // TODO reach 10 but an element already exisiting
        for (int i = 0; i < 20; i++) {
            int finalI = i;
//            executorService.submit(() -> {
//                lru.get("0");
//            });
            executorService.submit(() -> {
                lru.add(finalI + "", finalI + "");
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);
        assertThat(lru.getCache()).isEqualTo(
                Map.of("0","0",
                        "1","1",
                        "2", "2",
                        "3",  "3",
                        "4", "4",
                        "5",  "5",
                        "6", "6",
                        "7",  "7",
                        "8", "8",
                        "9", "9"
                )
        );
    }

}