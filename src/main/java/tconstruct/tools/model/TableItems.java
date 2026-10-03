package tconstruct.tools.model;

import static tconstruct.tools.model.BuildPreview.NO_PART;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import tconstruct.library.crafting.ToolPartRules;
import tconstruct.library.tools.BuildInput;
import tconstruct.library.tools.ToolCore;
import tconstruct.tools.gui.StationSlotLayouts;
import tconstruct.tools.logic.ToolStationLogic;

/** Repair & Modify on the table: the tool at the center and each material where the GUI's pentagon puts it. */
@SideOnly(Side.CLIENT)
final class TableItems {

    private TableItems() {}

    private static final int[][] PENTAGON = StationSlotLayouts.stationLayout(0);

    // TiC2's scales: 0.8 * 0.46875 for a material, times 1.3 more for the tool
    static final float TOOL_SCALE = 0.4875F;
    static final float TOOL_THICKNESS = 0.09375F;
    private static final float MATERIAL_SCALE = 0.375F;
    // vanilla's 1/16 is too thin at this scale; chosen with TOOL_THICKNESS so both slabs come out one height
    private static final float MATERIAL_THICKNESS = 0.12F;
    private static final float VANILLA_THICKNESS = 0.0625F;
    /** Clears the tabletop's top face so the two do not z-fight. */
    static final float REST = 0.001F;
    /** Keeps every material's sprite box on the table. */
    private static final float PENTAGON_REACH = 0.5F - MATERIAL_SCALE / 2F;
    /** Lifts one sprite over another so the two do not z-fight. */
    static final float HAIR = 0.0005F;
    private static final float MATERIAL_TOP = REST + MATERIAL_THICKNESS * MATERIAL_SCALE + PENTAGON[0].length * HAIR;
    /** Four hairs over the highest material: the depth buffer still tells that apart from about 40 blocks away. */
    private static final float MIDDLE_TOP = MATERIAL_TOP + 4 * HAIR;

    static void renderPentagon(ToolStationLogic logic, float tableTop) {
        // the center shows the output tool, and a part the craft takes in is not drawn beside it
        ItemStack output = craftedTool(logic);
        int taken = output != null ? takenParts(logic, logic.getStackInSlot(1), output) : 0;
        boolean fancy = Minecraft.getMinecraft().gameSettings.fancyGraphics;
        for (int slot = 1; slot <= PENTAGON[0].length && slot < logic.getSizeInventory(); slot++) {
            ItemStack stack = slot == 1 && output != null ? output : logic.getStackInSlot(slot);
            if (stack == null || (taken & (1 << slot)) != 0) continue;

            // GUI x -> world x, GUI y -> world z, about the tool's slot; the 61 is TiC2's
            float offX = reach((PENTAGON[0][slot - 1] - PENTAGON[0][0]) / 61F);
            float offZ = reach((PENTAGON[1][slot - 1] - PENTAGON[1][0]) / 61F);

            Block block = Block.getBlockFromItem(stack.getItem());
            if (stack.getItemSpriteNumber() == 0 && stack.getItem() instanceof ItemBlock
                    && RenderBlocks.renderItemIn3d(block.getRenderType())) {
                float cube = 0.2F;
                GL11.glPushMatrix();
                GL11.glTranslatef(offX, tableTop + cube / 2F - cube / 3F, offZ);
                // undoes renderBlockAsItem's quarter turn, as RenderItem does for a block in a frame
                GL11.glRotatef(-90F, 0F, 1F, 0F);
                GL11.glScalef(cube, cube, cube);
                LooseItems.renderBlockItem(block, stack.getItemDamage());
                GL11.glPopMatrix();
                continue;
            }

            boolean bigTool = stack.getItem() instanceof ToolCore && slot == 1;
            boolean framed = LooseItems.drawnFramed(stack);
            float scale = bigTool ? TOOL_SCALE : MATERIAL_SCALE;
            // a framed item is one flat quad with fast graphics
            float thickness = framed ? (fancy ? VANILLA_THICKNESS : 0F) : bigTool ? TOOL_THICKNESS : MATERIAL_THICKNESS;

            // the slab hangs below the face; the middle item lies over every material, each a hair over the slot before
            float top = slot == 1 ? MIDDLE_TOP : REST + thickness * scale + slot * HAIR;
            GL11.glPushMatrix();
            GL11.glTranslatef(offX, tableTop + top, offZ);
            GL11.glRotatef(-90F, 1F, 0F, 0F);
            GL11.glScalef(scale, scale, scale);
            if (framed) LooseItems.renderFramedItem(stack, logic, tableTop, fancy);
            else LooseItems.renderFlatItem(stack, thickness, fancy);
            GL11.glPopMatrix();
        }
    }

    private static float reach(float offset) {
        return Math.max(-PENTAGON_REACH, Math.min(PENTAGON_REACH, offset));
    }

    /** The output tool, if it is slot 1's tool after the craft; an old save may hold one built from parts. */
    private static ItemStack craftedTool(ToolStationLogic logic) {
        ItemStack tool = logic.getStackInSlot(1), output = logic.getStackInSlot(0);
        return tool != null && output != null && output.getItem() == tool.getItem() ? output : null;
    }

    /** Bits by slot of the parts the craft builds in: one slot per changed part, none when two could give it. */
    private static int takenParts(ToolStationLogic logic, ItemStack input, ItemStack output) {
        if (!(output.getItem() instanceof ToolCore)) return 0;
        ToolCore tool = (ToolCore) output.getItem();
        NBTTagCompound before = infiTool(input), after = infiTool(output);
        int[] changed = BuildPreview.noParts();
        boolean any = false;
        for (BuildInput kind : BuildInput.VALUES) {
            int material = after.getInteger(kind.key);
            if (material == before.getInteger(kind.key)) continue;
            changed[kind.renderPass] = material;
            any = true;
        }
        if (!any) return 0;
        Item[] parts = logic.partsOf(tool);
        // per layer: the one slot that gives it, -1 once two do
        int[] giver = new int[changed.length];
        for (int slot = 2; slot < logic.getSizeInventory(); slot++) {
            ItemStack stack = logic.getStackInSlot(slot);
            if (stack == null) continue;
            int gives = givenLayers(tool, parts, stack, changed);
            for (int pass = 0; pass < giver.length; pass++) {
                if ((gives & 1 << pass) != 0) giver[pass] = giver[pass] == 0 ? slot : -1;
            }
        }
        int taken = 0;
        for (int slot : giver) if (slot > 0) taken |= 1 << slot;
        return taken;
    }

    private static int givenLayers(ToolCore tool, Item[] parts, ItemStack stack, int[] changed) {
        int given = 0;
        for (BuildInput input : BuildInput.VALUES) {
            if (!ToolPartRules.acceptsPart(tool, input, stack)) continue;
            int[] materials = BuildPreview.noParts();
            BuildPreview.readCell(tool, parts, input, stack, materials);
            for (int pass = 0; pass < changed.length; pass++) {
                if (changed[pass] != NO_PART && materials[pass] == changed[pass]) given |= 1 << pass;
            }
        }
        return given;
    }

    private static NBTTagCompound infiTool(ItemStack stack) {
        return stack.hasTagCompound() ? stack.getTagCompound().getCompoundTag("InfiTool") : new NBTTagCompound();
    }
}
