import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class AuthServiceTest {

    private AuthService authService;

    @Before
    public void setUp() {
        DatabaseManager dbManager = new DatabaseManager();
        authService = new AuthService(dbManager);
    }

    @Test
    public void testUserRegistrationSuccess() {
        String email = "testuser_" + System.currentTimeMillis() + "@company.com";
        String username = "user_" + System.currentTimeMillis();

        AuthService.AuthResult res = authService.register(
                "Yashaswini R",
                email,
                username,
                "+1-555-0199",
                "SecurePass@123",
                "SecurePass@123"
        );

        assertTrue("Registration should succeed", res.isSuccess());
        assertNotNull("User object should be returned", res.getUser());
        assertEquals("Name should match", "Yashaswini R", res.getUser().getName());
        assertEquals("Email should match", email.toLowerCase(), res.getUser().getEmail());

        // Verify BCrypt hash
        String passwordHash = res.getUser().getPasswordHash();
        assertNotNull("Password hash must not be null", passwordHash);
        assertTrue("Password hash must start with $2a$", passwordHash.startsWith("$2a$"));
        assertNotEquals("Plain text password must NEVER be stored", "SecurePass@123", passwordHash);

        // Verify OTP is generated
        assertNotNull("OTP code should be generated", res.getOtpCode());
    }

    @Test
    public void testDuplicateEmailRegistrationFails() {
        String email = "dup_" + System.currentTimeMillis() + "@company.com";
        String username1 = "u1_" + System.currentTimeMillis();
        String username2 = "u2_" + System.currentTimeMillis();

        AuthService.AuthResult res1 = authService.register("User One", email, username1, "", "Pass123!", "Pass123!");
        assertTrue("First registration should succeed", res1.isSuccess());

        AuthService.AuthResult res2 = authService.register("User Two", email, username2, "", "Pass123!", "Pass123!");
        assertFalse("Duplicate email registration must fail", res2.isSuccess());
        assertTrue("Error message should mention email exists", res2.getMessage().toLowerCase().contains("already exists"));
    }

    @Test
    public void testDuplicateUsernameRegistrationFails() {
        String username = "uniqueuser_" + System.currentTimeMillis();
        String email1 = "e1_" + System.currentTimeMillis() + "@company.com";
        String email2 = "e2_" + System.currentTimeMillis() + "@company.com";

        authService.register("User One", email1, username, "", "Pass123!", "Pass123!");
        AuthService.AuthResult res2 = authService.register("User Two", email2, username, "", "Pass123!", "Pass123!");

        assertFalse("Duplicate username registration must fail", res2.isSuccess());
        assertTrue("Error message should mention username taken", res2.getMessage().toLowerCase().contains("taken"));
    }

    @Test
    public void testLoginWithBCryptVerification() {
        String email = "login_" + System.currentTimeMillis() + "@company.com";
        String username = "loginu_" + System.currentTimeMillis();
        String rawPassword = "MySecretPassword#2026";

        AuthService.AuthResult regRes = authService.register("Login User", email, username, "", rawPassword, rawPassword);
        assertTrue(regRes.isSuccess());

        // Verify OTP first
        authService.verifyOtp(email, regRes.getOtpCode());

        // Login with correct credentials
        AuthService.AuthResult loginRes = authService.login(email, rawPassword);
        assertTrue("Login with correct credentials must succeed", loginRes.isSuccess());
        assertNotNull("JWT Auth Token must be generated", loginRes.getToken());
        assertTrue("Token should start with jwt_sentinel_", loginRes.getToken().startsWith("jwt_sentinel_"));

        // Login with incorrect password
        AuthService.AuthResult badLoginRes = authService.login(email, "WrongPassword");
        assertFalse("Login with wrong password must fail", badLoginRes.isSuccess());
        assertEquals("Invalid email/username or password", badLoginRes.getMessage());
    }

    @Test
    public void testForgotPasswordSecurityRule() {
        String email = "exists_" + System.currentTimeMillis() + "@company.com";
        authService.register("Exist User", email, "existu_" + System.currentTimeMillis(), "", "Pass123!", "Pass123!");

        // Request reset for existing email
        AuthService.AuthResult res1 = authService.forgotPassword(email);
        assertTrue(res1.isSuccess());
        assertEquals("If an account exists for this email, you will receive a password reset link.", res1.getMessage());

        // Request reset for NON-existing email
        AuthService.AuthResult res2 = authService.forgotPassword("nonexistent_" + System.currentTimeMillis() + "@company.com");
        assertTrue("Should return success to prevent email enumeration", res2.isSuccess());
        assertEquals("If an account exists for this email, you will receive a password reset link.", res2.getMessage());
    }

    @Test
    public void testResetPasswordFlow() {
        String email = "reset_" + System.currentTimeMillis() + "@company.com";
        String username = "resetu_" + System.currentTimeMillis();

        authService.register("Reset User", email, username, "", "OldPass123", "OldPass123");
        AuthService.AuthResult forgotRes = authService.forgotPassword(email);
        String resetToken = forgotRes.getResetToken();
        assertNotNull("Reset token should be returned in test mode", resetToken);

        // Reset password
        String newPassword = "NewSecretPassword#2026";
        AuthService.AuthResult resetRes = authService.resetPassword(resetToken, newPassword, newPassword);
        assertTrue("Reset password should succeed", resetRes.isSuccess());

        // Login with old password should fail
        AuthService.AuthResult oldLogin = authService.login(email, "OldPass123");
        assertFalse("Login with old password must fail", oldLogin.isSuccess());

        // Login with new password should succeed
        AuthService.AuthResult newLogin = authService.login(email, newPassword);
        assertTrue("Login with new password must succeed", newLogin.isSuccess());
    }
}
