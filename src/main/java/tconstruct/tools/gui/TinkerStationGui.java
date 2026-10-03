package tconstruct.tools.gui;

import static tconstruct.tools.gui.CraftCells.LABEL_FROM_BOTTOM;
import static tconstruct.tools.gui.CraftCells.REFUSE_WASH;
import static tconstruct.tools.gui.StationButtons.COLUMN_W;

import java.awt.Rectangle;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import codechicken.nei.VisiblityData;
import codechicken.nei.api.INEIGuiHandler;
import codechicken.nei.api.TaggedInventoryArea;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import tconstruct.TConstruct;
import tconstruct.library.client.ToolGuiElement;
import tconstruct.plugins.nei.StationNei;
import tconstruct.tools.inventory.ToolStationContainer;
import tconstruct.tools.logic.ToolStationLogic;
import tconstruct.util.network.ToolStationPacket;

/** The station screens' GuiContainer half: the name field, slot drawing, the cursor's washes and NEI's handler. */
@SideOnly(Side.CLIENT)
@Optional.Interface(iface = "codechicken.nei.api.INEIGuiHandler", modid = "NotEnoughItems")
public abstract class TinkerStationGui extends GuiContainer implements INEIGuiHandler {

    public ToolStationLogic logic;
    public ToolStationContainer toolSlots;
    public GuiTextField text;
    public int selectedButton;
    CraftCells cells;
    ComponentList components;
    StationPanels panels;

    TinkerStationGui(Container container) {
        super(container);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        // vanilla drops a held stack on a click outside xSize by ySize; the side panels count as inside
        int w = this.xSize, h = this.ySize;
        this.xSize = panels.right();
        this.ySize = panels.bottom();
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.xSize = w;
        this.ySize = h;
        this.text.mouseClicked(mouseX - this.guiLeft, mouseY - this.guiTop, mouseButton);
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int state) {
        int w = this.xSize, h = this.ySize;
        this.xSize = panels.right();
        this.ySize = panels.bottom();
        super.mouseMovedOrUp(mouseX, mouseY, state);
        this.xSize = w;
        this.ySize = h;
    }

