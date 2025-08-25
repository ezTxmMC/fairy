package community.theprojects.fairy.webinterface;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

class FileUtils {

    static Path safeResolve(Path root, String uriPath) {
        try {
            String cleaned = uriPath.split("\\?")[0].split("#")[0];
            if (cleaned.contains("..")) return null;
            if (cleaned.startsWith("/")) cleaned = cleaned.substring(1);
            Path resolved = root.resolve(cleaned).normalize();
            if (!resolved.startsWith(root)) return null;
            return resolved;
        } catch (Exception e) {
            return null;
        }
    }

    static void copyDirectory(Path src, Path dst) throws IOException {
        if (!Files.exists(dst)) Files.createDirectories(dst);
        try (var paths = Files.walk(src)) {
            paths.forEach(source -> {
                try {
                    Path target = dst.resolve(src.relativize(source).toString());
                    if (Files.isDirectory(source)) {
                        if (!Files.exists(target)) Files.createDirectories(target);
                    } else {
                        Files.createDirectories(target.getParent());
                        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
    }

    static void deleteRecursively(Path path) throws IOException {
        if (!Files.exists(path)) return;
        try (var walk = Files.walk(path)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                try { Files.deleteIfExists(p); } catch (IOException ignored) { }
            });
        }
    }

    static boolean acceptsHtml(List<String> headers) {
        for (String h : headers) {
            if (h.toLowerCase(Locale.ROOT).startsWith("accept:") && h.toLowerCase(Locale.ROOT).contains("text/html")) return true;
        }
        return false;
    }

    static String contentType(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".html") || lower.endsWith(".htm")) return "text/html; charset=UTF-8";
        if (lower.endsWith(".css")) return "text/css; charset=UTF-8";
        if (lower.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (lower.endsWith(".mjs")) return "application/javascript; charset=UTF-8";
        if (lower.endsWith(".json")) return "application/json; charset=UTF-8";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".ico")) return "image/x-icon";
        if (lower.endsWith(".txt")) return "text/plain; charset=UTF-8";
        if (lower.endsWith(".map")) return "application/json; charset=UTF-8";
        if (lower.endsWith(".wasm")) return "application/wasm";
        if (lower.endsWith(".woff2")) return "font/woff2";
        if (lower.endsWith(".woff")) return "font/woff";
        if (lower.endsWith(".ttf")) return "font/ttf";
        return "application/octet-stream";
    }

    static void writeSimple(OutputStream out, int code, String status, String message) throws IOException {
        String bodyStr = "<!doctype html><html><head><meta charset=\"utf-8\"><title>" + status + "</title></head>" +
                "<body><h1>" + message + "</h1></body></html>";
        byte[] body = bodyStr.getBytes(StandardCharsets.UTF_8);
        out.write(("HTTP/1.1 " + code + " " + status + "\r\n" +
                "Content-Type: text/html; charset=UTF-8\r\n" +
                "Content-Length: " + body.length + "\r\n" +
                "Connection: close\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(body);
    }

    /**
     * Liefert einen Pfad zu einem Ressourcen-Verzeichnis (z. B. "dist").
     * Wenn die Ressourcen aus einem JAR geladen werden, wird das Verzeichnis in ein temporäres
     * Verzeichnis extrahiert und der Pfad dorthin zurückgegeben.
     *
     * @param dirName Verzeichnisname relativ zum Classpath-Root (ohne führenden Slash)
     * @return Pfad zum Verzeichnis oder null, falls nicht gefunden/fehlgeschlagen
     */
    static Path getStaticRootFromResources(String dirName) {
        try {
            String res = dirName.startsWith("/") ? dirName.substring(1) : dirName;
            URL url = FileUtils.class.getClassLoader().getResource(res);
            if (url == null) return null;

            if ("file".equalsIgnoreCase(url.getProtocol())) {
                return Paths.get(url.toURI()).toAbsolutePath().normalize();
            }

            // Bei jar:-URLs in temporäres Verzeichnis extrahieren
            if ("jar".equalsIgnoreCase(url.getProtocol())) {
                Path tmpBase = Paths.get(System.getProperty("java.io.tmpdir"), "fairy-webinterface-static");
                Files.createDirectories(tmpBase);
                Path tmp = Files.createTempDirectory(tmpBase, res.replace('/', '-') + "-");
                URI uri = url.toURI();
                try (FileSystem fs = FileSystems.newFileSystem(uri, java.util.Map.of())) {
                    Path jarDir = Paths.get(uri);
                    copyDirectory(jarDir, tmp);
                }
                return tmp;
            }
        } catch (Exception ignored) { }
        return null;
        }
}
