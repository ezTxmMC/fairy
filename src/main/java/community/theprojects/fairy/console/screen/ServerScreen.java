package community.theprojects.fairy.console.screen;

import community.theprojects.fairy.console.HexColor;
import community.theprojects.fairy.console.Printer;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ServerScreen implements MessageBroadcastListener {

    private final String serverId;
    private final ServerScreenManager screenManager;
    private final Printer printer;
    private final ConcurrentLinkedQueue<ScreenMessage> messageHistory = new ConcurrentLinkedQueue<>();

    private static final int MAX_HISTORY_SIZE = 100;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public ServerScreen(String serverId, ServerScreenManager screenManager, Printer printer) {
        this.serverId = serverId;
        this.screenManager = screenManager;
        this.printer = printer;
    }

    @Override
    public void onMessage(String messageServerId, String message, TrayIcon.MessageType type) {
        // Only handle messages for our server
        if (!serverId.equals(messageServerId)) {
            return;
        }

        // Convert TrayIcon.MessageType to internal MessageType
        MessageType internalType = mapToInternalMessageType(type);

        // Add to message history
        addToHistory(message, internalType);

        // If this screen is currently active, display the message
        if (serverId.equals(screenManager.getCurrentScreen())) {
            displayMessage(message, internalType);
        }
    }

    private MessageType mapToInternalMessageType(TrayIcon.MessageType trayType) {
        return switch (trayType) {
            case ERROR -> MessageType.SERVER_ERROR;
            case WARNING -> MessageType.SYSTEM_MESSAGE;
            case INFO -> MessageType.SERVER_OUTPUT;
            case NONE -> MessageType.SERVER_OUTPUT;
        };
    }

    private void addToHistory(String message, MessageType type) {
        ScreenMessage screenMessage = new ScreenMessage(message, type, LocalDateTime.now());
        messageHistory.offer(screenMessage);

        // Keep history size manageable
        while (messageHistory.size() > MAX_HISTORY_SIZE) {
            messageHistory.poll();
        }
    }

    private void displayMessage(String message, MessageType type) {
        String timestamp = LocalDateTime.now().format(TIME_FORMAT);
        String coloredTimestamp = HexColor.colorText("[" + timestamp + "] ", HexColor.Colors.GRAY);
        String coloredMessage = HexColor.colorText(message, getColorForMessageType(type));

        printer.println(coloredTimestamp + coloredMessage, true);
    }

    private String getColorForMessageType(MessageType type) {
        return switch (type) {
            case SERVER_OUTPUT -> HexColor.Colors.WHITE;
            case SERVER_ERROR -> HexColor.Colors.RED;
            case PLAYER_JOIN -> HexColor.Colors.GREEN;
            case PLAYER_LEAVE -> HexColor.Colors.ORANGE;
            case PLAYER_CHAT -> HexColor.Colors.CYAN;
            case SERVER_COMMAND -> HexColor.Colors.BLUE;
            case SYSTEM_MESSAGE -> HexColor.Colors.YELLOW;
        };
    }

    public void showRecentMessages(int count) {
        if (messageHistory.isEmpty()) {
            return;
        }

        // Convert to array to avoid concurrent modification
        ScreenMessage[] messages = messageHistory.toArray(new ScreenMessage[0]);

        // Show last 'count' messages
        int start = Math.max(0, messages.length - count);

        printer.println(HexColor.colorText("--- Recent Messages ---", HexColor.Colors.GRAY), true);

        for (int i = start; i < messages.length; i++) {
            ScreenMessage msg = messages[i];
            String timestamp = msg.timestamp().format(TIME_FORMAT);
            String coloredTimestamp = HexColor.colorText("[" + timestamp + "] ", HexColor.Colors.GRAY);
            String coloredMessage = HexColor.colorText(msg.message(), getColorForMessageType(msg.type()));

            printer.println(coloredTimestamp + coloredMessage, true);
        }

        printer.println(HexColor.colorText("--- End Recent Messages ---", HexColor.Colors.GRAY), true);
    }

    public void clearHistory() {
        messageHistory.clear();
    }

    public int getHistorySize() {
        return messageHistory.size();
    }

    public String getServerId() {
        return serverId;
    }

    private record ScreenMessage(String message, MessageType type, LocalDateTime timestamp) {}
}
