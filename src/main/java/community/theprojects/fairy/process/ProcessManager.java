package community.theprojects.fairy.process;

import community.theprojects.fairy.console.HexColor;
import community.theprojects.fairy.console.Printer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ProcessManager {

    private final Map<String, Process> processes = new ConcurrentHashMap<>();
    private final Printer printer;

    public ProcessManager(Printer printer) {
        this.printer = printer;
    }

    public ProcessResult executeCommand(String... command) {
        return executeCommand(List.of(command));
    }

    public ProcessResult executeCommand(List<String> command) {
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);

            Process process = builder.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();

            return new ProcessResult(exitCode, output.toString().trim(), null);

        } catch (IOException | InterruptedException e) {
            return new ProcessResult(-1, "", e.getMessage());
        }
    }

    public boolean startProcess(String processId, String... command) {
        return startProcess(processId, List.of(command));
    }

    public boolean startProcess(String processId, List<String> command) {
        if (processes.containsKey(processId)) {
            printer.println(HexColor.colorText("Process with ID '" + processId + "' already exists.", HexColor.Colors.YELLOW), true);
            return false;
        }

        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);

            Process process = builder.start();
            processes.put(processId, process);

            printer.println(HexColor.colorText("Process '" + processId + "' started successfully.", HexColor.Colors.GREEN), true);

            new Thread(() -> monitorProcess(processId, process)).start();

            return true;

        } catch (IOException e) {
            printer.println(HexColor.colorText("Failed to start process '" + processId + "': " + e.getMessage(), HexColor.Colors.RED), true);
            return false;
        }
    }

    public boolean stopProcess(String processId) {
        Process process = processes.get(processId);
        if (process == null) {
            printer.println(HexColor.colorText("Process with ID '" + processId + "' not found.", HexColor.Colors.YELLOW), true);
            return false;
        }

        process.destroy();

        try {
            if (!process.waitFor(5, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                printer.println(HexColor.colorText("Process '" + processId + "' forcibly terminated.", HexColor.Colors.ORANGE), true);
            } else {
                printer.println(HexColor.colorText("Process '" + processId + "' stopped successfully.", HexColor.Colors.GREEN), true);
            }

            processes.remove(processId);
            return true;

        } catch (InterruptedException e) {
            printer.println(HexColor.colorText("Failed to stop process '" + processId + "': " + e.getMessage(), HexColor.Colors.RED), true);
            return false;
        }
    }

    public void listProcesses() {
        if (processes.isEmpty()) {
            printer.println(HexColor.colorText("No running processes.", HexColor.Colors.GRAY), true);
            return;
        }

        printer.println(HexColor.colorText("Running processes:", HexColor.Colors.CYAN), true);
        processes.forEach((id, process) -> {
            String status = process.isAlive() ? "RUNNING" : "STOPPED";
            String color = process.isAlive() ? HexColor.Colors.GREEN : HexColor.Colors.RED;

            printer.println(HexColor.colorText("  " + id + ": ", HexColor.Colors.WHITE) + 
                           HexColor.colorText(status, color), true);
        });
    }

    public boolean isProcessRunning(String processId) {
        Process process = processes.get(processId);
        return process != null && process.isAlive();
    }

    public void killAllProcesses() {
        for (Map.Entry<String, Process> entry : processes.entrySet()) {
            Process process = entry.getValue();
            if (process.isAlive()) {
                process.destroyForcibly();
                printer.println(HexColor.colorText("Killed process: " + entry.getKey(), HexColor.Colors.ORANGE), true);
            }
        }
        processes.clear();
    }

    private void monitorProcess(String processId, Process process) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                printer.println(HexColor.colorText("[" + processId + "] ", HexColor.Colors.BLUE) + line, true);
            }

            int exitCode = process.waitFor();
            processes.remove(processId);

            String exitMessage = "Process '" + processId + "' exited with code: " + exitCode;
            String color = exitCode == 0 ? HexColor.Colors.GREEN : HexColor.Colors.RED;
            printer.println(HexColor.colorText(exitMessage, color), true);

        } catch (IOException | InterruptedException e) {
            printer.println(HexColor.colorText("Error monitoring process '" + processId + "': " + e.getMessage(), HexColor.Colors.RED), true);
        }
    }

    public static class ProcessResult {
        private final int exitCode;
        private final String output;
        private final String error;

        public ProcessResult(int exitCode, String output, String error) {
            this.exitCode = exitCode;
            this.output = output;
            this.error = error;
        }

        public int getExitCode() {
            return exitCode;
        }

        public String getOutput() {
            return output;
        }

        public String getError() {
            return error;
        }

        public boolean isSuccess() {
            return exitCode == 0 && error == null;
        }
    }
}
