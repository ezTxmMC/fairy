package community.theprojects.fairy.node.group;

import community.theprojects.fairy.api.config.IConfig;
import community.theprojects.fairy.api.group.IGroup;
import community.theprojects.fairy.api.group.IGroupTemplate;
import community.theprojects.fairy.api.group.IType;
import community.theprojects.fairy.api.group.ServerType;
import community.theprojects.fairy.api.util.JavaVersion;

public record ServerGroup(String id, String name, String description, int minimumMemory, int maximumMemory, int maxPlayers, boolean staticServices, ServerType serverType, JavaVersion javaVersion, IGroupTemplate groupTemplate) implements IGroup, IConfig {

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public int getMinimumMemory() {
        return this.minimumMemory;
    }

    @Override
    public int getMaximumMemory() {
        return this.maximumMemory;
    }

    @Override
    public int getMaxPlayers() {
        return this.maxPlayers;
    }

    @Override
    public boolean hasStaticServices() {
        return this.staticServices;
    }

    @Override
    public IType getType() {
        return this.serverType;
    }

    @Override
    public JavaVersion getJavaVersion() {
        return this.javaVersion;
    }

    @Override
    public IGroupTemplate getTemplate() {
        return this.groupTemplate;
    }
}
