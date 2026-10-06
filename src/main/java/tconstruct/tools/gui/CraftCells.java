package tconstruct.tools.gui;

import static net.minecraft.client.gui.Gui.drawRect;
import static tconstruct.tools.gui.ComponentList.COMPONENT_HOVER_WASH;
import static tconstruct.tools.gui.StationButtons.COLUMN_W;
import static tconstruct.tools.gui.StationDraw.drawCellFrame;
import static tconstruct.tools.gui.StationDraw.drawStretched;
import static tconstruct.tools.gui.StationDraw.icons;
import static tconstruct.tools.gui.StationSlotLayouts.CELL;
import static tconstruct.tools.gui.StationSlotLayouts.GHOST_SCALE;
import static tconstruct.tools.gui.StationSlotLayouts.ITEM_INSET;
import static tconstruct.tools.gui.StationSlotLayouts.PARK_PITCH;
import static tconstruct.tools.gui.StationSlotLayouts.PARK_X;
import static tconstruct.tools.gui.StationSlotLayouts.PARK_Y;
import static tconstruct.tools.gui.StationSlotLayouts.stretch;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import tconstruct.library.client.ToolGuiElement;
import tconstruct.tools.inventory.ToolStationContainer;
import tconstruct.tools.logic.ToolStationLogic;

/** The open tab's build cells: where they sit and how they are drawn. */
@SideOnly(Side.CLIENT)
final class CraftCells {

    private final ToolStationGui screen;
    private final ToolStationLogic logic;
    private final ToolStationContainer toolSlots;

    /** Set by {@link #placeSlots} every frame. */
    int craftOffsetY, outputOffsetY, arrowX;

    /** Slots past this count are parked in a row under the output. */
    private int layoutParts;

    /** The tool picture's top-left corner, in layout coordinates, before the cell stretch. */
    static final int GHOST_X = 10, BUILD_GHOST_TOP = 22, REPAIR_GHOST_TOP = 20;
    private final int[] cellX = new int[6], cellY = new int[6];

    /** The craft area runs down to the inventory label, or to the inventory on the repair tab, which has no label. */
    static final int CRAFT_TOP = 19, LABEL_FROM_BOTTOM = 94, INVENTORY_FROM_BOTTOM = 83;
    private static final int BUILD_OFFSET_Y = 9;
    static final int BUILD_GHOST_Y = BUILD_GHOST_TOP + BUILD_OFFSET_Y;
    static final float GHOST_SIZE = 16 * GHOST_SCALE;
    /** The arrow and the output well, in the panel's frame; the arrow's column is picked per tab. */
    static final int ARROW_W = 22, ARROW_H = 15, ARROW_Y = 38, WELL_X = 119, WELL_Y = 33, WELL_SIZE = 26,
            WELL_INSET = (WELL_SIZE - 16) / 2, OUTPUT_MIDDLE = WELL_Y + WELL_SIZE / 2;
    private static final int GHOST_RIGHT = (int) Math.ceil(GHOST_X + GHOST_SIZE);

    static final int ACCEPT_WASH = 0x7333DD55, REFUSE_WASH = 0x80FF3333;
    private static final int OFF_SCREEN = -9999;

    CraftCells(ToolStationGui screen) {
        this.screen = screen;
        logic = screen.logic;
        toolSlots = screen.toolSlots;
    }

    void applyTabLayout(ToolGuiElement element) {
        int[][] layout = element.slotX != null ? new int[][] { element.slotX, element.slotY }
                : screen.slotTypeLayout(element.slotType);
        if (layout == null) return;
        int slots = toolSlots.slots.length;
        layoutParts = Math.min(layout[0].length, slots);
        screen.slotX = new int[slots];
        screen.slotY = new int[slots];
        for (int i = 0; i < slots; i++) {
            screen.slotX[i] = i < layoutParts ? layout[0][i] : PARK_X + PARK_PITCH * (i - layoutParts);
            screen.slotY[i] = i < layoutParts ? layout[1][i] : PARK_Y;
        }
    }

    private boolean parked(int i) {
        return i >= layoutParts;
    }

    /** A placed part turns like its hint; 0 outside a build tab. */
    int turnOf(Slot slot) {
        if (!toolSlots.isRestricting() || screen.tabElement == null) return 0;
        for (int i = 0; i < toolSlots.slots.length; i++) {
            if (toolSlots.slots[i] == slot) return screen.tabElement.turnOf(i);
        }
        return 0;
    }

