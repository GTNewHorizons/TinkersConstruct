package tconstruct.smeltery.itemblocks;

import net.minecraft.block.Block;

import mantle.blocks.abstracts.MultiItemBlock;

public class SearedSlabItem extends MultiItemBlock {

    public static final String[] blockTypes = { "brick", "stone", "cobble", "paver", "road", "fancy", "square",
            "creeper" };

    public SearedSlabItem(Block b) {
        super(b, "block.searedstone.slab", blockTypes);
        setMaxDamage(0);
        setHasSubtypes(true);
    }
}
