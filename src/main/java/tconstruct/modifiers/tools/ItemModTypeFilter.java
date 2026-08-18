package tconstruct.modifiers.tools;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import tconstruct.library.modifier.ItemModifier;
import tconstruct.library.modifier.ModificationInfo;

public abstract class ItemModTypeFilter extends ItemModifier {

    public final List<Integer> increase;
    protected int max;

    public ItemModTypeFilter(int effect, String dataKey, ItemStack[] items, int[] values) {
        super(items, effect, dataKey);
        assert items.length == values.length : "Itemstacks and their values for tool modifiers must be the same length";
        this.increase = new ArrayList<>();
        for (int value : values) {
            increase.add(value);
        }
    }

    /**
     * Checks to see if the inputs match the stored items Note: Filters types, doesn't care about amount
     *
     * @param input The ItemStacks to compare against
     * @param tool  Item to modify, used for restrictions
     * @return Whether the recipe matches the input
     */
    @Override
    public boolean matches(ItemStack[] input, ItemStack tool) {
        if (!canModify(tool, input)) return false;

        boolean minimumMatch = false;
        for (ItemStack inputStack : input) {
            if (inputStack == null) continue;

            boolean match = false;
            for (Object check : stacks) {
                ItemStack stack = (ItemStack) check;
                if (stack.getItemDamage() == Short.MAX_VALUE) {
                    if (this.areItemsEquivalent(inputStack, stack)) match = true;
                } else {
                    if (this.areItemStacksEquivalent(inputStack, stack)) match = true;
                }
            }
            if (!match) return false;

            minimumMatch = true;
        }
        return minimumMatch;
    }

    // Needed because Lapis modifier does not put its max in the keyPair
    public ModificationInfo matchingAmount(ItemStack[] input, ItemStack tool) {
        return matchingAmount(input, tool, max);
    }

    public ModificationInfo matchingAmount(ItemStack[] input, ItemStack tool, int modifierMax) {

        int availableAmount = availableAmount(tool, modifierMax);
        int amount = 0;

        ArrayList<Integer> toRemove = new ArrayList<>();

        for (ItemStack inputStack : input) {
            if (inputStack == null) {
                continue;
            }
            for (int iter = 0; iter < stacks.size(); iter++) {
                ItemStack stack = stacks.get(iter);
                int perItemIncrease = increase.get(iter);
                int maxItems = availableAmount / perItemIncrease;
                int itemsUsed;
                int usedAmount = 0;

                if (stack.getItemDamage() == Short.MAX_VALUE) {

                    if (this.areItemsEquivalent(inputStack, stack)) {
                        itemsUsed = Math.min(maxItems, inputStack.stackSize);
                        usedAmount = perItemIncrease * itemsUsed;
                        amount += usedAmount;
                        availableAmount -= usedAmount;
                        toRemove.add(itemsUsed);
                    }
                } else {
                    if (this.areItemStacksEquivalent(inputStack, stack)) {
                        itemsUsed = Math.min(maxItems, inputStack.stackSize);
                        usedAmount = perItemIncrease * itemsUsed;
                        amount += usedAmount;
                        availableAmount -= usedAmount;
                        toRemove.add(itemsUsed);
                    }
                }
            }
        }
        int[] toRemoveArray = new int[toRemove.size()];
        for (int i = 0; i < toRemove.size(); i++) {
            toRemoveArray[i] = toRemove.get(i);
        }
        return new ModificationInfo(amount, toRemoveArray);
    }

    /**
     * How many points this craft may still add to the modifier.
     *
     * Progress is stored as {current, ceiling, tooltipIndex}; filling the ceiling opens another tier at the cost of one
     * of the tool's free modifier slots. Points flow past one ceiling within a single craft but never past two, which
     * is what TiC 1.12 does (ModifierAspect.LevelAspect: "only 1 level per application"). That lets a 9-point redstone
     * block land on a 45/50 tool instead of being refused, without letting one click silently spend every free modifier
     * slot.
     *
     * Modifiers holding a flat pool instead of tiers ({current, tooltipIndex}, i.e. Lapis) keep their single cap.
     */
    protected int availableAmount(ItemStack tool, int modifierMax) {
        NBTTagCompound tags = getModifierTag(tool);
        if (!tags.hasKey(key)) return modifierMax;

        int[] keyPair = tags.getIntArray(key);
        if (keyPair.length == 2) return Math.max(0, modifierMax - keyPair[0]);

        int remaining = Math.max(0, keyPair[1] - keyPair[0]);
        return tags.getInteger("Modifiers") > 0 ? remaining + modifierMax : remaining;
    }

    /**
     * Whether the inputs make any progress that the tool can actually accept: something matches, and it either fits
     * inside the current tier or the tool has a free modifier slot to open the next one.
     */
    protected boolean hasCapacityFor(ItemStack[] input, ItemStack tool) {
        NBTTagCompound tags = getModifierTag(tool);
        if (matchingAmount(input, tool).total() <= 0) return false;
        if (!tags.hasKey(key)) return tags.getInteger("Modifiers") > 0;

        int[] keyPair = tags.getIntArray(key);
        if (keyPair.length == 2) return true; // flat pool, already bounded by availableAmount
        return keyPair[0] + matchingAmount(input, tool).total() <= keyPair[1] || tags.getInteger("Modifiers") > 0;
    }

    /**
     * Books the increase against the modifier's progress, opening one new tier if the increase crosses the ceiling.
     * Writes the updated pair back to the tag.
     */
    protected void addProgress(NBTTagCompound tags, int[] keyPair, int increase) {
        keyPair[0] += increase;
        if (keyPair[0] > keyPair[1]) {
            keyPair[1] += max;
            tags.setInteger("Modifiers", tags.getInteger("Modifiers") - 1);
        }
        tags.setIntArray(key, keyPair);
    }

    /**
     * Adds a new itemstack to the list for increases
     *
     * @param stack  ItemStack to compare against
     * @param amount Amount to increase
     */
    public void addStackToMatchList(ItemStack stack, int amount) {
        if (stack == null) throw new NullPointerException(
                "ItemStack added to " + this.getClass().getSimpleName() + " cannot be null.");
        stacks.add(stack);
        increase.add(amount);
    }
}
