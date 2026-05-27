package tconstruct.smeltery.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mantle.blocks.MantleBlock;
import tconstruct.library.TConstructRegistry;
import tconstruct.util.config.PHConstruct;

/**
 * @author fuj1n
 *
 */
public class GlassBlockConnected extends MantleBlock {

    protected IIcon[] icons = new IIcon[16];
    private static final boolean shouldRenderSelectionBox = true;
    protected String folder;
    private final int renderPass;

    public GlassBlockConnected(String location, boolean hasAlpha) {
        super(Material.glass);
        this.stepSound = soundTypeGlass;
        folder = location;
        renderPass = hasAlpha ? 1 : 0;
        setHardness(0.3F);
        this.setCreativeTab(TConstructRegistry.blockTab);
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public int getRenderBlockPass() {
        return renderPass;
    }

    public boolean shouldConnectToBlock(IBlockAccess blockAccess, int x, int y, int z, Block block, int meta) {
        return block == this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
        return getConnectedBlockTexture(blockAccess, x, y, z, side, icons);
    }

    public IIcon getConnectedBlockTexture(IBlockAccess blockAccess, int x, int y, int z, int side, IIcon[] icons) {
        if (PHConstruct.connectedTexturesMode == 0) {
            return icons[0];
        }

        boolean isOpenUp = false, isOpenDown = false, isOpenLeft = false, isOpenRight = false;

        switch (side) {
            case 0:
            case 1:
                if (shouldConnectToBlock(
                        blockAccess,
                        x,
                        y,
                        z,
                        blockAccess.getBlock(x - 1, y, z),
                        blockAccess.getBlockMetadata(x - 1, y, z))) {
                    isOpenLeft = true;
                }

                if (shouldConnectToBlock(
                        blockAccess,
                        x,
                        y,
                        z,
                        blockAccess.getBlock(x + 1, y, z),
                        blockAccess.getBlockMetadata(x + 1, y, z))) {
                    isOpenRight = true;
                }

                if (shouldConnectToBlock(
                        blockAccess,
                        x,
                        y,
                        z,
                        blockAccess.getBlock(x, y, z - 1),
                        blockAccess.getBlockMetadata(x, y, z - 1))) {
                    isOpenUp = true;
                }

                if (shouldConnectToBlock(
                        blockAccess,
                        x,
                        y,
                        z,
                        blockAccess.getBlock(x, y, z + 1),
                        blockAccess.getBlockMetadata(x, y, z + 1))) {
                    isOpenDown = true;
                }

                break;
            case 2:
                if (shouldConnectToBlock(
                        blockAccess,
                        x,
                        y,
                        z,
                        blockAccess.getBlock(x + 1, y, z),
                        blockAccess.getBlockMetadata(x + 1, y, z))) {
                    isOpenLeft = true;
                }

                if (shouldConnectToBlock(
                        blockAccess,
                        x,
                        y,
                        z,
                        blockAccess.getBlock(x - 1, y, z),
                        blockAccess.getBlockMetadata(x - 1, y, z))) {
                    isOpenRight = true;
                }

                if (shouldConnectToBlock(
                        blockAccess,
                        x,
                        y,
                        z,
                        blockAccess.getBlock(x, y + 1, z),
                        blockAccess.getBlockMetadata(x, y + 1, z))) {
                    isOpenUp = true;
                }

                if (shouldConnectToBlock(
                        blockAccess,
                        x,
                        y,
                        z,
                        blockAccess.getBlock(x, y - 1, z),
                        blockAccess.getBlockMetadata(x, y - 1, z))) {
                    isOpenDown = true;
                }

                break;
            case 3:
                if (shouldConnectToBlock(
                        blockAccess,
                        x,
                        y,
                        z,
                        blockAccess.getBlock(x - 1, y, z),
                        blockAccess.getBlockMetadata(x - 1, y, z))) {
                    isOpenLeft = true;
                }

                if (shouldConnectToBlock(
                        blockAccess,
                        x,
                        y,
                        z,
                        blockAccess.getBlock(x + 1, y, z),
                        blockAccess.getBlockMetadata(x + 1, y, z))) {
                    isOpenRight = true;
                }

                if (shouldConnectToBlock(
                        blockAccess,
                        x,
                        y,
                        z,
                        blockAccess.getBlock(x, y + 1, z),
                        blockAccess.getBlockMetadata(x, y + 1, z))) {
                    isOpenUp = true;
                }

                if (shouldConnectToBlock(
                        blockAccess,
                        x,
                        y,
                        z,
                        blockAccess.getBlock(x, y - 1, z),
                        blockAccess.getBlockMetadata(x, y - 1, z))) {
                    isOpenDown = true;
                }

                break;
            case 4:
                if (shouldConnectToBlock(
                        blockAccess,
                        x,
                        y,
                        z,
                        blockAccess.getBlock(x, y, z - 1),
                        blockAccess.getBlockMetadata(x, y, z - 1))) {
                    isOpenLeft = true;
                }

                if (shouldConnectToBlock(
                        blockAccess,
                        x,
                        y,
                        z,
                        blockAccess.getBlock(x, y, z + 1),
                        blockAccess.getBlockMetadata(x, y, z + 1))) {
                    isOpenRight = true;
                }

                if (shouldConnectToBlock(
                        blockAccess,
                        x,
                        y,
                        z,
                        blockAccess.getBlock(x, y + 1, z),
                        blockAccess.getBlockMetadata(x, y + 1, z))) {
                    isOpenUp = true;
                }

                if (shouldConnectToBlock(
                        blockAccess,
                        x,
                        y,
                        z,
                        blockAccess.getBlock(x, y - 1, z),
                        blockAccess.getBlockMetadata(x, y - 1, z))) {
                    isOpenDown = true;
                }

                break;
            case 5:
                if (shouldConnectToBlock(
                        blockAccess,
                        x,
                        y,
                        z,
                        blockAccess.getBlock(x, y, z + 1),
                        blockAccess.getBlockMetadata(x, y, z + 1))) {
                    isOpenLeft = true;
                }

                if (shouldConnectToBlock(
                        blockAccess,
                        x,
                        y,
                        z,
                        blockAccess.getBlock(x, y, z - 1),
                        blockAccess.getBlockMetadata(x, y, z - 1))) {
                    isOpenRight = true;
                }

                if (shouldConnectToBlock(
                        blockAccess,
                        x,
                        y,
                        z,
                        blockAccess.getBlock(x, y + 1, z),
                        blockAccess.getBlockMetadata(x, y + 1, z))) {
                    isOpenUp = true;
                }

                if (shouldConnectToBlock(
                        blockAccess,
                        x,
                        y,
                        z,
                        blockAccess.getBlock(x, y - 1, z),
                        blockAccess.getBlockMetadata(x, y - 1, z))) {
                    isOpenDown = true;
                }

                break;
        }

        if (isOpenUp && isOpenDown && isOpenLeft && isOpenRight) {
            return icons[15];
        } else if (isOpenUp && isOpenDown && isOpenLeft) {
            return icons[14];
        } else if (isOpenUp && isOpenDown && isOpenRight) {
            return icons[13];
        } else if (isOpenDown && isOpenLeft && isOpenRight) {
            return icons[12];
        } else if (isOpenUp && isOpenLeft && isOpenRight) {
            return icons[11];
        } else if (isOpenDown && isOpenLeft) {
            return icons[10];
        } else if (isOpenDown && isOpenRight) {
            return icons[9];
        } else if (isOpenUp && isOpenLeft) {
            return icons[8];
        } else if (isOpenUp && isOpenRight) {
            return icons[7];
        } else if (isOpenDown && isOpenUp) {
            return icons[6];
        } else if (isOpenLeft && isOpenRight) {
            return icons[5];
        } else if (isOpenRight) {
            return icons[4];
        } else if (isOpenLeft) {
            return icons[3];
        } else if (isOpenUp) {
            return icons[2];
        } else if (isOpenDown) {
            return icons[1];
        } else {
            return icons[0];
        }
    }

    @Override
    public boolean shouldSideBeRendered(IBlockAccess blockAccess, int x, int y, int z, int side) {
        Block b = blockAccess.getBlock(x, y, z);
        return b != this && super.shouldSideBeRendered(blockAccess, x, y, z, side);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return icons[0];
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
        if (shouldRenderSelectionBox) {
            return super.getSelectedBoundingBoxFromPool(world, x, y, z);
        } else {
            return AxisAlignedBB.getBoundingBox(0D, 0D, 0D, 0D, 0D, 0D);
        }
    }

    protected void registerBlockIcons(IIconRegister iconRegister, IIcon[] icons, String folder) {
        icons[0] = iconRegister.registerIcon("tinker:glass/" + folder + "/glass");
        icons[1] = iconRegister.registerIcon("tinker:glass/" + folder + "/glass_1_d");
        icons[2] = iconRegister.registerIcon("tinker:glass/" + folder + "/glass_1_u");
        icons[3] = iconRegister.registerIcon("tinker:glass/" + folder + "/glass_1_l");
        icons[4] = iconRegister.registerIcon("tinker:glass/" + folder + "/glass_1_r");
        icons[5] = iconRegister.registerIcon("tinker:glass/" + folder + "/glass_2_h");
        icons[6] = iconRegister.registerIcon("tinker:glass/" + folder + "/glass_2_v");
        icons[7] = iconRegister.registerIcon("tinker:glass/" + folder + "/glass_2_dl");
        icons[8] = iconRegister.registerIcon("tinker:glass/" + folder + "/glass_2_dr");
        icons[9] = iconRegister.registerIcon("tinker:glass/" + folder + "/glass_2_ul");
        icons[10] = iconRegister.registerIcon("tinker:glass/" + folder + "/glass_2_ur");
        icons[11] = iconRegister.registerIcon("tinker:glass/" + folder + "/glass_3_d");
        icons[12] = iconRegister.registerIcon("tinker:glass/" + folder + "/glass_3_u");
        icons[13] = iconRegister.registerIcon("tinker:glass/" + folder + "/glass_3_l");
        icons[14] = iconRegister.registerIcon("tinker:glass/" + folder + "/glass_3_r");
        icons[15] = iconRegister.registerIcon("tinker:glass/" + folder + "/glass_4");
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        registerBlockIcons(iconRegister, icons, folder);
    }

    @Override
    public boolean canPlaceTorchOnTop(World world, int x, int y, int z) {
        return true;
    }
}
