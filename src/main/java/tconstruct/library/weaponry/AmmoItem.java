package tconstruct.library.weaponry;

import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import baubles.api.expanded.BaubleExpandedSlots;
import baubles.api.expanded.IBaubleExpanded;
import baubles.common.lib.PlayerHandler;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.battlegear2.api.PlayerEventChild;
import mods.battlegear2.api.weapons.IBattlegearWeapon;
import tconstruct.compat.LoadedMods;
import tconstruct.library.TConstructRegistry;
import tconstruct.library.tools.ToolCore;
import tconstruct.tools.TinkerTools;

@Optional.InterfaceList({
        @Optional.Interface(modid = "battlegear2", iface = "mods.battlegear2.api.weapons.IBattlegearWeapon"),
        @Optional.Interface(modid = "Baubles|Expanded", iface = "baubles.api.expanded.IBaubleExpanded"),
        @Optional.Interface(modid = "Baubles", iface = "baubles.api.IBauble") })
public abstract class AmmoItem extends ToolCore implements IBattlegearWeapon, IAmmo, IBauble, IBaubleExpanded {

    public AmmoItem(int baseDamage, String name) {
        super(baseDamage);
        this.setCreativeTab(TConstructRegistry.weaponryTab);
    }

    @Override
    public int getAmmoCount(ItemStack stack) {
        if (!stack.hasTagCompound()) return 0;
        NBTTagCompound tags = stack.getTagCompound().getCompoundTag("InfiTool");
        return tags.getInteger("Ammo");
    }

    @Override
    public int getMaxAmmo(ItemStack stack) {
        if (!stack.hasTagCompound()) return 0;
        NBTTagCompound tags = stack.getTagCompound().getCompoundTag("InfiTool");
        return getMaxAmmo(tags);
    }

    @Override
    public int getMaxAmmo(NBTTagCompound tags) {
        float dur = tags.getInteger("TotalDurability");
        return Math.max(1, (int) Math.ceil(dur * getAmmoModifier()));
    }

    @Override
    public int addAmmo(int toAdd, ItemStack stack) {
        if (!stack.hasTagCompound()) return toAdd;
        NBTTagCompound tags = stack.getTagCompound().getCompoundTag("InfiTool");
        int oldCount = tags.getInteger("Ammo");
        int newCount = Math.min(oldCount + toAdd, getMaxAmmo(stack));
        tags.setInteger("Ammo", newCount);
        return toAdd - (newCount - oldCount);
    }

    @Override
    public int consumeAmmo(int toUse, ItemStack stack) {
        if (!stack.hasTagCompound()) return toUse;
        NBTTagCompound tags = stack.getTagCompound().getCompoundTag("InfiTool");
        int oldCount = tags.getInteger("Ammo");
        int newCount = Math.max(oldCount - toUse, 0);
        tags.setInteger("Ammo", newCount);
        return toUse - (oldCount - newCount);
    }

    @Override
    public void setAmmo(int count, ItemStack stack) {
        if (!stack.hasTagCompound()) return;

        NBTTagCompound tags = stack.getTagCompound().getCompoundTag("InfiTool");
        tags.setInteger("Ammo", count);
    }

    public float getAmmoModifier() {
        return 0.1f;
    }

    public boolean pickupAmmo(ItemStack stack, ItemStack candidate, EntityPlayer player) {
        if (stack.getItem() == null || !stack.hasTagCompound() || !(stack.getItem() instanceof IAmmo pickedup)) {
            return false;
        }

        // check if our candidate fits
        if (candidate != null) {
            if (tryStackAmmo(stack, pickedup, candidate)) return true;
        }

        // search the player's inventory
        for (ItemStack invItem : player.inventory.mainInventory) {
            if (tryStackAmmo(stack, pickedup, invItem)) return true;
        }

        // search bauble slots
        if (LoadedMods.baubles) {
            for (ItemStack bauble : PlayerHandler.getPlayerBaubles(player).stackList) {
                if (tryStackAmmo(stack, pickedup, bauble)) return true;
            }
        }

        // couldn't find a matching thing.
        return false;
    }

    private boolean tryStackAmmo(ItemStack stack, IAmmo pickedup, ItemStack bauble) {
        if (!testIfAmmoMatches(stack, bauble)) {
            return false;
        }

        IAmmo ininventory = ((IAmmo) bauble.getItem());
        // we can be sure that it's ammo, since stack is ammo and they're equal
        int count = pickedup.getAmmoCount(stack);
        return count != ininventory.addAmmo(count, bauble);
    }

    private boolean testIfAmmoMatches(ItemStack reference, ItemStack candidate) {
        if (candidate == null) return false;
        if (!candidate.hasTagCompound() || !candidate.getTagCompound().hasKey("InfiTool")) return false;

        if (reference.getItem() != candidate.getItem()) return false;

        NBTTagCompound referenceTags = getComparisonTags(reference);
        NBTTagCompound testTags = getComparisonTags(candidate);

        return referenceTags.equals(testTags);
    }

