package tconstruct.world.itemblocks;

import net.minecraft.block.Block;

import mantle.blocks.abstracts.MultiItemBlock;

public class GravelOreItem extends MultiItemBlock {

    public static final String[] blockTypes = { "iron", "gold", "copper", "tin", "aluminum", "cobalt" };

    public GravelOreItem(Block b) {
        super(b, "block.ore.gravel", blockTypes);
        setMaxDamage(0);
        setHasSubtypes(true);
    }
}
