package community.theprojects.fairy.process;

import community.theprojects.fairy.console.HexColor;
import community.theprojects.fairy.console.Printer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JavaProcessRunner {

    private final InteractiveProcessManager processManager;
    private final Printer printer;

    public JavaProcessRunner(InteractiveProcessManager processManager, Printer printer) {
        this.processManager = processManager;
        this.printer = printer;
    }

    public boolean runJar(String processId, String workingDirectory, String jarFile, String... args) {
        return runJar(processId, workingDirectory, jarFile, List.of(args));
    }

    public boolean runJar(String processId, String workingDirectory, String jarFile, List<String> args) {
        Path jarPath = Paths.get(workingDirectory, jarFile);

        if (!jarPath.toFile().exists()) {
            printer.println(HexColor.colorText("JAR file not found: " + jarPath, HexColor.Colors.RED), true);
            return false;
        }

        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-jar");
        command.add(jarFile);
        command.addAll(args);

        return startJavaProcess(processId, workingDirectory, command);
    }

    public boolean runClass(String processId, String workingDirectory, String mainClass, String... args) {
        return runClass(processId, workingDirectory, mainClass, List.of(args));
    }

    public boolean runClass(String processId, String workingDirectory, String mainClass, List<String> args) {
        List<String> command = new ArrayList<>();
        command.add("java");
        command.add(mainClass);
        command.addAll(args);

        return startJavaProcess(processId, workingDirectory, command);
    }

    public boolean runWithClasspath(String processId, String workingDirectory, String classpath, String mainClass, String... args) {
        return runWithClasspath(processId, workingDirectory, classpath, mainClass, List.of(args));
    }

    public boolean runWithClasspath(String processId, String workingDirectory, String classpath, String mainClass, List<String> args) {
        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-cp");
        command.add(classpath);
        command.add(mainClass);
        command.addAll(args);

        return startJavaProcess(processId, workingDirectory, command);
    }

    public boolean runSpringBoot(String processId, String workingDirectory, String jarFile, String profile) {
        return runSpringBoot(processId, workingDirectory, jarFile, profile, new String[]{});
    }

    public boolean runSpringBoot(String processId, String workingDirectory, String jarFile, String profile, String... additionalArgs) {
        List<String> args = new ArrayList<>();
        args.add("--spring.profiles.active=" + profile);
        args.addAll(Arrays.asList(additionalArgs));

        return runJar(processId, workingDirectory, jarFile, args);
    }

    public boolean runWithJvmOptions(String processId, String workingDirectory, String mainClass, List<String> jvmOptions, String... args) {
        return runWithJvmOptions(processId, workingDirectory, mainClass, jvmOptions, List.of(args));
    }

    public boolean runWithJvmOptions(String processId, String workingDirectory, String mainClass, List<String> jvmOptions, List<String> args) {
        List<String> command = new ArrayList<>();
        command.add("java");
        command.addAll(jvmOptions);
        command.add(mainClass);
        command.addAll(args);

        return startJavaProcess(processId, workingDirectory, command);
    }

    public boolean compileAndRun(String processId, String workingDirectory, String javaFile, String... args) {
        return compileAndRun(processId, workingDirectory, javaFile, List.of(args));
    }

    public boolean compileAndRun(String processId, String workingDirectory, String javaFile, List<String> args) {
        Path javaPath = Paths.get(workingDirectory, javaFile);

        if (!javaPath.toFile().exists()) {
            printer.println(HexColor.colorText("Java file not found: " + javaPath, HexColor.Colors.RED), true);
            return false;
        }

        String className = javaFile.replaceAll("\\.java$", "");

        printer.println(HexColor.colorText("Compiling " + javaFile + "...", HexColor.Colors.YELLOW), true);

        try {
            ProcessBuilderWrapper compiler = ProcessBuilderWrapper.create("javac", javaFile)
                    .workingDirectory(new File(workingDirectory));

            Process compileProcess = compiler.start();
            int compileResult = compileProcess.waitFor();

            if (compileResult != 0) {
                printer.println(HexColor.colorText("Compilation failed with exit code: " + compileResult, HexColor.Colors.RED), true);
                return false;
            }

            printer.println(HexColor.colorText("Compilation successful. Running " + className + "...", HexColor.Colors.GREEN), true);

            return runClass(processId, workingDirectory, className, args);

        } catch (Exception e) {
            printer.println(HexColor.colorText("Compilation error: " + e.getMessage(), HexColor.Colors.RED), true);
            return false;
        }
    }

    private boolean startJavaProcess(String processId, String workingDirectory, List<String> command) {
        try {
            ProcessBuilderWrapper builder = ProcessBuilderWrapper.create(command)
                    .workingDirectory(new File(workingDirectory))
                    .redirectErrorStream(false);

            Process process = builder.start();

            return processManager.startInteractiveProcess(processId, process, Paths.get(workingDirectory));

        } catch (Exception e) {
            printer.println(HexColor.colorText("Failed to start Java process: " + e.getMessage(), HexColor.Colors.RED), true);
            return false;
        }
    }

    public static class JvmOptions {
        public static String heapSize(String size) {
            return "-Xmx" + size;
        }

        public static String initialHeapSize(String size) {
            return "-Xms" + size;
        }

        public static String enableRemoteDebugging(int port) {
            return "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=" + port;
        }

        public static String enableJmx(int port) {
            return "-Dcom.sun.management.jmxremote.port=" + port;
        }

        public static String systemProperty(String key, String value) {
            return "-D" + key + "=" + value;
        }

        public static String enableAssertions() {
            return "-ea";
        }

        public static String verboseGc() {
            return "-verbose:gc";
        }
    }
}
