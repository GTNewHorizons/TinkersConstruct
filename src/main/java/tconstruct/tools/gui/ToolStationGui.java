package tconstruct.tools.gui;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

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

    public ToolStationGui(InventoryPlayer inventoryplayer, ToolStationLogic stationlogic, World world, int x, int y,
            int z) {
        super(stationlogic.getGuiContainer(inventoryplayer, world, x, y, z));
        this.logic = stationlogic;
        toolSlots = (ToolStationContainer) inventorySlots;
        selectedButton = 0;
        setSlotType(0);
        setIconUVs();
        title = "§n" + StatCollector.translateToLocal("gui.toolforge1");
        body = StatCollector.translateToLocal("gui.toolforge2");
        Keyboard.enableRepeatEvents(true);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.text.mouseClicked(mouseX - this.guiLeft, mouseY - this.guiTop, mouseButton);
    }

    protected void setIconUVs() {
        iconX = new int[] { 0, 1, 2 };
        iconY = new int[] { 13, 13, 13 };
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
        ToolGuiElement repair = TConstructClientRegistry.toolButtons.get(0);
        GuiButtonTool repairButton = new GuiButtonTool(
                0,
                this.guiLeft,
                this.guiTop,
                repair.buttonIconX,
                repair.buttonIconY,
                repair.domain,
                repair.texture,
                repair); // Repair
        repairButton.enabled = false;
        this.buttonList.add(repairButton);

        for (int iter = 1; iter < TConstructClientRegistry.toolButtons.size(); iter++) {
            ToolGuiElement element = TConstructClientRegistry.toolButtons.get(iter);
            GuiButtonTool button = new GuiButtonTool(
                    iter,
                    this.guiLeft + 22 * (iter % 5),
                    this.guiTop + 22 * (iter / 5),
                    element.buttonIconX,
                    element.buttonIconY,
                    element.domain,
                    element.texture,
                    element);
            this.buttonList.add(button);
        }
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
        title = "§n" + StatCollector.translateToLocal(element.title);
        body = StatCollector.translateToLocal(element.body).replaceAll("\\\\n", "\n");
    }

    protected void setSlotType(int type) {
        switch (type) {
            case 0:
                slotX = new int[] { 56, 38, 38 }; // Repair
                slotY = new int[] { 37, 28, 46 };
                break;
            case 1:
                slotX = new int[] { 56, 56, 56 }; // Three parts
                slotY = new int[] { 19, 55, 37 };
                break;
            case 2:
                slotX = new int[] { 56, 56, 14 }; // Two parts
                slotY = new int[] { 28, 46, 37 };
                break;
            case 3:
                slotX = new int[] { 38, 47, 56 }; // Double head
                slotY = new int[] { 28, 46, 28 };
                break;
            case 7:
                slotX = new int[] { 56, 56, 56 }; // Three parts reverse
                slotY = new int[] { 19, 37, 55 };
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
        this.fontRendererObj
                .drawString(StatCollector.translateToLocal("container.inventory"), 118, this.ySize - 96 + 2, 0x000000);
        this.text.drawTextBox();

        if (logic.isStackInSlot(0)) {
            ToolStationGuiHelper.drawToolStats(logic.getStackInSlot(0), 294, 0);
        } else {
            drawToolInformation();
        }
    }

    protected void drawToolInformation() {
        this.drawCenteredString(fontRendererObj, title, 349, 8, 0xffffff);
        fontRendererObj.drawSplitString(body, 294, 24, 115, 0xffffff);
    }

    private static final ResourceLocation background = new ResourceLocation("tinker", "textures/gui/toolstation.png");
    private static final ResourceLocation icons = new ResourceLocation("tinker", "textures/gui/icons.png");
    private static final ResourceLocation description = new ResourceLocation("tinker", "textures/gui/description.png");

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

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(icons);
        // Draw the slots

        for (int i = 0; i < slotX.length; i++) {
            this.drawTexturedModalRect(cornerX + slotX[i], this.guiTop + slotY[i], 144, 216, 18, 18);
            if (!logic.isStackInSlot(i + 1)) {
                this.drawTexturedModalRect(
                        cornerX + slotX[i],
                        this.guiTop + slotY[i],
                        18 * iconX[i],
                        18 * iconY[i],
                        18,
                        18);
            }
        }

        // Draw description
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(description);
        this.drawTexturedModalRect(cornerX + 176, this.guiTop, 0, 0, 126, this.ySize + 30);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == 1 || (!this.text.isFocused() && keyCode == this.mc.gameSettings.keyBindInventory.getKeyCode())) {
            logic.setToolname("");
            updateServer("");
            Keyboard.enableRepeatEvents(false);
            this.mc.thePlayer.closeScreen();
        } else if (text.textboxKeyTyped(typedChar, keyCode)) {
            final var toolName = text.getText().trim();
            logic.setToolname(toolName);
            updateServer(toolName);
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
