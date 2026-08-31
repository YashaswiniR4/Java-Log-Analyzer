import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a security or operational alert triggered by pattern detection rules.
 */
public class Alert {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String message;
    private final String ruleType;
    private final LocalDateTime timestamp;

    public Alert(String message, String ruleType) {
        this.message = message;
        this.ruleType = ruleType;
        this.timestamp = LocalDateTime.now();
    }

    public String getMessage() {
        return message;
    }

    public String getRuleType() {
        return ruleType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getFormattedTimestamp() {
        return timestamp.format(FORMATTER);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s", ruleType, message);
    }
}
