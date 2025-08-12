package community.theprojects.fairy.api;

import community.theprojects.fairy.console.HexColor;
import community.theprojects.fairy.console.Printer;
import community.theprojects.fairy.minecraft.MinecraftServerManager;
import community.theprojects.fairy.process.InteractiveProcessManager;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

public class RestfulApiServer {

    private final MinecraftServerManager serverManager;
    private final InteractiveProcessManager processManager;
    private final Printer printer;
    private final SecretManager secretManager;
    private Javalin app;
    private int port;
    private boolean running = false;

    public RestfulApiServer(MinecraftServerManager serverManager, InteractiveProcessManager processManager, Printer printer) {
        this.serverManager = serverManager;
        this.processManager = processManager;
        this.printer = printer;
        this.secretManager = new SecretManager();
    }

    public boolean start(int port) {
        if (running) {
            printer.println(HexColor.colorText("API Server is already running on port " + this.port, HexColor.Colors.YELLOW), true);
            return false;
        }

        this.port = port;

        try {
            app = Javalin.create(config -> {
                config.showJavalinBanner = false;
                config.bundledPlugins.enableCors(cors -> {
                    cors.addRule(it -> it.anyHost());
                });
            });

            setupRoutes();

            app.start(port);
            running = true;

            printer.println(HexColor.colorText("REST API Server started on port " + port, HexColor.Colors.GREEN), true);
            printer.println(HexColor.colorText("API Token: " + secretManager.getApiToken(), HexColor.Colors.CYAN), true);

            return true;

        } catch (Exception e) {
            printer.println(HexColor.colorText("Failed to start API Server: " + e.getMessage(), HexColor.Colors.RED), true);
            return false;
        }
    }

    public void stop() {
        if (!running) {
            printer.println(HexColor.colorText("API Server is not running", HexColor.Colors.YELLOW), true);
            return;
        }

        if (app != null) {
            app.stop();
            running = false;
            printer.println(HexColor.colorText("REST API Server stopped", HexColor.Colors.ORANGE), true);
        }
    }

    private void setupRoutes() {
        app.before(this::authenticateRequest);

        app.get("/api/health", this::getHealth);
        app.get("/api/servers", this::getServers);
        app.get("/api/servers/{serverId}", this::getServer);
        app.post("/api/servers/{serverId}/start", this::startServer);
        app.post("/api/servers/{serverId}/stop", this::stopServer);
        app.post("/api/servers/{serverId}/restart", this::restartServer);
        app.post("/api/servers/{serverId}/command", this::executeCommand);

        app.get("/api/players/{playerName}/stats", this::getPlayerStats);
        app.post("/api/players/{playerName}/kick", this::kickPlayer);
        app.post("/api/players/{playerName}/ban", this::banPlayer);
        app.post("/api/players/{playerName}/give", this::giveItem);
        app.post("/api/players/{playerName}/teleport", this::teleportPlayer);
        app.post("/api/players/{playerName}/gamemode", this::setGamemode);

        app.get("/api/processes", this::getProcesses);
        app.get("/api/processes/{processId}/status", this::getProcessStatus);

        app.post("/api/auth/regenerate", this::regenerateApiToken);

        app.exception(Exception.class, (e, ctx) -> {
            printer.println(HexColor.colorText("API Error: " + e.getMessage(), HexColor.Colors.RED), true);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.json(Map.of("error", "Internal server error", "message", e.getMessage()));
        });

        app.error(404, ctx -> {
            ctx.json(Map.of("error", "Not found", "message", "Endpoint not found"));
        });

        app.error(401, ctx -> {
            ctx.json(Map.of("error", "Unauthorized", "message", "Invalid or missing API token"));
        });
    }

    private void authenticateRequest(Context ctx) {
        if (ctx.path().equals("/api/health")) {
            return;
        }

        String authHeader = ctx.header("Authorization");
        String token = ctx.queryParam("token");

        String providedToken = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            providedToken = authHeader.substring(7);
        } else if (token != null) {
            providedToken = token;
        }

