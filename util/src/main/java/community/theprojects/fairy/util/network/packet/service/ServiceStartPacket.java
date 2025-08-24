package community.theprojects.fairy.util.network.packet.service;

import com.nexoscript.nexonet.api.packet.Packet;

public class ServiceStartPacket extends Packet {
    private String serviceName;
    private String path;

    public ServiceStartPacket() {
        super("SERVICE_START");
    }

    public ServiceStartPacket(String serviceName, String path) {
        super("SERVICE_START");
        this.serviceName = serviceName;
        this.path = path;
    }

    public String getServiceName() {
        return this.serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String toString() {
        return "ServiceStartPacket{serviceName='" + this.serviceName + "', path='" + this.path + "'}";
    }
}
