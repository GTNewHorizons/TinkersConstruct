package tconstruct.tools.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import tconstruct.library.crafting.StencilBuilder;

/**
 * Input slot for the stencil table. Only accepts blank patterns.
 */
public class SlotBlankPattern extends Slot {

    public SlotBlankPattern(IInventory builder, int par3, int par4, int par5) {
        super(builder, par3, par4, par5);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return stack != null && StencilBuilder.isBlank(stack);
    }
}
