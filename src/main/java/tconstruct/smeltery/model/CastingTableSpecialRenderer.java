package tconstruct.smeltery.model;

import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import tconstruct.library.ItemBlocklike;
import tconstruct.smeltery.TinkerSmeltery;
import tconstruct.smeltery.logic.CastingTableLogic;
import tconstruct.tools.entity.FancyEntityItem;

/* Special renderer, only used for drawing tools */

@SideOnly(Side.CLIENT)
public class CastingTableSpecialRenderer extends TileEntitySpecialRenderer {

    @Override
    public void renderTileEntityAt(TileEntity logic, double var2, double var4, double var6, float var8) {
        // TConstruct.logger.info("Render!!!");
        this.render((CastingTableLogic) logic, var2, var4, var6, var8);
    }

    public void render(CastingTableLogic logic, double posX, double posY, double posZ, float var8) {
        GL11.glPushMatrix();
        float var10 = (float) (posX - 0.5F);
        float var11 = (float) (posY - 0.5F);
        float var12 = (float) (posZ - 0.5F);
        GL11.glTranslatef(var10, var11, var12);
        this.func_82402_b(logic);
        GL11.glPopMatrix();
    }

    private void func_82402_b(CastingTableLogic logic) {
        ItemStack stack = logic.getStackInSlot(0);

        if (stack != null) renderItem(logic, stack);

        stack = logic.getStackInSlot(1);

        if (stack != null) renderItem(logic, stack);
    }

    void renderItem(CastingTableLogic logic, ItemStack stack) {
        FancyEntityItem entityitem = new FancyEntityItem(logic.getWorldObj(), 0.0D, 0.0D, 0.0D, stack);
        entityitem.getEntityItem().stackSize = 1;
        entityitem.hoverStart = 0.0F;
        GL11.glPushMatrix();
        GL11.glTranslatef(1F, 1.48F, 0.55F);

        float rotationY = switch (logic.getRenderDirection()) {
            case 3 -> 180F;
            case 4 -> 90F;
            case 5 -> 270F;
            default -> 0F;
        };
        GL11.glRotatef(rotationY, 0F, 1F, 0F);

        if (logic.getRenderDirection() == 3) {
            GL11.glTranslatef(0F, 0F, -0.9F);
        } else if (logic.getRenderDirection() == 4) {
            GL11.glTranslatef(-0.45F, 0F, -0.45F);
        } else if (logic.getRenderDirection() == 5) {
            GL11.glTranslatef(0.45F, 0F, -0.45F);
        }

        GL11.glRotatef(90F, 1F, 0F, 0F);
        GL11.glScalef(2F, 2F, 2F);

        if (stack.getItem() instanceof ItemBlock || stack.getItem() instanceof ItemBlocklike) {
            GL11.glRotatef(-90F, 0F, 0F, 1F);
            GL11.glRotatef(90F, -1F, 0F, 0F);
            GL11.glTranslatef(-0.2275F, -0.1F, 0F);
        }

        if (stack.isItemEqual(new ItemStack(TinkerSmeltery.glassPane))) {
            GL11.glRotatef(90F, 1F, 0F, 0F);
            GL11.glTranslatef(0F, -0.194F, -0.1F);
            GL11.glScalef(0.85F, 0.85F, 0.85F);
        }

        RenderItem.renderInFrame = true;
        RenderManager.instance.renderEntityWithPosYaw(entityitem, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F);
        RenderItem.renderInFrame = false;

        GL11.glPopMatrix();
    }
}
