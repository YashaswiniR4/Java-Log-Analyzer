import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

public class IntegrationTest {

    private LogParser parser;
    private AlertDetector alertDetector;
    private ReportGenerator reportGenerator;

    @Before
    public void setUp() {
        parser = new LogParser();
        alertDetector = new AlertDetector();
        reportGenerator = new ReportGenerator();
    }

    @Test
    public void testCompleteEndToEndPipeline() throws IOException {
        String integrationFilePath = "test/test_data/sample_integration.log";

        // Step 1: LogParser
        List<LogEntry> entries = parser.parseLogFile(integrationFilePath);
        assertNotNull("Parsed entries list should not be null", entries);
        assertEquals("Sample integration log file should contain 20 entries", 20, entries.size());

        // Step 2: LogAnalyzer
        LogAnalyzer analyzer = new LogAnalyzer(entries);
        assertEquals(20, analyzer.getTotalLogsCount());
        assertEquals(Integer.valueOf(10), analyzer.getLogLevelCounts().get(LogLevel.INFO));
        assertEquals(Integer.valueOf(5), analyzer.getLogLevelCounts().get(LogLevel.WARNING));
        assertEquals(Integer.valueOf(5), analyzer.getLogLevelCounts().get(LogLevel.ERROR));

        // Step 3: AlertDetector
        List<Alert> alerts = alertDetector.detectAlerts(entries);
        assertNotNull("Alerts list should not be null", alerts);
        assertFalse("Integration file with 3 failed logins and 3 repeated errors should trigger alerts", alerts.isEmpty());

        boolean hasSecurityAlert = alerts.stream().anyMatch(a -> "SECURITY ALERT".equals(a.getRuleType()));
        boolean hasRepeatedErrorAlert = alerts.stream().anyMatch(a -> a.getMessage().contains("Repeated error pattern detected"));

        assertTrue("Should detect failed logins SECURITY ALERT", hasSecurityAlert);
        assertTrue("Should detect repeated error pattern ALERT", hasRepeatedErrorAlert);

        // Step 4: ReportGenerator
        String report = reportGenerator.generateReportString(analyzer, alerts, parser.getSkippedLineCount());
        assertNotNull("Generated report string should not be null", report);
        assertTrue(report.contains("Total Logs      : 20"));
        assertTrue(report.contains("INFO Logs       : 10"));
        assertTrue(report.contains("WARNING Logs    : 5"));
        assertTrue(report.contains("ERROR Logs      : 5"));
        assertTrue(report.contains("ALERTS DETECTED"));
    }
}
