package tconstruct.tools.gui;

import static tconstruct.tools.gui.StationButtons.COLUMN_W;
import static tconstruct.tools.gui.StationDraw.background;
import static tconstruct.tools.gui.StationPanels.PANEL_WIDTH_MAX;
import static tconstruct.tools.gui.StationPanels.PANEL_WIDTH_MIN;
import static tconstruct.tools.gui.StationPanels.PANEL_X;
import static tconstruct.tools.gui.StationPanels.UPPER_Y;
import static tconstruct.tools.gui.StationPanels.Y_SIZE;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import tconstruct.TConstruct;
import tconstruct.library.client.TConstructClientRegistry;
import tconstruct.library.client.ToolGuiElement;
import tconstruct.library.modifier.IModifyable;
import tconstruct.library.tools.ToolCore;
import tconstruct.tools.inventory.ToolStationContainer;
import tconstruct.tools.logic.ToolStationLogic;
import tconstruct.util.config.PHConstruct;
import tconstruct.util.network.ToolStationSelectionPacket;

@SideOnly(Side.CLIENT)
public class ToolStationGui extends TinkerStationGui {

    public int[] slotX, slotY, iconX, iconY;
    public String title, body = "";

    ToolGuiElement tabElement;
    ResourceLocation tabSheet;
    private ScrollSwitch scrollSwitch;

    public ToolStationGui(InventoryPlayer inventoryplayer, ToolStationLogic stationlogic, World world, int x, int y,
            int z) {
        super(stationlogic.getGuiContainer(inventoryplayer, world, x, y, z));
        this.logic = stationlogic;
        toolSlots = (ToolStationContainer) inventorySlots;
        cells = new CraftCells(this);
        components = new ComponentList(this);
        panels = new StationPanels(this);
        selectedButton = 0;
        ToolGuiElement repair = tabs().get(0);
        cells.applyTabLayout(repair);
        iconX = repair.iconsX;
        iconY = repair.iconsY;
        title = EnumChatFormatting.UNDERLINE + StatCollector.translateToLocal("gui.toolstation.repair");
        body = StatCollector.translateToLocal("gui.toolforge2");
        Keyboard.enableRepeatEvents(true);
    }

    @Override
    public void initGui() {
        // a resize re-inits this object, and super.initGui centers guiTop off ySize, so it goes back to 166 first
        this.ySize = 166;
        super.initGui();
        this.xSize = 176 + COLUMN_W;
        // assigned after super, so the taller panel grows down from guiTop instead of lifting the whole assembly
        this.ySize = Y_SIZE;
        this.guiLeft = (this.width - 176) / 2 - COLUMN_W;
        // the panels narrow before the assembly slides left; 185 is everything but the column and the panel: a 4 px
        // left margin, 178 to the panel, the 2 px beam cap and a 1 px right margin
        panels.panelWidth = Math.max(PANEL_WIDTH_MIN, Math.min(PANEL_WIDTH_MAX, this.width - (185 + COLUMN_W)));
        int footprintRight = PANEL_X + panels.panelWidth + 2; // through the right beam cap
        this.guiLeft = Math.max(4, Math.min(this.guiLeft, this.width - footprintRight - 1));

        if (this.text == null) {
            this.text = new GuiTextField(this.fontRendererObj, 70 + COLUMN_W, 7, 92, 12);
            this.text.setMaxStringLength(40);
            this.text.setEnableBackgroundDrawing(false);
            this.text.setVisible(true);
            this.text.setCanLoseFocus(true);
            this.text.setFocused(false);
            this.text.setTextColor(0xffffff);
        }

        this.buttonList.clear();
        StationButtons.createToolButtons(this);
        if (tabElement == null && toolSlots.selectedTool != null) StationButtons.showRememberedTab(this);
        // a resize rebuilds the buttons around the tab that is still open
        this.buttonList.get(0).enabled = selectedButton != 0;
        this.buttonList.get(selectedButton).enabled = false;
        // last, so a tab's button index stays its id
        scrollSwitch = new ScrollSwitch(this.guiLeft + panels.right(), this.guiTop + UPPER_Y, theme());
        this.buttonList.add(scrollSwitch);
        // NEI fits its item grid around the panels right after this, before the first frame lays them out
        panels.layoutPanels(fontRendererObj, shownTool());
    }

