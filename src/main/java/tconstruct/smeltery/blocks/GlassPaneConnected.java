package tconstruct.smeltery.blocks;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import tconstruct.smeltery.model.PaneConnectedRender;

public class GlassPaneConnected extends GlassBlockConnected {

    private IIcon sideIcon;

    public GlassPaneConnected(String location, boolean hasAlpha) {
        super(location, hasAlpha);
    }

    @Override
    public int getRenderType() {
        return PaneConnectedRender.model;
    }

    @Override
    public IIcon getConnectedBlockTexture(IBlockAccess blockAccess, int x, int y, int z, int side, IIcon[] icons) {
        if (side == 0 || side == 1) {
            if ((blockAccess.getBlock(x, y - 1, z) == this && side == 0)
                    || (blockAccess.getBlock(x, y + 1, z) == this && side == 1)) {
                return icons[15];
            }

            return getSideTextureIndex(blockAccess.getBlockMetadata(x, y, z));
        }

        return super.getConnectedBlockTexture(blockAccess, x, y, z, side, icons);
    }

    @Override
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB axisAlignedBB,
            List<AxisAlignedBB> par6List, Entity entity) {
        boolean flag = this.canPaneConnectTo(world, x, y, z - 1, ForgeDirection.NORTH);
        boolean flag1 = this.canPaneConnectTo(world, x, y, z + 1, ForgeDirection.SOUTH);
        boolean flag2 = this.canPaneConnectTo(world, x - 1, y, z, ForgeDirection.WEST);
        boolean flag3 = this.canPaneConnectTo(world, x + 1, y, z, ForgeDirection.EAST);

        if ((!flag2 || !flag3) && (flag2 || flag3 || flag || flag1)) {
            if (flag2 && !flag3) {
                this.setBlockBounds(0.0F, 0.0F, 0.4375F, 0.5F, 1.0F, 0.5625F);
                super.addCollisionBoxesToList(world, x, y, z, axisAlignedBB, par6List, entity);
            } else if (!flag2 && flag3) {
                this.setBlockBounds(0.5F, 0.0F, 0.4375F, 1.0F, 1.0F, 0.5625F);
                super.addCollisionBoxesToList(world, x, y, z, axisAlignedBB, par6List, entity);
            }
        } else {
            this.setBlockBounds(0.0F, 0.0F, 0.4375F, 1.0F, 1.0F, 0.5625F);
            super.addCollisionBoxesToList(world, x, y, z, axisAlignedBB, par6List, entity);
        }

        if ((!flag || !flag1) && (flag2 || flag3 || flag || flag1)) {
            if (flag && !flag1) {
                this.setBlockBounds(0.4375F, 0.0F, 0.0F, 0.5625F, 1.0F, 0.5F);
                super.addCollisionBoxesToList(world, x, y, z, axisAlignedBB, par6List, entity);
            } else if (!flag && flag1) {
                this.setBlockBounds(0.4375F, 0.0F, 0.5F, 0.5625F, 1.0F, 1.0F);
                super.addCollisionBoxesToList(world, x, y, z, axisAlignedBB, par6List, entity);
            }
        } else {
            this.setBlockBounds(0.4375F, 0.0F, 0.0F, 0.5625F, 1.0F, 1.0F);
            super.addCollisionBoxesToList(world, x, y, z, axisAlignedBB, par6List, entity);
        }
    }

    @Override
    public void setBlockBoundsForItemRender() {
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess blockAccess, int x, int y, int z) {
        float f = 0.4375F;
        float f1 = 0.5625F;
        float f2 = 0.4375F;
        float f3 = 0.5625F;
        boolean flag = this.canPaneConnectTo(blockAccess, x, y, z - 1, ForgeDirection.NORTH);
        boolean flag1 = this.canPaneConnectTo(blockAccess, x, y, z + 1, ForgeDirection.SOUTH);
        boolean flag2 = this.canPaneConnectTo(blockAccess, x - 1, y, z, ForgeDirection.WEST);
        boolean flag3 = this.canPaneConnectTo(blockAccess, x + 1, y, z, ForgeDirection.EAST);

        if ((!flag2 || !flag3) && (flag2 || flag3 || flag || flag1)) {
            if (flag2 && !flag3) {
                f = 0.0F;
            } else if (!flag2 && flag3) {
                f1 = 1.0F;
            }
        } else {
            f = 0.0F;
            f1 = 1.0F;
        }

        if ((!flag || !flag1) && (flag2 || flag3 || flag || flag1)) {
            if (flag && !flag1) {
                f2 = 0.0F;
            } else if (!flag && flag1) {
                f3 = 1.0F;
            }
        } else {
            f2 = 0.0F;
            f3 = 1.0F;
        }

        this.setBlockBounds(f, 0.0F, f2, f1, 1.0F, f3);
    }

    public IIcon getSideTextureIndex(int meta) {
        return sideIcon;
    }

    public final boolean canPaneConnectToBlock(Block b) {
        return b.isOpaqueCube() || b == this || b == Blocks.glass;
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        super.registerBlockIcons(iconRegister);
        this.sideIcon = iconRegister.registerIcon("tinker:glass/" + folder + "/glass_side");
    }

    public boolean canPaneConnectTo(IBlockAccess world, int x, int y, int z, ForgeDirection dir) {
        return canPaneConnectToBlock(world.getBlock(x, y, z)) || world.isSideSolid(x, y, z, dir.getOpposite(), false);
    }
}
