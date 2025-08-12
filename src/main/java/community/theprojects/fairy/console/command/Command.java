package community.theprojects.fairy.console.command;

public interface Command {

    void execute();

    default String getName() {
        return this.getClass().getSimpleName().replace("Command", "").toLowerCase();
    }
}
