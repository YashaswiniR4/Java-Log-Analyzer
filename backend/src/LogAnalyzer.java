import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Performs analytical queries, aggregations, filtering, and keyword search
 * over a collection of parsed LogEntry objects.
 */
public class LogAnalyzer {

    private final List<LogEntry> logEntries;

    public LogAnalyzer(List<LogEntry> logEntries) {
        this.logEntries = logEntries != null ? new ArrayList<>(logEntries) : new ArrayList<>();
    }

    public void setLogEntries(List<LogEntry> newEntries) {
        this.logEntries.clear();
        if (newEntries != null) {
            this.logEntries.addAll(newEntries);
        }
    }

    public int getTotalLogsCount() {
        return logEntries.size();
    }

    public Map<LogLevel, Integer> getLogLevelCounts() {
        Map<LogLevel, Integer> counts = new EnumMap<>(LogLevel.class);
        for (LogLevel level : LogLevel.values()) {
            counts.put(level, 0);
        }

        for (LogEntry entry : logEntries) {
            counts.put(entry.getLevel(), counts.get(entry.getLevel()) + 1);
        }

        return counts;
    }

    public List<LogEntry> filterByLevel(LogLevel level) {
        if (level == null) {
            return new ArrayList<>();
        }
        return logEntries.stream()
                .filter(entry -> entry.getLevel() == level)
                .collect(Collectors.toList());
    }

    public List<LogEntry> searchByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }
        final String lowerKeyword = keyword.trim().toLowerCase();
        return logEntries.stream()
                .filter(entry -> entry.getMessage().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }

    public List<LogEntry> filterByDate(LocalDate targetDate) {
        if (targetDate == null) {
            return new ArrayList<>();
        }
        return logEntries.stream()
                .filter(entry -> entry.getTimestamp() != null && entry.getTimestamp().toLocalDate().equals(targetDate))
                .collect(Collectors.toList());
    }

    public List<LogEntry> getLogEntries() {
        return new ArrayList<>(logEntries);
    }
}
