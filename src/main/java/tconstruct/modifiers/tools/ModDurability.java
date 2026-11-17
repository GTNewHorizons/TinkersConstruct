package tconstruct.modifiers.tools;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import tconstruct.library.crafting.ToolBuilder;
import tconstruct.library.modifier.ItemModifier;
import tconstruct.util.config.PHConstruct;

/* Adds an integer NBTTag */

public class ModDurability extends ItemModifier {

    String tooltipName;
    String color;
    int durability;
    float modifier;
    int miningLevel;

    public ModDurability(ItemStack[] items, int effect, int dur, float mod, int level, String k, String tip, String c) {
        super(items, effect, k);
        durability = dur;
        modifier = mod;
        miningLevel = level;
        tooltipName = tip;
        color = c;
    }

    @Override
    protected boolean canModify(ItemStack tool, ItemStack[] input) {
        NBTTagCompound tags = tool.getTagCompound().getCompoundTag("InfiTool");
        if (tags.hasKey(key)) return false;
        return super.canModify(tool, input);
    }

    @Override
    public void modify(ItemStack[] input, ItemStack tool) {
        NBTTagCompound tags = tool.getTagCompound().getCompoundTag("InfiTool");

        int base = tags.getInteger("BaseDurability");
        long bonus = tags.getInteger("BonusDurability");
        float modDur = tags.getFloat("ModDurability");

        bonus += durability;
        modDur += modifier;
        tags.setBoolean("Broken", false);

        // long: base + bonus can exceed Integer.MAX_VALUE, and a wrapped sum would trip the <= 0 check below.
        // The multiply is still done in float, so a tool that doesn't overflow gets exactly the value it got before.
        long total = (long) ((base + bonus) * (modDur + 1f));
        if (total <= 0) total = 1;

        tags.setInteger("TotalDurability", ToolBuilder.clampDurability(total));
        tags.setInteger("BonusDurability", ToolBuilder.clampDurability(bonus));
        tags.setFloat("ModDurability", modDur);

        if (PHConstruct.miningLevelIncrease) {
            int mLevel = tags.getInteger("HarvestLevel");
            if (mLevel < miningLevel) tags.setInteger("HarvestLevel", miningLevel);
        }

        int modifiers = tags.getInteger("Modifiers");
        modifiers -= 1;
        tags.setInteger("Modifiers", modifiers);

        tags.setBoolean(key, true);
        String modTip = color + key;
        addToolTip(tool, tooltipName, modTip);
    }
}
