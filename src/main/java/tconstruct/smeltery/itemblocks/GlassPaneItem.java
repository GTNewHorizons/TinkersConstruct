package tconstruct.smeltery.itemblocks;

import net.minecraft.block.Block;

import mantle.blocks.abstracts.MultiItemBlock;

public class GlassPaneItem extends MultiItemBlock {

    public static final String[] blockTypes = { "pure", "soul", "soul.pure" };

    public GlassPaneItem(Block b) {
        super(b, "block.glass", "pane", blockTypes);
        setMaxDamage(0);
        setHasSubtypes(true);
    }
}
