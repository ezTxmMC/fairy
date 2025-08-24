package community.theprojects.fairy.api;

import community.theprojects.fairy.api.config.IConfig;

import java.util.UUID;

public interface IWorker {

    void init();
    void start();
    void stop();
    IConfig getConfig();
    UUID getId();
    String getName();
    String getVersion();

}
