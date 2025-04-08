package tconstruct.api.harvesting;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import tconstruct.items.tools.Scythe;
import tconstruct.library.tools.AbilityHelper;

public class AoeCropHarvestHandler {

    /**
     * We're using EventPriority.HIGH here to run before RightClickCropHandler from EnderCore.
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void handlePlayerInteractEvent(PlayerInteractEvent event) {
        ItemStack equippedItem = event.entityPlayer.getCurrentEquippedItem();
        if (!event.world.isRemote && isScythe(equippedItem)
                && harvestCropsInRange(equippedItem, event.entityPlayer, event.world, event.x, event.y, event.z)) {
            event.setCanceled(true);
        }
    }

    private static boolean isScythe(ItemStack stack) {
        return stack != null && stack.getItem() instanceof Scythe;
    }

    private static boolean harvestCropsInRange(ItemStack stack, EntityPlayer player, World world, int x, int y, int z) {
        boolean harvesting = canPlayerHarvestCrop(world, x, y, z);
        if (harvesting) {
            if (player.isSneaking()) {
                tryHarvestCrop(stack, player, world, x, y, z);
            } else {
                harvestInAoe(stack, player, world, x, y, z);
            }
        }
        return harvesting;
    }

    private static boolean canPlayerHarvestCrop(World world, int x, int y, int z) {
        for (CropHarvestHandler handler : CropHarvestHandlers.getCropHarvestHandlers()) {
            if (handler.couldHarvest(world, x, y, z)) {
                return true;
            }
        }
        return false;
    }

    private static void harvestInAoe(ItemStack stack, EntityPlayer player, World world, int x, int y, int z) {
        for (int i = -2; i < 3; i++) {
            for (int j = -2; j < 3; j++) {
                for (int k = -2; k < 3; k++) {
                    if (!(stack.getTagCompound().getCompoundTag("InfiTool").getBoolean("Broken"))) {
                        tryHarvestCrop(stack, player, world, x + i, y + j, z + k);
                    }
                }

            }
        }
    }

    private static void tryHarvestCrop(ItemStack stack, EntityPlayer player, World world, int x, int y, int z) {
        for (CropHarvestHandler handler : CropHarvestHandlers.getCropHarvestHandlers()) {
            if (handler.couldHarvest(world, x, y, z)) {
                boolean harvestSuccessful = handler.tryHarvest(stack, player, world, x, y, z);
                if (harvestSuccessful && !player.capabilities.isCreativeMode) {
                    AbilityHelper.damageTool(stack, 1, null, false);
                }
                return;
            }
        }
    }
}
