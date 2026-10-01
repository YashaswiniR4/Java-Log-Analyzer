import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReportGeneratorTest {

    private ReportGenerator reportGenerator;
    private LogAnalyzer analyzer;

    @Before
    public void setUp() {
        reportGenerator = new ReportGenerator();

        List<LogEntry> entries = new ArrayList<>();
        entries.add(new LogEntry(LocalDateTime.now(), LogLevel.INFO, "System online", 1));
        entries.add(new LogEntry(LocalDateTime.now(), LogLevel.WARNING, "Memory usage 85%", 2));
        entries.add(new LogEntry(LocalDateTime.now(), LogLevel.ERROR, "Database connection error", 3));

        analyzer = new LogAnalyzer(entries);
    }

    @Test
    public void testGenerateReportStringWithAlerts() {
        List<Alert> alerts = new ArrayList<>();
        alerts.add(new Alert("High error rate detected", "ALERT"));
        alerts.add(new Alert("Multiple failed login attempts detected", "SECURITY ALERT"));

        String report = reportGenerator.generateReportString(analyzer, alerts, 2);

        assertNotNull(report);
        assertTrue(report.contains("LOG ANALYSIS SUMMARY REPORT"));
        assertTrue(report.contains("Total Logs      : 3"));
        assertTrue(report.contains("INFO Logs       : 1"));
        assertTrue(report.contains("WARNING Logs    : 1"));
        assertTrue(report.contains("ERROR Logs      : 1"));
        assertTrue(report.contains("Skipped Lines   : 2"));
        assertTrue(report.contains("1. [ALERT] High error rate detected"));
        assertTrue(report.contains("2. [SECURITY ALERT] Multiple failed login attempts detected"));
    }

    @Test
    public void testGenerateReportStringWithNoAlerts() {
        String report = reportGenerator.generateReportString(analyzer, new ArrayList<>(), 0);

        assertNotNull(report);
        assertTrue(report.contains("No unusual patterns or alerts detected."));
        assertTrue(report.contains("Skipped Lines   : 0"));
    }

    @Test
    public void testExportReportToFile() throws IOException {
        String outputPath = "test/test_data/temp_export_report.txt";
        List<Alert> alerts = new ArrayList<>();
        alerts.add(new Alert("Test alert for export", "ALERT"));

        reportGenerator.exportReportToFile(outputPath, analyzer, alerts, 1);

        File exportedFile = new File(outputPath);
        assertTrue("Exported file should exist on disk", exportedFile.exists());

        List<String> lines = FileUtils.readLines(outputPath);
        String fileContent = String.join("\n", lines);
        assertTrue(fileContent.contains("LOG ANALYSIS SUMMARY REPORT"));
        assertTrue(fileContent.contains("Test alert for export"));

        exportedFile.delete();
    }
}
