package revolut.urlshortner;

import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UrlShortner {
    final ConcurrentHashMap<String, ShortUrl> urls = new ConcurrentHashMap<>();

    ShortUrl shorten(String longUrl) {
// todo learn: computeIfAbsent (key, key -> {return value}), compute (key, (key, oldValue) -> {return newValue})
        return urls.compute(
                longUrl,
                (key, value) -> {
                    if (value == null || value.isExpired()) {
                        return new ShortUrl(
                                System.currentTimeMillis() + 3000L,
                                Base64.getEncoder().encodeToString(key.getBytes())
                        );
                    }
                    return value;
                }
        );
    }

    String resolve(String shortUrl) {
        return urls.entrySet().stream()
                .filter(e -> e.getValue().shortUrl().equals(shortUrl)
                        && !e.getValue().isExpired()) // not yet expired
                .map(Map.Entry::getKey).findFirst()
                .orElseThrow(RuntimeException::new);

    }

}
