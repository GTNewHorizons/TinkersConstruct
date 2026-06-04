package tconstruct.tools.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import tconstruct.library.modifier.IModifyable;
import tconstruct.library.tools.AbilityHelper;

public class SlotCraftingStation extends SlotCrafting {

    private final IInventory matrix;
    private final EntityPlayer player;

    public SlotCraftingStation(EntityPlayer par1EntityPlayer, IInventory par2IInventory, IInventory par3iInventory,
            int par4, int par5, int par6) {
        super(par1EntityPlayer, par2IInventory, par3iInventory, par4, par5, par6);
        this.matrix = par2IInventory;
        this.player = par1EntityPlayer;
    }

    @Override
    public void onPickupFromSlot(EntityPlayer player, ItemStack stack) {
        ItemStack tool = this.matrix.getStackInSlot(4);
        if (stack.getItem() instanceof IModifyable modifyable && tool != null
                && tool.getItem() instanceof IModifyable) {
            NBTTagCompound tags = stack.getTagCompound().getCompoundTag(modifyable.getBaseTagName());
            int[] toRemoveArray = tags.hasKey("ToRemove") ? tags.getIntArray("ToRemove") : null;
            int toRemoveIndex = 0;
            IInventory inventory = this.matrix;

            for (int i = 0; i <= inventory.getSizeInventory(); i++) {
                if (i == 4) continue;
                ItemStack item = inventory.getStackInSlot(i);
                if (item == null) {
                    continue;
                }
                if (toRemoveArray == null) {
                    inventory.decrStackSize(i, 1);
                } else {
                    inventory.decrStackSize(i, toRemoveArray[toRemoveIndex]);
                    toRemoveIndex++;
                }
            }
            tags.removeTag("ToRemove");

            matrix.setInventorySlotContents(4, null);
            player.worldObj.playSoundEffect(
                    player.posX,
                    player.posY,
                    player.posZ,
                    "tinker:little_saw",
                    1.0F,
                    (AbilityHelper.random.nextFloat() - AbilityHelper.random.nextFloat()) * 0.2F + 1.0F);
        } else {
            super.onPickupFromSlot(player, stack);
        }
    }
}
