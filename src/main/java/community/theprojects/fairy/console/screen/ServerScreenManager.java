package community.theprojects.fairy.console.screen;

import community.theprojects.fairy.console.HexColor;
import community.theprojects.fairy.console.Printer;
import community.theprojects.fairy.process.InteractiveProcessManager;
import org.jline.reader.LineReader;

import java.awt.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerScreenManager {

    private final InteractiveProcessManager processManager;
    private final Printer printer;
    private final LineReader lineReader;
    private final Map<String, ServerScreen> screens = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<MessageBroadcastListener> broadcastListeners = new CopyOnWriteArrayList<>();

    private String currentScreen = null; // null means main console

    public ServerScreenManager(InteractiveProcessManager processManager, Printer printer, LineReader lineReader) {
        this.processManager = processManager;
        this.printer = printer;
        this.lineReader = lineReader;
    }

    public void createScreen(String serverId) {
        if (screens.containsKey(serverId)) {
            printer.println(HexColor.colorText("Screen for server '" + serverId + "' already exists", HexColor.Colors.YELLOW), true);
            return;
        }

        if (!processManager.isProcessRunning(serverId)) {
            printer.println(HexColor.colorText("Cannot create screen: server '" + serverId + "' is not running", HexColor.Colors.RED), true);
            return;
        }

        ServerScreen screen = new ServerScreen(serverId, this, printer);
        screens.put(serverId, screen);

        // Register as message listener for this server
        addBroadcastListener(screen);

        printer.println(HexColor.colorText("Created screen for server: " + serverId, HexColor.Colors.GREEN), true);
    }

    public void removeScreen(String serverId) {
        ServerScreen screen = screens.remove(serverId);
        if (screen != null) {
            removeBroadcastListener(screen);

            // If we're currently in this screen, switch back to main
            if (serverId.equals(currentScreen)) {
                switchToMain();
            }

            printer.println(HexColor.colorText("Removed screen for server: " + serverId, HexColor.Colors.ORANGE), true);
        }
    }

    public boolean switchToServer(String serverId) {
        if (!screens.containsKey(serverId)) {
            printer.println(HexColor.colorText("No screen exists for server: " + serverId, HexColor.Colors.RED), true);
            printer.println(HexColor.colorText("Use 'screen create " + serverId + "' first", HexColor.Colors.GRAY), true);
            return false;
        }

        if (!processManager.isProcessRunning(serverId)) {
            printer.println(HexColor.colorText("Cannot switch to screen: server '" + serverId + "' is not running", HexColor.Colors.RED), true);
            return false;
        }

        String previousScreen = currentScreen;
        currentScreen = serverId;

        // Clear screen and show server info
        printer.println("\033[2J\033[H", false); // Clear screen and move cursor to top
        printer.println(HexColor.colorText("═══════════════════════════════════════", HexColor.Colors.CYAN), true);
        printer.println(HexColor.colorText("  Fairy Screen - Server: " + serverId, HexColor.Colors.WHITE), true);
        printer.println(HexColor.colorText("  Press Ctrl+A then D to detach", HexColor.Colors.GRAY), true);
        printer.println(HexColor.colorText("  Type 'screen main' to return to main console", HexColor.Colors.GRAY), true);
        printer.println(HexColor.colorText("═══════════════════════════════════════", HexColor.Colors.CYAN), true);

        // Show recent server messages
        ServerScreen screen = screens.get(serverId);
        if (screen != null) {
            screen.showRecentMessages(10);
        }

        if (previousScreen != null) {
            printer.println(HexColor.colorText("Switched from " + previousScreen + " to " + serverId, HexColor.Colors.GREEN), true);
        } else {
            printer.println(HexColor.colorText("Switched from main console to " + serverId, HexColor.Colors.GREEN), true);
        }

        return true;
    }

    public void switchToMain() {
        if (currentScreen == null) {
            printer.println(HexColor.colorText("Already in main console", HexColor.Colors.YELLOW), true);
            return;
        }

        String previousScreen = currentScreen;
        currentScreen = null;

        // Clear screen and show main console
        printer.println("\033[2J\033[H", false); // Clear screen and move cursor to top
        printer.println(HexColor.colorText("═══════════════════════════════════════", HexColor.Colors.GREEN), true);
        printer.println(HexColor.colorText("  Fairy Main Console", HexColor.Colors.WHITE), true);
        printer.println(HexColor.colorText("  Use 'screen list' to see available server screens", HexColor.Colors.GRAY), true);
        printer.println(HexColor.colorText("═══════════════════════════════════════", HexColor.Colors.GREEN), true);

        printer.println(HexColor.colorText("Switched from " + previousScreen + " to main console", HexColor.Colors.GREEN), true);
    }

    public boolean sendInputToCurrentScreen(String input) {
        if (currentScreen == null) {
            return false; // Not in a server screen
        }

        // Handle special screen commands
        if (input.startsWith("screen ")) {
            handleScreenCommand(input.substring(7));
            return true;
        }

        // Handle detach sequence (Ctrl+A D simulation)
        if (input.equals("exit") || input.equals("detach")) {
            switchToMain();
            return true;
        }

        // Send input to the server
        boolean sent = processManager.sendInput(currentScreen, input);
        if (sent) {
            // Echo the command in the current screen
            printer.println(HexColor.colorText("[INPUT] " + input, HexColor.Colors.BLUE), true);
        }

        return sent;
    }

    private void handleScreenCommand(String command) {
        String[] parts = command.split("\\s+");
        if (parts.length == 0) return;

        String subCommand = parts[0];

        switch (subCommand.toLowerCase()) {
            case "main" -> switchToMain();
            case "list" -> listScreens();
            case "create" -> {
                if (parts.length > 1) {
                    createScreen(parts[1]);
                } else {
                    printer.println(HexColor.colorText("Usage: screen create <server-id>", HexColor.Colors.YELLOW), true);
                }
            }
            case "switch", "s" -> {
                if (parts.length > 1) {
                    switchToServer(parts[1]);
                } else {
                    printer.println(HexColor.colorText("Usage: screen switch <server-id>", HexColor.Colors.YELLOW), true);
                }
            }
            default -> printer.println(HexColor.colorText("Unknown screen command: " + subCommand, HexColor.Colors.RED), true);
        }
    }

    public void listScreens() {
        if (screens.isEmpty()) {
            printer.println(HexColor.colorText("No server screens exist", HexColor.Colors.GRAY), true);
            return;
        }

        printer.println(HexColor.colorText("Active Server Screens:", HexColor.Colors.CYAN), true);

        for (Map.Entry<String, ServerScreen> entry : screens.entrySet()) {
            String serverId = entry.getKey();
            boolean isRunning = processManager.isProcessRunning(serverId);
            boolean isCurrent = serverId.equals(currentScreen);

            String status = isRunning ? "RUNNING" : "STOPPED";
            String indicator = isCurrent ? " (current)" : "";
            String color = isRunning ? HexColor.Colors.GREEN : HexColor.Colors.RED;

            printer.println(HexColor.colorText("  " + serverId + ": " + status + indicator, color), true);
        }

        String currentInfo = currentScreen != null ? currentScreen : "main console";
        printer.println(HexColor.colorText("Currently viewing: " + currentInfo, HexColor.Colors.WHITE), true);
    }

    public void broadcastMessage(String serverId, String message, TrayIcon.MessageType type) {
        // Broadcast only to registered listeners (server screens)
        for (MessageBroadcastListener listener : broadcastListeners) {
            listener.onMessage(serverId, message, type);
        }
    }

    private String getColorForMessageType(TrayIcon.MessageType type) {
        return switch (type) {
            case ERROR -> HexColor.Colors.RED;
            case WARNING -> HexColor.Colors.YELLOW;
            case INFO -> HexColor.Colors.WHITE;
            case NONE -> HexColor.Colors.GRAY;
        };
    }

    public String getCurrentScreen() {
        return currentScreen;
    }

    public boolean isInServerScreen() {
        return currentScreen != null;
    }

    public boolean isInMainConsole() {
        return currentScreen == null;
    }

    private void addBroadcastListener(MessageBroadcastListener listener) {
        broadcastListeners.add(listener);
    }

    private void removeBroadcastListener(MessageBroadcastListener listener) {
        broadcastListeners.remove(listener);
    }

    public void cleanup() {
        screens.clear();
        broadcastListeners.clear();
        currentScreen = null;
    }

    public void showStatus() {
        printer.println(HexColor.colorText("Screen System Status:", HexColor.Colors.CYAN), true);
        printer.println(HexColor.colorText("  Current Screen: " + (currentScreen != null ? currentScreen : "main console"), HexColor.Colors.WHITE), true);
        printer.println(HexColor.colorText("  Active Screens: " + screens.size(), HexColor.Colors.WHITE), true);

        if (!screens.isEmpty()) {
            printer.println(HexColor.colorText("  Screens: " + String.join(", ", screens.keySet()), HexColor.Colors.GRAY), true);
        }
    }
}
