package community.theprojects.fairy.node;

import community.theprojects.fairy.api.INode;
import community.theprojects.fairy.api.command.ICommandHandler;
import community.theprojects.fairy.api.config.IConfig;
import community.theprojects.fairy.api.console.IConsole;
import community.theprojects.fairy.node.command.*;
import community.theprojects.fairy.node.config.JsonFileHandler;
import community.theprojects.fairy.node.config.NodeConfig;
import community.theprojects.fairy.node.config.TemplatesConfig;
import community.theprojects.fairy.node.console.Console;

import java.io.IOException;
import java.util.UUID;

public final class FairyNode implements INode {
    private static FairyNode instance;
    private IConfig nodeConfig;
    private IConfig templatesConfig;
    private final UUID id;
    private final String name;
    private final String description;
    private final String version;
    private IConsole console;
    private ICommandHandler commandHandler;

    public FairyNode(String name, String description) {
        instance = this;
        try {
            this.nodeConfig = JsonFileHandler.readFromFile("config.json", NodeConfig.class);
        } catch (IOException e) {
            try {
                this.nodeConfig = new NodeConfig();
                JsonFileHandler.writeToFile(this.nodeConfig, "config.json", true);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        try {
            this.templatesConfig = JsonFileHandler.readFromFile("storage/templates.json", TemplatesConfig.class);
        } catch (IOException e) {
            try {
                this.templatesConfig = new TemplatesConfig();
                JsonFileHandler.writeToFile(this.templatesConfig, "storage/templates.json", true);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        this.id = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.version = "1.0.0_DEV+1";
    }

    @Override
    public void init() {
        this.console = new Console();
        this.commandHandler = new CommandHandler();
        this.commandHandler.addCommand("exit", new ExitCommand("Shutting down node."));
        this.commandHandler.addCommand("info", new InfoCommand("Information about this node."));
        this.commandHandler.addCommand("help", new HelpCommand("Shows this help menu."));
        this.commandHandler.addCommand("template", new TemplateCommand("Create and delete templates."));
    }

    @Override
    public void start() {
        this.console.start();
    }

    @Override
    public void stop() {
        this.commandHandler = null;
        this.console.stop();
        this.console = null;
        System.exit(0);
    }

    @Override
    public IConfig getConfig() {
        return this.nodeConfig;
    }

    @Override
    public IConfig getTemplatesConfig() {
        return this.templatesConfig;
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

    @Override
    public IConsole getConsole() {
        return this.console;
    }

    @Override
    public ICommandHandler getCommandHandler() {
        return this.commandHandler;
    }

    public static INode getInstance() {
        return instance;
    }
}
