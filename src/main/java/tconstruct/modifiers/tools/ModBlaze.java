package tconstruct.modifiers.tools;

import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import tconstruct.library.modifier.ModificationInfo;
import tconstruct.library.tools.ToolCore;

public class ModBlaze extends ItemModTypeFilter {

    String tooltipName;

    public ModBlaze(int effect, ItemStack[] items, int[] values) {
        super(effect, "Blaze", items, values);
        tooltipName = "\u00a76Fiery";
        this.max = 25;
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
            String modName = "\u00a76Blaze (" + increase + "/" + max + ")";
            int tooltipIndex = addToolTip(tool, tooltipName, modName);
            int[] keyPair = new int[] { increase, max, tooltipIndex };
            tags.setIntArray(key, keyPair);
            // spillover: a first craft may open more tiers than the one it comes with
            if (settleTiers(tags, keyPair)) updateModTag(tool, keyPair);
        }

        int fiery = tags.getInteger("Fiery");
        fiery += (increase);
        tags.setInteger("Fiery", fiery);
    }

    void updateModTag(ItemStack tool, int[] keys) {
        NBTTagCompound tags = tool.getTagCompound().getCompoundTag("InfiTool");
        String tip = "ModifierTip" + keys[2];
        String modName = "\u00a76Blaze (" + keys[0] + "/" + keys[1] + ")";
        tags.setString(tip, modName);
    }

    public boolean validType(ToolCore tool) {
        List<String> list = Arrays.asList(tool.getTraits());
        return list.contains("melee") || list.contains("ammo");
    }
}
