package community.theprojects.fairy.minecraft;

public enum MinecraftServerType {
    VANILLA("vanilla", "server.jar", "start"),
    SPIGOT("spigot", "spigot.jar", "start"),
    PAPER("paper", "paper.jar", "start"),
    VELOCITY("velocity", "velocity.jar", "start"),
    BUNGEECORD("bungeecord", "bungeecord.jar", "start"),
    FORGE("forge", "forge.jar", "run"),
    FABRIC("fabric", "fabric-server-launch.jar", "start"),
    NEOFORGE("neoforge", "neoforge.jar", "run"),
    QUILT("quilt", "quilt-server-launch.jar", "start");

    private final String name;
    private final String defaultJarName;
    private final String defaultScriptName;

    MinecraftServerType(String name, String defaultJarName, String defaultScriptName) {
        this.name = name;
        this.defaultJarName = defaultJarName;
        this.defaultScriptName = defaultScriptName;
    }

    public String getName() {
        return name;
    }

    public String getDefaultJarName() {
        return defaultJarName;
    }

    public String getDefaultScriptName() {
        return defaultScriptName;
    }

    public String getStartScript() {
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        return defaultScriptName + (isWindows ? ".bat" : ".sh");
    }

    public String getAlternativeStartScript() {
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        return "start" + (isWindows ? ".bat" : ".sh");
    }

    public boolean isProxy() {
        return this == VELOCITY || this == BUNGEECORD;
    }

    public boolean isModded() {
        return this == FORGE || this == FABRIC || this == NEOFORGE || this == QUILT;
    }

    public static MinecraftServerType fromString(String type) {
        for (MinecraftServerType serverType : values()) {
            if (serverType.name.equalsIgnoreCase(type)) {
                return serverType;
            }
        }
        return VANILLA;
    }
}
