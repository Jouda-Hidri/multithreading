package revolut;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class DriverThreadTest {

    @Test
    public void shouldWaitAndNotify() throws ExecutionException, InterruptedException, TimeoutException {
        Set<DriverThread> driversHavingAllGossip =
                ConcurrentHashMap.newKeySet();
        List<DriverThread> drivers = List.of(
                new DriverThread(1L, List.of(3, 1, 2, 3), driversHavingAllGossip),
                new DriverThread(2L, List.of(3, 2, 3, 1), driversHavingAllGossip),
                new DriverThread(3L, List.of(4, 2, 3, 4, 5), driversHavingAllGossip)
        );
        for (DriverThread driver : drivers) {
            driver.setDrivers(drivers);
        }
        ExecutorService service = Executors.newFixedThreadPool(3);

        Future<Integer> f1 =
                service.submit(() -> drivers.get(0).moveAndGossip(drivers));

        Future<Integer> f2 =
                service.submit(() -> drivers.get(1).moveAndGossip(drivers));

        Future<Integer> f3 =
                service.submit(() -> drivers.get(2).moveAndGossip(drivers));


        assertThat(f1.get(5, TimeUnit.SECONDS)).isEqualTo(4);
        assertThat(f2.get(5, TimeUnit.SECONDS)).isEqualTo(4);
        assertThat(f3.get(5, TimeUnit.SECONDS)).isEqualTo(4);

        service.shutdown();
    }
}