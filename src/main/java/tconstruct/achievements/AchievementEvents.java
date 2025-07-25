package tconstruct.achievements;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import tconstruct.items.tools.FryingPan;
import tconstruct.library.event.ToolCraftedEvent;
import tconstruct.library.tools.ToolCore;
import tconstruct.library.tools.Weapon;
import tconstruct.tools.logic.ToolForgeLogic;

public class AchievementEvents {

    @SubscribeEvent
    public void toolCreate(ToolCraftedEvent event) {
        if (event.player != null) {
            TAchievements.triggerAchievement(event.player, "tconstruct.tinkerer");

            if (event.tool != null && event.tool.getItem() instanceof Weapon) {
                TAchievements.triggerAchievement(event.player, "tconstruct.preparedFight");
            }

            if (event.inventory instanceof ToolForgeLogic && event.tool.getItem() instanceof ToolCore
                    && ((ToolCore) event.tool.getItem()).durabilityTypeExtra() != 0) {
                TAchievements.triggerAchievement(event.player, "tconstruct.proTinkerer");
            }
        }
    }

    @SubscribeEvent
    public void entitySlain(LivingDeathEvent event) {
        if (event.source != null && event.source.getEntity() instanceof EntityPlayer) {
            EntityPlayer murderer = (EntityPlayer) event.source.getEntity();

            if (murderer.getHeldItem() != null && murderer.getHeldItem().getItem() instanceof Weapon) {
                TAchievements.triggerAchievement(murderer, "tconstruct.enemySlayer");
            }
        }
    }

    @SubscribeEvent
    public void entityDrops(LivingDropsEvent event) {
        if (event.source.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.source.getEntity();

            if (player.getHeldItem() != null && player.getHeldItem().getItem() instanceof FryingPan) {
                for (int i = 0; i < event.drops.size(); i++) {
                    ItemStack is = event.drops.get(i).getEntityItem();
                    if (FurnaceRecipes.smelting().getSmeltingResult(is) != null
                            && FurnaceRecipes.smelting().getSmeltingResult(is).getItem() instanceof ItemFood) {
                        NBTTagCompound stackCompound = is.getTagCompound();
                        if (stackCompound == null) {
                            stackCompound = new NBTTagCompound();
                        }

                        stackCompound.setBoolean("frypanKill", true);
                        is.setTagCompound(stackCompound);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void tooltip(ItemTooltipEvent event) {
        if (event.itemStack == null || event.itemStack.getItem() == null
                || !event.itemStack.hasTagCompound()
                || !event.itemStack.getTagCompound().hasKey("frypanKill"))
            return;

        event.toolTip.add(
                "" + EnumChatFormatting.DARK_PURPLE
                        + EnumChatFormatting.ITALIC
                        + StatCollector.translateToLocal("frypankill.tooltip"));
    }
}
