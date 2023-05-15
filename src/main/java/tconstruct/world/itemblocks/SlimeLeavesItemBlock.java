package tconstruct.world.itemblocks;

import net.minecraft.block.Block;

import mantle.blocks.abstracts.MultiItemBlock;

public class SlimeLeavesItemBlock extends MultiItemBlock {

    public static final String[] blockTypes = { "blue" };

    public SlimeLeavesItemBlock(Block b) {
        super(b, "block.slime.leaves", blockTypes);
        setMaxDamage(0);
        setHasSubtypes(true);
    }
}
