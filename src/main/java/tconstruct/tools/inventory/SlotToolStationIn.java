package tconstruct.tools.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import tconstruct.library.crafting.ToolPartRules;
import tconstruct.library.tools.BuildInput;
import tconstruct.library.tools.ToolCore;
import tconstruct.tools.logic.TableFacing;

/** An input slot of the Tool Station and Tool Forge; on a build tab it takes only the part the tool uses here. */
public class SlotToolStationIn extends Slot {

    private final ToolStationContainer container;

    public SlotToolStationIn(ToolStationContainer container, IInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
        this.container = container;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        ToolCore tool = container.selectedTool;
        if (tool == null) return true;
        int index = getSlotIndex();
        return container.isPartSlot(index) && ToolPartRules.takesPart(tool, BuildInput.ofCell(index), stack);
    }

    /** The table turns toward whoever puts something in, however it gets here: a click, Bogosorter, NEI. */
    @Override
    public void putStack(ItemStack stack) {
        ItemStack was = getStack();
        // Bogosorter grows the stack in place and puts the same object back, so a size comparison cannot see it
        boolean in = stack != null && (stack == was || TableFacing.isNewInput(was, stack));
        super.putStack(stack);
        EntityPlayer player = container.invPlayer.player;
        if (in && !player.worldObj.isRemote) container.logic.table.faceToward(player);
    }
}
