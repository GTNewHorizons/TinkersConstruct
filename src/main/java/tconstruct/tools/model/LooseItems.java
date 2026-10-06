package tconstruct.tools.model;

import java.util.Random;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.client.IItemRenderer.ItemRendererHelper;
import net.minecraftforge.client.MinecraftForgeClient;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.google.common.collect.ImmutableSet;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import tconstruct.library.tools.ToolCore;

/** One item on the tabletop: a block as a small cube, a GregTech item through its own renderer, else a flat sprite. */
@SideOnly(Side.CLIENT)
final class LooseItems {

    private LooseItems() {}

    private static final RenderBlocks renderBlocksInstance = new RenderBlocks();
    /** An icon's width as RenderItem draws it in a frame: 0.5 / 0.975. */
    private static final float FRAMED_SIZE = 0.5128205F;
    /** How far a framed icon's right-reading face lies toward +z with fancy graphics, in icon widths. */
    private static final float FRAMED_FACE = 0.0203125F;
    /** GregTech renderers that lay a dropped item out as vanilla does; by name, as there is no GregTech dependency. */
    private static final Set<String> GREGTECH_RENDERERS = ImmutableSet.of(
            "gregtech.common.render.items.MetaGeneratedItemRenderer",
            "gregtech.common.render.MetaGeneratedToolRenderer",
            "gregtech.common.render.FlaskRenderer");
    /** Handed to a renderer that wants an entity. Never spawned; its world is dropped after each draw. */
    private static EntityItem holder;
    private static final Random RANDOM = new Random();
    private static final ResourceLocation GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");

    static void renderBlockItem(Block block, int meta) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        GL11.glColor4f(1F, 1F, 1F, 1F);
        boolean translucent = block.getRenderBlockPass() > 0;
        if (translucent) {
            GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        }
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        renderBlocksInstance.renderBlockAsItem(block, meta, 1.0F);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        if (translucent) GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1F, 1F, 1F, 1F);
    }

    /** The stack's icons centered on the origin in the local XY plane, extruded toward -z. */
    static void renderFlatItem(ItemStack stack, float thickness, boolean fancy) {
        Item item = stack.getItem();
        GL11.glPushMatrix();
        GL11.glTranslatef(-0.5F, -0.5F, 0F);
        // item lighting brightens a flat sprite and clips a tinted one toward white
        GL11.glDisable(GL11.GL_LIGHTING);
        TextureManager textures = Minecraft.getMinecraft().getTextureManager();
        ResourceLocation atlas = stack.getItemSpriteNumber() == 0 ? TextureMap.locationBlocksTexture
                : TextureMap.locationItemsTexture;
        textures.bindTexture(atlas);

        // ToolCore reports no render passes, since its item renderer draws it, but getIcon serves passes 0 to 10 and
        // pads the unused ones with blankSprite or emptyIcon
        boolean tinkerTool = item instanceof ToolCore;
        int passes = tinkerTool ? 11
                : item.requiresMultipleRenderPasses() ? item.getRenderPasses(stack.getItemDamage()) : 1;
        for (int pass = 0; pass < passes; pass++) {
            IIcon icon = tinkerTool || passes > 1 ? item.getIcon(stack, pass) : stack.getIconIndex();
            if (icon == null || icon == ToolCore.blankSprite || icon == ToolCore.emptyIcon) continue;
            glColor(item.getColorFromItemStack(stack, pass));
            // renderItemIn2D's front face is x-mirrored; swap the U arguments to right it
            ItemRenderer.renderItemIn2D(
                    Tessellator.instance,
                    icon.getMinU(),
                    icon.getMinV(),
                    icon.getMaxU(),
                    icon.getMaxV(),
                    icon.getIconWidth(),
                    icon.getIconHeight(),
                    thickness);
            if (fancy && stack.hasEffect(pass)) {
                drawGlint(thickness);
                textures.bindTexture(atlas);
            }
        }

        GL11.glColor4f(1F, 1F, 1F, 1F);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
    }

    private static void drawGlint(float thickness) {
        GL11.glDepthFunc(GL11.GL_EQUAL);
        Minecraft.getMinecraft().getTextureManager().bindTexture(GLINT);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE);
        GL11.glColor4f(0.5F * 0.76F, 0.25F * 0.76F, 0.8F * 0.76F, 1F);
        GL11.glMatrixMode(GL11.GL_TEXTURE);
        glintLayer(3000L, 1F, -50F, thickness);
        glintLayer(4873L, -1F, 10F, thickness);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glDisable(GL11.GL_BLEND);
        // the next tile entity's renderer may blend without setting a function
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
    }

    private static void glintLayer(long period, float direction, float angle, float thickness) {
        GL11.glPushMatrix();
        GL11.glScalef(0.125F, 0.125F, 0.125F);
        GL11.glTranslatef(direction * ((float) (Minecraft.getSystemTime() % period) / (float) period * 8F), 0F, 0F);
        GL11.glRotatef(angle, 0F, 0F, 1F);
        ItemRenderer.renderItemIn2D(Tessellator.instance, 0F, 0F, 1F, 1F, 255, 255, thickness);
        GL11.glPopMatrix();
    }

    /** Whether a GregTech renderer draws the stack flat: its colors, overlays and fluids live there only. */
    static boolean drawnFramed(ItemStack stack) {
        IItemRenderer renderer = MinecraftForgeClient.getItemRenderer(stack, ItemRenderType.ENTITY);
        if (renderer == null || !GREGTECH_RENDERERS.contains(renderer.getClass().getName())) return false;
        if (renderer.shouldUseRenderHelper(ItemRenderType.ENTITY, stack, ItemRendererHelper.BLOCK_3D)) return false;
        return !(stack.getItem() instanceof ItemBlock)
                || !RenderBlocks.renderItemIn3d(Block.getBlockFromItem(stack.getItem()).getRenderType());
    }

    /** The stack through its own renderer, framed, laid out as renderFlatItem lays a sprite. */
    static void renderFramedItem(ItemStack stack, TileEntity table, float tableTop, boolean fancy) {
        World world = table.getWorldObj();
        if (holder == null) holder = new EntityItem(world);
        holder.setWorld(world);
        // a renderer that lights the item by its entity's position reads the table's
        holder.setPosition(table.xCoord + 0.5, table.yCoord + tableTop, table.zCoord + 0.5);
        holder.setEntityItemStack(stack);
        holder.hoverStart = 0F;
        float face = fancy ? FRAMED_FACE : 0F;
        GL11.glPushMatrix();
        GL11.glScalef(1F / FRAMED_SIZE, 1F / FRAMED_SIZE, 1F / FRAMED_SIZE);
        // the framed icon spans -0.3 to 0.7 of its width; its right-reading face is brought to z = 0
        GL11.glTranslatef(0F, -0.2F * FRAMED_SIZE, -face * FRAMED_SIZE);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        boolean inFrame = RenderItem.renderInFrame;
        RenderItem.renderInFrame = true;
        ForgeHooksClient.renderEntityItem(
                holder,
                stack,
                0F,
                0F,
                RANDOM,
                Minecraft.getMinecraft().getTextureManager(),
                renderBlocksInstance,
                1);
        RenderItem.renderInFrame = inFrame;
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glColor4f(1F, 1F, 1F, 1F);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
        holder.setWorld(null);
    }

    static void glColor(int rgb) {
        GL11.glColor4f((rgb >> 16 & 255) / 255F, (rgb >> 8 & 255) / 255F, (rgb & 255) / 255F, 1F);
    }
}
