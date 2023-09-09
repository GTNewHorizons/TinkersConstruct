package tconstruct.gadgets.item;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import tconstruct.TConstruct;
import tconstruct.gadgets.TinkerGadgets;
import tconstruct.library.SlimeBounceHandler;
import tconstruct.library.TConstructRegistry;
import tconstruct.util.network.MovementUpdatePacket;

public class ItemSlimeSling extends Item {

    public ItemSlimeSling() {
        this.setMaxStackSize(1);
        this.setCreativeTab(TConstructRegistry.gadgetsTab);
    }

    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     */
    @Override
    public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn) {
        playerIn.setItemInUse(itemStackIn, this.getMaxItemUseDuration(itemStackIn));
        return itemStackIn;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int meta) {
        return this.itemIcon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconRegister) {
        this.itemIcon = iconRegister.registerIcon("tinker:gadgets/slimesling");
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.bow;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 72000;
    }

    // sling logic
    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityPlayer player, int timeLeft) {
        // has to be on ground to do something
        if (!player.onGround) {
            return;
        }

        // check if player was targeting a block
        MovingObjectPosition mop = getMovingObjectPositionFromPlayer(world, player, false);

        if (mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            // we fling the inverted player look vector
            Vec3 vec = player.getLookVec().normalize();

            double itemUseDuration = (this.getMaxItemUseDuration(stack) - timeLeft);
            double power = Math.min(itemUseDuration / 20D, 1.8D);
            double height = Math.max(vec.yCoord * power * 2, -2D);

            player.addVelocity(vec.xCoord * -power, -height, vec.zCoord * -power);

            if (player instanceof EntityPlayerMP) {
                EntityPlayerMP playerMP = (EntityPlayerMP) player;
                TConstruct.packetPipeline.sendTo(new MovementUpdatePacket(player), playerMP);
                playerMP.playerNetServerHandler.sendPacket(new S12PacketEntityVelocity(player));
            }
            player.playSound(TinkerGadgets.resource("slimesling"), 1f, 1f);
            SlimeBounceHandler.addBounceHandler(player);
            stack.damageItem(1, player);
        }
    }

    @Override
    public boolean isDamageable() {
        return true;
    }

    @Override
    public int getMaxDamage() {
        return 100;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4) {
        list.add(StatCollector.translateToLocal("gadgets.slimesling.tooltip1"));
        list.add(
                player.onGround ? StatCollector.translateToLocal("gadgets.slimesling.tooltip2")
                        : StatCollector.translateToLocal("gadgets.slimesling.tooltip3"));
    }
}
