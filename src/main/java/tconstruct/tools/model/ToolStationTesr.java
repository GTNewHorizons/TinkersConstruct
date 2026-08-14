package tconstruct.tools.model;

import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import tconstruct.library.tools.ToolCore;
import tconstruct.tools.entity.FancyEntityItem;
import tconstruct.tools.logic.ToolStationLogic;

/* Renders the station's contents lying on the table, mirroring the Repair & Modify pentagon */

@SideOnly(Side.CLIENT)
public class ToolStationTesr extends TileEntitySpecialRenderer {

    // GUI pentagon slot positions (see ToolStationGui.setSlotType case 0), tool first
    private static final int[] SLOT_X = { 33, 33, 11, 55, 15, 51 };
    private static final int[] SLOT_Y = { 40, 17, 35, 35, 61, 61 };

    @Override
    public void renderTileEntityAt(TileEntity logic, double posX, double posY, double posZ, float partialTicks) {
        render((ToolStationLogic) logic, posX, posY, posZ);
    }

    private void render(ToolStationLogic logic, double posX, double posY, double posZ) {
        for (int slot = 1; slot <= 6 && slot < logic.getSizeInventory(); slot++) {
            ItemStack stack = logic.getStackInSlot(slot);
            if (stack == null) continue;

            // map the GUI layout onto the table top: GUI x -> world x, GUI y -> world z
            float offX = (SLOT_X[slot - 1] - 33) / 61F * 0.75F;
            float offZ = (SLOT_Y[slot - 1] - 40) / 61F * 0.75F;

            // Tools render through FlexibleToolRenderer's ENTITY path (full-unit quad shifted -0.5/-0.25);
            // everything else through FancyItemRender's frame path (0.5128 quad, -0.05 nudge). Both sit on a
            // +0.1 bob baseline. That puts the sprite center at +0.35*s (tools) / +0.2026*s (items) along
            // the local up axis, which our 90-degree lay-flat maps to world z — cancel it so sprites center
            // on their pentagon spots.
            boolean isTool = stack.getItem() instanceof ToolCore;
            float scale = isTool && slot == 1 ? 1.05F : isTool ? 0.7F : 0.55F;
            float centerCorrection = (isTool ? 0.35F : 0.2026F) * scale;

            FancyEntityItem entityitem = new FancyEntityItem(logic.getWorldObj(), 0.0D, 0.0D, 0.0D, stack);
            entityitem.getEntityItem().stackSize = 1;
            entityitem.hoverStart = 0.0F;

            GL11.glPushMatrix();
            // stagger heights so overlapping flat sprites don't z-fight
            GL11.glTranslatef(
                    (float) posX + 0.5F + offX,
                    (float) posY + 1.005F + slot * 0.004F,
                    (float) posZ + 0.5F + offZ - centerCorrection);
            GL11.glRotatef(90F, 1F, 0F, 0F);
            GL11.glScalef(scale, scale, scale);
            RenderItem.renderInFrame = true;
            RenderManager.instance.renderEntityWithPosYaw(entityitem, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F);
            RenderItem.renderInFrame = false;
            GL11.glPopMatrix();
        }
    }
}
