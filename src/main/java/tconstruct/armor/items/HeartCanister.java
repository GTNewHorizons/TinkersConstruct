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
import tconstruct.library.TConstructRegistry;
import tconstruct.library.accessory.IHealthAccessory;
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
        if (meta == 1 || meta == 3 || meta == 5) {
            player.setItemInUse(stack, this.getMaxItemUseDuration(stack));
        }
        if (!world.isRemote && PHConstruct.enableTinkerInventoryTab && (meta == 2 || meta == 4 || meta == 6)) {
            TPlayerStats stats = TPlayerStats.get(player);
            if (stats != null && stats.armor != null) {
                ArmorExtended armor = stats.armor;
                int targetSlot = meta == 2 ? 6 : meta == 4 ? 5 : 4;
                ItemStack slotStack = armor.getStackInSlot(targetSlot);
                if (slotStack == null) // || slotStack.getItem() == this)
                {
                    armor.setInventorySlotContents(targetSlot, new ItemStack(this, 1, meta));
                    stack.stackSize--;
                } else if (slotStack.getItem() == this && slotStack.getItemDamage() == meta
                        && slotStack.stackSize < this.maxStackSize) {
                            slotStack.stackSize++;
                            stack.stackSize--;
                        }
                armor.recalculateHealth(player, stats);
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
        if (meta == 0 || meta % 2 == 1) list.add(StatCollector.translateToLocal("item.crafting.tooltip"));
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
        int type = item.getItemDamage();
        return ((type == 2 && slot == 6) || (type == 4 && slot == 5) || (type == 6 && slot == 4));
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
    @Optional.Method(modid = "Baubles")
    public BaubleType getBaubleType(ItemStack itemStack) {
        int meta = itemStack.getItemDamage();
        return (meta == 2 || meta == 4 || meta == 6) ? BaubleType.UNIVERSAL : BaubleType.RING;
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
        int meta = itemstack.getItemDamage();
        return meta == 2 || meta == 4 || meta == 6;
    }

    @Override
    @Optional.Method(modid = "Baubles")
    public boolean canUnequip(ItemStack itemstack, EntityLivingBase player) {
        return true;
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
