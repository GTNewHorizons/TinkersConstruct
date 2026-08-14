package tconstruct.tools.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import tconstruct.library.client.TConstructClientRegistry;
import tconstruct.library.client.ToolGuiElement;
import tconstruct.tools.logic.ToolForgeLogic;

@SideOnly(Side.CLIENT)
public class ToolForgeGui extends ToolStationGui {

    private static final ResourceLocation forgeBackground = new ResourceLocation(
            "tinker",
            "textures/gui/toolstation.png");
    private static final ResourceLocation forgeIcons = new ResourceLocation("tinker", "textures/gui/icons.png");
    private static final RenderItem ghostRender = new RenderItem();

    // Where slots 5 and 6 sit while a tool-building layout only uses four inputs
    private static final int PARK_X1 = 87, PARK_X2 = 107, PARK_Y = 62;

    public ToolForgeGui(InventoryPlayer inventoryplayer, ToolForgeLogic stationlogic, World world, int x, int y,
            int z) {
        super(inventoryplayer, stationlogic, world, x, y, z);
    }

    @Override
    protected void createToolButtons() {
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
        int offset = TConstructClientRegistry.tierTwoButtons.size();

        for (int iter = 0; iter < TConstructClientRegistry.tierTwoButtons.size(); iter++) {
            ToolGuiElement element = TConstructClientRegistry.tierTwoButtons.get(iter);
            GuiButtonTool button = new GuiButtonTool(
                    iter + 1,
                    this.guiLeft + 22 * ((iter + 1) % 5),
                    this.guiTop + 22 * ((iter + 1) / 5),
                    element.buttonIconX,
                    element.buttonIconY,
                    element.domain,
                    element.texture,
                    element);
            this.buttonList.add(button);
        }

        for (int iter = 1; iter < TConstructClientRegistry.toolButtons.size(); iter++) {
            ToolGuiElement element = TConstructClientRegistry.toolButtons.get(iter);
            GuiButtonTool button = new GuiButtonTool(
                    iter + offset,
                    this.guiLeft + 22 * ((iter + offset) % 5),
                    this.guiTop + 22 * ((iter + offset) / 5),
                    element.buttonIconX,
                    element.buttonIconY,
                    element.domain,
                    element.texture,
                    element);
            this.buttonList.add(button);
        }
    }

    @Override
    protected void setIconUVs() {
        setRepairIconUVs();
    }

    // TiC2 repair hints: pickaxe (tool), then ingot/lapis/gem/dust/quartz for top/left/right/bottom-left/bottom-right
    private void setRepairIconUVs() {
        iconX = new int[] { 0, 3, 2, 4, 1, 5 };
        iconY = new int[] { 13, 13, 13, 13, 13, 13 };
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        super.actionPerformed(button);
        // tool buttons carry four-slot icon sets; the Repair & Modify pentagon uses its own
        if (((GuiButtonTool) button).element.slotType == 0) {
            setRepairIconUVs();
        }
    }

    @Override
    protected void setSlotType(int type) {
        switch (type) {
            case 0:
                // TiC2-style Repair & Modify: tool centered, five modifier slots arranged around it
                slotX = new int[] { 33, 33, 11, 55, 15, 51 };
                slotY = new int[] { 40, 17, 35, 35, 61, 61 };
                break;
            case 1:
                slotX = new int[] { 56, 56, 56, 14, PARK_X1, PARK_X2 }; // Three parts
                slotY = new int[] { 19, 55, 37, 37, PARK_Y, PARK_Y };
                break;
            case 2:
                slotX = new int[] { 56, 56, 14, 14, PARK_X1, PARK_X2 }; // Two parts
                slotY = new int[] { 28, 46, 28, 46, PARK_Y, PARK_Y };
                break;
            case 3:
                slotX = new int[] { 38, 47, 56, 14, PARK_X1, PARK_X2 }; // Double head
                slotY = new int[] { 28, 46, 28, 37, PARK_Y, PARK_Y };
                break;
            case 4:
                slotX = new int[] { 47, 38, 56, 47, PARK_X1, PARK_X2 }; // Four parts
                slotY = new int[] { 19, 37, 37, 55, PARK_Y, PARK_Y };
                break;
            case 5:
                slotX = new int[] { 38, 47, 56, 47, PARK_X1, PARK_X2 }; // Four parts, double head
                slotY = new int[] { 19, 55, 19, 37, PARK_Y, PARK_Y };
                break;
            case 6:
                slotX = new int[] { 38, 38, 20, 56, PARK_X1, PARK_X2 }; // Double head
                slotY = new int[] { 28, 46, 28, 28, PARK_Y, PARK_Y };
                break;
            case 7:
                slotX = new int[] { 56, 56, 56, 14, PARK_X1, PARK_X2 }; // Three parts reverse
                slotY = new int[] { 19, 37, 55, 37, PARK_Y, PARK_Y };
                break;
            case 8:
                slotX = new int[] { 20, 38, 56, 38, PARK_X1, PARK_X2 }; // Double head middle
                slotY = new int[] { 28, 46, 28, 28, PARK_Y, PARK_Y };
                break;
            case 9:
                slotX = new int[] { 38, 56, 47, 47, PARK_X1, PARK_X2 }; // Four parts, crossbow.
                slotY = new int[] { 37, 37, 55, 19, PARK_Y, PARK_Y };
                break;
        }
        toolSlots.resetSlots(slotX, slotY);
    }

    @Override
    protected void drawInventoryLabel() {
        // the Repair & Modify pentagon reaches into the label's space
        if (selectedButton != 0) super.drawInventoryLabel();
    }

    @Override
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
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.mc.getTextureManager().bindTexture(forgeIcons);
            this.drawTexturedModalRect(0, 0, 54, 0, 18, 18);
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
        this.mc.getTextureManager().bindTexture(forgeBackground);
        this.drawTexturedModalRect(cornerX + 8, this.guiTop + 16, 8, 16, 80, 64);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_BLEND);
    }
}
