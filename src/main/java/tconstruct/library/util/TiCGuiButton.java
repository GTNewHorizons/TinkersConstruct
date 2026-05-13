package tconstruct.library.util;

import java.util.List;

import net.minecraft.client.gui.GuiButton;

import tconstruct.client.pages.TiCBookPage;

public abstract class TiCGuiButton extends GuiButton implements WidgetsHasTooltips {

    public TiCBookPage parentPage;
    public List<String> toolTips;
    public boolean needRenderTips = true;

    public TiCGuiButton(int stateName, int id, int p_i1021_3_, int p_i1021_4_, int p_i1021_5_, String p_i1021_6_,
            TiCBookPage parentPage) {
        super(stateName, id, p_i1021_3_, p_i1021_4_, p_i1021_5_, p_i1021_6_);
        this.parentPage = parentPage;
    }

    @Override
    public boolean isHover(int mouseX, int mouseY) {
        return mouseX >= this.xPosition && mouseY >= this.yPosition
                && mouseX < this.xPosition + this.width
                && mouseY < this.yPosition + this.height;
    }

    @Override
    public List<String> getTooltips() {
        return this.toolTips;
    }

    @Override
    public boolean needRenderTips() {
        return this.needRenderTips;
    }

}
