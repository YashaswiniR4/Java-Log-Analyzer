import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Robust Database Manager connecting to Supabase PostgreSQL via JDBC (`SUPABASE_DB_URL`)
 * with fallback support for Supabase REST API (`SUPABASE_URL`/`SUPABASE_KEY`).
 */
public class DatabaseManager {

    private String supabaseDbUrl;
    private String supabaseUrl;
    private String supabaseKey;
    private boolean jdbcConfigured = false;
    private boolean restConfigured = false;
    private final HttpClient httpClient;

    public DatabaseManager() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        loadConfiguration();
    }

    public void loadConfiguration() {
        // Fetch environment variables via DotEnvLoader or System env
        this.supabaseDbUrl = DotEnvLoader.get("SUPABASE_DB_URL", "");
        this.supabaseUrl = DotEnvLoader.get("SUPABASE_URL", "https://cqdyvlyqovcaigbvpxbt.supabase.co");
        this.supabaseKey = DotEnvLoader.get("SUPABASE_KEY", "");

        // Check JDBC driver loading
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("[WARNING] PostgreSQL JDBC Driver (org.postgresql.Driver) not found in classpath.");
        }

        // Validate JDBC configuration
        if (supabaseDbUrl != null && !supabaseDbUrl.isBlank()
                && !supabaseDbUrl.contains("YOUR_PASSWORD")
                && supabaseDbUrl.startsWith("jdbc:postgresql://")) {
            this.jdbcConfigured = true;
        } else {
            this.jdbcConfigured = false;
        }

        // Validate REST API configuration
        if (supabaseUrl != null && !supabaseUrl.isBlank()
                && supabaseKey != null && !supabaseKey.isBlank()
                && !supabaseKey.contains("YOUR_SUPABASE_ANON_KEY")) {
            this.restConfigured = true;
        } else {
            this.restConfigured = false;
        }
    }

    public boolean isConnected() {
        if (jdbcConfigured) {
            return testJdbcConnection();
        } else if (restConfigured) {
            return testRestConnection();
        }
        return false;
    }

    public String getConnectionStatusMessage() {
        if (jdbcConfigured) {
            return "Connected to Supabase PostgreSQL via JDBC SSL";
        } else if (restConfigured) {
            return "Connected to Supabase via REST API";
        } else {
            return "Local Mode (Supabase SUPABASE_DB_URL password not set in .env)";
        }
    }

    /**
     * Tests JDBC Direct Connection to Supabase PostgreSQL.
     */
    public boolean testJdbcConnection() {
        if (!jdbcConfigured) return false;
        try (Connection conn = DriverManager.getConnection(supabaseDbUrl)) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.out.println("[ERROR] Supabase JDBC Connection Error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Tests REST API Connection to Supabase.
     */
    public boolean testRestConnection() {
        if (!restConfigured) return false;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(supabaseUrl + "/rest/v1/logs?select=count"))
                    .header("apikey", supabaseKey)
                    .header("Authorization", "Bearer " + supabaseKey)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() >= 200 && response.statusCode() < 300;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Saves parsed log entries to Supabase 'public.logs' table via JDBC (or REST fallback).
     */
    public boolean saveLogs(List<LogEntry> logs) {
        if (logs == null || logs.isEmpty()) return true;

        if (jdbcConfigured) {
            String sql = "INSERT INTO public.logs (timestamp, level, message, raw_line_number) VALUES (?, ?, ?, ?)";
            try (Connection conn = DriverManager.getConnection(supabaseDbUrl);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                conn.setAutoCommit(false);
                for (LogEntry log : logs) {
                    pstmt.setTimestamp(1, Timestamp.valueOf(log.getTimestamp()));
                    pstmt.setString(2, log.getLevel().name());
                    pstmt.setString(3, log.getMessage());
                    pstmt.setInt(4, log.getRawLineNumber());
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
                conn.commit();
                System.out.println("[SUCCESS] Saved " + logs.size() + " log entries to Supabase PostgreSQL via JDBC!");
                return true;
            } catch (SQLException e) {
                System.out.println("[ERROR] JDBC Log Batch Insert Failed: " + e.getMessage());
            }
        }

        if (restConfigured) {
            return syncLogsRest(logs);
        }

        System.out.println("[INFO] Supabase DB credentials not configured in .env. Logs retained in memory.");
        return false;
    }

    /**
     * Retrieves stored log records from Supabase 'public.logs' table via JDBC.
     */
    public List<LogEntry> getLogsFromDb(String levelFilter, String searchKeyword) {
        List<LogEntry> list = new ArrayList<>();
        if (!jdbcConfigured) return list;

        StringBuilder sql = new StringBuilder("SELECT timestamp, level, message, raw_line_number FROM public.logs WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (levelFilter != null && !levelFilter.isBlank() && !"ALL".equalsIgnoreCase(levelFilter)) {
            sql.append(" AND level = ?");
            params.add(levelFilter.trim().toUpperCase());
        }

        if (searchKeyword != null && !searchKeyword.isBlank()) {
            sql.append(" AND LOWER(message) LIKE ?");
            params.add("%" + searchKeyword.trim().toLowerCase() + "%");
        }

        sql.append(" ORDER BY timestamp DESC LIMIT 500");

        try (Connection conn = DriverManager.getConnection(supabaseDbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("timestamp");
                    String lvl = rs.getString("level");
                    String msg = rs.getString("message");
                    int lineNum = rs.getInt("raw_line_number");

                    LocalDateTime ldt = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
                    LogLevel logLevel = LogLevel.parseLevel(lvl);
                    if (logLevel != null) {
                        list.add(new LogEntry(ldt, logLevel, msg, lineNum));
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("[ERROR] Querying logs from Supabase failed: " + e.getMessage());
        }

        return list;
    }

    /**
     * Saves alerts to Supabase 'public.alerts' table via JDBC.
     */
    public boolean saveAlerts(List<Alert> alerts) {
        if (alerts == null || alerts.isEmpty()) return true;

        if (jdbcConfigured) {
            String sql = "INSERT INTO public.alerts (rule_type, message, timestamp) VALUES (?, ?, ?)";
            try (Connection conn = DriverManager.getConnection(supabaseDbUrl);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                conn.setAutoCommit(false);
                for (Alert alert : alerts) {
                    pstmt.setString(1, alert.getRuleType());
                    pstmt.setString(2, alert.getMessage());
                    pstmt.setTimestamp(3, Timestamp.valueOf(alert.getTimestamp()));
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
                conn.commit();
                System.out.println("[SUCCESS] Saved " + alerts.size() + " alerts to Supabase PostgreSQL via JDBC!");
                return true;
            } catch (SQLException e) {
                System.out.println("[ERROR] JDBC Alert Batch Insert Failed: " + e.getMessage());
            }
        }
        return false;
    }

    /**
     * Saves analysis report to Supabase 'public.summary_reports' table via JDBC.
     */
    public boolean saveReport(String reportText, int totalLogs, int infoCount, int warningCount, int errorCount, int skippedLines) {
        if (jdbcConfigured) {
            String sql = "INSERT INTO public.summary_reports (generated_at, total_logs, info_count, warning_count, error_count, skipped_lines, report_text) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DriverManager.getConnection(supabaseDbUrl);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.setInt(2, totalLogs);
                pstmt.setInt(3, infoCount);
                pstmt.setInt(4, warningCount);
                pstmt.setInt(5, errorCount);
                pstmt.setInt(6, skippedLines);
                pstmt.setString(7, reportText);
                pstmt.executeUpdate();
                System.out.println("[SUCCESS] Saved summary report to Supabase PostgreSQL via JDBC!");
                return true;
            } catch (SQLException e) {
                System.out.println("[ERROR] JDBC Report Insert Failed: " + e.getMessage());
            }
        }
        return false;
    }

    private boolean syncLogsRest(List<LogEntry> logs) {
        try {
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < logs.size(); i++) {
                LogEntry log = logs.get(i);
                json.append(String.format(
                        "{\"timestamp\":\"%s\",\"level\":\"%s\",\"message\":\"%s\",\"raw_line_number\":%d}",
                        log.getFormattedTimestamp(),
                        log.getLevel().name(),
                        escapeJson(log.getMessage()),
                        log.getRawLineNumber()
                ));
                if (i < logs.size() - 1) json.append(",");
            }
            json.append("]");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(supabaseUrl + "/rest/v1/logs"))
                    .header("apikey", supabaseKey)
                    .header("Authorization", "Bearer " + supabaseKey)
                    .header("Content-Type", "application/json")
                    .header("Prefer", "return=minimal")
                    .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 201 || response.statusCode() == 200;
        } catch (Exception e) {
            return false;
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

    public boolean isJdbcConfigured() { return jdbcConfigured; }
    public boolean isRestConfigured() { return restConfigured; }
    public String getSupabaseDbUrl() { return supabaseDbUrl; }

    // ================================================================================
    //                     USER MANAGEMENT & AUTHENTICATION JDBC
    // ================================================================================

    public void initUserTables() {
        if (!jdbcConfigured) return;
        String sqlUsers = "CREATE TABLE IF NOT EXISTS public.users (" +
                "id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "email VARCHAR(100) UNIQUE NOT NULL, " +
                "username VARCHAR(50) UNIQUE NOT NULL, " +
                "phone VARCHAR(20), " +
                "password_hash VARCHAR(255) NOT NULL, " +
                "role VARCHAR(20) DEFAULT 'USER', " +
                "is_verified BOOLEAN DEFAULT FALSE, " +
                "is_active BOOLEAN DEFAULT TRUE, " +
                "otp_code VARCHAR(10), " +
                "created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP" +
                ")";

        String sqlResetTokens = "CREATE TABLE IF NOT EXISTS public.password_reset_tokens (" +
                "id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, " +
                "user_id BIGINT NOT NULL, " +
                "token_hash VARCHAR(255) NOT NULL, " +
                "expires_at TIMESTAMP WITHOUT TIME ZONE NOT NULL, " +
                "used BOOLEAN DEFAULT FALSE, " +
                "created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP" +
                ")";

        try (Connection conn = DriverManager.getConnection(supabaseDbUrl);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sqlUsers);
            stmt.execute(sqlResetTokens);
            System.out.println("[SUCCESS] Initialized Supabase Auth Tables (public.users, public.password_reset_tokens)");
        } catch (SQLException e) {
            System.out.println("[INFO] Auth Tables Initialization check: " + e.getMessage());
        }
    }

    public boolean saveUser(User user) {
        if (!jdbcConfigured || user == null) return false;
        String sql = "INSERT INTO public.users (name, email, username, phone, password_hash, role, is_verified, is_active, otp_code) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
        try (Connection conn = DriverManager.getConnection(supabaseDbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getUsername());
            pstmt.setString(4, user.getPhone());
            pstmt.setString(5, user.getPasswordHash());
            pstmt.setString(6, user.getRole());
            pstmt.setBoolean(7, user.isVerified());
            pstmt.setBoolean(8, user.isActive());
            pstmt.setString(9, user.getOtpCode());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    user.setId(rs.getLong("id"));
                    return true;
                }
            }
        } catch (SQLException e) {
            System.out.println("[ERROR] JDBC Save User Failed: " + e.getMessage());
        }
        return false;
    }

    public User findUserByEmail(String email) {
        if (!jdbcConfigured || email == null) return null;
        String sql = "SELECT id, name, email, username, phone, password_hash, role, is_verified, is_active, otp_code, created_at FROM public.users WHERE LOWER(email) = ?";
        try (Connection conn = DriverManager.getConnection(supabaseDbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapUserRow(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("[ERROR] JDBC Find User By Email Failed: " + e.getMessage());
        }
        return null;
    }

    public User findUserByUsername(String username) {
        if (!jdbcConfigured || username == null) return null;
        String sql = "SELECT id, name, email, username, phone, password_hash, role, is_verified, is_active, otp_code, created_at FROM public.users WHERE LOWER(username) = ?";
        try (Connection conn = DriverManager.getConnection(supabaseDbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username.trim().toLowerCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapUserRow(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("[ERROR] JDBC Find User By Username Failed: " + e.getMessage());
        }
        return null;
    }

    public User findUserById(long id) {
        if (!jdbcConfigured) return null;
        String sql = "SELECT id, name, email, username, phone, password_hash, role, is_verified, is_active, otp_code, created_at FROM public.users WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(supabaseDbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapUserRow(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("[ERROR] JDBC Find User By ID Failed: " + e.getMessage());
        }
        return null;
    }

    public boolean updateUserVerification(long userId, boolean verified) {
        if (!jdbcConfigured) return false;
        String sql = "UPDATE public.users SET is_verified = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(supabaseDbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, verified);
            pstmt.setLong(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("[ERROR] JDBC Update Verification Failed: " + e.getMessage());
        }
        return false;
    }

    public boolean savePasswordResetToken(long userId, String tokenHash, LocalDateTime expiresAt) {
        if (!jdbcConfigured) return false;
        String sql = "INSERT INTO public.password_reset_tokens (user_id, token_hash, expires_at) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(supabaseDbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            pstmt.setString(2, tokenHash);
            pstmt.setTimestamp(3, Timestamp.valueOf(expiresAt));
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("[ERROR] JDBC Save Password Reset Token Failed: " + e.getMessage());
        }
        return false;
    }

    public boolean invalidateResetToken(String tokenHash) {
        if (!jdbcConfigured) return false;
        String sql = "UPDATE public.password_reset_tokens SET used = true WHERE token_hash = ?";
        try (Connection conn = DriverManager.getConnection(supabaseDbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, tokenHash);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("[ERROR] JDBC Invalidate Reset Token Failed: " + e.getMessage());
        }
        return false;
    }

    public boolean updateUserPassword(long userId, String newPasswordHash) {
        if (!jdbcConfigured) return false;
        String sql = "UPDATE public.users SET password_hash = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(supabaseDbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newPasswordHash);
            pstmt.setLong(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("[ERROR] JDBC Update User Password Failed: " + e.getMessage());
        }
        return false;
    }

    private User mapUserRow(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("created_at");
        LocalDateTime createdAt = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
        User u = new User(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("username"),
                rs.getString("phone"),
                rs.getString("password_hash"),
                rs.getString("role"),
                rs.getBoolean("is_verified"),
                rs.getBoolean("is_active"),
                createdAt
        );
        u.setOtpCode(rs.getString("otp_code"));
        return u;
    }
}
