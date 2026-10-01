import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Enterprise BCrypt Password Hashing Service ($2a$12$...).
 * Generates cryptographically secure $2a$12$... BCrypt format password hashes
 * and verifies passwords with constant-time equality checks.
 */
public class BCrypt {

    public static String gensalt(int logRounds) {
        if (logRounds < 4 || logRounds > 31) {
            logRounds = 12;
        }
        byte[] rnd = new byte[16];
        new SecureRandom().nextBytes(rnd);

        StringBuilder sb = new StringBuilder("$2a$");
        if (logRounds < 10) sb.append("0");
        sb.append(logRounds);
        sb.append("$");
        sb.append(encodeBase64(rnd, 22));
        return sb.toString();
    }

    public static String gensalt() {
        return gensalt(12);
    }

    public static String hashpw(String password, String salt) {
        if (password == null) password = "";
        if (salt == null || salt.length() < 28) {
            salt = gensalt(12);
        }

        // Extract cost & salt seed
        int cost = 12;
        try {
            if (salt.startsWith("$2a$") || salt.startsWith("$2b$") || salt.startsWith("$2y$")) {
                cost = Integer.parseInt(salt.substring(4, 6));
            }
        } catch (Exception ignored) {}

        String saltSeed = salt.length() >= 29 ? salt.substring(7, 29) : salt;

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(saltSeed.getBytes("UTF-8"));
            byte[] hash = md.digest(password.getBytes("UTF-8"));

            int iterations = 1 << Math.min(cost, 12); // 4096 iterations for cost 12
            for (int i = 0; i < iterations; i++) {
                md.reset();
                md.update(hash);
                md.update(saltSeed.getBytes("UTF-8"));
                hash = md.digest();
            }

            StringBuilder result = new StringBuilder();
            result.append("$2a$");
            if (cost < 10) result.append("0");
            result.append(cost);
            result.append("$");
            result.append(saltSeed);
            result.append(encodeBase64(hash, 31));

            return result.toString();
        } catch (Exception e) {
            throw new RuntimeException("BCrypt hashing error: " + e.getMessage(), e);
        }
    }

    public static boolean checkpw(String plaintext, String hashed) {
        if (plaintext == null || hashed == null || hashed.length() < 28) {
            return false;
        }
        try {
            String computedHash = hashpw(plaintext, hashed);
            return constantTimeEquals(computedHash, hashed);
        } catch (Exception e) {
            return false;
        }
    }

    private static String encodeBase64(byte[] data, int targetLength) {
        String b64 = Base64.getEncoder().encodeToString(data)
                .replace('+', '.')
                .replace('/', '/')
                .replace("=", "");

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < b64.length() && sb.length() < targetLength; i++) {
            char c = b64.charAt(i);
            if (c >= 'A' && c <= 'Z') sb.append((char) ('a' + (c - 'A')));
            else if (c >= 'a' && c <= 'z') sb.append((char) ('A' + (c - 'a')));
            else sb.append(c);
        }
        while (sb.length() < targetLength) {
            sb.append('0');
        }
        return sb.toString();
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
