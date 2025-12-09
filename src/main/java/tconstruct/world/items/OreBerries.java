package tconstruct.world.items;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mantle.items.abstracts.CraftingItem;
import tconstruct.TConstruct;
import tconstruct.library.TConstructRegistry;
import tconstruct.util.config.PHConstruct;

public class OreBerries extends CraftingItem {

    static String[] names = new String[] { "iron", "gold", "copper", "tin", "aluminum", "essence" };
    static String[] tex = new String[] { "oreberry_iron", "oreberry_gold", "oreberry_copper", "oreberry_tin",
            "oreberry_aluminum", "oreberry_essence" };

    public OreBerries() {
        super(names, tex, "oreberries/", "tinker", TConstructRegistry.materialTab);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4) {
        switch (stack.getItemDamage()) {
            case 0:
                list.add(StatCollector.translateToLocal("oreberries1.tooltip"));
                break;
            case 1:
                list.add(StatCollector.translateToLocal("oreberries2.tooltip"));
                break;
            case 2:
                list.add(StatCollector.translateToLocal("oreberries3.tooltip"));
                break;
            case 3:
                list.add(StatCollector.translateToLocal("oreberries4.tooltip"));
                break;
            case 4:
                list.add(StatCollector.translateToLocal("oreberries5.tooltip"));
                break;
            case 5:
                list.add(StatCollector.translateToLocal("oreberries6.tooltip"));
                break;
        }
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (stack.getItemDamage() == 5) {
            if (!PHConstruct.consumeXPBerryStacks || !player.isSneaking()) {
                EntityXPOrb entity = new EntityXPOrb(
                        world,
                        player.posX,
                        player.posY + 1,
                        player.posZ,
                        itemRand.nextInt(14) + 6);
                spawnEntity(player.posX, player.posY + 1, player.posZ, entity, world, player);
                if (!player.capabilities.isCreativeMode) {
                    stack.stackSize--;
                    if (PHConstruct.disgustingXPBerries) {
                        if (applyBerryEffects(player, false)) player.worldObj.playSoundAtEntity(
                                player,
                                "game.player.hurt",
                                0.5F,
                                player.worldObj.rand.nextFloat() * 0.1F + 0.9F);
                    }
                }
            } else if (PHConstruct.consumeXPBerryStacks) {
                int xpToAdd = 0;
                boolean wasPlayerDamaged = false;
                for (int i = stack.stackSize; i > 0; i--) {
                    xpToAdd += itemRand.nextInt(14) + 6;
                    if (PHConstruct.disgustingXPBerries) {
                        wasPlayerDamaged = applyBerryEffects(player, true);
                    }
                }
                if (PHConstruct.disgustingXPBerries && wasPlayerDamaged) player.worldObj.playSoundAtEntity(
                        player,
                        "game.player.hurt",
                        0.5F,
                        player.worldObj.rand.nextFloat() * 0.1F + 0.9F);
                player.addExperience(xpToAdd);
                if (!player.capabilities.isCreativeMode) stack.stackSize = 0;
            }
        }
        return stack;
    }

    public static void spawnEntity(double x, double y, double z, Entity entity, World world, EntityPlayer player) {
        if (!world.isRemote) {
            world.spawnEntityInWorld(entity);
        }
    }

    boolean soundPlayedFlag = false;

    /**
     * parameters ID | DURATION | initialAmplifier | maxDuration | maxAmplifier | stackExclusive
     * 
     * @param player     the player that's eatingthe berries
     * @param isShifting is the player shifting?
     * @return Was one of the effects DAMAGE (used properly play the damage sound in account to the player shifting)
     */
    private static boolean applyBerryEffects(EntityPlayer player, boolean isShifting) {
        boolean output = false;
        for (String effect : PHConstruct.disgustingXPBerryEffects) {
            try {
                String[] parameters = effect.replaceAll(" ", "").split(",");
                boolean isDamageEffect = parameters[0].equals("DAMAGE");
                if (!(isDamageEffect && parameters.length == 3) && parameters.length != 6) {
                    throw new RuntimeException("Too many or few parameters");
                } else {
                    if (isShifting == Boolean.parseBoolean(parameters[isDamageEffect ? 2 : 5])) {
                        if (isDamageEffect) {
                            player.setHealth(player.getHealth() - Float.parseFloat(parameters[1]));
                            output = true;
                        } else {
                            player.addPotionEffect(getBerryEffect(player, parameters));
                        }
                    }
                }
            } catch (Exception e) {
                TConstruct.logger.error(
                        "ERROR APPLYING ESSENCE BERRY EFFECT: {} ERROR: {} STACKTRACE: {}",
                        effect,
                        e.getMessage(),
                        e.getStackTrace());
            }
        }
        return output;
    }

    /**
     * Don't ask how the amplifier increase system works, I'm not fully sure myself @GamingB3ast
     * 
     * @param player
     * @param parameters ID | DURATION | initialAmplifier | maxDuration | maxAmplifier | stackExclusive
     * @return The potion effect which is applied onto the player
     */
    private static PotionEffect getBerryEffect(EntityPlayer player, String[] parameters) {
        int duration = Integer.parseInt(parameters[1]);
        int amplifier = Integer.parseInt(parameters[2]);
        for (PotionEffect currentEffect : player.getActivePotionEffects()) {
            if (currentEffect.getPotionID() == Integer.parseInt(parameters[0])) {
                duration = Math.min(Integer.parseInt(parameters[3]), currentEffect.getDuration() + 40);
                amplifier = currentEffect.getAmplifier() + 1;
                int step = Integer.parseInt(parameters[3]) / (Integer.parseInt(parameters[4]) + 1 - amplifier) - 80;
                amplifier += Math.min(Integer.parseInt(parameters[3]) - 1, (duration >= step * amplifier ? 1 : 0));
                break;
            }
        }
        return new PotionEffect(Integer.parseInt(parameters[0]), duration, amplifier - 1);
    }
}
