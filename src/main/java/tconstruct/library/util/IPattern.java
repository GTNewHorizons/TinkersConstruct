package tconstruct.library.util;

import net.minecraft.item.ItemStack;

import tconstruct.library.crafting.PatternBuilder.MaterialSet;

public interface IPattern {

    int getPatternCost(ItemStack pattern);

    ItemStack getPatternOutput(ItemStack pattern, ItemStack input, MaterialSet set);
}
