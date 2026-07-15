package revolut;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.assertj.core.api.Assertions.assertThat;

class CounterTest {

    @Test
    public void shouldCount() throws Exception {
        BlockingQueue<List<String>> files = new LinkedBlockingQueue<>(5);

        /**
         * file1.txt
         * -----------
         * apple banana apple orange
         * <p>
         * file2.txt
         * -----------
         * banana apple grape
         * <p>
         * file3.txt
         * -----------
         * orange orange apple
         *
         */
        files.add(List.of("apple", "banana", "apple", "orange"));
        files.add(List.of("banana", "apple", "grape"));
        files.add(List.of("orange", "orange", "apple"));
        Counter counter = new Counter(files);
        counter.read();
        /**
         * apple: 4
         * banana: 2
         * orange: 3
         * grape: 1
         * */
        assertThat(counter.count("apple")).isEqualTo(4L);
        assertThat(counter.count("banana")).isEqualTo(2L);
        assertThat(counter.count("orange")).isEqualTo(3L);
        assertThat(counter.count("grape")).isEqualTo(1L);

    }

}