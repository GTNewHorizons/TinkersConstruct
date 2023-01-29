package tconstruct.modifiers.tools;

import java.util.*;

import net.minecraft.enchantment.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;

import tconstruct.library.tools.ToolCore;

public class ModButtertouch extends ModBoolean {

    public ModButtertouch(ItemStack[] items, int effect) {
        super(items, effect, "Silk Touch", "\u00a7e", "Silky");
    }

    @Override
    protected boolean canModify(ItemStack tool, ItemStack[] input) {
        if (tool.getItem() instanceof ToolCore) {
            ToolCore toolItem = (ToolCore) tool.getItem();
            if (!validType(toolItem)) return false;

            NBTTagCompound tags = tool.getTagCompound().getCompoundTag("InfiTool");
            if (!tags.getBoolean("Lava") && !tags.hasKey("Lapis")) {
                return tags.getInteger("Modifiers") > 0 && !tags.getBoolean(key);
            }
        }
        return false;
    }

    @Override
    public void modify(ItemStack[] input, ItemStack tool) {
        NBTTagCompound tags = tool.getTagCompound().getCompoundTag("InfiTool");
        tags.setBoolean(key, true);
        addEnchantment(tool, Enchantment.silkTouch, 1);

        int modifiers = tags.getInteger("Modifiers");
        modifiers -= 1;
        tags.setInteger("Modifiers", modifiers);

        int attack = tags.getInteger("Attack");
        attack -= 3;
        if (attack < 0) attack = 0;
        tags.setInteger("Attack", attack);

        int miningSpeed = tags.getInteger("MiningSpeed");
        miningSpeed -= 300;
        if (miningSpeed < 0) miningSpeed = 0;
        tags.setInteger("MiningSpeed", miningSpeed);

        if (tags.hasKey("MiningSpeed2")) {
            int miningSpeed2 = tags.getInteger("MiningSpeed2");
            miningSpeed2 -= 300;
            if (miningSpeed2 < 0) miningSpeed2 = 0;
            tags.setInteger("MiningSpeed2", miningSpeed2);
        }

        addToolTip(tool, color + tooltipName, color + key);
    }

    public void addEnchantment(ItemStack tool, Enchantment enchant, int level) // TODO: Move this to ItemModifier
    {
        NBTTagList tags = new NBTTagList();
        Map<Integer, Integer> enchantMap = EnchantmentHelper.getEnchantments(tool);
        Iterator<Map.Entry<Integer, Integer>> iterator = enchantMap.entrySet().iterator();
        int index;
        int lvl;
        boolean hasEnchant = false;
        while (iterator.hasNext()) {
            NBTTagCompound enchantTag = new NBTTagCompound();
            final Map.Entry<Integer, Integer> next = iterator.next();
            index = next.getKey();
            lvl = next.getValue();
            if (index == enchant.effectId) {
                hasEnchant = true;
                enchantTag.setShort("id", (short) index);
                enchantTag.setShort("lvl", (byte) level);
            } else {
                enchantTag.setShort("id", (short) index);
                enchantTag.setShort("lvl", (byte) lvl);
            }
            tags.appendTag(enchantTag);
        }
        if (!hasEnchant) {
            NBTTagCompound enchantTag = new NBTTagCompound();
            enchantTag.setShort("id", (short) enchant.effectId);
            enchantTag.setShort("lvl", (byte) level);
            tags.appendTag(enchantTag);
        }
        tool.stackTagCompound.setTag("ench", tags);
    }

    public boolean validType(ToolCore tool) {
        List<String> list = Arrays.asList(tool.getTraits());
        return list.contains("weapon") || list.contains("harvest");
    }
}
