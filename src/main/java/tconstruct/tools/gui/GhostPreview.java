package tconstruct.tools.gui;

import static tconstruct.tools.gui.CraftCells.ARROW_H;
import static tconstruct.tools.gui.CraftCells.ARROW_W;
import static tconstruct.tools.gui.CraftCells.ARROW_Y;
import static tconstruct.tools.gui.CraftCells.BUILD_GHOST_Y;
import static tconstruct.tools.gui.CraftCells.GHOST_SIZE;
import static tconstruct.tools.gui.CraftCells.GHOST_X;
import static tconstruct.tools.gui.CraftCells.WELL_SIZE;
import static tconstruct.tools.gui.CraftCells.WELL_X;
import static tconstruct.tools.gui.CraftCells.WELL_Y;
import static tconstruct.tools.gui.StationDraw.background;
import static tconstruct.tools.gui.StationDraw.icons;
import static tconstruct.tools.gui.StationSlotLayouts.GHOST_SCALE;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import tconstruct.client.FlexibleToolRenderer;
import tconstruct.library.tools.ToolCore;

/** The tool picture under the craft cells, and the arrow and output well beside them. */
@SideOnly(Side.CLIENT)
final class GhostPreview {

    private GhostPreview() {}

    private static final RenderItem ghostRender = new RenderItem();
    private static final FlexibleToolRenderer ghostToolRender = new FlexibleToolRenderer();
    /** The cover reaches 4 px above the picture and 1 px under it. */
    private static final int COVER_H = 4 + (int) Math.ceil(GHOST_SIZE) + 1;
    /** The arrow and the well are cut out of the panel sheet, so they can be drawn at any row. */
    private static final int ARROW_U = 0, ARROW_V = 222, WELL_U = 24, WELL_V = 222;

    static void drawGhostPreview(ToolStationGui screen, FontRenderer fontRendererObj, int cornerX) {
        int ghostY = screen.guiTop + BUILD_GHOST_Y;
        ItemStack tool = screen.logic.getStackInSlot(1);
        GL11.glPushMatrix();
        GL11.glTranslatef(cornerX + GHOST_X, ghostY, 0F);
        GL11.glScalef(GHOST_SCALE, GHOST_SCALE, 1F);
        if (screen.selectedButton != 0) {
            // the sprite is the 16 px inside the button's 18 px cell
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
            screen.mc.getTextureManager().bindTexture(screen.tabSheet);
            screen.drawTexturedModalRect(
                    0,
                    0,
                    screen.tabElement.buttonIconX * 18 + 1,
                    screen.tabElement.buttonIconY * 18 + 1,
                    16,
                    16);
        } else if (tool != null) {
            RenderHelper.enableGUIStandardItemLighting();
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            if (tool.getItem() instanceof ToolCore) {
                // the registered item renderer also draws the ammo count, which the scale matrix would blow up
                screen.mc.getTextureManager().bindTexture(TextureMap.locationItemsTexture);
                ghostToolRender.renderItem(IItemRenderer.ItemRenderType.INVENTORY, tool);
            } else {
                ghostRender.renderItemAndEffectIntoGUI(fontRendererObj, screen.mc.getTextureManager(), tool, 0, 0);
            }
        } else {
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            screen.mc.getTextureManager().bindTexture(icons);
            screen.drawTexturedModalRect(0, 0, 54, 0, 18, 18);
        }
        GL11.glPopMatrix();

        // item rendering flips lighting/depth/alpha state; restore what the background pass expects
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
        GL11.glDisable(GL11.GL_BLEND);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        // second blit of the panel region over the preview, so the foreground stays readable
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.82F);
        screen.mc.getTextureManager().bindTexture(background);
        // source and destination move together, so the cover is always a slice of blank panel
        screen.drawTexturedModalRect(cornerX + 8, ghostY - 4, 8, ghostY - 4 - screen.guiTop, 80, COVER_H);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_BLEND);
    }

    static void drawOutputColumn(ToolStationGui screen, int cornerX) {
        screen.drawTexturedModalRect(
                cornerX + screen.cells.arrowX,
                screen.guiTop + ARROW_Y + screen.cells.outputOffsetY,
                ARROW_U,
                ARROW_V,
                ARROW_W,
                ARROW_H);
        screen.drawTexturedModalRect(
                cornerX + WELL_X,
                screen.guiTop + WELL_Y + screen.cells.outputOffsetY,
                WELL_U,
                WELL_V,
                WELL_SIZE,
                WELL_SIZE);
    }
}
