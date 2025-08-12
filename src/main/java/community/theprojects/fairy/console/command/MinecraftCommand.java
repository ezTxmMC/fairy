package community.theprojects.fairy.console.command;

import community.theprojects.fairy.console.HexColor;
import community.theprojects.fairy.console.Printer;
import community.theprojects.fairy.minecraft.MinecraftCommandManager;

public record MinecraftCommand(Printer printer, MinecraftCommandManager commandManager, String[] args) implements Command {

    @Override
    public void execute() {
        if (args.length < 2) {
            showMinecraftHelp();
            return;
        }

        commandManager.executeMinecraftCommand(args);
    }

    private void showMinecraftHelp() {
        printer.println(HexColor.colorText("Minecraft Server Management Commands:", HexColor.Colors.CYAN), true);
        printer.println(HexColor.colorText("  minecraft start <id> <type> <directory> [jar] - Start server", HexColor.Colors.WHITE), true);
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
