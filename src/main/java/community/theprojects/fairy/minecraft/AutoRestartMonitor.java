package community.theprojects.fairy.minecraft;

import community.theprojects.fairy.config.ServerConfigManager;
import community.theprojects.fairy.config.ServerConfiguration;
import community.theprojects.fairy.console.HexColor;
import community.theprojects.fairy.console.Printer;
import community.theprojects.fairy.process.InteractiveProcessManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AutoRestartMonitor {

    private final MinecraftServerManager serverManager;
    private final ServerConfigManager configManager;
    private final InteractiveProcessManager processManager;
    private final Printer printer;
    private final ScheduledExecutorService scheduler;

    private final Map<String, RestartAttempt> restartAttempts = new ConcurrentHashMap<>();
    private final Map<String, Boolean> wasRunning = new ConcurrentHashMap<>();

    private static final int MAX_RESTART_ATTEMPTS = 3;
    private static final int RESTART_DELAY_SECONDS = 10;
    private static final int MONITORING_INTERVAL_SECONDS = 30;

    public AutoRestartMonitor(MinecraftServerManager serverManager, 
                             ServerConfigManager configManager, 
                             InteractiveProcessManager processManager, 
                             Printer printer) {
        this.serverManager = serverManager;
        this.configManager = configManager;
        this.processManager = processManager;
        this.printer = printer;
        this.scheduler = Executors.newScheduledThreadPool(2);
    }

    public void startMonitoring() {
        scheduler.scheduleAtFixedRate(this::checkServers, 30, MONITORING_INTERVAL_SECONDS, TimeUnit.SECONDS);
        printer.println(HexColor.colorText("Auto-restart monitoring started", HexColor.Colors.GREEN), true);
    }

    public void stopMonitoring() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        printer.println(HexColor.colorText("Auto-restart monitoring stopped", HexColor.Colors.ORANGE), true);
    }

    private void checkServers() {
        for (ServerConfiguration config : configManager.getAutoRestartConfigurations()) {
            String serverId = config.getServerId();
            boolean isCurrentlyRunning = processManager.isProcessRunning(serverId);
            Boolean wasRunningBefore = wasRunning.get(serverId);

            // Update running state
            wasRunning.put(serverId, isCurrentlyRunning);

            // Check if server crashed (was running before, not running now)
            if (wasRunningBefore != null && wasRunningBefore && !isCurrentlyRunning) {
                handleServerCrash(config);
            } else if (isCurrentlyRunning) {
                // Server is running, reset restart attempts
                restartAttempts.remove(serverId);
            }
        }
    }

    private void handleServerCrash(ServerConfiguration config) {
        String serverId = config.getServerId();
        RestartAttempt attempt = restartAttempts.computeIfAbsent(serverId, k -> new RestartAttempt());

        if (attempt.attempts >= MAX_RESTART_ATTEMPTS) {
            printer.println(HexColor.colorText("Server '" + serverId + "' has crashed " + attempt.attempts + 
                           " times, stopping auto-restart", HexColor.Colors.RED), true);

            // Disable auto-restart for this server to prevent infinite loops
            configManager.enableAutoRestart(serverId, false);
            return;
        }

        attempt.attempts++;
        long timeSinceLastAttempt = System.currentTimeMillis() - attempt.lastAttemptTime;

        // Don't restart too frequently
        if (timeSinceLastAttempt < TimeUnit.MINUTES.toMillis(2)) {
            printer.println(HexColor.colorText("Server '" + serverId + "' crashed, waiting before restart attempt " + 
                           attempt.attempts, HexColor.Colors.YELLOW), true);
            return;
        }

        attempt.lastAttemptTime = System.currentTimeMillis();

        printer.println(HexColor.colorText("Server '" + serverId + "' crashed, attempting restart (" + 
                       attempt.attempts + "/" + MAX_RESTART_ATTEMPTS + ")", HexColor.Colors.ORANGE), true);

        // Schedule restart with delay
        scheduler.schedule(() -> restartServer(config), RESTART_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    private void restartServer(ServerConfiguration config) {
        String serverId = config.getServerId();

        try {
            // Clean up any remaining registration before restart
            serverManager.getRegisteredServers().remove(serverId);

            // Use the already registered server instead of creating a new one
            boolean success = serverManager.startRegisteredServer(serverId);

            if (success) {
                config.updateLastStart();
                configManager.updateServerConfiguration(config);

                printer.println(HexColor.colorText("Successfully restarted server: " + serverId, 
                               HexColor.Colors.GREEN), true);

                // Mark as running again
                wasRunning.put(serverId, true);
            } else {
                printer.println(HexColor.colorText("Failed to restart server: " + serverId, 
                               HexColor.Colors.RED), true);
            }

        } catch (Exception e) {
            printer.println(HexColor.colorText("Error restarting server '" + serverId + "': " + e.getMessage(), 
                           HexColor.Colors.RED), true);
        }
    }

    private MinecraftServerManager.ServerConfig createServerConfig(ServerConfiguration config) {
        return new MinecraftServerManager.ServerConfig(
            config.getMinMemory(),
            config.getMaxMemory(),
            false,
            true,
            java.util.List.of(),
            java.util.List.of()
        );
    }

    public void registerRunningServer(String serverId) {
        wasRunning.put(serverId, true);
        restartAttempts.remove(serverId);
    }

    public void unregisterServer(String serverId) {
        wasRunning.remove(serverId);
        restartAttempts.remove(serverId);
    }

    public void showMonitoringStatus() {
        printer.println(HexColor.colorText("Auto-restart Monitoring Status:", HexColor.Colors.CYAN), true);

        if (wasRunning.isEmpty()) {
            printer.println(HexColor.colorText("  No servers being monitored", HexColor.Colors.GRAY), true);
            return;
        }

        for (Map.Entry<String, Boolean> entry : wasRunning.entrySet()) {
            String serverId = entry.getKey();
            Boolean running = entry.getValue();
            RestartAttempt attempt = restartAttempts.get(serverId);

            String status = running ? "RUNNING" : "STOPPED";
            String color = running ? HexColor.Colors.GREEN : HexColor.Colors.RED;

            String restartInfo = "";
            if (attempt != null && attempt.attempts > 0) {
                restartInfo = " (Restart attempts: " + attempt.attempts + ")";
            }

            printer.println(HexColor.colorText("  " + serverId + ": " + status + restartInfo, color), true);
        }
    }

    private static class RestartAttempt {
        int attempts = 0;
        long lastAttemptTime = 0;
    }
}
