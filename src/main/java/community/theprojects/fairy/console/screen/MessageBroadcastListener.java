package community.theprojects.fairy.console.screen;

import java.awt.*;

public interface MessageBroadcastListener {
    void onMessage(String serverId, String message, TrayIcon.MessageType type);
}
