package tconstruct.util;

public enum McTextFormatter {

    black("\u00A70"),
    dark_blue("\u00A71"),
    dark_green("\u00A72"),
    dark_aqua("\u00A73"),
    dark_red("\u00A74"),
    dark_purple("\u00A75"),
    gold("\u00A76"),
    gray("\u00A77"),
    dark_gray("\u00A78"),
    blue("\u00A79"),
    green("\u00A7a"),
    aqua("\u00A7b"),
    red("\u00A7c"),
    light_purple("\u00A7d"),
    yellow("\u00A7e"),
    white("\u00A7f"),
    reset("\u00A7r"),
    obfuscated("\u00A7k"),
    bold("\u00A7l"),
    strikethrough("\u00A7m"),
    underline("\u00A7n"),
    italic("\u00A7o");

    String code;

    McTextFormatter(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return this.code;
    }

    public static String addBlack(String str) {
        return black + str + reset;
    }

    public static String addDarkBlue(String str) {
        return dark_blue + str + reset;
    }

    public static String addDarkGreen(String str) {
        return dark_green + str + reset;
    }

    public static String addDarkAqua(String str) {
        return dark_aqua + str + reset;
    }

    public static String addDarkRed(String str) {
        return dark_red + str + reset;
    }

    public static String addDarkPurple(String str) {
        return dark_purple + str + reset;
    }

    public static String addGold(String str) {
        return gold + str + reset;
    }

    public static String addGray(String str) {
        return gray + str + reset;
    }

    public static String addDarkGray(String str) {
        return dark_gray + str + reset;
    }

    public static String addBlue(String str) {
        return blue + str + reset;
    }

    public static String addGreen(String str) {
        return green + str + reset;
    }

    public static String addAqua(String str) {
        return aqua + str + reset;
    }

    public static String addRed(String str) {
        return red + str + reset;
    }

    public static String addLightPurple(String str) {
        return light_purple + str + reset;
    }

    public static String addYellow(String str) {
        return yellow + str + reset;
    }

    public static String addWhite(String str) {
        return white + str + reset;
    }

    public static String addObfuscated(String str) {
        return obfuscated + str + reset;
    }

    public static String addBold(String str) {
        return bold + str + reset;
    }

    public static String addStrikethrough(String str) {
        return strikethrough + str + reset;
    }

    public static String addUnderLine(String str) {
        return underline + str + reset;
    }

    public static String addItalic(String str) {
        return italic + str + reset;
    }

    public static String reset() {
        return reset.code;
    }
}
