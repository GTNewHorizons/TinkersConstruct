package tconstruct.tools.blocks;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import tconstruct.tools.logic.BattlesignLogic;
import tconstruct.tools.model.BattlesignRender;

public class BattlesignBlock extends EquipBlock {

    public BattlesignBlock(Material material) {
        super(material);

        this.setBlockBounds(0.45F, 0F, 0.45F, 0.55F, 1.125F, 0.55F);
    }

    @Override
    public int getRenderType() {
        return BattlesignRender.battlesignModelID;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean shouldSideBeRendered(IBlockAccess worldIn, int x, int y, int z, int side) {
        return side == 0 && this.minY > 0.0D || (side == 1 && this.maxY < 1.0D
                || (side == 2 && this.minZ > 0.0D || (side == 3 && this.maxZ < 1.0D || (side == 4 && this.minX > 0.0D
                        || (side == 5 && this.maxX < 1.0D || !(worldIn.getBlock(x, y, z).isOpaqueCube()
                                && worldIn.getBlock(x, y + 1, z).isOpaqueCube()))))));
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new BattlesignLogic();
    }

    @Override
    public void onBlockPlacedBy(World par1World, int par2, int par3, int par4, EntityLivingBase par5EntityLivingBase,
            ItemStack par6ItemStack) {
        super.onBlockPlacedBy(par1World, par2, par3, par4, par5EntityLivingBase, par6ItemStack);
        int i3 = MathHelper.floor_double((par5EntityLivingBase.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;

        int newMeta = switch (i3) {
            case 3 -> 0;
            case 0 -> 3;
            case 1 -> 1;
            case 2 -> 2;
            default -> 0;
        };

        par1World.setBlockMetadataWithNotify(par2, par3, par4, newMeta, 2);
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess blockAccess, int x, int y, int z) {
        switch (blockAccess.getBlockMetadata(x, y, z)) {
            case 0:
                setBlockBounds(0.42F, 0.5F, 0F, 0.50F, 1.1F, 1F);
                break;
            case 1:
                setBlockBounds(0.50F, 0.5F, 0F, 0.58F, 1.1F, 1F);
                break;
            case 2:
                setBlockBounds(0F, 0.5F, 0.50F, 1F, 1.1F, 0.58F);
                break;
            case 3:
                setBlockBounds(0F, 0.5F, 0.42F, 1F, 1.1F, 0.50F);
                break;
        }
    }

    @Override
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB aabb, List<AxisAlignedBB> list,
            Entity entity) {
        setBlockBoundsBasedOnState(world, x, y, z);

        super.addCollisionBoxesToList(world, x, y, z, aabb, list, entity);
        setBlockBounds(0.45F, 0F, 0.45F, 0.55F, 1.125F, 0.55F);
        super.addCollisionBoxesToList(world, x, y, z, aabb, list, entity);

        this.setBlockBoundsForItemRender();
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return new BattlesignLogic();
    }

    @Override
    public Integer getGui(World world, int x, int y, int z, EntityPlayer entityplayer) {
        return null;
    }
}
