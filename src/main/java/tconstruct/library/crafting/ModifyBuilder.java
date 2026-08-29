package tconstruct.library.crafting;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;

import tconstruct.library.event.ModifyEvent;
import tconstruct.library.modifier.IModifyable;
import tconstruct.library.modifier.ItemModifier;

public class ModifyBuilder {

    public static ModifyBuilder instance = new ModifyBuilder();
    public List<ItemModifier> itemModifiers = new ArrayList<>();

    /**
     * Applies every modifier whose recipe can be satisfied by some subset of the input slots, so several different
     * modifiers can be applied in one craft (TiC 1.12 tool station behavior). Claiming runs in rounds: every modifier
     * bids the largest subset it matches and the biggest claim wins the round (registration order breaks ties), so a
     * multi-item recipe beats a single-item recipe sharing an ingredient (diamond + gold block stays the extra
     * modifier, not a Diamond upgrade plus a stray gold block), and a modifier that only becomes applicable mid-craft
     * (an extra-modifier item freeing a slot) still gets a later round. If a filled slot ends up feeding no modifier at
     * all, the whole craft is invalid.
     *
     * The output carries a "ToRemove" int array with one entry per filled input slot, in ascending slot order — the
     * order every crafting slot/container walks the inventory in when consuming ingredients.
     */
    public ItemStack modifyItem(ItemStack input, ItemStack[] modifiers) {
        ItemStack built = build(input, modifiers);
        if (built != null) return built;

        // With spillover on, a leveled modifier may take every free slot and leave a slot-hungry modifier
        // beside it with nothing to spend. Before giving anything up, try the one-tier-per-craft build.
        if (ItemModifier.isTierSpilloverOn()) {
            Boolean previousSpillover = ItemModifier.getTierSpillover();
            try {
                ItemModifier.setTierSpillover(false);
                built = build(input, modifiers);
            } finally {
                ItemModifier.setTierSpillover(previousSpillover);
            }
            if (built != null) return built;
        }

        // A modifier is allowed to spend one of the tool's free modifier slots to take an input it could not
        // otherwise use. That can leave a slot-hungry modifier in another slot with nothing to spend, which
        // fails the whole craft — so when the greedy attempt comes to nothing, try once more with no modifier
        // allowed to claim a slot for extra room. Whatever worked before this allowance existed still works.
        boolean previous = ItemModifier.isSlotSpendingAllowed();
        try {
            ItemModifier.setSlotSpendingAllowed(false);
            return build(input, modifiers);
        } finally {
            ItemModifier.setSlotSpendingAllowed(previous);
        }
    }

    private ItemStack build(ItemStack input, ItemStack[] modifiers) {
        ItemStack copy = input.copy(); // Prevent modifying the original
        if (!(copy.getItem() instanceof IModifyable item)) return null;
        // Subsets of the inputs are tracked as bits of an int. No station comes close to that many slots,
        // but refusing the craft beats quietly claiming a slot nobody bid on.
        if (modifiers.length > 31) return null;

        // Strip stale ToRemove tags that can cause extra amount of items being used or OOB crash
        copy.getTagCompound().getCompoundTag(item.getBaseTagName()).removeTag("ToRemove");

        // Working pool the modifiers claim from; consumption accumulates per slot
        ItemStack[] pool = new ItemStack[modifiers.length];
        int filled = 0;
        for (int i = 0; i < modifiers.length; i++) {
            if (modifiers[i] != null) {
                pool[i] = modifiers[i].copy();
                filled++;
            }
        }
        int[] consumed = new int[modifiers.length];
        boolean[] claimed = new boolean[modifiers.length];

        boolean built = false;
        // A slot feeds a given modifier at most once per craft (so a leveled modifier does not
        // restart on its own leftovers), tracked per modifier across all rounds. The count is read once:
        // a modifier registered while this craft is running simply waits for the next one.
        int registered = itemModifiers.size();
        int[] tried = new int[registered];
        while (true) {
            ItemModifier bestMod = null;
            int bestMask = 0;
            int bestIndex = -1;
            for (int m = 0; m < registered; m++) {
                ItemModifier mod = itemModifiers.get(m);
                if (!mod.validType(item)) continue;
                int mask = findLargestMatch(mod, pool, tried[m], copy);
                if (mask != 0 && Integer.bitCount(mask) > Integer.bitCount(bestMask)) {
                    bestMod = mod;
                    bestMask = mask;
                    bestIndex = m;
                }
            }
            if (bestMod == null) break;
            tried[bestIndex] |= bestMask;

            ModifyEvent event = new ModifyEvent(bestMod, item, copy);
            MinecraftForge.EVENT_BUS.post(event);
            if (event.isCanceled()) continue;

            built = true;
            bestMod.addMatchingEffect(copy); // Order matters here
            bestMod.modify(subArray(pool, bestMask), copy);

            // we do not allow negative modifiers >:(
            if (copy.getTagCompound().getCompoundTag(item.getBaseTagName()).getInteger("Modifiers") < 0) return null;

            claimConsumption(
                    copy.getTagCompound().getCompoundTag(item.getBaseTagName()),
                    pool,
                    bestMask,
                    consumed,
                    claimed);
        }
        if (!built) return null;

        for (int i = 0; i < modifiers.length; i++) {
            if (modifiers[i] != null && !claimed[i]) return null;
        }

        int[] toRemove = new int[filled];
        int index = 0;
        for (int i = 0; i < modifiers.length; i++) {
            if (modifiers[i] != null) toRemove[index++] = consumed[i];
        }
        copy.getTagCompound().getCompoundTag(item.getBaseTagName()).setIntArray("ToRemove", toRemove);
        return copy;
    }

