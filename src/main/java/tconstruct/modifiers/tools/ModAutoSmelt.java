package tconstruct.modifiers.tools;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ModAutoSmelt extends ModBoolean {

    public ModAutoSmelt(ItemStack[] items, int effect, String tag, String c, String tip) {
        super(items, effect, tag, c, tip);
    }

    @Override
    protected boolean canModify(ItemStack tool, ItemStack[] input) {
        NBTTagCompound tags = tool.getTagCompound().getCompoundTag("InfiTool");
        if (tags.getBoolean("Silk Touch")) return false;
        return super.canModify(tool, input);
    }
}
