package community.theprojects.fairy.console.command;

import community.theprojects.fairy.config.ServerConfigManager;
import community.theprojects.fairy.config.ServerConfiguration;
import community.theprojects.fairy.console.HexColor;
import community.theprojects.fairy.console.Printer;
import community.theprojects.fairy.minecraft.AutoRestartMonitor;

public record ConfigCommand(Printer printer, ServerConfigManager configManager, AutoRestartMonitor restartMonitor, String[] args) implements Command {

    @Override
    public void execute() {
        if (args.length < 2) {
            showConfigHelp();
            return;
        }

        String subCommand = args[1];

        switch (subCommand.toLowerCase()) {
            case "list" -> configManager.listConfigurations();
            case "autostart" -> handleAutoStartConfig();
            case "autorestart" -> handleAutoRestartConfig();
            case "export" -> handleExport();
            case "import" -> handleImport();
            case "monitor" -> restartMonitor.showMonitoringStatus();
            default -> {
                printer.println(HexColor.colorText("Unknown config command: " + subCommand, HexColor.Colors.RED), true);
                showConfigHelp();
            }
        }
    }

    private void handleAutoStartConfig() {
        if (args.length < 4) {
            printer.println(HexColor.colorText("Usage: config autostart <server-id> <enable|disable>", HexColor.Colors.YELLOW), true);
            return;
        }

        String serverId = args[2];
        String action = args[3];
        boolean enable = "enable".equalsIgnoreCase(action);

        configManager.enableAutoStart(serverId, enable);
    }

    private void handleAutoRestartConfig() {
        if (args.length < 4) {
            printer.println(HexColor.colorText("Usage: config autorestart <server-id> <enable|disable>", HexColor.Colors.YELLOW), true);
            return;
        }

        String serverId = args[2];
        String action = args[3];
        boolean enable = "enable".equalsIgnoreCase(action);

        configManager.enableAutoRestart(serverId, enable);
    }

    private void handleExport() {
        if (args.length < 3) {
            printer.println(HexColor.colorText("Usage: config export <filename>", HexColor.Colors.YELLOW), true);
            return;
        }

        String filename = args[2];
        configManager.exportConfigurations(filename);
    }

    private void handleImport() {
        if (args.length < 3) {
            printer.println(HexColor.colorText("Usage: config import <filename>", HexColor.Colors.YELLOW), true);
            return;
        }

        String filename = args[2];
        configManager.importConfigurations(filename);
    }

    private void showConfigHelp() {
        printer.println(HexColor.colorText("Configuration Management Commands:", HexColor.Colors.CYAN), true);
        printer.println(HexColor.colorText("  config list - List all server configurations", HexColor.Colors.WHITE), true);
        printer.println(HexColor.colorText("  config autostart <server-id> <enable|disable> - Configure auto-start", HexColor.Colors.WHITE), true);
        printer.println(HexColor.colorText("  config autorestart <server-id> <enable|disable> - Configure auto-restart", HexColor.Colors.WHITE), true);
        printer.println(HexColor.colorText("  config export <filename> - Export configurations", HexColor.Colors.WHITE), true);
        printer.println(HexColor.colorText("  config import <filename> - Import configurations", HexColor.Colors.WHITE), true);
        printer.println(HexColor.colorText("  config monitor - Show auto-restart monitoring status", HexColor.Colors.WHITE), true);
    }
}
