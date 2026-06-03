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

        NBTTagCompound tags = tool.getTagCompound().getCompoundTag("InfiTool");
        int availableAmount;
        if (tags.hasKey(key)) {
            int[] keyPair = tags.getIntArray(key);
            if (keyPair[0] % modifierMax == 0) {
                availableAmount = modifierMax;
            } else {
                // Blame Lapis modifier
                int upperLimit = keyPair.length == 2 ? modifierMax : keyPair[1];
                availableAmount = upperLimit - keyPair[0];
            }
        } else {
            availableAmount = modifierMax;
        }
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
