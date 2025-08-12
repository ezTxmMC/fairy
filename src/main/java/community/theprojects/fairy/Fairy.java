package community.theprojects.fairy;

import community.theprojects.fairy.console.Console;
import community.theprojects.fairy.runtime.Runtime;

public class Fairy {
    private Runtime runtime;
    private Console console;

    public Fairy() {
        this.initialize();
        this.start();
    }

    public void initialize() {
        this.runtime = new Runtime();
        this.console = new Console();
        this.console.create();
    }

    public void start() {
        this.runtime.setRunning(true);
        this.console.checker();
    }

    public Runtime getRuntime() {
        return runtime;
    }
}
