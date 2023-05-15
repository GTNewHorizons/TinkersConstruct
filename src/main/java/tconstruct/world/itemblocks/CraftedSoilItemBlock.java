package tconstruct.world.itemblocks;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;

import cpw.mods.fml.relauncher.*;
import mantle.blocks.abstracts.MultiItemBlock;

public class CraftedSoilItemBlock extends MultiItemBlock {

    public static final String[] blockTypes = { "Slime", "Grout", "BlueSlime", "GraveyardSoil", "ConsecratedSoil",
            "blue", "dirt", "Grout" };

    public CraftedSoilItemBlock(Block b) {
        super(b, "CraftedSoil", blockTypes);
        setMaxDamage(0);
        setHasSubtypes(true);
    }

    @Override
    public String getUnlocalizedName(ItemStack itemstack) {
        int pos = MathHelper.clamp_int(itemstack.getItemDamage(), 0, blockTypes.length - 1);
        if (pos <= 4) return super.getUnlocalizedName(itemstack);
        return "block.slime.soil." + blockTypes[pos];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4) {
        switch (stack.getItemDamage()) {
            case 1:
                if (StatCollector.canTranslate("grout.tooltip"))
                    list.add(StatCollector.translateToLocal("grout.tooltip"));
                break;
            case 3:
                list.add(StatCollector.translateToLocal("craftedsoil1.tooltip"));
                break;
            case 4:
                list.add(StatCollector.translateToLocal("craftedsoil2.tooltip"));
                break;
        }
    }
}
