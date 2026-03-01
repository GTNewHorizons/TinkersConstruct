package tconstruct.tools.gui;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import codechicken.nei.VisiblityData;
import codechicken.nei.api.INEIGuiHandler;
import codechicken.nei.api.TaggedInventoryArea;
import cpw.mods.fml.common.Optional;
import tconstruct.library.TConstructRegistry;
import tconstruct.library.crafting.PatternBuilder;
import tconstruct.library.tools.ArrowMaterial;
import tconstruct.library.tools.BowMaterial;
import tconstruct.library.tools.ToolMaterial;
import tconstruct.library.util.HarvestLevels;
import tconstruct.library.weaponry.ArrowShaftMaterial;
import tconstruct.tools.TinkerTools.MaterialID;
import tconstruct.tools.inventory.PartCrafterChestContainer;
import tconstruct.tools.logic.PartBuilderLogic;
import tconstruct.util.McTextFormatter;

@Optional.Interface(iface = "codechicken.nei.api.INEIGuiHandler", modid = "NotEnoughItems")
public class PartCrafterGui extends GuiContainer implements INEIGuiHandler {

    PartBuilderLogic logic;
    String title, otherTitle = "";
    boolean drawChestPart;
    boolean hasTop;
    ItemStack topMaterial;
    ToolMaterial topEnum, bottomEnum;
    ArrowMaterial arrowTop;
    BowMaterial bowTop;
    ArrowShaftMaterial arrowShaftTop;

    private static final int CRAFT_WIDTH = 176;
    private static final int CRAFT_HEIGHT = 166;
    private static final int DESC_WIDTH = 126;
    private static final int DESC_HEIGHT = 166;
    private static final int CHEST_WIDTH = 122;
    private static final int CHEST_HEIGHT = 114;

    // Panel positions

    private int craftingLeft = 0;
    private int craftingTop = 0;
    private int craftingTextLeft = 0;

    private int descLeft = 0;
    private int descTop = 0;
    private int descTextLeft = 0;

    private int chestLeft = 0;
    private int chestTop = 0;

    private static DecimalFormat df = new DecimalFormat("##.##");

    public PartCrafterGui(InventoryPlayer inventoryplayer, PartBuilderLogic partlogic, World world, int x, int y,
            int z) {
        super(partlogic.getGuiContainer(inventoryplayer, world, x, y, z));
        logic = partlogic;
        drawChestPart = inventorySlots instanceof PartCrafterChestContainer;

        title = McTextFormatter.addUnderLine(StatCollector.translateToLocal("gui.partcrafter1"));
    }

    @Override
    public void initGui() {
        super.initGui();

        this.xSize = CRAFT_WIDTH;
        this.ySize = CRAFT_HEIGHT;

        this.craftingLeft = (this.width - CRAFT_WIDTH) / 2;
        this.craftingTop = (this.height - CRAFT_HEIGHT) / 2;

        this.guiLeft = this.craftingLeft;
        this.guiTop = this.craftingTop;

        this.descLeft = this.craftingLeft + CRAFT_WIDTH;
        this.descTop = this.craftingTop;

        if (drawChestPart) {
            this.xSize += CHEST_WIDTH - 6;
            this.guiLeft -= CHEST_WIDTH - 6;

            this.chestLeft = this.guiLeft;
            this.chestTop = this.guiTop + 11;
        }

        this.craftingTextLeft = this.craftingLeft - this.guiLeft;
        this.descTextLeft = this.descLeft - this.guiLeft;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2) {
        if (drawChestPart) {
            this.fontRendererObj.drawString(StatCollector.translateToLocal("inventory.PatternChest"), 8, 17, 0x404040);
        }

        this.fontRendererObj
                .drawString(StatCollector.translateToLocal("crafters.PartBuilder"), craftingTextLeft + 6, 6, 0x404040);
        this.fontRendererObj.drawString(
                StatCollector.translateToLocal("container.inventory"),
                craftingTextLeft + 8,
                this.ySize - 96 + 2,
                0x404040);

        drawMaterialInformation();
    }

