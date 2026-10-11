package tconstruct.world.itemblocks;

import net.minecraft.block.Block;

import mantle.blocks.abstracts.MultiItemBlock;

public class MetalOreItemBlock extends MultiItemBlock {

    public static final String[] blockTypes = { "NetherSlag", "Cobalt", "Ardite", "Copper", "Tin", "Aluminum", "Slag" };

    public MetalOreItemBlock(Block b) {
        super(b, "MetalOre", blockTypes);
        setMaxDamage(0);
        setHasSubtypes(true);
    }

}
