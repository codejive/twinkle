package org.codejive.twinkle.image.util;

public class AnsiUtils {
    public static final String ESC = "\u001b"; // Control Sequence Introducer
    public static final String CSI = ESC + "["; // Control Sequence Introducer
    public static final String OSC = ESC + "]"; // Operating System Command
    public static final String ST = ESC + "\\"; // String Terminator for OSC
    public static final String BEL = "\u0007"; // Bell (Alternative String Terminator for OSC)

    public static final CharSequence STYLE_RESET = CSI + "0m"; // Reset all attributes

    public static String rgbFg(int fgR, int fgG, int fgB) {
        return CSI + "38;2;" + fgR + ";" + fgG + ";" + fgB + "m";
    }

    public static String rgbBg(int bgR, int bgG, int bgB) {
        return CSI + "48;2;" + bgR + ";" + bgG + ";" + bgB + "m";
    }
}
