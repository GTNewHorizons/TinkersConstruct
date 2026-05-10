package tconstruct.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class TiCRecipeHolder {

    private static final Field WIDTH_FIELD;
    private static final Field HEIGHT_FIELD;

    static {
        try {
            WIDTH_FIELD = ShapedOreRecipe.class.getDeclaredField("width");
            HEIGHT_FIELD = ShapedOreRecipe.class.getDeclaredField("height");

            WIDTH_FIELD.setAccessible(true);
            HEIGHT_FIELD.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public enum RecipeType {

        ShapedOre("ShapedOreRecipe"),
        ShapelessOre("ShapelessOreRecipe"),
        Shaped("ShapedRecipes"),
        Shapeless("ShapelessRecipes"),
        Furnace("FurnaceRecipes"),
        ToolStation("ToolStationRecipe"),
        ToolForge("ToolForgeRecipe");

        public String type;

        private RecipeType(String t) {
            this.type = t;
        }

    }

    private static int getSize(ShapedOreRecipe sor) {
        try {
            return Math.max(WIDTH_FIELD.getInt(sor), HEIGHT_FIELD.getInt(sor));
        } catch (IllegalAccessException e) {
            return 0;
        }
    }

    public final RecipeType recipeType;
    public final ItemStack outputStack;
    public final ItemStack[][] inputStacks;
    public final int varietyOfOre;
    public final int recipeSize;

    public TiCRecipeHolder(IRecipe recipe) {
        int maxRecipesSize = 0;
        RecipeType craftingType = null;
        ItemStack target = recipe.getRecipeOutput();
        ItemStack[][] inputs = new ItemStack[0][];
        int recipeSize = 0;
        List<ItemStack[]> a = new ArrayList<>();
        if (recipe instanceof ShapedOreRecipe sor) {
            craftingType = RecipeType.ShapedOre;
            for (Object b : Arrays.asList(sor.getInput())) {
                if (b instanceof List<?>l) {
                    maxRecipesSize = Math.max(maxRecipesSize, l.size());
                    a.add(l.toArray(new ItemStack[] {}));
                } else if (b instanceof ItemStack l) {
                    a.add(new ItemStack[] { l });
                } else {
                    a.add(null);
                }
            }
            inputs = a.toArray(inputs);
            recipeSize = getSize(sor);
        } else if (recipe instanceof ShapelessOreRecipe sor) {
            craftingType = RecipeType.ShapelessOre;
            for (Object b : sor.getInput()) {
                if (b instanceof List<?>l) {
                    maxRecipesSize = Math.max(maxRecipesSize, l.size());
                    a.add(l.toArray(new ItemStack[] {}));
                } else if (b instanceof ItemStack l) {
                    a.add(new ItemStack[] { l });
                }
            }
            inputs = a.toArray(inputs);
        } else if (recipe instanceof ShapedRecipes sr) {
            craftingType = RecipeType.Shaped;
            inputs = Arrays.asList(sr.recipeItems).stream().map(i -> new ItemStack[] { i }).collect(Collectors.toList())
                    .toArray(inputs);
            recipeSize = Math.max(sr.recipeWidth, sr.recipeHeight);
        } else if (recipe instanceof ShapelessRecipes sr) {
            craftingType = RecipeType.Shapeless;
            inputs = sr.recipeItems.stream().map(i -> new ItemStack[] { i }).collect(Collectors.toList())
                    .toArray(inputs);
        }
        recipeSize = recipeSize == 0 ? (inputs.length > 4 ? 3 : 2) : recipeSize;

        this.recipeType = craftingType;
        this.outputStack = target;
        this.inputStacks = inputs;
        this.varietyOfOre = maxRecipesSize;
        this.recipeSize = recipeSize;

    }

    public TiCRecipeHolder(ItemStack input, ItemStack output) {
        this(new ItemStack[][] { {input} }, output, RecipeType.Furnace);
    }

    public TiCRecipeHolder(ItemStack[][] input, ItemStack output, RecipeType t) {
        this.inputStacks = input;
        this.outputStack = output;
        this.varietyOfOre = 1;
        this.recipeSize = 0;
        this.recipeType = t;
    }

}
