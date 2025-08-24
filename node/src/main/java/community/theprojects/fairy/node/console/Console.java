package community.theprojects.fairy.node.console;

import community.theprojects.fairy.api.command.ICommand;
import community.theprojects.fairy.api.command.ICommandHandler;
import community.theprojects.fairy.api.console.IConsole;
import community.theprojects.fairy.api.console.IPrinter;
import community.theprojects.fairy.node.FairyNode;
import community.theprojects.fairy.api.exception.NoTerminalFoundException;
import org.jline.reader.*;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Console implements IConsole {
    private final List<String> commands = new ArrayList<>(List.of("info", "version", "help", "create", "start", "stop", "delete", "api", "minecraft", "process", "config", "screen", "echo", "secret", "clear", "history", "exit"));

    private Terminal terminal;
    private Completer completer;
    private LineReader reader;
    private IPrinter printer;

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
            this.printer = new Printer(terminal.writer(), HexColor.colorText("Fairy » ", HexColor.Colors.CYAN));
            printer.println(HexColor.colorText("Welcome to Fairy - Type 'help' to list commands.", HexColor.Colors.GREEN), true);
            terminal.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
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
            /*if (screenManager.isInServerScreen()) {
                if (!screenManager.sendInputToCurrentScreen(line)) {
                    handleNormalCommand(cmd, parts, arg);
                }
                terminal.flush();
                continue;
            }*/
            handleCommand(cmd, parts);
            terminal.flush();
        }
        this.stop();
    }

    @Override
    public void handleCommand(String cmd, String[] parts) {
        ICommandHandler commandHandler = FairyNode.getInstance().getCommandHandler();
        if (!commandHandler.getCommands().containsKey(cmd)) {
            return;
        }
        ICommand command = commandHandler.getCommands().get(cmd);
        if (command == null) {
            return;
        }
        String[] args = Arrays.copyOfRange(parts, 1, parts.length);
        command.execute(args);
    }

    @Override
    public void stop() {
        this.printer = null;
        this.reader = null;
        this.completer = null;
        this.terminal.flush();
        this.terminal = null;
    }

    @Override
    public IPrinter getPrinter() {
        return printer;
    }
}
