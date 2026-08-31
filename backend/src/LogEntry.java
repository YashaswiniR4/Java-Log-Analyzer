import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Encapsulates an individual log record containing timestamp, log level, and message.
 */
public class LogEntry {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final LocalDateTime timestamp;
    private final LogLevel level;
    private final String message;
    private final int rawLineNumber;

    public LogEntry(LocalDateTime timestamp, LogLevel level, String message) {
        this(timestamp, level, message, -1);
    }

    public LogEntry(LocalDateTime timestamp, LogLevel level, String message, int rawLineNumber) {
        this.timestamp = timestamp;
        this.level = level;
        this.message = message != null ? message.trim() : "";
        this.rawLineNumber = rawLineNumber;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public LogLevel getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public int getRawLineNumber() {
        return rawLineNumber;
    }

    public String getFormattedTimestamp() {
        if (timestamp == null) {
            return "N/A";
        }
        return timestamp.format(FORMATTER);
    }

    @Override
    public String toString() {
        return String.format("%s %-7s %s", getFormattedTimestamp(), level, message);
    }
}
