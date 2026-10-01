import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EdgeCaseTest {

    private LogParser parser;
    private AlertDetector detector;

    @Before
    public void setUp() {
        parser = new LogParser();
        detector = new AlertDetector();
    }

    @Test
    public void testEmptyLogFile() throws IOException {
        List<LogEntry> entries = parser.parseLogFile("test/test_data/empty.log");
        assertNotNull("Entries list should not be null", entries);
        assertEquals("Empty log file should produce 0 entries", 0, entries.size());
        assertEquals("Empty log file should have 0 skipped lines", 0, parser.getSkippedLineCount());

        LogAnalyzer analyzer = new LogAnalyzer(entries);
        assertEquals(0, analyzer.getTotalLogsCount());

        List<Alert> alerts = detector.detectAlerts(entries);
        assertTrue("Empty log file should trigger 0 alerts", alerts.isEmpty());
    }

    @Test
    public void testEmptyLinesLogFile() throws IOException {
        List<LogEntry> entries = parser.parseLogFile("test/test_data/empty_lines.log");
        assertEquals("Blank lines file should produce 0 entries", 0, entries.size());
        assertEquals("Blank lines should not count as malformed skipped lines", 0, parser.getSkippedLineCount());
    }

    @Test
    public void testMalformedLogFile() throws IOException {
        List<LogEntry> entries = parser.parseLogFile("test/test_data/malformed.log");
        assertEquals("Malformed file should produce 0 valid entries", 0, entries.size());
        assertTrue("Malformed file should have skipped lines > 0", parser.getSkippedLineCount() > 0);
    }

    @Test
    public void testUnsupportedLogLevelFile() throws IOException {
        List<LogEntry> entries = parser.parseLogFile("test/test_data/unsupported_level.log");
        assertEquals("Unsupported levels (DEBUG, TRACE, FATAL) should produce 0 valid entries", 0, entries.size());
        assertEquals("Unsupported levels should be tracked as skipped lines", 4, parser.getSkippedLineCount());
    }

    @Test
    public void testSingleLogEntryFile() throws IOException {
        List<LogEntry> entries = parser.parseLogFile("test/test_data/single_entry.log");
        assertEquals("Single entry file should produce 1 entry", 1, entries.size());

        LogAnalyzer analyzer = new LogAnalyzer(entries);
        assertEquals(1, analyzer.getTotalLogsCount());
        assertEquals(Integer.valueOf(1), analyzer.getLogLevelCounts().get(LogLevel.INFO));
    }

    @Test
    public void testLargeNumberOfLogEntries() {
        List<LogEntry> largeEntries = new ArrayList<>();
        int count = 1000;
        for (int i = 1; i <= count; i++) {
            LogLevel level = (i % 3 == 0) ? LogLevel.ERROR : ((i % 2 == 0) ? LogLevel.WARNING : LogLevel.INFO);
            largeEntries.add(new LogEntry(LocalDateTime.now(), level, "Stress log message " + i, i));
        }

        LogAnalyzer analyzer = new LogAnalyzer(largeEntries);
        assertEquals(1000, analyzer.getTotalLogsCount());

        List<Alert> alerts = detector.detectAlerts(largeEntries);
        assertFalse("Large log set with many errors and warnings should trigger alerts", alerts.isEmpty());
    }

    @Test
    public void testThresholdBoundaryConditions() throws IOException {
        List<LogEntry> entries = parser.parseLogFile("test/test_data/threshold_boundary.log");
        // File has exactly 5 ERRORs, 2 Failed Logins, 2 Repeated errors, 10 WARNINGs.
        List<Alert> alerts = detector.detectAlerts(entries);
        assertTrue("Boundary threshold file (5 errors, 2 logins, 10 warnings) should NOT trigger any alert", alerts.isEmpty());

        // Exceed boundary thresholds by +1
        entries.add(new LogEntry(LocalDateTime.now(), LogLevel.ERROR, "Extra error line", 99)); // 6 errors now
        entries.add(new LogEntry(LocalDateTime.now(), LogLevel.WARNING, "Failed login attempt extra", 100)); // 3 failed logins now
        entries.add(new LogEntry(LocalDateTime.now(), LogLevel.WARNING, "Extra warning line", 101)); // 11 warnings now

        List<Alert> triggeredAlerts = detector.detectAlerts(entries);
        assertFalse("Exceeding boundary threshold by +1 should trigger alerts", triggeredAlerts.isEmpty());
    }
}
