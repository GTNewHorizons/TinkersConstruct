package tconstruct.tools.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import tconstruct.library.client.TConstructClientRegistry;
import tconstruct.library.client.ToolGuiElement;
import tconstruct.tools.logic.ToolForgeLogic;

@SideOnly(Side.CLIENT)
public class ToolForgeGui extends ToolStationGui {

    public ToolForgeGui(InventoryPlayer inventoryplayer, ToolForgeLogic stationlogic, World world, int x, int y,
            int z) {
        super(inventoryplayer, stationlogic, world, x, y, z);
    }

    @Override
    StationTheme theme() {
        return StationTheme.METAL;
    }

    @Override
    protected List<ToolGuiElement> tabs() {
        List<ToolGuiElement> station = TConstructClientRegistry.toolButtons;
        List<ToolGuiElement> tabs = new ArrayList<>(station.size() + TConstructClientRegistry.tierTwoButtons.size());
        tabs.add(station.get(0));
        tabs.addAll(TConstructClientRegistry.tierTwoButtons);
        tabs.addAll(station.subList(1, station.size()));
        return tabs;
    }

    @Override
    protected int[][] slotTypeLayout(int type) {
        return StationSlotLayouts.forgeLayout(type);
    }
}
