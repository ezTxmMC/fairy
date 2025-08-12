package community.theprojects.fairy.config;

import community.theprojects.fairy.console.HexColor;
import community.theprojects.fairy.console.Printer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerConfigManager {

    private static final String CONFIG_FILE = "servers.json";
    private static final String BACKUP_SUFFIX = ".backup";

    private final Map<String, ServerConfiguration> configurations = new ConcurrentHashMap<>();
    private final Printer printer;
    private final Path configPath;

    public ServerConfigManager(Printer printer) {
        this.printer = printer;
        this.configPath = Paths.get(CONFIG_FILE);
        loadConfigurations();
    }

    public void addServerConfiguration(ServerConfiguration config) {
        configurations.put(config.getServerId(), config);
        saveConfigurations();

        printer.println(HexColor.colorText("Server configuration saved: " + config.getServerId(), 
                       HexColor.Colors.GREEN), true);
    }

    public void removeServerConfiguration(String serverId) {
        ServerConfiguration removed = configurations.remove(serverId);
        if (removed != null) {
            saveConfigurations();
            printer.println(HexColor.colorText("Server configuration removed: " + serverId, 
                           HexColor.Colors.ORANGE), true);
        }
    }

    public ServerConfiguration getServerConfiguration(String serverId) {
        return configurations.get(serverId);
    }

    public List<ServerConfiguration> getAllConfigurations() {
        return new ArrayList<>(configurations.values());
    }

    public List<ServerConfiguration> getAutoStartConfigurations() {
        return configurations.values().stream()
                .filter(config -> config.isEnabled() && config.isAutoStart())
                .toList();
    }

    public List<ServerConfiguration> getAutoRestartConfigurations() {
        return configurations.values().stream()
                .filter(config -> config.isEnabled() && config.isAutoRestart())
                .toList();
    }

    public void updateServerConfiguration(ServerConfiguration config) {
        configurations.put(config.getServerId(), config);
        saveConfigurations();
    }

    public void enableAutoStart(String serverId, boolean enable) {
        ServerConfiguration config = configurations.get(serverId);
        if (config != null) {
            config.setAutoStart(enable);
            saveConfigurations();

            String status = enable ? "enabled" : "disabled";
            printer.println(HexColor.colorText("Auto-start " + status + " for: " + serverId, 
                           HexColor.Colors.CYAN), true);
        }
    }

    public void enableAutoRestart(String serverId, boolean enable) {
        ServerConfiguration config = configurations.get(serverId);
        if (config != null) {
            config.setAutoRestart(enable);
            saveConfigurations();

            String status = enable ? "enabled" : "disabled";
            printer.println(HexColor.colorText("Auto-restart " + status + " for: " + serverId, 
                           HexColor.Colors.CYAN), true);
        }
    }

    private void loadConfigurations() {
        if (!Files.exists(configPath)) {
            printer.println(HexColor.colorText("No server configuration file found, starting fresh", 
                           HexColor.Colors.GRAY), true);
            return;
        }

        try {
            String content = Files.readString(configPath);
            JSONObject root = new JSONObject(content);
            JSONArray servers = root.optJSONArray("servers");

            if (servers != null) {
                for (int i = 0; i < servers.length(); i++) {
                    JSONObject serverJson = servers.getJSONObject(i);
                    ServerConfiguration config = ServerConfiguration.fromJson(serverJson);
                    configurations.put(config.getServerId(), config);
                }
            }

            printer.println(HexColor.colorText("Loaded " + configurations.size() + " server configurations", 
                           HexColor.Colors.GREEN), true);

        } catch (IOException e) {
            printer.println(HexColor.colorText("Failed to load server configurations: " + e.getMessage(), 
                           HexColor.Colors.RED), true);
        } catch (Exception e) {
            printer.println(HexColor.colorText("Invalid configuration file format: " + e.getMessage(), 
                           HexColor.Colors.RED), true);
            backupCorruptedConfig();
        }
    }

    private void saveConfigurations() {
        try {
            // Create backup before saving
            if (Files.exists(configPath)) {
                Path backupPath = Paths.get(CONFIG_FILE + BACKUP_SUFFIX);
                Files.copy(configPath, backupPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }

            JSONObject root = new JSONObject();
            JSONArray servers = new JSONArray();

            for (ServerConfiguration config : configurations.values()) {
                servers.put(config.toJson());
            }

            root.put("servers", servers);
            root.put("version", "1.0");
            root.put("lastUpdated", java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            Files.writeString(configPath, root.toString(2), 
                            StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);

        } catch (IOException e) {
            printer.println(HexColor.colorText("Failed to save server configurations: " + e.getMessage(), 
                           HexColor.Colors.RED), true);
        }
    }

    private void backupCorruptedConfig() {
        try {
            Path corruptedPath = Paths.get(CONFIG_FILE + ".corrupted." + System.currentTimeMillis());
            Files.move(configPath, corruptedPath);
            printer.println(HexColor.colorText("Corrupted config backed up to: " + corruptedPath, 
                           HexColor.Colors.YELLOW), true);
        } catch (IOException e) {
            printer.println(HexColor.colorText("Failed to backup corrupted config: " + e.getMessage(), 
                           HexColor.Colors.RED), true);
        }
    }

    public void exportConfigurations(String filename) {
        try {
            JSONObject root = new JSONObject();
            JSONArray servers = new JSONArray();

            for (ServerConfiguration config : configurations.values()) {
                servers.put(config.toJson());
            }

            root.put("servers", servers);
            root.put("exportedAt", java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            root.put("version", "1.0");

            Path exportPath = Paths.get(filename);
            Files.writeString(exportPath, root.toString(2));

            printer.println(HexColor.colorText("Configurations exported to: " + filename, 
                           HexColor.Colors.GREEN), true);

        } catch (IOException e) {
            printer.println(HexColor.colorText("Failed to export configurations: " + e.getMessage(), 
                           HexColor.Colors.RED), true);
        }
    }

    public void importConfigurations(String filename) {
        try {
            Path importPath = Paths.get(filename);
            if (!Files.exists(importPath)) {
                printer.println(HexColor.colorText("Import file not found: " + filename, 
                               HexColor.Colors.RED), true);
                return;
            }

            String content = Files.readString(importPath);
            JSONObject root = new JSONObject(content);
            JSONArray servers = root.optJSONArray("servers");

            int importedCount = 0;
            if (servers != null) {
                for (int i = 0; i < servers.length(); i++) {
                    JSONObject serverJson = servers.getJSONObject(i);
                    ServerConfiguration config = ServerConfiguration.fromJson(serverJson);
                    configurations.put(config.getServerId(), config);
                    importedCount++;
                }
            }

            saveConfigurations();
            printer.println(HexColor.colorText("Imported " + importedCount + " server configurations", 
                           HexColor.Colors.GREEN), true);

        } catch (IOException e) {
            printer.println(HexColor.colorText("Failed to import configurations: " + e.getMessage(), 
                           HexColor.Colors.RED), true);
        } catch (Exception e) {
            printer.println(HexColor.colorText("Invalid import file format: " + e.getMessage(), 
                           HexColor.Colors.RED), true);
        }
    }

    public void listConfigurations() {
        if (configurations.isEmpty()) {
            printer.println(HexColor.colorText("No server configurations found", HexColor.Colors.GRAY), true);
            return;
        }

        printer.println(HexColor.colorText("Server Configurations:", HexColor.Colors.CYAN), true);
        for (ServerConfiguration config : configurations.values()) {
            String autoFlags = "";
            if (config.isAutoStart()) autoFlags += " [AUTO-START]";
            if (config.isAutoRestart()) autoFlags += " [AUTO-RESTART]";
            if (!config.isEnabled()) autoFlags += " [DISABLED]";

            printer.println(HexColor.colorText("  " + config.getServerId() + " (" + config.getServerType().getName() + ")" + autoFlags, 
                           config.isEnabled() ? HexColor.Colors.WHITE : HexColor.Colors.GRAY), true);
            printer.println(HexColor.colorText("    Directory: " + config.getWorkingDirectory(), HexColor.Colors.GRAY), true);
        }
    }
}
