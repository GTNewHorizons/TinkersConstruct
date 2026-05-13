package tconstruct.library.util;

import java.util.List;

public class StringWithPosition implements WidgetsHasTooltips {

    final private List<String> tooltips;

    final private int startPositionX;
    final private int startPositionY;
    final private int width;
    final private int height;

    public StringWithPosition(List<String> tooltips, int startPositionX, int startPositionY, int width, int height,
            float scale) {
        this.tooltips = tooltips;
        this.startPositionX = (int) (startPositionX * scale);
        this.startPositionY = (int) (startPositionY * scale);
        this.width = (int) (width * scale);
        this.height = (int) (height * scale);
    }

    public boolean isHover(int mouseX, int mouseY) {
        return (this.startPositionX <= mouseX && mouseX <= this.startPositionX + this.width)
                && (this.startPositionY <= mouseY && mouseY <= this.startPositionY + this.height);
    }

    @Override
    public List<String> getTooltips() {
        return this.tooltips;
    }

    @Override
    public boolean needRenderTips() {
        return true;
    }

}
