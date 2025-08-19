package community.theprojects.fairy.node.console;

import community.theprojects.fairy.node.FairyNode;
import community.theprojects.fairy.node.exception.NoTerminalFoundException;
import org.jline.reader.*;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class Console {
    private final List<String> commands = new ArrayList<>(List.of("info", "version", "help", "create", "start", "stop", "delete", "api", "minecraft", "process", "config", "screen", "echo", "secret", "clear", "history", "exit"));

    private Terminal terminal;
    private Completer completer;
    private LineReader reader;
    private Printer printer;

    public Console() {
        try {
            terminal = TerminalBuilder.builder()
                    .system(true)
                    .build();
            completer = (reader, line, candidates) -> {
                String buffer = line.line();
                for (String command : commands) {
                    if (command.startsWith(buffer)) {
                        candidates.add(new Candidate(command));
                    }
                }
            };
            reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .completer(completer)
                    .history(new DefaultHistory())
                    .build();
            if (terminal == null) {
                throw new NoTerminalFoundException("Terminal is null.");
            }
            this.printer = new Printer(terminal.writer(), "Fairy | ");
            printer.println(HexColor.colorText("Welcome to Fairy - Type 'help' to list commands.", HexColor.Colors.GREEN), true);
            terminal.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void start() {
        while (true) {
            String line;
            try {
                line = reader.readLine(HexColor.colorText(System.getProperty("user.name") + "@" + System.getenv("HOSTNAME") + " » ", "#229DDA"));
            } catch (UserInterruptException e) {
                continue;
            } catch (EndOfFileException e) {
                break;
            }
            if (line == null) break;
            line = line.trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split("\\s+");
            String cmd = parts[0];
            String arg = parts.length > 1 ? parts[1] : "";
            if (screenManager.isInServerScreen()) {
                if (!screenManager.sendInputToCurrentScreen(line)) {
                    handleNormalCommand(cmd, parts, arg);
                }
                terminal.flush();
                continue;
            }
            handleNormalCommand(cmd, parts, arg);
            terminal.flush();
        }
        this.stop();
    }

    public void stop() {
        this.printer.println("Stopping node '" + FairyNode.getInstance().getName() + "'");
        this.printer = null;
        this.reader = null;
        this.completer = null;
        this.terminal.flush();
        this.terminal = null;
    }

    public Printer getPrinter() {
        return printer;
    }
}
