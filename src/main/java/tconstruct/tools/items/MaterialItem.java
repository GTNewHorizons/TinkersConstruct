package tconstruct.tools.items;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mantle.items.abstracts.CraftingItem;
import tconstruct.library.TConstructRegistry;

public class MaterialItem extends CraftingItem {

    public MaterialItem() {
        super(materialNames, getTextures(), "materials/", "tinker", TConstructRegistry.materialTab);
    }

    private static String[] getTextures() {
        String[] names = new String[craftingTextures.length];
        for (int i = 0; i < craftingTextures.length; i++) {
            if (craftingTextures[i].equals("")) names[i] = "";
            else names[i] = "material_" + craftingTextures[i];
        }
        return names;
    }

    static String[] materialNames = new String[] { "PaperStack", "SlimeCrystal", "SearedBrick", "CobaltIngot",
            "ArditeIngot", "ManyullynIngot", "Mossball", "LavaCrystal", "NecroticBone", "CopperIngot", "TinIngot",
            "AluminumIngot", "RawAluminum", "BronzeIngot", "AluBrassIngot", "AlumiteIngot", "SteelIngot",
            "BlueSlimeCrystal", "ObsidianIngot", "IronNugget", "CopperNugget", "TinNugget", "AluminumNugget",
            "EssenceCrystal", "AluBrassNugget", "SilkyCloth", "SilkyJewel", "ObsidianNugget", "CobaltNugget",
            "ArditeNugget", "ManyullynNugget", "BronzeNugget", "AlumiteNugget", "SteelNugget", "PigIronIngot",
            "PigIronNugget", "GlueBall", "SearedBrick", "ArditeDust", "CobaltDust", "AluminumDust", "ManyullynDust",
            "AluBrassDust", "Reinforcement" };

    static String[] craftingTextures = new String[] { "paperstack", "slimecrystal", "searedbrick", "cobaltingot",
            "arditeingot", "manyullyningot", "mossball", "lavacrystal", "necroticbone", "copperingot", "tiningot",
            "aluminumingot", "aluminumraw", "bronzeingot", "alubrassingot", "alumiteingot", "steelingot",
            "blueslimecrystal", "obsidianingot", "nugget_iron", "nugget_copper", "nugget_tin", "nugget_aluminum", "",
            "nugget_alubrass", "silkycloth", "silkyjewel", "nugget_obsidian", "nugget_cobalt", "nugget_ardite",
            "nugget_manyullyn", "nugget_bronze", "nugget_alumite", "nugget_steel", "pigironingot", "nugget_pigiron",
            "glueball", "searedbrick_nether", "ardite_dust", "cobalt_dust", "aluminum_dust", "manyullyn_dust",
            "alubrass_dust", "reinforcement" };

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean par4) {
        switch (stack.getItemDamage()) {
            case 6:
                list.add(
                        StatCollector.translateToLocal("modifier.tooltip.Main") + "\u00a72 "
                                + StatCollector.translateToLocal("modifier.tool.moss"));
                break;
            case 7:
                list.add(
                        StatCollector.translateToLocal("modifier.tooltip.Main") + "\u00a74 "
                                + StatCollector.translateToLocal("modifier.tooltip.Auto-Smelt"));
                break;
            case 8:
                list.add(
                        StatCollector.translateToLocal("modifier.tooltip.Main") + "\u00a78 "
                                + StatCollector.translateToLocal("modifier.tool.necro"));
                break;
            case 26:
                list.add(
                        StatCollector.translateToLocal("modifier.tooltip.Main") + "\u00a7e "
                                + StatCollector.translateToLocal("modifier.tooltip.Silky"));
                break;
            case 43:
                list.add(
                        StatCollector.translateToLocal("modifier.tooltip.Main") + "\u00a75 "
                                + StatCollector.translateToLocal("tool.reinforced"));
                break;
            default:
                break;
        }
    }
}
