package tconstruct.blocks;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class BlockFalling extends Block {

    public static boolean fallInstantly;

    public BlockFalling() {
        super(Material.craftedSnow);
        this.setCreativeTab(CreativeTabs.tabBlock);
    }

    public BlockFalling(Material p_i45405_1_) {
        super(p_i45405_1_);
    }

    @Override
    public void onBlockAdded(World worldIn, int x, int y, int z) {
        worldIn.scheduleBlockUpdate(x, y, z, this, this.tickRate(worldIn));
    }

    @Override
    public void onNeighborBlockChange(World worldIn, int x, int y, int z, Block neighbor) {
        worldIn.scheduleBlockUpdate(x, y, z, this, this.tickRate(worldIn));
    }

    @Override
    public void updateTick(World worldIn, int x, int y, int z, Random random) {
        if (!worldIn.isRemote) {
            this.func_149830_m(worldIn, x, y, z);
        }
    }

    private void func_149830_m(World p_149830_1_, int p_149830_2_, int p_149830_3_, int p_149830_4_) {
        if (func_149831_e(p_149830_1_, p_149830_2_, p_149830_3_ - 1, p_149830_4_) && p_149830_3_ >= 0) {
            byte b0 = 32;

            if (!fallInstantly && p_149830_1_.checkChunksExist(
                    p_149830_2_ - b0,
                    p_149830_3_ - b0,
                    p_149830_4_ - b0,
                    p_149830_2_ + b0,
                    p_149830_3_ + b0,
                    p_149830_4_ + b0)) {
                // if (!p_149830_1_.isRemote) {
                // EntityFallingBlock entityfallingblock = new
                // EntityFallingBlock(p_149830_1_,
                // (double)((float)p_149830_2_ + 0.5F),
                // (double)((float)p_149830_3_ + 0.5F),
                // (double)((float)p_149830_4_ + 0.5F), this,
                // p_149830_1_.getBlockMetadata(p_149830_2_, p_149830_3_,
                // p_149830_4_));
                // this.func_149829_a(entityfallingblock);
                // p_149830_1_.spawnEntityInWorld(entityfallingblock);
                // }
            } else {
                p_149830_1_.setBlockToAir(p_149830_2_, p_149830_3_, p_149830_4_);

                while (func_149831_e(p_149830_1_, p_149830_2_, p_149830_3_ - 1, p_149830_4_) && p_149830_3_ > 0) {
                    --p_149830_3_;
                }

                if (p_149830_3_ > 0) {
                    p_149830_1_.setBlock(p_149830_2_, p_149830_3_, p_149830_4_, this);
                }
            }
        }
    }

    protected void func_149829_a(EntityFallingBlock p_149829_1_) {}

    @Override
    public int tickRate(World worldIn) {
        return 2;
    }

    public static boolean func_149831_e(World p_149831_0_, int p_149831_1_, int p_149831_2_, int p_149831_3_) {
        Block block = p_149831_0_.getBlock(p_149831_1_, p_149831_2_, p_149831_3_);

        if (block.isAir(p_149831_0_, p_149831_1_, p_149831_2_, p_149831_3_)) {
            return true;
        } else if (block == Blocks.fire) {
            return true;
        } else {
            // TODO: King, take a look here when doing liquids!
            // Material material = block.blockMaterial;
            // return material == Material.water ? true : material ==
            // Material.lava;
            return false;
        }
    }

    public void func_149828_a(World p_149828_1_, int p_149828_2_, int p_149828_3_, int p_149828_4_, int p_149828_5_) {}
}
