package community.theprojects.fairy.console;

/**
 * Utility-Klasse für Hex-basierte Text-Farbgebung in der Konsole.
 * Konvertiert Hex-Farbcodes in ANSI-Escape-Codes für Terminal-Ausgabe.
 */
public final class HexColor {

    // ANSI-Escape-Code-Konstanten
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_FOREGROUND_PREFIX = "\u001B[38;2;";
    private static final String ANSI_BACKGROUND_PREFIX = "\u001B[48;2;";
    private static final String ANSI_SUFFIX = "m";

    private HexColor() {
        // Utility-Klasse - keine Instanziierung
    }

    /**
     * Konvertiert einen Hex-Farbcode in einen ANSI-Vordergrund-Farbcode.
     * 
     * @param hexColor Hex-Farbcode (z.B. "#FF0000" oder "FF0000")
     * @return ANSI-Escape-Code für Vordergrundfarbe
     */
    public static String foreground(String hexColor) {
        RGB rgb = hexToRgb(hexColor);
        return ANSI_FOREGROUND_PREFIX + rgb.red + ";" + rgb.green + ";" + rgb.blue + ANSI_SUFFIX;
    }

    /**
     * Konvertiert einen Hex-Farbcode in einen ANSI-Hintergrund-Farbcode.
     * 
     * @param hexColor Hex-Farbcode (z.B. "#FF0000" oder "FF0000")
     * @return ANSI-Escape-Code für Hintergrundfarbe
     */
    public static String background(String hexColor) {
        RGB rgb = hexToRgb(hexColor);
        return ANSI_BACKGROUND_PREFIX + rgb.red + ";" + rgb.green + ";" + rgb.blue + ANSI_SUFFIX;
    }

    /**
     * Gibt den ANSI-Reset-Code zurück, um alle Farben zurückzusetzen.
     * 
     * @return ANSI-Reset-Code
     */
    public static String reset() {
        return ANSI_RESET;
    }

    /**
     * Wraps Text mit einer Vordergrundfarbe und setzt die Farbe danach zurück.
     * 
     * @param text Text, der gefärbt werden soll
     * @param hexColor Hex-Farbcode für die Vordergrundfarbe
     * @return Gefärbter Text mit Reset-Code
     */
    public static String colorText(String text, String hexColor) {
        return foreground(hexColor) + text + reset();
    }

    /**
     * Wraps Text mit Vordergrund- und Hintergrundfarbe und setzt die Farben danach zurück.
     * 
     * @param text Text, der gefärbt werden soll
     * @param foregroundColor Hex-Farbcode für die Vordergrundfarbe
     * @param backgroundColor Hex-Farbcode für die Hintergrundfarbe
     * @return Gefärbter Text mit Reset-Code
     */
    public static String colorText(String text, String foregroundColor, String backgroundColor) {
        return foreground(foregroundColor) + background(backgroundColor) + text + reset();
    }

    /**
     * Konvertiert einen Hex-Farbcode in RGB-Werte.
     * 
     * @param hexColor Hex-Farbcode (mit oder ohne #)
     * @return RGB-Objekt mit rot-, grün- und blauwerten
     */
    private static RGB hexToRgb(String hexColor) {
        if (hexColor == null || hexColor.isEmpty()) {
            throw new IllegalArgumentException("Hex-Farbe darf nicht null oder leer sein");
        }

        // Entferne # falls vorhanden
        String hex = hexColor.startsWith("#") ? hexColor.substring(1) : hexColor;

        // Validiere Hex-Format
        if (!hex.matches("^[0-9A-Fa-f]{6}$")) {
            throw new IllegalArgumentException("Ungültiges Hex-Format: " + hexColor + ". Erwartet: #RRGGBB oder RRGGBB");
        }

        int red = Integer.valueOf(hex.substring(0, 2), 16);
        int green = Integer.valueOf(hex.substring(2, 4), 16);
        int blue = Integer.valueOf(hex.substring(4, 6), 16);

        return new RGB(red, green, blue);
    }

    /**
     * Record für RGB-Werte.
     */
    private record RGB(int red, int green, int blue) {}

    // Vordefinierte Farben für einfache Verwendung
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

        private Colors() {
            // Utility-Klasse - keine Instanziierung
        }
    }
}
