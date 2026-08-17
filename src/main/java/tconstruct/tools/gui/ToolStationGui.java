package tconstruct.tools.gui;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import codechicken.nei.VisiblityData;
import codechicken.nei.api.INEIGuiHandler;
import codechicken.nei.api.TaggedInventoryArea;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import tconstruct.TConstruct;
import tconstruct.library.client.TConstructClientRegistry;
import tconstruct.library.client.ToolGuiElement;
import tconstruct.tools.inventory.ToolStationContainer;
import tconstruct.tools.logic.ToolStationLogic;
import tconstruct.util.network.ToolStationPacket;

@SideOnly(Side.CLIENT)
@Optional.Interface(iface = "codechicken.nei.api.INEIGuiHandler", modid = "NotEnoughItems")
public class ToolStationGui extends GuiContainer implements INEIGuiHandler {

    public ToolStationLogic logic;
    public ToolStationContainer toolSlots;
    public GuiTextField text;
    public int selectedButton;
    public int[] slotX, slotY, iconX, iconY;
    public String title, body = "";

    private static final RenderItem ghostRender = new RenderItem();

    // Where extra modifier slots sit while a tool-building layout is selected
    protected static final int PARK_X1 = 87, PARK_X2 = 107, PARK_X3 = 127, PARK_Y = 62;

    public ToolStationGui(InventoryPlayer inventoryplayer, ToolStationLogic stationlogic, World world, int x, int y,
            int z) {
        super(stationlogic.getGuiContainer(inventoryplayer, world, x, y, z));
        this.logic = stationlogic;
        toolSlots = (ToolStationContainer) inventorySlots;
        selectedButton = 0;
        setSlotType(0);
        setIconUVs();
        title = EnumChatFormatting.UNDERLINE + StatCollector.translateToLocal("gui.toolforge1");
        body = StatCollector.translateToLocal("gui.toolforge2");
        Keyboard.enableRepeatEvents(true);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.text.mouseClicked(mouseX - this.guiLeft, mouseY - this.guiTop, mouseButton);
    }

    protected void setIconUVs() {
        setRepairIconUVs();
    }

    // TiC2 repair hints: pickaxe (tool), then ingot/lapis/gem/dust/quartz for top/left/right/bottom-left/bottom-right
    protected void setRepairIconUVs() {
        iconX = new int[] { 0, 3, 2, 4, 1, 5 };
        iconY = new int[] { 13, 13, 13, 13, 13, 13 };
    }

    @Override
    public void initGui() {
        super.initGui();
        this.xSize = 176 + 110;
        this.guiLeft = (this.width - 176) / 2 - 110;

        if (this.text == null) {
            this.text = new GuiTextField(this.fontRendererObj, 70 + 110, 8, 102, 12);
            this.text.setMaxStringLength(40);
            this.text.setEnableBackgroundDrawing(false);
            this.text.setVisible(true);
            this.text.setCanLoseFocus(true);
            this.text.setFocused(false);
            this.text.setTextColor(0xffffff);
        }

        this.buttonList.clear();
        createToolButtons();
    }

    protected void createToolButtons() {
        for (int iter = 0; iter < TConstructClientRegistry.toolButtons.size(); iter++) {
            ToolGuiElement element = TConstructClientRegistry.toolButtons.get(iter);
            GuiButtonTool button = new GuiButtonTool(
                    iter,
                    this.guiLeft + 22 * (iter % 5),
                    this.guiTop + 9 + 22 * (iter / 5),
                    element.buttonIconX,
                    element.buttonIconY,
                    element.domain,
                    element.texture,
                    element);
            this.buttonList.add(button);
        }
        this.buttonList.get(0).enabled = false;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        this.buttonList.get(selectedButton).enabled = true;
        selectedButton = button.id;
        button.enabled = false;

        ToolGuiElement element = ((GuiButtonTool) button).element;
        setSlotType(element.slotType);
        iconX = element.iconsX;
        iconY = element.iconsY;
        // tool buttons carry per-part icon sets; the Repair & Modify pentagon uses its own
        if (element.slotType == 0) {
            setRepairIconUVs();
        }
        title = "§n" + StatCollector.translateToLocal(element.title);
        body = StatCollector.translateToLocal(element.body).replace("\\n", "\n");
    }

