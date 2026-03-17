package tconstruct.tools.logic;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;

import tconstruct.library.util.IToolPart;
import tconstruct.tools.inventory.PartChestContainer;

public class PartChestLogic extends TiCChestLogic {

    public PartChestLogic() {
        super(IToolPart.class);
    }

    @Override
    public String getDefaultName() {
        return "toolstation.partholder";
    }

    @Override
    public Container getGuiContainer(InventoryPlayer inventoryplayer, World world, int x, int y, int z) {
        return new PartChestContainer(inventoryplayer, this);
    }

}
