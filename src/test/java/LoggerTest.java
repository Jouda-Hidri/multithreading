import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

class LoggerTest {

    @Test
    public void shouldLog() throws InterruptedException {
        Logger logger = new Logger();
        ExecutorService service = Executors.newFixedThreadPool(500);
        for(int i=0; i<10; i++) {
            service.submit(() -> logger.log("i"));
        }
        service.shutdown();
        service.awaitTermination(1, TimeUnit.SECONDS);
        assertThat(logger.getLogQueue()).isNotEmpty();
    }

    @Test
    public void shouldBatch() throws InterruptedException {
        Logger logger = new Logger();
        ExecutorService service = Executors.newFixedThreadPool(100);
        for(int i=0; i<150; i++) {
            int finalI = i;
            service.submit(() -> logger.log(""+ finalI));
        }
        service.shutdown();
        service.awaitTermination(1, TimeUnit.SECONDS);
        Thread.sleep(2000);
        assertThat(logger.getLogQueue()).isEmpty();
    }

}