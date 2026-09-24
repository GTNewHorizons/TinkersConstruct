package tconstruct.api;

import net.minecraft.item.ItemStack;

/**
 * Provides an item-aware stack capacity for a slot or its backing inventory.
 *
 * <p>
 * The returned value is the complete capacity for the supplied item in the supplied slot. Implementations may return a
 * value larger than the item's normal stack size when the inventory supports extended stacks. The backing inventory
 * must preserve values up to the returned capacity when a stack is written back.
 * </p>
 */
public interface IExtendedStackLimitProvider {

    int getExtendedStackLimit(int slotIndex, ItemStack stack);
}
