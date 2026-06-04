package tconstruct.smeltery.itemblocks;

import net.minecraft.block.Block;
import net.minecraft.util.IIcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mantle.blocks.abstracts.MultiItemBlock;

public class StainedGlassClearPaneItem extends MultiItemBlock {

    public static final String[] blockTypes = { "white", "orange", "magenta", "lightblue", "yellow", "lime", "pink",
            "gray", "lightgray", "cyan", "purple", "blue", "brown", "green", "red", "black" };

    public StainedGlassClearPaneItem(Block b) {
        super(b, "block.stainedglass", "pane", blockTypes);
        setMaxDamage(0);
        setHasSubtypes(true);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int meta) {
        return this.field_150939_a.getIcon(1, meta);
    }
}
