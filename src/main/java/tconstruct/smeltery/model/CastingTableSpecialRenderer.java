package tconstruct.smeltery.model;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

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
        if (stack != null) {
            renderItem(logic, stack);
        } else {
            int castingDelay = logic.getCastingDelay();
            int maxDelay = logic.getMaxCastingDelay();
            if (castingDelay > 0 && maxDelay > 0 && logic.getRenderOffset() == 0) {
                ItemStack output = logic.getRecipeOutput();
                if (output != null) {
                    // alpha fades in linearly; temperature tint fades from hot orange (0xFF8020) to
                    // white over the last 25% of cooling so the item reads as molten metal.
                    int timer = maxDelay - castingDelay;
                    int opacity4 = (4 * 255) * timer / maxDelay;
                    float itemAlpha = (opacity4 / 4) / 255f;
                    int temperature = opacity4 > 3 * 255 ? (4 * 255 - opacity4) : 255;
                    float r = 1f - temperature * (1f - 0xFF / 255f) / 255f;
                    float g = 1f - temperature * (1f - 0x80 / 255f) / 255f;
                    float b = 1f - temperature * (1f - 0x20 / 255f) / 255f;
                    renderItemWithFade(logic, output, r, g, b, itemAlpha);
                }
            }
        }
    }

    void renderItem(CastingTableLogic logic, ItemStack stack) {
        renderItemInternal(logic, stack, false, 1f, 1f, 1f, 1f);
    }

    // Renders the cooling preview using the exact same entity render path as the finished item,
    // so position and size always match. Fade and warm tint are applied uniformly via a
    // GL_CONSTANT_COLOR blend (the 1.7.10 equivalent of 1.20.1's CastingItemRenderTypeBuffer).
    void renderItemWithFade(CastingTableLogic logic, ItemStack stack, float r, float g, float b, float alpha) {
        renderItemInternal(logic, stack, true, r, g, b, alpha);
    }

    private void renderItemInternal(CastingTableLogic logic, ItemStack stack, boolean fade, float r, float g, float b,
            float alpha) {
        FancyEntityItem entityitem = new FancyEntityItem(logic.getWorldObj(), 0.0D, 0.0D, 0.0D, stack);
        entityitem.getEntityItem().stackSize = 1;
        entityitem.hoverStart = 0.0F;
        float prevBrightnessX = OpenGlHelper.lastBrightnessX;
        float prevBrightnessY = OpenGlHelper.lastBrightnessY;
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

        if (fade) {
            // Uniform tint + fade: src is multiplied by (r,g,b,alpha) as a constant color and
            // blended over the background by alpha. Depth test stays ENABLED so the model sorts
            // its own faces (no blocky self-overlap) and walls/liquid occlude it correctly.
            GL11.glEnable(GL11.GL_BLEND);
            GL14.glBlendColor(r * alpha, g * alpha, b * alpha, alpha);
            GL11.glBlendFunc(0x8001, 0x8004);
            // full-bright so the hot tint glows instead of being dimmed by world light
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
        }

        RenderItem.renderInFrame = true;
        RenderManager.instance.renderEntityWithPosYaw(entityitem, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F);
        RenderItem.renderInFrame = false;

        if (fade) {
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, prevBrightnessX, prevBrightnessY);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDisable(GL11.GL_BLEND);
        }

        GL11.glPopMatrix();
    }
}