    void placeSlots(int ySize) {
        int ghostTop = screen.selectedButton == 0 ? REPAIR_GHOST_TOP : BUILD_GHOST_TOP;
        for (int i = 0; i < screen.slotX.length; i++) {
            boolean parked = parked(i);
            cellX[i] = parked ? screen.slotX[i] : stretch(screen.slotX[i], GHOST_X);
            cellY[i] = parked ? screen.slotY[i] : stretch(screen.slotY[i], ghostTop);
        }
        int craftBottom = ySize - (screen.selectedButton == 0 ? INVENTORY_FROM_BOTTOM : LABEL_FROM_BOTTOM);
        int top = craftBottom, bottom = CRAFT_TOP, right = 0;
        for (int i = 0; i < screen.slotX.length; i++) {
            // a parked spare must not size the block, or the layout shifts under the cursor when one is picked up
            if (parked(i) || !toolSlots.isActiveSlot(i + 1)) continue;
            top = Math.min(top, cellY[i]);
            bottom = Math.max(bottom, cellY[i] + CELL);
            right = Math.max(right, cellX[i] + CELL);
        }
        // with nothing active the block is the whole area
        if (top > bottom) {
            top = CRAFT_TOP;
            bottom = craftBottom;
        }
        if (screen.selectedButton == 0) {
            // the anvil shares the build tabs' picture row, so switching tabs does not move it
            craftOffsetY = BUILD_GHOST_Y - REPAIR_GHOST_TOP;
        } else if (screen.tabElement != null && screen.tabElement.slotX == null) {
            // a tab without its own layout centers its block on the tool's picture
            craftOffsetY = Math.round(BUILD_GHOST_Y + GHOST_SIZE / 2 - (top + bottom) / 2.0F);
        } else {
            craftOffsetY = BUILD_OFFSET_Y;
        }
        right = Math.max(right, GHOST_RIGHT);
        arrowX = right + (WELL_X - right - ARROW_W) / 2;
        int level = screen.selectedButton == 0 ? cellY[0] + CELL / 2 : (top + bottom) / 2;
        outputOffsetY = level + craftOffsetY - OUTPUT_MIDDLE;
        // on a build tab the row of spares under the well stays above the Inventory label
        if (screen.selectedButton != 0) outputOffsetY = Math.min(outputOffsetY, craftBottom - 1 - PARK_Y - CELL);

        for (int i = 0; i < screen.slotX.length; i++) {
            Slot slot = toolSlots.slots[i];
            boolean active = toolSlots.isActiveSlot(i + 1);
            slot.xDisplayPosition = active ? cellX[i] + COLUMN_W + ITEM_INSET : OFF_SCREEN;
            slot.yDisplayPosition = active ? cellY[i] + rowOffset(i) + ITEM_INSET : OFF_SCREEN;
        }
        toolSlots.toolSlot.yDisplayPosition = WELL_Y + WELL_INSET + outputOffsetY;
    }

    private int rowOffset(int i) {
        return parked(i) ? outputOffsetY : craftOffsetY;
    }

    void drawCells(FontRenderer fontRendererObj, float zLevel, int cornerX, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        screen.mc.getTextureManager().bindTexture(icons);
        // a 28% black interior under a full-strength border lets the ghost show through the cell
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        ItemStack held = screen.consideredWhileRestricted(mouseX, mouseY);
        int lit = screen.components.litComponentSlots(fontRendererObj, mouseX, mouseY);
        for (int i = 0; i < screen.slotX.length; i++) {
            if (!toolSlots.isActiveSlot(i + 1)) continue;
            int x = cornerX + cellX[i];
            int y = screen.guiTop + cellY[i] + rowOffset(i);
            GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.28F);
            drawStretched(zLevel, x + 1, y + 1, CELL - 2, CELL - 2, 145, 217, 16, 16);
            int wash = held != null && toolSlots.slots[i].isItemValid(held) ? ACCEPT_WASH
                    : (lit & 1 << (i + 1)) != 0 ? COMPONENT_HOVER_WASH : 0;
            if (wash != 0) {
                // the interior sprite is gray, so a colored wash needs its own quad; drawRect leaves blend off
                drawRect(x + 1, y + 1, x + CELL - 1, y + CELL - 1, wash);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            }
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            drawCellFrame(zLevel, x, y);
            boolean hinted = i < screen.iconX.length && i < screen.iconY.length && !logic.isStackInSlot(i + 1);
            if (hinted && screen.tabTool() == null) {
                screen.drawTexturedModalRect(x + 1, y + 1, 18 * screen.iconX[i], 18 * screen.iconY[i], 18, 18);
            } else if (hinted) {
                SlotHints.drawStretchedHint(
                        zLevel,
                        screen.iconX[i],
                        screen.iconY[i],
                        x + CELL / 2,
                        y + CELL / 2,
                        screen.tabElement.turnOf(i));
            }
        }
        GL11.glDisable(GL11.GL_BLEND);
    }
}
