package tconstruct.smeltery.itemblocks;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import mantle.blocks.abstracts.MultiItemBlock;

public class CastingChannelItem extends MultiItemBlock {

    public static final String[] blockTypes = { "Channel" };

    public CastingChannelItem(Block b) {
        super(b, "Smeltery", blockTypes);
        setMaxDamage(0);
        setHasSubtypes(true);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4) {
        list.add(StatCollector.translateToLocal("smeltery.purity.tooltip"));
    }
}
