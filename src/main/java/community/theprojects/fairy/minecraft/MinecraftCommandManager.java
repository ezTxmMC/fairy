package community.theprojects.fairy.minecraft;

import community.theprojects.fairy.console.HexColor;
import community.theprojects.fairy.console.Printer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MinecraftCommandManager {

    private final MinecraftServerManager serverManager;
    private final Printer printer;

    public MinecraftCommandManager(MinecraftServerManager serverManager, Printer printer) {
        this.serverManager = serverManager;
        this.printer = printer;
    }

    public void executeMinecraftCommand(String[] parts) {
        if (parts.length < 2) {
            showMinecraftHelp();
            return;
        }

        String subCommand = parts[1];

        // Debug output to help troubleshoot
        printer.println(HexColor.colorText("Debug: Received " + parts.length + " parts: " + String.join(" | ", parts), HexColor.Colors.GRAY), true);

        switch (subCommand.toLowerCase()) {
            case "start" -> handleStartCommand(parts);
            case "stop" -> handleStopCommand(parts);
            case "restart" -> handleRestartCommand(parts);
            case "list" -> serverManager.listServers();
            case "status" -> handleStatusCommand(parts);
            case "command" -> handleServerCommand(parts);
            case "kick" -> handleKickCommand(parts);
            case "ban" -> handleBanCommand(parts);
            case "give" -> handleGiveCommand(parts);
            case "tp" -> handleTeleportCommand(parts);
            case "gamemode" -> handleGamemodeCommand(parts);
            case "stats" -> handleStatsCommand(parts);
            case "backup" -> handleBackupCommand(parts);
            case "plugin" -> handlePluginCommand(parts);
            case "whitelist" -> handleWhitelistCommand(parts);
            case "op" -> handleOpCommand(parts);
            default -> {
                printer.println(HexColor.colorText("Unknown minecraft command: " + subCommand, HexColor.Colors.RED), true);
                showMinecraftHelp();
            }
        }
    }

    private void handleStartCommand(String[] parts) {
        if (parts.length < 5) {
            printer.println(HexColor.colorText("Usage: minecraft start <server-id> <type> <directory> [script-file]", 
                           HexColor.Colors.YELLOW), true);
            return;
        }

        String serverId = parts[2];
        String serverType = parts[3];
        String directory = parts[4];
        String scriptFile = parts.length > 5 ? parts[5] : null;

        MinecraftServerType type = MinecraftServerType.fromString(serverType);
        MinecraftServerManager.ServerConfig config = type.isProxy() ? 
            MinecraftServerManager.ServerConfig.defaultConfig() : 
            MinecraftServerManager.ServerConfig.highPerformance();

        serverManager.startServer(serverId, type, directory, scriptFile, config);
    }

    private void handleStopCommand(String[] parts) {
        if (parts.length < 3) {
            printer.println(HexColor.colorText("Usage: minecraft stop <server-id>", HexColor.Colors.YELLOW), true);
            return;
        }

        serverManager.stopServer(parts[2]);
    }

    private void handleRestartCommand(String[] parts) {
        if (parts.length < 3) {
            printer.println(HexColor.colorText("Usage: minecraft restart <server-id>", HexColor.Colors.YELLOW), true);
            return;
        }

        String serverId = parts[2];
        printer.println(HexColor.colorText("Restarting server: " + serverId, HexColor.Colors.YELLOW), true);

        serverManager.stopServer(serverId);

        new Thread(() -> {
            try {
                Thread.sleep(5000);
                printer.println(HexColor.colorText("Starting server: " + serverId, HexColor.Colors.GREEN), true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void handleServerCommand(String[] parts) {
        if (parts.length < 4) {
            printer.println(HexColor.colorText("Usage: minecraft command <server-id> <command>", HexColor.Colors.YELLOW), true);
            return;
        }

        String serverId = parts[2];
        String command = String.join(" ", java.util.Arrays.copyOfRange(parts, 3, parts.length));

        serverManager.sendCommand(serverId, command);
    }

    private void handleKickCommand(String[] parts) {
        if (parts.length < 4) {
            printer.println(HexColor.colorText("Usage: minecraft kick <server-id> <player> [reason]", HexColor.Colors.YELLOW), true);
            return;
        }

        String serverId = parts[2];
        String playerName = parts[3];
        String reason = parts.length > 4 ? String.join(" ", java.util.Arrays.copyOfRange(parts, 4, parts.length)) : null;

        serverManager.kickPlayer(serverId, playerName, reason);
    }

    private void handleStatsCommand(String[] parts) {
        if (parts.length < 3) {
            printer.println(HexColor.colorText("Usage: minecraft stats <player-name>", HexColor.Colors.YELLOW), true);
            return;
        }

        serverManager.showPlayerStats(parts[2]);
    }


    private void handleBackupCommand(String[] parts) {
        if (parts.length < 3) {
            printer.println(HexColor.colorText("Usage: minecraft backup <server-id>", HexColor.Colors.YELLOW), true);
            return;
        }

        String serverId = parts[2];
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String backupName = "backup_" + serverId + "_" + timestamp;

        serverManager.sendCommand(serverId, "save-all");

        try {
            Thread.sleep(2000);
            printer.println(HexColor.colorText("Creating backup: " + backupName, HexColor.Colors.YELLOW), true);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void handleGiveCommand(String[] parts) {
        if (parts.length < 6) {
            printer.println(HexColor.colorText("Usage: minecraft give <server-id> <player> <item> <amount>", 
                           HexColor.Colors.YELLOW), true);
            return;
        }

        String serverId = parts[2];
        String playerName = parts[3];
        String item = parts[4];
        int amount = Integer.parseInt(parts[5]);

        serverManager.giveItem(serverId, playerName, item, amount);
    }

    private void handleTeleportCommand(String[] parts) {
        if (parts.length < 5) {
            printer.println(HexColor.colorText("Usage: minecraft tp <server-id> <player> <target/coords>", 
                           HexColor.Colors.YELLOW), true);
            return;
        }

        String serverId = parts[2];
        String playerName = parts[3];
        String target = String.join(" ", java.util.Arrays.copyOfRange(parts, 4, parts.length));

        serverManager.teleportPlayer(serverId, playerName, target);
    }

    private void handleGamemodeCommand(String[] parts) {
        if (parts.length < 5) {
            printer.println(HexColor.colorText("Usage: minecraft gamemode <server-id> <player> <gamemode>", 
                           HexColor.Colors.YELLOW), true);
            return;
        }

        String serverId = parts[2];
        String playerName = parts[3];
        String gamemode = parts[4];

        serverManager.setGamemode(serverId, playerName, gamemode);
    }

    private void handleBanCommand(String[] parts) {
        if (parts.length < 4) {
            printer.println(HexColor.colorText("Usage: minecraft ban <server-id> <player> [reason]", 
                           HexColor.Colors.YELLOW), true);
            return;
        }

        String serverId = parts[2];
        String playerName = parts[3];
        String reason = parts.length > 4 ? String.join(" ", java.util.Arrays.copyOfRange(parts, 4, parts.length)) : null;

        serverManager.banPlayer(serverId, playerName, reason);
    }

    private void handleStatusCommand(String[] parts) {
        if (parts.length < 3) {
            serverManager.listServers();
            return;
        }

        String serverId = parts[2];
        printer.println(HexColor.colorText("Status for server: " + serverId, HexColor.Colors.CYAN), true);
    }

    private void handlePluginCommand(String[] parts) {
        if (parts.length < 4) {
            printer.println(HexColor.colorText("Usage: minecraft plugin <server-id> <list|reload|enable|disable> [plugin-name]", 
                           HexColor.Colors.YELLOW), true);
            return;
        }

        String serverId = parts[2];
        String action = parts[3];

        switch (action.toLowerCase()) {
            case "list" -> serverManager.sendCommand(serverId, "plugins");
            case "reload" -> {
                String pluginName = parts.length > 4 ? parts[4] : null;
                if (pluginName != null) {
                    serverManager.sendCommand(serverId, "plugman reload " + pluginName);
                } else {
                    serverManager.sendCommand(serverId, "reload");
                }
            }
            default -> printer.println(HexColor.colorText("Unknown plugin action: " + action, HexColor.Colors.RED), true);
        }
    }

    private void handleWhitelistCommand(String[] parts) {
        if (parts.length < 5) {
            printer.println(HexColor.colorText("Usage: minecraft whitelist <server-id> <add|remove|list|on|off> [player]", 
                           HexColor.Colors.YELLOW), true);
            return;
        }

        String serverId = parts[2];
        String action = parts[3];
        String playerName = parts.length > 4 ? parts[4] : null;

        String command = switch (action.toLowerCase()) {
            case "add" -> "whitelist add " + playerName;
            case "remove" -> "whitelist remove " + playerName;
            case "list" -> "whitelist list";
            case "on" -> "whitelist on";
            case "off" -> "whitelist off";
            default -> null;
        };

        if (command != null) {
            serverManager.sendCommand(serverId, command);
        } else {
            printer.println(HexColor.colorText("Unknown whitelist action: " + action, HexColor.Colors.RED), true);
        }
    }

    private void handleOpCommand(String[] parts) {
        if (parts.length < 5) {
            printer.println(HexColor.colorText("Usage: minecraft op <server-id> <add|remove|list> [player]", 
                           HexColor.Colors.YELLOW), true);
            return;
        }

        String serverId = parts[2];
        String action = parts[3];
        String playerName = parts.length > 4 ? parts[4] : null;

        String command = switch (action.toLowerCase()) {
            case "add" -> "op " + playerName;
            case "remove" -> "deop " + playerName;
            case "list" -> "list ops";
            default -> null;
        };

        if (command != null) {
            serverManager.sendCommand(serverId, command);
        } else {
            printer.println(HexColor.colorText("Unknown op action: " + action, HexColor.Colors.RED), true);
        }
    }

    private void showMinecraftHelp() {
        printer.println(HexColor.colorText("Minecraft Server Management Commands:", HexColor.Colors.CYAN), true);
        printer.println(HexColor.colorText("  minecraft start <id> <type> <directory> [script] - Start server", HexColor.Colors.WHITE), true);
        printer.println(HexColor.colorText("  minecraft stop <id> - Stop server", HexColor.Colors.WHITE), true);
        printer.println(HexColor.colorText("  minecraft restart <id> - Restart server", HexColor.Colors.WHITE), true);
        printer.println(HexColor.colorText("  minecraft list - List all servers", HexColor.Colors.WHITE), true);
        printer.println(HexColor.colorText("  minecraft status [id] - Show server status", HexColor.Colors.WHITE), true);
        printer.println(HexColor.colorText("  minecraft command <id> <cmd> - Execute server command", HexColor.Colors.WHITE), true);
        printer.println(HexColor.colorText("  minecraft kick <id> <player> [reason] - Kick player", HexColor.Colors.WHITE), true);
        printer.println(HexColor.colorText("  minecraft ban <id> <player> [reason] - Ban player", HexColor.Colors.WHITE), true);
        printer.println(HexColor.colorText("  minecraft give <id> <player> <item> <amount> - Give item", HexColor.Colors.WHITE), true);
        printer.println(HexColor.colorText("  minecraft tp <id> <player> <target> - Teleport player", HexColor.Colors.WHITE), true);
        printer.println(HexColor.colorText("  minecraft gamemode <id> <player> <mode> - Set gamemode", HexColor.Colors.WHITE), true);
        printer.println(HexColor.colorText("  minecraft stats <player> - Show player stats", HexColor.Colors.WHITE), true);
        printer.println(HexColor.colorText("  minecraft backup <id> - Create server backup", HexColor.Colors.WHITE), true);

        printer.println(HexColor.colorText("\nSupported server types:", HexColor.Colors.YELLOW), true);
        printer.println(HexColor.colorText("  vanilla, spigot, paper, velocity, bungeecord, forge, fabric, neoforge, quilt", HexColor.Colors.GRAY), true);
    }
}
