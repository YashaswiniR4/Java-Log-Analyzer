/**
 * Enum representing log levels in the application.
 * Provides standard levels: INFO, WARNING, and ERROR.
 */
public enum LogLevel {
    INFO,
    WARNING,
    ERROR;

    public static LogLevel parseLevel(String levelStr) {
        if (levelStr == null) {
            return null;
        }
        try {
            return LogLevel.valueOf(levelStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
