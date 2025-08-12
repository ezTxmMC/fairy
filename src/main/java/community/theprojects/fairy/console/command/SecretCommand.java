package community.theprojects.fairy.console.command;

import community.theprojects.fairy.console.HexColor;
import community.theprojects.fairy.console.Printer;
import org.jline.reader.LineReader;
import org.jline.reader.EndOfFileException;
import org.jline.reader.UserInterruptException;

public record SecretCommand(Printer printer, LineReader reader) implements Command {

    @Override
    public void execute() {
        try {
            String secret = reader.readLine("Password: ", '*');
            if (secret != null) {
                printer.println(HexColor.colorText("Entered (masked): ", HexColor.Colors.CYAN) + 
                               HexColor.colorText((!secret.isEmpty() ? "<hidden>" : "<empty>"), HexColor.Colors.MAGENTA), true);
            }
        } catch (UserInterruptException | EndOfFileException e) {
            printer.println(HexColor.colorText("Input cancelled", HexColor.Colors.YELLOW), true);
        }
    }
}
