package tconstruct.plugins.nei;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;

import codechicken.nei.guihook.GuiContainerManager;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** The slot hooks NEI's coremod puts around a slot's item, for a slot drawn without vanilla's slot drawing. */
@SideOnly(Side.CLIENT)
public final class StationNei {

    private static final boolean LOADED = Loader.isModLoaded("NotEnoughItems");

    private StationNei() {}

    /** Before the item, as NEI calls it. */
    public static void underlay(GuiContainer gui, Slot slot) {
        if (LOADED) Hooks.underlay(gui, slot);
    }

    /** After the item and its count: the search highlight's darkening, among others. */
    public static void overlay(GuiContainer gui, Slot slot) {
        if (LOADED) Hooks.overlay(gui, slot);
    }

    /** The only class naming NEI, so it loads only when NEI is there. */
    private static final class Hooks {

        static void underlay(GuiContainer gui, Slot slot) {
            GuiContainerManager.getManager(gui).renderSlotUnderlay(slot);
        }

        static void overlay(GuiContainer gui, Slot slot) {
            GuiContainerManager.getManager(gui).renderSlotOverlay(slot);
        }
    }
}
