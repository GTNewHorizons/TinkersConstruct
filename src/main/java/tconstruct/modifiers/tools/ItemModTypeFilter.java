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
        int inTier = remainingInTier(tool, modifierMax);
        int spillover = spilloverCapacity(tool, modifierMax);
        ModificationInfo info = matchWithin(input, inTier + spillover);
        if (spillover > 0) return info;

        // Items are consumed whole, so an input can be too coarse to make any progress at all — a 9-point
        // redstone block on a tool sitting 5 points below its ceiling used to be refused outright, leaving the
        // tool stuck there. That is where TiC 1.12 lets progress run past the ceiling, at the price of one of
        // the tool's free modifier slots. Anything that does fit stays inside the tier, so topping a modifier
        // off from a stack still stops at the ceiling instead of spending a slot unasked.
        if (info.total() <= 0 && canOpenTier(tool)) {
            info = matchWithin(input, inTier + modifierMax);
        }
        return info;
    }

    private ModificationInfo matchWithin(ItemStack[] input, int availableAmount) {
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
     * Points the modifier can still take without opening a new tier. Progress is stored as {current, ceiling,
     * tooltipIndex}; modifiers holding a flat pool instead of tiers ({current, tooltipIndex}, i.e. Lapis) keep their
     * single cap.
     */
    protected int remainingInTier(ItemStack tool, int modifierMax) {
        NBTTagCompound tags = getModifierTag(tool);
        if (!tags.hasKey(key)) return modifierMax;

        int[] keyPair = tags.getIntArray(key);
        if (keyPair.length == 2) return Math.max(0, modifierMax - keyPair[0]);
        return Math.max(0, keyPair[1] - keyPair[0]);
    }

    /**
     * Extra points on offer when the "Modifier tier spillover" option is on: every free modifier slot is another tier
     * the craft may open. Off by default — then a craft opens at most one tier, as in TiC 1.12 — and never offered on
     * the builder's thrifty retry, since spending slots is exactly what that pass holds back.
     */
    protected int spilloverCapacity(ItemStack tool, int modifierMax) {
        if (!isTierSpilloverOn() || !isSlotSpendingAllowed() || isFlatPool()) return 0;

        NBTTagCompound tags = getModifierTag(tool);
        int free = tags.getInteger("Modifiers");
        // a first application pays one slot for the tier it comes with; further tiers cost one each
        if (!tags.hasKey(key)) return Math.max(0, free - 1) * modifierMax;
        return Math.max(0, free) * modifierMax;
    }

    /** A modifier holding one flat pool for the tool's life ({current, tooltipIndex}) has no tiers to spill into. */
    protected boolean isFlatPool() {
        return false;
    }

    /**
     * Whether a free modifier slot is available to pay for the next tier. Only one tier is ever opened per craft, as in
     * TiC 1.12 (ModifierAspect.LevelAspect: "only 1 level per application").
     */
    protected boolean canOpenTier(ItemStack tool) {
        // The thrifty pass only takes back the room this class added — a modifier standing exactly on its
        // ceiling has always been able to open the next one, and crafts depend on it doing so.
        if (!isSlotSpendingAllowed() && remainingInTier(tool, max) > 0) return false;

        NBTTagCompound tags = getModifierTag(tool);
        if (!tags.hasKey(key)) return false; // the first application already comes with a whole tier
        return tags.getIntArray(key).length > 2 && tags.getInteger("Modifiers") > 0;
    }

    /**
     * Whether the inputs make progress the tool can actually accept: something matches, and it either fits inside the
     * current tier or the tool has a free modifier slot to open the next one.
     */
    protected boolean hasCapacityFor(ItemStack[] input, ItemStack tool) {
        NBTTagCompound tags = getModifierTag(tool);
        int total = matchingAmount(input, tool).total();
        // Nothing to add: normally not worth a craft, but on the thrifty pass an input the modifier cannot use
        // still has to be claimed, or the builder throws out a craft whose other modifiers were fine. Only room
        // the modifier genuinely has left counts, so a modifier sitting on its ceiling still turns the input down.
        if (total <= 0) return !isSlotSpendingAllowed() && tags.hasKey(key) && remainingInTier(tool, max) > 0;
        if (!tags.hasKey(key)) return tags.getInteger("Modifiers") > 0;

        int[] keyPair = tags.getIntArray(key);
        if (keyPair.length == 2) return true; // flat pool, already bounded by its own cap
        return keyPair[0] + total <= keyPair[1] || tags.getInteger("Modifiers") > 0;
    }

    /**
     * Books the increase against the modifier's progress, opening one new tier if the increase crosses the ceiling.
     * Writes the updated pair back to the tag.
     */
    protected void addProgress(NBTTagCompound tags, int[] keyPair, int increase) {
        keyPair[0] += increase;
        settleTiers(tags, keyPair);
        tags.setIntArray(key, keyPair);
    }

    /**
     * Opens a tier for every ceiling the progress has passed, one free modifier slot each. Capacity is bounded so this
     * runs at most once per craft unless spillover is on. Returns whether any tier was opened.
     */
    protected boolean settleTiers(NBTTagCompound tags, int[] keyPair) {
        // One tier unless spillover is in effect, then as many as the free slots can pay for — the same bound
        // capacity was computed under, so a corrupt pair sitting past its ceiling cannot run away here.
        int limit = 1;
        if (isTierSpilloverOn() && isSlotSpendingAllowed()) limit = Math.max(1, tags.getInteger("Modifiers"));
        boolean opened = false;
        while (keyPair[0] > keyPair[1] && limit-- > 0) {
            keyPair[1] += max;
            tags.setInteger("Modifiers", tags.getInteger("Modifiers") - 1);
            opened = true;
        }
        return opened;
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
