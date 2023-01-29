package tconstruct.armor.model;

import java.util.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import tconstruct.armor.items.ArmorPattern;

public class RenderArmorCast implements IItemRenderer {

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return type != ItemRenderType.FIRST_PERSON_MAP;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        switch (helper) {
            case BLOCK_3D:
            case ENTITY_BOBBING:
            case ENTITY_ROTATION:
            case EQUIPPED_BLOCK:
            case INVENTORY_BLOCK:
                return true;
            default:
                return false;
        }
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        RenderBlocks renderer = Minecraft.getMinecraft().renderGlobal.renderBlocksRg;
        Tessellator tessellator = Tessellator.instance;
        IIcon baseIcon = item.getItem() instanceof ArmorPattern ? ((ArmorPattern) item.getItem()).getBaseIcon()
                : item.getIconIndex();
        GL11.glPushMatrix();
        switch (type) {
            case ENTITY:
                GL11.glScalef(0.8F, 0.33F, 0.8F);
                GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
                break;
            case INVENTORY:
                GL11.glScalef(1F, 0.6F, 1F);
                GL11.glTranslatef(0F, -0.1F, 0F);
                break;
            case EQUIPPED:
            case EQUIPPED_FIRST_PERSON:
                GL11.glScalef(1F, 0.6F, 1F);
                break;
            default:
                break;
        }
        renderer.setRenderBounds(0.1F, 0.1F, 0.1F, 0.9F, 0.83F, 0.9F);
        renderCube(tessellator, renderer, baseIcon);
        renderer.setRenderBounds(0.1F, 0.1F, 0.1F, 0.9F, 1.0F, 0.9F);
        ArrayList<ForgeDirection> sides = new ArrayList<>();
        sides.add(ForgeDirection.NORTH);
        sides.add(ForgeDirection.SOUTH);
        sides.add(ForgeDirection.EAST);
        sides.add(ForgeDirection.WEST);
        renderSelectedCube(tessellator, renderer, baseIcon, sides);
        renderCastTop(item, type);

