package tconstruct.tools.inventory;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import tconstruct.armor.inventory.SlotOnlyTake;
import tconstruct.tools.TinkerTools;
import tconstruct.tools.logic.StencilTableLogic;

public class PatternShaperContainer extends Container {

	public StencilTableLogic logic;

	public static ItemStack BLANK_PATTERN = new ItemStack(TinkerTools.blankPattern, 1, 0);

	public PatternShaperContainer(InventoryPlayer inventoryplayer, StencilTableLogic shaper) {
		logic = shaper;
		this.addSlotToContainer(new Slot(shaper, 0, 48, 35));
		this.addSlotToContainer(new SlotOnlyTake(shaper, 1, 106, 35));
		/*
		 * for (int i = 0; i < 3; i++) { for (int l = 0; l < 3; l++) {
		 * this.addSlotToContainer(new Slot(craftMatrix, l + i * 3, 30 + l * 18, 17 + i
		 * * 18)); } }
		 */

		/* Player inventory */
		for (int column = 0; column < 3; column++) {
			for (int row = 0; row < 9; row++) {
				this.addSlotToContainer(
						new Slot(inventoryplayer, row + column * 9 + 9, 8 + row * 18, 84 + column * 18));
			}
		}

		for (int column = 0; column < 9; column++) {
			this.addSlotToContainer(new Slot(inventoryplayer, column, 8 + column * 18, 142));
		}
	}

	/*
	 * public void onCraftMatrixChanged(IInventory iinventory) {
	 * craftResult.setInventorySloTRepos(0,
	 * CraftingManager.getInstance().findMatchingRecipe(craftMatrix, worldObj)); }
	 */

	/*
	 * @Override public void onContainerClosed(EntityPlayer entityplayer) {
	 * super.onContainerClosed(entityplayer); if (logic.worldObj.isRemote) { return;
	 * } ItemStack itemstack = logic.getStackInSlot(0); if (itemstack != null) {
	 * entityplayer.dropPlayerItem(itemstack); } }
	 */

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		Block block = logic.getWorldObj().getBlock(logic.xCoord, logic.yCoord, logic.zCoord);
		if (block != TinkerTools.toolStationWood && block != TinkerTools.craftingSlabWood)
			return false;
		return logic.isUseableByPlayer(entityplayer);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotID) {
		return null;
	}

	@Override
	public ItemStack slotClick(int slotId, int clickedButton, int mode, EntityPlayer player) {
		if (mode == 1 && clickedButton == 0) {
			// is it shift left click?
			Slot slot = (Slot) this.inventorySlots.get(slotId);
			if (slot != null && slot.getHasStack()) {
				ItemStack stack = slot.getStack();
				if (slotId >= logic.getSizeInventory() && stack.isItemEqual(BLANK_PATTERN)) {
					// clicked in player inventory
					if (!this.mergeItemStack(stack, 0, 1, false)) {
						return null;
					}
				} else if (slotId < logic.getSizeInventory()) {
					// pattern slot
					if (!this.mergeItemStack(stack, 2, this.inventorySlots.size(), false)) {
						return null;
					}else if(slotId == 1) {
						// special for shift click the output pattern
						this.inventorySlots.get(0).decrStackSize(1);
					}
				}
				if (stack.stackSize == 0) {
					slot.putStack((ItemStack) null);
				} else {
					slot.onSlotChanged();
				}
			}
		}
		return super.slotClick(slotId, clickedButton, mode, player);
	}
}