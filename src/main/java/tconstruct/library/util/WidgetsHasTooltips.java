package tconstruct.library.util;

import java.util.List;

public interface WidgetsHasTooltips {

    public List<String> getTooltips();

    public boolean needRenderTips();

    public boolean isHover(int mouseX, int mouseY);

}
