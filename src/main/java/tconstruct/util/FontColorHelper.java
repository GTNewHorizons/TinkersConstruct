package tconstruct.util;

import java.awt.Color;

public class FontColorHelper {

    private static final double CONTRAST_THRESHOLD = 3;

    public static boolean isReadable(int bgRgb, int fgRgb, double threshold) {
        double bgLum = getRelativeLuminance(bgRgb);
        double fgLum = getRelativeLuminance(fgRgb);
        double contrast = getContrastRatio(bgLum, fgLum);
        return contrast >= threshold;
    }

    public static boolean isReadable(int bgRgb, int fgRgb) {
        return isReadable(bgRgb, fgRgb, CONTRAST_THRESHOLD);
    }

    public static int adjustForegroundKeepHue(int bgRgb, int fgRgb, double threshold) {
        if (isReadable(bgRgb, fgRgb, threshold)) {
            return fgRgb;
        }

        float[] fgHsv = rgbToHsv(fgRgb);

        float hue = fgHsv[0];
        float saturation = fgHsv[1];
        float value = fgHsv[2];

        double bgLum = getRelativeLuminance(bgRgb);
        boolean brightBackground = bgLum > 0.5;

        final float VALUE_MIN = 0.0f;
        final float VALUE_MAX = 1.0f;
        float step = 0.05f;
        int maxIterations = 20;
        int iter = 0;

        float newValue = value;
        while (iter < maxIterations) {
            int candidateRgb = hsvToRgb(hue, saturation, newValue);
            if (isReadable(bgRgb, candidateRgb, threshold)) {
                return candidateRgb;
            }
            if (brightBackground) {
                newValue -= step;
                if (newValue < VALUE_MIN) {
                    newValue = VALUE_MIN;
                    return hsvToRgb(hue, saturation, newValue);
                }
            } else {
                newValue += step;
                if (newValue > VALUE_MAX) {
                    newValue = VALUE_MAX;
                    return hsvToRgb(hue, saturation, newValue);
                }
            }
            iter++;
            if (iter > 10) step = 0.01f;
        }
        return hsvToRgb(hue, saturation, newValue);
    }

    public static int adjustForegroundKeepHue(int bgRgb, int fgRgb) {
        return adjustForegroundKeepHue(bgRgb, fgRgb, CONTRAST_THRESHOLD);
    }

    private static double getRelativeLuminance(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return getRelativeLuminance(r, g, b);
    }

    private static double getRelativeLuminance(int r, int g, int b) {
        double rs = linearize(r / 255.0);
        double gs = linearize(g / 255.0);
        double bs = linearize(b / 255.0);
        return 0.2126 * rs + 0.7152 * gs + 0.0722 * bs;
    }

    private static double linearize(double channel) {
        if (channel <= 0.03928) {
            return channel / 12.92;
        }
        return Math.pow((channel + 0.055) / 1.055, 2.4);
    }

    private static double getContrastRatio(double lum1, double lum2) {
        double lighter = Math.max(lum1, lum2);
        double darker = Math.min(lum1, lum2);
        return (lighter + 0.05) / (darker + 0.05);
    }

    private static float[] rgbToHsv(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        float[] hsv = new float[3];
        Color.RGBtoHSB(r, g, b, hsv);
        hsv[0] = hsv[0] * 360;
        return hsv;
    }

    private static int hsvToRgb(float hue, float saturation, float value) {
        float h = hue / 360.0f;
        int rgb = Color.HSBtoRGB(h, saturation, value);
        return rgb & 0x00FFFFFF;
    }

}
