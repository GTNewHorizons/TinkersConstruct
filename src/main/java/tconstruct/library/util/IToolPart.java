package tconstruct.library.util;

import net.minecraft.item.ItemStack;

public interface IToolPart {

    /**
     * Parts to build tools from. ex: Pickaxe Head
     *
     * @param stack This item
     * @return Proper material ID. -1 for invalid
     */
    int getMaterialID(ItemStack stack);
}
