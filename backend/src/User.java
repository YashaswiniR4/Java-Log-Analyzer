import java.time.LocalDateTime;

/**
 * Domain model representing a User in the company-level authentication system.
 */
public class User {
    private long id;
    private String name;
    private String email;
    private String username;
    private String phone;
    private String passwordHash;
    private String role;
    private boolean isVerified;
    private boolean isActive;
    private String otpCode;
    private LocalDateTime createdAt;

    public User() {
        this.role = "USER";
        this.isVerified = false;
        this.isActive = true;
    }

    public User(long id, String name, String email, String username, String phone, String passwordHash, String role, boolean isVerified, boolean isActive, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.username = username;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.role = role != null ? role : "USER";
        this.isVerified = isVerified;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String toJson() {
        StringBuilder json = new StringBuilder("{");
        json.append("\"id\":").append(id).append(",");
        json.append("\"name\":\"").append(escapeJson(name)).append("\",");
        json.append("\"email\":\"").append(escapeJson(email)).append("\",");
        json.append("\"username\":\"").append(escapeJson(username)).append("\",");
        json.append("\"phone\":\"").append(escapeJson(phone != null ? phone : "")).append("\",");
        json.append("\"role\":\"").append(escapeJson(role)).append("\",");
        json.append("\"isVerified\":").append(isVerified).append(",");
        json.append("\"isActive\":").append(isActive);
        if (createdAt != null) {
            json.append(",\"createdAt\":\"").append(createdAt.toString()).append("\"");
        }
        json.append("}");
        return json.toString();
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
