package tconstruct.plugins.nei;

import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import codechicken.nei.PositionedStack;
import codechicken.nei.api.IStackPositioner;
import codechicken.nei.recipe.GuiRecipe;
import tconstruct.TConstruct;
import tconstruct.tools.gui.CraftingStationGui;

public class CraftingStationStackPositioner implements IStackPositioner {

    @Override
    public ArrayList<PositionedStack> positionStacks(ArrayList<PositionedStack> stacks) {
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;

        if (screen instanceof GuiRecipe) {
            screen = ((GuiRecipe<?>) screen).firstGui;
        }

        if (screen instanceof CraftingStationGui) {
            CraftingStationGui gui = (CraftingStationGui) screen;

            int offsetX = gui.hasChest() ? 5 + gui.getChestWidth() : 5;
            int offsetY = 11;

            for (PositionedStack stack : stacks) {
                stack.relx += offsetX;
                stack.rely += offsetY;
            }
        } else {
            TConstruct.logger.warn("No CraftingStationGui found!");
        }

        return stacks;
    }
}
