package revolut;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DriverThreadTest {

    @Test
    void shouldExchangeGossipAmongFourDriversAtSameStop() {
        List<DriverThread> drivers = List.of(
                new DriverThread(1L, List.of(3, 1, 2, 3)),
                new DriverThread(2L, List.of(3, 2, 3, 1)),
                new DriverThread(3L, List.of(4, 2, 3, 4, 5))
        );

        CyclicBarrier start = new CyclicBarrier(drivers.size());
        CyclicBarrier finish = new CyclicBarrier(drivers.size());

        drivers.forEach(d -> d.setDrivers(drivers, start, finish));

        ExecutorService executor = Executors.newFixedThreadPool(drivers.size());

        List<Future<Integer>> futures = drivers.stream()
                .map(d -> executor.submit(d::moveAndGossip))
                .toList();

        for (Future<Integer> future : futures) {
            assertDoesNotThrow(() -> future.get(5, TimeUnit.SECONDS));
        }

        assertTrue(drivers.stream()
                .allMatch(d -> d.gossip.size() == drivers.size()));

        executor.shutdown();
    }
}