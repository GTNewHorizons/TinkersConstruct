package tconstruct.plugins.nei;

import static com.gtnewhorizon.gtnhlib.util.numberformatting.NumberFormatUtil.formatNumber;

import java.awt.Rectangle;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import mantle.utils.ItemMetaWrapper;
import tconstruct.library.crafting.Smeltery;
import tconstruct.library.util.ColorUtils;

public class RecipeHandlerMelting extends RecipeHandlerBase {

    public static final Rectangle MOLTEN_TANK = new Rectangle(115, 20, 18, 18);

    public class CachedMeltingRecipe extends CachedRecipe {

        private final PositionedStack input;
        private final int temperature;
        private final PositionedStack.Fluid output;

        public CachedMeltingRecipe(ItemStack input) {
            this(input, input);
        }

        public CachedMeltingRecipe(List<ItemStack> input) {
            this(input, input.get(0));
        }

        private CachedMeltingRecipe(Object input, ItemStack first) {
            this.input = new PositionedStack(input, 28, 21);
            this.temperature = Smeltery.getLiquifyTemperature(first);
            FluidStack result = Smeltery.getSmelteryResult(first);
            this.output = result != null
                    ? new PositionedStack.Fluid(
                            result,
                            MOLTEN_TANK.x,
                            MOLTEN_TANK.y,
                            MOLTEN_TANK.width,
                            MOLTEN_TANK.height,
                            0)
                    : null;
        }

        @Override
        public PositionedStack getIngredient() {
            return this.input;
        }

        @Override
        public PositionedStack getResult() {
            return this.output;
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
        GuiDraw.drawStringC(formatNumber(temperature) + " C", 81, 9, ColorUtils.neiTemperature.getColor(), false);
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (outputId.equals(getRecipeID())) {
            ObjectSet<String> processedGroups = new ObjectOpenHashSet<>();
            for (ItemMetaWrapper key : Smeltery.getSmeltingList().keySet()) {
                loadFromWrapper(key, processedGroups);
            }
        } else {
            super.loadCraftingRecipes(outputId, results);
        }
    }

    @Override
    public void loadCraftingRecipes(FluidStack result) {
        ObjectSet<String> processedGroups = new ObjectOpenHashSet<>();
        for (Entry<ItemMetaWrapper, FluidStack> pair : Smeltery.getSmeltingList().entrySet()) {
            if (areFluidsEqual(pair.getValue(), result)) {
                loadFromWrapper(pair.getKey(), processedGroups);
            }
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingred) {
        ObjectSet<String> processedGroups = new ObjectOpenHashSet<>();
        for (ItemMetaWrapper key : Smeltery.getSmeltingList().keySet()) {
            if (NEIServerUtils.areStacksSameTypeCrafting(new ItemStack(key.item, 1, key.meta), ingred)) {
                loadFromWrapper(key, processedGroups);
            }
        }
    }

    private void loadFromWrapper(ItemMetaWrapper wrapper, ObjectSet<String> processedGroups) {
        String smeltingGroup = Smeltery.getSmeltingGroup(wrapper);
        if (smeltingGroup != null) {
            if (processedGroups.add(smeltingGroup)) {
                this.arecipes.add(new CachedMeltingRecipe(Smeltery.getSmeltingGroupItems(smeltingGroup)));
            }
        } else {
            this.arecipes.add(new CachedMeltingRecipe(new ItemStack(wrapper.item, 1, wrapper.meta)));
        }
    }
}
