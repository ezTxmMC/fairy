package community.theprojects.fairy.config;

import community.theprojects.fairy.minecraft.MinecraftServerType;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ServerConfiguration {

    private String serverId;
    private MinecraftServerType serverType;
    private String workingDirectory;
    private String scriptFile;
    private boolean autoStart;
    private boolean autoRestart;
    private String minMemory;
    private String maxMemory;
    private LocalDateTime lastStart;
    private LocalDateTime createdAt;
    private boolean enabled;

    public ServerConfiguration() {
        this.createdAt = LocalDateTime.now();
        this.enabled = true;
        this.autoStart = true;
        this.autoRestart = true;
        this.minMemory = "1G";
        this.maxMemory = "2G";
    }

    public ServerConfiguration(String serverId, MinecraftServerType serverType, String workingDirectory, String scriptFile) {
        this();
        this.serverId = serverId;
        this.serverType = serverType;
        this.workingDirectory = workingDirectory;
        this.scriptFile = scriptFile;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("serverId", serverId);
        json.put("serverType", serverType.getName());
        json.put("workingDirectory", workingDirectory);
        json.put("scriptFile", scriptFile);
        json.put("autoStart", autoStart);
        json.put("autoRestart", autoRestart);
        json.put("minMemory", minMemory);
        json.put("maxMemory", maxMemory);
        json.put("enabled", enabled);
        json.put("createdAt", createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        if (lastStart != null) {
            json.put("lastStart", lastStart.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

        return json;
    }

    public static ServerConfiguration fromJson(JSONObject json) {
        ServerConfiguration config = new ServerConfiguration();

        config.serverId = json.getString("serverId");
        config.serverType = MinecraftServerType.fromString(json.getString("serverType"));
        config.workingDirectory = json.getString("workingDirectory");
        config.scriptFile = json.optString("scriptFile", null);
        config.autoStart = json.optBoolean("autoStart", true);
        config.autoRestart = json.optBoolean("autoRestart", true);
        config.minMemory = json.optString("minMemory", "1G");
        config.maxMemory = json.optString("maxMemory", "2G");
        config.enabled = json.optBoolean("enabled", true);

        String createdAtStr = json.optString("createdAt");
        if (!createdAtStr.isEmpty()) {
            config.createdAt = LocalDateTime.parse(createdAtStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }

        String lastStartStr = json.optString("lastStart");
        if (!lastStartStr.isEmpty()) {
            config.lastStart = LocalDateTime.parse(lastStartStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }

        return config;
    }

    public void updateLastStart() {
        this.lastStart = LocalDateTime.now();
    }

    // Getters and setters
    public String getServerId() { return serverId; }
    public void setServerId(String serverId) { this.serverId = serverId; }

    public MinecraftServerType getServerType() { return serverType; }
    public void setServerType(MinecraftServerType serverType) { this.serverType = serverType; }

    public String getWorkingDirectory() { return workingDirectory; }
    public void setWorkingDirectory(String workingDirectory) { this.workingDirectory = workingDirectory; }

    public String getScriptFile() { return scriptFile; }
    public void setScriptFile(String scriptFile) { this.scriptFile = scriptFile; }

    public boolean isAutoStart() { return autoStart; }
    public void setAutoStart(boolean autoStart) { this.autoStart = autoStart; }

    public boolean isAutoRestart() { return autoRestart; }
    public void setAutoRestart(boolean autoRestart) { this.autoRestart = autoRestart; }

    public String getMinMemory() { return minMemory; }
    public void setMinMemory(String minMemory) { this.minMemory = minMemory; }

    public String getMaxMemory() { return maxMemory; }
    public void setMaxMemory(String maxMemory) { this.maxMemory = maxMemory; }

    public LocalDateTime getLastStart() { return lastStart; }
    public void setLastStart(LocalDateTime lastStart) { this.lastStart = lastStart; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    @Override
    public String toString() {
        return "ServerConfig{" +
                "id='" + serverId + '\'' +
                ", type=" + serverType.getName() +
                ", autoStart=" + autoStart +
                ", autoRestart=" + autoRestart +
                ", enabled=" + enabled +
                '}';
    }
}
