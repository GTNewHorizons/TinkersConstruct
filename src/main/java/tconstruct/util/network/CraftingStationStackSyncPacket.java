package tconstruct.util.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import mantle.common.network.AbstractPacket;
import tconstruct.tools.inventory.CraftingStationContainer;

public class CraftingStationStackSyncPacket extends AbstractPacket {

    private static final int FLAG_HAS_STACK = 1;
    private static final int FLAG_HAS_TAG = 2;
    private static final int MAX_UPDATE_COUNT = 255;

    private int windowId;
    private int[] slotIds;
    private ItemStack[] stacks;

    public CraftingStationStackSyncPacket() {}

    public CraftingStationStackSyncPacket(int windowId, int[] slotIds, ItemStack[] stacks) {
        if (slotIds == null || stacks == null || slotIds.length != stacks.length || slotIds.length > MAX_UPDATE_COUNT) {
            throw new IllegalArgumentException("Crafting station update arrays must have the same size up to 255");
        }

        this.windowId = windowId;
        this.slotIds = slotIds.clone();
        this.stacks = copyStacks(stacks);
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
        buffer.writeByte(windowId);
        buffer.writeByte(slotIds.length);

        for (int i = 0; i < slotIds.length; i++) {
            buffer.writeShort(slotIds[i]);

            ItemStack stack = stacks[i];
            if (stack == null || stack.stackSize <= 0 || stack.getItem() == null) {
                buffer.writeByte(0);
                continue;
            }

            int flags = FLAG_HAS_STACK;
            if (stack.hasTagCompound()) flags |= FLAG_HAS_TAG;
            buffer.writeByte(flags);
            ByteBufUtils.writeVarInt(buffer, Item.getIdFromItem(stack.getItem()), 3);
            ByteBufUtils.writeVarInt(buffer, stack.stackSize, 5);
            buffer.writeShort(stack.getItemDamage());
            if ((flags & FLAG_HAS_TAG) != 0) ByteBufUtils.writeTag(buffer, stack.getTagCompound());
        }
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
        windowId = buffer.readByte();
        int updateCount = buffer.readUnsignedByte();
        slotIds = new int[updateCount];
        stacks = new ItemStack[updateCount];

        for (int i = 0; i < updateCount; i++) {
            slotIds[i] = buffer.readShort();
            int flags = buffer.readUnsignedByte();
            if ((flags & FLAG_HAS_STACK) == 0) continue;

            int itemId = ByteBufUtils.readVarInt(buffer, 3);
            int stackSize = ByteBufUtils.readVarInt(buffer, 5);
            int itemDamage = buffer.readShort();
            Item item = Item.getItemById(itemId);
            if (item == null || stackSize <= 0) {
                if ((flags & FLAG_HAS_TAG) != 0) ByteBufUtils.readTag(buffer);
                continue;
            }

            ItemStack stack = new ItemStack(item, stackSize, itemDamage);
            if ((flags & FLAG_HAS_TAG) != 0) stack.setTagCompound(ByteBufUtils.readTag(buffer));
            stacks[i] = stack;
        }
    }

    @Override
    public void handleClientSide(EntityPlayer player) {
        if (player == null || !(player.openContainer instanceof CraftingStationContainer container)
                || container.windowId != windowId) {
            return;
        }

        for (int i = 0; i < slotIds.length; i++) {
            if (slotIds[i] == -1) {
                player.inventory.setItemStack(stacks[i]);
                continue;
            }

            if (slotIds[i] < 0 || slotIds[i] >= container.inventorySlots.size()) continue;
            Slot slot = container.getSlot(slotIds[i]);
            if (slot != null) slot.putStack(stacks[i]);
        }
    }

    @Override
    public void handleServerSide(EntityPlayer player) {}

    private static ItemStack[] copyStacks(ItemStack[] source) {
        ItemStack[] copies = new ItemStack[source.length];
        for (int i = 0; i < source.length; i++) {
            copies[i] = source[i] == null ? null : source[i].copy();
        }
        return copies;
    }
}
