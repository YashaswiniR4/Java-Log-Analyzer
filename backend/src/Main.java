import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Main application entry point providing an interactive Console Menu, Web Server, and Supabase Database Sync.
 */
public class Main {

    private static final String DEFAULT_LOG_PATH = "../logs/application.log";
    private static final String FALLBACK_LOG_PATH = "logs/application.log";
    private static final String DEFAULT_REPORT_PATH = "../reports/summary_report.txt";
    private static final String FALLBACK_REPORT_PATH = "reports/summary_report.txt";
    private static final DateTimeFormatter DATE_INPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static LogParser parser = new LogParser();
    private static LogAnalyzer analyzer = null;
    private static AlertDetector alertDetector = new AlertDetector();
    private static ReportGenerator reportGenerator = new ReportGenerator();
    private static DatabaseManager databaseManager = new DatabaseManager();
    private static LogWebServer webServer = null;
    private static String loadedFilePath = null;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("========================================");
        System.out.println("   WELCOME TO JAVA LOG ANALYZER SYSTEM  ");
        System.out.println("========================================");

        // Attempt automatic initial load of default log file
        String initialPath = FileUtils.fileExists(DEFAULT_LOG_PATH) ? DEFAULT_LOG_PATH :
                             (FileUtils.fileExists(FALLBACK_LOG_PATH) ? FALLBACK_LOG_PATH : null);

        if (initialPath != null) {
            loadLogFile(initialPath, false);
        }

        // Check if running in non-interactive server mode (e.g., Render, Docker, or CLI --server flag)
        boolean isServerMode = (args != null && args.length > 0 && "--server".equalsIgnoreCase(args[0]))
                || System.getenv("PORT") != null;

