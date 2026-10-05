package tconstruct.items.tools;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import tconstruct.TConstruct;
import tconstruct.library.crafting.Detailing.DetailInput;
import tconstruct.library.tools.AbilityHelper;
import tconstruct.library.tools.ToolCore;
import tconstruct.tools.TinkerTools;

public class Chisel extends ToolCore {

    public Chisel() {
        super(0);
        this.setUnlocalizedName("InfiTool.Chisel");
        this.setContainerItem(this);
    }

    @Override
    public ItemStack getContainerItem(ItemStack itemStack) {
        if (itemStack.hasTagCompound()) {
            int reinforced = 0;
            NBTTagCompound tags = itemStack.getTagCompound();

            if (tags.getCompoundTag("InfiTool").hasKey("Unbreaking"))
                reinforced = tags.getCompoundTag("InfiTool").getInteger("Unbreaking");

            if (random.nextInt(10) < 10 - reinforced) {
                AbilityHelper.damageTool(itemStack, 1, null, false);
            }
        }
        return itemStack;
    }

    @Override
    public boolean doesContainerItemLeaveCraftingGrid(ItemStack par1ItemStack) {
        return par1ItemStack.hasTagCompound()
                && par1ItemStack.getTagCompound().getCompoundTag("InfiTool").getBoolean("Broken");
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
            float clickX, float clickY, float clickZ) {
        if (!player.capabilities.isCreativeMode && (!stack.hasTagCompound()
                || stack.getTagCompound().getCompoundTag("InfiTool").getBoolean("Broken"))) return false;
        if (!world.canMineBlock(player, x, y, z) || !player.canPlayerEdit(x, y, z, side, stack)) return false;

        Block block = world.getBlock(x, y, z);
        int meta = world.getBlockMetadata(x, y, z);
        DetailInput details = TConstruct.chiselDetailing.getDetailing(block, meta);
        if (details == null || world.getTileEntity(x, y, z) != null) return false;
        if (world.isRemote) return true;

        if (!world.setBlock(x, y, z, Block.getBlockFromItem(details.output.getItem()), details.outputMeta, 3))
            return false;
        if (!player.capabilities.isCreativeMode) {
            int reinforced = stack.getTagCompound().getCompoundTag("InfiTool").getInteger("Unbreaking");
            if (random.nextInt(10) < 10 - reinforced) {
                AbilityHelper.damageTool(stack, 1, null, false);
            }
        }
        world.playAuxSFX(2001, x, y, z, Block.getIdFromBlock(block) + (meta << 12));
        return true;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        return stack;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack itemstack) {
        return EnumAction.none;
    }

    @Override
    public int getPartAmount() {
        return 2;
    }

    @Override
    public void registerPartPaths(int index, String[] location) {
        headStrings.put(index, location[0]);
        brokenPartStrings.put(index, location[1]);
        handleStrings.put(index, location[2]);
    }

    @Override
    public String getIconSuffix(int partType) {
        switch (partType) {
            case 0:
                return "_chisel_head";
            case 1:
                return "_chisel_head_broken";
            case 2:
                return "_chisel_handle";
            default:
                return "";
        }
    }

    @Override
    public String getEffectSuffix() {
        return "_chisel_effect";
    }

    @Override
    public String getDefaultFolder() {
        return "chisel";
    }

    @Override
    public Item getHeadItem() {
        return TinkerTools.chiselHead;
    }

    @Override
    public Item getAccessoryItem() {
        return null;
    }

    @Override
    public String[] getTraits() {
        return new String[] { "utility" };
    }
}
