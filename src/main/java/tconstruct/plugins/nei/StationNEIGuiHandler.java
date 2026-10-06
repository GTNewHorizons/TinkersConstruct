package tconstruct.plugins.nei;

import net.minecraft.client.gui.inventory.GuiContainer;

import codechicken.nei.api.INEIGuiAdapter;
import tconstruct.tools.gui.TinkerStationGui;

/** NEI asks only registered handlers which item grid cells a GUI covers, never the GUI itself. */
public class StationNEIGuiHandler extends INEIGuiAdapter {

    @Override
    public boolean hideItemPanelSlot(GuiContainer gui, int x, int y, int w, int h) {
        return gui instanceof TinkerStationGui station && station.hideItemPanelSlot(gui, x, y, w, h);
    }
}
