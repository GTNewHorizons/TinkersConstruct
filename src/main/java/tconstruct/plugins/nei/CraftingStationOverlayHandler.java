package tconstruct.plugins.nei;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;

import codechicken.nei.recipe.DefaultOverlayHandler;
import codechicken.nei.recipe.IRecipeHandler;
import tconstruct.tools.gui.CraftingStationGui;
import tconstruct.tools.inventory.CraftingStationContainer;
import tconstruct.tools.logic.CraftingStationLogic;

/**
 * Modified copy of DefaultOverlayHandler from NotEnoughItems
 */
public class CraftingStationOverlayHandler extends DefaultOverlayHandler {

    @Override
    public void overlayRecipe(GuiContainer gui, IRecipeHandler handler, int recipeIndex, boolean maxTransfer) {
        transferRecipe(gui, handler, recipeIndex, maxTransfer ? Integer.MAX_VALUE : 1);
    }

    @Override
    public int transferRecipe(GuiContainer gui, IRecipeHandler handler, int recipeIndex, int multiplier) {

        if (gui.inventorySlots instanceof CraftingStationContainer stationContainer) {
            if (stationContainer.logic.chest != null && gui instanceof CraftingStationGui stationGui) {
                offsetx = 5 + stationGui.getChestWidth();
            } else {
                offsetx = 5;
            }
        }

        return super.transferRecipe(gui, handler, recipeIndex, multiplier);
    }

    @Override
    public boolean canMoveFrom(Slot slot, GuiContainer gui) {
        if (gui.inventorySlots instanceof CraftingStationContainer stationContainer) {
            CraftingStationLogic logic = stationContainer.logic;
            if (logic.chest != null && slot.inventory == logic.chest.get()) return true;
            if (logic.doubleChest != null && slot.inventory == logic.doubleChest.get()) return true;
        }

        return super.canMoveFrom(slot, gui);
    }
}
