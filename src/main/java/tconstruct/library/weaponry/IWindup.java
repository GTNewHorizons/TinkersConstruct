package tconstruct.library.weaponry;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import tconstruct.weaponry.client.CrosshairType;

/**
 * This class has a windup time, meaning it takes some time to reach full potency. Windup progress is visualized with a
 * special crosshair.
 */
public interface IWindup {

    int getWindupTime(ItemStack itemStack); // how long it takes to fully wind up

    float getWindupProgress(ItemStack itemStack, EntityPlayer player); // how far we've winded up, 0.0-1.0

    float getMinWindupProgress(ItemStack itemStack); // how long it has been winded up at least to fire (0.0-1.0)

    CrosshairType getCrosshairType();

    boolean zoomOnWindup(ItemStack itemStack);

    float getZoom(ItemStack itemStack);
}
