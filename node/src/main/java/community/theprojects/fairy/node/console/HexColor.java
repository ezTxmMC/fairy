package community.theprojects.fairy.node.console;

public final class HexColor {

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_FOREGROUND_PREFIX = "\u001B[38;2;";
    private static final String ANSI_BACKGROUND_PREFIX = "\u001B[48;2;";
    private static final String ANSI_SUFFIX = "m";

    public static String foreground(String hexColor) {
        RGB rgb = hexToRgb(hexColor);
        return ANSI_FOREGROUND_PREFIX + rgb.red + ";" + rgb.green + ";" + rgb.blue + ANSI_SUFFIX;
    }

    public static String background(String hexColor) {
        RGB rgb = hexToRgb(hexColor);
        return ANSI_BACKGROUND_PREFIX + rgb.red + ";" + rgb.green + ";" + rgb.blue + ANSI_SUFFIX;
    }

    public static String reset() {
        return ANSI_RESET;
    }

    public static String colorText(String text, String hexColor) {
        return foreground(hexColor) + text + reset();
    }

    public static String colorText(String text, String foregroundColor, String backgroundColor) {
        return foreground(foregroundColor) + background(backgroundColor) + text + reset();
    }

    private static RGB hexToRgb(String hexColor) {
        if (hexColor == null || hexColor.isEmpty()) {
            throw new IllegalArgumentException("Hex-Farbe darf nicht null oder leer sein");
        }
        String hex = hexColor.startsWith("#") ? hexColor.substring(1) : hexColor;
        if (!hex.matches("^[0-9A-Fa-f]{6}$")) {
            throw new IllegalArgumentException("Ungültiges Hex-Format: " + hexColor + ". Erwartet: #RRGGBB oder RRGGBB");
        }
        int red = Integer.valueOf(hex.substring(0, 2), 16);
        int green = Integer.valueOf(hex.substring(2, 4), 16);
        int blue = Integer.valueOf(hex.substring(4, 6), 16);

        return new RGB(red, green, blue);
    }

    private record RGB(int red, int green, int blue) {}

    public static final class Colors {
        public static final String RED = "#FF0000";
        public static final String GREEN = "#00FF00";
        public static final String BLUE = "#0000FF";
        public static final String YELLOW = "#FFFF00";
        public static final String MAGENTA = "#FF00FF";
        public static final String CYAN = "#00FFFF";
        public static final String WHITE = "#FFFFFF";
        public static final String BLACK = "#000000";
        public static final String ORANGE = "#FFA500";
        public static final String PURPLE = "#800080";
        public static final String PINK = "#FFC0CB";
        public static final String GRAY = "#808080";
        public static final String DARK_GRAY = "#404040";
        public static final String LIGHT_GRAY = "#C0C0C0";
    }
}
