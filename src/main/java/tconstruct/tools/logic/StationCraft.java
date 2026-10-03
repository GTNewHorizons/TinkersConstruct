package tconstruct.tools.logic;

import java.util.Arrays;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import mantle.blocks.abstracts.InventoryLogic;
import tconstruct.library.crafting.ModifyBuilder;
import tconstruct.library.crafting.ToolBuilder;
import tconstruct.library.modifier.IModifyable;
import tconstruct.library.tools.BuildInput;
import tconstruct.library.tools.ToolCore;

/** What a Tool Station or Tool Forge builds from its slots, and what a craft takes from them. */
public abstract class StationCraft extends InventoryLogic {

    private ToolCore partsTool;
    private Item[] toolParts;

    /** Client: the server's tab, its build cells and whether it built nothing, as of its last description packet. */
    private ToolCore syncedTab;
    private ItemStack[] syncedCells;
    private boolean syncedNothing;

    StationCraft(int slots) {
        super(slots);
    }

    public abstract ToolCore getSelectedTool();

    protected abstract ItemStack tryRenameTool(ItemStack output, String name);

    /** How many cells a build here reads: head, handle and accessory; the forge adds the extra. */
    public int buildSlotCount() {
        return 3;
    }

    /** Whether a build with these parts reads this slot; the screen, slots, builder and craft all go by it. */
    public boolean isPartSlot(Item[] parts, int slot) {
        return slot >= 1 && slot <= buildSlotCount() && slot <= parts.length && parts[slot - 1] != null;
    }

    /** Cached for the last tool asked: getToolBuildComponents allocates per call and the screen asks every frame. */
    public Item[] partsOf(ToolCore tool) {
        if (tool != partsTool) {
            partsTool = tool;
            toolParts = tool.getToolBuildComponents();
        }
        return toolParts;
    }

    /** What a build of this tool hands the builder, indexed by BuildInput. */
    public ItemStack[] buildInputs(ToolCore tool) {
        Item[] parts = partsOf(tool);
        ItemStack[] in = new ItemStack[BuildInput.VALUES.length];
        for (BuildInput input : BuildInput.VALUES) {
            if (isPartSlot(parts, input.cell())) in[input.ordinal()] = inventory[input.cell()];
        }
        // the builder reads the extra only beside an accessory
        if (in[BuildInput.ACCESSORY.ordinal()] == null) in[BuildInput.EXTRA.ordinal()] = null;
        return in;
    }

    void rememberServerBuild() {
        syncedTab = getSelectedTool();
        syncedNothing = inventory[0] == null;
        if (syncedTab == null) return;
        ItemStack[] cells = buildInputs(syncedTab);
        syncedCells = new ItemStack[cells.length];
        for (int i = 0; i < cells.length; i++) syncedCells[i] = ItemStack.copyItemStack(cells[i]);
    }

    /** Client: the server built nothing from the parts these cells hold now; false until its answer for them comes. */
    public boolean serverBuildsNothing(ToolCore tab) {
        return tab != null && tab == syncedTab && syncedNothing && sameParts(buildInputs(tab), syncedCells);
    }

    /** By item, damage and tags: a count does not change what a build makes. */
    private static boolean sameParts(ItemStack[] cells, ItemStack[] others) {
        for (int i = 0; i < cells.length; i++) {
            ItemStack a = cells[i], b = others[i];
            if (a == null && b == null) continue;
            if (a == null || b == null || a.getItem() != b.getItem() || a.getItemDamage() != b.getItemDamage())
                return false;
            if (!ItemStack.areItemStackTagsEqual(a, b)) return false;
        }
        return true;
    }

    /** A tool is repaired, modified or renamed; any other item is only named. */
    ItemStack modifyTool(String name) {
        ItemStack tool = inventory[1];
        if (tool == null) return null;
        if (!(tool.getItem() instanceof IModifyable)) return name.equals("") ? null : tryRenameTool(null, name);
        ItemStack[] materials = Arrays.copyOfRange(inventory, 2, inventory.length);
        boolean any = false;
        for (ItemStack material : materials) any |= material != null;
        ItemStack output = any ? ModifyBuilder.instance.modifyItem(tool, materials) : tool.copy();
        return name.equals("") ? output : tryRenameTool(output, name);
    }

    ItemStack buildFromParts(String name) {
        ItemStack[] in = buildInputs(getSelectedTool());
        ItemStack tool = ToolBuilder.instance.buildTool(in[0], in[1], in[2], in[3], name);
        return tool != null && tool.getItem() == getSelectedTool() ? tool : null;
    }

    /** Takes what this output's craft used; true when it sounds: a build, or a modify that took a material. */
    public boolean takeCraftInputs(ItemStack output) {
        if (getSelectedTool() != null) {
            ItemStack[] in = buildInputs(getSelectedTool());
            for (BuildInput input : BuildInput.VALUES) if (in[input.ordinal()] != null) decrStackSize(input.cell(), 1);
            return true;
        }
        if (!(output.getItem() instanceof IModifyable)) {
            takeNamingInputs(output.stackSize);
            return false;
        }
        NBTTagCompound tags = output.getTagCompound().getCompoundTag(((IModifyable) output.getItem()).getBaseTagName());
        int[] toRemove = tags.hasKey("ToRemove") ? tags.getIntArray("ToRemove") : null;
        int next = 0;
        boolean took = false;
        for (int slot = 2; slot < inventory.length; slot++) {
            if (inventory[slot] == null) continue;
            decrStackSize(slot, toRemove == null || next >= toRemove.length ? 1 : toRemove[next++]);
            took = true;
        }
        tags.removeTag("ToRemove");
        decrStackSize(1, inventory[1].stackSize);
        return took;
    }

    /** The named items taken from the center, and one name tag if the station holds one. */
    private void takeNamingInputs(int taken) {
        // a right-click or a drop takes part of the output; the rest stays in the center
        if (inventory[1] != null) decrStackSize(1, Math.min(taken, inventory[1].stackSize));
        for (int slot = 2; slot < inventory.length; slot++) {
            if (inventory[slot] != null && inventory[slot].getItem() == Items.name_tag) {
                decrStackSize(slot, 1);
                return;
            }
        }
    }
}
