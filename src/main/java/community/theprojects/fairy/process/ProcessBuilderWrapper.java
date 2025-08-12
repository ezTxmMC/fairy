package community.theprojects.fairy.process;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProcessBuilderWrapper {

    private final ProcessBuilder processBuilder;

    public ProcessBuilderWrapper(String... command) {
        this.processBuilder = new ProcessBuilder(command);
    }

    public ProcessBuilderWrapper(List<String> command) {
        this.processBuilder = new ProcessBuilder(command);
    }

    public ProcessBuilderWrapper workingDirectory(String directory) {
        return workingDirectory(new File(directory));
    }

    public ProcessBuilderWrapper workingDirectory(File directory) {
        processBuilder.directory(directory);
        return this;
    }

    public ProcessBuilderWrapper environment(String key, String value) {
        processBuilder.environment().put(key, value);
        return this;
    }

    public ProcessBuilderWrapper environment(Map<String, String> env) {
        processBuilder.environment().putAll(env);
        return this;
    }

    public ProcessBuilderWrapper redirectErrorStream(boolean redirect) {
        processBuilder.redirectErrorStream(redirect);
        return this;
    }

    public ProcessBuilderWrapper redirectOutput(ProcessBuilder.Redirect redirect) {
        processBuilder.redirectOutput(redirect);
        return this;
    }

    public ProcessBuilderWrapper redirectError(ProcessBuilder.Redirect redirect) {
        processBuilder.redirectError(redirect);
        return this;
    }

    public ProcessBuilderWrapper redirectInput(ProcessBuilder.Redirect redirect) {
        processBuilder.redirectInput(redirect);
        return this;
    }

    public ProcessBuilderWrapper inheritIO() {
        processBuilder.inheritIO();
        return this;
    }

    public ProcessBuilderWrapper addArgument(String argument) {
        List<String> command = new ArrayList<>(processBuilder.command());
        command.add(argument);
        processBuilder.command(command);
        return this;
    }

    public ProcessBuilderWrapper addArguments(String... arguments) {
        List<String> command = new ArrayList<>(processBuilder.command());
        command.addAll(List.of(arguments));
        processBuilder.command(command);
        return this;
    }

    public ProcessBuilderWrapper addArguments(List<String> arguments) {
        List<String> command = new ArrayList<>(processBuilder.command());
        command.addAll(arguments);
        processBuilder.command(command);
        return this;
    }

    public ProcessBuilderWrapper clearEnvironment() {
        processBuilder.environment().clear();
        return this;
    }

    public ProcessBuilderWrapper removeEnvironment(String key) {
        processBuilder.environment().remove(key);
        return this;
    }

    public Process start() throws IOException {
        return processBuilder.start();
    }

    public ProcessBuilder getProcessBuilder() {
        return processBuilder;
    }

    public List<String> getCommand() {
        return new ArrayList<>(processBuilder.command());
    }

    public File getWorkingDirectory() {
        return processBuilder.directory();
    }

    public Map<String, String> getEnvironment() {
        return processBuilder.environment();
    }

    public static ProcessBuilderWrapper create(String... command) {
        return new ProcessBuilderWrapper(command);
    }

    public static ProcessBuilderWrapper create(List<String> command) {
        return new ProcessBuilderWrapper(command);
    }

    public static ProcessBuilderWrapper shell(String command) {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return new ProcessBuilderWrapper("cmd", "/c", command);
        } else {
            return new ProcessBuilderWrapper("sh", "-c", command);
        }
    }

    public static ProcessBuilderWrapper python(String script) {
        return new ProcessBuilderWrapper("python", script);
    }

    public static ProcessBuilderWrapper java(String className) {
        return new ProcessBuilderWrapper("java", className);
    }

    public static ProcessBuilderWrapper node(String script) {
        return new ProcessBuilderWrapper("node", script);
    }
}