    private NBTTagCompound getComparisonTags(ItemStack stack) {
        NBTTagCompound tags = stack.getTagCompound().getCompoundTag("InfiTool");
        NBTTagCompound out = new NBTTagCompound();

        copyTag(out, tags, "Head");
        copyTag(out, tags, "Handle");
        copyTag(out, tags, "Accessory");
        copyTag(out, tags, "Extra");
        copyTag(out, tags, "RenderHead");
        copyTag(out, tags, "RenderHandle");
        copyTag(out, tags, "RenderAccessory");
        copyTag(out, tags, "RenderExtra");
        copyTag(out, tags, "TotalDurability");
        copyTag(out, tags, "Attack");
        copyTag(out, tags, "MiningSpeed");
        copyTag(out, tags, "HarvestLevel");
        copyTag(out, tags, "Modifiers");

        return out;
    }

    private void copyTag(NBTTagCompound out, NBTTagCompound in, String tag) {
        if (in.hasKey(tag)) out.setInteger(tag, in.getInteger(tag));
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
        // ammo doesn't hurt on smacking stuff with it
        return false;
    }

    @Override
    @Optional.Method(modid = "battlegear2")
    public boolean sheatheOnBack(ItemStack item) {
        return true;
    }

    @Override
    @Optional.Method(modid = "battlegear2")
    public boolean isOffhandHandDual(ItemStack off) {
        return true;
    }

    @Override
    @Optional.Method(modid = "battlegear2")
    public boolean offhandAttackEntity(PlayerEventChild.OffhandAttackEvent event, ItemStack mainhandItem,
            ItemStack offhandItem) {
        event.cancelParent = false;
        event.swingOffhand = false;
        event.shouldAttack = false;
        return false;
    }

    @Override
    @Optional.Method(modid = "battlegear2")
    public boolean offhandClickAir(PlayerInteractEvent event, ItemStack mainhandItem, ItemStack offhandItem) {
        event.setCanceled(false);
        return false;
    }

    @Override
    @Optional.Method(modid = "battlegear2")
    public boolean offhandClickBlock(PlayerInteractEvent event, ItemStack mainhandItem, ItemStack offhandItem) {
        event.setCanceled(false);
        return false;
    }

    @Override
    @Optional.Method(modid = "battlegear2")
    public void performPassiveEffects(Side effectiveSide, ItemStack mainhandItem, ItemStack offhandItem) {
        // unused
    }

    @Override
    @Optional.Method(modid = "battlegear2")
    public boolean allowOffhand(ItemStack mainhand, ItemStack offhand) {
        if (offhand == null) return true;
        return (mainhand != null && mainhand.getItem() != TinkerTools.cleaver
                && mainhand.getItem() != TinkerTools.battleaxe)
                && (offhand.getItem() != TinkerTools.cleaver && offhand.getItem() != TinkerTools.battleaxe);
    }

    @Override
    @Optional.Method(modid = "Baubles|Expanded")
    public String[] getBaubleTypes(ItemStack itemstack) {
        return new String[] { BaubleExpandedSlots.quiverType };
    }

    // Fallback for base Baubles
    @Override
    @Optional.Method(modid = "Baubles")
    public BaubleType getBaubleType(ItemStack itemStack) {
        return BaubleType.RING;
    }

    @Override
    @Optional.Method(modid = "Baubles")
    public void onWornTick(ItemStack itemstack, EntityLivingBase player) {
        onUpdate(itemstack, player.worldObj, player, 0, false);
    }

    @Override
    @Optional.Method(modid = "Baubles")
    public void onEquipped(ItemStack itemstack, EntityLivingBase player) {}

    @Override
    @Optional.Method(modid = "Baubles")
    public void onUnequipped(ItemStack itemstack, EntityLivingBase player) {}

    @Override
    @Optional.Method(modid = "Baubles")
    public boolean canEquip(ItemStack itemstack, EntityLivingBase player) {
        return true;
    }

    @Override
    @Optional.Method(modid = "Baubles")
    public boolean canUnequip(ItemStack itemstack, EntityLivingBase player) {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> lines, boolean advanced) {
        super.addInformation(stack, player, lines, advanced);
        if (LoadedMods.baubles) addBaubleInformation(lines);
    }

    @SideOnly(Side.CLIENT)
    @Optional.Method(modid = "Baubles")
    private void addBaubleInformation(List<String> lines) {
        if (LoadedMods.baublesExpanded) {
            if (GuiScreen.isShiftKeyDown()) {
                lines.add(StatCollector.translateToLocal("tooltip.compatibleslots"));
                lines.add(StatCollector.translateToLocal("slot.quiver"));
                if (LoadedMods.tiCTooltips) lines.add(""); // Required for spacing
            } else if (!LoadedMods.tiCTooltips) {
                lines.add(StatCollector.translateToLocal("tooltip.shiftprompt"));
            }
        } else {
            lines.add(StatCollector.translateToLocal("baubletype.any"));
        }
    }
}
