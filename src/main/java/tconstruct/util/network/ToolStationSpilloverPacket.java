package tconstruct.util.network;

import net.minecraft.entity.player.EntityPlayer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import mantle.common.network.AbstractPacket;
import tconstruct.tools.inventory.ToolStationContainer;
import tconstruct.tools.logic.ToolStationLogic;

/** The station's "fill every tier" toggle, sent from the GUI to the server. */
public class ToolStationSpilloverPacket extends AbstractPacket {

    private int x, y, z;
    private boolean on;

    public ToolStationSpilloverPacket() {}

    public ToolStationSpilloverPacket(int x, int y, int z, boolean on) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.on = on;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
        buffer.writeInt(x);
        buffer.writeInt(y);
        buffer.writeInt(z);
        buffer.writeBoolean(on);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
        x = buffer.readInt();
        y = buffer.readInt();
        z = buffer.readInt();
        on = buffer.readBoolean();
    }

    @Override
    public void handleClientSide(EntityPlayer player) {}

    @Override
    public void handleServerSide(EntityPlayer player) {
        if (player.openContainer instanceof ToolStationContainer) {
            ToolStationContainer container = (ToolStationContainer) player.openContainer;
            ToolStationLogic logic = container.logic;
            if (logic != null && logic.xCoord == x && logic.yCoord == y && logic.zCoord == z) {
                logic.setTierSpillover(on);
            }
        }
    }
}
