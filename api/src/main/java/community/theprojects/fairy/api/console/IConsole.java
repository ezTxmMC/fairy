package community.theprojects.fairy.api.console;

public interface IConsole {

    void start();
    void stop();
    void handleCommand(String cmd, String[] parts);
    IPrinter getPrinter();

}
