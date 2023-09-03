package tconstruct.armor.items.slime;

import java.util.List;
import java.util.Locale;

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
import tconstruct.library.SlimeBounceHandler;
import tconstruct.library.TConstructRegistry;
import tconstruct.util.network.MovementUpdatePacket;

public class ItemSlimeSling extends Item {

    public ItemSlimeSling() {
        this.setMaxStackSize(1);
        this.setMaxDamage(100);
        this.setUnlocalizedName(getUnlocalizedName());
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
        return 50000;
    }

    // sling logic
    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityPlayer player, int timeLeft) {
        // has to be on ground to do something
        if (!player.onGround) {
            return;
        }

        // copy chargeup code from bow \o/
        int i = this.getMaxItemUseDuration(stack) - timeLeft;

        float f = i / 5.0F;
        f = Math.min((f * f + f * 2.0F) * (4.0F / 3.0F), 6.0F);

        // check if player was targeting a block
        MovingObjectPosition mop = getMovingObjectPositionFromPlayer(world, player, false);

        if (mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            // we fling the inverted player look vector
            Vec3 vec = player.getLookVec().normalize();

            player.addVelocity(vec.xCoord * -f, vec.yCoord * -f / 3f, vec.zCoord * -f);

            if (player instanceof EntityPlayerMP) {
                EntityPlayerMP playerMP = (EntityPlayerMP) player;
                TConstruct.packetPipeline.sendTo(new MovementUpdatePacket(player), playerMP);
                playerMP.playerNetServerHandler.sendPacket(new S12PacketEntityVelocity(player));
            }
            player.playSound(resource("slimesling"), 1f, 1f);
            SlimeBounceHandler.addBounceHandler(player);
            stack.damageItem(1, player);
            // TinkerCommons.potionSlimeBounce.apply(player);
        }
    }

    private String resource(String res) {
        return String.format("%s:%s", "tinker", res);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4) {
        list.add(StatCollector.translateToLocal("gadgets.slimesling.tooltip1"));
        list.add(
                player.onGround ? StatCollector.translateToLocal("gadgets.slimesling.tooltip2")
                        : StatCollector.translateToLocal("gadgets.slimesling.tooltip3"));
    }

    @Override
    public String getUnlocalizedName() {
        return String.format("%s.%s", TConstruct.modID, "slimesling".toLowerCase(Locale.US));
    }
}