    protected void setSlotType(int type) {
        switch (type) {
            case 0:
                // TiC2-style Repair & Modify: tool centered, five modifier slots arranged around it
                slotX = new int[] { 33, 33, 11, 55, 15, 51 };
                slotY = new int[] { 40, 17, 35, 35, 61, 61 };
                break;
            case 1:
                slotX = new int[] { 56, 56, 56, PARK_X1, PARK_X2, PARK_X3 }; // Three parts
                slotY = new int[] { 19, 55, 37, PARK_Y, PARK_Y, PARK_Y };
                break;
            case 2:
                slotX = new int[] { 56, 56, 14, PARK_X1, PARK_X2, PARK_X3 }; // Two parts
                slotY = new int[] { 28, 46, 37, PARK_Y, PARK_Y, PARK_Y };
                break;
            case 3:
                slotX = new int[] { 38, 47, 56, PARK_X1, PARK_X2, PARK_X3 }; // Double head
                slotY = new int[] { 28, 46, 28, PARK_Y, PARK_Y, PARK_Y };
                break;
            case 7:
                slotX = new int[] { 56, 56, 56, PARK_X1, PARK_X2, PARK_X3 }; // Three parts reverse
                slotY = new int[] { 19, 37, 55, PARK_Y, PARK_Y, PARK_Y };
                break;
        }
        toolSlots.resetSlots(slotX, slotY);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        this.text.updateCursorCounter();
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2) {
        this.fontRendererObj.drawString(StatCollector.translateToLocal(logic.getInvName()), 116, 8, 0x000000);
        drawInventoryLabel();
        this.text.drawTextBox();

        if (logic.isStackInSlot(0)) {
            // stats in the upper panel, modifiers in the lower one
            ToolStationGuiHelper.drawToolStatsSplit(logic.getStackInSlot(0), 294, 11, 92, 294, 109, 184);
        } else {
            drawToolInformation();
        }
    }

    protected void drawToolInformation() {
        this.drawCenteredString(fontRendererObj, title, 349, 20, 0xffffff);
        fontRendererObj.drawSplitString(body, 294, 36, 115, 0xffffff);
    }

    protected void drawInventoryLabel() {
        // the Repair & Modify pentagon reaches into the label's space
        if (selectedButton == 0) return;
        this.fontRendererObj
                .drawString(StatCollector.translateToLocal("container.inventory"), 118, this.ySize - 96 + 2, 0x000000);
    }

    private static final ResourceLocation background = new ResourceLocation("tinker", "textures/gui/toolstation.png");
    private static final ResourceLocation icons = new ResourceLocation("tinker", "textures/gui/icons.png");
    private static final ResourceLocation panelTexture = new ResourceLocation("tinker", "textures/gui/panel.png");
    private static final ResourceLocation beamsTexture = new ResourceLocation("tinker", "textures/gui/beams.png");

    // panel.png skins (TiC2 GuiInfoPanel): plain at (0,0), wood at (126,0), metal at (126,83)
    protected int panelSkinU() {
        return 126;
    }

    protected int panelSkinV() {
        return 0; // wood
    }

    // beams.png rows: wood at v0, metal at v7
    protected int beamRowV() {
        return 0;
    }

    /**
     * Draw the background layer for the GuiContainer (everything behind the items)
     */
    @Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
        // Draw the background
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(background);
        final int cornerX = this.guiLeft + 110;
        this.drawTexturedModalRect(cornerX, this.guiTop, 0, 0, 176, this.ySize);

        if (this.text.isFocused()) {
            this.drawTexturedModalRect(cornerX + 62, this.guiTop, 0, this.ySize, 112, 22);
        }

        drawCentralPanelExtras(cornerX);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(icons);
        // Draw the slots

        for (int i = 0; i < slotX.length; i++) {
            this.drawTexturedModalRect(cornerX + slotX[i], this.guiTop + slotY[i], 144, 216, 18, 18);
            if (i < iconX.length && i < iconY.length && !logic.isStackInSlot(i + 1)) {
                this.drawTexturedModalRect(
                        cornerX + slotX[i],
                        this.guiTop + slotY[i],
                        18 * iconX[i],
                        18 * iconY[i],
                        18,
                        18);
            }
        }

        // Draw the two TiC2-style info panels (tool stats on top, modifiers below), pushed down
        // below the beam like TiC2's modules (beam 7 + panel decoration 4)
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(panelTexture);
        drawInfoPanel(cornerX + 176, this.guiTop + 11, 126, 94);
        drawInfoPanel(cornerX + 176, this.guiTop + 109, 126, 87);

