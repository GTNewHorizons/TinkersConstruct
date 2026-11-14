package tconstruct.armor.items;

import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import baubles.api.expanded.IBaubleExpanded;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mantle.items.abstracts.CraftingItem;
import tconstruct.TConstruct;
import tconstruct.armor.player.ArmorExtended;
import tconstruct.armor.player.TPlayerStats;
import tconstruct.compat.BaublesHelper;
import tconstruct.compat.LoadedMods;
import tconstruct.library.TConstructRegistry;
import tconstruct.library.accessory.IHealthAccessory;
import tconstruct.util.InventoryHelper;
import tconstruct.util.config.PHConstruct;

@Optional.InterfaceList({
        @Optional.Interface(modid = "Baubles|Expanded", iface = "baubles.api.expanded.IBaubleExpanded"),
        @Optional.Interface(modid = "Baubles", iface = "baubles.api.IBauble") })
public class HeartCanister extends CraftingItem implements IHealthAccessory, IBauble, IBaubleExpanded {

    public HeartCanister() {
        super(
                new String[] { "empty", "miniheart.red", "red", "miniheart.yellow", "yellow", "miniheart.green",
                        "green" },
                new String[] { "canister_empty", "miniheart_red", "canister_red", "miniheart_yellow", "canister_yellow",
                        "miniheart_green", "canister_green" },
                "",
                "tinker",
                TConstructRegistry.materialTab);
        this.setMaxStackSize(10);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        int meta = stack.getItemDamage();
        if (isMiniHeart(meta)) {
            player.setItemInUse(stack, this.getMaxItemUseDuration(stack));
        } else if (!world.isRemote && isCanister(meta)) {
            if (PHConstruct.enableTinkerInventoryTab) {
                equipToAccessoryTab(player, stack, accessorySlot(meta));
            } else if (LoadedMods.baubles) {
                equipToBaubles(player, stack);
            }
        }
        return stack;
    }

    @Override
    public ItemStack onEaten(ItemStack stack, World world, EntityPlayer player) {
        int meta = stack.getItemDamage();
        --stack.stackSize;
        player.heal((meta + 1) * 10);
        world.playSoundAtEntity(player, "random.burp", 0.5F, world.rand.nextFloat() * 0.1F + 0.9F);
        return stack;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack par1ItemStack) {
        return EnumAction.eat;
    }

    public int getMaxItemUseDuration(ItemStack par1ItemStack) {
        return 32;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4) {
        int meta = stack.getItemDamage();
        if (!isCanister(meta)) list.add(StatCollector.translateToLocal("item.crafting.tooltip"));
        else {
            list.add(StatCollector.translateToLocal("item.accessory.tooltip"));
            list.add(StatCollector.translateToLocal("canister.tooltip"));
        }

        switch (meta) {
            case 1:
                list.add(StatCollector.translateToLocal("canister.red.tooltip1"));
                list.add(StatCollector.translateToLocal("canister.red.tooltip2"));
                break;
            case 3:
                list.add(StatCollector.translateToLocal("canister.yellow.tooltip1"));
                list.add(StatCollector.translateToLocal("canister.yellow.tooltip2"));
                break;
            case 5:
                list.add(StatCollector.translateToLocal("canister.green.tooltip1"));
                list.add(StatCollector.translateToLocal("canister.green.tooltip2"));
                break;
        }
    }

    @Override
    public boolean canEquipAccessory(ItemStack item, int slot) {
        return accessorySlot(item.getItemDamage()) == slot;
    }

    private static void equipToAccessoryTab(EntityPlayer player, ItemStack stack, int slot) {
        TPlayerStats stats = TPlayerStats.get(player);
        if (stats == null || stats.armor == null) {
            return;
        }
        ArmorExtended armor = stats.armor;
        stack.stackSize -= InventoryHelper.insertIntoSlot(armor, slot, stack, armor.getInventoryStackLimit(), 1);
        armor.recalculateHealth(player, stats);
    }

    @Optional.Method(modid = "Baubles")
    private static void equipToBaubles(EntityPlayer player, ItemStack stack) {
        BaublesHelper.tryEquipOne(player, stack);
    }

    @Override
    @Optional.Method(modid = "Baubles|Expanded")
    public String[] getBaubleTypes(ItemStack itemstack) {
        return switch (itemstack.getItemDamage()) {
            case 2 -> new String[] { TConstruct.HEART_CANISTER_RED_TYPE };
            case 4 -> new String[] { TConstruct.HEART_CANISTER_YELLOW_TYPE };
            case 6 -> new String[] { TConstruct.HEART_CANISTER_GREEN_TYPE };
            default -> new String[0];
        };
    }

    @Override
    @Optional.Method(modid = "Baubles|Expanded")
    public void onSlotContentsChanged(ItemStack itemstack, EntityLivingBase player) {
        recalculatePlayerHealth(player);
    }

    @Override
    @Optional.Method(modid = "Baubles")
    public BaubleType getBaubleType(ItemStack itemStack) {
        return BaubleType.UNIVERSAL;
    }

    @Override
    @Optional.Method(modid = "Baubles")
    public void onWornTick(ItemStack itemstack, EntityLivingBase player) {}

    @Override
    @Optional.Method(modid = "Baubles")
    public void onEquipped(ItemStack itemstack, EntityLivingBase player) {
        recalculatePlayerHealth(player);
    }

    @Override
    @Optional.Method(modid = "Baubles")
    public void onUnequipped(ItemStack itemstack, EntityLivingBase player) {
        recalculatePlayerHealth(player);
    }

    @Override
    @Optional.Method(modid = "Baubles")
    public boolean canEquip(ItemStack itemstack, EntityLivingBase player) {
        return isCanister(itemstack.getItemDamage());
    }

    @Override
    @Optional.Method(modid = "Baubles")
    public boolean canUnequip(ItemStack itemstack, EntityLivingBase player) {
        return true;
    }

    private static boolean isMiniHeart(int meta) {
        return meta == 1 || meta == 3 || meta == 5;
    }

    private static boolean isCanister(int meta) {
        return meta == 2 || meta == 4 || meta == 6;
    }

    private static int accessorySlot(int meta) {
        return switch (meta) {
            case 2 -> 6;
            case 4 -> 5;
            case 6 -> 4;
            default -> -1;
        };
    }

    private void recalculatePlayerHealth(EntityLivingBase entity) {
        if (!(entity instanceof EntityPlayer player)) {
            return;
        }
        TPlayerStats stats = TPlayerStats.get(player);
        if (stats != null && stats.armor != null) {
            stats.armor.recalculateHealth(player, stats);
        }
    }

    @Override
    public int getHealthBoost(ItemStack item) {
        return item.stackSize * 2;
    }
}
