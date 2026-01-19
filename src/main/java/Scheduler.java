import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Scheduler {

    private final PriorityBlockingQueue<ScheduledTask> queue;
    private final ScheduledExecutorService scheduledExecutorService;

    public Scheduler() {
        queue = new PriorityBlockingQueue<>();
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        worker();
    }

    public void schedule(Runnable task, long delayMs) {
        if (!queue.offer(new ScheduledTask(task, delayMs))) {
            System.out.println("Queue is currently full");
        }
    }

    public long next() {
        return queue.poll().getDelayMs();
    }

    /**
     * Every second, run all scheduled tasks
     */
    private void worker() // Thread that executes tasks when their time comes
    {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            while (!queue.isEmpty() && queue.peek().scheduled()) {
                queue.poll().run();
            }
        }, 1, 1, TimeUnit.SECONDS);

    }

    public void shutdown() throws InterruptedException {
        scheduledExecutorService.shutdown();
        scheduledExecutorService.awaitTermination(1, TimeUnit.SECONDS);
    }

    public static class ScheduledTask implements Comparable<ScheduledTask> {
        private final Runnable task;
        private final long delayMs;
        private final long created;

        public ScheduledTask(Runnable task, long delayMs) {
            this.task = task;
            this.delayMs = delayMs;
            this.created = System.currentTimeMillis();
        }

        @Override
        public int compareTo(ScheduledTask o) {
            return this.executionTime().compareTo(o.executionTime());
        }

        public void run() {
            this.task.run();
        }

        private Long executionTime() {
            return created + delayMs;
        }

        private boolean scheduled() {
            return executionTime() <= System.currentTimeMillis();
        }

        public Long getDelayMs() {
            return delayMs;
        }
    }
}
