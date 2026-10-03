package tconstruct.tools.gui;

import static com.gtnewhorizon.gtnhlib.util.numberformatting.NumberFormatUtil.formatNumber;
import static tconstruct.util.McTextFormatter.addWhite;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.EnumChatFormatting;

/** The rows a stats readout queues and draws, and the colors its numbers take. */
final class StatRows {

    private StatRows() {}

    static final FontRenderer fontRendererObj = Minecraft.getMinecraft().fontRenderer;
    static int xPos, yPos;

    /** TiC2's stat colors (HeadMaterialStats, BowMaterialStats); a whole row takes its number's color. */
    static final int COLOR_ATTACK = 0xD76464;
    static final int COLOR_SPEED = 0x78A0CD;
    static final int COLOR_DRAWSPEED = 0x808080;
    private static final int COLOR_DURABILITY = 0x47CC47;
    static final int WHITE = 0xFFFFFFFF;
    /** The color of {@link EnumChatFormatting#RED}. */
    static final int RED = 0xFF5555;

    /** Queued until {@link #flush} or {@link #takePending}. */
    static final List<Row> pending = new ArrayList<>();

    static final class Row {

        final String text;
        final int color;
        final int gap;
        /** An ending drawn in {@link #COLOR_DURABILITY} whatever the row's color, or null. */
        final String tail;

        Row(String text, int color, int gap, String tail) {
            this.text = text;
            this.color = color;
            this.gap = gap;
            this.tail = tail;
        }
    }

    /** TiC2's {@code CustomFontColor.valueToColorCode}: 0 is red, 1 is green. */
    static int valueToColor(float value) {
        float hue = Math.max(0.01F, Math.min(0.5F, value / 3.0F));
        return Color.HSBtoRGB(hue, 0.65F, 0.8F) & 0xFFFFFF;
    }

    static int fractionColor(float part, float whole) {
        return valueToColor(whole > 0 ? part / whole : 0F);
    }

    /** The slash is white through format codes, which a wrap carries onto the continuation. */
    private static String fraction(int part, int whole) {
        return formatNumber(part) + addWhite(" / ") + formatNumber(whole);
    }

    static void newline() {
        write("", WHITE);
    }

    static void write(String s) {
        write(s, WHITE);
    }

    static void write(String s, int color) {
        write(s, color, 0);
    }

    static void write(String s, int color, int gap) {
        write(s, color, gap, null);
    }

    static void write(String s, int color, int gap, String tail) {
        pending.add(new Row(s, color, gap, tail));
    }

    /** The whole stays in {@link #COLOR_DURABILITY} however far the part has fallen. */
    static void writeFraction(String label, int part, int whole, int color) {
        write(label + fraction(part, whole), color, 0, formatNumber(whole));
    }

    static void flush() {
        draw(pending, Integer.MAX_VALUE, false);
        pending.clear();
    }

    static void drawRows(List<Row> rows, int first, int x, int y, int lastRowY) {
        drawRows(rows, first, x, y, lastRowY, true);
    }

    static void drawRows(List<Row> rows, int first, int x, int y, int lastRowY, boolean shadow) {
        xPos = x;
        yPos = y;
        draw(rows.subList(first, rows.size()), lastRowY, shadow);
    }

    private static void draw(List<Row> rows, int lastRowY, boolean shadow) {
        for (Row row : rows) {
            if (yPos <= lastRowY && !row.text.isEmpty()) drawRow(row, shadow);
            yPos += 10 + row.gap;
        }
    }

    private static void drawRow(Row row, boolean shadow) {
        if (row.tail == null || row.color == COLOR_DURABILITY || !row.text.endsWith(row.tail)) {
            fontRendererObj.drawString(row.text, xPos, yPos, row.color, shadow);
            return;
        }
        String head = row.text.substring(0, row.text.length() - row.tail.length());
        // with a shadow the returned end counts the shadow's extra column
        int end = fontRendererObj.drawString(head, xPos, yPos, row.color, shadow) - (shadow ? 1 : 0);
        fontRendererObj.drawString(row.tail, end, yPos, COLOR_DURABILITY, shadow);
    }

    static List<Row> wrap(List<Row> rows, int width) {
        List<Row> wrapped = new ArrayList<>();
        for (Row row : rows) {
            if (fontRendererObj.getStringWidth(row.text) <= width) {
                wrapped.add(row);
                continue;
            }
            List<?> parts = fontRendererObj.listFormattedStringToWidth(row.text, width);
            for (int j = 0; j < parts.size(); j++) {
                boolean last = j == parts.size() - 1;
                wrapped.add(new Row((String) parts.get(j), row.color, last ? row.gap : 0, last ? row.tail : null));
            }
        }
        return wrapped;
    }

    static int lastRowOffset(List<Row> rows) {
        int last = rows.size();
        while (last > 0 && rows.get(last - 1).text.isEmpty()) last--;
        int offset = 0;
        for (int i = 0; i < last - 1; i++) offset += 10 + rows.get(i).gap;
        return offset;
    }

    /** need[k]: the panel height rows k on take, with top px above the first row and pad px under the last. */
    static int[] need(List<Row> rows, int top, int pad) {
        int[] need = new int[Math.max(1, rows.size())];
        for (int k = 0; k < need.length; k++) {
            need[k] = top + lastRowOffset(rows.subList(Math.min(k, rows.size()), rows.size())) + pad;
        }
        return need;
    }

    /** Cut to width px, ending in "..." when anything was cut. */
    static String ellipsize(FontRenderer fontRendererObj, String text, int width) {
        if (fontRendererObj.getStringWidth(text) <= width) return text;
        String dots = "...";
        String cut = fontRendererObj.trimStringToWidth(text, width - fontRendererObj.getStringWidth(dots));
        return cut.replaceAll("\\s+$", "") + dots;
    }

    static List<Row> takePending(int white) {
        List<Row> rows = new ArrayList<>(pending.size());
        for (Row row : pending) rows.add(row.color == WHITE ? new Row(row.text, white, row.gap, row.tail) : row);
        pending.clear();
        return rows;
    }
}
