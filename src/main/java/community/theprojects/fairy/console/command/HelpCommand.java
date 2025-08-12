package community.theprojects.fairy.console.command;

import community.theprojects.fairy.console.HexColor;
import community.theprojects.fairy.console.Printer;

import java.util.List;

public record HelpCommand(Printer printer, List<String> commands) implements Command {

    @Override
    public void execute() {
        printer.println(HexColor.colorText("Available commands: ", HexColor.Colors.CYAN) +
                HexColor.colorText(String.join(", ", commands), HexColor.Colors.YELLOW), true);

        printer.println(HexColor.colorText("\nMain command categories:", HexColor.Colors.CYAN), true);
        printer.println(HexColor.colorText("  api - REST API server management", HexColor.Colors.WHITE), true);
        printer.println(HexColor.colorText("  minecraft - Minecraft server management", HexColor.Colors.WHITE), true);
        printer.println(HexColor.colorText("  process - Java process management", HexColor.Colors.WHITE), true);
        printer.println(HexColor.colorText("  help, history, clear, exit - Console utilities", HexColor.Colors.WHITE), true);

        printer.println(HexColor.colorText("\nNote: REST API starts automatically on port 8080", HexColor.Colors.GRAY), true);
        printer.println(HexColor.colorText("Use 'api status' to check API server status", HexColor.Colors.GRAY), true);
    }
}
