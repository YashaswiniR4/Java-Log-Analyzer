import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Generates structured text summary reports and saves them to local disk files.
 */
public class ReportGenerator {

    private static final DateTimeFormatter REPORT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String generateReportString(LogAnalyzer analyzer, List<Alert> alerts, int skippedLines) {
        StringBuilder sb = new StringBuilder();
        Map<LogLevel, Integer> levelCounts = analyzer.getLogLevelCounts();

        sb.append("========================================\n");
        sb.append("       LOG ANALYSIS SUMMARY REPORT      \n");
        sb.append("========================================\n\n");

        sb.append("Generated On:\n");
        sb.append(LocalDateTime.now().format(REPORT_DATE_FORMATTER)).append("\n\n");

        sb.append(String.format("Total Logs      : %d\n", analyzer.getTotalLogsCount()));
        sb.append(String.format("INFO Logs       : %d\n", levelCounts.getOrDefault(LogLevel.INFO, 0)));
        sb.append(String.format("WARNING Logs    : %d\n", levelCounts.getOrDefault(LogLevel.WARNING, 0)));
        sb.append(String.format("ERROR Logs      : %d\n", levelCounts.getOrDefault(LogLevel.ERROR, 0)));
        sb.append(String.format("Skipped Lines   : %d\n\n", skippedLines));

        sb.append("----------------------------------------\n");
        sb.append("ALERTS DETECTED                         \n");
        sb.append("----------------------------------------\n\n");

        if (alerts == null || alerts.isEmpty()) {
            sb.append("No unusual patterns or alerts detected.\n\n");
        } else {
            int index = 1;
            for (Alert alert : alerts) {
                sb.append(String.format("%d. %s\n", index++, alert.toString()));
            }
            sb.append("\n");
        }

        sb.append("----------------------------------------\n");
        sb.append("END OF REPORT                           \n");
        sb.append("========================================\n");

        return sb.toString();
    }

    public void exportReportToFile(String outputPath, LogAnalyzer analyzer, List<Alert> alerts, int skippedLines) throws IOException {
        String reportContent = generateReportString(analyzer, alerts, skippedLines);
        FileUtils.writeStringToFile(outputPath, reportContent);
    }
}
