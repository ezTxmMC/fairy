package community.theprojects.fairy.webinterface;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

class ProcessRunner {

    static boolean isExecutableAvailable(String cmd) {
        try {
            Process p = new ProcessBuilder(cmd, "--version").redirectErrorStream(true).start();
            boolean ok = p.waitFor(5, TimeUnit.SECONDS) && p.exitValue() == 0;
            if (!ok) p.destroyForcibly();
            return ok;
        } catch (Exception e) {
            return false;
        }
    }

    static boolean runCommand(List<String> cmd, Path workDir, int timeoutSeconds) {
        return runCommand(cmd, workDir, timeoutSeconds, null);
    }

    static boolean runCommand(List<String> cmd, Path workDir, int timeoutSeconds, Path prependToPath) {
        System.out.println("Starte: " + String.join(" ", cmd) + " (wd=" + workDir + ")");
        try {
            ProcessBuilder pb = new ProcessBuilder(cmd).directory(workDir.toFile()).inheritIO();
            if (prependToPath != null) {
                Map<String, String> env = pb.environment();
                String pathKey = "PATH";
                for (String k : env.keySet()) { if ("PATH".equalsIgnoreCase(k)) { pathKey = k; break; } }
                String existing = env.getOrDefault(pathKey, "");
                env.put(pathKey, prependToPath.toString() + File.pathSeparator + existing);
            }
            Process p = pb.start();
            boolean finished = p.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                p.destroyForcibly();
                System.out.println("Timeout für: " + String.join(" ", cmd));
                return false;
            }
            int code = p.exitValue();
            System.out.println("Beendet (" + code + "): " + String.join(" ", cmd));
            return code == 0;
        } catch (IOException | InterruptedException e) {
            System.out.println("Fehler bei Ausführung: " + e.getMessage());
            return false;
        }
    }
}
