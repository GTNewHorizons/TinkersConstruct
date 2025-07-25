package tconstruct.client.tabs;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;

import codechicken.nei.NEIClientConfig;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TabRegistry {

    public void registerEvent() {
        MinecraftForge.EVENT_BUS.register(new EventHandler());
    }

    private static final ArrayList<AbstractTab> tabList = new ArrayList<>();

    public static void registerTab(AbstractTab tab) {
        tabList.add(tab);
    }

    public static ArrayList<AbstractTab> getTabList() {
        return tabList;
    }

    // Client method
    public void guiPostInit(GuiScreenEvent.InitGuiEvent.Post event) {
        if ((event.gui instanceof GuiInventory)) {
            int xSize = 176;
            int ySize = 166;
            int guiLeft = (event.gui.width - xSize) / 2;
            int guiTop = (event.gui.height - ySize) / 2;
            guiLeft += getPotionOffset();

            updateTabValues(guiLeft, guiTop, InventoryTabVanilla.class);
            addTabsToList(event.gui.buttonList);
        }
    }

    private static final Minecraft mc = FMLClientHandler.instance().getClient();

    public static void openInventoryGui() {
        mc.thePlayer.sendQueue.addToSendQueue(new C0DPacketCloseWindow(mc.thePlayer.openContainer.windowId));
        GuiInventory inventory = new GuiInventory(mc.thePlayer);
        mc.displayGuiScreen(inventory);
    }

    public static void updateTabValues(int cornerX, int cornerY, Class<?> selectedButton) {
        int count = 2;
        for (AbstractTab t : tabList) {
            if (t.shouldAddToList()) {
                t.id = count;
                t.xPosition = cornerX + (count - 2) * 28;
                t.yPosition = cornerY - 28;
                t.enabled = !t.getClass().equals(selectedButton);
                count++;
            }
        }
    }

    public static void addTabsToList(List buttonList) {
        for (AbstractTab tab : tabList) {
            if (tab.shouldAddToList()) {
                buttonList.add(tab);
            }
        }
    }

    public static int getPotionOffset() {
        // If at least one potion is active...
        if (!mc.thePlayer.getActivePotionEffects().isEmpty()) {
            if (Loader.isModLoaded("NotEnoughItems")) {
                try {
                    if (NEIClientConfig.isHidden() || !NEIClientConfig.isEnabled()) {
                        // If NEI is disabled or hidden, offset the tabs by 60
                        return 60;
                    }
                } catch (Exception ignored) {}
            } else {
                // If NEI is not installed, offset the tabs
                return 60;
            }
        }
        // No potions, no offset needed
        return 0;
    }

    public class EventHandler {

        @SideOnly(Side.CLIENT)
        @SubscribeEvent
        public void guiPostInitWrapper(GuiScreenEvent.InitGuiEvent.Post event) {
            TabRegistry.this.guiPostInit(event);
        }
    }
}
