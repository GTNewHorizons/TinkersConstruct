package tconstruct.tools.items;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import tconstruct.library.TConstructRegistry;
import tconstruct.library.crafting.ToolBuilder;
import tconstruct.library.crafting.ToolRecipe;
import tconstruct.library.tools.ToolCore;

public class CreativeModifier extends Item {

    public CreativeModifier() {
        super();
        this.setCreativeTab(TConstructRegistry.materialTab);
    }

    /**
     * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
     */
    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item1, CreativeTabs creativeTabs, List<ItemStack> list) {
        list.add(new ItemStack(item1, 1, 0));

        for (ToolRecipe recipe : ToolBuilder.instance.combos) {
            ToolCore tool = recipe.getType();
            ItemStack item = new ItemStack(item1, 1, 0);
            NBTTagCompound compound = new NBTTagCompound();
            compound.setString("TargetLock", tool.getToolName());

            item.setTagCompound(compound);
            list.add(item);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconRegister) {
        this.itemIcon = iconRegister.registerIcon("tinker:skull_char_gold");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean adv) {
        list.add(
                StatCollector.translateToLocal("modifier.tooltip.Main") + " "
                        + StatCollector.translateToLocal("modifier.tooltip.Creative"));
        if (stack.hasTagCompound()) {
            String targetLock;
            targetLock = stack.getTagCompound().getString("TargetLock");
            targetLock = StatCollector.translateToLocal("tool." + targetLock.toLowerCase());
            list.add(StatCollector.translateToLocal("creativeModLock.tooltip") + targetLock);
        }
    }
}
