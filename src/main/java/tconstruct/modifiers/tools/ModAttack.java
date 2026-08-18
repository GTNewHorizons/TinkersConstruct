package tconstruct.modifiers.tools;

import java.util.Arrays;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import tconstruct.library.modifier.IModifyable;
import tconstruct.library.modifier.ModificationInfo;
import tconstruct.library.tools.ToolCore;

public class ModAttack extends ItemModTypeFilter {

    String tooltipName;
    int threshold;
    String guiType;
    String modifierType;
    boolean ammoOnly;

    // Regular weapons
    public ModAttack(String type, int effect, ItemStack[] items, int[] value) {
        super(effect, "ModAttack", items, value);
        tooltipName = "\u00a7fSharpness";
        guiType = type;
        this.max = 72;
        threshold = 24;
        modifierType = "Tool";
        ammoOnly = false;
    }

    // projectiles
    public ModAttack(String type, int effect, ItemStack[] items, int[] value, boolean ammoOnly) {
        super(effect, "ModAttack", items, value);
        tooltipName = "\u00a7fSharpness";
        guiType = type;
        this.max = 48;
        threshold = 24;
        modifierType = "Tool";
        this.ammoOnly = ammoOnly;
    }

    // gloves
    public ModAttack(String type, int effect, ItemStack[] items, int[] value, int max, int threshold,
            String modifierType) {
        super(effect, "ModAttack", items, value);
        tooltipName = "\u00a7fKnuckles";
        guiType = type;
        this.max = max;
        this.threshold = threshold;
        this.modifierType = modifierType;
        ammoOnly = false;
    }

    @Override
    protected boolean canModify(ItemStack tool, ItemStack[] input) {
        if (tool.getItem() instanceof ToolCore) {
            if (Arrays.asList(((ToolCore) tool.getItem()).getTraits()).contains("ammo") != ammoOnly) return false;
        }

        if (tool.getItem() instanceof IModifyable) {
            IModifyable toolItem = (IModifyable) tool.getItem();
            if (!validType(toolItem)) return false;

            return hasCapacityFor(input, tool);
        }
        return false;
    }

    @Override
    public boolean validType(IModifyable input) {
        String type = input.getModifyType();
        return type.equals(modifierType);
    }

    @Override
    public void modify(ItemStack[] input, ItemStack tool) {
        IModifyable toolItem = (IModifyable) tool.getItem();
        NBTTagCompound tags = tool.getTagCompound().getCompoundTag(toolItem.getBaseTagName());

        ModificationInfo modificationInfo = matchingAmount(input, tool);
        int increase = modificationInfo.total();
        tags.setIntArray("ToRemove", modificationInfo.toRemove());

        if (tags.hasKey(key)) {
            int[] keyPair = tags.getIntArray(key);
            int previous = keyPair[0];
            addProgress(tags, keyPair, increase);
            addAttack(tags, previous, increase);
            updateModTag(tool, keyPair);

        } else {
            int modifiers = tags.getInteger("Modifiers");
            modifiers -= 1;
            tags.setInteger("Modifiers", modifiers);
            String modName = "\u00a7f" + guiType + " (" + increase + "/" + max + ")";
            int tooltipIndex = addToolTip(tool, tooltipName, modName);
            int[] keyPair = new int[] { increase, max, tooltipIndex };
            tags.setIntArray(key, keyPair);

            int attack = tags.getInteger("Attack");
            attack += 1 + increase / threshold;
            tags.setInteger("Attack", attack);
        }
    }

    /**
     * Damage steps up once per threshold of progress, so a later increase is worth the thresholds it carries the tool
     * past; the first application additionally counts one step outright (mDiyo's flat +1, restored upstream in #323),
     * so a tool holding K points carries 1 + K/threshold bonus attack however the quartz was batched.
     */
    private void addAttack(NBTTagCompound tags, int previous, int increase) {
        int steps = (previous + increase) / threshold - previous / threshold;
        if (steps > 0) tags.setInteger("Attack", tags.getInteger("Attack") + steps);
    }

    void updateModTag(ItemStack tool, int[] keys) {
        NBTTagCompound tags = tool.getTagCompound().getCompoundTag(getTagName(tool));
        String tip = "ModifierTip" + keys[2];
        String modName = "\u00a7f" + guiType + " (" + keys[0] + "/" + keys[1] + ")";
        tags.setString(tip, modName);
    }
}
