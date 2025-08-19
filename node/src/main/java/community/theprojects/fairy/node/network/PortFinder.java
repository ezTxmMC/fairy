package community.theprojects.fairy.node.network;

import java.io.IOException;
import java.net.ServerSocket;

public class PortFinder {

    private static boolean isPortAvailable(int port) {
        try (java.net.ServerSocket socket = new ServerSocket(port)) {
            socket.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static int findAvailablePort(int startPort, int endPort) {
        for (int port = startPort; port <= endPort; port++) {
            if (isPortAvailable(port)) {
                return port;
            }
        }
        return -1;
    }
}
