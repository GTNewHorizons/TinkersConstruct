package tconstruct.world.entity;

import java.util.ArrayList;

import javax.annotation.Nonnull;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

import com.kuba6000.mobsinfo.api.IMobInfoProvider;
import com.kuba6000.mobsinfo.api.MobDrop;

import cpw.mods.fml.common.Optional;

@Optional.Interface(iface = "com.kuba6000.mobsinfo.api.IMobInfoProvider", modid = "mobsinfo")
public abstract class SlimeBase extends EntitySlime implements IMob, IMobInfoProvider {

    /**
     * the time between each jump of the slime, used for counting
     */
    protected int slimeJumpDelay = 0;

    public SlimeBase(World world) {
        super(world);
        initializeSlime();
    }

    @Override
    public void setSlimeSize(int size) {
        this.dataWatcher.updateObject(16, (byte) size);
        this.setSize(0.6F * (float) size, 0.6F * (float) size);
        this.setPosition(this.posX, this.posY, this.posZ);
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(this.getMaxHealthForSize());
        this.setHealth(this.getMaxHealthForSize());

        this.jumpMovementFactor = 0.004F * size + 0.01F;

        this.experienceValue = size + (int) Math.pow(2, size);
    }

    /**
     * returns the health for the slime depending on its size
     */
    protected float getMaxHealthForSize() {
        int i = this.getSlimeSize();
        if (i == 1) return 4;

        return (float) Math.min(i * i + 8, 49);
    }

    /**
     * Gets the amount of time the slime needs to wait between jumps.
     */
    @Override
    protected int getJumpDelay() {
        return this.rand.nextInt(120) + 40;
    }

    /**
     * Return an instance of the implementing entity here. Used for the slime splitting on death.
     */
    protected abstract SlimeBase createInstance(World world);

    protected void initializeSlime() {
        int offset = this.rand.nextInt(299);
        if (offset < 149) offset = 1;
        else if (offset < 298) offset = 2;
        else offset = 3;
        int size = 1 << offset;
        this.yOffset = 0.0F;
        this.slimeJumpDelay = this.rand.nextInt(120) + 40;
        this.setSlimeSize(size);
    }

    @Override
    public void jump() {
        this.motionY = 0.05 * getSlimeSize() + 0.37;

        if (this.isPotionActive(Potion.jump)) {
            this.motionY += (float) (this.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1F;
        }

        if (this.isSprinting()) {
            float f = this.rotationYaw * 0.017453292F;
            this.motionX -= MathHelper.sin(f) * 0.2F;
            this.motionZ += MathHelper.cos(f) * 0.2F;
        }

        if (!(this instanceof IBossDisplayData) && this.getBrightness(1.0F) > 0.9F
                && rand.nextInt(5) == 0
                && this.worldObj.canBlockSeeTheSky(
                        MathHelper.floor_double(this.posX),
                        MathHelper.floor_double(this.posY),
                        MathHelper.floor_double(this.posZ))) {
            int size = this.getSlimeSize() - 1;
            if (size <= 0) this.kill();
            else this.setSlimeSize(size);
        }

        this.isAirBorne = true;
        ForgeHooks.onLivingJump(this);
    }

    @Override
    protected void updateEntityActionState() {
        this.despawnEntity();

        EntityPlayer entityplayer = this.worldObj.getClosestVulnerablePlayerToEntity(this, 16.0D);

        if (entityplayer != null) {
            this.faceEntity(entityplayer, 10.0F, 20.0F);
        } else if (this.onGround && this.slimeJumpDelay == 1) {
            this.rotationYaw = this.rotationYaw + rand.nextFloat() * 180 - 90;
            if (rotationYaw > 360) rotationYaw -= 360;
            if (rotationYaw < 0) rotationYaw += 360;
        }

        if (this.onGround && this.slimeJumpDelay-- <= 0) {
            this.slimeJumpDelay = this.getJumpDelay();

            if (entityplayer != null) {
                this.slimeJumpDelay /= 12;
            }

            this.isJumping = true;

            if (this.makesSoundOnJump()) {
                this.playSound(
                        this.getJumpSound(),
                        this.getSoundVolume(),
                        ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F) * 0.8F);
            }

            this.moveStrafing = 1.0F - this.rand.nextFloat() * 2.0F;
            this.moveForward = (float) this.getSlimeSize();
        } else {
            this.isJumping = false;

            if (this.onGround) {
                this.moveStrafing = this.moveForward = 0.0F;
            }
        }
    }