    void drawDefaultInformation() {
        title = McTextFormatter.addUnderLine(StatCollector.translateToLocal("gui.partcrafter2"));
        this.drawCenteredString(fontRendererObj, title, descTextLeft + DESC_WIDTH / 2, 8, 0xFFFFFF);
        fontRendererObj.drawSplitString(
                StatCollector.translateToLocal("gui.partcrafter3"),
                descTextLeft + 8,
                24,
                115,
                0xFFFFFF);
    }

    void drawMaterialInformation() {
        ItemStack met = logic.getStackInSlot(2) != null ? logic.getStackInSlot(2) : logic.getStackInSlot(3);

        if (topMaterial != met) {
            topMaterial = met;
            int topID = PatternBuilder.instance.getPartID(met);

            if (topID != Short.MAX_VALUE) // && topResult != null)
            {
                topEnum = TConstructRegistry.getMaterial(topID);
                arrowTop = TConstructRegistry.getArrowMaterial(topID);
                bowTop = TConstructRegistry.getBowMaterial(topID);
                arrowShaftTop = topID <= MaterialID.Wood
                        ? (ArrowShaftMaterial) TConstructRegistry.getCustomMaterial(topID, ArrowShaftMaterial.class)
                        : null;

                hasTop = true;
                title = McTextFormatter.addUnderLine(topEnum.localizedName());
            } else hasTop = false;
        }

        int offset = 6;
        if (hasTop) {
            this.drawCenteredString(
                    fontRendererObj,
                    title,
                    descTextLeft + DESC_WIDTH / 2,
                    offset,
                    topEnum.primaryColor());
            offset += 14;

            GL11.glPushMatrix();
            GL11.glScaled(0.95f, 0.95f, 1.0f);
            int scaledDescTextLeft = (int) (descTextLeft / 0.95) + 7;

            this.fontRendererObj.drawString(
                    StatCollector.translateToLocal("gui.partcrafter.durability")
                            + McTextFormatter.addGreen(String.valueOf(topEnum.durability())),
                    scaledDescTextLeft,
                    offset,
                    0xFFFFFF);
            offset += 11;

            this.fontRendererObj.drawString(
                    StatCollector.translateToLocal("gui.partcrafter.mininglevel")
                            + HarvestLevels.getHarvestLevelName(topEnum.harvestLevel()),
                    scaledDescTextLeft,
                    offset,
                    0xFFFFFF);
            offset += 11;

            this.fontRendererObj.drawString(
                    StatCollector.translateToLocal("gui.partcrafter.miningspeed")
                            + McTextFormatter.addAqua(String.valueOf(topEnum.toolSpeed() / 100f)),
                    scaledDescTextLeft,
                    offset,
                    0xFFFFFF);
            offset += 11;

            this.fontRendererObj.drawString(
                    StatCollector.translateToLocal("gui.partcrafter.attack")
                            + McTextFormatter.addRed(String.valueOf(topEnum.attack())),
                    scaledDescTextLeft,
                    offset,
                    0xFFFFFF);
            offset += 11;

            this.fontRendererObj.drawString(
                    StatCollector.translateToLocal("gui.partcrafter.handlemodifier")
                            + McTextFormatter.addYellow(String.valueOf(topEnum.handleDurability())),
                    scaledDescTextLeft,
                    offset,
                    0xFFFFFF);
            offset += 11;

            this.fontRendererObj.drawString(
                    StatCollector.translateToLocal("gui.partcrafter.drawspeed")
                            + McTextFormatter.addGray(df.format(bowTop.drawspeed / 20f) + "s"),
                    scaledDescTextLeft,
                    offset,
                    0xFFFFFF);
            offset += 11;

            this.fontRendererObj.drawString(
                    StatCollector.translateToLocal("gui.partcrafter.arrowspeed")
                            + McTextFormatter.addGray(df.format(bowTop.flightSpeedMax)),
                    scaledDescTextLeft,
                    offset,
                    0xFFFFFF);
            offset += 11;

            if (arrowShaftTop != null) {

                this.fontRendererObj.drawString(
                        McTextFormatter.addUnderLine(StatCollector.translateToLocal("gui.partcrafter.arrow")),
                        scaledDescTextLeft,
                        offset,
                        0xFFFFFF);
                offset += 11;

                this.fontRendererObj.drawString(
                        StatCollector.translateToLocal("gui.partcrafter.weight")
                                + McTextFormatter.addYellow(df.format(arrowShaftTop.weight)),
                        scaledDescTextLeft + 4,
                        offset,
                        0xFFFFFF);
                offset += 11;

                this.fontRendererObj.drawString(
                        StatCollector.translateToLocal("gui.partcrafter.breakchance")
                                + McTextFormatter.addYellow(df.format(arrowShaftTop.fragility * 100f) + "%"),
                        scaledDescTextLeft + 4,
                        offset,
                        0xFFFFFF);
                offset += 11;
            }

            this.fontRendererObj.drawString(
                    McTextFormatter.addUnderLine(StatCollector.translateToLocal("gui.partcrafter.bolt")),
                    scaledDescTextLeft,
                    offset,
                    0xFFFFFF);
            offset += 11;

            this.fontRendererObj.drawString(
                    StatCollector.translateToLocal("gui.partcrafter.weight")
                            + McTextFormatter.addYellow(df.format(arrowTop.mass)),
                    scaledDescTextLeft + 4,
                    offset,
                    0xFFFFFF);
            offset += 11;

            this.fontRendererObj.drawString(
                    StatCollector.translateToLocal("gui.partcrafter.breakchance")
                            + McTextFormatter.addYellow(df.format(arrowTop.breakChance * 100f) + "%"),
                    scaledDescTextLeft + 4,
                    offset,
                    0xFFFFFF);
            offset += 11;

            GL11.glPopMatrix();
        }
        if (!hasTop) drawDefaultInformation();
    }

