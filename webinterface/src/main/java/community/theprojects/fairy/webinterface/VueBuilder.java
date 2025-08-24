package community.theprojects.fairy.webinterface;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

class VueBuilder {

    private final WebConfig cfg;
    private final VueProjectResolver resolver = new VueProjectResolver();
    private final NpmProvisioner npmProvisioner = new NpmProvisioner();

    private Path runningRoot;
    private boolean tempRoot;

    VueBuilder(WebConfig cfg) {
        this.cfg = cfg;
    }

    Path prepareRunningRoot() {
        try {
            if (cfg.runningDirOverride != null && !cfg.runningDirOverride.isBlank()) {
                runningRoot = Paths.get(cfg.runningDirOverride).toAbsolutePath();
                Files.createDirectories(runningRoot);
                tempRoot = false;
            } else {
                Path tmpBase = Paths.get(System.getProperty("java.io.tmpdir"), "fairy-webinterface-running");
                Files.createDirectories(tmpBase);
                runningRoot = Files.createTempDirectory(tmpBase, "run-");
                tempRoot = true;
            }
            System.out.println("Running-Ordner: " + runningRoot);
            Path vueProjectDir = resolver.resolve(cfg);
            if (vueProjectDir == null) {
                System.out.println("Kein Vue-Projekt gefunden. Running-Ordner bleibt leer.");
                cleanupOnFailure();
                return null;
            }
            System.out.println("Vue-Projekt: " + vueProjectDir);
            String pm = pickPackageManager(vueProjectDir);
            if (pm == null) {
                System.out.println("Kein Package-Manager gefunden. Bitte npm/yarn/pnpm installieren oder 'vue.pm' setzen.");
                cleanupOnFailure();
                return null;
            }
            String npmCmdOverride = null;
            Path nodeDirForEnv = null;
            if ("npm".equals(pm)) {
                String npmCmd = npmProvisioner.resolveNpmCommand(runningRoot);
                if (npmCmd != null) {
                    npmCmdOverride = npmCmd;
                    Path npmPath = Paths.get(npmCmd);
                    nodeDirForEnv = npmPath.getParent();
                } else if (!ProcessRunner.isExecutableAvailable("npm")) {
                    System.out.println("npm ist nicht verfügbar.");
                    cleanupOnFailure();
                    return null;
                }
            }
            List<String> installCmd = new ArrayList<>();
            if ("pnpm".equals(pm)) {
                installCmd.addAll(List.of("pnpm", "install", "--frozen-lockfile"));
            } else if ("yarn".equals(pm)) {
                installCmd.addAll(List.of("yarn", "install", "--frozen-lockfile"));
            } else {
                if (Files.exists(vueProjectDir.resolve("package-lock.json"))) {
                    installCmd.add(npmCmdOverride != null ? npmCmdOverride : "npm");
                    installCmd.add("ci");
                } else {
                    installCmd.add(npmCmdOverride != null ? npmCmdOverride : "npm");
                    installCmd.addAll(List.of("install", "--no-audit", "--no-fund"));
                }
            }
            if (!ProcessRunner.runCommand(installCmd, vueProjectDir, 10 * 60, nodeDirForEnv)) {
                System.out.println("Install fehlgeschlagen.");
                cleanupOnFailure();
                return null;
            }
            List<String> buildCmd = new ArrayList<>();
            if ("pnpm".equals(pm)) {
                buildCmd.addAll(List.of("pnpm", "run", "build"));
            } else if ("yarn".equals(pm)) {
                buildCmd.addAll(List.of("yarn", "build"));
            } else {
                buildCmd.add(npmCmdOverride != null ? npmCmdOverride : "npm");
                buildCmd.addAll(List.of("run", "build"));
            }
            if (!ProcessRunner.runCommand(buildCmd, vueProjectDir, 10 * 60, nodeDirForEnv)) {
                System.out.println("Build-Command fehlgeschlagen.");
                cleanupOnFailure();
                return null;
            }
            Path outDir = vueProjectDir.resolve(cfg.vueOutDir);
            if (!Files.isDirectory(outDir)) {
                System.out.println("Build-Output nicht gefunden: " + outDir);
                cleanupOnFailure();
                return null;
            }
            FileUtils.copyDirectory(outDir, runningRoot);
            System.out.println("Build ausgeliefert unter: " + runningRoot);
            return runningRoot;
        } catch (Exception e) {
            System.out.println("Fehler bei Vorbereitung des Running-Ordners: " + e.getMessage());
            cleanupOnFailure();
            return null;
        }
    }

    void cleanupRunningRoot() {
        if (runningRoot != null && tempRoot) {
            try {
                FileUtils.deleteRecursively(runningRoot);
            } catch (Exception ignored) { }
        }
    }

    private void cleanupOnFailure() {
        if (tempRoot && runningRoot != null) {
            try {
                FileUtils.deleteRecursively(runningRoot);
            } catch (Exception ignored) { } finally { runningRoot = null; }
        }
    }

    private String pickPackageManager(Path vueProjectDir) {
        if (cfg.packageManager != null && !cfg.packageManager.isBlank()) {
            String pm = sanitizePm(cfg.packageManager);
            if (isPmAvailable(pm)) return pm;
            System.out.println("Konfigurierter Package-Manager nicht gefunden: " + cfg.packageManager);
            return null;
        }
        if (Files.exists(vueProjectDir.resolve("pnpm-lock.yaml")) && ProcessRunner.isExecutableAvailable("pnpm")) return "pnpm";
        if (Files.exists(vueProjectDir.resolve("yarn.lock")) && ProcessRunner.isExecutableAvailable("yarn")) return "yarn";
        if (ProcessRunner.isExecutableAvailable("npm")) return "npm";
        return "npm";
    }

    private static boolean isPmAvailable(String pm) {
        if ("pnpm".equals(pm)) return ProcessRunner.isExecutableAvailable("pnpm");
        if ("yarn".equals(pm)) return ProcessRunner.isExecutableAvailable("yarn");
        return ProcessRunner.isExecutableAvailable("npm");
    }

    private static String sanitizePm(String pm) {
        pm = pm.trim().toLowerCase();
        if (pm.contains("pnpm")) return "pnpm";
        if (pm.contains("yarn")) return "yarn";
        return "npm";
    }
}
