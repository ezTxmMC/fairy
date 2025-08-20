package community.theprojects.fairy.node.group;

import community.theprojects.fairy.api.config.IConfig;
import community.theprojects.fairy.api.group.*;
import community.theprojects.fairy.api.util.JavaVersion;

public class ProxyGroup implements IGroup, IConfig {
    private final String id;
    private final String name;
    private final String description;
    private final int minimumMemory;
    private final int maximumMemory;
    private final int maxPlayers;
    private final boolean staticServices;
    private final ProxyType proxyType;
    private final JavaVersion javaVersion;
    private final IGroupTemplate groupTemplate;

    public ProxyGroup(String id, String name, String description, int minimumMemory, int maximumMemory, int maxPlayers, boolean staticServices, ProxyType proxyType, JavaVersion javaVersion, IGroupTemplate groupTemplate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.minimumMemory = minimumMemory;
        this.maximumMemory = maximumMemory;
        this.maxPlayers = maxPlayers;
        this.staticServices = staticServices;
        this.proxyType = proxyType;
        this.javaVersion = javaVersion;
        this.groupTemplate = groupTemplate;
    }

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
        return this.proxyType;
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
