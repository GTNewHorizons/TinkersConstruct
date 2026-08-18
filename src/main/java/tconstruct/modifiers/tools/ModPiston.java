package tconstruct.modifiers.tools;

import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import tconstruct.library.modifier.ModificationInfo;
import tconstruct.library.tools.ToolCore;

public class ModPiston extends ItemModTypeFilter {

    String tooltipName;

    public ModPiston(int effect, ItemStack[] items, int[] values) {
        super(effect, "Piston", items, values);
        tooltipName = "\u00a77Knockback";
        this.max = 10;
    }

    @Override
    protected boolean canModify(ItemStack tool, ItemStack[] input) {
        if (tool.getItem() instanceof ToolCore) {
            ToolCore toolItem = (ToolCore) tool.getItem();
            if (!validType(toolItem)) return false;

            return hasCapacityFor(input, tool);
        }
        return false;
    }

    @Override
    public void modify(ItemStack[] input, ItemStack tool) {
        NBTTagCompound tags = tool.getTagCompound().getCompoundTag("InfiTool");
        ModificationInfo modificationInfo = matchingAmount(input, tool);
        int increase = modificationInfo.total();
        tags.setIntArray("ToRemove", modificationInfo.toRemove());

        if (tags.hasKey(key)) {
            int[] keyPair = tags.getIntArray(key);
            addProgress(tags, keyPair, increase);
            updateModTag(tool, keyPair);
        } else {
            int modifiers = tags.getInteger("Modifiers");
            modifiers -= 1;
            tags.setInteger("Modifiers", modifiers);
            String modName = "\u00a74Knockback (" + increase + "/" + max + ")";
            int tooltipIndex = addToolTip(tool, tooltipName, modName);
            int[] keyPair = new int[] { increase, max, tooltipIndex };
            tags.setIntArray(key, keyPair);
        }

        float knockback = tags.getFloat("Knockback");

        knockback += 0.1 * increase;
        tags.setFloat("Knockback", knockback);
    }

    void updateModTag(ItemStack tool, int[] keys) {
        NBTTagCompound tags = tool.getTagCompound().getCompoundTag("InfiTool");
        String tip = "ModifierTip" + keys[2];
        String modName = "\u00a77Knockback (" + keys[0] + "/" + keys[1] + ")";
        tags.setString(tip, modName);
    }

    public boolean validType(ToolCore tool) {
        List<String> list = Arrays.asList(tool.getTraits());
        return list.contains("weapon") || list.contains("ammo");
    }
}
