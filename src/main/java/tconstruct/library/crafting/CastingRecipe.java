package tconstruct.library.crafting;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

import tconstruct.library.client.FluidRenderProperties;

public class CastingRecipe {

    public ItemStack output;
    public FluidStack castingMetal;
    public ItemStack cast;
    public boolean consumeCast;
    public int coolTime;
    public FluidRenderProperties fluidRenderProperties;
    public boolean ignoreNBT;

    public CastingRecipe(ItemStack replacement, FluidStack metal, ItemStack cast, boolean consume, int delay,
            FluidRenderProperties props, boolean ignoreNBT) {
        castingMetal = metal;
        this.cast = cast;
        output = replacement;
        consumeCast = consume;
        coolTime = delay;
        fluidRenderProperties = props;
        this.ignoreNBT = ignoreNBT;
    }

    public CastingRecipe(ItemStack replacement, FluidStack metal, ItemStack cast, boolean consume, int delay,
            FluidRenderProperties props) {
        this(replacement, metal, cast, consume, delay, props, false);
    }

    public boolean matches(@Nullable FluidStack metal, @Nullable ItemStack inputCast) {
        if (metal != null && castingMetal.isFluidEqual(metal)) {
            if (inputCast != null && cast != null
                    && cast.getItemDamage() == OreDictionary.WILDCARD_VALUE
                    && inputCast.getItem() == cast.getItem()) {
                return true;
            } else if (!ignoreNBT && ItemStack.areItemStacksEqual(cast, inputCast)) {
                return true;
            } else return ignoreNBT && inputCast != null && cast != null && cast.isItemEqual(inputCast);
        }
        return false;
    }

    public ItemStack getResult() {
        return output.copy();
    }
}
