package tconstruct.tools.model;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import tconstruct.library.tools.ToolCore;
import tconstruct.tools.logic.ToolStationLogic;

/* Renders the station's contents lying on the table, mirroring the Repair & Modify pentagon */

@SideOnly(Side.CLIENT)
public class ToolStationTesr extends TileEntitySpecialRenderer {

    // GUI pentagon slot positions (see ToolStationGui.setSlotType case 0), tool first
    private static final int[] SLOT_X = { 33, 33, 11, 55, 15, 51 };
    private static final int[] SLOT_Y = { 40, 17, 35, 35, 61, 61 };

    private static final float TOOL_SCALE = 0.85F;
    private static final float TOOL_THICKNESS = 0.09375F;
    private static final float MATERIAL_SCALE = 0.4F;
    private static final float MATERIAL_THICKNESS = 0.25F;

    private final RenderBlocks renderBlocksInstance = new RenderBlocks();

    /** Alpha footprint of one icon: opaque[r * width + c], row 0 = icon top. */
    private static final class IconMask {

        final int width, height;
        final boolean[] opaque;

        IconMask(int width, int height, boolean[] opaque) {
            this.width = width;
            this.height = height;
            this.opaque = opaque;
        }
    }

    private static final Map<IIcon, IconMask> MASK_CACHE = new HashMap<>();
    private static byte[] itemsAtlasAlpha = null;
    private static int atlasWidth, atlasHeight;

    @Override
    public void renderTileEntityAt(TileEntity logic, double posX, double posY, double posZ, float partialTicks) {
        render((ToolStationLogic) logic, posX, posY, posZ);
    }

    private void render(ToolStationLogic logic, double posX, double posY, double posZ) {
        // per-pixel resting: a material whose opaque pixels overlap the tool's opaque pixels lies on
        // the tool's slab; anything over the sprite's transparent corners stays down on the wood
        ItemStack center = logic.getSizeInventory() > 1 ? logic.getStackInSlot(1) : null;
        List<IconMask> toolMasks = center != null && center.getItem() instanceof ToolCore ? iconMasks(center) : null;
        float toolTop = TOOL_THICKNESS * TOOL_SCALE + 0.006F;

        for (int slot = 1; slot <= 6 && slot < logic.getSizeInventory(); slot++) {
            ItemStack stack = logic.getStackInSlot(slot);
            if (stack == null) continue;

            // map the GUI layout onto the table top: GUI x -> world x, GUI y -> world z
            float offX = (SLOT_X[slot - 1] - 33) / 61F * 0.65F;
            float offZ = (SLOT_Y[slot - 1] - 40) / 61F * 0.65F;

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

            float basePlane = 0F;
            if (!bigTool && toolMasks != null && restsOnTool(stack, offX, offZ, toolMasks)) {
                basePlane = toolTop;
            }

            GL11.glPushMatrix();
            // the extrusion hangs below the sprite face, so lift by its world depth to rest it on its
            // base plane; stagger heights against z-fighting
            GL11.glTranslatef(
                    (float) posX + 0.5F + offX,
                    (float) posY + 1.001F + basePlane + thickness * scale + slot * 0.004F,
                    (float) posZ + 0.5F + offZ);
            // lay the sprite flat, face up (icon top pointing north), sized by scale
            GL11.glRotatef(-90F, 1F, 0F, 0F);
            GL11.glScalef(scale, scale, scale);
            renderFlatItem(stack, thickness);
            GL11.glPopMatrix();
        }
    }

    /**
     * True when at least a few of the material sprite's opaque pixels lie over opaque pixels of the tool sprite. Both
     * sprites use the same table mapping, so a shared texel-to-world transform keeps the comparison consistent.
     */
    private static boolean restsOnTool(ItemStack material, float offX, float offZ, List<IconMask> toolMasks) {
        int overlapping = 0;
        for (IconMask mask : iconMasks(material)) {
            for (int r = 0; r < mask.height; r++) {
                for (int c = 0; c < mask.width; c++) {
                    if (!mask.opaque[r * mask.width + c]) continue;
                    // world offset of this texel from the table center
                    float wx = offX + (0.5F - (c + 0.5F) / mask.width) * MATERIAL_SCALE;
                    float wz = offZ + ((r + 0.5F) / mask.height - 0.5F) * MATERIAL_SCALE;
                    for (IconMask tool : toolMasks) {
                        int tc = (int) ((0.5F - wx / TOOL_SCALE) * tool.width);
                        int tr = (int) ((wz / TOOL_SCALE + 0.5F) * tool.height);
                        if (tc >= 0 && tc < tool.width
                                && tr >= 0
                                && tr < tool.height
                                && tool.opaque[tr * tool.width + tc]) {
                            if (++overlapping >= 3) return true;
                            break;
                        }
                    }
                }
            }
        }
        return false;
    }

    /** Footprint masks for every render pass of the stack, from the cached items-atlas alpha channel. */
    private static List<IconMask> iconMasks(ItemStack stack) {
        Item item = stack.getItem();
        boolean tinkerTool = item instanceof ToolCore;
        int passes = tinkerTool ? 11
                : item.requiresMultipleRenderPasses() ? item.getRenderPasses(stack.getItemDamage()) : 1;
        List<IconMask> masks = new ArrayList<>(passes);
        for (int pass = 0; pass < passes; pass++) {
            IIcon icon = tinkerTool || passes > 1 ? item.getIcon(stack, pass) : stack.getIconIndex();
            if (icon == null) continue;
            IconMask mask = maskFor(icon);
            if (mask != null) masks.add(mask);
        }
        return masks;
    }

    private static IconMask maskFor(IIcon icon) {
        IconMask cached = MASK_CACHE.get(icon);
        if (cached != null) return cached;
        if (itemsAtlasAlpha == null && !readItemsAtlasAlpha()) return null;

        int w = Math.max(1, icon.getIconWidth());
        int h = Math.max(1, icon.getIconHeight());
        boolean[] opaque = new boolean[w * h];
        float minU = icon.getMinU(), du = icon.getMaxU() - minU;
        float minV = icon.getMinV(), dv = icon.getMaxV() - minV;
        for (int r = 0; r < h; r++) {
            int ay = (int) ((minV + dv * (r + 0.5F) / h) * atlasHeight);
            for (int c = 0; c < w; c++) {
                int ax = (int) ((minU + du * (c + 0.5F) / w) * atlasWidth);
                if (ax >= 0 && ax < atlasWidth && ay >= 0 && ay < atlasHeight) {
                    opaque[r * w + c] = (itemsAtlasAlpha[ay * atlasWidth + ax] & 0xFF) > 32;
                }
            }
        }
        IconMask mask = new IconMask(w, h, opaque);
        MASK_CACHE.put(icon, mask);
        return mask;
    }

    /** One-time readback of the item atlas alpha channel; sprites never move within a session. */
    private static boolean readItemsAtlasAlpha() {
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationItemsTexture);
        atlasWidth = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
        atlasHeight = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
        if (atlasWidth <= 0 || atlasHeight <= 0) return false;
        ByteBuffer buffer = BufferUtils.createByteBuffer(atlasWidth * atlasHeight * 4);
        GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
        itemsAtlasAlpha = new byte[atlasWidth * atlasHeight];
        for (int i = 0; i < itemsAtlasAlpha.length; i++) {
            itemsAtlasAlpha[i] = buffer.get(i * 4 + 3);
        }
        return true;
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