    /**
     * Finds the largest subset of unclaimed pool slots this modifier matches, largest-first so a single application
     * sees everything it can use at once — for single-modifier inputs that reproduces the old full-array call exactly.
     * Returns a bitmask over pool indices, or 0 when nothing matches.
     */
    private int findLargestMatch(ItemModifier mod, ItemStack[] pool, int excludeMask, ItemStack tool) {
        int[] avail = new int[pool.length];
        int count = 0;
        for (int i = 0; i < pool.length; i++) {
            if (pool[i] != null && (excludeMask & (1 << i)) == 0) avail[count++] = i;
        }
        for (int size = count; size >= 1; size--) {
            for (int bits = 1; bits < (1 << count); bits++) {
                if (Integer.bitCount(bits) != size) continue;
                int mask = 0;
                for (int b = 0; b < count; b++) {
                    if ((bits & (1 << b)) != 0) mask |= 1 << avail[b];
                }
                if (mod.matches(subArray(pool, mask), tool)) return mask;
            }
        }
        return 0;
    }

    /** Copy of the pool with only the masked slots present, positions preserved. */
    private ItemStack[] subArray(ItemStack[] pool, int mask) {
        ItemStack[] result = new ItemStack[pool.length];
        for (int i = 0; i < pool.length; i++) {
            if ((mask & (1 << i)) != 0) result[i] = pool[i];
        }
        return result;
    }

    /**
     * Books the modifier's consumption against the claimed slots and clears its per-application ToRemove tag. The tag
     * is positional over the subset's filled slots in ascending order, defaulting to 1 item per slot when absent —
     * mirroring how the crafting slots consumed it before this class merged the bookkeeping.
     */
    private void claimConsumption(NBTTagCompound tags, ItemStack[] pool, int mask, int[] consumed, boolean[] claimed) {
        int[] toRemove = tags.hasKey("ToRemove") ? tags.getIntArray("ToRemove") : null;
        int index = 0;
        for (int i = 0; i < pool.length; i++) {
            if ((mask & (1 << i)) == 0) continue;
            int amount = 1;
            if (toRemove != null && index < toRemove.length) amount = toRemove[index];
            index++;
            claimed[i] = true;
            amount = Math.min(amount, pool[i].stackSize);
            consumed[i] += amount;
            // A slot feeds one modifier per craft: whatever is left over in it is not offered to the others,
            // so a stack of diamonds beside a gold block buys the extra modifier without a second diamond
            // quietly going into a durability upgrade nobody asked for.
            pool[i] = null;
        }
        tags.removeTag("ToRemove");
    }

    public static void registerModifier(ItemModifier mod) {
        if (mod == null) throw new NullPointerException("Modifier cannot be null.");
        instance.itemModifiers.add(mod);
    }
}
