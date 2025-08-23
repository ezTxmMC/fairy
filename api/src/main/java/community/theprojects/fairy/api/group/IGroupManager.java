package community.theprojects.fairy.api.group;

import community.theprojects.fairy.api.service.IService;

import java.util.List;

public interface IGroupManager {

    IService startService(String groupName);
    IService startServices(String groupName, int count);
    void stopService(String groupName, int number);
    void stopServices(String groupName, int... number);
    void shutdownGroup(String groupName);
    List<IService> getServices(String groupName);

}
