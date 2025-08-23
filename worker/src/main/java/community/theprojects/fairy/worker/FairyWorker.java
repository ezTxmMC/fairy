package community.theprojects.fairy.worker;

import community.theprojects.fairy.api.IWorker;
import community.theprojects.fairy.api.config.IConfig;
import community.theprojects.fairy.util.json.JsonFileHandler;
import community.theprojects.fairy.worker.config.WorkerConfig;
import community.theprojects.fairy.worker.network.WorkerClient;

import java.io.IOException;
import java.util.UUID;

public class FairyWorker implements IWorker {
    private static IWorker instance;
    private IConfig workerConfig;
    private final UUID id;
    private final String name;
    private final String description;
    private final String version;
    private WorkerClient workerClient;

    public FairyWorker(String name, String description) {
        instance = this;
        try {
            this.workerConfig = JsonFileHandler.readFromFile("config.json", WorkerConfig.class);
        } catch (IOException e) {
            try {
                this.workerConfig = new WorkerConfig();
                JsonFileHandler.writeToFile(this.workerConfig, "config.json", true);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        this.id = UUID.fromString(((WorkerConfig) workerConfig).getId());
        this.name = name;
        this.description = description;
        this.version = "1.0.0_DEV+1";
    }

    @Override
    public void init() {
        this.workerClient = new WorkerClient();
    }

    @Override
    public void start() {
        this.workerClient.start();
    }

    @Override
    public void stop() {
        this.workerClient.interrupt();
    }

    @Override
    public IConfig getConfig() {
        return null;
    }

    @Override
    public UUID getId() {
        return null;
    }

    @Override
    public String getName() {
        return "";
    }
}
