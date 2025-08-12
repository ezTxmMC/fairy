package community.theprojects.fairy.console.command;

import community.theprojects.fairy.console.HexColor;
import community.theprojects.fairy.console.Printer;
import org.jline.reader.LineReader;

public record HistoryCommand(Printer printer, LineReader reader) implements Command {

    @Override
    public void execute() {
        reader.getHistory().forEach(h -> printer.println(
                HexColor.colorText(String.valueOf(h.index()), HexColor.Colors.GRAY) +
                        HexColor.colorText(": ", HexColor.Colors.WHITE) +
                        HexColor.colorText(h.line(), HexColor.Colors.LIGHT_GRAY), true));
    }
}
