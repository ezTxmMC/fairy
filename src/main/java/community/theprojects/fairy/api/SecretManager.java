package community.theprojects.fairy.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class SecretManager {

    private static final String SECRET_FILE = ".secret";
    private static final int TOKEN_LENGTH = 32;

    private String apiToken;
    private String tokenHash;

    public SecretManager() {
        loadOrGenerateSecret();
    }

    public String getApiToken() {
        return apiToken;
    }

    public boolean isValidToken(String providedToken) {
        if (providedToken == null || apiToken == null) {
            return false;
        }

        return MessageDigest.isEqual(
            hashToken(providedToken).getBytes(),
            tokenHash.getBytes()
        );
    }

    public String regenerateToken() {
        generateNewToken();
        saveSecretToFile();
        return apiToken;
    }

    private void loadOrGenerateSecret() {
        Path secretPath = Paths.get(SECRET_FILE);

        if (Files.exists(secretPath)) {
            try {
                String content = Files.readString(secretPath).trim();
                if (isValidTokenFormat(content)) {
                    this.apiToken = content;
                    this.tokenHash = hashToken(content);
                    return;
                }
            } catch (IOException e) {
                System.err.println("Error reading secret file: " + e.getMessage());
            }
        }

        generateNewToken();
        saveSecretToFile();
    }

    private void generateNewToken() {
        SecureRandom random = new SecureRandom();
        byte[] tokenBytes = new byte[TOKEN_LENGTH];
        random.nextBytes(tokenBytes);

        this.apiToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        this.tokenHash = hashToken(this.apiToken);
    }

    private void saveSecretToFile() {
        try {
            Path secretPath = Paths.get(SECRET_FILE);

            String fileContent = """
                # Fairy API Secret Token
                # This file contains the API authentication token for the REST API
                # Keep this file secure and do not share it!
                # Generated: %s

                %s
                """.formatted(java.time.LocalDateTime.now().toString(), apiToken);

            Files.writeString(secretPath, fileContent);

            try {
                if (System.getProperty("os.name").toLowerCase().contains("nix") || 
                    System.getProperty("os.name").toLowerCase().contains("nux") ||
                    System.getProperty("os.name").toLowerCase().contains("mac")) {

                    Runtime.getRuntime().exec("chmod 600 " + SECRET_FILE);
                }
            } catch (IOException e) {
                System.err.println("Warning: Could not set file permissions for .secret file");
            }

        } catch (IOException e) {
            System.err.println("Error saving secret file: " + e.getMessage());
        }
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    private boolean isValidTokenFormat(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        String cleanToken = token.trim();

        try {
            Base64.getUrlDecoder().decode(cleanToken + "==");
            return cleanToken.length() >= 20;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public boolean secretFileExists() {
        return Files.exists(Paths.get(SECRET_FILE));
    }

    public void deleteSecretFile() throws IOException {
        Path secretPath = Paths.get(SECRET_FILE);
        if (Files.exists(secretPath)) {
            Files.delete(secretPath);
        }
    }

    public String getTokenInfo() {
        return String.format("""
            API Token Information:
            - Token Length: %d characters
            - Hash Algorithm: SHA-256
            - File Location: %s
            - File Exists: %s
            - Generated: %s
            """, 
            apiToken != null ? apiToken.length() : 0,
            Paths.get(SECRET_FILE).toAbsolutePath(),
            secretFileExists(),
            secretFileExists() ? "From file" : "Runtime generated"
        );
    }
}
