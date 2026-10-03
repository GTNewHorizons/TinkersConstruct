package tconstruct.weaponry;

import net.minecraft.item.ItemStack;

import tconstruct.library.TConstructRegistry;
import tconstruct.library.crafting.ToolBuilder;
import tconstruct.library.crafting.ToolPartRules;
import tconstruct.library.tools.CustomMaterial;
import tconstruct.library.tools.FletchingMaterial;
import tconstruct.library.tools.FletchlingLeafMaterial;
import tconstruct.library.weaponry.ArrowShaftMaterial;

public final class AmmoMaterials {

    private AmmoMaterials() {}

    /** The shaft material a handle builds with, found as WeaponryHandler.buildArrow, then onAmmoCrafted, finds it. */
    public static ArrowShaftMaterial shaftMaterial(ItemStack handle) {
        ItemStack built = ToolPartRules.handleAsBuilt(handle);
        CustomMaterial shaft = TConstructRegistry.getCustomMaterial(built, ArrowShaftMaterial.class);
        if (shaft == null) {
            shaft = TConstructRegistry
                    .getCustomMaterial(ToolBuilder.instance.getMaterialID(built), ArrowShaftMaterial.class);
        }
        return (ArrowShaftMaterial) shaft;
    }

    /** The fletching registered under the id, feather or leaf kind; the registry matches the class exactly. */
    public static FletchingMaterial fletchingMaterial(int materialId) {
        CustomMaterial fletching = TConstructRegistry.getCustomMaterial(materialId, FletchingMaterial.class);
        if (fletching == null) {
            fletching = TConstructRegistry.getCustomMaterial(materialId, FletchlingLeafMaterial.class);
        }
        return (FletchingMaterial) fletching;
    }
}
