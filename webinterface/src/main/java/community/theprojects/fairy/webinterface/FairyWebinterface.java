package community.theprojects.fairy.webinterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FairyWebinterface {
    private final ServerSocket serverSocket;
    private boolean running = false;
    private Path staticRoot;
    private Thread serverThread;

    public FairyWebinterface(int port) {
        try {
            this.serverSocket = new ServerSocket(port);
            System.out.println("Server läuft auf http://localhost:" + port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void start() {
        if (running && serverThread != null && serverThread.isAlive()) return;
        // statische Assets aus resources/dist ermitteln
        this.staticRoot = FileUtils.getStaticRootFromResources("dist");
        if (this.staticRoot != null) {
            System.out.println("Statische Dateien aus: " + this.staticRoot);
        } else {
            System.out.println("Warnung: resources/dist nicht gefunden. Es werden 503-Responses gesendet.");
        }
        running = true;
        serverThread = new Thread(this::runServer, "FairyWebinterface-Server");
        serverThread.setDaemon(false);
        serverThread.start();
    }

    private void runServer() {
        while (running) {
            try (Socket client = this.serverSocket.accept();
                 BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
                 OutputStream out = client.getOutputStream()) {
                String requestLine = in.readLine();
                if (requestLine == null || requestLine.isEmpty()) continue;
                List<String> headers = new ArrayList<>();
                String header;
                while ((header = in.readLine()) != null && !header.isEmpty()) headers.add(header);
                String[] parts = requestLine.split(" ");
                if (parts.length < 2) { FileUtils.writeSimple(out, 400, "Bad Request", "Bad Request"); continue; }
                String method = parts[0];
                String path = parts[1];
                boolean headOnly = "HEAD".equalsIgnoreCase(method);
                if (staticRoot != null && Files.isDirectory(staticRoot)) {
                    serveFromStatic(path, headers, out, headOnly);
                } else {
                    FileUtils.writeSimple(out, 503, "Service Unavailable", "No static assets (resources/dist) available");
                }
            } catch (IOException e) {
                // ignore single request errors
            }
        }
    }

    public void stop() {
        this.running = false;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (serverThread != null) {
            try {
                serverThread.join(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void serveFromStatic(String requestPath, List<String> headers, OutputStream out, boolean headOnly) throws IOException {
        String path = (requestPath == null || requestPath.isEmpty() || "/".equals(requestPath)) ? "/index.html" : requestPath;
        Path target = FileUtils.safeResolve(staticRoot, path);
        if (target == null) { FileUtils.writeSimple(out, 400, "Bad Request", "Invalid path"); return; }
        if (Files.isDirectory(target)) target = target.resolve("index.html");
        if (Files.exists(target) && Files.isRegularFile(target)) {
            byte[] data = Files.readAllBytes(target);
            String contentType = FileUtils.contentType(target.getFileName().toString());
            out.write(("HTTP/1.1 200 OK\r\n" +
                    "Content-Type: " + contentType + "\r\n" +
                    "Content-Length: " + data.length + "\r\n" +
                    "Connection: close\r\n\r\n").getBytes(StandardCharsets.UTF_8));
            if (!headOnly) out.write(data);
        } else {
            if (FileUtils.acceptsHtml(headers)) {
                Path index = staticRoot.resolve("index.html");
                if (Files.exists(index)) {
                    byte[] data = Files.readAllBytes(index);
                    out.write(("HTTP/1.1 200 OK\r\n" +
                            "Content-Type: text/html; charset=UTF-8\r\n" +
                            "Content-Length: " + data.length + "\r\n" +
                            "Connection: close\r\n\r\n").getBytes(StandardCharsets.UTF_8));
                    if (!headOnly) out.write(data);
                } else {
                    FileUtils.writeSimple(out, 500, "Internal Server Error", "index.html not found");
                }
            } else {
                FileUtils.writeSimple(out, 404, "Not Found", "404 Not Found");
            }
        }
    }
}
