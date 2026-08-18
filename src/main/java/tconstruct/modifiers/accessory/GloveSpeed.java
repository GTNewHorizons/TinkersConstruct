package tconstruct.modifiers.accessory;

import java.util.Arrays;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;

import tconstruct.library.accessory.AccessoryCore;
import tconstruct.library.modifier.IModifyable;
import tconstruct.library.modifier.ModificationInfo;
import tconstruct.modifiers.tools.ItemModTypeFilter;

public class GloveSpeed extends ItemModTypeFilter {

    String tooltipName;

    public GloveSpeed(int effect, ItemStack[] items, int[] values) {
        super(effect, "Redstone", items, values);
        tooltipName = ("\u00a74" + StatCollector.translateToLocal("modifier.tooltip.Haste"));
        this.max = 100;
    }

    @Override
    protected boolean canModify(ItemStack input, ItemStack[] modifiers) {
        if (input.getItem() instanceof AccessoryCore) {
            // is glove?
            if (!Arrays.asList(((AccessoryCore) input.getItem()).getTraits()).contains("glove")) return false;

            return hasCapacityFor(modifiers, input);
        }

        return false;
    }

    @Override
    public void modify(ItemStack[] modifiers, ItemStack input) {
        NBTTagCompound tags = getModifierTag(input);
        int[] keyPair;
        ModificationInfo modificationInfo = matchingAmount(modifiers, input);
        int increase = modificationInfo.total();
        tags.setIntArray("ToRemove", modificationInfo.toRemove());

        if (tags.hasKey(key)) {
            keyPair = tags.getIntArray(key);
            addProgress(tags, keyPair, increase);
            updateModTag(input, keyPair);
        } else {
            int mods = tags.getInteger("Modifiers");
            mods -= 1;
            tags.setInteger("Modifiers", mods);
            String modName = "\u00a74Redstone (" + increase + "/" + max + ")";
            int tooltipIndex = addToolTip(input, tooltipName, modName);
            keyPair = new int[] { increase, max, tooltipIndex };
            tags.setIntArray(key, keyPair);
        }

        int miningSpeed = tags.getInteger("MiningSpeed");
        int boost = 1;

        miningSpeed += (increase * boost);
        tags.setInteger("MiningSpeed", miningSpeed);
    }

    void updateModTag(ItemStack input, int[] keys) {
        NBTTagCompound tags = getModifierTag(input);
        String tip = "ModifierTip" + keys[2];
        String modName = "\u00a74Redstone (" + keys[0] + "/" + keys[1] + ")";
        tags.setString(tip, modName);
    }

    @Override
    public boolean validType(IModifyable type) {
        return type.getModifyType().equals("Accessory");
    }
}
