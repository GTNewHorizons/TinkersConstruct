package tconstruct.library.crafting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import tconstruct.library.TConstructRegistry;
import tconstruct.library.tools.BuildInput;
import tconstruct.library.tools.CustomMaterial;
import tconstruct.library.tools.ToolCore;
import tconstruct.library.tools.ToolMaterial;
import tconstruct.library.weaponry.ArrowShaftMaterial;
import tconstruct.tools.TinkerTools;

/** What a build of a tool takes at each of its inputs, by the kind of part and by its material. */
public final class ToolPartRules {

    private ToolPartRules() {}

    /**
     * Whether the stack is a part this input takes by kind: the tool's own part there, or one a recipe for the tool
     * takes as that kind, as it is or as partAsBuilt turns it. A part the tool names for another input is refused.
     */
    public static boolean acceptsPart(ToolCore tool, BuildInput input, ItemStack stack) {
        if (stack == null) return false;
        Item[] parts = tool.getToolBuildComponents();
        if (input.ordinal() >= parts.length || parts[input.ordinal()] == null) return false;
        Item item = stack.getItem();
        if (item == parts[input.ordinal()]) return true;
        for (Item part : parts) if (part == item) return false;

        BuildInput kind = tool.kindOf(input);
        List<Item> candidates = new ArrayList<>();
        candidates.add(item);
        ItemStack built = partAsBuilt(tool, input, stack);
        if (built != stack) candidates.add(built.getItem());
        if (kind == BuildInput.HANDLE) {
            // WeaponryHandler.buildArrow swaps a shaft material for its part
            CustomMaterial shaft = TConstructRegistry.getCustomMaterial(built, ArrowShaftMaterial.class);
            if (shaft != null) candidates.add(shaft.craftingItem.getItem());
        }
        for (ToolRecipe recipe : ToolBuilder.instance.combos) {
            if (recipe.getType().getClass() != tool.getClass()) continue;
            for (Item candidate : candidates) if (kind.validIn(recipe, candidate)) return true;
        }
        return false;
    }

    public static boolean takesPart(ToolCore tool, BuildInput input, ItemStack stack) {
        return acceptsPart(tool, input, stack) && tool.takesPartMaterial(input, stack);
    }

    /**
     * What the build makes of the stack: a bone or a stickWood stick in a handle is a tool rod, and a string where the
     * binding goes is a binding of IguanaTweaks' String material while that is registered. Anything else as it is.
     */
    public static ItemStack partAsBuilt(ToolCore tool, BuildInput input, ItemStack stack) {
        BuildInput kind = tool.kindOf(input);
        if (kind == BuildInput.HANDLE) return handleAsBuilt(stack);
        if (kind == BuildInput.ACCESSORY && stack.getItem() == Items.string
                && kind.partOf(tool) == TinkerTools.binding) {
            int string = materialId("String");
            if (string >= 0) return new ItemStack(TinkerTools.binding, 1, string);
        }
        return stack;
    }

    /** The tool rod TinkerToolEvents.buildTool makes of a bone or a stickWood stick; anything else as it is. */
    public static ItemStack handleAsBuilt(ItemStack handle) {
        if (handle.getItem() == Items.bone) return new ItemStack(TinkerTools.toolRod, 1, TinkerTools.MaterialID.Bone);
        if (isStickWood(handle)) return new ItemStack(TinkerTools.toolRod, 1, TinkerTools.MaterialID.Wood);
        return handle;
    }

    private static boolean isStickWood(ItemStack stack) {
        for (ItemStack stick : OreDictionary.getOres("stickWood")) {
            if (OreDictionary.itemMatches(stick, stack, false)) return true;
        }
        return false;
    }

    private static int materialId(String name) {
        ToolMaterial material = TConstructRegistry.getMaterial(name);
        if (material == null) return -1;
        for (Map.Entry<Integer, ToolMaterial> entry : TConstructRegistry.toolMaterials.entrySet()) {
            if (entry.getValue() == material) return entry.getKey();
        }
        return -1;
    }
}
