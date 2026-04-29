package tconstruct.plugins.nei;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import mantle.utils.ItemMetaWrapper;
import tconstruct.library.crafting.Smeltery;

public class RecipeHandlerMelting extends RecipeHandlerBase {

    public static final Rectangle MOLTEN_TANK = new Rectangle(115, 20, 18, 18);

    public class CachedMeltingRecipe extends CachedBaseRecipe {

        private final PositionedStack input;
        private final int temperature;
        private final FluidTankElement output;

        public CachedMeltingRecipe(ItemStack input) {
            this.input = new PositionedStack(input, 28, 21);
            this.temperature = Smeltery.getLiquifyTemperature(input);
            this.output = new FluidTankElement(MOLTEN_TANK, 1, Smeltery.getSmelteryResult(input));
            this.output.capacity = this.output.fluid != null ? this.output.fluid.amount : 1000;
        }

        public CachedMeltingRecipe(List<ItemStack> input) {
            this.input = new PositionedStack(input, 28, 21);
            this.temperature = Smeltery.getLiquifyTemperature(input.get(0));
            this.output = new FluidTankElement(MOLTEN_TANK, 1, Smeltery.getSmelteryResult(input.get(0)));
            this.output.capacity = this.output.fluid != null ? this.output.fluid.amount : 1000;
        }

        @Override
        public PositionedStack getIngredient() {
            return this.input;
        }

        @Override
        public PositionedStack getResult() {
            return null;
        }

        @Override
        public List<FluidTankElement> getFluidTanks() {
            List<FluidTankElement> tanks = new ArrayList<>();
            tanks.add(this.output);
            return tanks;
        }
    }

    @Override
    public String getRecipeName() {
        return StatCollector.translateToLocal("tconstruct.nei.melting");
    }

    @Override
    public String getRecipeID() {
        return "tconstruct.smeltery.melting";
    }

    @Override
    public String getGuiTexture() {
        return "tinker:textures/gui/nei/smeltery.png";
    }

    @Override
    public void loadTransferRects() {
        this.transferRects.add(new RecipeTransferRect(new Rectangle(72, 20, 16, 34), this.getRecipeID()));
    }

    @Override
    public void drawBackground(int recipe) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GuiDraw.changeTexture(this.getGuiTexture());
        GuiDraw.drawTexturedModalRect(0, 0, 0, 0, 160, 55);
    }

    @Override
    public void drawExtras(int recipe) {
        int temperature = ((CachedMeltingRecipe) this.arecipes.get(recipe)).temperature;
        GuiDraw.drawStringC(temperature + " C", 81, 9, 0x808080, false);
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (outputId.equals(getRecipeID())) {
            IntSet processedGroups = new IntOpenHashSet();
            for (ItemMetaWrapper key : Smeltery.getSmeltingList().keySet()) {
                loadFromWrapper(key, processedGroups);
            }
        } else {
            super.loadCraftingRecipes(outputId, results);
        }
    }

    @Override
    public void loadCraftingRecipes(FluidStack result) {
        IntSet processedGroups = new IntOpenHashSet();
        for (Entry<ItemMetaWrapper, FluidStack> pair : Smeltery.getSmeltingList().entrySet()) {
            if (areFluidsEqual(pair.getValue(), result)) {
                loadFromWrapper(pair.getKey(), processedGroups);
            }
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingred) {
        IntSet processedGroups = new IntOpenHashSet();
        for (ItemMetaWrapper key : Smeltery.getSmeltingList().keySet()) {
            if (NEIServerUtils.areStacksSameTypeCrafting(new ItemStack(key.item, 1, key.meta), ingred)) {
                loadFromWrapper(key, processedGroups);
            }
        }
    }

    private void loadFromWrapper(ItemMetaWrapper wrapper, IntSet processedGroups) {
        int smeltingGroup = Smeltery.getSmeltingGroup(wrapper);
        if (smeltingGroup != -1) {
            if (processedGroups.add(smeltingGroup)) {
                this.arecipes.add(new CachedMeltingRecipe(Smeltery.getSmeltingGroupItems(smeltingGroup)));
            }
        } else {
            this.arecipes.add(new CachedMeltingRecipe(new ItemStack(wrapper.item, 1, wrapper.meta)));
        }
    }
}
