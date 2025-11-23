package tconstruct.util.network;

import net.minecraft.entity.player.EntityPlayer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import mantle.common.network.AbstractPacket;
import tconstruct.tools.inventory.CraftingStationContainer;

public class CraftingStationDumpPacket extends AbstractPacket {

    public CraftingStationDumpPacket() {}

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {}

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {}

    @Override
    public void handleClientSide(EntityPlayer player) {}

    @Override
    public void handleServerSide(EntityPlayer player) {
        if (player.openContainer instanceof CraftingStationContainer) {
            CraftingStationContainer container = (CraftingStationContainer) player.openContainer;
            container.dumpCraftingGrid();
        }
    }
}
