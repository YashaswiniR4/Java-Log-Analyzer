import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

public class LogParserTest {

    private LogParser parser;

    @Before
    public void setUp() {
        parser = new LogParser();
    }

    @Test
    public void testParseValidInfoLog() {
        String line = "2026-10-01 10:15:30 INFO User logged in successfully";
        LogEntry entry = parser.parseLine(line, 1);

        assertNotNull("Parsed entry should not be null", entry);
        assertEquals("INFO", entry.getLevel().name());
        assertEquals("User logged in successfully", entry.getMessage());
        assertEquals(1, entry.getRawLineNumber());
        assertEquals("2026-10-01 10:15:30", entry.getFormattedTimestamp());
    }

    @Test
    public void testParseValidWarningLog() {
        String line = "2026-10-01 11:20:00 WARNING High CPU utilization detected";
        LogEntry entry = parser.parseLine(line, 2);

        assertNotNull("Parsed entry should not be null", entry);
        assertEquals("WARNING", entry.getLevel().name());
        assertEquals("High CPU utilization detected", entry.getMessage());
        assertEquals(2, entry.getRawLineNumber());
    }

    @Test
    public void testParseValidErrorLog() {
        String line = "2026-10-01 12:30:45 ERROR Database connection failed";
        LogEntry entry = parser.parseLine(line, 3);

        assertNotNull("Parsed entry should not be null", entry);
        assertEquals("ERROR", entry.getLevel().name());
        assertEquals("Database connection failed", entry.getMessage());
        assertEquals(3, entry.getRawLineNumber());
    }

    @Test
    public void testParseEmptyLine() {
        LogEntry entry1 = parser.parseLine("", 1);
        LogEntry entry2 = parser.parseLine("   ", 2);
        LogEntry entry3 = parser.parseLine(null, 3);

        assertNull("Empty line should return null", entry1);
        assertNull("Whitespace line should return null", entry2);
        assertNull("Null line should return null", entry3);
    }

    @Test
    public void testParseMalformedLine() {
        String malformedLine = "This is a corrupted log line without structure";
        LogEntry entry = parser.parseLine(malformedLine, 4);

        assertNull("Malformed line should return null", entry);
    }

    @Test
    public void testParseLineMissingTimestamp() {
        String line = "INFO System status check";
        LogEntry entry = parser.parseLine(line, 5);

        assertNull("Line missing timestamp should return null", entry);
    }

    @Test
    public void testParseLineMissingLevel() {
        String line = "2026-10-01 10:00:00 System status check";
        LogEntry entry = parser.parseLine(line, 6);

        assertNull("Line missing log level should return null", entry);
    }

    @Test
    public void testParseLineUnsupportedLevel() {
        String line = "2026-10-01 10:00:00 DEBUG Debugging variable x = 5";
        LogEntry entry = parser.parseLine(line, 7);

        assertNull("Unsupported level DEBUG should return null", entry);
    }

    @Test
    public void testParseLogContentWithMixedLines() {
        String content = "2026-10-01 10:00:00 INFO System initialized\n" +
                         "Malformed corrupt line here\n" +
                         "2026-10-01 10:01:00 ERROR Out of memory\n" +
                         "\n" +
                         "2026-10-01 10:02:00 WARNING Disk usage high\n";

        List<LogEntry> entries = parser.parseLogContent(content);

        assertEquals("Should parse 3 valid log entries", 3, entries.size());
        assertEquals("Should track 1 skipped malformed line", 1, parser.getSkippedLineCount());
        assertEquals("First entry should be INFO", LogLevel.INFO, entries.get(0).getLevel());
        assertEquals("Second entry should be ERROR", LogLevel.ERROR, entries.get(1).getLevel());
        assertEquals("Third entry should be WARNING", LogLevel.WARNING, entries.get(2).getLevel());
    }

    @Test
    public void testParseLogFileNonExistentFile() {
        try {
            parser.parseLogFile("non_existent_path/file.log");
            fail("Expected IOException when file does not exist");
        } catch (IOException e) {
            assertTrue("Exception message should indicate file not found", e.getMessage().contains("not found"));
        }
    }
}
