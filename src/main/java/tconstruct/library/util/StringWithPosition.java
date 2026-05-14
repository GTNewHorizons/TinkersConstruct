package tconstruct.library.util;

import java.util.Arrays;
import java.util.List;

public class StringWithPosition implements WidgetsHasTooltips {

    final private List<String> tooltips;

    final private int startPositionX;
    final private int startPositionY;
    final private int width;
    final private int height;

    public StringWithPosition(String tooltips, int startPositionX, int startPositionY, int width, int height) {
        this(Arrays.asList(tooltips.split("\\\\n")), startPositionX, startPositionY, width, height, 1);
    }

    public StringWithPosition(List<String> tooltips, int startPositionX, int startPositionY, int width, int height,
            float scale) {
        this.tooltips = tooltips;
        this.startPositionX = startPositionX;
        this.startPositionY = startPositionY;
        this.width = width;
        this.height = height;
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
