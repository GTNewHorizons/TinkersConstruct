package tconstruct.tools.logic;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/*
 * Slots 0: Battlesign item
 */

public class BattlesignLogic extends EquipLogic {

    public static final int LINE_COUNT = 5;

    protected String[] text;

    public BattlesignLogic() {
        super(1);
    }

    @Override
    public String getDefaultName() {
        return "decoration.battlesign";
    }

    @Override
    /* NBT */
    public void readFromNBT(NBTTagCompound tags) {
        super.readFromNBT(tags);

        text = new String[tags.getInteger("lineCount")];
        for (int i = 0; i < text.length; i++) {
            text[i] = tags.getString("line" + i);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tags) {
        super.writeToNBT(tags);

        if (text == null) {
            text = new String[0];
        }

        tags.setInteger("lineCount", text.length);
        for (int i = 0; i < text.length; i++) {
            tags.setString("line" + i, text[i]);
        }
    }

    @Override
    public Container getGuiContainer(InventoryPlayer inventoryplayer, World world, int x, int y, int z) {
        return null; // Not a normal gui block
    }

    @Override
    public String getInventoryName() {
        return null;
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    public void setText(String[] text) {
        this.text = text;
    }

    public String[] getText() {
        return text;
    }
}
