package tconstruct.blocks.slime;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import tconstruct.library.TConstructRegistry;
import tconstruct.world.TinkerWorld;

public class SlimeLeaves extends BlockLeaves {

    private static final String[][] leaves = new String[][] { { "slimeleaves_blue_fancy" },
            { "slimeleaves_blue_fast" } };

    public SlimeLeaves() {
        super();
        setCreativeTab(TConstructRegistry.blockTab);
        setHardness(0.3f);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getBlockColor() {
        return 0xffffff;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getRenderColor(int par1) {
        return 0xffffff;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int colorMultiplier(IBlockAccess par1IBlockAccess, int par2, int par3, int par4) {
        return 0xffffff;
    }

    /**
     * Ticks the block if it's been scheduled
     */
    @Override
    public void updateTick(World worldIn, int x, int y, int z, Random random) {
        if (!worldIn.isRemote) {
            int l = worldIn.getBlockMetadata(x, y, z);

            if ((l & 8) != 0 && (l & 4) == 0) {
                byte b0 = 4;
                int i1 = b0 + 1;
                byte b1 = 32;
                int j1 = b1 * b1;
                int k1 = b1 / 2;

                if (this.field_150128_a == null) {
                    this.field_150128_a = new int[b1 * b1 * b1];
                }

                int l1;

                if (worldIn.checkChunksExist(x - i1, y - i1, z - i1, x + i1, y + i1, z + i1)) {
                    int i2;
                    int j2;

                    for (l1 = -b0; l1 <= b0; ++l1) {
                        for (i2 = -b0; i2 <= b0; ++i2) {
                            for (j2 = -b0; j2 <= b0; ++j2) {
                                Block block = worldIn.getBlock(x + l1, y + i2, z + j2);

                                if (!(block instanceof SlimeGel)) {
                                    if (block.isLeaves(worldIn, x + l1, y + i2, z + j2)) {
                                        this.field_150128_a[(l1 + k1) * j1 + (i2 + k1) * b1 + j2 + k1] = -2;
                                    } else {
                                        this.field_150128_a[(l1 + k1) * j1 + (i2 + k1) * b1 + j2 + k1] = -1;
                                    }
                                } else {
                                    this.field_150128_a[(l1 + k1) * j1 + (i2 + k1) * b1 + j2 + k1] = 0;
                                }
                            }
                        }
                    }

                    for (l1 = 1; l1 <= 4; ++l1) {
                        for (i2 = -b0; i2 <= b0; ++i2) {
                            for (j2 = -b0; j2 <= b0; ++j2) {
                                for (int k2 = -b0; k2 <= b0; ++k2) {
                                    if (this.field_150128_a[(i2 + k1) * j1 + (j2 + k1) * b1 + k2 + k1] == l1 - 1) {
                                        if (this.field_150128_a[(i2 + k1 - 1) * j1 + (j2 + k1) * b1 + k2 + k1] == -2) {
                                            this.field_150128_a[(i2 + k1 - 1) * j1 + (j2 + k1) * b1 + k2 + k1] = l1;
                                        }

                                        if (this.field_150128_a[(i2 + k1 + 1) * j1 + (j2 + k1) * b1 + k2 + k1] == -2) {
                                            this.field_150128_a[(i2 + k1 + 1) * j1 + (j2 + k1) * b1 + k2 + k1] = l1;
                                        }

                                        if (this.field_150128_a[(i2 + k1) * j1 + (j2 + k1 - 1) * b1 + k2 + k1] == -2) {
                                            this.field_150128_a[(i2 + k1) * j1 + (j2 + k1 - 1) * b1 + k2 + k1] = l1;
                                        }

                                        if (this.field_150128_a[(i2 + k1) * j1 + (j2 + k1 + 1) * b1 + k2 + k1] == -2) {
                                            this.field_150128_a[(i2 + k1) * j1 + (j2 + k1 + 1) * b1 + k2 + k1] = l1;
                                        }

                                        if (this.field_150128_a[(i2 + k1) * j1 + (j2 + k1) * b1 + (k2 + k1 - 1)]
                                                == -2) {
                                            this.field_150128_a[(i2 + k1) * j1 + (j2 + k1) * b1 + (k2 + k1 - 1)] = l1;
                                        }

                                        if (this.field_150128_a[(i2 + k1) * j1 + (j2 + k1) * b1 + k2 + k1 + 1] == -2) {
                                            this.field_150128_a[(i2 + k1) * j1 + (j2 + k1) * b1 + k2 + k1 + 1] = l1;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                l1 = this.field_150128_a[k1 * j1 + k1 * b1 + k1];

                if (l1 >= 0) {
                    worldIn.setBlockMetadataWithNotify(x, y, z, l & -9, 4);
                } else {
                    this.removeLeaves(worldIn, x, y, z);
                }
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        for (int detail = 0; detail < leaves.length; ++detail) {
            field_150129_M[detail] = new IIcon[leaves[detail].length];
            for (int leaf = 0; leaf < leaves[detail].length; ++leaf) {
                field_150129_M[detail][leaf] = reg.registerIcon("tinker:" + leaves[detail][leaf]);
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return field_150129_M[Blocks.leaves.isOpaqueCube() ? 1 : 0][(meta % 4) % leaves[0].length];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockAccess worldIn, int x, int y, int z, int side) {
        return Blocks.leaves.shouldSideBeRendered(worldIn, x, y, z, side);
    }

    @Override
    public boolean isOpaqueCube() {
        return Blocks.leaves.isOpaqueCube();
    }

    @Override
    public void getSubBlocks(Item id, CreativeTabs tab, List<ItemStack> list) {
        for (int iter = 0; iter < leaves[0].length; iter++) {
            list.add(new ItemStack(id, 1, iter));
        }
    }

    /* Drops */

    /**
     * Returns the ID of the items to drop on destruction.
     */
    @Override
    public Item getItemDropped(int meta, Random random, int fortune) {
        return Item.getItemFromBlock(TinkerWorld.slimeSapling);
    }

    /**
     * Drops the block items with a specified chance of dropping the specified items
     */
    @Override
    public void dropBlockAsItemWithChance(World world, int x, int y, int z, int meta, float chance, int fortune) {
        if (!world.isRemote) {
            int dropChance = 35;

            /*
             * if ((meta & 3) == 3) { j1 = 40; }
             */

            if (fortune > 0) {
                dropChance -= 2 << fortune;

                if (dropChance < 15) {
                    dropChance = 15;
                }
            }

            if (world.rand.nextInt(dropChance) == 0) {
                Item k1 = this.getItemDropped(meta, world.rand, fortune);
                this.dropBlockAsItem(world, x, y, z, new ItemStack(k1, 1, this.damageDropped(meta)));
            }

            dropChance = 80;

            if (fortune > 0) {
                dropChance -= 10 << fortune;

                if (dropChance < 20) {
                    dropChance = 20;
                }
            }

            if ((meta & 3) == 0 && world.rand.nextInt(dropChance) == 0) {
                this.dropBlockAsItem(world, x, y, z, new ItemStack(TinkerWorld.strangeFood, 1, 0));
            }
        }
    }

    @Override
    public String[] func_150125_e() {
        return new String[] { "slime" };
    }
}
