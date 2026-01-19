import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class DateTimeConverter {

    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();
    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // --- Long <-> LocalDateTime ---

    public static LocalDateTime fromTimestampToLocalDateTime(long epochSeconds) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), DEFAULT_ZONE);
    }

    public static long fromLocalDateTimeToTimestamp(LocalDateTime dateTime) {
        return dateTime.atZone(DEFAULT_ZONE).toEpochSecond();
    }

    // --- String <-> LocalDateTime ---

    public static LocalDateTime fromStringToLocalDateTime(String dateTimeStr) {
        Objects.requireNonNull(dateTimeStr, "DateTime string cannot be null");
        return LocalDateTime.parse(dateTimeStr, DEFAULT_FORMATTER);
    }

    public static String fromLocalDateTimeToString(LocalDateTime dateTime) {
        return dateTime.format(DEFAULT_FORMATTER);
    }

    // --- String <-> ZonedDateTime ---

    public static ZonedDateTime fromStringToZonedDateTime(String zdtStr) {
        return ZonedDateTime.parse(zdtStr);
    }

    public static String fromZonedDateTimeToString(ZonedDateTime zdt) {
        return zdt.toString(); // ISO-8601
    }

    // --- Long <-> ZonedDateTime ---

    public static ZonedDateTime fromTimestampToZonedDateTime(long epochSeconds, ZoneId zone) {
        return Instant.ofEpochSecond(epochSeconds).atZone(zone);
    }

    public static long fromZonedDateTimeToTimestamp(ZonedDateTime zdt) {
        return zdt.toEpochSecond();
    }

    // --- LocalDateTime <-> ZonedDateTime ---

    public static ZonedDateTime fromLocalDateTimeToZoned(LocalDateTime dt, ZoneId zone) {
        return dt.atZone(zone);
    }

    public static LocalDateTime fromZonedToLocalDateTime(ZonedDateTime zdt) {
        return zdt.toLocalDateTime();
    }
}
