package tconstruct.library.util;

import java.util.List;

import net.minecraft.client.gui.GuiButton;

public abstract class TiCGuiButton extends GuiButton {

    public List<String> toolTips;

    public TiCGuiButton(int stateName, int id, int p_i1021_3_, int p_i1021_4_, int p_i1021_5_, String p_i1021_6_) {
        super(stateName, id, p_i1021_3_, p_i1021_4_, p_i1021_5_, p_i1021_6_);
    }

    public boolean isHover(int mouseX, int mouseY) {
        return mouseX >= this.xPosition && mouseY >= this.yPosition
                && mouseX < this.xPosition + this.width
                && mouseY < this.yPosition + this.height;
    }

    public List<String> getTooltips() {
        return this.toolTips;
    }

}
