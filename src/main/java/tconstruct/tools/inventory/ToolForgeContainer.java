package tconstruct.tools.inventory;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;

import tconstruct.tools.TinkerTools;
import tconstruct.tools.logic.ToolForgeLogic;

public class ToolForgeContainer extends ToolStationContainer {

    public ToolForgeContainer(InventoryPlayer inventoryplayer, ToolForgeLogic logic) {
        super(inventoryplayer, logic);
    }

    @Override
    protected void playCraftSound(EntityPlayer player) {
        // 1021: the anvil being used
        logic.getWorldObj().playAuxSFX(1021, logic.xCoord, logic.yCoord, logic.zCoord, 0);
    }

    @Override
    public boolean canInteractWith(EntityPlayer entityplayer) {
        Block block = logic.getWorldObj().getBlock(logic.xCoord, logic.yCoord, logic.zCoord);
        if (block != TinkerTools.toolForge && block != TinkerTools.craftingSlabWood) return false;
        return logic.isUseableByPlayer(entityplayer);
    }
}
