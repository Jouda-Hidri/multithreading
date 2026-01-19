import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Logger {

    private BlockingQueue<String> logQueue = new LinkedBlockingQueue<>(5);

    public Logger() {
        runLogger();
    }

    public void log(String message) {
        logQueue.add(message);
    }

    private void runLogger() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
                    System.out.println("Batch");
                    while (!logQueue.isEmpty()) {
                        System.out.println(logQueue.poll());
                    }
                },
                1, 1, TimeUnit.SECONDS);
    }

    public BlockingQueue<String> getLogQueue() {
        return logQueue;
    }
}
