package tconstruct.world.itemblocks;

import mantle.blocks.abstracts.MultiItemBlock;

import net.minecraft.block.Block;
import net.minecraft.util.*;

import tconstruct.world.TinkerWorld;
import cpw.mods.fml.relauncher.*;

public class SlimeTallGrassItem extends MultiItemBlock {

    public static final String[] blockTypes = { "tallgrass", "tallgrass.fern" };

    public SlimeTallGrassItem(Block b) {
        super(b, "block.slime", blockTypes);
        setMaxDamage(0);
        setHasSubtypes(true);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int meta) {
        int arr = MathHelper.clamp_int(meta, 0, blockTypes.length);
        return TinkerWorld.slimeTallGrass.getIcon(0, arr);
    }
}
