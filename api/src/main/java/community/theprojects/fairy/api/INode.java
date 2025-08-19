package community.theprojects.fairy.api;

import java.util.UUID;

public interface INode {

    void init();
    void start();
    void stop();
    UUID getId();
    String getName();
    String getDescription();
    String getVersion();

}
