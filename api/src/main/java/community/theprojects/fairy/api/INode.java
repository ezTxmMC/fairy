package community.theprojects.fairy.api;

import community.theprojects.fairy.api.command.ICommandHandler;
import community.theprojects.fairy.api.config.IConfig;
import community.theprojects.fairy.api.console.IConsole;

import java.util.UUID;

public interface INode {

    void init();
    void start();
    void stop();
    IConfig getConfig();
    IConfig getTemplatesConfig();
    UUID getId();
    String getName();
    String getDescription();
    String getVersion();
    IConsole getConsole();
    ICommandHandler getCommandHandler();

}
