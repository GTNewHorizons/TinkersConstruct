package tconstruct.tools.gui;

import java.awt.Rectangle;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import codechicken.nei.VisiblityData;
import codechicken.nei.api.INEIGuiHandler;
import codechicken.nei.api.TaggedInventoryArea;
import cpw.mods.fml.common.Optional;
import tconstruct.TConstruct;
import tconstruct.library.TConstructRegistry;
import tconstruct.library.crafting.PatternBuilder;
import tconstruct.library.modifier.IModifyable;
import tconstruct.library.tools.ToolMaterial;
import tconstruct.library.util.ColorUtils;
import tconstruct.library.util.HarvestLevels;
import tconstruct.tools.logic.CraftingStationLogic;
import tconstruct.util.network.CraftingStationDumpPacket;

@Optional.Interface(iface = "codechicken.nei.api.INEIGuiHandler", modid = "NotEnoughItems")
public class CraftingStationGui extends GuiContainer implements INEIGuiHandler {

    private static final int CRAFTING_WIDTH = 176;
    private static final int CRAFTING_HEIGHT = 166;
    private static final int DESCRIPTION_WIDTH = 126;
    private static final int DESCRIPTION_HEIGHT = 172;
    private static final int DEFAULT_COLUMNS = 6;
    private static final int MIN_COLUMNS = 5;
    private static final int MAX_OVERHANG_ROWS = 7;
    private static final int NEI_VERTICAL_MARGIN = 22;
    private static final int NEI_PANEL_MARGIN = 2;
    private static final int NEI_BOOKMARK_GROUP_WIDTH = 7;
    private static final int MIN_BOOKMARK_COLUMNS = 4;
    private static final int SCROLL_SEPARATOR_HEIGHT = 4;
    private static final int OUTSIDE_SLOT_ID = -999;
    private static final int CLICK_MODE_PICKUP = 0;
    private static final int CLICK_MODE_QUICK_MOVE = 1;
    private static final int CLICK_MODE_THROW = 4;
    // Treat an empty trailing slot as a small layout cost, not a hard constraint.
    private static final double UNUSED_SLOT_SCORE_PENALTY = 0.01D;

    /*
     * Slider/slots related. Taken & adapted from Tinkers Construct 1.12 under the MIT License
     */
    private static final ResourceLocation gui_inventory = new ResourceLocation("tinker", "textures/gui/generic.png");

    public static final GuiElementScalable slotElement = new GuiElementScalable(7, 7, 18, 18, 64, 64);
    public static final GuiElementScalable slotEmptyElement = new GuiElementScalable(7 + 18, 7, 18, 18, 64, 64);
    private static final GuiElementScalable scrollSeparator = new GuiElementScalable(7, 57, 1, 1, 64, 64);

    private static final GuiElementDuex sliderNormal = new GuiElementDuex(7, 25, 10, 15, 64, 64);
    private static final GuiElementDuex sliderLow = new GuiElementDuex(17, 25, 10, 15, 64, 64);
    private static final GuiElementDuex sliderHigh = new GuiElementDuex(27, 25, 10, 15, 64, 64);
    private static final GuiElementDuex sliderTop = new GuiElementDuex(43, 7, 12, 1, 64, 64);
    private static final GuiElementDuex sliderBottom = new GuiElementDuex(43, 38, 12, 1, 64, 64);
    private static final GuiElementScalable sliderBackground = new GuiElementScalable(43, 8, 12, 30, 64, 64);
    private static final GuiElementScalable textBackground = new GuiElementScalable(7 + 18, 7, 18, 10, 64, 64);

    private final GuiSliderWidget slider = new GuiSliderWidget(
            sliderNormal,
            sliderHigh,
            sliderLow,
            sliderTop,
            sliderBottom,
            sliderBackground);
    private final GuiBorderWidget border = new GuiBorderWidget();

    private int firstSlotId;
    private int lastSlotId;
    private int chestSlotCount;

    /* end slider/slots */

    private static final ResourceLocation background = new ResourceLocation("tinker", "textures/gui/tinkertable.png");
    private static final ResourceLocation description = new ResourceLocation("tinker", "textures/gui/description.png");
    private static final ResourceLocation icons = new ResourceLocation("tinker", "textures/gui/icons.png");

    public boolean active;

    // Panel positions
    public String toolName;
    public GuiTextField text;
    public String title, body;
    CraftingStationLogic logic;

    private int craftingLeft = 0;
    private int craftingTop = 0;
    private int craftingTextLeft = 0;
    private int descLeft = 0;
    private int descTop = 0;
    private int descTextLeft = 0;

