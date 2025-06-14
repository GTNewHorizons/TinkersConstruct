package tconstruct.plugins.wdmla;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.util.ResourceLocation;

import com.gtnewhorizons.wdmla.api.accessor.Accessor;
import com.gtnewhorizons.wdmla.api.provider.IClientExtensionProvider;
import com.gtnewhorizons.wdmla.api.provider.IServerExtensionProvider;
import com.gtnewhorizons.wdmla.api.view.ClientViewGroup;
import com.gtnewhorizons.wdmla.api.view.FluidView;
import com.gtnewhorizons.wdmla.api.view.ViewGroup;

import tconstruct.smeltery.logic.SmelteryLogic;

public enum SmelteryFluidProvider
        implements IServerExtensionProvider<FluidView.Data>, IClientExtensionProvider<FluidView.Data, FluidView> {

    INSTANCE;

    private static final int FLUID_LIMIT = 4;

    @Override
    public List<ClientViewGroup<FluidView>> getClientGroups(Accessor accessor, List<ViewGroup<FluidView.Data>> groups) {
        return ClientViewGroup.map(groups, FluidView::readDefault, (group, clientGroup) -> {});
    }

    @Override
    public List<ViewGroup<FluidView.Data>> getGroups(Accessor accessor) {
        if (!(accessor.getTarget() instanceof SmelteryLogic smelteryLogic)) {
            return null;
        }
        if (smelteryLogic.moltenMetal.isEmpty()) {
            return Arrays.asList(new ViewGroup<>(Arrays.asList(new FluidView.Data(null, smelteryLogic.maxLiquid))));
        }
        List<ViewGroup<FluidView.Data>> tanks = smelteryLogic.moltenMetal.stream().limit(FLUID_LIMIT).map(
                fluidStack -> new ViewGroup<>(Arrays.asList(new FluidView.Data(fluidStack, smelteryLogic.maxLiquid))))
                .collect(Collectors.toList());
        if (smelteryLogic.moltenMetal.size() > FLUID_LIMIT) {
            tanks.get(FLUID_LIMIT - 1).getExtraData().setInteger("+", smelteryLogic.moltenMetal.size() - tanks.size());
        }
        return tanks;
    }

    @Override
    public ResourceLocation getUid() {
        return TinkerWDMla.TiC("smeltery_fluid");
    }
}
