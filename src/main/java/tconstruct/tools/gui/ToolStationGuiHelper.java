package tconstruct.tools.gui;

import static com.gtnewhorizon.gtnhlib.util.numberformatting.NumberFormatUtil.formatNumber;
import static tconstruct.tools.gui.InfoPanel.PANEL_TEXT_COLOR;
import static tconstruct.tools.gui.StatReadout.drawStatsBody;
import static tconstruct.tools.gui.StatRows.WHITE;
import static tconstruct.tools.gui.StatRows.flush;
import static tconstruct.tools.gui.StatRows.newline;
import static tconstruct.tools.gui.StatRows.takePending;
import static tconstruct.tools.gui.StatRows.wrap;
import static tconstruct.tools.gui.StatRows.write;

import java.util.List;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import tconstruct.library.modifier.IModifyable;
import tconstruct.library.tools.ToolCore;
import tconstruct.tools.gui.StatRows.Row;

public final class ToolStationGuiHelper {

    // non-instantiable
    private ToolStationGuiHelper() {}

    private static final int MODIFIERS_GAP = 5;

    public static void drawToolStats(ItemStack stack, int x, int y) {
        Item item = stack.getItem();
        NBTTagCompound tags = resolveTags(stack);

        StatRows.pending.clear();
        StatRows.xPos = x;
        StatRows.yPos = y + 8;

        drawCenteredString(
                StatRows.fontRendererObj,
                "\u00A7n" + titleOf(stack),
                StatRows.xPos + 55,
                StatRows.yPos,
                WHITE);
        newline();
        newline();
        drawStatsBody(stack, item, tags);
        newline();
        drawModifiers(tags, 0);
        flush();
    }

    /** The Crafting Station's readout under its title: the stats, a blank row, then the modifiers. */
    static List<Row> readoutRows(ItemStack stack, int width) {
        StatRows.pending.clear();
        NBTTagCompound tags = resolveTags(stack);
        drawStatsBody(stack, stack.getItem(), tags);
        newline();
        drawModifiers(tags, 0);
        return wrap(takePending(WHITE), width);
    }

    static List<Row> statsRows(ItemStack stack, int width) {
        StatRows.pending.clear();
        drawStatsBody(stack, stack.getItem(), resolveTags(stack));
        return wrap(takePending(PANEL_TEXT_COLOR), width);
    }

    static List<Row> modifierRows(ItemStack stack, int width) {
        StatRows.pending.clear();
        drawModifiers(resolveTags(stack), MODIFIERS_GAP);
        return wrap(takePending(PANEL_TEXT_COLOR), width);
    }

    private static NBTTagCompound resolveTags(ItemStack stack) {
        NBTTagCompound tags = stack.getTagCompound();
        if (stack.getItem() instanceof IModifyable modifyable) {
            tags = tags.getCompoundTag(modifyable.getBaseTagName());
        }
        return tags;
    }

    static String titleOf(ItemStack stack) {
        return stack.getItem() instanceof ToolCore ? ((ToolCore) stack.getItem()).getLocalizedToolName()
                : stack.getDisplayName();
    }

    /** {@code gap}: px between the remaining count and the list under it. */
    private static void drawModifiers(NBTTagCompound tags, int gap) {
        int modifiers = tags.getInteger("Modifiers");
        // remaining modifiers
        if (modifiers != 0) write(
                StatCollector.translateToLocal("gui.toolstation18") + formatNumber(modifiers),
                WHITE,
                tags.hasKey("ModifierTip1") ? gap : 0);

        // Modifier-header (if we have modifiers)
        if (tags.hasKey("ModifierTip1")) {
            write(StatCollector.translateToLocal("gui.toolstation17"));

            String tooltip = "ModifierTip";
            int tipNum = 1;
            while (tags.hasKey(tooltip + tipNum)) {
                String tipName = tags.getString(tooltip + tipNum);
                String locString = "modifier.toolstation." + tipName;
                // strip out the '(X of Y)' in some for the localization strings.. sigh
                int bracket = tipName.indexOf("(");
                if (bracket > 0) locString = "modifier.toolstation." + tipName.substring(0, bracket);
                locString = EnumChatFormatting.getTextWithoutFormattingCodes(locString.replace(" ", ""));

                if (StatCollector.canTranslate(locString)) {
                    tipName = tipName.replace(
                            EnumChatFormatting.getTextWithoutFormattingCodes(tipName),
                            StatCollector.translateToLocal(locString));
                    // re-add the X/Y
                    if (bracket > 0) tipName += " " + tags.getString(tooltip + tipNum).substring(bracket);
                }
                write("- " + tipName);
                tipNum++;
            }
        }
    }

    /**
     * Renders the specified text to the screen, center-aligned. Copied out of GUI
     */
    public static void drawCenteredString(FontRenderer fontRendererIn, String text, int x, int y, int color) {
        fontRendererIn.drawStringWithShadow(text, x - fontRendererIn.getStringWidth(text) / 2, y, color);
    }
}
