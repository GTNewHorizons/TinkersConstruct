package tconstruct.tools.model;

import static tconstruct.tools.model.TableItems.REST;
import static tconstruct.tools.model.TableItems.TOOL_SCALE;
import static tconstruct.tools.model.TableItems.TOOL_THICKNESS;

import java.util.Arrays;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import tconstruct.library.crafting.ToolBuilder;
import tconstruct.library.crafting.ToolPartRules;
import tconstruct.library.tools.BuildInput;
import tconstruct.library.tools.DualMaterialToolPart;
import tconstruct.library.tools.ToolCore;
import tconstruct.library.weaponry.ArrowShaftMaterial;
import tconstruct.tools.logic.ToolStationLogic;
import tconstruct.weaponry.AmmoMaterials;
import tconstruct.weaponry.TinkerWeaponry;

/** The tool a build tab is making, drawn on the tabletop from its cells, each read the way the builder reads it. */
@SideOnly(Side.CLIENT)
final class BuildPreview {

    private BuildPreview() {}

    /** Width of the tool taking shape, in blocks. */
    static final float BUILD_SCALE = 0.75F;
    private static final float SLAB = TOOL_THICKNESS * TOOL_SCALE;
    static final int NO_PART = Integer.MIN_VALUE;

    static void renderBuild(ToolStationLogic logic, ToolCore tool, float tableTop) {
        int[] materials = layerMaterials(logic, tool);
        boolean placed = false;
        for (int material : materials) placed |= material != NO_PART;
        if (!placed) return;
        ItemStack preview = previewOf(tool, materials);
        int passes = Math.min(BuildInput.VALUES.length, tool.getPartAmount());
        // a missing layer draws its part's default icon, key -1
        IIcon[] icons = new IIcon[passes];
        for (int pass = 0; pass < passes; pass++) {
            IIcon icon = materials[pass] == NO_PART ? BuildInput.ofPass(pass).iconsOf(tool).get(-1)
                    : tool.getIcon(preview, pass);
            icons[pass] = icon == ToolCore.blankSprite || icon == ToolCore.emptyIcon ? null : icon;
        }
        int width = 1, height = 1;
        for (IIcon icon : icons) {
            if (icon == null) continue;
            width = Math.max(width, icon.getIconWidth());
            height = Math.max(height, icon.getIconHeight());
        }
        GL11.glPushMatrix();
        GL11.glTranslatef(0F, tableTop + REST + SLAB, 0F);
        GL11.glRotatef(-90F, 1F, 0F, 0F);
        GL11.glScalef(BUILD_SCALE, BUILD_SCALE, BUILD_SCALE);
        GL11.glTranslatef(-0.5F, -0.5F, 0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationItemsTexture);
        // item lighting would clip a tinted layer toward white
        GL11.glDisable(GL11.GL_LIGHTING);
        boolean[][] masks = new boolean[passes][];
        boolean readable = true;
        for (int pass = 0; pass < passes; pass++) {
            if (materials[pass] == NO_PART || icons[pass] == null) continue;
            masks[pass] = TexelMasks.onGrid(icons[pass], width, height);
            readable &= masks[pass] != null;
        }
        int[] owner = readable ? LayerSlabs.owners(tool, materials, icons, masks, width, height) : null;
        for (int pass = 0; pass < passes; pass++) {
            if (materials[pass] == NO_PART || icons[pass] == null) continue;
            LooseItems.glColor(tool.getColorFromItemStack(preview, pass));
            if (owner != null) LayerSlabs.drawLayer(icons[pass], pass, SLAB / BUILD_SCALE, owner, width, height);
            else LayerSlabs.drawWhole(icons[pass], pass, SLAB / BUILD_SCALE);
        }
        GL11.glColor4f(1F, 1F, 1F, 1F);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
    }

    static int[] noParts() {
        int[] materials = new int[BuildInput.VALUES.length];
        Arrays.fill(materials, NO_PART);
        return materials;
    }

    /** A part draws as soon as it is placed, whether or not the build accepts the cells yet. */
    private static int[] layerMaterials(ToolStationLogic logic, ToolCore tool) {
        Item[] parts = logic.partsOf(tool);
        int[] materials = noParts();
        for (BuildInput input : BuildInput.VALUES) {
            // a station has no extra cell to read
            if (!logic.isPartSlot(parts, input.cell())) continue;
            ItemStack stack = logic.getStackInSlot(input.cell());
            if (stack != null) readCell(tool, parts, input, stack, materials);
        }
        return materials;
    }

    static void readCell(ToolCore tool, Item[] parts, BuildInput input, ItemStack stack, int[] materials) {
        if (input == BuildInput.HEAD && parts[input.ordinal()] == TinkerWeaponry.partBolt) {
            if (stack.getItem() instanceof DualMaterialToolPart core) {
                // second material to the head, first to the shaft, as WeaponryHandler.buildBolt splits a core
                materials[BuildInput.HEAD.renderPass] = orNoPart(core.getMaterialID2(stack));
                materials[BuildInput.HANDLE.renderPass] = orNoPart(core.getMaterialID(stack));
            }
            return;
        }
        materials[tool.kindOf(input).renderPass] = materialOf(tool, input, parts[input.ordinal()], stack);
    }

    private static int materialOf(ToolCore tool, BuildInput input, Item part, ItemStack stack) {
        if (part == TinkerWeaponry.partArrowShaft) {
            ArrowShaftMaterial shaft = AmmoMaterials.shaftMaterial(stack);
            return shaft != null ? shaft.materialID : NO_PART;
        }
        return orNoPart(ToolBuilder.instance.getMaterialID(ToolPartRules.partAsBuilt(tool, input, stack)));
    }

    private static int orNoPart(int id) {
        return id == -1 ? NO_PART : id;
    }

    /** A stack with only the material keys, enough for getIcon and getColorFromItemStack. Never stored. */
    private static ItemStack previewOf(ToolCore tool, int[] materials) {
        NBTTagCompound tags = new NBTTagCompound();
        for (BuildInput kind : BuildInput.VALUES) {
            int material = materials[kind.renderPass];
            if (material == NO_PART) continue;
            tags.setInteger(kind.key, material);
            tags.setInteger(kind.renderKey, material);
        }
        NBTTagCompound compound = new NBTTagCompound();
        compound.setTag("InfiTool", tags);
        ItemStack preview = new ItemStack(tool);
        preview.setTagCompound(compound);
        return preview;
    }
}
