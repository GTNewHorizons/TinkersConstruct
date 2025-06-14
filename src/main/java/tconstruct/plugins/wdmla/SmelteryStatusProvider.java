package tconstruct.plugins.wdmla;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import com.gtnewhorizons.wdmla.api.accessor.BlockAccessor;
import com.gtnewhorizons.wdmla.api.provider.IBlockComponentProvider;
import com.gtnewhorizons.wdmla.api.provider.IServerDataProvider;
import com.gtnewhorizons.wdmla.api.ui.ITooltip;
import com.gtnewhorizons.wdmla.impl.ui.StatusHelper;

import tconstruct.smeltery.logic.SmelteryLogic;

public enum SmelteryStatusProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor) {
        if (accessor.getServerData().hasKey("State")) {
            int status = accessor.getServerData().getByte("State");
            switch (status) {
                case 1:
                    tooltip.child(StatusHelper.INSTANCE.insufficientFuel());
                    break;
                case 2:
                    tooltip.child(StatusHelper.INSTANCE.idle());
                    break;
                case 3:
                    tooltip.child(StatusHelper.INSTANCE.runningFine());
                    break;
                default:
                    tooltip.child(StatusHelper.INSTANCE.structureIncomplete());
                    break;
            }
        }
    }

    @Override
    public void appendServerData(NBTTagCompound data, BlockAccessor accessor) {
        if (accessor.getTileEntity() instanceof SmelteryLogic logic) {
            if (!logic.validStructure) {
                data.setByte("State", (byte) 0);
            } else if (!logic.hasFuel()) {
                data.setByte("State", (byte) 1);
            } else if (!logic.isInUse()) {
                data.setByte("State", (byte) 2);
            } else {
                data.setByte("State", (byte) 3);
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return TinkerWDMla.TiC("smeltery_status");
    }
}
