import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AlertDetectorTest {

    private AlertDetector detector;

    @Before
    public void setUp() {
        // Default thresholds: errorThreshold = 5, failedLoginThreshold = 2, repeatedErrorThreshold = 3, warningThreshold = 10
        detector = new AlertDetector();
    }

    @Test
    public void testNullOrEmptyInput() {
        assertTrue("Null input should return empty alert list", detector.detectAlerts(null).isEmpty());
        assertTrue("Empty input should return empty alert list", detector.detectAlerts(new ArrayList<>()).isEmpty());
    }

    @Test
    public void testRule1HighErrorCountTriggers() {
        List<LogEntry> entries = new ArrayList<>();
        // Add 6 ERROR entries (threshold is 5, so > 5 triggers alert)
        for (int i = 1; i <= 6; i++) {
            entries.add(new LogEntry(LocalDateTime.now(), LogLevel.ERROR, "Error type " + i, i));
        }

        List<Alert> alerts = detector.detectAlerts(entries);
        assertEquals(1, alerts.size());
        assertEquals("ALERT", alerts.get(0).getRuleType());
        assertTrue(alerts.get(0).getMessage().contains("High number of ERROR logs detected"));
        assertTrue(alerts.get(0).getMessage().contains("6 errors"));
    }

    @Test
    public void testRule1HighErrorCountNonTriggeringBoundary() {
        List<LogEntry> entries = new ArrayList<>();
        // Add exactly 5 ERROR entries (threshold is 5, so > 5 does NOT trigger)
        for (int i = 1; i <= 5; i++) {
            entries.add(new LogEntry(LocalDateTime.now(), LogLevel.ERROR, "Error type " + i, i));
        }

        List<Alert> alerts = detector.detectAlerts(entries);
        assertTrue("5 errors should not trigger high error count alert (threshold: 5)", alerts.isEmpty());
    }

    @Test
    public void testRule2FailedLoginSecurityAlertTriggers() {
        List<LogEntry> entries = new ArrayList<>();
        // Add 3 failed login attempts (threshold is 2, so > 2 triggers SECURITY ALERT)
        entries.add(new LogEntry(LocalDateTime.now(), LogLevel.WARNING, "Failed login attempt from IP 10.0.0.1", 1));
        entries.add(new LogEntry(LocalDateTime.now(), LogLevel.WARNING, "FAILED LOGIN attempt from IP 10.0.0.2", 2));
        entries.add(new LogEntry(LocalDateTime.now(), LogLevel.WARNING, "failed login attempt for admin", 3));

        List<Alert> alerts = detector.detectAlerts(entries);
        assertEquals(1, alerts.size());
        assertEquals("SECURITY ALERT", alerts.get(0).getRuleType());
        assertTrue(alerts.get(0).getMessage().contains("Multiple failed login attempts detected"));
    }

    @Test
    public void testRule2FailedLoginSecurityAlertNonTriggeringBoundary() {
        List<LogEntry> entries = new ArrayList<>();
        // Add exactly 2 failed login attempts (threshold is 2, so > 2 does NOT trigger)
        entries.add(new LogEntry(LocalDateTime.now(), LogLevel.WARNING, "Failed login attempt 1", 1));
        entries.add(new LogEntry(LocalDateTime.now(), LogLevel.WARNING, "Failed login attempt 2", 2));

        List<Alert> alerts = detector.detectAlerts(entries);
        assertTrue("2 failed logins should not trigger security alert (threshold: 2)", alerts.isEmpty());
    }

    @Test
    public void testRule3RepeatedErrorPatternTriggers() {
        List<LogEntry> entries = new ArrayList<>();
        // Add 3 identical error messages (threshold is >= 3, so 3 triggers)
        String sameError = "Database connection timeout on port 5432";
        entries.add(new LogEntry(LocalDateTime.now(), LogLevel.ERROR, sameError, 1));
        entries.add(new LogEntry(LocalDateTime.now(), LogLevel.ERROR, sameError, 2));
        entries.add(new LogEntry(LocalDateTime.now(), LogLevel.ERROR, sameError, 3));

        List<Alert> alerts = detector.detectAlerts(entries);
        assertFalse(alerts.isEmpty());
        boolean hasRepeatedAlert = alerts.stream().anyMatch(a -> a.getMessage().contains("Repeated error pattern detected"));
        assertTrue("Should detect repeated error pattern", hasRepeatedAlert);
    }

    @Test
    public void testRule3RepeatedErrorPatternNonTriggeringBoundary() {
        List<LogEntry> entries = new ArrayList<>();
        // Add 2 identical error messages (threshold is >= 3, so 2 does NOT trigger)
        String sameError = "Database connection timeout";
        entries.add(new LogEntry(LocalDateTime.now(), LogLevel.ERROR, sameError, 1));
        entries.add(new LogEntry(LocalDateTime.now(), LogLevel.ERROR, sameError, 2));

        List<Alert> alerts = detector.detectAlerts(entries);
        boolean hasRepeatedAlert = alerts.stream().anyMatch(a -> a.getMessage().contains("Repeated error pattern detected"));
        assertFalse("2 repeated errors should not trigger repeated error alert (threshold: 3)", hasRepeatedAlert);
    }

    @Test
    public void testRule4ExcessiveWarningsTriggers() {
        List<LogEntry> entries = new ArrayList<>();
        // Add 11 WARNING entries (threshold is 10, so > 10 triggers alert)
        for (int i = 1; i <= 11; i++) {
            entries.add(new LogEntry(LocalDateTime.now(), LogLevel.WARNING, "Disk space low warning " + i, i));
        }

        List<Alert> alerts = detector.detectAlerts(entries);
        assertEquals(1, alerts.size());
        assertEquals("WARNING ALERT", alerts.get(0).getRuleType());
        assertTrue(alerts.get(0).getMessage().contains("Excessive warning activity detected"));
    }

    @Test
    public void testRule4ExcessiveWarningsNonTriggeringBoundary() {
        List<LogEntry> entries = new ArrayList<>();
        // Add exactly 10 WARNING entries (threshold is 10, so > 10 does NOT trigger)
        for (int i = 1; i <= 10; i++) {
            entries.add(new LogEntry(LocalDateTime.now(), LogLevel.WARNING, "Disk space low warning " + i, i));
        }

        List<Alert> alerts = detector.detectAlerts(entries);
        assertTrue("10 warnings should not trigger excessive warnings alert (threshold: 10)", alerts.isEmpty());
    }

    @Test
    public void testCustomThresholdsConstructor() {
        // Custom thresholds: errorThreshold = 2, failedLoginThreshold = 1, repeatedErrorThreshold = 2, warningThreshold = 3
        AlertDetector customDetector = new AlertDetector(2, 1, 2, 3);

        assertEquals(2, customDetector.getErrorThreshold());
        assertEquals(1, customDetector.getFailedLoginThreshold());
        assertEquals(2, customDetector.getRepeatedErrorThreshold());
        assertEquals(3, customDetector.getWarningThreshold());
    }
}
