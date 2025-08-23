package community.theprojects.fairy.node.network;

import com.nexoscript.nexonet.server.Server;
import community.theprojects.fairy.node.FairyNode;
import community.theprojects.fairy.util.network.packet.service.ServiceStartPacket;

public class NodeServer extends Thread {
    private final Server server;

    public NodeServer() {
        try {
            Thread.sleep(1000);
            this.server = new Server(false);
            this.server.getPacketManager().registerPacketType("SERVICE_START", ServiceStartPacket.class);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        server.onServerSend(((iClientHandler, packet) -> {

        }));
        server.onServerReceived(((iClientHandler, packet) -> {

        }));
        server.onClientConnect((client -> {
            FairyNode.getInstance().getConsole().getPrinter().println("Client connected: " + client.getClientSocket().getInetAddress().getHostAddress(), true);
        }));
        server.onClientDisconnect((client -> {
            FairyNode.getInstance().getConsole().getPrinter().println("Client disconnected: " + client.getClientSocket().getInetAddress().getHostAddress(), true);
        }));
        server.start(2912);
    }
}
