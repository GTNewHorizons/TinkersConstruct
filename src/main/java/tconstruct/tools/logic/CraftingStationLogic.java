package tconstruct.tools.logic;

import java.lang.ref.WeakReference;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import mantle.blocks.abstracts.InventoryLogic;
import tconstruct.tools.inventory.CraftingStationContainer;
import tconstruct.util.config.PHConstruct;

public class CraftingStationLogic extends InventoryLogic implements ISidedInventory {

    private static final String SIDE_INVENTORY_PREFERENCES_TAG = "SideInventoryPreferences";
    private static final int CRAFTING_GRID_FIRST_SLOT = 1;
    private static final int CRAFTING_GRID_SIZE = 9;
    private static final int VALID_SIDE_INVENTORY_PREFERENCES = (1 << CRAFTING_GRID_SIZE) - 1;

    public ForgeDirection chestDirection = ForgeDirection.UNKNOWN;
    public int chestSize;
    public WeakReference<IInventory> chest;
    public WeakReference<IInventory> doubleChest;
    public WeakReference<IInventory> patternChest;
    public WeakReference<IInventory> furnace;
    public boolean tinkerTable;
    public boolean stencilTable;
    public boolean doubleFirst;

    public int invRows, invColumns, slotCount;

    private static final int[] NO_SLOTS = new int[0];

    private int sideInventoryPreferences;

    /** Cached result of {@link #getInventories()}; rebuilt when the adjacent inventories are rescanned. */
    @SuppressWarnings("rawtypes")
    private WeakReference[] inventories = new WeakReference[4];

    public CraftingStationLogic() {
        super(10); // 9 for crafting, 1 for output
    }

    @Override
    public boolean canDropInventorySlot(int slot) {
        return slot != 0;
    }

    @Override
    public ItemStack decrStackSize(int slot, int quantity) {
        if (slot == 0) {
            for (int i = 1; i < getSizeInventory(); i++) decrStackSize(i, 1);
        }
        ItemStack removed = super.decrStackSize(slot, quantity);
        clearSideInventoryPreferenceIfEmpty(slot);
        return removed;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack itemstack) {
        super.setInventorySlotContents(slot, itemstack);
        clearSideInventoryPreferenceIfEmpty(slot);
    }

    public boolean prefersSideInventory(int slot) {
        int preference = getSideInventoryPreference(slot);
        return preference != 0 && (sideInventoryPreferences & preference) != 0;
    }

    public void setSideInventoryPreference(int slot, boolean prefersSideInventory) {
        int preference = getSideInventoryPreference(slot);
        if (preference == 0) return;

        int updatedPreferences = prefersSideInventory ? sideInventoryPreferences | preference
                : sideInventoryPreferences & ~preference;
        setSideInventoryPreferences(updatedPreferences);
    }

    public int getSideInventoryPreferences() {
        return sideInventoryPreferences;
    }

    public void setSideInventoryPreferences(int preferences) {
        preferences &= VALID_SIDE_INVENTORY_PREFERENCES;
        if (sideInventoryPreferences != preferences) {
            sideInventoryPreferences = preferences;
            markDirty();
        }
    }

    private void clearSideInventoryPreferenceIfEmpty(int slot) {
        if (slot < CRAFTING_GRID_FIRST_SLOT || slot >= CRAFTING_GRID_FIRST_SLOT + CRAFTING_GRID_SIZE) return;

        ItemStack stack = getStackInSlot(slot);
        if (stack == null || stack.stackSize <= 0) {
            setSideInventoryPreference(slot, false);
        }
    }

    private static int getSideInventoryPreference(int slot) {
        int gridSlot = slot - CRAFTING_GRID_FIRST_SLOT;
        return gridSlot >= 0 && gridSlot < CRAFTING_GRID_SIZE ? 1 << gridSlot : 0;
    }

    @Override
    public void readFromNBT(NBTTagCompound tags) {
        super.readFromNBT(tags);
        sideInventoryPreferences = tags.getInteger(SIDE_INVENTORY_PREFERENCES_TAG) & VALID_SIDE_INVENTORY_PREFERENCES;
    }

    @Override
    public void writeToNBT(NBTTagCompound tags) {
        super.writeToNBT(tags);
        tags.setInteger(SIDE_INVENTORY_PREFERENCES_TAG, sideInventoryPreferences);
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return isUseableByPlayer(player, this.getInventories()) && super.isUseableByPlayer(player);
    }

