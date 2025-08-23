package community.theprojects.fairy.worker.network;

import com.nexoscript.nexonet.client.Client;
import community.theprojects.fairy.util.network.packet.service.ServiceStartPacket;

public class WorkerClient extends Thread {
    private final Client client;

    public WorkerClient() {
        try {
            Thread.sleep(1100);
            this.client = new Client(false);
            this.client.getPacketManager().registerPacketType("SERVICE_START", ServiceStartPacket.class);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        client.onClientSend(((iClientHandler, packet) -> {

        }));
        client.onClientReceived(((iClientHandler, packet) -> {

        }));
        client.onClientConnect((client -> {

        }));
        client.onClientDisconnect((client -> {
            try {
                Thread.sleep(5000);
                client.connect("127.0.0.1", 2912);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }));
        client.connect("127.0.0.1", 2912);
    }
}
