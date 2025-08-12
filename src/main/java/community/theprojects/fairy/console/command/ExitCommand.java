package community.theprojects.fairy.console.command;

import community.theprojects.fairy.console.HexColor;
import community.theprojects.fairy.console.Printer;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;

import java.io.IOException;

public record ExitCommand(Printer printer, Terminal terminal, LineReader reader) implements Command {

    @Override
    public void execute() {
        printer.println(HexColor.colorText("Exiting console...", HexColor.Colors.ORANGE), true);
        terminal.flush();
        try {
            reader.getHistory().save();
        } catch (IOException ignored) {
        }
        System.exit(0);
    }
}
