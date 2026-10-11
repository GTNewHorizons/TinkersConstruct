package tconstruct.smeltery.logic;

import java.util.stream.IntStream;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;

/** The block appearance of the contents, independent of the GUI's actual inventory stacks. */
public final class SmelteryRenderData {

    private final int[] blocks;
    private final int[] metadata;
    private final float[] heights;

    public SmelteryRenderData(int size, int[] runs) {
        blocks = new int[size];
        metadata = new int[size];
        heights = new float[size];
        int start = 0;
        for (int i = 0; i < runs.length; i += 4) {
            int end = runs[i];
            java.util.Arrays.fill(blocks, start, end, runs[i + 1]);
            java.util.Arrays.fill(metadata, start, end, runs[i + 2]);
            java.util.Arrays.fill(heights, start, end, Float.intBitsToFloat(runs[i + 3]));
            start = end;
        }
    }

    /** Each run stores its exclusive end, block ID, metadata and exact rendered height. */
    public static int[] encode(ItemStack[] inventory, ItemStack[] renderStacks, int[] temperatures) {
        IntStream.Builder runs = IntStream.builder();
        int previousBlock = -1;
        int previousMeta = 0;
        int previousHeight = 0;
        for (int slot = 0; slot < inventory.length; slot++) {
            int block = 0;
            int meta = 0;
            int height = 0;
            ItemStack input = inventory[slot];
            ItemStack render = renderStacks[slot];
            if (input != null && render != null && temperatures[slot] / 10 > 20) {
                block = Block.getIdFromBlock(Block.getBlockFromItem(render.getItem()));
                meta = render.getItemDamage();
                height = Float.floatToIntBits(
                        MathHelper.clamp_float(input.stackSize / (float) render.stackSize, 0.01F, 1.0F));
            }
            if (block != previousBlock || meta != previousMeta || height != previousHeight) {
                if (slot > 0) runs.add(slot).add(previousBlock).add(previousMeta).add(previousHeight);
                previousBlock = block;
                previousMeta = meta;
                previousHeight = height;
            }
        }
        if (inventory.length > 0) {
            runs.add(inventory.length).add(previousBlock).add(previousMeta).add(previousHeight);
        }
        return runs.build().toArray();
    }

    public Block getBlock(int slot) {
        return Block.getBlockById(blocks[slot]);
    }

    public int size() {
        return blocks.length;
    }

    public int getMetadata(int slot) {
        return metadata[slot];
    }

    public float getHeight(int slot) {
        return heights[slot];
    }
}
