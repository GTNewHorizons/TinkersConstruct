package tconstruct.tools.logic;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.world.World;

import tconstruct.library.tools.ToolCore;
import tconstruct.tools.inventory.ToolStationContainer;

/*
 * Simple class for storing items in the block
 */

public class ToolStationLogic extends StationCraft implements ISidedInventory {

    private static final int[] NO_SLOTS = new int[0];

    public ItemStack previousTool;
    public String toolName;
    /** The tab the tile builds for, set by whoever last picked one on it, like toolName; null is Repair & Modify. */
    private ToolCore selectedTool;
    public final StationTabs tabs = new StationTabs();
    public final TableFacing table = new TableFacing(this);

    public ToolStationLogic() {
        super(7); // 0 output, 1 tool, 2-6 materials
        toolName = "";
    }

    public ToolStationLogic(int slots) {
        super(slots);
        toolName = "";
    }

    @Override
    public boolean canDropInventorySlot(int slot) {
        return slot != 0;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int var1) {
        return null;
    }

    @Override
    public String getDefaultName() {
        return "crafters.ToolStation";
    }

    @Override
    public Container getGuiContainer(InventoryPlayer inventoryplayer, World world, int x, int y, int z) {
        return new ToolStationContainer(inventoryplayer, this);
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        super.setInventorySlotContents(slot, stack);
        if (slot != 0) {
            buildTool(toolName);
        }
        syncContentsToClients();
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        ItemStack itemstack = super.decrStackSize(slot, amount);
        if (slot != 0) {
            buildTool(toolName);
        }
        syncContentsToClients();
        return itemstack;
    }

    // the table renders its contents in the world, so clients need the inventory
    public void syncContentsToClients() {
        if (worldObj != null && !worldObj.isRemote) {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    public ToolCore getSelectedTool() {
        return selectedTool;
    }

    /** The player is null when loading the tile; true when anything the tile saves changed. */
    public boolean setSelectedTool(ToolCore tool, EntityPlayer player) {
        boolean changed = tool != selectedTool;
        selectedTool = tool;
        if (player != null && tabs.rememberTab(player, tool)) changed = true;
        return changed;
    }

    @Override
    public void readFromNBT(NBTTagCompound tags) {
        super.readFromNBT(tags);
        table.readFromNBT(tags);
        setSelectedTool(StationTabs.toolNamed(tags.getString("SelectedTool")), null);
        tabs.readFromNBT(tags);
    }

    @Override
    public void writeToNBT(NBTTagCompound tags) {
        super.writeToNBT(tags);
        table.writeToNBT(tags);
        if (selectedTool != null) tags.setString("SelectedTool", Item.itemRegistry.getNameForObject(selectedTool));
        tabs.writeToNBT(tags);
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);
        // a local connection passes the packet unencoded; without the copy the integrated client shares the server's
        // stack tags and strips the output's ToRemove before the server reads it
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, (NBTTagCompound) tag.copy());
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
        readFromNBT(packet.func_148857_g());
        rememberServerBuild();
    }

    @Override
    public void markDirty() {
        if (this.worldObj != null) {
            this.blockMetadata = this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);
            this.worldObj.markTileEntityChunkModified(this.xCoord, this.yCoord, this.zCoord, this);
        }
    }

    public void buildTool(String name) {
        ItemStack output = selectedTool == null ? modifyTool(name) : buildFromParts(name);
        // the client predicts with its own config; for cells the server refused, the server's word stands
        if (output != null && worldObj != null && worldObj.isRemote && serverBuildsNothing(selectedTool)) output = null;
        inventory[0] = output;
    }

    public void setToolname(String name) {
        toolName = name;
        buildTool(name);
    }

    protected ItemStack tryRenameTool(ItemStack output, String name) {
        ItemStack temp;
        if (output != null) temp = output;
        else temp = inventory[1].copy();

        NBTTagCompound tags = temp.getTagCompound();
        if (tags == null) {
            tags = new NBTTagCompound();
            temp.setTagCompound(tags);
        }

        NBTTagCompound display = null;
        if (tags.hasKey("display") && tags.getCompoundTag("display").hasKey("Name"))
            display = tags.getCompoundTag("display");

        boolean doRename = false;
        if (display == null) {
            display = new NBTTagCompound();
            doRename = true;
        }
        // we only allow renaming with a nametag otherwise
        else if (!name.equals(display.getString("Name"))) {
            int nametagCount = 0;
            for (ItemStack itemStack : inventory)
                if (itemStack != null && itemStack.getItem() == Items.name_tag) nametagCount++;

            doRename = nametagCount == 1;
        }

        if (!doRename) return output;

        display.setString("Name", name);
        tags.setTag("display", display);
        temp.setRepairCost(2);
        output = temp;

        return output;
    }

    @Override
    public boolean canUpdate() {
        return false;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        return NO_SLOTS;
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
        return "null";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}
}
