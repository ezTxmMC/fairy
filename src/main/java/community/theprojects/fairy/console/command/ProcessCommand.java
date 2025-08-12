package community.theprojects.fairy.console.command;

import community.theprojects.fairy.console.HexColor;
import community.theprojects.fairy.console.Printer;
import community.theprojects.fairy.process.InteractiveProcessManager;
import community.theprojects.fairy.process.JavaProcessRunner;

import java.util.Arrays;
import java.util.List;

public record ProcessCommand(Printer printer, InteractiveProcessManager processManager, JavaProcessRunner javaRunner, String[] args) implements Command {

    @Override
    public void execute() {
        if (args.length < 2) {
            showProcessHelp();
            return;
        }

        String subCommand = args[1];

        switch (subCommand.toLowerCase()) {
            case "list" -> processManager.listProcesses();
            case "stop" -> handleStop();
            case "attach" -> handleAttach();
            case "send" -> handleSend();
            case "java" -> handleJava();
            case "jar" -> handleJar();
            case "compile" -> handleCompile();
            default -> {
                printer.println(HexColor.colorText("Unknown process command: " + subCommand, HexColor.Colors.RED), true);
                showProcessHelp();
            }
        }
    }

    private void handleStop() {
        if (args.length < 3) {
            printer.println(HexColor.colorText("Usage: process stop <process-id>", HexColor.Colors.YELLOW), true);
            return;
        }

        processManager.stopProcess(args[2]);
    }

    private void handleAttach() {
        if (args.length < 3) {
            printer.println(HexColor.colorText("Usage: process attach <process-id>", HexColor.Colors.YELLOW), true);
            return;
        }

        processManager.attachToProcess(args[2]);
    }

    private void handleSend() {
        if (args.length < 4) {
            printer.println(HexColor.colorText("Usage: process send <process-id> <input>", HexColor.Colors.YELLOW), true);
            return;
        }

        String processId = args[2];
        String input = String.join(" ", Arrays.copyOfRange(args, 3, args.length));

        processManager.sendInput(processId, input);
    }

    private void handleJava() {
        if (args.length < 5) {
            printer.println(HexColor.colorText("Usage: process java <process-id> <directory> <main-class> [args...]", HexColor.Colors.YELLOW), true);
            return;
        }

        String processId = args[2];
        String directory = args[3];
        String mainClass = args[4];
        String[] javaArgs = args.length > 5 ? Arrays.copyOfRange(args, 5, args.length) : new String[0];

        javaRunner.runClass(processId, directory, mainClass, javaArgs);
    }

    private void handleJar() {
        if (args.length < 5) {
            printer.println(HexColor.colorText("Usage: process jar <process-id> <directory> <jar-file> [args...]", HexColor.Colors.YELLOW), true);
            return;
        }

        String processId = args[2];
        String directory = args[3];
        String jarFile = args[4];
        String[] jarArgs = args.length > 5 ? Arrays.copyOfRange(args, 5, args.length) : new String[0];

        javaRunner.runJar(processId, directory, jarFile, jarArgs);
    }

    private void handleCompile() {
        if (args.length < 5) {
            printer.println(HexColor.colorText("Usage: process compile <process-id> <directory> <java-file> [args...]", HexColor.Colors.YELLOW), true);
            return;
        }

        String processId = args[2];
        String directory = args[3];
        String javaFile = args[4];
        String[] runArgs = args.length > 5 ? Arrays.copyOfRange(args, 5, args.length) : new String[0];

        javaRunner.compileAndRun(processId, directory, javaFile, runArgs);
    }

    private void showProcessHelp() {
        printer.println(HexColor.colorText("Process Management Commands:", HexColor.Colors.CYAN), true);
        printer.println(HexColor.colorText("  process list - List all active processes", HexColor.Colors.WHITE), true);
        printer.println(HexColor.colorText("  process stop <id> - Stop process", HexColor.Colors.WHITE), true);
        printer.println(HexColor.colorText("  process attach <id> - Attach to process for interactive input", HexColor.Colors.WHITE), true);
        printer.println(HexColor.colorText("  process send <id> <input> - Send input to process", HexColor.Colors.WHITE), true);
        printer.println(HexColor.colorText("  process java <id> <dir> <class> [args] - Run Java class", HexColor.Colors.WHITE), true);
        printer.println(HexColor.colorText("  process jar <id> <dir> <jar> [args] - Run JAR file", HexColor.Colors.WHITE), true);
        printer.println(HexColor.colorText("  process compile <id> <dir> <file> [args] - Compile and run Java file", HexColor.Colors.WHITE), true);
    }
}