        if (providedToken == null || !secretManager.isValidToken(providedToken)) {
            ctx.status(HttpStatus.UNAUTHORIZED);
            ctx.json(Map.of("error", "Unauthorized", "message", "Invalid or missing API token"));
            return;
        }
    }

    private void getHealth(Context ctx) {
        ctx.json(Map.of(
            "status", "healthy",
            "timestamp", System.currentTimeMillis(),
            "uptime", System.currentTimeMillis(),
            "version", "1.0.0"
        ));
    }

    private void getServers(Context ctx) {
        Map<String, Object> response = new HashMap<>();
        response.put("servers", serverManager.getRegisteredServers());
        response.put("count", serverManager.getRegisteredServers().size());

        ctx.json(response);
    }

    private void getServer(Context ctx) {
        String serverId = ctx.pathParam("serverId");
        boolean isRunning = processManager.isProcessRunning(serverId);

        ctx.json(Map.of(
            "serverId", serverId,
            "running", isRunning,
            "status", isRunning ? "RUNNING" : "STOPPED"
        ));
    }

    private void startServer(Context ctx) {
        String serverId = ctx.pathParam("serverId");

        if (processManager.isProcessRunning(serverId)) {
            ctx.status(HttpStatus.CONFLICT);
            ctx.json(Map.of("error", "Server already running", "serverId", serverId));
            return;
        }

        boolean success = serverManager.startRegisteredServer(serverId);

        if (success) {
            ctx.json(Map.of("success", true, "message", "Server started", "serverId", serverId));
        } else {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(Map.of("error", "Failed to start server", "serverId", serverId));
        }
    }

    private void stopServer(Context ctx) {
        String serverId = ctx.pathParam("serverId");

        if (!processManager.isProcessRunning(serverId)) {
            ctx.status(HttpStatus.CONFLICT);
            ctx.json(Map.of("error", "Server not running", "serverId", serverId));
            return;
        }

        boolean success = serverManager.stopServer(serverId);

        if (success) {
            ctx.json(Map.of("success", true, "message", "Server stopped", "serverId", serverId));
        } else {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(Map.of("error", "Failed to stop server", "serverId", serverId));
        }
    }

    private void restartServer(Context ctx) {
        String serverId = ctx.pathParam("serverId");

        serverManager.stopServer(serverId);

        new Thread(() -> {
            try {
                Thread.sleep(5000);
                boolean success = serverManager.startRegisteredServer(serverId);
                printer.println(HexColor.colorText("Server restart " + (success ? "successful" : "failed") + ": " + serverId, 
                               success ? HexColor.Colors.GREEN : HexColor.Colors.RED), true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

        ctx.json(Map.of("success", true, "message", "Server restart initiated", "serverId", serverId));
    }

    private void executeCommand(Context ctx) {
        String serverId = ctx.pathParam("serverId");

        if (!processManager.isProcessRunning(serverId)) {
            ctx.status(HttpStatus.CONFLICT);
            ctx.json(Map.of("error", "Server not running", "serverId", serverId));
            return;
        }

        Map<String, Object> requestBody = ctx.bodyAsClass(Map.class);
        String command = (String) requestBody.get("command");

        if (command == null || command.trim().isEmpty()) {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(Map.of("error", "Command is required"));
            return;
        }

        serverManager.sendCommand(serverId, command);

        ctx.json(Map.of(
            "success", true, 
            "message", "Command executed", 
            "serverId", serverId, 
            "command", command
        ));
    }

    private void getPlayerStats(Context ctx) {
        String playerName = ctx.pathParam("playerName");
        Map<String, Object> stats = serverManager.getPlayerStatsAsMap(playerName);

        if (stats == null) {
            ctx.status(HttpStatus.NOT_FOUND);
            ctx.json(Map.of("error", "Player stats not found", "playerName", playerName));
            return;
        }

        ctx.json(stats);
    }

    private void kickPlayer(Context ctx) {
        String playerName = ctx.pathParam("playerName");
        Map<String, Object> requestBody = ctx.bodyAsClass(Map.class);
        String serverId = (String) requestBody.get("serverId");
        String reason = (String) requestBody.get("reason");

        if (serverId == null) {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(Map.of("error", "serverId is required"));
            return;
        }

        if (!processManager.isProcessRunning(serverId)) {
            ctx.status(HttpStatus.CONFLICT);
            ctx.json(Map.of("error", "Server not running", "serverId", serverId));
            return;
        }

        serverManager.kickPlayer(serverId, playerName, reason);

        ctx.json(Map.of(
            "success", true, 
            "message", "Player kicked", 
            "playerName", playerName,
            "serverId", serverId,
            "reason", reason != null ? reason : "No reason provided"
        ));
    }

    private void banPlayer(Context ctx) {
        String playerName = ctx.pathParam("playerName");
        Map<String, Object> requestBody = ctx.bodyAsClass(Map.class);
        String serverId = (String) requestBody.get("serverId");
        String reason = (String) requestBody.get("reason");

        if (serverId == null) {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(Map.of("error", "serverId is required"));
            return;
        }

        if (!processManager.isProcessRunning(serverId)) {
            ctx.status(HttpStatus.CONFLICT);
            ctx.json(Map.of("error", "Server not running", "serverId", serverId));
            return;
        }

        serverManager.banPlayer(serverId, playerName, reason);

        ctx.json(Map.of(
            "success", true, 
            "message", "Player banned", 
            "playerName", playerName,
            "serverId", serverId,
            "reason", reason != null ? reason : "No reason provided"
        ));
    }

    private void giveItem(Context ctx) {
        String playerName = ctx.pathParam("playerName");
        Map<String, Object> requestBody = ctx.bodyAsClass(Map.class);
        String serverId = (String) requestBody.get("serverId");
        String item = (String) requestBody.get("item");
        Integer amount = (Integer) requestBody.get("amount");

        if (serverId == null || item == null || amount == null) {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(Map.of("error", "serverId, item and amount are required"));
            return;
        }

        if (!processManager.isProcessRunning(serverId)) {
            ctx.status(HttpStatus.CONFLICT);
            ctx.json(Map.of("error", "Server not running", "serverId", serverId));
            return;
        }

        serverManager.giveItem(serverId, playerName, item, amount);

        ctx.json(Map.of(
            "success", true, 
            "message", "Item given to player", 
            "playerName", playerName,
            "serverId", serverId,
            "item", item,
            "amount", amount
        ));
    }

    private void teleportPlayer(Context ctx) {
        String playerName = ctx.pathParam("playerName");
        Map<String, Object> requestBody = ctx.bodyAsClass(Map.class);
        String serverId = (String) requestBody.get("serverId");
        String target = (String) requestBody.get("target");

        if (serverId == null || target == null) {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(Map.of("error", "serverId and target are required"));
            return;
        }

        if (!processManager.isProcessRunning(serverId)) {
            ctx.status(HttpStatus.CONFLICT);
            ctx.json(Map.of("error", "Server not running", "serverId", serverId));
            return;
        }

        serverManager.teleportPlayer(serverId, playerName, target);

        ctx.json(Map.of(
            "success", true, 
            "message", "Player teleported", 
            "playerName", playerName,
            "serverId", serverId,
            "target", target
        ));
    }

    private void setGamemode(Context ctx) {
        String playerName = ctx.pathParam("playerName");
        Map<String, Object> requestBody = ctx.bodyAsClass(Map.class);
        String serverId = (String) requestBody.get("serverId");
        String gamemode = (String) requestBody.get("gamemode");

        if (serverId == null || gamemode == null) {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(Map.of("error", "serverId and gamemode are required"));
            return;
        }

        if (!processManager.isProcessRunning(serverId)) {
            ctx.status(HttpStatus.CONFLICT);
            ctx.json(Map.of("error", "Server not running", "serverId", serverId));
            return;
        }

        serverManager.setGamemode(serverId, playerName, gamemode);

        ctx.json(Map.of(
            "success", true, 
            "message", "Gamemode set", 
            "playerName", playerName,
            "serverId", serverId,
            "gamemode", gamemode
        ));
    }

    private void getProcesses(Context ctx) {
        Map<String, Object> response = new HashMap<>();
        response.put("processes", processManager.getRunningProcesses());

        ctx.json(response);
    }

    private void getProcessStatus(Context ctx) {
        String processId = ctx.pathParam("processId");
        boolean isRunning = processManager.isProcessRunning(processId);

        ctx.json(Map.of(
            "processId", processId,
            "running", isRunning,
            "status", isRunning ? "RUNNING" : "STOPPED"
        ));
    }

    private void regenerateApiToken(Context ctx) {
        String newToken = secretManager.regenerateToken();

        ctx.json(Map.of(
            "success", true,
            "message", "API token regenerated",
            "newToken", newToken
        ));

        printer.println(HexColor.colorText("API token regenerated: " + newToken, HexColor.Colors.YELLOW), true);
    }

    public String regenerateApiToken() {
        return secretManager.regenerateToken();
    }

    public boolean isRunning() {
        return running;
    }

    public int getPort() {
        return port;
    }

    public String getApiToken() {
        return secretManager.getApiToken();
    }
}
