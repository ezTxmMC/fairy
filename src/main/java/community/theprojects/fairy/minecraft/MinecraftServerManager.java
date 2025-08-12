package community.theprojects.fairy.minecraft;

import community.theprojects.fairy.config.ServerConfigManager;
import community.theprojects.fairy.console.HexColor;
import community.theprojects.fairy.console.Printer;
import community.theprojects.fairy.process.InteractiveProcessManager;
import community.theprojects.fairy.process.JavaProcessRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MinecraftServerManager {

    private final InteractiveProcessManager processManager;
    private final JavaProcessRunner javaRunner;
    private final Printer printer;
    private final Map<String, MinecraftServer> servers = new ConcurrentHashMap<>();
    private final Map<String, PlayerStats> playerStats = new ConcurrentHashMap<>();
    private ServerConfigManager serverConfigManager;

    private static final Pattern PLAYER_JOIN_PATTERN = Pattern.compile("\\[.*\\] \\[Server thread/INFO\\]: (\\w+) joined the game");
    private static final Pattern PLAYER_LEAVE_PATTERN = Pattern.compile("\\[.*\\] \\[Server thread/INFO\\]: (\\w+) left the game");
    private static final Pattern PLAYER_CHAT_PATTERN = Pattern.compile("\\[.*\\] \\[Server thread/INFO\\]: <(\\w+)> (.*)");

    public MinecraftServerManager(InteractiveProcessManager processManager, JavaProcessRunner javaRunner, Printer printer) {
        this.processManager = processManager;
        this.javaRunner = javaRunner;
        this.printer = printer;
    }

    public boolean startServer(String serverId, MinecraftServerType serverType, String workingDirectory, String scriptFile, ServerConfig config) {
        if (servers.containsKey(serverId) && processManager.isProcessRunning(serverId)) {
            printer.println(HexColor.colorText("Server '" + serverId + "' is already running.", HexColor.Colors.YELLOW), true);
            return false;
        }

        // Remove from registration if it exists but is not running (for restart scenarios)
        if (servers.containsKey(serverId) && !processManager.isProcessRunning(serverId)) {
            servers.remove(serverId);
        }

        Path serverPath = Paths.get(workingDirectory);
        Path scriptPath = findStartScript(serverPath, serverType, scriptFile);

        if (scriptPath == null) {
            printer.println(HexColor.colorText("No start script found in: " + serverPath, HexColor.Colors.RED), true);
            printer.println(HexColor.colorText("Looked for: " + getScriptSearchList(serverType, scriptFile), HexColor.Colors.GRAY), true);
            return false;
        }

        MinecraftServer server = new MinecraftServer(serverId, serverType, serverPath, scriptPath, config);
        servers.put(serverId, server);

        boolean started = startServerWithScript(serverId, workingDirectory, scriptPath);

        // Automatically create server configuration if it doesn't exist
        if (started) {
            createOrUpdateServerConfiguration(serverId, serverType, workingDirectory, scriptPath != null ? scriptPath.getFileName().toString() : null);
        }

        if (started) {
            printer.println(HexColor.colorText("Started " + serverType.getName() + " server '" + serverId + "' using " + scriptPath.getFileName(), HexColor.Colors.GREEN), true);

            if (serverType == MinecraftServerType.VANILLA && !new File(serverPath.toFile(), "eula.txt").exists()) {
                printer.println(HexColor.colorText("Remember to accept the EULA in eula.txt", HexColor.Colors.YELLOW), true);
            }
        }

        return started;
    }

    private Path findStartScript(Path serverPath, MinecraftServerType serverType, String customScript) {
        if (customScript != null) {
            Path customPath = serverPath.resolve(customScript);
            if (customPath.toFile().exists()) {
                return customPath;
            }
        }

        String[] scriptsToTry = {
            serverType.getStartScript(),
            serverType.getAlternativeStartScript(),
            "start.sh", "start.bat", "run.sh", "run.bat"
        };

        for (String scriptName : scriptsToTry) {
            Path scriptPath = serverPath.resolve(scriptName);
            if (scriptPath.toFile().exists()) {
                return scriptPath;
            }
        }

        return null;
    }

    private String getScriptSearchList(MinecraftServerType serverType, String customScript) {
        StringBuilder list = new StringBuilder();

        if (customScript != null) {
            list.append(customScript).append(", ");
        }

        list.append(serverType.getStartScript())
            .append(", ").append(serverType.getAlternativeStartScript())
            .append(", start.sh, start.bat, run.sh, run.bat");

        return list.toString();
    }

    private boolean startServerWithScript(String serverId, String workingDirectory, Path scriptPath) {
        try {
            boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");

            List<String> command = new ArrayList<>();
            if (isWindows) {
                command.add("cmd");
                command.add("/c");
                command.add(scriptPath.getFileName().toString());
            } else {
                makeScriptExecutable(scriptPath);
                command.add("bash");
                command.add(scriptPath.getFileName().toString());
            }

            return processManager.startInteractiveProcess(
                serverId, 
                new ProcessBuilder(command)
                    .directory(new File(workingDirectory))
                    .start(),
                Paths.get(workingDirectory)
            );

        } catch (Exception e) {
            printer.println(HexColor.colorText("Failed to start server with script: " + e.getMessage(), HexColor.Colors.RED), true);
            return false;
        }
    }

    private void makeScriptExecutable(Path scriptPath) {
        try {
            if (!System.getProperty("os.name").toLowerCase().contains("win")) {
                Runtime.getRuntime().exec("chmod +x " + scriptPath.toString());
            }
        } catch (Exception e) {
            printer.println(HexColor.colorText("Warning: Could not make script executable: " + e.getMessage(), HexColor.Colors.YELLOW), true);
        }
    }

    public boolean stopServer(String serverId) {
        MinecraftServer server = servers.get(serverId);
        if (server == null) {
            printer.println(HexColor.colorText("Server '" + serverId + "' not found.", HexColor.Colors.YELLOW), true);
            return false;
        }

        processManager.sendInput(serverId, "stop");

        new Thread(() -> {
            try {
                Thread.sleep(10000);
                if (processManager.isProcessRunning(serverId)) {
                    processManager.stopProcess(serverId);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

        return true;
    }

    public void sendCommand(String serverId, String command) {
        if (!processManager.isProcessRunning(serverId)) {
            printer.println(HexColor.colorText("Server '" + serverId + "' is not running.", HexColor.Colors.RED), true);
            return;
        }

        processManager.sendInput(serverId, command);
        printer.println(HexColor.colorText("[" + serverId + "] Command sent: " + command, HexColor.Colors.GREEN), true);
    }

    public void kickPlayer(String serverId, String playerName, String reason) {
        String command = "kick " + playerName + (reason != null ? " " + reason : "");
        sendCommand(serverId, command);
    }

    public void banPlayer(String serverId, String playerName, String reason) {
        String command = "ban " + playerName + (reason != null ? " " + reason : "");
        sendCommand(serverId, command);
    }

    public void giveItem(String serverId, String playerName, String item, int amount) {
        sendCommand(serverId, "give " + playerName + " " + item + " " + amount);
    }

    public void setGamemode(String serverId, String playerName, String gamemode) {
        sendCommand(serverId, "gamemode " + gamemode + " " + playerName);
    }

    public void teleportPlayer(String serverId, String playerName, String targetOrCoords) {
        sendCommand(serverId, "tp " + playerName + " " + targetOrCoords);
    }

    public void listServers() {
        if (servers.isEmpty()) {
            printer.println(HexColor.colorText("No servers registered.", HexColor.Colors.GRAY), true);
            return;
        }

        printer.println(HexColor.colorText("Registered Minecraft servers:", HexColor.Colors.CYAN), true);
        servers.forEach((id, server) -> {
            boolean running = processManager.isProcessRunning(id);
            String status = running ? "RUNNING" : "STOPPED";
            String color = running ? HexColor.Colors.GREEN : HexColor.Colors.RED;

            printer.println(HexColor.colorText("  " + id + " (" + server.type().getName() + "): ", HexColor.Colors.WHITE) +
                           HexColor.colorText(status, color) +
                           HexColor.colorText(" - " + server.workingDirectory(), HexColor.Colors.GRAY), true);
        });
    }

    public void showPlayerStats(String playerName) {
        PlayerStats stats = playerStats.get(playerName.toLowerCase());
        if (stats == null) {
            printer.println(HexColor.colorText("No stats found for player: " + playerName, HexColor.Colors.YELLOW), true);
            return;
        }

        printer.println(HexColor.colorText("Player Stats for " + playerName + ":", HexColor.Colors.CYAN), true);
        printer.println(HexColor.colorText("  Total Joins: " + stats.totalJoins, HexColor.Colors.WHITE), true);
        printer.println(HexColor.colorText("  Total Playtime: " + formatTime(stats.totalPlaytime), HexColor.Colors.WHITE), true);
        printer.println(HexColor.colorText("  Messages Sent: " + stats.messagesSent, HexColor.Colors.WHITE), true);
        printer.println(HexColor.colorText("  First Join: " + stats.firstJoin, HexColor.Colors.WHITE), true);
        printer.println(HexColor.colorText("  Last Seen: " + stats.lastSeen, HexColor.Colors.WHITE), true);
        printer.println(HexColor.colorText("  Currently Online: " + (stats.isOnline ? "Yes" : "No"), 
                         stats.isOnline ? HexColor.Colors.GREEN : HexColor.Colors.RED), true);
    }

    public void processLogLine(String serverId, String logLine) {
        Matcher joinMatcher = PLAYER_JOIN_PATTERN.matcher(logLine);
        if (joinMatcher.find()) {
            String playerName = joinMatcher.group(1);
            handlePlayerJoin(serverId, playerName);
            return;
        }

        Matcher leaveMatcher = PLAYER_LEAVE_PATTERN.matcher(logLine);
        if (leaveMatcher.find()) {
            String playerName = leaveMatcher.group(1);
            handlePlayerLeave(serverId, playerName);
            return;
        }

        Matcher chatMatcher = PLAYER_CHAT_PATTERN.matcher(logLine);
        if (chatMatcher.find()) {
            String playerName = chatMatcher.group(1);
            handlePlayerChat(serverId, playerName);
        }
    }

    private void handlePlayerJoin(String serverId, String playerName) {
        PlayerStats stats = playerStats.computeIfAbsent(playerName.toLowerCase(), 
            k -> new PlayerStats(playerName));

        stats.totalJoins++;
        stats.isOnline = true;
        stats.currentSessionStart = System.currentTimeMillis();

        if (stats.firstJoin == null) {
            stats.firstJoin = new java.util.Date();
        }

        printer.println(HexColor.colorText("[" + serverId + "] Player joined: " + playerName, HexColor.Colors.GREEN), true);
    }

    private void handlePlayerLeave(String serverId, String playerName) {
        PlayerStats stats = playerStats.get(playerName.toLowerCase());
        if (stats != null) {
            stats.isOnline = false;
            stats.lastSeen = new java.util.Date();

            if (stats.currentSessionStart > 0) {
                stats.totalPlaytime += System.currentTimeMillis() - stats.currentSessionStart;
                stats.currentSessionStart = 0;
            }
        }

        printer.println(HexColor.colorText("[" + serverId + "] Player left: " + playerName, HexColor.Colors.ORANGE), true);
    }

    private void handlePlayerChat(String serverId, String playerName) {
        PlayerStats stats = playerStats.get(playerName.toLowerCase());
        if (stats != null) {
            stats.messagesSent++;
        }
    }


    public void setServerConfigManager(ServerConfigManager serverConfigManager) {
        this.serverConfigManager = serverConfigManager;
    }

    private void createOrUpdateServerConfiguration(String serverId, MinecraftServerType serverType, String workingDirectory, String scriptFile) {
        if (serverConfigManager == null) {
            return;
        }

        // Check if configuration already exists
        community.theprojects.fairy.config.ServerConfiguration existingConfig = serverConfigManager.getServerConfiguration(serverId);

        if (existingConfig == null) {
            // Create new configuration with autoStart and autoRestart enabled by default
            community.theprojects.fairy.config.ServerConfiguration newConfig = 
                new community.theprojects.fairy.config.ServerConfiguration(serverId, serverType, workingDirectory, scriptFile);

            serverConfigManager.addServerConfiguration(newConfig);

            printer.println(HexColor.colorText("Created configuration for server: " + serverId + " (autoStart & autoRestart enabled)", 
                           HexColor.Colors.CYAN), true);
        } else {
            // Update last start time
            existingConfig.updateLastStart();
            serverConfigManager.updateServerConfiguration(existingConfig);
        }
    }

    private String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long remainingSeconds = seconds % 60;

        return String.format("%dh %dm %ds", hours, minutes, remainingSeconds);
    }

    private static class PlayerStats {
        String playerName;
        int totalJoins = 0;
        long totalPlaytime = 0;
        int messagesSent = 0;
        java.util.Date firstJoin;
        java.util.Date lastSeen;
        boolean isOnline = false;
        long currentSessionStart = 0;

        PlayerStats(String playerName) {
            this.playerName = playerName;
        }
    }

    public record MinecraftServer(
        String id,
        MinecraftServerType type,
        Path workingDirectory,
        Path scriptPath,
        ServerConfig config
    ) {}

    public Map<String, MinecraftServer> getRegisteredServers() {
        return new HashMap<>(servers);
    }

    public boolean startRegisteredServer(String serverId) {
        MinecraftServer server = servers.get(serverId);
        if (server == null) {
            return false;
        }

        return startServer(serverId, server.type(), server.workingDirectory().toString(), 
                          server.scriptPath().getFileName().toString(), server.config());
    }

    public Map<String, Object> getPlayerStatsAsMap(String playerName) {
        PlayerStats stats = playerStats.get(playerName.toLowerCase());
        if (stats == null) {
            return null;
        }

        Map<String, Object> statsMap = new HashMap<>();
        statsMap.put("playerName", stats.playerName);
        statsMap.put("totalJoins", stats.totalJoins);
        statsMap.put("totalPlaytime", stats.totalPlaytime);
        statsMap.put("messagesSent", stats.messagesSent);
        statsMap.put("firstJoin", stats.firstJoin);
        statsMap.put("lastSeen", stats.lastSeen);
        statsMap.put("isOnline", stats.isOnline);
        statsMap.put("playtimeFormatted", formatTime(stats.totalPlaytime));

        return statsMap;
    }

    public record ServerConfig(
        String minMemory,
        String maxMemory,
        boolean enableGarbageCollectionLogging,
        boolean optimizeForMinecraft,
        List<String> additionalJvmArgs,
        List<String> additionalServerArgs
    ) {
        public static ServerConfig defaultConfig() {
            return new ServerConfig(
                "1G", "2G", false, true,
                List.of(), List.of()
            );
        }

        public static ServerConfig highPerformance() {
            return new ServerConfig(
                "2G", "4G", true, true,
                List.of("-server"), List.of()
            );
        }
    }
}
