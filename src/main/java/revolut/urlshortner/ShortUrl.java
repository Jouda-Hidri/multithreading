package revolut.urlshortner;

public record ShortUrl(Long expiresAt, String shortUrl) {

    public boolean isExpired() {
        return expiresAt() < System.currentTimeMillis();
    }
}
