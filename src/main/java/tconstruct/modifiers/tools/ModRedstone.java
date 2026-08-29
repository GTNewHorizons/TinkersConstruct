package tconstruct.modifiers.tools;

import java.util.Arrays;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import tconstruct.library.modifier.IModifyable;
import tconstruct.library.modifier.ModificationInfo;
import tconstruct.library.tools.ToolCore;

public class ModRedstone extends ItemModTypeFilter {

    public String tooltipName;

    public ModRedstone(int effect, ItemStack[] items, int[] values) {
        super(effect, "Redstone", items, values);
        tooltipName = "\u00a74Haste";
        this.max = 50;
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
        int[] keyPair;
        ModificationInfo modificationInfo = matchingAmount(input, tool);
        int increase = modificationInfo.total();
        tags.setIntArray("ToRemove", modificationInfo.toRemove());

        int previous;
        if (tags.hasKey(key)) {
            keyPair = tags.getIntArray(key);
            previous = keyPair[0];
            addProgress(tags, keyPair, increase);
            updateModTag(tool, keyPair);
        } else {
            int modifiers = tags.getInteger("Modifiers");
            modifiers -= 1;
            tags.setInteger("Modifiers", modifiers);
            String modName = "\u00a74Redstone (" + increase + "/" + max + ")";
            int tooltipIndex = addToolTip(tool, tooltipName, modName);
            keyPair = new int[] { increase, max, tooltipIndex };
            previous = 0;
            tags.setIntArray(key, keyPair);
            // spillover: a first craft may open more tiers than the one it comes with
            if (settleTiers(tags, keyPair)) updateModTag(tool, keyPair);
        }

        int perPoint = 8;
        Item temp = tool.getItem();
        if (temp instanceof ToolCore) {
            ToolCore toolcore = (ToolCore) temp;
            if (toolcore.durabilityTypeHandle() == 2) perPoint += 2;
            if (toolcore.durabilityTypeAccessory() == 2) perPoint += 2;
            if (toolcore.durabilityTypeExtra() == 2) perPoint += 2;
        }

        // Each tier is worth 2 more speed per point than the one below it, so a craft whose points cross a tier
        // boundary has to pay for each point at its own tier's rate. Summing point by point keeps one bulk craft
        // worth exactly the same as the sequential crafts it replaces.
        int gained = 0;
        for (int point = previous + 1; point <= previous + increase; point++) {
            gained += perPoint + ((point - 1) / 50 * 2);
        }

        int miningSpeed = tags.getInteger("MiningSpeed");
        miningSpeed += gained;
        tags.setInteger("MiningSpeed", miningSpeed);

        String[] type = { "MiningSpeed2", "MiningSpeedHandle", "MiningSpeedExtra" };

        for (int i = 0; i < 3; i++) {
            if (tags.hasKey(type[i])) {
                int speed = tags.getInteger(type[i]);
                speed += gained;
                tags.setInteger(type[i], speed);
            }
        }
    }

    void updateModTag(ItemStack tool, int[] keys) {
        NBTTagCompound tags = tool.getTagCompound().getCompoundTag("InfiTool");
        String tip = "ModifierTip" + keys[2];
        String modName = "\u00a74Redstone (" + keys[0] + "/" + keys[1] + ")";
        tags.setString(tip, modName);
    }

    public boolean validType(IModifyable input) {
        return input.getModifyType().equals("Tool");
    }

    public boolean validType(ToolCore tool) {
        List<String> list = Arrays.asList(tool.getTraits());

        // handled by the windup modifier
        if (list.contains("windup")) return false;
        return list.contains("harvest") || list.contains("utility");
    }
}
