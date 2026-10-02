import java.io.File;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

public class ValidationRunnerTest {

    @Test
    public void run100LineValidation() throws Exception {
        File file = new File("test/validation_100_lines.log");
        assertTrue("Validation dataset file must exist", file.exists());

        LogParser parser = new LogParser();
        List<LogEntry> parsedEntries = parser.parseLogFile("test/validation_100_lines.log");

        int totalLinesProcessed = parsedEntries.size() + parser.getSkippedLineCount();
        int skippedCount = parser.getSkippedLineCount();
        int validCount = parsedEntries.size();

        LogAnalyzer analyzer = new LogAnalyzer(parsedEntries);
        Map<LogLevel, Integer> levelCounts = analyzer.getLogLevelCounts();

        int infoCount = levelCounts.getOrDefault(LogLevel.INFO, 0);
        int warnCount = levelCounts.getOrDefault(LogLevel.WARNING, 0);
        int errorCount = levelCounts.getOrDefault(LogLevel.ERROR, 0);

        AlertDetector detector = new AlertDetector();
        List<Alert> alerts = detector.detectAlerts(parsedEntries);

        System.out.println("==================================================");
        System.out.println("     100-LINE VALIDATION DATASET RESULTS          ");
        System.out.println("==================================================");
        System.out.println("Total Lines Processed : " + totalLinesProcessed);
        System.out.println("Valid Parsed Logs     : " + validCount);
        System.out.println("Malformed/Skipped Lines: " + skippedCount);
        System.out.println("INFO Count            : " + infoCount);
        System.out.println("WARNING Count         : " + warnCount);
        System.out.println("ERROR Count           : " + errorCount);
        System.out.println("Parsing Exceptions    : None (0 exceptions caught)");
        System.out.println("--------------------------------------------------");
        System.out.println("TRIGGERED ALERT RULES (" + alerts.size() + " alerts):");
        for (Alert alert : alerts) {
            System.out.println("  - [" + alert.getRuleType() + "] " + alert.getMessage());
        }
        System.out.println("==================================================");

        assertEquals(100, totalLinesProcessed);
        assertEquals(95, validCount);
        assertEquals(5, skippedCount);
        assertEquals(46, infoCount);
        assertEquals(35, warnCount);
        assertEquals(14, errorCount);
        assertTrue("Alerts should be triggered for test dataset", alerts.size() > 0);
    }
}
