package tconstruct.modifiers.tools;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import tconstruct.library.modifier.ModificationInfo;

public class ModAntiSpider extends ItemModTypeFilter {

    String tooltipName;
    String guiType;

    public ModAntiSpider(String type, int effect, ItemStack[] items, int[] values) {
        super(effect, "ModAntiSpider", items, values);
        tooltipName = "\u00a72Bane of Arthropods";
        guiType = type;
        this.max = 4;
    }

    @Override
    protected boolean canModify(ItemStack tool, ItemStack[] input) {
        return hasCapacityFor(input, tool);
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
            String modName = "\u00a72" + guiType + " (" + increase + "/" + max + ")";
            int tooltipIndex = addToolTip(tool, tooltipName, modName);
            int[] keyPair = new int[] { increase, max, tooltipIndex };
            tags.setIntArray(key, keyPair);
            // spillover: a first craft may open more tiers than the one it comes with
            if (settleTiers(tags, keyPair)) updateModTag(tool, keyPair);
        }
    }

    void updateModTag(ItemStack tool, int[] keys) {
        NBTTagCompound tags = tool.getTagCompound().getCompoundTag("InfiTool");
        String tip = "ModifierTip" + keys[2];
        String modName = "\u00a72" + guiType + " (" + keys[0] + "/" + keys[1] + ")";
        tags.setString(tip, modName);
    }
}
