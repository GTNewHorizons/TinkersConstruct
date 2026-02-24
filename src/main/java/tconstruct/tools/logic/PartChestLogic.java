package tconstruct.tools.logic;

import mantle.blocks.abstracts.InventoryLogic;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import tconstruct.library.util.IPattern;
import tconstruct.library.util.IToolPart;
import tconstruct.tools.inventory.PartChestContainer;

public class PartChestLogic extends InventoryLogic {

    public PartChestLogic() {
        super(30);
    }

    @Override
    public boolean canUpdate() {
        return false;
    }

    @Override
    public String getDefaultName() {
        return "toolstation.partholder";
    }

    @Override
    public Container getGuiContainer(InventoryPlayer inventoryplayer, World world, int x, int y, int z) {
        return new PartChestContainer(inventoryplayer, this);
    }

    @Override
    public String getInventoryName() {
        return getDefaultName();
    }

    @Override
    public boolean hasCustomInventoryName() {
        return true;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack itemstack) {
        return itemstack != null && itemstack.getItem() instanceof IToolPart;
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    public boolean insertItemStackIntoInventory(ItemStack stack) {
        for (int i = 0; i < this.inventory.length; i++) {
            if (this.inventory[i] == null) {
                this.inventory[i] = stack.copy();
                stack.stackSize = 0;
                this.markDirty();
                return true;
            }
        }
        return false;
    }
}
