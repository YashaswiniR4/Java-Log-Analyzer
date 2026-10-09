import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Embedded JDK HTTP Server providing REST API endpoints for React / Vite frontend and static file serving.
 */
public class LogWebServer {

    private final int port;
    private final LogAnalyzer analyzer;
    private final LogParser parser;
    private final AlertDetector alertDetector;
    private final ReportGenerator reportGenerator;
    private final DatabaseManager databaseManager;
    private final AuthService authService;
    private String logFilePath;
    private HttpServer server;

    public LogWebServer(int port, LogAnalyzer analyzer, LogParser parser, AlertDetector alertDetector, ReportGenerator reportGenerator, DatabaseManager databaseManager, String logFilePath) {
        this.port = port;
        this.analyzer = analyzer;
        this.parser = parser;
        this.alertDetector = alertDetector;
        this.reportGenerator = reportGenerator;
        this.databaseManager = databaseManager;
        this.authService = new AuthService(databaseManager);
        this.logFilePath = logFilePath;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // REST API Handlers
        server.createContext("/api/health", new HealthHandler());
        server.createContext("/api/summary", new SummaryHandler());
        server.createContext("/api/logs", new LogsHandler());
        server.createContext("/api/alerts", new AlertsHandler());
        server.createContext("/api/report", new ReportHandler());
        server.createContext("/api/sync", new SyncHandler());
        server.createContext("/api/upload", new UploadHandler());

        // Auth REST API Handlers
        server.createContext("/api/auth/register", new AuthRegisterHandler());
        server.createContext("/api/auth/verify-otp", new AuthVerifyOtpHandler());
        server.createContext("/api/auth/login", new AuthLoginHandler());
        server.createContext("/api/auth/forgot-password", new AuthForgotPasswordHandler());
        server.createContext("/api/auth/reset-password", new AuthResetPasswordHandler());
        server.createContext("/api/auth/me", new AuthMeHandler());
        server.createContext("/api/auth/logout", new AuthLogoutHandler());

        // Static Assets Handler (Serves frontend dashboard)
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(null);
        server.start();
        System.out.println("\n========================================");
        System.out.println("  JAVA LOG ANALYZER & MONITORING API  ");
        System.out.println("========================================");
        System.out.println("Server URL  : http://localhost:" + port + "/");
        System.out.println("API Health  : http://localhost:" + port + "/api/health");
        System.out.println("DB Status   : " + databaseManager.getConnectionStatusMessage());
        System.out.println("========================================\n");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("\n[INFO] Backend Web Server stopped.");
        }
    }

    private static void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void sendTextResponse(HttpExchange exchange, int statusCode, String text) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) {
            return params;
        }
        for (String param : query.split("&")) {
            String[] entry = param.split("=", 2);
            if (entry.length > 0) {
                String key = URLDecoder.decode(entry[0], StandardCharsets.UTF_8);
                String value = entry.length > 1 ? URLDecoder.decode(entry[1], StandardCharsets.UTF_8) : "";
                params.put(key, value);
            }
        }
        return params;
    }

    private class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String json = String.format(
                    "{\"status\":\"UP\",\"dbConnected\":%b,\"dbMessage\":\"%s\",\"totalLogs\":%d}",
                    databaseManager.isConnected(),
                    escapeJson(databaseManager.getConnectionStatusMessage()),
                    analyzer.getTotalLogsCount()
            );
            sendJsonResponse(exchange, 200, json);
        }
    }

    private class SummaryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 204, "");
                return;
            }

            Map<LogLevel, Integer> levelCounts = analyzer.getLogLevelCounts();
            String json = String.format(
                    "{\"totalLogs\":%d,\"infoCount\":%d,\"warningCount\":%d,\"errorCount\":%d,\"skippedLines\":%d,\"activeFile\":\"%s\",\"dbConnected\":%b,\"dbStatus\":\"%s\"}",
                    analyzer.getTotalLogsCount(),
                    levelCounts.getOrDefault(LogLevel.INFO, 0),
                    levelCounts.getOrDefault(LogLevel.WARNING, 0),
                    levelCounts.getOrDefault(LogLevel.ERROR, 0),
                    parser.getSkippedLineCount(),
                    logFilePath,
                    databaseManager.isConnected(),
                    escapeJson(databaseManager.getConnectionStatusMessage())
            );
            sendJsonResponse(exchange, 200, json);
        }
    }

    private class LogsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 204, "");
                return;
            }

            Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
            String levelFilter = params.getOrDefault("level", "ALL");
            String search = params.getOrDefault("search", "");

            List<LogEntry> entries = filterInMemoryLogs(levelFilter, search, params.get("date"));

            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < entries.size(); i++) {
                LogEntry entry = entries.get(i);
                json.append(String.format(
                        "{\"timestamp\":\"%s\",\"level\":\"%s\",\"message\":\"%s\",\"rawLineNumber\":%d}",
                        entry.getFormattedTimestamp(),
                        entry.getLevel().name(),
                        escapeJson(entry.getMessage()),
                        entry.getRawLineNumber()
                ));
                if (i < entries.size() - 1) {
                    json.append(",");
                }
            }
            json.append("]");

            sendJsonResponse(exchange, 200, json.toString());
        }

        private List<LogEntry> filterInMemoryLogs(String levelStr, String search, String dateStr) {
            List<LogEntry> list = analyzer.getLogEntries();

            if (levelStr != null && !"ALL".equalsIgnoreCase(levelStr)) {
                LogLevel level = LogLevel.parseLevel(levelStr);
                if (level != null) {
                    list = analyzer.filterByLevel(level);
                }
            }

            if (search != null && !search.isBlank()) {
                list = list.stream()
                        .filter(e -> e.getMessage().toLowerCase().contains(search.toLowerCase()))
                        .toList();
            }

            if (dateStr != null && !dateStr.isBlank()) {
                try {
                    LocalDate date = LocalDate.parse(dateStr);
                    list = list.stream()
                            .filter(e -> e.getTimestamp() != null && e.getTimestamp().toLocalDate().equals(date))
                            .toList();
                } catch (Exception ignored) {}
            }

            return list;
        }
    }

    private class AlertsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 204, "");
                return;
            }

            List<Alert> alerts = alertDetector.detectAlerts(analyzer.getLogEntries());
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < alerts.size(); i++) {
                Alert alert = alerts.get(i);
                json.append(String.format(
                        "{\"ruleType\":\"%s\",\"message\":\"%s\",\"timestamp\":\"%s\"}",
                        escapeJson(alert.getRuleType()),
                        escapeJson(alert.getMessage()),
                        alert.getFormattedTimestamp()
                ));
                if (i < alerts.size() - 1) {
                    json.append(",");
                }
            }
            json.append("]");

            sendJsonResponse(exchange, 200, json.toString());
        }
    }

    private class ReportHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 204, "");
                return;
            }

            List<Alert> alerts = alertDetector.detectAlerts(analyzer.getLogEntries());
            String reportText = reportGenerator.generateReportString(analyzer, alerts, parser.getSkippedLineCount());

            // Save report to database if connected
            databaseManager.saveReport(reportText, analyzer.getTotalLogsCount(),
                    analyzer.getLogLevelCounts().getOrDefault(LogLevel.INFO, 0),
                    analyzer.getLogLevelCounts().getOrDefault(LogLevel.WARNING, 0),
                    analyzer.getLogLevelCounts().getOrDefault(LogLevel.ERROR, 0),
                    parser.getSkippedLineCount());

            sendTextResponse(exchange, 200, reportText);
        }
    }

    private class SyncHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 204, "");
                return;
            }

            boolean logSaved = databaseManager.saveLogs(analyzer.getLogEntries());
            List<Alert> alerts = alertDetector.detectAlerts(analyzer.getLogEntries());
            boolean alertSaved = databaseManager.saveAlerts(alerts);

            if (logSaved && alertSaved) {
                sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Successfully synchronized log records and alerts to Supabase PostgreSQL database!\"}");
            } else {
                sendJsonResponse(exchange, 200, "{\"success\":false,\"message\":\"Analysis complete locally. Configure SUPABASE_DB_URL in .env to enable Supabase database persistence.\"}");
            }
        }
    }

    private class UploadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 204, "");
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed. Use POST for file upload.\"}");
                return;
            }

            try (InputStream is = exchange.getRequestBody(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }

                String rawBody = baos.toString(StandardCharsets.UTF_8);

                // Strip basic multipart boundary header if uploaded via FormData
                String fileContent = rawBody;
                if (rawBody.contains("Content-Type:") || rawBody.contains("filename=")) {
                    int contentStart = rawBody.indexOf("\r\n\r\n");
                    if (contentStart != -1) {
                        fileContent = rawBody.substring(contentStart + 4);
                        int endBoundary = fileContent.lastIndexOf("\r\n--");
                        if (endBoundary != -1) {
                            fileContent = fileContent.substring(0, endBoundary);
                        }
                    }
                }

                List<LogEntry> parsedEntries = parser.parseLogContent(fileContent);
                analyzer.setLogEntries(parsedEntries);
                logFilePath = "Uploaded File (" + parsedEntries.size() + " entries parsed)";

                // Persist new records into Supabase PostgreSQL
                boolean dbSaved = databaseManager.saveLogs(parsedEntries);
                List<Alert> newAlerts = alertDetector.detectAlerts(parsedEntries);
                databaseManager.saveAlerts(newAlerts);

                Map<LogLevel, Integer> counts = analyzer.getLogLevelCounts();
                String msg = String.format(
                    "Successfully uploaded and analyzed %d log entries! (%d INFO, %d WARNING, %d ERROR, %d Skipped). %s",
                    parsedEntries.size(),
                    counts.getOrDefault(LogLevel.INFO, 0),
                    counts.getOrDefault(LogLevel.WARNING, 0),
                    counts.getOrDefault(LogLevel.ERROR, 0),
                    parser.getSkippedLineCount(),
                    dbSaved ? "Persisted to Supabase PostgreSQL database." : "Retained locally."
                );

                String json = String.format(
                    "{\"success\":true,\"message\":\"%s\",\"totalLogs\":%d,\"infoCount\":%d,\"warningCount\":%d,\"errorCount\":%d,\"skippedLines\":%d}",
                    escapeJson(msg),
                    analyzer.getTotalLogsCount(),
                    counts.getOrDefault(LogLevel.INFO, 0),
                    counts.getOrDefault(LogLevel.WARNING, 0),
                    counts.getOrDefault(LogLevel.ERROR, 0),
                    parser.getSkippedLineCount()
                );

                sendJsonResponse(exchange, 200, json);

            } catch (Exception e) {
                sendJsonResponse(exchange, 500, "{\"success\":false,\"message\":\"Upload processing error: " + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    // ================================================================================
    //                       AUTHENTICATION HANDLERS
    // ================================================================================

    private static String getClientIp(HttpExchange exchange) {
        String xForwardedFor = exchange.getRequestHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        InetSocketAddress remote = exchange.getRemoteAddress();
        return (remote != null && remote.getAddress() != null) ? remote.getAddress().getHostAddress() : "unknown";
    }

    private class AuthRegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 204, "");
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
                return;
            }
            String body = readRequestBody(exchange);
            String name = getJsonValue(body, "name");
            String email = getJsonValue(body, "email");
            String username = getJsonValue(body, "username");
            String phone = getJsonValue(body, "phone");
            String password = getJsonValue(body, "password");
            String confirmPassword = getJsonValue(body, "confirmPassword");

            String clientIp = getClientIp(exchange);
            AuthService.AuthResult res = authService.register(name, email, username, phone, password, confirmPassword, clientIp);
            sendJsonResponse(exchange, res.isSuccess() ? 200 : 400, res.toJson());
        }
    }

    private class AuthVerifyOtpHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 204, "");
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
                return;
            }
            String body = readRequestBody(exchange);
            String email = getJsonValue(body, "email");
            String otpCode = getJsonValue(body, "otpCode");

            AuthService.AuthResult res = authService.verifyOtp(email, otpCode);
            sendJsonResponse(exchange, res.isSuccess() ? 200 : 400, res.toJson());
        }
    }

    private class AuthLoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 204, "");
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
                return;
            }
            String body = readRequestBody(exchange);
            String identifier = getJsonValue(body, "identifier");
            if (identifier == null) identifier = getJsonValue(body, "email");
            String password = getJsonValue(body, "password");

            AuthService.AuthResult res = authService.login(identifier, password);
            sendJsonResponse(exchange, res.isSuccess() ? 200 : 401, res.toJson());
        }
    }

    private class AuthForgotPasswordHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 204, "");
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
                return;
            }
            String body = readRequestBody(exchange);
            String email = getJsonValue(body, "email");

            String clientIp = getClientIp(exchange);
            AuthService.AuthResult res = authService.forgotPassword(email, clientIp);
            sendJsonResponse(exchange, 200, res.toJson());
        }
    }

    private class AuthResetPasswordHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 204, "");
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
                return;
            }
            String body = readRequestBody(exchange);
            String token = getJsonValue(body, "token");
            String newPassword = getJsonValue(body, "newPassword");
            String confirmPassword = getJsonValue(body, "confirmPassword");

            AuthService.AuthResult res = authService.resetPassword(token, newPassword, confirmPassword);
            sendJsonResponse(exchange, res.isSuccess() ? 200 : 400, res.toJson());
        }
    }

    private class AuthMeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 204, "");
                return;
            }
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            String token = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }

            User user = authService.validateToken(token);
            if (user != null) {
                sendJsonResponse(exchange, 200, "{\"success\":true,\"user\":" + user.toJson() + "}");
            } else {
                sendJsonResponse(exchange, 401, "{\"success\":false,\"message\":\"Unauthorized. Token invalid or expired.\"}");
            }
        }
    }

    private class AuthLogoutHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 204, "");
                return;
            }
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            String token = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
            authService.logout(token);
            sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Logged out successfully.\"}");
        }
    }

    private static String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int n;
            while ((n = is.read(buffer)) != -1) {
                baos.write(buffer, 0, n);
            }
            return baos.toString(StandardCharsets.UTF_8);
        }
    }

    private static String getJsonValue(String json, String key) {
        if (json == null || key == null) return null;
        String pattern = "\"" + key + "\":\"";
        int start = json.indexOf(pattern);
        if (start != -1) {
            start += pattern.length();
            int end = json.indexOf("\"", start);
            if (end != -1) {
                return json.substring(start, end).replace("\\\"", "\"").replace("\\\\", "\\");
            }
        }
        return null;
    }

    private static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) {
                path = "/index.html";
            }

            File file = new File("frontend/dist" + path);
            if (!file.exists()) {
                file = new File("../frontend" + path);
            }
            if (!file.exists()) {
                file = new File("frontend" + path);
            }

            if (!file.exists() || file.isDirectory()) {
                sendTextResponse(exchange, 404, "404 Not Found");
                return;
            }

            String contentType = getMimeType(path);
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, file.length());

            try (FileInputStream fis = new FileInputStream(file); OutputStream os = exchange.getResponseBody()) {
                byte[] buffer = new byte[8192];
                int count;
                while ((count = fis.read(buffer)) >= 0) {
                    os.write(buffer, 0, count);
                }
            }
        }

        private static String getMimeType(String path) {
            if (path.endsWith(".html")) return "text/html; charset=UTF-8";
            if (path.endsWith(".css")) return "text/css; charset=UTF-8";
            if (path.endsWith(".js")) return "application/javascript; charset=UTF-8";
            if (path.endsWith(".json")) return "application/json; charset=UTF-8";
            return "text/plain";
        }
    }

    private static String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