    private int chestLeft = 0;
    private int chestTop = 0;
    private int chestWidth = 0;
    private ChestLayout chestLayout;

    public CraftingStationGui(InventoryPlayer inventory, CraftingStationLogic logic, World world, int x, int y, int z) {
        super(logic.getGuiContainer(inventory, world, x, y, z));
        this.logic = logic;

        title = "\u00A7n" + StatCollector.translateToLocal("gui.toolforge1");
        body = StatCollector.translateToLocal("gui.toolforge2");
        toolName = "";
    }

    @Override
    public void initGui() {
        super.initGui();

        this.xSize = CRAFTING_WIDTH;
        this.ySize = CRAFTING_HEIGHT;

        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        this.craftingLeft = this.guiLeft;
        this.craftingTop = this.guiTop;

        if (logic.chest != null) {
            updateChest();
        } else {
            slider.hide();
            if (logic.tinkerTable) {
                this.descLeft = this.guiLeft + CRAFTING_WIDTH;
                this.descTop = this.craftingTop;
            }
        }

        this.craftingTextLeft = this.craftingLeft - this.guiLeft;
        this.descTextLeft = this.descLeft - this.guiLeft;

        // Add dump button if chest is connected
        this.buttonList.clear();
        if (logic.chest != null) {
            this.buttonList.add(new GuiButtonDump(0, this.craftingLeft + 161, this.craftingTop + 5));
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0 && logic.chest != null) {
            TConstruct.packetPipeline.sendToServer(new CraftingStationDumpPacket());
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        drawDumpButtonTooltip(mouseX, mouseY);
    }

    private void drawDumpButtonTooltip(int mouseX, int mouseY) {
        for (Object obj : this.buttonList) {
            if (obj instanceof GuiButtonDump) {
                GuiButtonDump button = (GuiButtonDump) obj;
                if (button.func_146115_a()) {
                    this.drawHoveringText(
                            Collections.singletonList(
                                    StatCollector.translateToLocal("craftingstation.dump_button.tooltip")),
                            mouseX,
                            mouseY,
                            this.fontRendererObj);
                    return;
                }
            }
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2) {
        if (logic.chest != null) {
            if (logic.chest.get() instanceof TileEntity te) {
                if (te.getWorldObj().getTileEntity(te.xCoord, te.yCoord, te.zCoord) == null
                        && te.getWorldObj().isRemote) {
                    mc.thePlayer.closeScreen();
                    return;
                }
            }
            this.fontRendererObj.drawString(
                    StatCollector.translateToLocal(logic.chest.get().getInventoryName()),
                    chestLeft - guiLeft + 8,
                    chestTop - guiTop + 6,
                    ColorUtils.inventoryTitle.getColor());
        }

        this.fontRendererObj.drawString(
                StatCollector.translateToLocal(logic.tinkerTable ? "crafters.TinkerTable" : logic.getInvName()),
                craftingTextLeft + 8,
                craftingTop - guiTop + 6,
                ColorUtils.inventoryTitle.getColor());
        this.fontRendererObj.drawString(
                StatCollector.translateToLocal("container.inventory"),
                craftingTextLeft + 8,
                craftingTop - guiTop + 72,
                ColorUtils.inventoryTitle.getColor());

        if (logic.tinkerTable) {
            if (logic.isStackInSlot(0)) // output slot = modified item
                drawToolStats(logic.getStackInSlot(0));
            else if (logic.isStackInSlot(5)) { // center slot if no output item
                // other slots empty?
                if (!logic.isStackInSlot(1) && !logic.isStackInSlot(2)
                        && !logic.isStackInSlot(3)
                        && !logic.isStackInSlot(4)
                        && !logic.isStackInSlot(6)
                        && !logic.isStackInSlot(7)
                        && !logic.isStackInSlot(8)
                        && !logic.isStackInSlot(9))
                    drawToolStats(logic.getStackInSlot(5));
                else drawToolInformation();
            } else drawToolInformation();
        }
    }

    void drawToolStats(ItemStack stack) {
        if (stack == null) return;

        if (stack.getItem() instanceof IModifyable)
            ToolStationGuiHelper.drawToolStats(stack, descTextLeft + 10, descTop - guiTop);

        int matID = PatternBuilder.instance.getPartID(stack);

        if (matID != Short.MAX_VALUE && matID > 0) {
            ToolMaterial material = TConstructRegistry.getMaterial(matID);

            if (material != null) drawMaterialStats(material);
        }
    }

    void drawToolInformation() {
        int offsetX = descTextLeft + 63;
        int offsetY = descTop - guiTop;

        this.drawCenteredString(fontRendererObj, title, offsetX, offsetY + 8, 0xffffff);
        fontRendererObj.drawSplitString(body, offsetX - 56, offsetY + 24, 115, 0xffffff);
    }

    protected void drawMaterialStats(ToolMaterial materialEnum) {
        final int baseX = descTextLeft + 8;
        final int baseY = descTop - guiTop + 8;

        String centerTitle = "\u00A7n" + materialEnum.localizedName();

        drawCenteredString(this.fontRendererObj, centerTitle, baseX + 55, baseY, 16777215);

        this.fontRendererObj.drawString(
                StatCollector.translateToLocal("gui.partcrafter4") + materialEnum.durability(),
                baseX,
                baseY + 16,
                16777215);
        this.fontRendererObj.drawString(
                StatCollector.translateToLocal("gui.partcrafter5") + materialEnum.handleDurability() + "x",
                baseX,
                baseY + 27,
                16777215);
        this.fontRendererObj.drawString(
                StatCollector.translateToLocal("gui.partcrafter6") + materialEnum.toolSpeed() / 100f,
                baseX,
                baseY + 38,
                16777215);
        this.fontRendererObj.drawString(
                StatCollector.translateToLocal("gui.partcrafter7")
                        + HarvestLevels.getHarvestLevelName(materialEnum.harvestLevel()),
                baseX,
                baseY + 49,
                16777215);

        int attack = materialEnum.attack();
        String heart = attack == 2 ? StatCollector.translateToLocal("gui.partcrafter8")
                : StatCollector.translateToLocal("gui.partcrafter9");
        if (attack % 2 == 0) this.fontRendererObj.drawString(
                StatCollector.translateToLocal("gui.partcrafter10") + attack / 2 + heart,
                baseX,
                baseY + 60,
                0xffffff);
        else this.fontRendererObj.drawString(
                StatCollector.translateToLocal("gui.partcrafter10") + attack / 2f + heart,
                baseX,
                baseY + 60,
                0xffffff);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {

        // Draw the background
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(background);
        this.drawTexturedModalRect(this.craftingLeft, this.craftingTop, 0, 0, 176, 166);

        if (active) {
            this.drawTexturedModalRect(this.craftingLeft + 62, this.craftingTop, 0, 166, 112, 22);
        }

        this.mc.getTextureManager().bindTexture(icons);

        // Draw the slots
        if (logic.tinkerTable && !logic.isStackInSlot(5))
            this.drawTexturedModalRect(this.craftingLeft + 47, this.craftingTop + 33, 0, 233, 18, 18);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        this.mc.getTextureManager().bindTexture(gui_inventory);
        if (hasChest()) {
            drawChest();

            // slider
            if (slider.isEnabled()) {
                slider.update(mouseX, mouseY, !isMouseOverFullSlot(mouseX, mouseY) && isMouseInChest(mouseX, mouseY));
                slider.draw();

                updateChestSlots();
            }
        }
        // Draw description
        if (logic.tinkerTable) {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.mc.getTextureManager().bindTexture(description);
            this.drawTexturedModalRect(this.descLeft, this.descTop, 0, 0, 126, 172);
        }
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

    @Override
    public boolean hideItemPanelSlot(GuiContainer gui, int x, int y, int w, int h) {
        Rectangle itemPanelSlot = new Rectangle(x, y, w, h);
        if (intersectsChest(itemPanelSlot)) return true;
        if (new Rectangle(craftingLeft, craftingTop, CRAFTING_WIDTH, CRAFTING_HEIGHT).intersects(itemPanelSlot))
            return true;

        return logic.tinkerTable
                && new Rectangle(descLeft, descTop, DESCRIPTION_WIDTH, DESCRIPTION_HEIGHT).intersects(itemPanelSlot);
    }

    public boolean hasChest() {
        return logic.chest != null;
    }

    public boolean isMouseInChest(int mouseX, int mouseY) {
        return intersectsChest(new Rectangle(mouseX, mouseY, 1, 1));
    }

    public boolean isMouseOverFullSlot(int mouseX, int mouseY) {
        for (final Slot slot : inventorySlots.inventorySlots) {
            if (isMouseOverSlot(slot, mouseX, mouseY) && slot.getHasStack()) {
                return true;
            }
        }
        return false;
    }

    protected boolean shouldDrawName() {
        return this.logic.chest != null && this.logic.chest.get().getInventoryName() != null
                && !this.logic.chest.get().getInventoryName().isEmpty();
    }

    @Override
    public void func_146977_a /* drawSlot */(Slot slot) {
        if (!slot.func_111238_b /* isEnabled */()) return;

        super.func_146977_a(slot);
    }

    public boolean shouldDrawSlot(Slot slot) {
        if (!(slot instanceof ChestSlot chestSlot)) return true;

        // all visible
        if (!slider.isEnabled()) return true;

        if (chestLayout.lShaped && chestSlot.getVisualIndex() < chestLayout.topCapacity) return true;

        return firstSlotId <= chestSlot.getVisualIndex() && lastSlotId > chestSlot.getVisualIndex();
    }

    @Override
    public boolean isMouseOverSlot(Slot slotIn, int mouseX, int mouseY) {
        return super.isMouseOverSlot(slotIn, mouseX, mouseY) && shouldDrawSlot(slotIn);
    }

    @Override
    protected void handleMouseClick(Slot slot, int slotId, int button, int mode) {
        if (slotId == OUTSIDE_SLOT_ID && slot instanceof ChestSlot && shouldDrawSlot(slot)) {
            slotId = slot.slotNumber;
            if (mode == CLICK_MODE_THROW) mode = CLICK_MODE_PICKUP;
            if (mode == CLICK_MODE_PICKUP && isShiftKeyDown()) mode = CLICK_MODE_QUICK_MOVE;
        }

        super.handleMouseClick(slot, slotId, button, mode);
    }

    private int getDisplayedRows() {
        return chestLayout.visibleRows;
    }

    // updatePosition
    public void updateChest() {
        chestSlotCount = logic.slotCount;
        chestLayout = selectChestLayout();

        if (chestLayout.scrolling) {
            slider.enable();
            slider.show();
        } else {
            slider.disable();
            slider.hide();
        }

        this.xSize = CRAFTING_WIDTH;
        this.ySize = CRAFTING_HEIGHT;
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        this.craftingLeft = guiLeft;
        this.craftingTop = guiTop;
        this.chestLeft = craftingLeft - chestLayout.lowerPanelWidth;
        this.chestTop = craftingTop - chestLayout.craftingTopOffset + chestLayout.chestTopOffset;
        this.chestWidth = chestLayout.lowerPanelWidth;
        this.descLeft = craftingLeft + CRAFTING_WIDTH;
        this.descTop = craftingTop;

        configureChestWidgets();
        positionMainSlots();

        updateChestSlots();
    }

    // updates slot visibility
    protected void updateChestSlots() {
        if (!hasChest()) return;

        int xOffset = chestLeft - guiLeft + border.w;
        int yOffset = chestTop - guiTop + border.h + getHeaderHeight();

        firstSlotId = chestLayout.scrolling
                ? (chestLayout.lShaped ? chestLayout.topCapacity : 0) + slider.getValue() * chestLayout.columns
                : 0;
        lastSlotId = chestLayout.scrolling
                ? Math.min(chestSlotCount, firstSlotId + getDisplayedRows() * chestLayout.columns)
                : chestSlotCount;

        for (Object o : inventorySlots.inventorySlots) {
            if (!(o instanceof ChestSlot slot)) continue;

            if (shouldDrawSlot(slot)) {
                slot.enable();
                positionChestSlot(slot, xOffset, yOffset);
            } else {
                slot.disable();
                slot.xDisplayPosition = 0;
                slot.yDisplayPosition = 0;
            }
        }
    }

    private void positionChestSlot(ChestSlot slot, int xOffset, int yOffset) {
        int visualIndex = slot.getVisualIndex();
        final int x;
        final int y;

        if (!chestLayout.lShaped) {
            int offset = visualIndex - firstSlotId;
            x = (offset % chestLayout.columns) * slotElement.w;
            y = (offset / chestLayout.columns) * slotElement.h;
        } else if (visualIndex < chestLayout.topCapacity) {
            x = (visualIndex % chestLayout.topColumns) * slotElement.w;
            y = (visualIndex / chestLayout.topColumns) * slotElement.h;
        } else {
            int offset = visualIndex - (chestLayout.scrolling ? firstSlotId : chestLayout.topCapacity);
            x = (offset % chestLayout.columns) * slotElement.w;
            y = (chestLayout.overhangRows + offset / chestLayout.columns) * slotElement.h + chestLayout.separatorHeight;
        }

        slot.xDisplayPosition = x + xOffset + 1;
        slot.yDisplayPosition = y + yOffset + 1;
    }

    // drawSlots
    protected void drawChestSlots(int xPos, int yPos) {
        if (!hasChest()) return;

        if (chestLayout.lShaped) {
            drawSlotRows(xPos, yPos, chestLayout.topColumns, Math.min(chestSlotCount, chestLayout.topCapacity));
            drawSlotRows(
                    xPos,
                    yPos + chestLayout.overhangRows * slotElement.h + chestLayout.separatorHeight,
                    chestLayout.columns,
                    chestLayout.scrolling ? lastSlotId - firstSlotId
                            : Math.max(0, chestSlotCount - chestLayout.topCapacity));
            if (chestLayout.scrolling) {
                int separatorY = yPos + chestLayout.overhangRows * slotElement.h;
                scrollSeparator.drawScaled(
                        xPos,
                        separatorY,
                        chestLayout.topColumns * slotElement.w,
                        chestLayout.separatorHeight);
            }
        } else {
            drawSlotRows(xPos, yPos, chestLayout.columns, lastSlotId - firstSlotId);
        }
    }

    private void drawChest() {
        if (chestLayout.lShaped) {
            drawLShapedBorder();
        } else {
            border.draw();
        }

        int x = chestLeft + border.w;
        int y = chestTop + border.h;

        if (shouldDrawName()) {
            int titleWidth = chestLayout.lShaped ? chestLayout.topColumns * slotElement.w
                    : chestLayout.panelWidth - border.w * 2;
            textBackground.drawScaledX(x, y, titleWidth);
            y += textBackground.h;
        }

        drawChestSlots(x, y);
    }

    private void drawLShapedBorder() {
        int wideRight = chestLeft + chestLayout.panelWidth;
        int lowerRight = chestLeft + chestLayout.lowerPanelWidth;
        int elbowY = chestTop + border.h
                + getHeaderHeight()
                + chestLayout.overhangRows * slotElement.h
                + chestLayout.separatorHeight;
        int bottom = chestTop + chestLayout.panelHeight;

        border.cornerTopLeft.draw(chestLeft, chestTop);
        border.borderTop.drawScaledX(chestLeft + border.w, chestTop, chestLayout.panelWidth - border.w * 2);
        border.cornerTopRight.draw(wideRight - border.w, chestTop);

        border.borderLeft.drawScaledY(chestLeft, chestTop + border.h, chestLayout.panelHeight - border.h * 2);
        border.borderRight.drawScaledY(wideRight - border.w, chestTop + border.h, elbowY - chestTop - border.h);
        border.cornerBottomRight.draw(wideRight - border.w, elbowY);
        border.borderBottom.drawScaledX(lowerRight, elbowY, wideRight - lowerRight - border.w);
        border.drawConcaveBottomRight(lowerRight - border.w, elbowY);
        border.borderRight.drawScaledY(lowerRight - border.w, elbowY + border.h, bottom - elbowY - border.h * 2);

        border.cornerBottomLeft.draw(chestLeft, bottom - border.h);
        border.borderBottom
                .drawScaledX(chestLeft + border.w, bottom - border.h, chestLayout.lowerPanelWidth - border.w * 2);
        border.cornerBottomRight.draw(lowerRight - border.w, bottom - border.h);
    }

    private void drawSlotRows(int x, int y, int columns, int slotCount) {
        if (slotCount <= 0) return;

        int width = columns * slotElement.w;
        int fullRows = slotCount / columns;
        int slotsLeft = slotCount % columns;

        for (int row = 0; row < fullRows; row++) {
            slotElement.drawScaledX(x, y + row * slotElement.h, width);
        }

        if (slotsLeft > 0) {
            int rowY = y + fullRows * slotElement.h;
            slotElement.drawScaledX(x, rowY, slotsLeft * slotElement.w);
            slotEmptyElement.drawScaledX(x + slotsLeft * slotElement.w, rowY, width - slotsLeft * slotElement.w);
        }
    }

    private void configureChestWidgets() {
        int headerHeight = getHeaderHeight();

        border.setPosition(chestLeft, chestTop);
        border.setSize(chestLayout.panelWidth, chestLayout.panelHeight);

        if (chestLayout.scrolling) {
            int sliderTopOffset = chestLayout.lShaped
                    ? chestLayout.overhangRows * slotElement.h + chestLayout.separatorHeight
                    : 0;
            slider.setPosition(
                    chestLeft + border.w + chestLayout.columns * slotElement.w,
                    chestTop + border.h + headerHeight + sliderTopOffset);
            slider.setSize(chestLayout.visibleRows * slotElement.h);
            slider.setSliderParameters(0, chestLayout.totalRows - chestLayout.visibleRows, 1);
        }
    }

    private void positionMainSlots() {
        int xOffset = craftingLeft - guiLeft;
        int yOffset = craftingTop - guiTop;

        setSlotPosition(0, xOffset + 124, yOffset + 35);

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                setSlotPosition(1 + column + row * 3, xOffset + 30 + column * 18, yOffset + 17 + row * 18);
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                setSlotPosition(10 + column + row * 9, xOffset + 8 + column * 18, yOffset + 84 + row * 18);
            }
        }

        for (int column = 0; column < 9; column++) {
            setSlotPosition(37 + column, xOffset + 8 + column * 18, yOffset + 142);
        }
    }

    private void setSlotPosition(int index, int x, int y) {
        Slot slot = inventorySlots.getSlot(index);
        slot.xDisplayPosition = x;
        slot.yDisplayPosition = y;
    }

    private boolean intersectsChest(Rectangle rectangle) {
        if (!hasChest() || chestLayout == null) return false;

        if (!chestLayout.lShaped) {
            return new Rectangle(chestLeft, chestTop, chestLayout.panelWidth, chestLayout.panelHeight)
                    .intersects(rectangle);
        }

        int headerHeight = getHeaderHeight();
        int topSectionHeight = border.h + headerHeight
                + chestLayout.overhangRows * slotElement.h
                + chestLayout.separatorHeight
                + border.h;
        Rectangle top = new Rectangle(chestLeft, chestTop, chestLayout.panelWidth, topSectionHeight);
        Rectangle lower = new Rectangle(
                chestLeft,
                chestTop + topSectionHeight - border.h,
                chestLayout.lowerPanelWidth,
                chestLayout.panelHeight - topSectionHeight + border.h);
        return top.intersects(rectangle) || lower.intersects(rectangle);
    }

    private int getHeaderHeight() {
        return shouldDrawName() ? textBackground.h : 0;
    }

    private ChestLayout selectChestLayout() {
        int craftingLeft = (width - CRAFTING_WIDTH) / 2;
        int bookmarkWidth = NEI_PANEL_MARGIN + NEI_BOOKMARK_GROUP_WIDTH + MIN_BOOKMARK_COLUMNS * slotElement.w;
        int availableChestWidth = Math.max(1, craftingLeft - bookmarkWidth);
        int availableHeight = Math.max(1, height - NEI_VERTICAL_MARGIN * 2);
        int headerHeight = getHeaderHeight();
        int descriptionWidth = logic.tinkerTable ? DESCRIPTION_WIDTH : 0;
        int sideHeight = logic.tinkerTable ? DESCRIPTION_HEIGHT : CRAFTING_HEIGHT;
        int availableWidth = availableChestWidth + CRAFTING_WIDTH + descriptionWidth;
        int maxColumns = Math.max(MIN_COLUMNS, (availableChestWidth - border.w * 2) / slotElement.w);

        // Preserve the original six-column layout for standard connected inventories.
        if (chestSlotCount <= DEFAULT_COLUMNS * 10) {
            return createRectangularLayout(
                    DEFAULT_COLUMNS,
                    ceilDiv(chestSlotCount, DEFAULT_COLUMNS),
                    false,
                    headerHeight,
                    descriptionWidth,
                    sideHeight,
                    Integer.MAX_VALUE,
                    Integer.MAX_VALUE);
        }

        // Prefer a complete layout that fits, so scrolling remains a fallback.
        ChestLayout best = null;
        for (int columns = MIN_COLUMNS; columns <= maxColumns; columns++) {
            ChestLayout rectangle = createRectangularLayout(
                    columns,
                    ceilDiv(chestSlotCount, columns),
                    false,
                    headerHeight,
                    descriptionWidth,
                    sideHeight,
                    availableWidth,
                    availableHeight);
            best = selectBetterCompleteLayout(best, rectangle);

            for (int overhangRows = 1; overhangRows <= MAX_OVERHANG_ROWS; overhangRows++) {
                ChestLayout lShape = createLShapedLayout(
                        columns,
                        overhangRows,
                        Integer.MAX_VALUE,
                        false,
                        headerHeight,
                        descriptionWidth,
                        sideHeight,
                        availableWidth,
                        availableHeight);
                best = selectBetterCompleteLayout(best, lShape);
            }
        }

        if (best != null) return best;

        // If no complete layout fits, maximize the visible capacity of a scrolling layout.
        ChestLayout fallback = null;
        int maxVisibleRows = Math.max(1, (availableHeight - headerHeight - border.h * 2) / slotElement.h);
        int centeredCraftingTop = (height - CRAFTING_HEIGHT) / 2;
        int maxVisibleLowerRows = Math.max(1, (height - NEI_VERTICAL_MARGIN - centeredCraftingTop) / slotElement.h);
        for (int columns = MIN_COLUMNS; columns <= maxColumns; columns++) {
            ChestLayout candidate = createRectangularLayout(
                    columns,
                    maxVisibleRows,
                    true,
                    headerHeight,
                    descriptionWidth,
                    sideHeight,
                    availableWidth,
                    availableHeight);
            fallback = selectBetterScrollingLayout(fallback, candidate);

            for (int overhangRows = 1; overhangRows <= MAX_OVERHANG_ROWS; overhangRows++) {
                ChestLayout lShape = createLShapedLayout(
                        columns,
                        overhangRows,
                        maxVisibleLowerRows,
                        true,
                        headerHeight,
                        descriptionWidth,
                        sideHeight,
                        availableWidth,
                        availableHeight);
                fallback = selectBetterScrollingLayout(fallback, lShape);
            }
        }

        if (fallback != null) return fallback;

        // Last resort for screens that cannot fit even the reserved minimum layout.
        return createRectangularLayout(
                MIN_COLUMNS,
                maxVisibleRows,
                true,
                headerHeight,
                descriptionWidth,
                sideHeight,
                Integer.MAX_VALUE,
                Integer.MAX_VALUE);
    }

    private ChestLayout createRectangularLayout(int columns, int visibleRows, boolean scrolling, int headerHeight,
            int descriptionWidth, int sideHeight, int availableWidth, int availableHeight) {
        int panelWidth = columns * slotElement.w + border.w * 2 + (scrolling ? slider.width : 0);
        int panelHeight = visibleRows * slotElement.h + headerHeight + border.h * 2;
        int combinedWidth = panelWidth + CRAFTING_WIDTH + descriptionWidth;
        int combinedHeight = Math.max(panelHeight, sideHeight);
        int chestTopOffset = (combinedHeight - panelHeight) / 2;
        int craftingTopOffset = (combinedHeight - sideHeight) / 2;
        int totalRows = ceilDiv(chestSlotCount, columns);
        int visibleCapacity = columns * visibleRows;
        int unusedSlots = Math.max(0, visibleCapacity - chestSlotCount);

        return new ChestLayout(
                false,
                scrolling,
                columns,
                columns,
                0,
                0,
                0,
                visibleRows,
                totalRows,
                panelWidth,
                panelWidth,
                panelHeight,
                combinedHeight,
                chestTopOffset,
                craftingTopOffset,
                visibleCapacity,
                unusedSlots,
                score(combinedWidth, combinedHeight, availableWidth, availableHeight),
                combinedWidth <= availableWidth && fitsVertically(combinedHeight, craftingTopOffset));
    }

    private ChestLayout createLShapedLayout(int columns, int overhangRows, int visibleLowerRows, boolean scrolling,
            int headerHeight, int descriptionWidth, int sideHeight, int availableWidth, int availableHeight) {
        int overhangColumns = ceilDiv(CRAFTING_WIDTH, slotElement.w);
        int topColumns = columns + overhangColumns;
        int topCapacity = topColumns * overhangRows;
        int remainingSlots = chestSlotCount - topCapacity;
        if (remainingSlots <= 0) return null;

        int totalLowerRows = ceilDiv(remainingSlots, columns);
        if (!scrolling) visibleLowerRows = totalLowerRows;
        int separatorHeight = scrolling ? SCROLL_SEPARATOR_HEIGHT : 0;

        int panelWidth = topColumns * slotElement.w + border.w * 2;
        int lowerPanelWidth = columns * slotElement.w + border.w * 2 + (scrolling ? slider.width : 0);
        int panelHeight = (overhangRows + visibleLowerRows) * slotElement.h + separatorHeight
                + headerHeight
                + border.h * 2;
        int craftingTopOffset = border.h + headerHeight + overhangRows * slotElement.h + separatorHeight + border.h;
        int combinedWidth = Math.max(panelWidth, lowerPanelWidth + CRAFTING_WIDTH + descriptionWidth);
        int combinedHeight = Math.max(panelHeight, craftingTopOffset + sideHeight);
        int visibleCapacity = topCapacity + visibleLowerRows * columns;
        double screenUsage = score(combinedWidth, combinedHeight, availableWidth, availableHeight);

        return new ChestLayout(
                true,
                scrolling,
                columns,
                topColumns,
                topCapacity,
                overhangRows,
                separatorHeight,
                visibleLowerRows,
                totalLowerRows,
                panelWidth,
                lowerPanelWidth,
                panelHeight,
                combinedHeight,
                0,
                craftingTopOffset,
                visibleCapacity,
                scrolling ? 0 : visibleCapacity - chestSlotCount,
                screenUsage,
                combinedWidth <= availableWidth && fitsVertically(combinedHeight, craftingTopOffset));
    }

    private boolean fitsVertically(int combinedHeight, int craftingTopOffset) {
        int centeredCraftingTop = (height - CRAFTING_HEIGHT) / 2;
        int combinedTop = centeredCraftingTop - craftingTopOffset;
        return combinedTop >= NEI_VERTICAL_MARGIN && combinedTop + combinedHeight <= height - NEI_VERTICAL_MARGIN;
    }

    private ChestLayout selectBetterCompleteLayout(ChestLayout current, ChestLayout candidate) {
        if (candidate == null || !candidate.fits) return current;
        if (current == null) return candidate;

        double currentScore = current.score + current.unusedSlots * UNUSED_SLOT_SCORE_PENALTY;
        double candidateScore = candidate.score + candidate.unusedSlots * UNUSED_SLOT_SCORE_PENALTY;
        if (candidateScore < currentScore) return candidate;
        if (candidateScore > currentScore) return current;
        if (candidate.unusedSlots < current.unusedSlots) return candidate;
        if (candidate.unusedSlots > current.unusedSlots) return current;
        if (!candidate.lShaped && current.lShaped) return candidate;
        return current;
    }

    private ChestLayout selectBetterScrollingLayout(ChestLayout current, ChestLayout candidate) {
        if (candidate == null || !candidate.fits) return current;
        if (current == null || candidate.visibleCapacity > current.visibleCapacity) return candidate;
        if (candidate.visibleCapacity < current.visibleCapacity) return current;
        if (candidate.score < current.score) return candidate;
        return current;
    }

    private static double score(int width, int height, int availableWidth, int availableHeight) {
        return Math.max((double) width / availableWidth, (double) height / availableHeight);
    }

    private static int ceilDiv(int value, int divisor) {
        return (value + divisor - 1) / divisor;
    }

    /** Geometry shared by rendering, slot positioning, hit testing, and NEI integration. */
    private static class ChestLayout {

        private final boolean lShaped;
        private final boolean scrolling;
        private final int columns;
        private final int topColumns;
        private final int topCapacity;
        private final int overhangRows;
        private final int separatorHeight;
        private final int visibleRows;
        private final int totalRows;
        private final int panelWidth;
        private final int lowerPanelWidth;
        private final int panelHeight;
        private final int combinedHeight;
        private final int chestTopOffset;
        private final int craftingTopOffset;
        private final int visibleCapacity;
        private final int unusedSlots;
        private final double score;
        private final boolean fits;

        private ChestLayout(boolean lShaped, boolean scrolling, int columns, int topColumns, int topCapacity,
                int overhangRows, int separatorHeight, int visibleRows, int totalRows, int panelWidth,
                int lowerPanelWidth, int panelHeight, int combinedHeight, int chestTopOffset, int craftingTopOffset,
                int visibleCapacity, int unusedSlots, double score, boolean fits) {
            this.lShaped = lShaped;
            this.scrolling = scrolling;
            this.columns = columns;
            this.topColumns = topColumns;
            this.topCapacity = topCapacity;
            this.overhangRows = overhangRows;
            this.separatorHeight = separatorHeight;
            this.visibleRows = visibleRows;
            this.totalRows = totalRows;
            this.panelWidth = panelWidth;
            this.lowerPanelWidth = lowerPanelWidth;
            this.panelHeight = panelHeight;
            this.combinedHeight = combinedHeight;
            this.chestTopOffset = chestTopOffset;
            this.craftingTopOffset = craftingTopOffset;
            this.visibleCapacity = visibleCapacity;
            this.unusedSlots = unusedSlots;
            this.score = score;
            this.fits = fits;
        }
    }

    private static class GuiButtonDump extends GuiButton {

        private GuiButtonDump(int id, int x, int y) {
            super(id, x, y, 10, 10, "");
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY) {
            if (this.visible) {
                this.field_146123_n = mouseX >= this.xPosition && mouseY >= this.yPosition
                        && mouseX < this.xPosition + this.width
                        && mouseY < this.yPosition + this.height;

                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                mc.getTextureManager().bindTexture(CraftingStationGui.icons);
                int v = this.field_146123_n ? 213 : 223;
                drawTexturedModalRect(this.xPosition, this.yPosition, 0, v, this.width, this.height);
            }
        }
    }

    /*
     * Hide the deprecated stuff at the bottom
     */
    @Deprecated
    public static final int CHEST_WIDTH = 116;
}