    /** The tabs in button order, Repair & Modify first. */
    protected List<ToolGuiElement> tabs() {
        return TConstructClientRegistry.toolButtons;
    }

    StationTheme theme() {
        return StationTheme.WOOD;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == scrollSwitch) {
            PHConstruct.setStationPanelScrollBar(!panels.scrolls());
            return;
        }
        if (!(button instanceof GuiButtonTool tab)) return;
        showTab(tab);
        toolSlots.selectTool(tabTool());
        TConstruct.packetPipeline.sendToServer(new ToolStationSelectionPacket(tabTool()));
    }

    void showTab(GuiButtonTool button) {
        this.buttonList.get(selectedButton).enabled = true;
        selectedButton = button.id;
        button.enabled = false;

        ToolGuiElement element = button.element;
        tabElement = element;
        tabSheet = new ResourceLocation(element.domain, element.texture);
        cells.applyTabLayout(element);
        iconX = element.iconsX;
        iconY = element.iconsY;
        title = "§n" + StatCollector.translateToLocal(element.title);
        body = StatCollector.translateToLocal(element.body).replace("\\n", "\n");
        components.setComponents(element.tool);
        // not ySize: a tab is pressed while a click has it widened to the side panels
        cells.placeSlots(Y_SIZE);
    }

    ToolCore tabTool() {
        return tabElement == null ? null : tabElement.tool;
    }

    private ItemStack shownTool() {
        ItemStack output = logic.getStackInSlot(0);
        if (output != null || toolSlots.isRestricting()) return output;
        ItemStack center = logic.getStackInSlot(1);
        return center != null && center.getItem() instanceof IModifyable ? center : null;
    }

    /** The cells' x row then their y row, or null for a type this block has no layout for. */
    protected int[][] slotTypeLayout(int type) {
        return StationSlotLayouts.stationLayout(type);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        this.text.updateCursorCounter();
        // the server's answer to opening can move the container to another tab
        if (tabTool() != toolSlots.selectedTool) StationButtons.showRememberedTab(this);
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRendererObj.drawString(StatCollector.translateToLocal(logic.getInvName()), COLUMN_W + 6, 8, 0x000000);
        drawInventoryLabel();
        this.text.drawTextBox();

        panels.drawPanelText(fontRendererObj, mouseX, mouseY);

        drawRefusedSlot(mouseX, mouseY);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        drawSlotPartTooltip(mouseX, mouseY);
        String caption = panels.cutCaptionAt(mouseX - this.guiLeft, mouseY - this.guiTop);
        if (caption != null) drawHoveringText(Collections.singletonList(caption), mouseX, mouseY, fontRendererObj);
        if (scrollSwitch.func_146115_a()) {
            drawHoveringText(Collections.singletonList(scrollSwitch.tooltip()), mouseX, mouseY, fontRendererObj);
        }
    }

    /**
     * Draw the background layer for the GuiContainer (everything behind the items)
     */
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        panels.layoutPanels(fontRendererObj, shownTool());
        // before the buttons draw, so the switch shows the panels drawn this frame
        scrollSwitch.on = panels.scrolls();
        cells.placeSlots(this.ySize);
        // Draw the background
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(background);
        final int cornerX = this.guiLeft + COLUMN_W;
        this.drawTexturedModalRect(cornerX, this.guiTop, 0, 0, 176, this.ySize);

        GhostPreview.drawGhostPreview(this, fontRendererObj, cornerX);

        // drawn after the ghost's cover, which can reach under the arrow's tail
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(background);
        if (this.text.isFocused()) {
            this.drawTexturedModalRect(cornerX + 68, this.guiTop + 6, 0, 210, 102, 12);
        }
        GhostPreview.drawOutputColumn(this, cornerX);

        cells.drawCells(fontRendererObj, this.zLevel, cornerX, mouseX, mouseY);

        if (toolSlots.isRestricting() && this.mc.thePlayer.inventory.getItemStack() == null) {
            Slot hovered = components.hoveredBuildSlot(mouseX, mouseY);
            if (hovered != null) components.drawInventoryMatches(hovered);
        }

        StationDraw.drawFrames(this, mouseX, mouseY);
    }
}
