package community.theprojects.fairy.process;

import community.theprojects.fairy.console.HexColor;
import community.theprojects.fairy.console.Printer;
import community.theprojects.fairy.console.screen.ServerScreenManager;
import org.jline.reader.LineReader;
import org.jline.reader.UserInterruptException;
import org.jline.reader.EndOfFileException;

import java.awt.*;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InteractiveProcessManager {

    private final Map<String, InteractiveProcess> activeProcesses = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final Printer printer;
    private final LineReader lineReader;
    private ServerScreenManager screenManager;

    public InteractiveProcessManager(Printer printer, LineReader lineReader) {
        this.printer = printer;
        this.lineReader = lineReader;
    }

    public void setScreenManager(ServerScreenManager screenManager) {
        this.screenManager = screenManager;
    }

    public boolean startJavaProcess(String processId, String workingDirectory, String mainClass, String... args) {
        return startJavaProcess(processId, workingDirectory, mainClass, List.of(args));
    }

    public boolean startJavaProcess(String processId, String workingDirectory, String mainClass, List<String> args) {
        if (activeProcesses.containsKey(processId)) {
            printer.println(HexColor.colorText("Process '" + processId + "' already exists.", HexColor.Colors.YELLOW), true);
            return false;
        }

        try {
            Path workDir = Paths.get(workingDirectory);
            if (!workDir.toFile().exists()) {
                printer.println(HexColor.colorText("Working directory does not exist: " + workingDirectory, HexColor.Colors.RED), true);
                return false;
            }

            ProcessBuilderWrapper builder = ProcessBuilderWrapper.java(mainClass)
                    .addArguments(args)
                    .workingDirectory(workDir.toFile())
                    .redirectErrorStream(false);

            Process process = builder.start();

            InteractiveProcess interactiveProcess = new InteractiveProcess(
                    processId, process, workDir, 
                    new BufferedWriter(new OutputStreamWriter(process.getOutputStream())),
                    new BufferedReader(new InputStreamReader(process.getInputStream())),
                    new BufferedReader(new InputStreamReader(process.getErrorStream()))
            );

            activeProcesses.put(processId, interactiveProcess);

            startOutputReaders(interactiveProcess);

            printer.println(HexColor.colorText("Java process '" + processId + "' started in " + workingDirectory, HexColor.Colors.GREEN), true);

            return true;

        } catch (IOException e) {
            printer.println(HexColor.colorText("Failed to start Java process: " + e.getMessage(), HexColor.Colors.RED), true);
            return false;
        }
    }

    public boolean sendInput(String processId, String input) {
        InteractiveProcess process = activeProcesses.get(processId);
        if (process == null) {
            printer.println(HexColor.colorText("Process '" + processId + "' not found.", HexColor.Colors.YELLOW), true);
            return false;
        }

        if (!process.process().isAlive()) {
            printer.println(HexColor.colorText("Process '" + processId + "' is not running.", HexColor.Colors.RED), true);
            return false;
        }

        try {
            process.inputWriter().write(input + System.lineSeparator());
            process.inputWriter().flush();
            return true;
        } catch (IOException e) {
            printer.println(HexColor.colorText("Failed to send input to process '" + processId + "': " + e.getMessage(), HexColor.Colors.RED), true);
            return false;
        }
    }

    public boolean attachToProcess(String processId) {
        InteractiveProcess process = activeProcesses.get(processId);
        if (process == null) {
            printer.println(HexColor.colorText("Process '" + processId + "' not found.", HexColor.Colors.YELLOW), true);
            return false;
        }

        if (!process.process().isAlive()) {
            printer.println(HexColor.colorText("Process '" + processId + "' is not running.", HexColor.Colors.RED), true);
            return false;
        }

        printer.println(HexColor.colorText("Attached to process '" + processId + "'. Type 'exit' to detach.", HexColor.Colors.GREEN), true);

        while (process.process().isAlive()) {
            try {
                String input = lineReader.readLine(HexColor.colorText("[" + processId + "] ", HexColor.Colors.BLUE));

                if (input == null || "exit".equals(input.trim())) {
                    printer.println(HexColor.colorText("Detached from process '" + processId + "'.", HexColor.Colors.ORANGE), true);
                    break;
                }

                sendInput(processId, input);

            } catch (org.jline.reader.UserInterruptException | org.jline.reader.EndOfFileException e) {
                printer.println(HexColor.colorText("Detached from process '" + processId + "'.", HexColor.Colors.ORANGE), true);
                break;

            } catch (Exception e) {
                printer.println(HexColor.colorText("Error in interactive mode: " + e.getMessage(), HexColor.Colors.RED), true);
                break;
            }
        }

        return true;
    }

    public boolean stopProcess(String processId) {
        InteractiveProcess process = activeProcesses.get(processId);
        if (process == null) {
            printer.println(HexColor.colorText("Process '" + processId + "' not found.", HexColor.Colors.YELLOW), true);
            return false;
        }

        try {
            process.inputWriter().close();
            process.outputReader().close();
            process.errorReader().close();
        } catch (IOException e) {
            printer.println(HexColor.colorText("Error closing streams: " + e.getMessage(), HexColor.Colors.YELLOW), true);
        }

        process.process().destroy();

        try {
            if (!process.process().waitFor(5, java.util.concurrent.TimeUnit.SECONDS)) {
                process.process().destroyForcibly();
                printer.println(HexColor.colorText("Process '" + processId + "' forcibly terminated.", HexColor.Colors.ORANGE), true);
            } else {
                printer.println(HexColor.colorText("Process '" + processId + "' stopped successfully.", HexColor.Colors.GREEN), true);
            }

            activeProcesses.remove(processId);
            return true;

        } catch (InterruptedException e) {
            printer.println(HexColor.colorText("Failed to stop process '" + processId + "': " + e.getMessage(), HexColor.Colors.RED), true);
            return false;
        }
    }

    public void listProcesses() {
        if (activeProcesses.isEmpty()) {
            printer.println(HexColor.colorText("No active processes.", HexColor.Colors.GRAY), true);
            return;
        }

        printer.println(HexColor.colorText("Active processes:", HexColor.Colors.CYAN), true);
        activeProcesses.forEach((id, process) -> {
            String status = process.process().isAlive() ? "RUNNING" : "STOPPED";
            String color = process.process().isAlive() ? HexColor.Colors.GREEN : HexColor.Colors.RED;

            printer.println(HexColor.colorText("  " + id + ": ", HexColor.Colors.WHITE) + 
                           HexColor.colorText(status, color) +
                           HexColor.colorText(" (WorkDir: " + process.workingDirectory() + ")", HexColor.Colors.GRAY), true);
        });
    }

    public boolean isProcessRunning(String processId) {
        InteractiveProcess process = activeProcesses.get(processId);
        return process != null && process.process().isAlive();
    }

    public boolean startInteractiveProcess(String processId, Process process, Path workingDirectory) {
        if (activeProcesses.containsKey(processId)) {
            printer.println(HexColor.colorText("Process '" + processId + "' already exists.", HexColor.Colors.YELLOW), true);
            return false;
        }

        try {
            InteractiveProcess interactiveProcess = new InteractiveProcess(
                    processId, process, workingDirectory,
                    new BufferedWriter(new OutputStreamWriter(process.getOutputStream())),
                    new BufferedReader(new InputStreamReader(process.getInputStream())),
                    new BufferedReader(new InputStreamReader(process.getErrorStream()))
            );

            activeProcesses.put(processId, interactiveProcess);
            startOutputReaders(interactiveProcess);

            printer.println(HexColor.colorText("Interactive process '" + processId + "' started in " + workingDirectory, HexColor.Colors.GREEN), true);
            return true;

        } catch (Exception e) {
            printer.println(HexColor.colorText("Failed to start interactive process: " + e.getMessage(), HexColor.Colors.RED), true);
            return false;
        }
    }

    public Map<String, Object> getRunningProcesses() {
        Map<String, Object> processes = new HashMap<>();

        activeProcesses.forEach((id, process) -> {
            Map<String, Object> processInfo = new HashMap<>();
            processInfo.put("id", id);
            processInfo.put("running", process.process().isAlive());
            processInfo.put("workingDirectory", process.workingDirectory().toString());
            processes.put(id, processInfo);
        });

        return processes;
    }

    public void shutdown() {
        for (String processId : activeProcesses.keySet()) {
            stopProcess(processId);
        }
        executorService.shutdown();
    }

    private void startOutputReaders(InteractiveProcess interactiveProcess) {
        // Use Virtual Threads for each process console
        Thread.ofVirtual().name("output-" + interactiveProcess.processId()).start(() -> {
            try (BufferedReader reader = interactiveProcess.outputReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Send to screen system instead of direct printing
                    if (screenManager != null) {
                        screenManager.broadcastMessage(interactiveProcess.processId(), line, TrayIcon.MessageType.INFO);
                    }
                }
            } catch (IOException e) {
                if (interactiveProcess.process().isAlive() && screenManager != null) {
                    screenManager.broadcastMessage(interactiveProcess.processId(), 
                        "Output reader error: " + e.getMessage(), TrayIcon.MessageType.ERROR);
                }
            }
        });

        Thread.ofVirtual().name("error-" + interactiveProcess.processId()).start(() -> {
            try (BufferedReader reader = interactiveProcess.errorReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Send error output to screen system
                    if (screenManager != null) {
                        screenManager.broadcastMessage(interactiveProcess.processId(), line, TrayIcon.MessageType.ERROR);
                    }
                }
            } catch (IOException e) {
                if (interactiveProcess.process().isAlive() && screenManager != null) {
                    screenManager.broadcastMessage(interactiveProcess.processId(), 
                        "Error reader error: " + e.getMessage(), TrayIcon.MessageType.ERROR);
                }
            }
        });

        Thread.ofVirtual().name("monitor-" + interactiveProcess.processId()).start(() -> {
            try {
                int exitCode = interactiveProcess.process().waitFor();
                activeProcesses.remove(interactiveProcess.processId());

                // Broadcast exit message to screen system
                if (screenManager != null) {
                    TrayIcon.MessageType messageType = exitCode == 0 ? TrayIcon.MessageType.INFO : TrayIcon.MessageType.ERROR;
                    screenManager.broadcastMessage(interactiveProcess.processId(), 
                        "Process exited with code: " + exitCode, messageType);
                }

            } catch (InterruptedException e) {
                if (screenManager != null) {
                    screenManager.broadcastMessage(interactiveProcess.processId(), 
                        "Process monitoring interrupted", TrayIcon.MessageType.WARNING);
                }
            }
        });
    }

    private record InteractiveProcess(
            String processId,
            Process process,
            Path workingDirectory,
            BufferedWriter inputWriter,
            BufferedReader outputReader,
            BufferedReader errorReader
    ) {}
}
