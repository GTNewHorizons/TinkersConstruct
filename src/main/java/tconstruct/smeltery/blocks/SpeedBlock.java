package tconstruct.smeltery.blocks;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import tconstruct.blocks.TConstructBlock;

public class SpeedBlock extends TConstructBlock {

    public static String[] textureNames = new String[] { "brownstone_rough", "brownstone_rough_road",
            "brownstone_smooth", "brownstone_smooth_brick", "brownstone_smooth_road", "brownstone_smooth_fancy",
            "brownstone_smooth_chiseled" };

    public SpeedBlock() {
        super(Material.rock, 3.0f, textureNames);
    }

    /// Invoked whenever the local player walks over this block.
    /// Only invoked clientside.
    public void onWalkedOn(World world, int x, int y, int z, Entity entity) {
        if (entity.motionX == 0 && entity.motionZ == 0) return;
        if (entity.isInWater()) return;
        if (entity.isWet()) return;
        if (entity.isSneaking()) return;

        double tSpeed = 1.15;

        int metadata = world.getBlockMetadata(x, y, z);
        if (metadata == 1 || metadata == 4) {
            tSpeed = 1.3;
        }

        entity.motionX *= tSpeed;
        entity.motionZ *= tSpeed;
    }

    @Override
    public int damageDropped(int meta) {
        return meta;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item b, CreativeTabs tab, List<ItemStack> list) {
        for (int iter = 0; iter < textureNames.length; iter++) {
            list.add(new ItemStack(b, 1, iter));
        }
    }
}
