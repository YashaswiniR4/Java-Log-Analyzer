import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Responsible for opening log files, parsing lines using regular expressions,
 * and creating structured LogEntry objects. Skips malformed lines gracefully.
 */
public class LogParser {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Pattern LOG_PATTERN = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2})\\s+(INFO|WARNING|ERROR)\\s+(.*)$");

    private int skippedLineCount = 0;
    private final List<String> skippedLineDetails = new ArrayList<>();

    public List<LogEntry> parseLogFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("Log file not found: " + filePath);
        }

        List<LogEntry> entries = new ArrayList<>();
        skippedLineCount = 0;
        skippedLineDetails.clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                }

                LogEntry entry = parseLine(line, lineNumber);
                if (entry != null) {
                    entries.add(entry);
                } else {
                    skippedLineCount++;
                    skippedLineDetails.add("Line " + lineNumber + ": " + line);
                }
            }
        }

        return entries;
    }

    public List<LogEntry> parseLogContent(String content) {
        List<LogEntry> entries = new ArrayList<>();
        skippedLineCount = 0;
        skippedLineDetails.clear();

        if (content == null || content.isEmpty()) {
            return entries;
        }

        String[] lines = content.split("\\r?\\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue;
            }

            LogEntry entry = parseLine(line, i + 1);
            if (entry != null) {
                entries.add(entry);
            } else {
                skippedLineCount++;
                skippedLineDetails.add("Line " + (i + 1) + ": " + line);
            }
        }

        return entries;
    }

    public LogEntry parseLine(String line, int lineNumber) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        Matcher matcher = LOG_PATTERN.matcher(line.trim());
        if (!matcher.matches()) {
            return null;
        }

        String timestampStr = matcher.group(1);
        String levelStr = matcher.group(2);
        String message = matcher.group(3);

        try {
            LocalDateTime timestamp = LocalDateTime.parse(timestampStr, DATE_FORMATTER);
            LogLevel level = LogLevel.parseLevel(levelStr);

            if (level == null) {
                return null;
            }

            return new LogEntry(timestamp, level, message, lineNumber);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public int getSkippedLineCount() {
        return skippedLineCount;
    }

    public List<String> getSkippedLineDetails() {
        return new ArrayList<>(skippedLineDetails);
    }
}