        // beam trim at the GUI's top edge, level with the central panel and overhanging each side
        // module by one cap (TiC2: x = module.guiLeft - beamL.w, y = cornerY)
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(beamsTexture);
        drawBeam(this.guiLeft - 2, this.guiTop, 114);
        drawBeam(cornerX + 174, this.guiTop, 130);
    }

    /** Draws one 9-slice panel from panel.png using the theme skin (4px corners, 118x75 scalable body). */
    protected void drawInfoPanel(int x, int y, int w, int h) {
        int u = panelSkinU(), v = panelSkinV();
        // corners
        this.drawTexturedModalRect(x, y, u, v, 4, 4);
        this.drawTexturedModalRect(x + w - 4, y, u + 122, v, 4, 4);
        this.drawTexturedModalRect(x, y + h - 4, u, v + 79, 4, 4);
        this.drawTexturedModalRect(x + w - 4, y + h - 4, u + 122, v + 79, 4, 4);
        // edges and background, stretched
        drawStretched(x + 4, y, w - 8, 4, u + 4, v, 118, 4, 256, 256);
        drawStretched(x + 4, y + h - 4, w - 8, 4, u + 4, v + 79, 118, 4, 256, 256);
        drawStretched(x, y + 4, 4, h - 8, u, v + 4, 4, 75, 256, 256);
        drawStretched(x + w - 4, y + 4, 4, h - 8, u + 122, v + 4, 4, 75, 256, 256);
        drawStretched(x + 4, y + 4, w - 8, h - 8, u + 4, v + 4, 118, 75, 256, 256);
    }

    /** Draws one beam strip from beams.png (2px caps, scalable center) using the theme row. */
    protected void drawBeam(int x, int y, int w) {
        int v = beamRowV();
        this.drawStretched(x, y, 2, 7, 0, v, 2, 7, 133, 14);
        this.drawStretched(x + 2, y, w - 4, 7, 2, v, 129, 7, 133, 14);
        this.drawStretched(x + w - 2, y, 2, 7, 131, v, 2, 7, 133, 14);
    }

    /** drawTexturedModalRect assumes a 256x256 sheet; this maps an arbitrary texture region to any size. */
    protected void drawStretched(int x, int y, int w, int h, int u, int v, int uw, int vh, int texW, int texH) {
        float us = 1.0F / texW, vs = 1.0F / texH;
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();
        tess.addVertexWithUV(x, y + h, this.zLevel, u * us, (v + vh) * vs);
        tess.addVertexWithUV(x + w, y + h, this.zLevel, (u + uw) * us, (v + vh) * vs);
        tess.addVertexWithUV(x + w, y, this.zLevel, (u + uw) * us, v * vs);
        tess.addVertexWithUV(x, y, this.zLevel, u * us, v * vs);
        tess.draw();
    }

    /** Drawn after the main panel background, before the slot frames. Textures may be rebound freely. */
    protected void drawCentralPanelExtras(int cornerX) {
        if (selectedButton != 0) return;

        // oversized ghost preview behind the slots, TiC2-style: the tool being modified, or an anvil when empty
        GL11.glPushMatrix();
        GL11.glTranslatef(cornerX + 10, this.guiTop + 20, 0F);
        GL11.glScalef(3.7F, 3.7F, 1F);
        if (logic.isStackInSlot(1)) {
            ItemStack tool = logic.getStackInSlot(1);
            RenderHelper.enableGUIStandardItemLighting();
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            ghostRender.renderItemAndEffectIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), tool, 0, 0);
        } else {
            // darken the pale anvil sprite so it reads as a silhouette through the cover
            GL11.glColor4f(0.45F, 0.45F, 0.45F, 1.0F);
            this.mc.getTextureManager().bindTexture(icons);
            this.drawTexturedModalRect(0, 0, 54, 0, 18, 18);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        }
        GL11.glPopMatrix();

        // item rendering flips lighting/depth/alpha state; restore what the background pass expects
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        // translucent cover so the preview stays in the background
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.82F);
        this.mc.getTextureManager().bindTexture(background);
        this.drawTexturedModalRect(cornerX + 8, this.guiTop + 16, 8, 16, 80, 64);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_BLEND);
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

    @Override
    public boolean hideItemPanelSlot(GuiContainer gui, int x, int y, int w, int h) {
        if (y + h - 4 < guiTop || y + 4 > guiTop + ySize) return false;
        return x - w - 4 >= guiLeft - 40 && x + 4 <= guiLeft + xSize + 126;
    }
}
