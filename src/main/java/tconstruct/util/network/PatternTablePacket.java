package tconstruct.util.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import mantle.common.network.AbstractPacket;
import tconstruct.tools.inventory.PatternShaperChestContainer;
import tconstruct.tools.inventory.PatternShaperContainer;
import tconstruct.tools.logic.StencilTableLogic;

public class PatternTablePacket extends AbstractPacket {

    int x, y, z;
    ItemStack contents;

    public PatternTablePacket() {}

    public PatternTablePacket(int x, int y, int z, ItemStack contents) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.contents = contents;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
        buffer.writeInt(x);
        buffer.writeInt(y);
        buffer.writeInt(z);
        ByteBufUtils.writeItemStack(buffer, contents);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
        x = buffer.readInt();
        y = buffer.readInt();
        z = buffer.readInt();
        contents = ByteBufUtils.readItemStack(buffer);
    }

    @Override
    public void handleClientSide(EntityPlayer player) {}

    @Override
    public void handleServerSide(EntityPlayer player) {
        if (player.openContainer instanceof PatternShaperContainer
                || player.openContainer instanceof PatternShaperChestContainer) {
            StencilTableLogic logic = null;
            if (player.openContainer instanceof PatternShaperContainer psc) {
                logic = psc.logic;
            } else if (player.openContainer instanceof PatternShaperChestContainer pscc) {
                logic = pscc.logic;
            }
            if (logic != null && logic.xCoord == this.x && logic.yCoord == this.y && logic.zCoord == this.z)
                logic.setSelectedPattern(contents);
        }
    }
}
