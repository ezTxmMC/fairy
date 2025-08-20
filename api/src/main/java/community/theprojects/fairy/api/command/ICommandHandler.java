package community.theprojects.fairy.api.command;

import java.util.HashMap;

public interface ICommandHandler {

    void addCommand(String name, ICommand command);
    HashMap<String, ICommand> getCommands();

}
