import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

class SchedulerTest {

    @Test
    public void shouldFillQueue() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Scheduler scheduler = new Scheduler();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() ->
        {
            scheduler.schedule(
                    () -> {
                        System.out.println("Single scheduled task");
                        latch.countDown();
                    },
                    2000);
        });
        latch.await(1, TimeUnit.SECONDS);
        assertThat(latch.getCount()).isEqualTo(1);
        scheduler.shutdown();
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.SECONDS);

    }

    @Test
    public void shouldScheduleTask() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Scheduler scheduler = new Scheduler();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() ->
        {
            scheduler.schedule(
                    () -> {
                        System.out.println("Single scheduled task");
                        latch.countDown();
                    },
                    1000);
        });
        latch.await(2, TimeUnit.SECONDS);
        assertThat(latch.getCount()).isEqualTo(0L);
        scheduler.shutdown();
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.SECONDS);
    }

    @Test
    public void shouldScheduleOneTask() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        Scheduler scheduler = new Scheduler();
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        executorService.submit(() ->
                scheduler.schedule(
                        () -> {
                            System.out.println("Single scheduled task");
                            latch.countDown();
                        },
                        1000)
        );
        executorService.submit(() ->
                scheduler.schedule(
                        () -> {
                            System.out.println("Single not yet scheduled task");
                            latch.countDown();
                        },
                        3000));
        latch.await(2, TimeUnit.SECONDS);
        assertThat(latch.getCount()).isEqualTo(1);
        scheduler.shutdown();
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.SECONDS);
    }

    @Test
    public void shouldOrderByDelay() throws InterruptedException {
        Scheduler scheduler = new Scheduler();
        scheduler.schedule(() -> System.out.println("x"), 3000);
        scheduler.schedule(() -> System.out.println("y"), 1000);
        assertThat(scheduler.next()).isEqualTo(1000);
        assertThat(scheduler.next()).isEqualTo(3000);
        scheduler.shutdown();
    }

}