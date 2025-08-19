package community.theprojects.fairy.node;

import community.theprojects.fairy.api.INode;
import community.theprojects.fairy.node.console.Console;

import java.util.UUID;

public final class FairyNode implements INode {
    private final UUID id;
    private final String name;
    private final String description;
    private final String version;
    private static FairyNode instance;
    private Console console;

    public FairyNode(String name, String description) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.version = "1.0.0_DEV+1";
        instance = this;
    }

    @Override
    public void init() {
        this.console = new Console();
    }

    @Override
    public void start() {
        this.console.start();
    }

    @Override
    public void stop() {

    }

    @Override
    public UUID getId() {
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
    public String getVersion() {
        return this.version;
    }

    public Console getConsole() {
        return console;
    }

    public static INode getInstance() {
        return instance;
    }
}
