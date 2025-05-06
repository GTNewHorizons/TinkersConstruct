package tconstruct.plugins.wdmla;

import java.util.Arrays;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import com.gtnewhorizons.wdmla.api.accessor.BlockAccessor;
import com.gtnewhorizons.wdmla.api.provider.IBlockComponentProvider;
import com.gtnewhorizons.wdmla.api.provider.IServerDataProvider;
import com.gtnewhorizons.wdmla.api.ui.IComponent;
import com.gtnewhorizons.wdmla.api.ui.ITooltip;
import com.gtnewhorizons.wdmla.impl.ui.ThemeHelper;

import tconstruct.blocks.logic.DryingRackLogic;
import tconstruct.library.crafting.DryingRackRecipes;

public enum DryingRackProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor) {
        ItemStack content = ItemStack.loadItemStackFromNBT(accessor.getServerData());
        if (content != null) {
            int time = accessor.getServerData().getInteger("Time");
            int maxTime = accessor.getServerData().getInteger("MaxTime");
            if (time != 0 && maxTime != 0) {
                ItemStack result = DryingRackRecipes.getDryingResult(content);
                if (result != null) {
                    result.stackSize = 0;
                }
                IComponent progressComponent = ThemeHelper.INSTANCE.furnaceLikeProgress(
                        Arrays.asList(content),
                        Arrays.asList(result),
                        time,
                        maxTime,
                        accessor.showDetails());
                if (progressComponent != null) {
                    tooltip.child(progressComponent);
                }
            } else {
                tooltip.child(ThemeHelper.INSTANCE.itemStackFullLine(content));
            }
        }
    }

    @Override
    public void appendServerData(NBTTagCompound data, BlockAccessor accessor) {
        if (accessor.getTarget() instanceof DryingRackLogic tileEntity) {
            ItemStack stack = tileEntity.getStackInSlot(0);
            if (stack == null) {
                return;
            }
            NBTTagCompound rawData = new NBTTagCompound();
            tileEntity.writeToNBT(rawData);
            stack = stack.copy();
            stack.writeToNBT(data);
            data.setInteger("MaxTime", rawData.getInteger("MaxTime"));
            data.setInteger("Time", rawData.getInteger("Time"));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return TinkerWDMla.TiC("drying_rack");
    }
}
