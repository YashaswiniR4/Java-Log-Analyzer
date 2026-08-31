import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Lightweight Core Java environment variable loader that reads .env files
 * without requiring external third-party dependencies.
 */
public class DotEnvLoader {

    private static final Map<String, String> ENV_MAP = new HashMap<>();
    private static boolean loaded = false;

    public static synchronized void loadEnv() {
        if (loaded) return;

        // Candidate paths for .env file
        String[] candidatePaths = {
            ".env",
            "../.env",
            "../../.env",
            System.getProperty("user.dir") + "/.env",
            System.getProperty("user.dir") + "/../.env"
        };

        for (String path : candidatePaths) {
            File envFile = new File(path);
            if (envFile.exists() && envFile.isFile()) {
                parseFile(envFile);
                break;
            }
        }
        loaded = true;
    }

    private static void parseFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int eqIdx = line.indexOf('=');
                if (eqIdx > 0) {
                    String key = line.substring(0, eqIdx).trim();
                    String val = line.substring(eqIdx + 1).trim();
                    // Strip quotes if present
                    if ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'"))) {
                        val = val.substring(1, val.length() - 1);
                    }
                    ENV_MAP.put(key, val);
                }
            }
        } catch (IOException e) {
            System.out.println("[WARNING] Error reading .env file: " + e.getMessage());
        }
    }

    public static String get(String key) {
        loadEnv();
        // Check OS system environment first
        String sysVal = System.getenv(key);
        if (sysVal != null && !sysVal.isBlank()) {
            return sysVal;
        }
        // Fallback to loaded .env file map
        return ENV_MAP.get(key);
    }

    public static String get(String key, String defaultValue) {
        String val = get(key);
        return (val != null && !val.isBlank()) ? val : defaultValue;
    }
}
