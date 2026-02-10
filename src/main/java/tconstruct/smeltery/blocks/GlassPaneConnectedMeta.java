package tconstruct.smeltery.blocks;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import tconstruct.util.config.PHConstruct;

public class GlassPaneConnectedMeta extends GlassPaneConnected {

    public String[] textures;
    public IIcon[][] icons;
    public IIcon[] sideIcons;
    public static final boolean ignoreMetaForConnectedGlass = PHConstruct.connectedTexturesMode == 2;

    public GlassPaneConnectedMeta(String location, boolean hasAlpha, String... textures) {
        super(location, hasAlpha);
        this.textures = textures;
        this.icons = new IIcon[textures.length][16];
        this.sideIcons = new IIcon[textures.length];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
        int meta = blockAccess.getBlockMetadata(x, y, z);
        if (meta < icons.length) {
            return getConnectedBlockTexture(blockAccess, x, y, z, side, icons[meta]);
        } else {
            return getConnectedBlockTexture(blockAccess, x, y, z, side, icons[0]);
        }
    }

    @Override
    public boolean shouldConnectToBlock(IBlockAccess blockAccess, int x, int y, int z, Block block, int meta) {
        return block == this && (meta == blockAccess.getBlockMetadata(x, y, z) || ignoreMetaForConnectedGlass);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return icons[meta][0];
    }

    @Override
    public void getSubBlocks(Item b, CreativeTabs creativeTabs, List<ItemStack> list) {
        for (int i = 0; i < textures.length; i++) {
            list.add(new ItemStack(b, 1, i));
        }
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        for (int i = 0; i < textures.length; i++) {
            registerBlockIcons(iconRegister, icons[i], folder + "/" + textures[i]);
            sideIcons[i] = iconRegister.registerIcon("tinker:glass/" + folder + "/" + textures[i] + "/glass_side");
        }
    }

    @Override
    public IIcon getSideTextureIndex(int meta) {
        return sideIcons[meta];
    }

    @Override
    public int damageDropped(int meta) {
        return meta;
    }
}
