package tconstruct.util.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import mantle.common.network.AbstractPacket;
import tconstruct.smeltery.inventory.SmelteryContainer;

/** Heat-bar levels and aggregate fuel, sent only to an open smeltery container. */
public class SmelteryGuiPacket extends AbstractPacket {

    private int windowId;
    private int slots;
    private int[] heatRuns;
    private FluidStack fuel;
    private int fuelCapacity;

    public SmelteryGuiPacket() {}

    public SmelteryGuiPacket(SmelteryContainer container, int[] heatRuns, FluidStack fuel, int fuelCapacity) {
        windowId = container.windowId;
        slots = container.smelterySize;
        this.heatRuns = heatRuns.clone();
        this.fuel = fuel.copy();
        this.fuelCapacity = fuelCapacity;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
        buffer.writeInt(windowId);
        buffer.writeInt(slots);
        buffer.writeInt(heatRuns.length);
        for (int run : heatRuns) buffer.writeInt(run);
        ByteBufUtils.writeTag(buffer, fuel.writeToNBT(new NBTTagCompound()));
        buffer.writeInt(fuelCapacity);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
        windowId = buffer.readInt();
        slots = buffer.readInt();
        int length = buffer.readInt();
        if (slots < 0 || length < 0 || length > slots || length > buffer.readableBytes() / Integer.BYTES) {
            throw new IllegalArgumentException("Invalid smeltery heat run count");
        }
        heatRuns = new int[length];
        for (int i = 0; i < heatRuns.length; i++) heatRuns[i] = buffer.readInt();
        fuel = FluidStack.loadFluidStackFromNBT(ByteBufUtils.readTag(buffer));
        fuelCapacity = buffer.readInt();
    }

    @Override
    public void handleClientSide(EntityPlayer player) {
        if (player.openContainer instanceof SmelteryContainer container && container.windowId == windowId
                && container.smelterySize == slots) {
            container.updateGuiState(heatRuns, fuel, fuelCapacity);
        }
    }

    @Override
    public void handleServerSide(EntityPlayer player) {}
}
