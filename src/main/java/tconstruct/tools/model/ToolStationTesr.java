package tconstruct.tools.model;

import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.event.TextureStitchEvent;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mantle.blocks.abstracts.InventorySlab;
import tconstruct.library.tools.ToolCore;
import tconstruct.tools.logic.ToolStationLogic;

/** Draws the station's contents on the tabletop, after TiC2's tables, turned to face whoever last put something in. */
@SideOnly(Side.CLIENT)
public class ToolStationTesr extends TileEntitySpecialRenderer {

    @Override
    public void renderTileEntityAt(TileEntity logic, double posX, double posY, double posZ, float partialTicks) {
        render((ToolStationLogic) logic, posX, posY, posZ);
    }

    /** A restitch makes new icons, and a resource pack may change their textures. */
    @SubscribeEvent
    public void onStitch(TextureStitchEvent.Post event) {
        TexelMasks.clear();
    }

    private void render(ToolStationLogic logic, double posX, double posY, double posZ) {
        // a slab in the lower half of its block (InventorySlab: metadata / 8) tops out at 0.5
        float tableTop = logic.getBlockType() instanceof InventorySlab && logic.getBlockMetadata() / 8 != 1 ? 0.5F
                : 1.0F;
        GL11.glPushMatrix();
        // an earlier renderer may leave either off; the lights go in before the turn, to stay fixed to the world
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        RenderHelper.enableStandardItemLighting();
        // facing 0 reads upright from the south; each further Direction index is a quarter turn clockwise from above
        GL11.glTranslatef((float) posX + 0.5F, (float) posY, (float) posZ + 0.5F);
        GL11.glRotatef(-90F * logic.table.getFacing(), 0F, 1F, 0F);
        ToolCore tool = logic.getSelectedTool();
        if (tool != null) {
            BuildPreview.renderBuild(logic, tool, tableTop);
            GL11.glPopMatrix();
            return;
        }
        TableItems.renderPentagon(logic, tableTop);
        GL11.glPopMatrix();
    }
}
