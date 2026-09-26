package tconstruct.api;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * Resolves the effective capacity used by container operations.
 */
public class ExtendedStackLimitHelper {

    public static boolean hasExtendedStackLimit(Slot slot) {
        return getProvider(slot) != null;
    }

    public static boolean hasExtendedStackLimit(Container container) {
        if (container == null) return false;

        for (Object object : container.inventorySlots) {
            if (object instanceof Slot slot && hasExtendedStackLimit(slot)) return true;
        }
        return false;
    }

    public static int getStackLimit(Slot slot, ItemStack stack) {
        if (slot == null || stack == null) return 0;

        IExtendedStackLimitProvider provider = getProvider(slot);
        if (provider != null) {
            return Math.max(0, provider.getExtendedStackLimit(slot.getSlotIndex(), stack));
        }

        return Math.max(0, Math.min(slot.getSlotStackLimit(), stack.getMaxStackSize()));
    }

    private static IExtendedStackLimitProvider getProvider(Slot slot) {
        if (slot instanceof IExtendedStackLimitProvider provider) return provider;
        if (slot.inventory instanceof IExtendedStackLimitProvider provider) return provider;
        return null;
    }
}
