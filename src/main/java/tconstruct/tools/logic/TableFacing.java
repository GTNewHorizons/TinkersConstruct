package tconstruct.tools.logic;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Direction;
import net.minecraft.util.MathHelper;

/** The way a station's tabletop display faces, and what turns it. */
public class TableFacing {

    private final ToolStationLogic logic;
    /** The side the display faces, as a Direction index. */
    private int facing;

    TableFacing(ToolStationLogic logic) {
        this.logic = logic;
    }

    public void faceToward(EntityPlayer player) {
        int look = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
        int side = Direction.rotateOpposite[look];
        if (side == facing) return;
        facing = side;
        logic.markDirty();
        logic.syncContentsToClients();
    }

    public int getFacing() {
        return facing;
    }

    public boolean receivedInput(ItemStack[] before) {
        for (int i = 1; i < before.length; i++) if (isNewInput(before[i], logic.getStackInSlot(i))) return true;
        return false;
    }

    public static boolean isNewInput(ItemStack was, ItemStack now) {
        if (now == null) return false;
        return was == null || now.stackSize > was.stackSize
                || !now.isItemEqual(was)
                || !ItemStack.areItemStackTagsEqual(now, was);
    }

    void readFromNBT(NBTTagCompound tags) {
        // a station saved without the key reads 0, south
        facing = tags.getByte("Facing") & 3;
    }

    void writeToNBT(NBTTagCompound tags) {
        tags.setByte("Facing", (byte) facing);
    }
}
