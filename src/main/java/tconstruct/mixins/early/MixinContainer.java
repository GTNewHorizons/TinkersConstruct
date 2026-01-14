package tconstruct.mixins.early;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;

import tconstruct.tools.logic.ToolStationLogic;

@Mixin(Container.class)
public abstract class MixinContainer {

    @Inject(
            method = "slotClick(IIILnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/item/ItemStack;",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/item/ItemStack;stackSize:I",
                    opcode = org.objectweb.asm.Opcodes.PUTFIELD,
                    shift = At.Shift.AFTER,
                    ordinal = 3))
    private void notifyToolStation(int slotId, int clickedButton, int mode, EntityPlayer player,
            CallbackInfoReturnable<ItemStack> cir, @Local Slot slot2) {
        if (slot2.inventory instanceof ToolStationLogic toolStation) {
            toolStation.tryBuildTool(slot2.getSlotIndex());
        }
    }
}
