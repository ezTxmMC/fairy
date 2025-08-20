package community.theprojects.fairy.api.service;

import java.util.UUID;

public interface IService {

    UUID getId();
    String getName();
    String getDescription();
    String getGroup();
    Process getProcess();

}
