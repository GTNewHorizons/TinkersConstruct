package tconstruct.tools.logic;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import tconstruct.library.util.IPattern;
import tconstruct.tools.inventory.PatternChestContainer;

public class PatternChestLogic extends TiCChestLogic {

    @Override
    public String getDefaultName() {
        return "toolstation.patternholder";
    }

    @Override
    public Container getGuiContainer(InventoryPlayer inventoryplayer, World world, int x, int y, int z) {
        return new PatternChestContainer(inventoryplayer, this);
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack itemstack) {
        return itemstack != null && itemstack.getItem() instanceof IPattern;
    }

}
