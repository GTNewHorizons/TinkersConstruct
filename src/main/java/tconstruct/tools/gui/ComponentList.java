package tconstruct.tools.gui;

import static net.minecraft.client.gui.Gui.drawRect;
import static tconstruct.tools.gui.CraftCells.ACCEPT_WASH;
import static tconstruct.tools.gui.InfoPanel.PANEL_TEXT_COLOR;
import static tconstruct.tools.gui.StationPanels.PANEL_TEXT_X;
import static tconstruct.tools.gui.StationPanels.PANEL_X;
import static tconstruct.tools.gui.StationPanels.TEXT_INSET;
import static tconstruct.tools.gui.StationPanels.TEXT_PAD;
import static tconstruct.tools.gui.StationPanels.UPPER_Y;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import tconstruct.library.crafting.ToolPartRules;
import tconstruct.library.tools.BuildInput;
import tconstruct.library.tools.ToolCore;
import tconstruct.tools.inventory.ToolStationContainer;
import tconstruct.tools.logic.ToolStationLogic;

/** The open tab's component rows, the build cells they stand for, and the wrong-material warning. */
@SideOnly(Side.CLIENT)
final class ComponentList {

    private final ToolStationGui screen;
    private final ToolStationLogic logic;
    private final ToolStationContainer toolSlots;

    /** The open tab's part names and the slot each is built from; null on the repair tab. */
    String[] componentNames;
    int[] componentSlots;

    private List<?> warningRows;
    private int warningWidth;

    /** Vanilla's hovered-button yellow. */
    static final int COMPONENT_HOVER = 0xFFFFA0, COMPONENT_HOVER_WASH = 0x80000000 | COMPONENT_HOVER;

    ComponentList(ToolStationGui screen) {
        this.screen = screen;
        logic = screen.logic;
        toolSlots = screen.toolSlots;
    }

    void setComponents(ToolCore tool) {
        if (tool == null) {
            componentNames = null;
            componentSlots = null;
            return;
        }
        Item[] parts = logic.partsOf(tool);
        List<String> names = new ArrayList<>();
        List<Integer> slots = new ArrayList<>();
        for (BuildInput input : BuildInput.VALUES) {
            if (input.ordinal() >= parts.length || parts[input.ordinal()] == null) continue;
            names.add(StatCollector.translateToLocal(parts[input.ordinal()].getUnlocalizedName() + ".name"));
            slots.add(input.cell());
        }
        componentNames = names.toArray(new String[0]);
        componentSlots = new int[slots.size()];
        for (int i = 0; i < componentSlots.length; i++) componentSlots[i] = slots.get(i);
    }

    /** A right-kind part in a material the tool refuses, or every part of the right kind and nothing built. */
    boolean showsMaterialWarning() {
        ToolCore tool = screen.tabTool();
        if (tool == null) return false;
        boolean allByKind = true;
        for (int slot : componentSlots) {
            ItemStack part = logic.getStackInSlot(slot);
            BuildInput input = BuildInput.ofCell(slot);
            boolean kind = ToolPartRules.acceptsPart(tool, input, part);
            if (kind && !tool.takesPartMaterial(input, part)) return true;
            allByKind &= kind;
        }
        return allByKind && logic.serverBuildsNothing(tool);
    }

    void drawWarning(FontRenderer fontRendererObj) {
        InfoPanel upper = screen.panels.upper;
        upper.caption(fontRendererObj, StatCollector.translateToLocal("gui.toolstation.warning"));
        List<?> rows = warningRows(fontRendererObj, upper.wrapWidth(screen.panels.textWidth()));
        int y = UPPER_Y + TEXT_INSET, bottom = UPPER_Y + screen.panels.upperH - TEXT_PAD;
        for (int i = upper.firstRow(); i < rows.size(); i++) {
            if (y + fontRendererObj.FONT_HEIGHT > bottom) break;
            fontRendererObj.drawStringWithShadow((String) rows.get(i), PANEL_TEXT_X, y, PANEL_TEXT_COLOR);
            y += fontRendererObj.FONT_HEIGHT;
        }
        // so the heading fade starts fresh when the description comes back
        screen.panels.description.glowTime = Minecraft.getSystemTime();
    }

