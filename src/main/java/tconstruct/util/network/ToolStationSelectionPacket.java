package tconstruct.util.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraftforge.common.ForgeHooks;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import mantle.common.network.AbstractPacket;
import tconstruct.library.tools.ToolCore;
import tconstruct.tools.inventory.ToolStationContainer;

/** The tab of an open station or forge screen: a tab click from the client, the opening tab from the server. */
public class ToolStationSelectionPacket extends AbstractPacket {

    private int toolId;

    public ToolStationSelectionPacket() {}

    public ToolStationSelectionPacket(ToolCore tool) {
        this.toolId = tool == null ? -1 : Item.getIdFromItem(tool);
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
        buffer.writeInt(toolId);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
        toolId = buffer.readInt();
    }

    @Override
    public void handleClientSide(EntityPlayer player) {
        if (player.openContainer instanceof ToolStationContainer container) container.selectOpeningTab(tool());
    }

    @Override
    public void handleServerSide(EntityPlayer player) {
        // canInteractWith compares coordinates, not worlds: a player who just changed dimension at that spot passes
        if (player.openContainer instanceof ToolStationContainer container
                && player.worldObj == container.logic.getWorldObj()
                && player.isEntityAlive()
                && ForgeHooks.canInteractWith(player, container)) {
            container.selectTool(tool());
            // the client already handed the refused stacks back with no click for the server to confirm, so the whole
            // window is resent, as vanilla does after a click it refuses
            container.detectAndSendChanges();
            if (player instanceof EntityPlayerMP mp) {
                mp.sendContainerAndContentsToPlayer(container, container.getInventory());
            }
        } else if (player instanceof EntityPlayerMP mp) {
            // the client already handed parts back for a switch that does not happen
            mp.sendContainerToPlayer(player.inventoryContainer);
            if (player.openContainer instanceof ToolStationContainer open) mp.sendContainerToPlayer(open);
        }
    }

    private ToolCore tool() {
        Item item = toolId < 0 ? null : Item.getItemById(toolId);
        return item instanceof ToolCore ? (ToolCore) item : null;
    }
}
