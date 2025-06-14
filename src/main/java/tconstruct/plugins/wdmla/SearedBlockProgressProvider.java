package tconstruct.plugins.wdmla;

import java.util.Arrays;
import java.util.List;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import com.gtnewhorizons.wdmla.api.accessor.Accessor;
import com.gtnewhorizons.wdmla.api.provider.IClientExtensionProvider;
import com.gtnewhorizons.wdmla.api.provider.IServerExtensionProvider;
import com.gtnewhorizons.wdmla.api.view.ClientViewGroup;
import com.gtnewhorizons.wdmla.api.view.ProgressView;
import com.gtnewhorizons.wdmla.api.view.ViewGroup;
import com.gtnewhorizons.wdmla.impl.ui.ThemeHelper;
import com.gtnewhorizons.wdmla.util.FormatUtil;

import tconstruct.smeltery.logic.CastingBlockLogic;

public enum SearedBlockProgressProvider implements IServerExtensionProvider<ProgressView.Data>,
        IClientExtensionProvider<ProgressView.Data, ProgressView> {

    INSTANCE;

    @Override
    public ResourceLocation getUid() {
        return TinkerWDMla.TiC("seared_blocks");
    }

    @Override
    public List<ClientViewGroup<ProgressView>> getClientGroups(Accessor accessor,
            List<ViewGroup<ProgressView.Data>> groups) {
        return ClientViewGroup.map(groups, ProgressView::read, (group, clientGroup) -> {
            ProgressView view = clientGroup.views.get(0);
            view.description = ThemeHelper.INSTANCE.value(
                    StatCollector.translateToLocal("hud.msg.wdmla.progress"),
                    FormatUtil.PERCENTAGE_STANDARD.format((float) view.progress / 100));
            view.hasScale = true;
        });
    }

    @Override
    public List<ViewGroup<ProgressView.Data>> getGroups(Accessor accessor) {
        if (accessor.getTarget() instanceof CastingBlockLogic castingBlockLogic) {
            int progress = castingBlockLogic.getProgress();
            if (progress == 0) {
                return null;
            }
            ProgressView.Data progressData = new ProgressView.Data(progress, 100);
            ViewGroup<ProgressView.Data> group = new ViewGroup<>(Arrays.asList(progressData));
            return Arrays.asList(group);
        }
        return null;
    }
}
