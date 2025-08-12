package community.theprojects.fairy.console.command;

import community.theprojects.fairy.console.HexColor;
import community.theprojects.fairy.console.Printer;
import community.theprojects.fairy.console.screen.ServerScreenManager;

public record ScreenCommand(Printer printer, ServerScreenManager screenManager, String[] args) implements Command {

    @Override
    public void execute() {
        if (args.length < 2) {
            showScreenHelp();
            return;
        }

        String subCommand = args[1];

        switch (subCommand.toLowerCase()) {
            case "list", "ls" -> screenManager.listScreens();
            case "create", "new" -> handleCreate();
            case "switch", "s" -> handleSwitch();
            case "main", "m" -> screenManager.switchToMain();
            case "remove", "rm" -> handleRemove();
            case "status" -> screenManager.showStatus();
            default -> {
                printer.println(HexColor.colorText("Unknown screen command: " + subCommand, HexColor.Colors.RED), true);
                showScreenHelp();
            }
        }
    }

    private void handleCreate() {
        if (args.length < 3) {
            printer.println(HexColor.colorText("Usage: screen create <server-id>", HexColor.Colors.YELLOW), true);
            return;
        }

        String serverId = args[2];
        screenManager.createScreen(serverId);
    }

    private void handleSwitch() {
        if (args.length < 3) {
            printer.println(HexColor.colorText("Usage: screen switch <server-id>", HexColor.Colors.YELLOW), true);
            return;
        }

        String serverId = args[2];
        screenManager.switchToServer(serverId);
    }

    private void handleRemove() {
        if (args.length < 3) {
            printer.println(HexColor.colorText("Usage: screen remove <server-id>", HexColor.Colors.YELLOW), true);
            return;
        }

        String serverId = args[2];
        screenManager.removeScreen(serverId);
    }

    private void showScreenHelp() {
        printer.println(HexColor.colorText("Screen System Commands (Linux screen-like):", HexColor.Colors.CYAN), true);
        printer.println(HexColor.colorText("  screen list - List all active screens", HexColor.Colors.WHITE), true);
        printer.println(HexColor.colorText("  screen create <server-id> - Create new screen for server", HexColor.Colors.WHITE), true);
        printer.println(HexColor.colorText("  screen switch <server-id> - Switch to server screen", HexColor.Colors.WHITE), true);
        printer.println(HexColor.colorText("  screen main - Switch back to main console", HexColor.Colors.WHITE), true);
        printer.println(HexColor.colorText("  screen remove <server-id> - Remove server screen", HexColor.Colors.WHITE), true);
        printer.println(HexColor.colorText("  screen status - Show screen system status", HexColor.Colors.WHITE), true);

        printer.println(HexColor.colorText("\nShortcuts (when in server screen):", HexColor.Colors.YELLOW), true);
        printer.println(HexColor.colorText("  screen main - Return to main console", HexColor.Colors.GRAY), true);
        printer.println(HexColor.colorText("  screen s <server-id> - Switch to another server", HexColor.Colors.GRAY), true);
        printer.println(HexColor.colorText("  detach or exit - Return to main console", HexColor.Colors.GRAY), true);
    }
}
