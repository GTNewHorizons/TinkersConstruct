package tconstruct.world.entity;

import java.util.ArrayList;

import javax.annotation.Nonnull;

import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import com.kuba6000.mobsinfo.api.MobDrop;

import cpw.mods.fml.common.Optional;
import tconstruct.armor.TinkerArmor;
import tconstruct.library.TConstructRegistry;
import tconstruct.library.crafting.ToolBuilder;
import tconstruct.library.tools.ToolCore;

public class KingBlueSlime extends SlimeBase implements IBossDisplayData {

    public KingBlueSlime(World world) {
        super(world);

        this.experienceValue = 500;

        // persistance required. this is used by named entities to not despawn, for example.
        func_110163_bv();
    }

    @Override
    protected String getSlimeParticle() {
        return "blueslime";
    }

    @Override
    protected SlimeBase createInstance(World world) {
        return new KingBlueSlime(world);
    }

    @Override
    protected void initializeSlime() {
        this.yOffset = 0.0F;
        this.slimeJumpDelay = this.rand.nextInt(120) + 40;
        this.setSlimeSize(8);
    }

    @Override
    protected float getMaxHealthForSize() {
        return 100;
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }

    @Override
    public void setDead() {
        if (!this.worldObj.isRemote && this.getHealth() <= 0) {
            // doesn't break into the next smaller one. let's spawn many tiny slimes instead! :D
            int c = 15 + this.rand.nextInt(6);
            for (; c > 0; c--) {
                BlueSlime entityslime = new BlueSlime(this.worldObj);
                entityslime.setSlimeSize(1);
                double r = rand.nextDouble() * Math.PI;
                double x = Math.cos(r);
                double z = Math.sin(r);
                entityslime.setLocationAndAngles(
                        this.posX - 1d + x,
                        this.posY + 0.5D,
                        this.posZ - 1d + z,
                        this.rand.nextFloat() * 360.0F,
                        0.0F);
                entityslime.motionX = x;
                entityslime.motionY = -0.5d - rand.nextDouble();
                entityslime.motionZ = z;
                this.worldObj.spawnEntityInWorld(entityslime);
            }
        }

        this.isDead = true;
    }

    @Override
    protected void dropFewItems(boolean par1, int par2) {
        // ANY CHANGE MADE IN HERE MUST ALSO BE MADE IN provideDropsInformation!
        super.dropFewItems(par1, par2);

        ToolCore tool = getValidTool();

        final ItemStack headStack = new ItemStack(tool.getHeadItem(), 1, 17);
        final ItemStack handleStack = new ItemStack(tool.getHandleItem(), 1, 17);
        final ItemStack accessoryStack = tool.getAccessoryItem() != null ? new ItemStack(tool.getAccessoryItem(), 1, 17)
                : null;
        final ItemStack extraStack = tool.getExtraItem() != null ? new ItemStack(tool.getExtraItem(), 1, 17) : null;

        String loc = "tool." + tool.getToolName().toLowerCase() + ".kingslime"; // special localization the same way as
                                                                                // materials
        String name;
        if (StatCollector.canTranslate(loc)) name = StatCollector.translateToLocal(loc);
        else name = StatCollector.translateToLocal("tool.kingslimeprefix") + " " + tool.getLocalizedToolName();

        ItemStack toolStack = ToolBuilder.instance.buildTool(headStack, handleStack, accessoryStack, extraStack, name);

        if (toolStack != null) {
            NBTTagCompound tags = toolStack.getTagCompound().getCompoundTag("InfiTool");
            tags.setInteger("Attack", 5 + tool.getDamageVsEntity(null));
            tags.setInteger("TotalDurability", 2500);
            tags.setInteger("BaseDurability", 2500);
            tags.setInteger("MiningSpeed", 1400);

            this.entityDropItem(toolStack, 0f);
            if (rand.nextInt(5) == 0) {
                ItemStack dropStack = new ItemStack(TinkerArmor.heartCanister, 1, 1);
                this.entityDropItem(dropStack, 0f);
            }
        }
    }

    @Optional.Method(modid = "mobsinfo")
    @Override
    public void provideDropsInformation(@Nonnull ArrayList<MobDrop> drops) {
        super.provideDropsInformation(drops);
        double chance = 1d / TConstructRegistry.tools.size();
        int validtools = 0;
        for (ToolCore tool : TConstructRegistry.tools) {
            final ItemStack headStack = new ItemStack(tool.getHeadItem(), 1, 17);
            final ItemStack handleStack = new ItemStack(tool.getHandleItem(), 1, 17);
            final ItemStack accessoryStack = tool.getAccessoryItem() != null
                    ? new ItemStack(tool.getAccessoryItem(), 1, 17)
                    : null;
            final ItemStack extraStack = tool.getExtraItem() != null ? new ItemStack(tool.getExtraItem(), 1, 17) : null;

            String loc = "tool." + tool.getToolName().toLowerCase() + ".kingslime"; // special localization the same way
                                                                                    // as
            // materials
            String name;
            if (StatCollector.canTranslate(loc)) name = StatCollector.translateToLocal(loc);
            else name = String.format(
                    StatCollector.translateToLocal("tool.nameformat"),
                    StatCollector.translateToLocal("tool.kingslimeprefix"),
                    tool.getLocalizedToolName());

            ItemStack toolStack = ToolBuilder.instance
                    .buildTool(headStack, handleStack, accessoryStack, extraStack, name);

            if (toolStack != null) {
                NBTTagCompound tags = toolStack.getTagCompound().getCompoundTag("InfiTool");
                tags.setInteger("Attack", 5 + tool.getDamageVsEntity(null));
                tags.setInteger("TotalDurability", 2500);
                tags.setInteger("BaseDurability", 2500);
                tags.setInteger("MiningSpeed", 1400);
                validtools++;
                drops.add(MobDrop.create(toolStack).withChance(chance));
            }
        }
        drops.add(
                MobDrop.create(new ItemStack(TinkerArmor.heartCanister, 1, 1)).withChance((chance / 5d) * validtools));
    }

    ToolCore getValidTool() {
        return TConstructRegistry.tools.get(rand.nextInt(TConstructRegistry.tools.size()));
    }
}
