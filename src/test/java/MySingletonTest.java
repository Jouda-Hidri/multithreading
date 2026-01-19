import static org.assertj.core.api.Assertions.assertThat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

class MySingletonTest {

    @BeforeEach
    public void reset() {
        MySingleton.reset();
    }

    @Test
    public void shouldReturnSameInstanceSync() throws InterruptedException {
        List<MySingleton> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            list.add(MySingleton.create());
        }
        assertThat(new HashSet<>(list)).hasSize(1);
    }

    @RepeatedTest(5)
    public void shouldReturnSameInstanceAsync() throws InterruptedException {
        ExecutorService service = Executors.newFixedThreadPool(10);
        ArrayList<Future<MySingleton>> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(service.submit(MySingleton::create));
        }
        service.shutdown();
        service.awaitTermination(1, TimeUnit.SECONDS);
        Set<MySingleton> set = list.stream().map(f -> {
            try {
                return f.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toSet());
        assertThat(set).hasSize(1);

        ThreadLocal<SimpleDateFormat> format = ThreadLocal.withInitial(() -> new SimpleDateFormat("sth"));

    }

}