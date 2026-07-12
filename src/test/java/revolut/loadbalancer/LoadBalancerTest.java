package revolut.loadbalancer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class LoadBalancerTest {
    LoadBalancer lb = new LoadBalancer();

    @BeforeEach
    public void setup() {
        lb.addServer();
        lb.addServer();
        lb.addServer();
    }

    @Test
    public void shouldRoundRobin() {
        assertThat(lb.getServer().getId()).isEqualTo("1");
        assertThat(lb.getServer().getId()).isEqualTo("2");
        assertThat(lb.getServer().getId()).isEqualTo("3");
        assertThat(lb.getServer().getId()).isEqualTo("1");
    }

    @Test
    public void shouldRoundRobinConc() {
        ExecutorService executor =
                Executors.newFixedThreadPool(5000);
        List<Future<Server>> futures = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            lb.addServer();
            futures.add(executor.submit(() -> lb.getServer()));
            lb.removeServer();
        }
        Map<String, Long> countsByServerID = futures.stream()
                .map(LoadBalancerTest::getServer)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Server::getId, Collectors.counting()));
        HashSet<Long> set = new HashSet<>(countsByServerID.values());
        assertThat(set.size())
                .isEqualTo(2); //each server is appearing either 3 or 2 times
    }

    private static Server getServer(Future<Server> f) {
        try {
            return f.get();
        } catch (InterruptedException | ExecutionException e) {
            return null;
        }
    }

}