        if (isServerMode) {
            startServerMode();
            return;
        }

        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("Enter your choice: ");
            if (!scanner.hasNextLine()) {
                // Non-interactive input stream closed -> fallback to server mode
                startServerMode();
                break;
            }
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    handleLoadLogFile(scanner);
                    break;
                case "2":
                    handleViewSummary();
                    break;
                case "3":
                    handleFilterByLevel(scanner);
                    break;
                case "4":
                    handleSearchByKeyword(scanner);
                    break;
                case "5":
                    handleFilterByDate(scanner);
                    break;
                case "6":
                    handleDetectUnusualPatterns();
                    break;
                case "7":
                    handleGenerateSummaryReport();
                    break;
                case "8":
                    handleLaunchWebServer();
                    break;
                case "9":
                    handleSupabaseDatabaseSync();
                    break;
                case "10":
                    if (webServer != null) {
                        webServer.stop();
                    }
                    System.out.println("\nThank you for using Java Log Analyzer. Exiting application...");
                    running = false;
                    break;
                default:
                    System.out.println("\n[ERROR] Invalid choice. Please enter a number between 1 and 10.\n");
            }
        }
        scanner.close();
    }

    private static void printMenu() {
        System.out.println("========================================");
        System.out.println("       JAVA LOG ANALYZER & MONITOR      ");
        System.out.println("========================================");
        System.out.println("1. Load and Analyze Log File");
        System.out.println("2. View Summary");
        System.out.println("3. Filter Logs by Level");
        System.out.println("4. Search Logs by Keyword");
        System.out.println("5. Filter Logs by Date");
        System.out.println("6. Detect Unusual Patterns");
        System.out.println("7. Generate Summary Report");
        System.out.println("8. Launch Web Dashboard Server (http://localhost:8080)");
        System.out.println("9. Sync Logs & Alerts with Supabase Database");
        System.out.println("10. Exit");
        System.out.println("========================================");
    }

    private static void handleLoadLogFile(Scanner scanner) {
        String defaultPath = loadedFilePath != null ? loadedFilePath : (FileUtils.fileExists(DEFAULT_LOG_PATH) ? DEFAULT_LOG_PATH : FALLBACK_LOG_PATH);
        System.out.print("\nEnter log file path (Press Enter for default: " + defaultPath + "): ");
        String customPath = scanner.nextLine().trim();
        String path = customPath.isEmpty() ? defaultPath : customPath;
        loadLogFile(path, true);
    }

    private static boolean loadLogFile(String filePath, boolean verbose) {
        try {
            List<LogEntry> entries = parser.parseLogFile(filePath);
            analyzer = new LogAnalyzer(entries);
            loadedFilePath = filePath;

            if (verbose) {
                System.out.println("\n[SUCCESS] Log file loaded successfully from: " + filePath);
                System.out.println("Successfully parsed entries : " + analyzer.getTotalLogsCount());
                System.out.println("Skipped malformed lines     : " + parser.getSkippedLineCount() + "\n");
            }
            return true;
        } catch (IOException e) {
            System.out.println("\n[ERROR] Failed to load log file: " + e.getMessage() + "\n");
            return false;
        }
    }

    private static boolean checkLogsLoaded() {
        if (analyzer == null) {
            System.out.println("\n[WARNING] No log file currently loaded. Attempting to load default log file...");
            String path = FileUtils.fileExists(DEFAULT_LOG_PATH) ? DEFAULT_LOG_PATH : FALLBACK_LOG_PATH;
            if (!loadLogFile(path, true)) {
                System.out.println("[ERROR] Please option 1 to specify a valid log file first.\n");
                return false;
            }
        }
        return true;
    }

    private static void handleViewSummary() {
        if (!checkLogsLoaded()) return;

        Map<LogLevel, Integer> levelCounts = analyzer.getLogLevelCounts();

        System.out.println("\n========================================");
        System.out.println("       LOG ANALYZER SUMMARY             ");
        System.out.println("========================================");
        System.out.println("Active File     : " + loadedFilePath);
        System.out.println("Total Valid Logs: " + analyzer.getTotalLogsCount());
        System.out.println("INFO Logs       : " + levelCounts.getOrDefault(LogLevel.INFO, 0));
        System.out.println("WARNING Logs    : " + levelCounts.getOrDefault(LogLevel.WARNING, 0));
        System.out.println("ERROR Logs      : " + levelCounts.getOrDefault(LogLevel.ERROR, 0));
        System.out.println("Skipped Lines   : " + parser.getSkippedLineCount());
        System.out.println("Supabase Config : " + databaseManager.getConnectionStatusMessage());
        System.out.println("========================================\n");
    }

    private static void handleFilterByLevel(Scanner scanner) {
        if (!checkLogsLoaded()) return;

        System.out.print("\nEnter log level (INFO, WARNING, ERROR): ");
        String levelInput = scanner.nextLine().trim();
        LogLevel level = LogLevel.parseLevel(levelInput);

        if (level == null) {
            System.out.println("[ERROR] Invalid log level entered. Allowed values: INFO, WARNING, ERROR.\n");
            return;
        }

        List<LogEntry> filtered = analyzer.filterByLevel(level);
        System.out.println("\n----------------------------------------");
        System.out.println(level + " LOGS (" + filtered.size() + " entries found)");
        System.out.println("----------------------------------------");
        if (filtered.isEmpty()) {
            System.out.println("No matching entries found.");
        } else {
            for (LogEntry entry : filtered) {
                System.out.println(entry);
            }
        }
        System.out.println("----------------------------------------\n");
    }

    private static void handleSearchByKeyword(Scanner scanner) {
        if (!checkLogsLoaded()) return;

        System.out.print("\nEnter search keyword (e.g. database, login, failed): ");
        String keyword = scanner.nextLine().trim();

        if (keyword.isEmpty()) {
            System.out.println("[ERROR] Search keyword cannot be empty.\n");
            return;
        }

        List<LogEntry> matching = analyzer.searchByKeyword(keyword);
        System.out.println("\n----------------------------------------");
        System.out.println("KEYWORD SEARCH: \"" + keyword + "\" (" + matching.size() + " matches)");
        System.out.println("----------------------------------------");
        if (matching.isEmpty()) {
            System.out.println("No logs matching keyword found.");
        } else {
            for (LogEntry entry : matching) {
                System.out.println(entry);
            }
        }
        System.out.println("----------------------------------------\n");
    }

    private static void handleFilterByDate(Scanner scanner) {
        if (!checkLogsLoaded()) return;

        System.out.print("\nEnter date to filter (YYYY-MM-DD, e.g. 2026-08-18): ");
        String dateStr = scanner.nextLine().trim();

        try {
            LocalDate targetDate = LocalDate.parse(dateStr, DATE_INPUT_FORMATTER);
            List<LogEntry> matching = analyzer.filterByDate(targetDate);

            System.out.println("\n----------------------------------------");
            System.out.println("LOGS FOR DATE: " + dateStr + " (" + matching.size() + " matches)");
            System.out.println("----------------------------------------");
            if (matching.isEmpty()) {
                System.out.println("No log entries found for date: " + dateStr);
            } else {
                for (LogEntry entry : matching) {
                    System.out.println(entry);
                }
            }
            System.out.println("----------------------------------------\n");
        } catch (DateTimeParseException e) {
            System.out.println("[ERROR] Invalid date format. Please use YYYY-MM-DD (e.g., 2026-08-18).\n");
        }
    }

    private static void handleDetectUnusualPatterns() {
        if (!checkLogsLoaded()) return;

        List<Alert> alerts = alertDetector.detectAlerts(analyzer.getLogEntries());

        System.out.println("\n========================================");
        System.out.println("             ALERTS DETECTED            ");
        System.out.println("========================================");
        if (alerts.isEmpty()) {
            System.out.println("No unusual patterns or alerts detected.");
        } else {
            for (Alert alert : alerts) {
                System.out.println(alert);
            }
        }
        System.out.println("========================================\n");
    }

    private static void handleGenerateSummaryReport() {
        if (!checkLogsLoaded()) return;

        List<Alert> alerts = alertDetector.detectAlerts(analyzer.getLogEntries());

        String targetReportPath = FileUtils.fileExists("../reports") || new java.io.File("../reports").exists() ? DEFAULT_REPORT_PATH : FALLBACK_REPORT_PATH;

        try {
            reportGenerator.exportReportToFile(targetReportPath, analyzer, alerts, parser.getSkippedLineCount());
            System.out.println("\n[SUCCESS] Summary report successfully generated!");
            System.out.println("Report Location: " + targetReportPath + "\n");
        } catch (IOException e) {
            System.out.println("\n[ERROR] Failed to write summary report: " + e.getMessage() + "\n");
        }
    }

    private static void handleLaunchWebServer() {
        if (!checkLogsLoaded()) return;

        if (webServer != null) {
            System.out.println("\n[INFO] Web Server is already running on http://localhost:8080/");
            return;
        }

        try {
            webServer = new LogWebServer(8080, analyzer, parser, alertDetector, reportGenerator, databaseManager, loadedFilePath);
            webServer.start();
        } catch (IOException e) {
            System.out.println("\n[ERROR] Failed to start Web Dashboard Server: " + e.getMessage() + "\n");
        }
    }

    private static void handleSupabaseDatabaseSync() {
        if (!checkLogsLoaded()) return;

        System.out.println("\n========================================");
        System.out.println("      SUPABASE DATABASE INTEGRATION     ");
        System.out.println("========================================");
        System.out.println("Status: " + databaseManager.getConnectionStatusMessage());

        if (databaseManager.isConnected()) {
            System.out.println("\nSynchronizing logs and alerts with Supabase PostgreSQL...");
            databaseManager.saveLogs(analyzer.getLogEntries());
            List<Alert> alerts = alertDetector.detectAlerts(analyzer.getLogEntries());
            databaseManager.saveAlerts(alerts);
        } else {
            System.out.println("\n[NOTE] SUPABASE_DB_URL password is not set in .env.");
            System.out.println("To enable Supabase PostgreSQL persistence, update SUPABASE_DB_URL in .env file.");
        }
        System.out.println("========================================\n");
    }

    private static void startServerMode() {
        int port = 8080;
        String portEnv = System.getenv("PORT");
        if (portEnv != null && !portEnv.isBlank()) {
            try {
                port = Integer.parseInt(portEnv.trim());
            } catch (Exception ignored) {}
        }

        System.out.println("\n[SERVER MODE] Launching Java Log Analyzer Web Server on port " + port + "...");

        try {
            if (analyzer == null) {
                analyzer = new LogAnalyzer(java.util.Collections.emptyList());
            }

            webServer = new LogWebServer(port, analyzer, parser, alertDetector, reportGenerator, databaseManager, loadedFilePath);
            webServer.start();

            System.out.println("[SERVER MODE] Server active & listening on port " + port + ". Thread locked.");
            synchronized (Main.class) {
                Main.class.wait();
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Server Mode Execution Error: " + e.getMessage());
        }
    }
}
