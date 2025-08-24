package community.theprojects.fairy.node.command;

import community.theprojects.fairy.api.command.ICommand;
import community.theprojects.fairy.api.command.ICommandHandler;

import java.util.HashMap;

public class CommandHandler implements ICommandHandler {
    private final HashMap<String, ICommand> commands;

    public CommandHandler() {
        this.commands = new HashMap<>();
    }

    @Override
    public void addCommand(String name, ICommand command) {
        this.commands.put(name, command);
    }

    @Override
    public HashMap<String, ICommand> getCommands() {
        return commands;
    }
}
