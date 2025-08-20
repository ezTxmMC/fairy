package community.theprojects.fairy.node.config;

import community.theprojects.fairy.api.config.IConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class NodeConfig implements IConfig {
    private final String id;
    private final String host;
    private final int port;
    private final HashMap<String, Object> cluster;

    public NodeConfig() {
        this.id = UUID.randomUUID().toString();
        this.host = "127.0.0.1";
        this.port = 8080;
        this.cluster = new HashMap<>();
        this.cluster.put("enabled", false);
        this.cluster.put("nodes", new ArrayList<>());
    }

    @Override
    public String toString() {
        return "NodeConfig{id=" + id + ", host=" + host + ", port=" + port + ", cluster=" + cluster + '}';
    }
}
