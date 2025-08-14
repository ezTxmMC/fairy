package community.theprojects.fairy.console;

import community.theprojects.fairy.api.RestfulApiServer;
import community.theprojects.fairy.config.ServerConfigManager;
import community.theprojects.fairy.config.ServerConfiguration;
import community.theprojects.fairy.console.command.*;
import community.theprojects.fairy.console.screen.ServerScreenManager;
import community.theprojects.fairy.exception.NoTerminalFoundException;
import community.theprojects.fairy.minecraft.AutoRestartMonitor;
import community.theprojects.fairy.minecraft.MinecraftCommandManager;
import community.theprojects.fairy.minecraft.MinecraftServerManager;
import community.theprojects.fairy.process.InteractiveProcessManager;
import community.theprojects.fairy.process.JavaProcessRunner;
import org.jline.reader.*;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class Console {
    private static final int DEFAULT_API_PORT = 8080;
    private static final String API_PORT_PROPERTY = "fairy.api.port";
    private static final String API_AUTOSTART_PROPERTY = "fairy.api.autostart";

    private final List<String> commands = new ArrayList<>(List.of("info", "version", "help", "create", "start", "stop", "delete", "api", "minecraft", "process", "config", "screen", "echo", "secret", "clear", "history", "exit"));
    private Terminal terminal;
    private Completer completer;
    private LineReader reader;
    private Printer printer;

    private InteractiveProcessManager processManager;
    private JavaProcessRunner javaRunner;
    private MinecraftServerManager minecraftServerManager;
    private MinecraftCommandManager minecraftCommandManager;
    private RestfulApiServer apiServer;
    private ServerConfigManager serverConfigManager;
    private AutoRestartMonitor autoRestartMonitor;
    private ServerScreenManager screenManager;

    public void create() {
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

            this.processManager = new InteractiveProcessManager(printer, reader);
            this.javaRunner = new JavaProcessRunner(processManager, printer);
            this.minecraftServerManager = new MinecraftServerManager(processManager, javaRunner, printer);
            this.minecraftCommandManager = new MinecraftCommandManager(minecraftServerManager, printer);
            this.serverConfigManager = new ServerConfigManager(printer);

            this.minecraftServerManager.setServerConfigManager(serverConfigManager);
            this.autoRestartMonitor = new AutoRestartMonitor(minecraftServerManager, serverConfigManager, processManager, printer);
            this.screenManager = new ServerScreenManager(processManager, printer, reader);
            this.processManager.setScreenManager(screenManager);
            this.apiServer = new RestfulApiServer(minecraftServerManager, processManager, printer);

            startApiAutomatically();
            startAutoRestartMonitoring();
            startServersAutomatically();

            printer.println(HexColor.colorText("Welcome to Fairy - Type 'help' to list commands.", HexColor.Colors.GREEN), true);
            terminal.flush();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    reader.getHistory().save();

                    printer.println(HexColor.colorText("Shutting down all server processes...", HexColor.Colors.ORANGE), true);

                    if (autoRestartMonitor != null) {
                        autoRestartMonitor.stopMonitoring();
                    }

                    if (screenManager != null) {
                        screenManager.cleanup();
                    }

                    if (processManager != null) {
                        processManager.shutdown();
                    }

                    if (apiServer != null && apiServer.isRunning()) {
                        apiServer.stop();
                    }

                    printer.println(HexColor.colorText("All processes stopped.", HexColor.Colors.GREEN), true);
                    terminal.flush();
                } catch (IOException e) {
                    System.err.println("Error during shutdown: " + e.getMessage());
                }
            }));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void checker() {
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
        this.end();
    }

    private void handleNormalCommand(String cmd, String[] parts, String arg) {
        switch (cmd) {
            case "help" -> new HelpCommand(printer, commands).execute();
            case "exit" -> new ExitCommand(printer, terminal, reader).execute();
            case "history" -> new HistoryCommand(printer, reader).execute();
            case "clear" -> new ClearCommand(terminal).execute();
            case "api" -> new ApiCommand(printer, apiServer, parts).execute();
            case "minecraft" -> new MinecraftCommand(printer, minecraftCommandManager, parts).execute();
            case "process" -> new ProcessCommand(printer, processManager, javaRunner, parts).execute();
            case "config" -> new ConfigCommand(printer, serverConfigManager, autoRestartMonitor, parts).execute();
            case "screen" -> new ScreenCommand(printer, screenManager, parts).execute();
            case "echo" -> new EchoCommand(printer, arg).execute();
            case "secret" -> new SecretCommand(printer, reader).execute();
            default -> new UnknownCommand(printer, cmd).execute();
        }
    }

    private void startApiAutomatically() {
        boolean autoStart = Boolean.parseBoolean(System.getProperty(API_AUTOSTART_PROPERTY, "true"));

        if (!autoStart) {
            printer.println(HexColor.colorText("API auto-start disabled (fairy.api.autostart=false)", HexColor.Colors.GRAY), true);
            return;
        }

        String portProperty = System.getProperty(API_PORT_PROPERTY);
        int apiPort = DEFAULT_API_PORT;

        if (portProperty != null) {
            try {
                apiPort = Integer.parseInt(portProperty);
            } catch (NumberFormatException e) {
                printer.println(HexColor.colorText("Invalid API port property '" + portProperty + "', using default: " + DEFAULT_API_PORT, 
                               HexColor.Colors.YELLOW), true);
            }
        }

        if (!isPortAvailable(apiPort)) {
            printer.println(HexColor.colorText("Port " + apiPort + " is not available for API server", HexColor.Colors.YELLOW), true);
            int alternativePort = findAvailablePort(apiPort + 1, apiPort + 100);
            if (alternativePort != -1) {
                boolean success = apiServer.start(alternativePort);
                if (success) {
                    printer.println(HexColor.colorText("API started on alternative port " + alternativePort, HexColor.Colors.GREEN), true);
                    printer.println(HexColor.colorText("API Token: " + apiServer.getApiToken(), HexColor.Colors.CYAN), true);
                }
                return;
            }
            printer.println(HexColor.colorText("No available ports found for API server", HexColor.Colors.RED), true);
            printer.println(HexColor.colorText("Start manually with: api start <port>", HexColor.Colors.YELLOW), true);
            return;
        }
        boolean success = apiServer.start(apiPort);
        if (success) {
            printer.println(HexColor.colorText("REST API automatically started on port " + apiPort, HexColor.Colors.GREEN), true);
            printer.println(HexColor.colorText("API Endpoint: http://localhost:" + apiPort + "/api", HexColor.Colors.GRAY), true);
            printer.println(HexColor.colorText("API Token: " + apiServer.getApiToken(), HexColor.Colors.CYAN), true);
            printer.println(HexColor.colorText("Use 'api status' for more information", HexColor.Colors.GRAY), true);
            return;
        }
        printer.println(HexColor.colorText("Failed to auto-start API server on port " + apiPort, HexColor.Colors.RED), true);
        printer.println(HexColor.colorText("You can manually start it with: api start <port>", HexColor.Colors.YELLOW), true);
    }

    private boolean isPortAvailable(int port) {
        try (java.net.ServerSocket socket = new java.net.ServerSocket(port)) {
            socket.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private int findAvailablePort(int startPort, int endPort) {
        for (int port = startPort; port <= endPort; port++) {
            if (isPortAvailable(port)) {
                return port;
            }
        }
        return -1;
    }

    private void startAutoRestartMonitoring() {
        if (autoRestartMonitor != null) {
            autoRestartMonitor.startMonitoring();
        }
    }

    private void startServersAutomatically() {
        java.util.List<ServerConfiguration> autoStartServers = serverConfigManager.getAutoStartConfigurations();

        if (autoStartServers.isEmpty()) {
            printer.println(HexColor.colorText("No servers configured for auto-start", HexColor.Colors.GRAY), true);
            return;
        }

        printer.println(HexColor.colorText("Starting " + autoStartServers.size() + " auto-start servers...", 
                       HexColor.Colors.CYAN), true);

        for (ServerConfiguration config : autoStartServers) {
            try {
                MinecraftServerManager.ServerConfig serverConfig = new MinecraftServerManager.ServerConfig(
                    config.getMinMemory(), config.getMaxMemory(), false, true, 
                    java.util.List.of(), java.util.List.of()
                );

                boolean success = minecraftServerManager.startServer(
                    config.getServerId(),
                    config.getServerType(),
                    config.getWorkingDirectory(),
                    config.getScriptFile(),
                    serverConfig
                );

                if (success) {
                    config.updateLastStart();
                    serverConfigManager.updateServerConfiguration(config);
                    autoRestartMonitor.registerRunningServer(config.getServerId());
                    printer.println(HexColor.colorText("Auto-started: " + config.getServerId(), HexColor.Colors.GREEN), true);
                }
                if (!success) {
                    printer.println(HexColor.colorText("Failed to auto-start: " + config.getServerId(), HexColor.Colors.RED), true);
                }
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                printer.println(HexColor.colorText("Error auto-starting " + config.getServerId() + ": " + e.getMessage(), HexColor.Colors.RED), true);
            } catch (Exception e) {
                printer.println(HexColor.colorText("Error auto-starting " + config.getServerId() + ": " + e.getMessage(), HexColor.Colors.RED), true);
            }
        }
    }

    private void end() {
        try {
            reader.getHistory().save();
        } catch (IOException ignored) {}

        if (autoRestartMonitor != null) {
            autoRestartMonitor.stopMonitoring();
        }

        if (screenManager != null) {
            screenManager.cleanup();
        }

        if (processManager != null) {
            processManager.shutdown();
        }

        if (apiServer != null && apiServer.isRunning()) {
            printer.println(HexColor.colorText("Stopping API server...", HexColor.Colors.ORANGE), true);
            apiServer.stop();
        }

        printer.println(HexColor.colorText("Console exited!", HexColor.Colors.GREEN), true);
        terminal.flush();
    }

    public LineReader getReader() {
        return reader;
    }

    public Completer getCompleter() {
        return completer;
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public List<String> getCommands() {
        return commands;
    }
}