    /** need[k]: the upper panel's height for the warning from row k on, wrapped to {@code width}. */
    int[] warningNeed(FontRenderer fontRendererObj, int width) {
        int n = warningRows(fontRendererObj, width).size();
        int[] need = new int[Math.max(1, n)];
        for (int k = 0; k < need.length; k++) need[k] = TEXT_INSET + (n - k) * fontRendererObj.FONT_HEIGHT + TEXT_PAD;
        return need;
    }

    private List<?> warningRows(FontRenderer fontRendererObj, int width) {
        if (warningRows == null || width != warningWidth) {
            String text = StatCollector.translateToLocal("gui.toolstation.warning.material");
            warningRows = fontRendererObj.listFormattedStringToWidth(text, width);
            warningWidth = width;
        }
        return warningRows;
    }

    void drawComponents(FontRenderer fontRendererObj, int mouseX, int mouseY) {
        screen.panels.lower.caption(fontRendererObj, StatCollector.translateToLocal("gui.toolstation.components"));
        ToolCore tool = screen.tabTool();
        String red = EnumChatFormatting.RED.toString();
        int lit = litComponentSlots(fontRendererObj, mouseX, mouseY);
        for (int i = 0; i < componentNames.length; i++) {
            int slot = componentSlots[i];
            ItemStack part = logic.getStackInSlot(slot);
            boolean filled = ToolPartRules.takesPart(tool, BuildInput.ofCell(slot), part);
            boolean isLit = (lit & 1 << slot) != 0;
            String line = (filled || isLit ? "" : red) + " * " + componentNames[i];
            int y = screen.panels.lowerY + TEXT_INSET + i * fontRendererObj.FONT_HEIGHT;
            fontRendererObj.drawStringWithShadow(line, PANEL_TEXT_X, y, isLit ? COMPONENT_HOVER : PANEL_TEXT_COLOR);
        }
    }

    /** One bit per slot index: the slot whose row or cell is hovered, and every slot taking the considered stack. */
    int litComponentSlots(FontRenderer fontRendererObj, int mouseX, int mouseY) {
        if (componentNames == null || screen.panels.shown() != null) return 0;
        ItemStack part = screen.consideredWhileRestricted(mouseX, mouseY);
        int lit = 0;
        for (int slot : componentSlots) {
            if (part != null && toolSlots.slots[slot - 1].isItemValid(part)) lit |= 1 << slot;
        }
        int hovered = hoveredComponentSlot(fontRendererObj, mouseX, mouseY);
        return hovered == 0 ? lit : lit | 1 << hovered;
    }

    /** The slot index, or 0 for none. */
    private int hoveredComponentSlot(FontRenderer fontRendererObj, int mouseX, int mouseY) {
        int x = mouseX - screen.guiLeft, y = mouseY - screen.guiTop - screen.panels.lowerY - TEXT_INSET;
        if (x >= PANEL_X + 4 && x < PANEL_X + screen.panels.panelWidth - 4 && y >= 0) {
            int row = y / fontRendererObj.FONT_HEIGHT;
            if (row < componentSlots.length) return componentSlots[row];
        }
        Slot hovered = hoveredBuildSlot(mouseX, mouseY);
        if (hovered == null) return 0;
        for (int slot : componentSlots) if (slot == hovered.getSlotIndex()) return slot;
        return 0;
    }

    /** Mouse in screen coordinates; null for none. */
    Slot hoveredBuildSlot(int mouseX, int mouseY) {
        for (int i = 0; i < toolSlots.slots.length; i++) {
            Slot slot = toolSlots.slots[i];
            if (toolSlots.isActiveSlot(i + 1) && screen.isMouseOverSlot(slot, mouseX, mouseY)) return slot;
        }
        return null;
    }

    void drawInventoryMatches(Slot hovered) {
        // the player's slots follow the tile's in the container
        for (int i = logic.getSizeInventory(); i < screen.inventorySlots.inventorySlots.size(); i++) {
            Slot slot = (Slot) screen.inventorySlots.inventorySlots.get(i);
            ItemStack stack = slot.getStack();
            if (!toolSlots.takesByShiftClick(hovered, stack)) continue;
            int x = screen.guiLeft + slot.xDisplayPosition, y = screen.guiTop + slot.yDisplayPosition;
            drawRect(x, y, x + 16, y + 16, ACCEPT_WASH);
        }
        // drawRect leaves its color set
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
