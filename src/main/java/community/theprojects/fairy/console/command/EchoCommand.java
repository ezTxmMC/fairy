package community.theprojects.fairy.console.command;

import community.theprojects.fairy.console.HexColor;
import community.theprojects.fairy.console.Printer;

public record EchoCommand(Printer printer, String message) implements Command {

    @Override
    public void execute() {
        printer.println(HexColor.colorText(message, HexColor.Colors.WHITE), true);
    }
}
