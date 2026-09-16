package tconstruct.modifiers.tools;

import net.minecraft.item.ItemStack;

import tconstruct.library.tools.AOEHarvestTool;

/* Width++/Height++ from TiC2: boolean flag, applies once, only fits tools with an AOE */

public class ModExpander extends ModBoolean {

    public ModExpander(ItemStack[] items, String tag, String c, String tip) {
        super(items, 0, tag, c, tip);
    }

    @Override
    protected boolean canModify(ItemStack tool, ItemStack[] input) {
        return tool.getItem() instanceof AOEHarvestTool && super.canModify(tool, input);
    }

    @Override
    public void addMatchingEffect(ItemStack input) {
        // expanders have no render overlay on the tool
    }
}
