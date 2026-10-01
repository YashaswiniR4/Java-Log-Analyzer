import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LogAnalyzerTest {

    private LogAnalyzer analyzer;
    private List<LogEntry> sampleEntries;

    @Before
    public void setUp() {
        sampleEntries = new ArrayList<>();
        sampleEntries.add(new LogEntry(LocalDateTime.of(2026, 10, 1, 10, 0, 0), LogLevel.INFO, "System started", 1));
        sampleEntries.add(new LogEntry(LocalDateTime.of(2026, 10, 1, 10, 5, 0), LogLevel.INFO, "User login successful", 2));
        sampleEntries.add(new LogEntry(LocalDateTime.of(2026, 10, 1, 10, 10, 0), LogLevel.WARNING, "High memory usage detected", 3));
        sampleEntries.add(new LogEntry(LocalDateTime.of(2026, 10, 1, 10, 15, 0), LogLevel.WARNING, "Failed login attempt from IP 10.0.0.1", 4));
        sampleEntries.add(new LogEntry(LocalDateTime.of(2026, 10, 1, 10, 20, 0), LogLevel.ERROR, "Database connection failed", 5));
        sampleEntries.add(new LogEntry(LocalDateTime.of(2026, 10, 2, 10, 0, 0), LogLevel.ERROR, "Disk read timeout", 6));

        analyzer = new LogAnalyzer(sampleEntries);
    }

    @Test
    public void testGetTotalLogsCount() {
        assertEquals(6, analyzer.getTotalLogsCount());
    }

    @Test
    public void testGetLogLevelCounts() {
        Map<LogLevel, Integer> counts = analyzer.getLogLevelCounts();

        assertEquals(Integer.valueOf(2), counts.get(LogLevel.INFO));
        assertEquals(Integer.valueOf(2), counts.get(LogLevel.WARNING));
        assertEquals(Integer.valueOf(2), counts.get(LogLevel.ERROR));
    }

    @Test
    public void testEmptyInput() {
        LogAnalyzer emptyAnalyzer = new LogAnalyzer(new ArrayList<>());

        assertEquals(0, emptyAnalyzer.getTotalLogsCount());
        Map<LogLevel, Integer> counts = emptyAnalyzer.getLogLevelCounts();
        assertEquals(Integer.valueOf(0), counts.get(LogLevel.INFO));
        assertEquals(Integer.valueOf(0), counts.get(LogLevel.WARNING));
        assertEquals(Integer.valueOf(0), counts.get(LogLevel.ERROR));
    }

    @Test
    public void testNullInput() {
        LogAnalyzer nullAnalyzer = new LogAnalyzer(null);
        assertEquals(0, nullAnalyzer.getTotalLogsCount());
    }

    @Test
    public void testFilterByLevelInfo() {
        List<LogEntry> infoLogs = analyzer.filterByLevel(LogLevel.INFO);
        assertEquals(2, infoLogs.size());
        for (LogEntry entry : infoLogs) {
            assertEquals(LogLevel.INFO, entry.getLevel());
        }
    }

    @Test
    public void testFilterByLevelWarning() {
        List<LogEntry> warnLogs = analyzer.filterByLevel(LogLevel.WARNING);
        assertEquals(2, warnLogs.size());
        for (LogEntry entry : warnLogs) {
            assertEquals(LogLevel.WARNING, entry.getLevel());
        }
    }

    @Test
    public void testFilterByLevelError() {
        List<LogEntry> errorLogs = analyzer.filterByLevel(LogLevel.ERROR);
        assertEquals(2, errorLogs.size());
        for (LogEntry entry : errorLogs) {
            assertEquals(LogLevel.ERROR, entry.getLevel());
        }
    }

    @Test
    public void testFilterByLevelNull() {
        List<LogEntry> logs = analyzer.filterByLevel(null);
        assertTrue("Filter with null level should return empty list", logs.isEmpty());
    }

    @Test
    public void testSearchByKeywordCaseInsensitive() {
        List<LogEntry> dbResults = analyzer.searchByKeyword("DATABASE");
        assertEquals(1, dbResults.size());
        assertTrue(dbResults.get(0).getMessage().contains("Database connection failed"));

        List<LogEntry> loginResults = analyzer.searchByKeyword("login");
        assertEquals(2, loginResults.size());
    }

    @Test
    public void testSearchByKeywordNoMatch() {
        List<LogEntry> results = analyzer.searchByKeyword("nonexistent_search_query");
        assertTrue(results.isEmpty());
    }

    @Test
    public void testSearchByKeywordNullOrEmpty() {
        assertTrue(analyzer.searchByKeyword(null).isEmpty());
        assertTrue(analyzer.searchByKeyword("").isEmpty());
        assertTrue(analyzer.searchByKeyword("   ").isEmpty());
    }

    @Test
    public void testFilterByDate() {
        LocalDate date1 = LocalDate.of(2026, 10, 1);
        List<LogEntry> date1Logs = analyzer.filterByDate(date1);
        assertEquals(5, date1Logs.size());

        LocalDate date2 = LocalDate.of(2026, 10, 2);
        List<LogEntry> date2Logs = analyzer.filterByDate(date2);
        assertEquals(1, date2Logs.size());
    }

    @Test
    public void testFilterByDateNull() {
        List<LogEntry> logs = analyzer.filterByDate(null);
        assertTrue(logs.isEmpty());
    }

    @Test
    public void testSetLogEntries() {
        List<LogEntry> newEntries = new ArrayList<>();
        newEntries.add(new LogEntry(LocalDateTime.now(), LogLevel.INFO, "Updated entry", 1));

        analyzer.setLogEntries(newEntries);
        assertEquals(1, analyzer.getTotalLogsCount());
        assertEquals("Updated entry", analyzer.getLogEntries().get(0).getMessage());
    }
}
