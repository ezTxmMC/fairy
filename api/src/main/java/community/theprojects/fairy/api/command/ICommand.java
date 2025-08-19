package community.theprojects.fairy.api.command;

public interface ICommand {

    void execute(String[] args);
    String getName();
    String getDescription();

}
