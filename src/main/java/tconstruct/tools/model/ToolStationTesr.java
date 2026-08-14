package tconstruct.tools.model;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import tconstruct.library.tools.ToolCore;
import tconstruct.tools.logic.ToolStationLogic;

/*
 * Renders the station's contents lying on the table, mirroring the Repair & Modify pentagon. The layout follows TiC2's
 * block display: a modest tool in the center and small materials pushed out towards the table edges, so the sprites
 * simply don't meet — flat, even, and collision-free by composition.
 */

@SideOnly(Side.CLIENT)
public class ToolStationTesr extends TileEntitySpecialRenderer {

    // GUI pentagon slot positions (see ToolStationGui.setSlotType case 0), tool first
    private static final int[] SLOT_X = { 33, 33, 11, 55, 15, 51 };
    private static final int[] SLOT_Y = { 40, 17, 35, 35, 61, 61 };

    private static final float SPREAD = 0.9F;
    private static final float TOOL_SCALE = 0.55F;
    private static final float TOOL_THICKNESS = 0.09375F;
    private static final float MATERIAL_SCALE = 0.35F;
    // chosen so tool and material slabs come out at the same world height
    private static final float MATERIAL_THICKNESS = 0.15F;

    private final RenderBlocks renderBlocksInstance = new RenderBlocks();

    @Override
    public void renderTileEntityAt(TileEntity logic, double posX, double posY, double posZ, float partialTicks) {
        render((ToolStationLogic) logic, posX, posY, posZ);
    }

    private void render(ToolStationLogic logic, double posX, double posY, double posZ) {
        for (int slot = 1; slot <= 6 && slot < logic.getSizeInventory(); slot++) {
            ItemStack stack = logic.getStackInSlot(slot);
            if (stack == null) continue;

            // map the GUI layout onto the table top: GUI x -> world x, GUI y -> world z
            float offX = (SLOT_X[slot - 1] - 33) / 61F * SPREAD;
            float offZ = (SLOT_Y[slot - 1] - 40) / 61F * SPREAD;

            Block block = Block.getBlockFromItem(stack.getItem());
            if (stack.getItemSpriteNumber() == 0 && block != null
                    && RenderBlocks.renderItemIn3d(block.getRenderType())) {
                // block items sit on the table as small cubes rather than oversized flat stickers,
                // sunk a third into the wood so they don't tower over the flat sprites
                float cube = 0.2F;
                GL11.glPushMatrix();
                GL11.glTranslatef(
                        (float) posX + 0.5F + offX,
                        (float) posY + 1.0F + cube / 2F - cube / 3F,
                        (float) posZ + 0.5F + offZ);
                GL11.glScalef(cube, cube, cube);
                // the face renderers sample whatever texture is bound; give them the blocks atlas
                Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
                GL11.glColor4f(1F, 1F, 1F, 1F);
                renderBlocksInstance.renderBlockAsItem(block, stack.getItemDamage(), 1.0F);
                GL11.glPopMatrix();
                continue;
            }

            boolean bigTool = stack.getItem() instanceof ToolCore && slot == 1;
            float scale = bigTool ? TOOL_SCALE : MATERIAL_SCALE;
            float thickness = bigTool ? TOOL_THICKNESS : MATERIAL_THICKNESS;

            GL11.glPushMatrix();
            // the extrusion hangs below the sprite face, so lift by its world depth to rest it on the
            // table; stagger heights against z-fighting
            GL11.glTranslatef(
                    (float) posX + 0.5F + offX,
                    (float) posY + 1.001F + thickness * scale + slot * 0.004F,
                    (float) posZ + 0.5F + offZ);
            // lay the sprite flat, face up (icon top pointing north), sized by scale
            GL11.glRotatef(-90F, 1F, 0F, 0F);
            GL11.glScalef(scale, scale, scale);
            renderFlatItem(stack, thickness);
            GL11.glPopMatrix();
        }
    }

    /**
     * Draws the stack's 2D icon(s) exactly centered on the current origin, lying in the local XY plane. renderItemIn2D
     * spans (0,0)-(1,1) and extrudes its thickness towards -z, so a half-unit shift centers it — no entity-render
     * pipeline, none of its accumulated origin offsets.
     */
    private void renderFlatItem(ItemStack stack, float thickness) {
        Item item = stack.getItem();
        GL11.glPushMatrix();
        GL11.glTranslatef(-0.5F, -0.5F, 0F);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        Minecraft.getMinecraft().getTextureManager().bindTexture(
                stack.getItemSpriteNumber() == 0 ? TextureMap.locationBlocksTexture : TextureMap.locationItemsTexture);

        // ToolCore opts out of the vanilla multipass API (getRenderPasses() == 0, getIconIndex() == blank) because
        // its custom item renderer draws it; getIcon(stack, pass) still yields the real part and effect layers
        boolean tinkerTool = item instanceof ToolCore;
        int passes = tinkerTool ? 11
                : item.requiresMultipleRenderPasses() ? item.getRenderPasses(stack.getItemDamage()) : 1;
        for (int pass = 0; pass < passes; pass++) {
            IIcon icon = tinkerTool || passes > 1 ? item.getIcon(stack, pass) : stack.getIconIndex();
            if (icon == null) continue;
            int color = item.getColorFromItemStack(stack, pass);
            GL11.glColor4f((color >> 16 & 255) / 255F, (color >> 8 & 255) / 255F, (color & 255) / 255F, 1F);
            ItemRenderer.renderItemIn2D(
                    Tessellator.instance,
                    icon.getMaxU(),
                    icon.getMinV(),
                    icon.getMinU(),
                    icon.getMaxV(),
                    icon.getIconWidth(),
                    icon.getIconHeight(),
                    thickness);
        }

        GL11.glColor4f(1F, 1F, 1F, 1F);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();
    }
}
