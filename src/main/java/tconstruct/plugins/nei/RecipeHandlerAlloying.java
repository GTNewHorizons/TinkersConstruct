package tconstruct.plugins.nei;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.PositionedStack;
import tconstruct.library.crafting.AlloyMix;
import tconstruct.library.crafting.Smeltery;

public class RecipeHandlerAlloying extends RecipeHandlerBase {

    public static final Rectangle OUTPUT_TANK = new Rectangle(118, 9, 18, 32);

    public class CachedAlloyingRecipe extends CachedRecipe {

        private final List<PositionedStack> inputs;
        private final PositionedStack.Fluid output;

        public CachedAlloyingRecipe(AlloyMix recipe) {
            this.inputs = new ArrayList<>();

            int maxAmount = recipe.mixers.get(0).amount;
            for (FluidStack stack : recipe.mixers) {
                if (stack.amount > maxAmount) {
                    maxAmount = stack.amount;
                }
            }
            this.output = new PositionedStack.Fluid(
                    recipe.result,
                    OUTPUT_TANK.x,
                    OUTPUT_TANK.y,
                    OUTPUT_TANK.width,
                    OUTPUT_TANK.height,
                    maxAmount);

            int width = 36 / recipe.mixers.size();
            int counter = 0;
            for (FluidStack stack : recipe.mixers) {
                int tankWidth = counter == recipe.mixers.size() - 1 ? 36 - width * counter : width;
                this.inputs.add(new PositionedStack.Fluid(stack, 21 + width * counter, 9, tankWidth, 32, maxAmount));
                counter++;
            }
        }

        @Override
        public List<PositionedStack> getIngredients() {
            return this.inputs;
        }

        @Override
        public PositionedStack getResult() {
            return this.output;
        }
    }

    @Override
    public String getRecipeName() {
        return StatCollector.translateToLocal("tconstruct.nei.alloying");
    }

    @Override
    public String getRecipeID() {
        return "tconstruct.smeltery.alloying";
    }

    @Override
    public String getGuiTexture() {
        return "tinker:textures/gui/nei/smeltery.png";
    }

    @Override
    public void loadTransferRects() {
        this.transferRects.add(new RecipeTransferRect(new Rectangle(76, 21, 22, 15), this.getRecipeID()));
    }

    @Override
    public void drawBackground(int recipe) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GuiDraw.changeTexture(this.getGuiTexture());
        GuiDraw.drawTexturedModalRect(0, 0, 0, 62, 160, 65);
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (outputId.equals(this.getRecipeID())) {
            for (AlloyMix recipe : Smeltery.getAlloyList()) {
                if (!recipe.mixers.isEmpty()) {
                    this.arecipes.add(new CachedAlloyingRecipe(recipe));
                }
            }
        } else {
            super.loadCraftingRecipes(outputId, results);
        }
    }

    @Override
    public void loadCraftingRecipes(FluidStack result) {
        for (AlloyMix recipe : Smeltery.getAlloyList()) {
            if (areFluidsEqual(recipe.result, result) && !recipe.mixers.isEmpty()) {
                this.arecipes.add(new CachedAlloyingRecipe(recipe));
            }
        }
    }

    @Override
    public void loadUsageRecipes(FluidStack ingredient) {
        for (AlloyMix recipe : Smeltery.getAlloyList()) {
            for (FluidStack liquid : recipe.mixers) {
                if (areFluidsEqual(liquid, ingredient) && !recipe.mixers.isEmpty()) {
                    this.arecipes.add(new CachedAlloyingRecipe(recipe));
                    break;
                }
            }
        }
    }
}
