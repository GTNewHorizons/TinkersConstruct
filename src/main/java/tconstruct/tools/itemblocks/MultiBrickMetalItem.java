package tconstruct.tools.itemblocks;

import net.minecraft.block.Block;

import mantle.blocks.abstracts.MultiItemBlock;

public class MultiBrickMetalItem extends MultiItemBlock {

    static String[] blockTypes = { "brick.alumite", "brick.ardite", "brick.cobalt", "brick.manyullyn",
            "fancybrick.alumite", "fancybrick.ardite", "fancybrick.cobalt", "fancybrick.manyullyn" };

    public MultiBrickMetalItem(Block b) {
        super(b, "block", blockTypes);
        setMaxDamage(0);
        setHasSubtypes(true);
    }
}
