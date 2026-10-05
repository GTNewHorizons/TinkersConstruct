package tconstruct.modifiers.tools;

import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import tconstruct.library.modifier.ModificationInfo;
import tconstruct.library.tools.ToolCore;

public class ModPiston extends ItemModTypeFilter {

    private static final String LINE = "\u00a74Knockback (", GRAY_LINE = "\u00a77Knockback (";

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

            if (matchingAmount(input, tool).total() > max) return false;

            NBTTagCompound tags = tool.getTagCompound().getCompoundTag("InfiTool");
            if (!tags.hasKey(key))
                return tags.getInteger("Modifiers") > 0 && matchingAmount(input, tool).total() <= max;

            int[] keyPair = tags.getIntArray(key);
            if (keyPair[0] + matchingAmount(input, tool).total() <= keyPair[1]) return true;
            else if (keyPair[0] == keyPair[1]) return tags.getInteger("Modifiers") > 0;
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
            if (keyPair[0] % max == 0) {
                keyPair[0] += increase;
                keyPair[1] += max;
                tags.setIntArray(key, keyPair);

                int modifiers = tags.getInteger("Modifiers");
                modifiers -= 1;
                tags.setInteger("Modifiers", modifiers);
            } else {
                keyPair[0] += increase;
                tags.setIntArray(key, keyPair);
            }
            updateModTag(tool, keyPair);
        } else {
            int modifiers = tags.getInteger("Modifiers");
            modifiers -= 1;
            tags.setInteger("Modifiers", modifiers);
            String modName = LINE + increase + "/" + max + ")";
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
        String modName = LINE + keys[0] + "/" + keys[1] + ")";
        tags.setString(tip, modName);
    }

    /** A tool's saved Knockback line from before every piston wrote it red is repainted red. */
    public static void repaintLine(NBTTagCompound tags) {
        int[] keys = tags.getIntArray("Piston");
        if (keys.length < 3) return;
        String tip = "ModifierTip" + keys[2];
        String line = tags.getString(tip);
        if (line.startsWith(GRAY_LINE)) tags.setString(tip, LINE + line.substring(GRAY_LINE.length()));
    }

    public boolean validType(ToolCore tool) {
        List<String> list = Arrays.asList(tool.getTraits());
        return list.contains("weapon") || list.contains("ammo");
    }
}
