import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * Enterprise Lightweight Pure Java SMTP Email Service supporting real Gmail delivery
 * via SSL (smtp.gmail.com:465) with zero external dependencies.
 */
public class EmailService {

    private String smtpHost;
    private int smtpPort;
    private String smtpUser;
    private String smtpPass;
    private String smtpFrom;
    private boolean enabled;

    public EmailService() {
        loadConfiguration();
    }

    public void loadConfiguration() {
        this.smtpHost = DotEnvLoader.get("SMTP_HOST", "smtp.gmail.com");
        String portStr = DotEnvLoader.get("SMTP_PORT", "465");
        try {
            this.smtpPort = Integer.parseInt(portStr);
        } catch (Exception e) {
            this.smtpPort = 465;
        }

        this.smtpUser = DotEnvLoader.get("SMTP_USER", "");
        this.smtpPass = DotEnvLoader.get("SMTP_PASS", "");
        this.smtpFrom = DotEnvLoader.get("SMTP_FROM", smtpUser.isEmpty() ? "LogAnalyzer PRO <no-reply@loganalyzer.com>" : smtpUser);

        if (smtpUser != null && !smtpUser.isBlank() && !smtpUser.contains("YOUR_GMAIL")
                && smtpPass != null && !smtpPass.isBlank() && !smtpPass.contains("YOUR_GMAIL_APP_PASSWORD")) {
            this.enabled = true;
            System.out.println("[EMAIL] Real Gmail SMTP Service configured for " + smtpUser);
        } else {
            this.enabled = false;
            System.out.println("[INFO] Real Gmail SMTP credentials not set in .env (SMTP_USER/SMTP_PASS). OTPs displayed on screen & console.");
        }
    }

    /**
     * Send 6-Digit Email Verification OTP code via Gmail SMTP.
     */
    public boolean sendOtpEmail(String recipientEmail, String recipientName, String otpCode) {
        String subject = "🔑 Your LogAnalyzer PRO Verification Code: " + otpCode;
        String body = "Hello " + recipientName + ",\n\n"
                + "Thank you for registering with LogAnalyzer PRO.\n\n"
                + "Your 6-Digit Email Verification Code (OTP) is:\n\n"
                + "    " + otpCode + "\n\n"
                + "Enter this code on the activation screen to verify your account.\n"
                + "If you did not request this code, please ignore this email.\n\n"
                + "Regards,\n"
                + "LogAnalyzer PRO Security Team";

        return sendEmail(recipientEmail, subject, body);
    }

    /**
     * Send Password Reset Link via Gmail SMTP.
     */
    public boolean sendPasswordResetEmail(String recipientEmail, String resetToken) {
        String resetUrl = "http://localhost:3000/reset-password?token=" + resetToken;
        String subject = "🔒 Password Reset Request - LogAnalyzer PRO";
        String body = "Hello,\n\n"
                + "We received a request to reset your password for your LogAnalyzer PRO account.\n\n"
                + "Click the link below or copy your reset token to create a new password:\n\n"
                + "Reset Token: " + resetToken + "\n"
                + "Reset Link : " + resetUrl + "\n\n"
                + "This link will expire in 30 minutes. If you did not request a password reset, you can safely ignore this email.\n\n"
                + "Regards,\n"
                + "LogAnalyzer PRO Security Team";

        return sendEmail(recipientEmail, subject, body);
    }

    /**
     * Sends email via SSL Socket to smtp.gmail.com.
     */
    public boolean sendEmail(String toEmail, String subject, String bodyText) {
        if (!enabled) {
            System.out.println("[EMAIL DEMO] Would send email to " + toEmail + " | Subject: " + subject);
            return false;
        }

        System.out.println("[EMAIL] Connecting to " + smtpHost + ":" + smtpPort + " to send email to " + toEmail + "...");

        try (SSLSocket socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(smtpHost, smtpPort);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {

            socket.setSoTimeout(15000);

            readResponse(reader); // 220 Greeting
            sendCommand(writer, "EHLO " + smtpHost);
            readResponse(reader);

            // AUTH LOGIN
            sendCommand(writer, "AUTH LOGIN");
            readResponse(reader); // 334 Username prompt

            sendCommand(writer, Base64.getEncoder().encodeToString(smtpUser.getBytes(StandardCharsets.UTF_8)));
            readResponse(reader); // 334 Password prompt

            sendCommand(writer, Base64.getEncoder().encodeToString(smtpPass.getBytes(StandardCharsets.UTF_8)));
            String authResp = readResponse(reader); // 235 Authentication successful

            if (!authResp.startsWith("235")) {
                System.out.println("[ERROR] Gmail SMTP Authentication Failed: " + authResp);
                return false;
            }

            // MAIL FROM
            sendCommand(writer, "MAIL FROM:<" + smtpUser + ">");
            readResponse(reader);

            // RCPT TO
            sendCommand(writer, "RCPT TO:<" + toEmail + ">");
            readResponse(reader);

            // DATA
            sendCommand(writer, "DATA");
            readResponse(reader); // 354 Start mail input

            // Headers & Body
            writer.write("From: " + smtpFrom + "\r\n");
            writer.write("To: " + toEmail + "\r\n");
            writer.write("Subject: " + subject + "\r\n");
            writer.write("Content-Type: text/plain; charset=UTF-8\r\n");
            writer.write("\r\n");
            writer.write(bodyText + "\r\n");
            writer.write(".\r\n");
            writer.flush();

            String dataResp = readResponse(reader); // 250 OK

            sendCommand(writer, "QUIT");

            if (dataResp.startsWith("250")) {
                System.out.println("[SUCCESS] Email successfully sent to " + toEmail + " via Gmail SMTP!");
                return true;
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Email Sending Error: " + e.getMessage());
        }
        return false;
    }

    private void sendCommand(BufferedWriter writer, String command) throws Exception {
        writer.write(command + "\r\n");
        writer.flush();
    }

    private String readResponse(BufferedReader reader) throws Exception {
        String line = reader.readLine();
        if (line == null) return "";
        return line;
    }

    public boolean isEnabled() { return enabled; }
}
