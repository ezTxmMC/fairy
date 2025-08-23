package community.theprojects.fairy.worker.config;

import community.theprojects.fairy.api.config.IConfig;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.UUID;

public class WorkerConfig implements IConfig {
    private final String id;
    private final String host;
    private final int port;
    private final HashMap<String, Object> cluster;

    public WorkerConfig() {
        this.id = UUID.randomUUID().toString();
        this.host = "127.0.0.1";
        this.port = 8080;
        this.cluster = new HashMap<>();
        this.cluster.put("enabled", false);
        JSONObject node = new JSONObject();
        node.put("name", "node-1");
        node.put("host", host);
        node.put("port", port);
        this.cluster.put("node", node);
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "WorkerConfig{}";
    }
}
