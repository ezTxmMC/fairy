package community.theprojects.fairy.console.command;

import org.jline.terminal.Terminal;
import org.jline.utils.InfoCmp;

public record ClearCommand(Terminal terminal) implements Command {

    @Override
    public void execute() {
        terminal.puts(InfoCmp.Capability.clear_screen);
        terminal.flush();
    }
}