    @Override
    public void setDead() {
        int size = this.getSlimeSize();

        if (!this.worldObj.isRemote && size > 1 && this.getHealth() <= 0 && size < 8) {
            float f = (-0.5F) * (float) size / 4.0F;
            float f1 = (-0.5F) * (float) size / 4.0F;
            SlimeBase entityslime = this.createInstance(this.worldObj);
            entityslime.setSlimeSize(size / 2);
            entityslime.setLocationAndAngles(
                    this.posX + (double) f,
                    this.posY + 0.5D,
                    this.posZ + (double) f1,
                    this.rand.nextFloat() * 360.0F,
                    0.0F);
            this.worldObj.spawnEntityInWorld(entityslime);
        }

        this.isDead = true;
    }

    // Drops items depending on slime size
    @Override
    protected void dropFewItems(boolean par1, int par2) {
        // ANY CHANGE MADE IN HERE MUST ALSO BE MADE IN provideDropsInformation!
        int size = this.getSlimeSize();
        Item j = this.getDropItem();

        if (j != null && (rand.nextInt(2) == 0) || size >= 8) {
            int k = rand.nextInt(3) + rand.nextInt(this.getSlimeSize());

            if (par2 > 0) {
                k += this.rand.nextInt(par2 + 1);
            }

            for (int l = 0; l < k; ++l) {
                this.entityDropItem(new ItemStack(j), 1);
            }
        }
    }

    @Optional.Method(modid = "mobsinfo")
    @Override
    public void provideDropsInformation(@Nonnull ArrayList<MobDrop> drops) {
        Item j = this.getDropItem();
        if (j != null) {
            drops.add(MobDrop.create(j).withChance(MobDrop.getChanceBasedOnFromTo(0, 9) * 0.5d).withLooting());
        }
    }

    @Override
    protected void fall(float distance) {
        // we call the event for proper behaviour with other stuff
        ForgeHooks.onLivingFall(this, distance);
        // but don't calc damage or anything
    }

    @Override
    public boolean getCanSpawnHere() {
        // needs free space
        if (!this.worldObj.checkNoEntityCollision(this.boundingBox)
                || !this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox).isEmpty()
                || this.worldObj.isAnyLiquid(this.boundingBox))
            return false;

        int x = MathHelper.floor_double(this.posX);
        int y = MathHelper.floor_double(this.boundingBox.minY);
        int z = MathHelper.floor_double(this.posZ);

        if (this.worldObj.getSavedLightValue(EnumSkyBlock.Sky, x, y, z) > this.rand.nextInt(32)) {
            return false;
        } else {
            int light = this.worldObj.getBlockLightValue(x, y, z);

            if (this.worldObj.isThundering()) {
                int i1 = this.worldObj.skylightSubtracted;
                this.worldObj.skylightSubtracted = 10;
                light = this.worldObj.getBlockLightValue(x, y, z);
                this.worldObj.skylightSubtracted = i1;
            }

            return light <= this.rand.nextInt(8);
        }
    }

    // slime jockeys!
    @Override
    public double getMountedYOffset() {
        return this.height * 0.3;
    }

    /**
     * Returns the volume for the sounds this mob makes.
     */
    @Override
    protected float getSoundVolume() {
        return Math.min(0.05F * (float) this.getSlimeSize(), 0.3f);
    }

}
