package tconstruct.tools.logic;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import tconstruct.library.tools.ToolCore;

/** The tab each player left a station on. */
public class StationTabs {

    /** By player name, which the client and the server agree on in every login mode. */
    private final Map<String, ToolCore> tabs = new HashMap<>();

    /** The tab this player left the station on, null for Repair & Modify. */
    public ToolCore tabOf(EntityPlayer player) {
        return tabs.get(player.getCommandSenderName());
    }

    /** True when the entry changed; Repair & Modify drops it. */
    boolean rememberTab(EntityPlayer player, ToolCore tool) {
        String name = player.getCommandSenderName();
        return tool == null ? tabs.remove(name) != null : tabs.put(name, tool) != tool;
    }

    static ToolCore toolNamed(String name) {
        Object item = name.isEmpty() ? null : Item.itemRegistry.getObject(name);
        return item instanceof ToolCore ? (ToolCore) item : null;
    }

    void readFromNBT(NBTTagCompound tags) {
        tabs.clear();
        NBTTagList list = tags.getTagList("Tabs", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound entry = list.getCompoundTagAt(i);
            ToolCore tool = toolNamed(entry.getString("Tool"));
            if (tool != null) tabs.put(entry.getString("Player"), tool);
        }
    }

    void writeToNBT(NBTTagCompound tags) {
        NBTTagList list = new NBTTagList();
        for (Map.Entry<String, ToolCore> entry : tabs.entrySet()) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("Player", entry.getKey());
            tag.setString("Tool", Item.itemRegistry.getNameForObject(entry.getValue()));
            list.appendTag(tag);
        }
        if (list.tagCount() > 0) tags.setTag("Tabs", list);
    }
}
