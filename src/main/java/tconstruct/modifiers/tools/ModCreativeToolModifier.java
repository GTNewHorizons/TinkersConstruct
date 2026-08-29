package tconstruct.modifiers.tools;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import tconstruct.library.modifier.ItemModifier;
import tconstruct.library.tools.ToolCore;

public class ModCreativeToolModifier extends ItemModifier {

    public ModCreativeToolModifier(ItemStack[] items) {
        super(items, 0, "");
    }

    @Override
    protected boolean canModify(ItemStack tool, ItemStack[] input) {
        if (tool.getItem() instanceof ToolCore) {
            ToolCore toolItem = (ToolCore) tool.getItem();
            for (ItemStack stack : input) {
                if (stack != null && stack.hasTagCompound()) {
                    String targetLock = stack.getTagCompound().getString("TargetLock");
                    if (!targetLock.equals("") && !targetLock.equals(toolItem.getToolName())) return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void modify(ItemStack[] input, ItemStack tool) {
        NBTTagCompound tags = tool.getTagCompound().getCompoundTag("InfiTool");
        int modifiers = tags.getInteger("Modifiers");
        // One slot per item. With the station's "Tiers: fill all" on, the whole stack is taken at once; the recipe
        // is a single item, so the subset offered here holds exactly one stack.
        int count = 1;
        if (isTierSpilloverOn()) {
            for (ItemStack stack : input) {
                if (stack != null) count = stack.stackSize;
            }
            tags.setIntArray("ToRemove", new int[] { count });
        }
        modifiers += count;
        tags.setInteger("Modifiers", modifiers);
    }

    public void addMatchingEffect(ItemStack tool) {}
}
