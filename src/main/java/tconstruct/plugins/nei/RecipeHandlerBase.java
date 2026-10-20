package tconstruct.plugins.nei;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.IFluidContainerItem;

import codechicken.nei.recipe.TemplateRecipeHandler;

public abstract class RecipeHandlerBase extends TemplateRecipeHandler {

    public abstract String getRecipeID();

    public void loadCraftingRecipes(FluidStack result) {}

    public void loadUsageRecipes(FluidStack ingredient) {}

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        try {
            if (outputId.equals("liquid") && results.length == 1 && results[0] instanceof FluidStack) {
                this.loadCraftingRecipes((FluidStack) results[0]);
            } else {
                super.loadCraftingRecipes(outputId, results);
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        FluidStack fluid = getFluidStack(result);
        if (fluid != null) {
            this.loadCraftingRecipes(fluid);
        }
    }

    @Override
    public void loadUsageRecipes(String inputId, Object... ingredients) {
        try {
            if (inputId.equals("liquid") && ingredients.length == 1 && ingredients[0] instanceof FluidStack) {
                this.loadUsageRecipes((FluidStack) ingredients[0]);
            } else {
                super.loadUsageRecipes(inputId, ingredients);
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingred) {
        FluidStack fluid = getFluidStack(ingred);
        if (fluid != null) {
            this.loadUsageRecipes(fluid);
        }
    }

    public static List getSingleList(Object o) {
        List list = new ArrayList();
        list.add(o);
        return list;
    }

    public static FluidStack getFluidStack(ItemStack stack) {
        if (stack == null) {
            return null;
        }
        Item item = stack.getItem();
        FluidStack fluidStack = null;
        if ((item instanceof IFluidContainerItem)) {
            fluidStack = ((IFluidContainerItem) item).getFluid(stack);
        }
        if (fluidStack == null) {
            fluidStack = FluidContainerRegistry.getFluidForFilledItem(stack);
        }
        if ((fluidStack == null) && (Block.getBlockFromItem(stack.getItem()) instanceof IFluidBlock)) {
            Fluid fluid = ((IFluidBlock) Block.getBlockFromItem(stack.getItem())).getFluid();
            if (fluid != null) {
                return new FluidStack(fluid, 1000);
            }
        }
        return fluidStack;
    }

    public static boolean areFluidsEqual(FluidStack fluidStack1, FluidStack fluidStack2) {
        if (fluidStack1 == null || fluidStack2 == null) {
            return false;
        }
        return fluidStack1.isFluidEqual(fluidStack2);
    }
}
