import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Detects operational anomalies and security threats in log entries based on configurable rules.
 */
public class AlertDetector {

    private int errorThreshold = 5;
    private int failedLoginThreshold = 2;
    private int repeatedErrorThreshold = 3;
    private int warningThreshold = 10;

    public AlertDetector() {}

    public AlertDetector(int errorThreshold, int failedLoginThreshold, int repeatedErrorThreshold, int warningThreshold) {
        this.errorThreshold = errorThreshold;
        this.failedLoginThreshold = failedLoginThreshold;
        this.repeatedErrorThreshold = repeatedErrorThreshold;
        this.warningThreshold = warningThreshold;
    }

    public List<Alert> detectAlerts(List<LogEntry> logEntries) {
        List<Alert> alerts = new ArrayList<>();

        if (logEntries == null || logEntries.isEmpty()) {
            return alerts;
        }

        checkHighErrorCount(logEntries, alerts);
        checkFailedLoginAttempts(logEntries, alerts);
        checkRepeatedErrorPattern(logEntries, alerts);
        checkExcessiveWarnings(logEntries, alerts);

        return alerts;
    }

    private void checkHighErrorCount(List<LogEntry> logEntries, List<Alert> alerts) {
        long errorCount = logEntries.stream()
                .filter(entry -> entry.getLevel() == LogLevel.ERROR)
                .count();

        if (errorCount > errorThreshold) {
            String msg = String.format("High number of ERROR logs detected (%d errors, threshold: %d).",
                    errorCount, errorThreshold);
            alerts.add(new Alert(msg, "ALERT"));
        }
    }

    private void checkFailedLoginAttempts(List<LogEntry> logEntries, List<Alert> alerts) {
        long failedLogins = logEntries.stream()
                .filter(entry -> entry.getMessage().toLowerCase().contains("failed login"))
                .count();

        if (failedLogins > failedLoginThreshold) {
            String msg = String.format("Multiple failed login attempts detected (%d attempts detected, threshold: %d).",
                    failedLogins, failedLoginThreshold);
            alerts.add(new Alert(msg, "SECURITY ALERT"));
        }
    }

    private void checkRepeatedErrorPattern(List<LogEntry> logEntries, List<Alert> alerts) {
        Map<String, Integer> errorFrequency = new HashMap<>();

        for (LogEntry entry : logEntries) {
            if (entry.getLevel() == LogLevel.ERROR) {
                String errorMsg = entry.getMessage();
                errorFrequency.put(errorMsg, errorFrequency.getOrDefault(errorMsg, 0) + 1);
            }
        }

        for (Map.Entry<String, Integer> entry : errorFrequency.entrySet()) {
            if (entry.getValue() >= repeatedErrorThreshold) {
                String msg = String.format("Repeated error pattern detected: \"%s\" occurred %d times (threshold: %d).",
                        entry.getKey(), entry.getValue(), repeatedErrorThreshold);
                alerts.add(new Alert(msg, "ALERT"));
            }
        }
    }

    private void checkExcessiveWarnings(List<LogEntry> logEntries, List<Alert> alerts) {
        long warningCount = logEntries.stream()
                .filter(entry -> entry.getLevel() == LogLevel.WARNING)
                .count();

        if (warningCount > warningThreshold) {
            String msg = String.format("Excessive warning activity detected (%d warnings, threshold: %d).",
                    warningCount, warningThreshold);
            alerts.add(new Alert(msg, "WARNING ALERT"));
        }
    }

    public int getErrorThreshold() { return errorThreshold; }
    public void setErrorThreshold(int errorThreshold) { this.errorThreshold = errorThreshold; }

    public int getFailedLoginThreshold() { return failedLoginThreshold; }
    public void setFailedLoginThreshold(int failedLoginThreshold) { this.failedLoginThreshold = failedLoginThreshold; }

    public int getRepeatedErrorThreshold() { return repeatedErrorThreshold; }
    public void setRepeatedErrorThreshold(int repeatedErrorThreshold) { this.repeatedErrorThreshold = repeatedErrorThreshold; }

    public int getWarningThreshold() { return warningThreshold; }
    public void setWarningThreshold(int warningThreshold) { this.warningThreshold = warningThreshold; }
}
