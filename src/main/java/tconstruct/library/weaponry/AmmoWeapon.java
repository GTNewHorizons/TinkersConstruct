package tconstruct.library.weaponry;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.battlegear2.api.PlayerEventChild;
import mods.battlegear2.api.weapons.IBattlegearWeapon;
import tconstruct.tools.TinkerTools;
import tconstruct.weaponry.client.CrosshairType;

/**
 * Throwing weapons that utilize the ammo system on themselves. Throwing knifes etc.
 */
@Optional.InterfaceList({
        @Optional.Interface(modid = "battlegear2", iface = "mods.battlegear2.api.weapons.IBattlegearWeapon") })
public abstract class AmmoWeapon extends AmmoItem implements IBattlegearWeapon, IAccuracy, IWindup {

    public AmmoWeapon(int baseDamage, String name) {
        super(baseDamage, name);
    }

    @Override
    public EnumAction getItemUseAction(ItemStack par1ItemStack) {
        return EnumAction.none;
    } // we use custom animation renderiiing!

    @Override
    public int getMaxItemUseDuration(ItemStack par1ItemStack) {
        return 72000;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (getAmmoCount(stack) > 0) player.setItemInUse(stack, this.getMaxItemUseDuration(stack));
        return stack;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
            float clickX, float clickY, float clickZ) {
        return false;
    }

    /**
     * How long it takes to "ready" the weapon. To reach the point, where holding the right mouse button any longer
     * doesn't have an impact.
     */
    @Override
    public int getWindupTime(ItemStack itemStack) {
        return 0;
    }

    @Override
    public float getMinWindupProgress(ItemStack itemStack) {
        return 0;
    }

    public float getWindupProgress(ItemStack itemStack, int timeInUse) {
        float time = (float) timeInUse;
        float windup = getWindupTime(itemStack);
        if (time > windup) time = windup;

        return time / windup;
    }

    public float minAccuracy(ItemStack itemStack) {
        return 0.5f;
    }

    public float maxAccuracy(ItemStack itemStack) {
        return 0.5f;
    }

    @SideOnly(Side.CLIENT)
    public float getWindupProgress(ItemStack itemStack, EntityPlayer player) {
        // what are you doing!
        if (player.inventory.getCurrentItem() != itemStack) return 0f;

        // are we using it?
        if (player.getItemInUse() == null) return 0f;

        return getWindupProgress(itemStack, getMaxItemUseDuration(itemStack) - player.getItemInUseCount());
    }

    public float getAccuracy(ItemStack itemStack, int time) {
        float dif = minAccuracy(itemStack) - maxAccuracy(itemStack);

        return minAccuracy(itemStack) - dif * getWindupProgress(itemStack, time);
    }

    @Override
    public String[] getTraits() {
        return new String[] { "weapon", "thrown", "ammo", "windup" };
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityPlayer player, int durationLeft) {
        int time = this.getMaxItemUseDuration(stack) - durationLeft;
        if (getWindupProgress(stack, time) >= getMinWindupProgress(stack)) launchProjectile(stack, world, player, time);
    }

    protected void launchProjectile(ItemStack stack, World world, EntityPlayer player, int time) {
        ItemStack reference = stack.copy();
        reference.stackSize = 1;
        // spawn projectile on server
        if (!world.isRemote) {
            ((IAmmo) reference.getItem()).setAmmo(1, reference);

            Entity projectile = createProjectile(reference, world, player, getAccuracy(stack, time), time);
            world.spawnEntityInWorld(projectile);
        }

        // reduce ammo
        if (!player.capabilities.isCreativeMode) {
            if (reference.hasTagCompound()) {
                if (random.nextInt(10)
                        < 10 - reference.getTagCompound().getCompoundTag("InfiTool").getInteger("Unbreaking")) {
                    this.consumeAmmo(1, stack);
                }
            }
        }
    }

    protected abstract Entity createProjectile(ItemStack reference, World world, EntityPlayer player, float accuracy,
            int time);

    /**
     * used for displaying the damage, return the value used for pseed in createProjectile/ProjectileBase constructor
     */
    public abstract float getProjectileSpeed();

    @SideOnly(Side.CLIENT)
    public CrosshairType getCrosshairType() {
        return CrosshairType.SQUARE;
    }

    @Override
    public boolean zoomOnWindup(ItemStack itemStack) {
        return false;
    }

    @Override
    public float getZoom(ItemStack itemStack) {
        return 1.0f;
    }

    /*---- Battlegear Support START ----*/

    @Override
    @Optional.Method(modid = "battlegear2")
    public boolean sheatheOnBack(ItemStack item) {
        return true;
    }

    @Override
    @Optional.Method(modid = "battlegear2")
    public boolean isOffhandHandDual(ItemStack off) {
        return true;
    }

    @Override
    @Optional.Method(modid = "battlegear2")
    public boolean offhandAttackEntity(PlayerEventChild.OffhandAttackEvent event, ItemStack mainhandItem,
            ItemStack offhandItem) {
        return true;
    }

    @Override
    @Optional.Method(modid = "battlegear2")
    public boolean offhandClickAir(PlayerInteractEvent event, ItemStack mainhandItem, ItemStack offhandItem) {
        onItemRightClick(offhandItem, event.entity.worldObj, event.entityPlayer);
        return true;
    }

    @Override
    @Optional.Method(modid = "battlegear2")
    public boolean offhandClickBlock(PlayerInteractEvent event, ItemStack mainhandItem, ItemStack offhandItem) {
        return true;
    }

    @Override
    @Optional.Method(modid = "battlegear2")
    public void performPassiveEffects(Side effectiveSide, ItemStack mainhandItem, ItemStack offhandItem) {
        // unused
    }

    @Override
    @Optional.Method(modid = "battlegear2")
    public boolean allowOffhand(ItemStack mainhand, ItemStack offhand) {
        if (offhand == null) return true;
        return (mainhand != null && mainhand.getItem() != TinkerTools.cleaver
                && mainhand.getItem() != TinkerTools.battleaxe)
                && (offhand.getItem() != TinkerTools.cleaver && offhand.getItem() != TinkerTools.battleaxe);
    }
}
