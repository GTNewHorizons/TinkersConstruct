package tconstruct.library.crafting;

import java.util.LinkedList;

import net.minecraft.init.Items;
import net.minecraft.item.Item;

import tconstruct.library.TConstructRegistry;
import tconstruct.library.tools.ToolCore;

/*
 * Small class for checking if a particular tool combination is this one
 */

public class ToolRecipe {

    protected LinkedList<Item> headList = new LinkedList<>();
    protected LinkedList<Item> handleList = new LinkedList<>();
    protected LinkedList<Item> accessoryList = new LinkedList<>();
    protected LinkedList<Item> extraList = new LinkedList<>();
    protected ToolCore result;
    protected Item toolRod = TConstructRegistry.getItem("toolRod");

    /*
     * public ToolRecipe(Item head, ToolCore tool) { this(head, TContent.toolRod, null, null, tool); }
     */

    public ToolRecipe(Item head, Item handle, ToolCore tool) {
        this(head, handle, null, null, tool);
    }

    public ToolRecipe(Item head, Item handle, Item accessory, ToolCore tool) {
        this(head, handle, accessory, null, tool);
    }

    public ToolRecipe(Item head, Item handle, Item accessory, Item extra, ToolCore tool) {
        this.headList.add(head);
        this.handleList.add(handle);
        if (accessory != null) this.accessoryList.add(accessory);
        if (extra != null) this.extraList.add(extra);
        result = tool;
    }

    public void addHeadItem(Item head) {
        this.headList.add(head);
    }

    public void addHandleItem(Item head) {
        this.handleList.add(head);
    }

    public void addAccessoryItem(Item head) {
        this.accessoryList.add(head);
    }

    public void addExtraItem(Item head) {
        this.extraList.add(head);
    }

    public boolean validHead(Item input) {
        for (Item part : headList) {
            if (part == input) return true;
        }
        return false;
    }

    public boolean validHandle(Item input) {
        for (Item part : handleList) {
            if (part == input) return true;
            if (toolRod != null && part == toolRod && (input == Items.stick || input == Items.bone)) return true;
        }
        return false;
    }

    public boolean validAccessory(Item input) {
        if (input == null) {
            return accessoryList.size() < 1;
        }
        for (Item part : accessoryList) {
            if (part == input) return true;
            if (toolRod != null && part == toolRod && (input == Items.stick || input == Items.bone)) return true;
        }
        return false;
    }

    public boolean validExtra(Item input) {
        if (input == null) {
            return extraList.size() < 1;
        }
        for (Item part : extraList) {
            if (part == input) return true;
            if (toolRod != null && part == toolRod && (input == Items.stick || input == Items.bone)) return true;
        }
        return false;
    }

    public ToolCore getType() {
        return result;
    }
}
