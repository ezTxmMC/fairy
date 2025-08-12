package community.theprojects.fairy.console.command;

import community.theprojects.fairy.console.HexColor;
import community.theprojects.fairy.console.Printer;

public record UnknownCommand(Printer printer, String commandName) implements Command {

    @Override
    public void execute() {
        printer.println(HexColor.colorText("Unknown command: ", HexColor.Colors.RED) +
                HexColor.colorText(commandName, HexColor.Colors.YELLOW) +
                HexColor.colorText(". Type 'help'.", HexColor.Colors.RED), true);
    }
}
