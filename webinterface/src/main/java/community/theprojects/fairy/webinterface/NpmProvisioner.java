package community.theprojects.fairy.webinterface;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.Locale;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

class NpmProvisioner {

    private static final String NODE_VERSION = "v20.19.0";

    String resolveNpmCommand(Path cacheBase) {
        if (ProcessRunner.isExecutableAvailable(npmCmdName())) return npmCmdName();
        if (!isWindows()) {
            System.out.println("npm nicht gefunden und automatischer Download ist nur für Windows implementiert.");
            return null;
        }
        try {
            Path cacheDir = cacheBase.resolve("node-cache").resolve(NODE_VERSION);
            Path installDir = cacheDir.resolve("node-" + NODE_VERSION + "-win-x64");
            Path npmCmd = installDir.resolve(npmCmdName());
            if (Files.exists(npmCmd)) return npmCmd.toAbsolutePath().toString();
            Files.createDirectories(cacheDir);
            String zipName = "node-" + NODE_VERSION + "-win-x64.zip";
            String url = "https://nodejs.org/dist/" + NODE_VERSION + "/node-" + NODE_VERSION + "-win-x64.zip";
            Path zipPath = cacheDir.resolve(zipName);
            if (!Files.exists(zipPath)) {
                System.out.println("Lade Node (" + NODE_VERSION + ") herunter...");
                download(url, zipPath);
            }
            System.out.println("Entpacke Node nach: " + installDir);
            unzipStripTopLevel(zipPath, installDir);
            if (Files.exists(npmCmd)) return npmCmd.toAbsolutePath().toString();
            Optional<Path> found = findExecutableRecursively(installDir, npmCmdName());
            if (found.isPresent()) return found.get().toAbsolutePath().toString();
        } catch (Exception e) {
            System.out.println("npm-Download/Provisionierung fehlgeschlagen: " + e.getMessage());
        }
        return null;
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win");
    }

    private static String npmCmdName() {
        return isWindows() ? "npm.cmd" : "npm";
    }

    private static void download(String urlStr, Path dest) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(60000);
        conn.setRequestProperty("User-Agent", "FairyWebinterface");
        try (BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
             FileOutputStream out = new FileOutputStream(dest.toFile())) {
            in.transferTo(out);
        } finally {
            conn.disconnect();
        }
    }

    private static void unzipStripTopLevel(Path zip, Path dest) throws IOException {
        Files.createDirectories(dest);
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zip))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName().replace('\\', '/');
                int idx = name.indexOf('/');
                if (idx >= 0) name = name.substring(idx + 1);
                if (name.isEmpty()) continue;
                Path outPath = dest.resolve(name);
                if (entry.isDirectory()) Files.createDirectories(outPath);
                else {
                    if (outPath.getParent() != null) Files.createDirectories(outPath.getParent());
                    try (FileOutputStream fos = new FileOutputStream(outPath.toFile())) { zis.transferTo(fos); }
                }
            }
        }
    }

    private static Optional<Path> findExecutableRecursively(Path root, String fileName) {
        try {
            try (var walk = Files.walk(root)) {
                return walk.filter(p -> Files.isRegularFile(p) && p.getFileName().toString().equalsIgnoreCase(fileName)).findFirst();
            }
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