    @Override
    public Container getGuiContainer(InventoryPlayer inventoryplayer, World world, int x, int y, int z) {
        chest = null;
        chestSize = 0;
        slotCount = 0;
        chestDirection = ForgeDirection.UNKNOWN;
        doubleChest = null;
        patternChest = null;
        furnace = null;
        tinkerTable = false;

        for (final ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            final int xPos = x + dir.offsetX, yPos = y + dir.offsetY, zPos = z + dir.offsetZ;
            final TileEntity tile = world.getTileEntity(xPos, yPos, zPos);
            if (!(tile instanceof IInventory inv) || (tile instanceof CraftingStationLogic)
                    || isBlacklisted(tile.getClass()))
                continue;

            if (patternChest == null && tile instanceof PatternChestLogic) {
                patternChest = new WeakReference<>(inv);
                continue;
            } else if (furnace == null && (tile instanceof TileEntityFurnace || tile instanceof FurnaceLogic)) {
                furnace = new WeakReference<>(inv);
                continue;
            } else if (!tinkerTable && tile instanceof ToolStationLogic) {
                tinkerTable = true;
                continue;
            }

            if (tile instanceof ISidedInventory sidedIvn
                    && sidedIvn.getAccessibleSlotsFromSide(dir.getOpposite().ordinal()).length == 0)
                continue;

            if (chest == null && inv.isUseableByPlayer(inventoryplayer.player)) {
                chest = new WeakReference<>(inv);
                chestDirection = dir;
                invColumns = 6;
                chestSize = tile instanceof ISidedInventory sidedIvn
                        ? sidedIvn.getAccessibleSlotsFromSide(dir.getOpposite().ordinal()).length
                        : inv.getSizeInventory();

                if (tile instanceof TileEntityChest tileChest) {
                    if (tileChest.adjacentChestZPos != null) {
                        doubleChest = new WeakReference<>(tileChest.adjacentChestZPos);
                        doubleFirst = false;
                    } else if (tileChest.adjacentChestZNeg != null) {
                        doubleChest = new WeakReference<>(tileChest.adjacentChestZNeg);
                        doubleFirst = true;
                    } else if (tileChest.adjacentChestXPos != null) {
                        doubleChest = new WeakReference<>(tileChest.adjacentChestXPos);
                        doubleFirst = false;
                    } else if (tileChest.adjacentChestXNeg != null) {
                        doubleChest = new WeakReference<>(tileChest.adjacentChestXNeg);
                        doubleFirst = true;
                    }
                }
                slotCount = chestSize * (doubleChest != null ? 2 : 1);
                invRows = (int) Math.ceil((double) slotCount / invColumns);
            }
        }

        this.inventories = new WeakReference[] { this.chest, this.doubleChest, this.patternChest, this.furnace };

        return new CraftingStationContainer(inventoryplayer, this, x, y, z);
    }

    private boolean isBlacklisted(Class<? extends TileEntity> clazz) {
        return PHConstruct.craftingStationBlacklist.contains(clazz.getName());
    }

    public boolean isDoubleChest() {
        return this.doubleChest != null;
    }

    public IInventory getFirstInventory() {
        if (doubleFirst && doubleChest != null) {
            return doubleChest.get();
        } else {
            return chest != null ? chest.get() : null;
        }
    }

    public IInventory getSecondInventory() {
        if (!isDoubleChest()) return null;

        if (doubleFirst) {
            return chest.get();
        } else {
            return doubleChest == null ? null : doubleChest.get();
        }
    }

    @Override
    protected String getDefaultName() {
        return "crafters.CraftingStation";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return true;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static boolean isUseableByPlayer(EntityPlayer player, WeakReference[] inventories) {
        for (WeakReference<IInventory> ref : inventories) {
            if (ref != null) {
                IInventory inv = ref.get();
                if (inv != null && !inv.isUseableByPlayer(player)) return false;
            }
        }

        return true;
    }

    @SuppressWarnings("rawtypes")
    public WeakReference[] getInventories() {
        return this.inventories;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int var1) {
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
        return getDefaultName();
    }

    @Override
    public void openInventory() {
        // TODO Auto-generated method stub

    }

    @Override
    public void closeInventory() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean canUpdate() {
        return false;
    }
}
