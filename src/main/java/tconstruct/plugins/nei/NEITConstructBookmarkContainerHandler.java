package tconstruct.plugins.nei;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

import codechicken.nei.api.IBookmarkContainerHandler;
import tconstruct.tools.gui.CraftingStationGui;
import tconstruct.tools.inventory.CraftingStationContainer;
import tconstruct.tools.logic.CraftingStationLogic;

public class NEITConstructBookmarkContainerHandler implements IBookmarkContainerHandler {

    protected static Minecraft mc = Minecraft.getMinecraft();
    final int magicNumber = (5 * 9) + 1; // First 46 slots are player inventory in the station, including crafting field
                                         // and product

    @Override
    public void pullBookmarkItemsFromContainer(GuiContainer guiContainer, ArrayList<ItemStack> bookmarkItems) {
        CraftingStationGui gui = (CraftingStationGui) guiContainer;
        CraftingStationLogic logic = ((CraftingStationContainer) guiContainer.inventorySlots).logic;
        if (logic.getFirstInventory() == null) {
            return;
        }

        List<ItemStack> containerStacks = getStacks(gui);
        for (ItemStack bookmarkItem : bookmarkItems) {

            int bookmarkSizeBackup = bookmarkItem.stackSize;

            for (int i = magicNumber; i < containerStacks.size(); i++) {
                ItemStack containerItem = containerStacks.get(i);

                if (containerItem == null) {
                    continue;
                }

                if (bookmarkItem.isItemEqual(containerItem)) {
                    if (bookmarkItem.stackSize <= 0) {
                        break;
                    }

                    int transferAmount = Math.min(bookmarkItem.stackSize, containerItem.stackSize);

                    moveItems(guiContainer, i, transferAmount);
                    bookmarkItem.stackSize -= transferAmount;

                    if (bookmarkItem.stackSize == 0) {
                        break;
                    }
                }
            }
            bookmarkItem.stackSize = bookmarkSizeBackup;
        }
    }

    private List<ItemStack> getStacks(GuiContainer container) {
        List<ItemStack> result = new ArrayList<>();
        for (int i = 0; i < container.inventorySlots.inventorySlots.size(); i++) {
            result.add(container.inventorySlots.getSlot(i).getStack());
        }
        return result;
    }

    private void moveItems(GuiContainer container, int fromSlot, int transferAmount) {
        for (int i = 0; i < transferAmount; i++) {
            int toSlot = findValidPlayerInventoryDestination(container.inventorySlots, fromSlot);
            if (toSlot == -1) {
                return;
            }
            clickSlot(container, fromSlot, 0);
            clickSlot(container, toSlot, 1);
            clickSlot(container, fromSlot, 0);
        }
    }

    private void clickSlot(GuiContainer container, int slotIdx, int button) {
        mc.playerController.windowClick(container.inventorySlots.windowId, slotIdx, button, 0, mc.thePlayer);
    }

    private int findValidPlayerInventoryDestination(Container container, int fromSlot) {
        ItemStack stackToMove = container.getSlot(fromSlot).getStack();
        for (int i = 10; i < magicNumber; i++) { // 10 comes from the magic number including crafting field + product
            ItemStack toStack = container.getSlot(i).getStack();
            if (toStack == null) {
                return i;
            }
            int diff = stackToMove.getMaxStackSize() - toStack.stackSize;
            if (toStack.isItemEqual(stackToMove) && diff > 0) {
                return i;
            }
        }
        return -1;
    }
}