        GL11.glPopMatrix();
    }

    public void renderCube(Tessellator tessellator, RenderBlocks renderer, IIcon icon) {
        tessellator.startDrawingQuads();
        tessellator.setNormal(-1F, 0F, 0F);
        renderer.renderFaceXNeg(null, 0F, 0F, 0F, icon);
        tessellator.setNormal(1F, 0F, 0F);
        renderer.renderFaceXPos(null, 0F, 0F, 0F, icon);
        tessellator.setNormal(0F, -1F, 0F);
        renderer.renderFaceYNeg(null, 0F, 0F, 0F, icon);
        tessellator.setNormal(0F, 1F, 0F);
        renderer.renderFaceYPos(null, 0F, 0F, 0F, icon);
        tessellator.setNormal(0F, 0F, -1F);
        renderer.renderFaceZNeg(null, 0F, 0F, 0F, icon);
        tessellator.setNormal(0F, 0F, 1F);
        renderer.renderFaceZPos(null, 0F, 0F, 0F, icon);
        tessellator.draw();
    }

    public void renderSelectedCube(Tessellator tessellator, RenderBlocks renderer, IIcon icon,
            List<ForgeDirection> sidesToRender) {
        tessellator.startDrawingQuads();
        tessellator.setNormal(-1F, 0F, 0F);
        if (sidesToRender.contains(ForgeDirection.WEST)) renderer.renderFaceXNeg(null, 0F, 0F, 0F, icon);
        tessellator.setNormal(1F, 0F, 0F);
        if (sidesToRender.contains(ForgeDirection.EAST)) renderer.renderFaceXPos(null, 0F, 0F, 0F, icon);
        tessellator.setNormal(0F, -1F, 0F);
        if (sidesToRender.contains(ForgeDirection.DOWN)) renderer.renderFaceYNeg(null, 0F, 0F, 0F, icon);
        tessellator.setNormal(0F, 1F, 0F);
        if (sidesToRender.contains(ForgeDirection.UP)) renderer.renderFaceYPos(null, 0F, 0F, 0F, icon);
        tessellator.setNormal(0F, 0F, -1F);
        if (sidesToRender.contains(ForgeDirection.SOUTH)) renderer.renderFaceZNeg(null, 0F, 0F, 0F, icon);
        tessellator.setNormal(0F, 0F, 1F);
        if (sidesToRender.contains(ForgeDirection.NORTH)) renderer.renderFaceZPos(null, 0F, 0F, 0F, icon);
        tessellator.draw();
    }

    public void renderCastTop(ItemStack par1ItemStack, ItemRenderType type) {
        GL11.glPushMatrix();
        IIcon icon = par1ItemStack.getIconIndex();

        if (icon == null) {
            GL11.glPopMatrix();
            return;
        }
        TextureManager texturemanager = Minecraft.getMinecraft().renderEngine;
        texturemanager.bindTexture(texturemanager.getResourceLocation(par1ItemStack.getItemSpriteNumber()));
        Tessellator tessellator = Tessellator.instance;
        float f = icon.getMinU();
        float f1 = icon.getMaxU();
        float f2 = icon.getMinV();
        float f3 = icon.getMaxV();
        float f4 = 0.0F;
        float f5 = 0.3F;
        GL11.glTranslatef(-f4, -f5, 0.0F);
        float f6 = 1.5F;
        GL11.glRotatef(90F, 90F, 0F, 0F);
        GL11.glScalef(0.9F, 0.9F, 2F);
        GL11.glTranslatef(0.05F, 0.05F, -0.6F);
        renderItemIn2D(tessellator, f1, f2, f, f3, icon.getIconWidth(), icon.getIconHeight(), 0.0625F);
        GL11.glScalef(0.8F, 0.8F, 0.9F);
        GL11.glTranslatef(0.125F, 0.125F, 0.05F);
        renderItemIn2D(tessellator, f1, f2, f, f3, icon.getIconWidth(), icon.getIconHeight(), 0.0625F);
        GL11.glScalef(0.6F, 0.6F, 0.6F);
        GL11.glTranslatef(0.335F, 0.335F, 0.05F);
        renderItemIn2D(tessellator, f1, f2, f, f3, icon.getIconWidth(), icon.getIconHeight(), 0.0625F);

        GL11.glPopMatrix();
    }

    public static void renderItemIn2D(Tessellator par0Tessellator, float par1, float par2, float par3, float par4,
            float par5, float par6, float par7) {
        par0Tessellator.startDrawingQuads();
        par0Tessellator.setNormal(0.0F, 0.0F, 1.0F);
        par0Tessellator.addVertexWithUV(0.0D, 0.0D, 0.0D, par1, par4);
        par0Tessellator.addVertexWithUV(1.0D, 0.0D, 0.0D, par3, par4);
        par0Tessellator.addVertexWithUV(1.0D, 1.0D, 0.0D, par3, par2);
        par0Tessellator.addVertexWithUV(0.0D, 1.0D, 0.0D, par1, par2);
        par0Tessellator.draw();
        par0Tessellator.startDrawingQuads();
        par0Tessellator.setNormal(0.0F, 0.0F, -1.0F);
        par0Tessellator.addVertexWithUV(0.0D, 1.0D, 0.0F - par7, par1, par2);
        par0Tessellator.addVertexWithUV(1.0D, 1.0D, 0.0F - par7, par3, par2);
        par0Tessellator.addVertexWithUV(1.0D, 0.0D, 0.0F - par7, par3, par4);
        par0Tessellator.addVertexWithUV(0.0D, 0.0D, 0.0F - par7, par1, par4);
        par0Tessellator.draw();
        float f5 = 0.5F * (par1 - par3) / par5;
        float f6 = 0.5F * (par4 - par2) / par6;
        par0Tessellator.startDrawingQuads();
        par0Tessellator.setNormal(-1.0F, 0.0F, 0.0F);
        int k;
        float f7;
        float f8;

        for (k = 0; k < par5; ++k) {
            f7 = (float) k / par5;
            f8 = par1 + (par3 - par1) * f7 - f5;
            par0Tessellator.addVertexWithUV(f7, 0.0D, 0.0F - par7, f8, par4);
            par0Tessellator.addVertexWithUV(f7, 0.0D, 0.0D, f8, par4);
            par0Tessellator.addVertexWithUV(f7, 1.0D, 0.0D, f8, par2);
            par0Tessellator.addVertexWithUV(f7, 1.0D, 0.0F - par7, f8, par2);
        }

        par0Tessellator.draw();
        par0Tessellator.startDrawingQuads();
        par0Tessellator.setNormal(1.0F, 0.0F, 0.0F);
        float f9;

        for (k = 0; k < par5; ++k) {
            f7 = (float) k / par5;
            f8 = par1 + (par3 - par1) * f7 - f5;
            f9 = f7 + 1.0F / par5;
            par0Tessellator.addVertexWithUV(f9, 1.0D, 0.0F - par7, f8, par2);
            par0Tessellator.addVertexWithUV(f9, 1.0D, 0.0D, f8, par2);
            par0Tessellator.addVertexWithUV(f9, 0.0D, 0.0D, f8, par4);
            par0Tessellator.addVertexWithUV(f9, 0.0D, 0.0F - par7, f8, par4);
        }

        par0Tessellator.draw();
        par0Tessellator.startDrawingQuads();
        par0Tessellator.setNormal(0.0F, 1.0F, 0.0F);

        for (k = 0; k < par6; ++k) {
            f7 = (float) k / par6;
            f8 = par4 + (par2 - par4) * f7 - f6;
            f9 = f7 + 1.0F / par6;
            par0Tessellator.addVertexWithUV(0.0D, f9, 0.0D, par1, f8);
            par0Tessellator.addVertexWithUV(1.0D, f9, 0.0D, par3, f8);
            par0Tessellator.addVertexWithUV(1.0D, f9, 0.0F - par7, par3, f8);
            par0Tessellator.addVertexWithUV(0.0D, f9, 0.0F - par7, par1, f8);
        }

        par0Tessellator.draw();
        par0Tessellator.startDrawingQuads();
        par0Tessellator.setNormal(0.0F, -1.0F, 0.0F);

        for (k = 0; k < par6; ++k) {
            f7 = (float) k / par6;
            f8 = par4 + (par2 - par4) * f7 - f6;
            par0Tessellator.addVertexWithUV(1.0D, f7, 0.0D, par3, f8);
            par0Tessellator.addVertexWithUV(0.0D, f7, 0.0D, par1, f8);
            par0Tessellator.addVertexWithUV(0.0D, f7, 0.0F - par7, par1, f8);
            par0Tessellator.addVertexWithUV(1.0D, f7, 0.0F - par7, par3, f8);
        }

        par0Tessellator.draw();
    }
}
