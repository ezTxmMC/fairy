package community.theprojects.fairy.node.group;

import community.theprojects.fairy.api.group.IGroupManager;
import community.theprojects.fairy.api.service.IService;

import java.util.List;

public class GroupManager implements IGroupManager {

    @Override
    public IService startService(String groupName) {
        return null;
    }

    @Override
    public IService startServices(String groupName, int count) {
        return null;
    }

    @Override
    public void shutdownGroup(String groupName) {

    }

    @Override
    public List<IService> getServices(String groupName) {
        return List.of();
    }
}