    private static final ResourceLocation background = new ResourceLocation("tinker", "textures/gui/toolparts.png");
    private static final ResourceLocation minichest = new ResourceLocation(
            "tinker",
            "textures/gui/patternchestmini.png");
    private static final ResourceLocation description = new ResourceLocation("tinker", "textures/gui/description.png");

    @Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
        // Draw the background
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(background);
        this.drawTexturedModalRect(craftingLeft, craftingTop, 0, 0, CRAFT_WIDTH, CRAFT_HEIGHT);

        // Draw Slots
        this.drawTexturedModalRect(craftingLeft + 39, craftingTop + 26, 0, 166, 98, 36);
        if (!logic.isStackInSlot(0)) {
            this.drawTexturedModalRect(craftingLeft + 39, craftingTop + 26, 176, 0, 18, 18);
        }
        if (!logic.isStackInSlot(2)) {
            this.drawTexturedModalRect(craftingLeft + 57, craftingTop + 26, 176, 18, 18, 18);
        }
        if (!logic.isStackInSlot(1)) {
            this.drawTexturedModalRect(craftingLeft + 39, craftingTop + 44, 176, 0, 18, 18);
        }
        if (!logic.isStackInSlot(3)) {
            this.drawTexturedModalRect(craftingLeft + 57, craftingTop + 44, 176, 36, 18, 18);
        }

        // Draw chest
        if (drawChestPart) {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.mc.getTextureManager().bindTexture(minichest);
            this.drawTexturedModalRect(chestLeft, chestTop, 0, 0, CHEST_WIDTH, CHEST_HEIGHT);
        }

        // Draw description
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(description);
        this.drawTexturedModalRect(descLeft, descTop, DESC_WIDTH, 0, DESC_WIDTH, DESC_HEIGHT);
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
        if (y + h - 4 < guiTop || y + 4 > guiTop + ySize) return false;
        return x - w - 4 >= guiLeft - 40 && x + 4 <= guiLeft + xSize + DESC_WIDTH;
    }
}