    /** Reads the event's wheel: Mouse.getDWheel is one total, and Mouse Tweaks empties it before the screen draws. */
    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel == 0) return;
        // as GuiScreen.handleMouseInput reads the event's position
        int x = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int y = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        panels.scroll(x - this.guiLeft, y - this.guiTop, wheel);
    }

    private ItemStack heldWhileRestricted() {
        return whileRestricting(this.mc.thePlayer.inventory.getItemStack());
    }

    ItemStack consideredWhileRestricted(int mouseX, int mouseY) {
        ItemStack held = this.mc.thePlayer.inventory.getItemStack();
        return whileRestricting(held != null ? held : stackUnderMouse(mouseX, mouseY));
    }

    private ItemStack whileRestricting(ItemStack stack) {
        return toolSlots.isRestricting() ? stack : null;
    }

    /** Mouse coordinates are untranslated, as {@link #isMouseOverSlot} takes them. */
    private ItemStack stackUnderMouse(int mouseX, int mouseY) {
        for (int i = 0; i < this.inventorySlots.inventorySlots.size(); i++) {
            Slot slot = (Slot) this.inventorySlots.inventorySlots.get(i);
            if (isMouseOverSlot(slot, mouseX, mouseY)) return slot.getStack();
        }
        return null;
    }

    /** The hover wash in red over a slot that refuses the held stack. Mouse coordinates are untranslated. */
    void drawRefusedSlot(int mouseX, int mouseY) {
        ItemStack held = heldWhileRestricted();
        if (held == null) return;
        for (Slot slot : toolSlots.slots) {
            if (!isMouseOverSlot(slot, mouseX, mouseY) || slot.isItemValid(held)) continue;
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glColorMask(true, true, true, false);
            int x = slot.xDisplayPosition, y = slot.yDisplayPosition;
            drawGradientRect(x - 1, y - 1, x + 17, y + 17, REFUSE_WASH, REFUSE_WASH);
            GL11.glColorMask(true, true, true, true);
            // lighting stays off: the foreground layer runs without it
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            break;
        }
    }

    /** A part in a build cell is drawn turned like its hint. */
    @Override
    public void func_146977_a /* drawSlot */(Slot slot) {
        int turn = cells.turnOf(slot);
        ItemStack stack = slot.getStack();
        if (turn == 0 || stack == null) {
            super.func_146977_a(slot);
            return;
        }
        int x = slot.xDisplayPosition, y = slot.yDisplayPosition;
        // a bare part has no upright overlay, so the normal slot drawing, and any mod hooking it, can run turned
        boolean bare = stack.stackSize == 1 && !stack.getItem().showDurabilityBar(stack)
                && !(this.field_147007_t && this.field_147008_s.contains(slot));
        if (!bare) {
            // lifted as vanilla lifts a slot's item; NEI's hooks run upright around it, where its coremod puts them
            this.zLevel = 100F;
            itemRender.zLevel = 100F;
            StationNei.underlay(this, slot);
        }
        boolean mirror = (turn & ToolGuiElement.MIRROR) != 0;
        // a mirror reverses the winding face culling keeps
        if (mirror) GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glPushMatrix();
        GL11.glTranslatef(x + 8, y + 8, 0F);
        // written after the turn so it applies first: mirror, then turn left, as the hint is
        GL11.glRotatef(-90F * (turn & 3), 0F, 0F, 1F);
        if (mirror) GL11.glScalef(-1F, 1F, 1F);
        if (bare) {
            GL11.glTranslatef(-x - 8, -y - 8, 0F);
            super.func_146977_a(slot);
        } else {
            // the count is drawn upright, after the pop
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            itemRender.renderItemAndEffectIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), stack, -8, -8);
        }
        GL11.glPopMatrix();
        if (mirror) GL11.glEnable(GL11.GL_CULL_FACE);
        if (bare) return;
        itemRender.renderItemOverlayIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), stack, x, y, null);
        StationNei.overlay(this, slot);
        itemRender.zLevel = 0F;
        this.zLevel = 0F;
    }

    protected void drawInventoryLabel() {
        // the repair pentagon reaches into the label's space
        if (selectedButton == 0) return;
        this.fontRendererObj.drawString(
                StatCollector.translateToLocal("container.inventory"),
                COLUMN_W + 8,
                this.ySize - LABEL_FROM_BOTTOM,
                0x000000);
    }

    void drawSlotPartTooltip(int mouseX, int mouseY) {
        if (components.componentNames == null || !toolSlots.isRestricting() || heldWhileRestricted() != null) return;
        Slot hovered = components.hoveredBuildSlot(mouseX, mouseY);
        if (hovered == null || hovered.getHasStack()) return;
        for (int i = 0; i < components.componentSlots.length; i++) {
            if (components.componentSlots[i] != hovered.getSlotIndex()) continue;
            drawHoveringText(Collections.singletonList(components.componentNames[i]), mouseX, mouseY, fontRendererObj);
            return;
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == 1 || (!this.text.isFocused() && keyCode == this.mc.gameSettings.keyBindInventory.getKeyCode())) {
            logic.setToolname("");
            updateServer("");
            Keyboard.enableRepeatEvents(false);
            this.mc.thePlayer.closeScreen();
        } else if (text.textboxKeyTyped(typedChar, keyCode)) {
            final String toolName = text.getText().trim();
            logic.setToolname(toolName);
            updateServer(toolName);
        } else {
            // for nei
            super.keyTyped(typedChar, keyCode);
        }
    }

    private void updateServer(String name) {
        TConstruct.packetPipeline.sendToServer(new ToolStationPacket(logic.xCoord, logic.yCoord, logic.zCoord, name));
    }

    @Override
    public VisiblityData modifyVisiblity(GuiContainer gui, VisiblityData currentVisibility) {
        currentVisibility.showWidgets = width - xSize >= 107;
        if (guiLeft < 58) {
            currentVisibility.showStateButtons = false;
        }
        return currentVisibility;
    }

    @Override
    public Iterable<Integer> getItemSpawnSlots(GuiContainer gui, ItemStack item) {
        return null;
    }

    @Override
    public List<TaggedInventoryArea> getInventoryAreas(GuiContainer gui) {
        return Collections.emptyList();
    }

    @Override
    public boolean handleDragNDrop(GuiContainer gui, int mousex, int mousey, ItemStack draggedStack, int button) {
        return false;
    }

    /** NEI never asks the GUI itself; StationNEIGuiHandler asks here. */
    @Override
    public boolean hideItemPanelSlot(GuiContainer gui, int x, int y, int w, int h) {
        return panels.covers(new Rectangle(x - guiLeft, y - guiTop, w, h));
    }
}
