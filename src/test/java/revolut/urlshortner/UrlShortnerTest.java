package revolut.urlshortner;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.lang.Thread.sleep;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UrlShortnerTest {
    UrlShortner urlShortner = new UrlShortner();

    @Test
    public void shouldShorten() throws ExecutionException, InterruptedException {

        ExecutorService executor =
                Executors.newFixedThreadPool(100);

        int count = 1; //10000;

        List<Future> futures = new ArrayList<>();
        for (int i = 0; i < count; i++) { // first loop, old
            futures.add(executor.submit(() -> {
                        return urlShortner.shorten("https://example.com").shortUrl();
                    }
            ));
        }
        sleep(5000L); // 5 seconds
        for (int i = 0; i < count; i++) { // second loop, new
            futures.add(executor.submit(() -> {
                        return urlShortner.shorten("https://example.com/2").shortUrl();
                    }
            ));
        }
        assertThatThrownBy(
                () -> urlShortner.resolve("aHR0cHM6Ly9leGFtcGxlLmNvbQ=="))
                .isInstanceOf(RuntimeException.class);
        assertThat(urlShortner.resolve("aHR0cHM6Ly9leGFtcGxlLmNvbS8y")).isEqualTo("https://example.com/2");
        assertThat(futures.get(0).get()).isEqualTo("aHR0cHM6Ly9leGFtcGxlLmNvbQ==");
        assertThat(futures.get(count).get()).isEqualTo("aHR0cHM6Ly9leGFtcGxlLmNvbS8y");
    }

    @Test
    public void shouldShortenSimple() {
        UrlShortner urlShortner = new UrlShortner();

        String result = urlShortner.shorten("https://example.com").shortUrl();
        String result2 = urlShortner.shorten("https://example.com/2").shortUrl();

        assertThat(result).isEqualTo("aHR0cHM6Ly9leGFtcGxlLmNvbQ==");
        assertThat(result2).isEqualTo("aHR0cHM6Ly9leGFtcGxlLmNvbS8y");

    }

}