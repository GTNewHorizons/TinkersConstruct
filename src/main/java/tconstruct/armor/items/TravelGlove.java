package tconstruct.armor.items;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import baubles.api.expanded.BaubleExpandedSlots;
import baubles.api.expanded.IBaubleExpanded;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import tconstruct.armor.ArmorProxyClient;
import tconstruct.library.accessory.AccessoryCore;
import tconstruct.library.accessory.IAccessoryModel;

@Optional.InterfaceList({
        @Optional.Interface(modid = "Baubles|Expanded", iface = "baubles.api.expanded.IBaubleExpanded"),
        @Optional.Interface(modid = "Baubles", iface = "baubles.api.IBauble") })
public class TravelGlove extends AccessoryCore implements IAccessoryModel, IBauble, IBaubleExpanded {

    public TravelGlove() {
        super("travelgear/travel_glove");
    }

    @Override
    public boolean canEquipAccessory(ItemStack item, int slot) {
        return slot == 1;
    }

    @Override
    @Optional.Method(modid = "Baubles|Expanded")
    public String[] getBaubleTypes(ItemStack itemstack) {
        return new String[] { BaubleExpandedSlots.universalType };
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
    public boolean isItemTool(ItemStack stack) {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected void registerModifiers(IIconRegister iconRegister) {
        this.modifiers = new IIcon[4];
        this.modifiers[0] = iconRegister.registerIcon("tinker:travelgear/glove_guard");
        this.modifiers[1] = iconRegister.registerIcon("tinker:travelgear/glove_speedaura");
        this.modifiers[2] = iconRegister.registerIcon("tinker:travelgear/glove_spines");
        this.modifiers[3] = iconRegister.registerIcon("tinker:travelgear/glove_sticky");
    }

    @SideOnly(Side.CLIENT)
    public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, int armorSlot) {
        return ArmorProxyClient.glove;
    }

    ResourceLocation texture = new ResourceLocation("tinker", "textures/armor/travel_1.png");

    @Override
    @SideOnly(Side.CLIENT)
    public ResourceLocation getWearbleTexture(Entity entity, ItemStack stack, int slot) {
        return texture;
    }

    @Override
    public String[] getTraits() {
        return new String[] { "accessory", "glove" };
    }
}
