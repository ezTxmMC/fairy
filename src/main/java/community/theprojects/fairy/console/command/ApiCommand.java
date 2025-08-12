package community.theprojects.fairy.console.command;

import community.theprojects.fairy.api.RestfulApiServer;
import community.theprojects.fairy.console.HexColor;
import community.theprojects.fairy.console.Printer;

public record ApiCommand(Printer printer, RestfulApiServer apiServer, String[] args) implements Command {

    @Override
    public void execute() {
        if (args.length < 2) {
            showApiHelp();
            return;
        }

        String subCommand = args[1];

        switch (subCommand.toLowerCase()) {
            case "start" -> handleStart();
            case "stop" -> handleStop();
            case "status" -> handleStatus();
            case "token" -> handleToken();
            case "regenerate" -> handleRegenerate();
            default -> {
                printer.println(HexColor.colorText("Unknown api command: " + subCommand, HexColor.Colors.RED), true);
                showApiHelp();
            }
        }
    }

    private void handleStart() {
        if (args.length < 3) {
            printer.println(HexColor.colorText("Usage: api start <port>", HexColor.Colors.YELLOW), true);
            return;
        }

        try {
            int port = Integer.parseInt(args[2]);
            boolean success = apiServer.start(port);

            if (success) {
                printer.println(HexColor.colorText("API Server started successfully on port " + port, HexColor.Colors.GREEN), true);
            }
        } catch (NumberFormatException e) {
            printer.println(HexColor.colorText("Invalid port number: " + args[2], HexColor.Colors.RED), true);
        }
    }

    private void handleStop() {
        apiServer.stop();
    }

    private void handleStatus() {
        if (apiServer.isRunning()) {
            printer.println(HexColor.colorText("API Server Status: ", HexColor.Colors.CYAN) + 
                           HexColor.colorText("RUNNING", HexColor.Colors.GREEN), true);
            printer.println(HexColor.colorText("Port: " + apiServer.getPort(), HexColor.Colors.WHITE), true);
            printer.println(HexColor.colorText("Endpoint: http://localhost:" + apiServer.getPort() + "/api", HexColor.Colors.GRAY), true);
        } else {
            printer.println(HexColor.colorText("API Server Status: ", HexColor.Colors.CYAN) + 
                           HexColor.colorText("STOPPED", HexColor.Colors.RED), true);
        }
    }

    private void handleToken() {
        String token = apiServer.getApiToken();
        printer.println(HexColor.colorText("Current API Token: ", HexColor.Colors.CYAN) + 
                       HexColor.colorText(token, HexColor.Colors.YELLOW), true);
        printer.println(HexColor.colorText("Use this token in Authorization header: Bearer " + token, HexColor.Colors.GRAY), true);
    }

    private void handleRegenerate() {
        String newToken = apiServer.regenerateApiToken();
        printer.println(HexColor.colorText("API Token regenerated: ", HexColor.Colors.GREEN) + 
                       HexColor.colorText(newToken, HexColor.Colors.YELLOW), true);
    }

    private void showApiHelp() {
        printer.println(HexColor.colorText("API Server Management Commands:", HexColor.Colors.CYAN), true);
        printer.println(HexColor.colorText("  api start <port> - Start API server on specified port", HexColor.Colors.WHITE), true);
        printer.println(HexColor.colorText("  api stop - Stop API server", HexColor.Colors.WHITE), true);
        printer.println(HexColor.colorText("  api status - Show API server status", HexColor.Colors.WHITE), true);
        printer.println(HexColor.colorText("  api token - Show current API token", HexColor.Colors.WHITE), true);
        printer.println(HexColor.colorText("  api regenerate - Regenerate API token", HexColor.Colors.WHITE), true);
    }
}
