package tconstruct.tools.logic;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;

import tconstruct.library.util.IPattern;
import tconstruct.tools.inventory.PatternChestContainer;

public class PatternChestLogic extends TiCChestLogic {

    public PatternChestLogic() {
        super(IPattern.class);
    }

    @Override
    public String getDefaultName() {
        return "toolstation.patternholder";
    }

    @Override
    public Container getGuiContainer(InventoryPlayer inventoryplayer, World world, int x, int y, int z) {
        return new PatternChestContainer(inventoryplayer, this);
    }

}
