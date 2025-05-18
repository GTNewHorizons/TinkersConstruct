package tconstruct.plugins.wdmla;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import com.gtnewhorizons.wdmla.api.accessor.BlockAccessor;
import com.gtnewhorizons.wdmla.api.provider.IBlockComponentProvider;
import com.gtnewhorizons.wdmla.api.ui.ITooltip;
import com.gtnewhorizons.wdmla.impl.ui.ThemeHelper;

import tconstruct.mechworks.logic.TileEntityLandmine;

public enum LandmineHeaderProvider implements IBlockComponentProvider {

    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor) {
        if (accessor.getTileEntity() instanceof TileEntityLandmine landmine) {
            ItemStack cover = landmine.getStackInSlot(3);
            if (cover != null) {
                ThemeHelper.INSTANCE.overrideTooltipHeader(tooltip, cover);
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return TinkerWDMla.TiC("landmine_header");
    }
}
