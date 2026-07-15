package revolut;

import java.util.List;
import java.util.concurrent.*;

public class Counter {

    private final ExecutorService executorService = Executors.newFixedThreadPool(3);
    private final ConcurrentHashMap<String, Long> output = new ConcurrentHashMap<>();
    // read only
    BlockingQueue<List<String>> files;

    public Counter(BlockingQueue<List<String>> files) {
        this.files = files;
    }

    public void read() throws InterruptedException {
        while (!files.isEmpty()) {
            executorService.submit(() -> read(files.poll()));
        }

        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);
    }

    public void read(List<String> words) {
        // TODO learn: words.forEach(w -> output.merge(w, 1L, (oldValue, newValue) -> oldValue + newValue));
        words.forEach(word -> output.merge(word, 1L, Long::sum));
    }

    public Long count(String word) {
        return output.getOrDefault(word, 0L);
    }

}
