package tconstruct.tools.logic;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import mantle.blocks.abstracts.InventoryLogic;
import tconstruct.library.crafting.StencilBuilder;
import tconstruct.tools.inventory.PatternShaperChestContainer;
import tconstruct.tools.inventory.PatternShaperContainer;

public class StencilTableLogic extends InventoryLogic implements ISidedInventory {

    private ItemStack selectedStack;

    public StencilTableLogic() {
        super(2);
    }

    @Override
    public boolean canUpdate() {
        return false;
    }

    @Override
    public String getDefaultName() {
        return "toolstation.PatternShaper";
    }

    @Override
    public Container getGuiContainer(InventoryPlayer inventoryplayer, World world, int x, int y, int z) {
        for (int xPos = x - 1; xPos <= x + 1; xPos++) {
            for (int zPos = z - 1; zPos <= z + 1; zPos++) {
                for (int yPos = y - 1; yPos <= y + 1; yPos++) {
                    TileEntity tile = world.getTileEntity(xPos, yPos, zPos);
                    if (tile instanceof PatternChestLogic && (x == xPos || z == zPos))
                        return new PatternShaperChestContainer(inventoryplayer, this, (PatternChestLogic) tile);
                }
            }
        }
        return new PatternShaperContainer(inventoryplayer, this);
    }

    /*
     * @Override public void onInventoryChanged() { if (inventory[0] == null) inventory[1] = null;
     * super.onInventoryChanged(); }
     */

    public void setSelectedPattern(ItemStack stack) {
        selectedStack = stack;
        if (stack == null) this.setInventorySlotContents(1, null);
        else {
            ItemStack stackBlank = this.getStackInSlot(0);
            if (stackBlank != null && stackBlank.stackSize > 0 && StencilBuilder.isBlank(stackBlank)) {
                boolean warning = true;
                for (ItemStack tempStack : StencilBuilder.instance.stencils.values()) {
                    if (tempStack != null && stack.isItemEqual(tempStack)) {
                        stack = tempStack.copy();
                        warning = false;
                        break;
                    }
                }
                if (!warning) this.setInventorySlotContents(1, stack);
            }
        }
    }

    public void setSelectedPattern() {
        setSelectedPattern(selectedStack);
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack itemstack) {
        super.setInventorySlotContents(slot, itemstack);
        if (slot == 0) {
            if (itemstack != null && StencilBuilder.isBlank(itemstack)) {
                super.setInventorySlotContents(1, selectedStack);
            } else if (itemstack != null) {
                ItemStack cpy = itemstack.copy();
                cpy.stackSize = 1;
                super.setInventorySlotContents(1, cpy);
            } else {
                super.setInventorySlotContents(1, null);
            }
        }
    }

    @Override
    public ItemStack decrStackSize(int slot, int quantity) {
        if (slot == 1) {
            super.decrStackSize(0, 1);
            if (inventory[0] == null) return super.decrStackSize(slot, quantity);
            else return inventory[1].copy();
        } else {
            ItemStack ret = super.decrStackSize(slot, quantity);
            if (inventory[0] == null) super.decrStackSize(1, 1);
            return ret;
        }
    }

    public void altDecrStackSize(int slot, int quantity) {
        if (inventory[slot] != null) {
            if (inventory[slot].stackSize <= quantity) {
                ItemStack stack = inventory[slot];
                inventory[slot] = null;
                return;
            }
            ItemStack split = inventory[slot].splitStack(quantity);
            if (inventory[slot].stackSize == 0) {
                inventory[slot] = null;
            }
        }
    }

    @Override
    public boolean canDropInventorySlot(int slot) {
        return slot == 0;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        return new int[0];
    }

    @Override
    public boolean canInsertItem(int i, ItemStack itemstack, int j) {
        return false;
    }

    @Override
    public boolean canExtractItem(int i, ItemStack itemstack, int j) {
        return false;
    }

    @Override
    public String getInventoryName() {
        return StatCollector.translateToLocal("toolstation.PatternShaper");
    }

    @Override
    public boolean hasCustomInventoryName() {
        return true;
    }

    @Override
    public void closeInventory() {}

    @Override
    public void openInventory() {}
}
