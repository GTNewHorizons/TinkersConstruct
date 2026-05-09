package tconstruct.util;

import net.minecraft.item.ItemStack;

public class ItemStackWithPosition {

    final private ItemStack i;

    final private int startPositionX;
    final private int startPositionY;
    final private int width;
    final private int height;

    final static int defaultWidth = 16;
    final static int defaultHeight = 16;

    public ItemStackWithPosition(ItemStack i, int startPositionX, int startPositionY, int width, int height) {
        this.i = i;
        this.startPositionX = startPositionX;
        this.startPositionY = startPositionY;
        this.width = width;
        this.height = height;
    }

    public ItemStackWithPosition(ItemStack i, int startPositionX, int startPositionY, float scale) {
        this.i = i;
        this.startPositionX = (int) (startPositionX * scale);
        this.startPositionY = (int) (startPositionY * scale);
        this.width = (int) (defaultWidth * scale);
        this.height = (int) (defaultHeight * scale);
    }

    public boolean isHovered(int mouseX, int mouseY) {
        return (this.startPositionX <= mouseX && mouseX <= this.startPositionX + this.width)
                && (this.startPositionY <= mouseY && mouseY <= this.startPositionY + this.height);
    }

    public ItemStack getItemStack() {
        return this.i;
    }

}
