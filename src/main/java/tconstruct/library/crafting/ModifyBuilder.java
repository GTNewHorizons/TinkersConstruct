package tconstruct.library.crafting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

import tconstruct.library.event.ModifyEvent;
import tconstruct.library.modifier.IModifyable;
import tconstruct.library.modifier.ItemModifier;

public class ModifyBuilder {

    public static ModifyBuilder instance = new ModifyBuilder();
    public List<ItemModifier> itemModifiers = new ArrayList<>();

    // used for the implementation of https://github.com/GTNewHorizons/TinkersConstruct/pull/290
    // forced to do it this way because doing it properly would require the refactoring of how modifiers
    // are implemented in TiC, which would probably also break compatibility with addons
    // really hacky, but it works(tm)
    public static ItemStack[] makeDummyModifierArray(ItemStack[] modifiers) {
        int arraySize = 0;

        for (ItemStack modifier : modifiers) {
            if (modifier != null) arraySize += modifier.stackSize;
        }

        ItemStack[] res = new ItemStack[arraySize];
        int fromIndex = 0;

        for (ItemStack modifier : modifiers) {
            if (modifier == null) continue;

            ItemStack itemCopy = modifier.copy();
            itemCopy.stackSize = 1;
            Arrays.fill(res, fromIndex, fromIndex + modifier.stackSize, itemCopy);
            fromIndex += modifier.stackSize; // prevent overwriting previous entries
        }

        return res;
    }

    public ItemStack modifyItem(ItemStack input, ItemStack[] modifiers) {
        ItemStack copy = input.copy(); // Prevent modifying the original
        if (copy.getItem() instanceof IModifyable) {
            IModifyable item = (IModifyable) copy.getItem();

            boolean built = false;
            for (ItemModifier mod : itemModifiers) {
                if (mod.matches(modifiers, copy) && mod.validType(item)) {
                    ModifyEvent event = new ModifyEvent(mod, item, copy);
                    MinecraftForge.EVENT_BUS.post(event);
                    if (event.isCanceled()) continue;

                    built = true;
                    mod.addMatchingEffect(copy); // Order matters here
                    mod.modify(modifiers, copy);

                    // we do not allow negative modifiers >:(
                    if (copy.getTagCompound().getCompoundTag(item.getBaseTagName()).getInteger("Modifiers") < 0)
                        return null;
                }
            }
            if (built) return copy;
        }
        return null;
    }

    public static void registerModifier(ItemModifier mod) {
        if (mod == null) throw new NullPointerException("Modifier cannot be null.");
        instance.itemModifiers.add(mod);
    }
}
