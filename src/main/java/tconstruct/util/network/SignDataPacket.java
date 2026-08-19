package tconstruct.util.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import mantle.common.network.AbstractPacket;
import tconstruct.tools.logic.BattlesignLogic;

public class SignDataPacket extends AbstractPacket {

    private static final int MAX_LINE_LENGTH = 256;
    private static final double MAX_EDIT_DISTANCE_SQ = 64D;

    private int x, y, z;
    private String[] text;

    public SignDataPacket() {}

    public SignDataPacket(int x, int y, int z, String[] text) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
        buffer.writeInt(x);
        buffer.writeInt(y);
        buffer.writeInt(z);
        buffer.writeInt(text.length);
        for (String line : text) {
            ByteBufUtils.writeUTF8String(buffer, line);
        }
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
        x = buffer.readInt();
        y = buffer.readInt();
        z = buffer.readInt();
        int lineCount = Math.max(0, Math.min(buffer.readInt(), BattlesignLogic.LINE_COUNT));
        text = new String[lineCount];
        for (int i = 0; i < lineCount; i++) {
            String line = ByteBufUtils.readUTF8String(buffer);
            text[i] = line.length() > MAX_LINE_LENGTH ? line.substring(0, MAX_LINE_LENGTH) : line;
        }
    }

    @Override
    public void handleClientSide(EntityPlayer player) {}

    @Override
    public void handleServerSide(EntityPlayer player) {
        if (!player.worldObj.blockExists(x, y, z)) {
            return;
        }

        if (player.getDistanceSq(x + 0.5D, y + 0.5D, z + 0.5D) > MAX_EDIT_DISTANCE_SQ) {
            return;
        }

        TileEntity te = player.worldObj.getTileEntity(x, y, z);

        if (!(te instanceof BattlesignLogic)) {
            return;
        }

        ((BattlesignLogic) te).setText(text);
        te.markDirty();
        player.worldObj.markBlockForUpdate(x, y, z);
    }
}
