package tconstruct.tools.items;

import java.util.List;
import java.util.Objects;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mantle.books.BookData;
import mantle.books.BookDataStore;
import mantle.client.gui.GuiManual;
import mantle.items.abstracts.CraftingItem;
import tconstruct.TConstruct;
import tconstruct.achievements.TAchievements;
import tconstruct.library.TConstructRegistry;

public class Manual extends CraftingItem {

    static String[] name = new String[] { "beginner", "toolstation", "smeltery", "diary", "weaponry" };
    static String[] textureName = new String[] { "tinkerbook_diary", "tinkerbook_toolstation", "tinkerbook_smeltery",
            "tinkerbook_blue", "tinkerbook_green" };

    public Manual() {
        super(name, textureName, "", "tinker", TConstructRegistry.materialTab);
        setUnlocalizedName("tconstruct.manual");
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        TAchievements.triggerAchievement(player, "tconstruct.beginner");

        if (world.isRemote) {
            openBook(stack, world, player);
        }
        return stack;
    }

    @SideOnly(Side.CLIENT)
    public void openBook(ItemStack stack, World world, EntityPlayer player) {
        BookData data = BookDataStore.getBookfromName(TConstruct.modID, getBookName(stack.getItemDamage()));
        if (Objects.nonNull(data)) {
            player.openGui(TConstruct.instance, mantle.client.MProxyClient.manualGuiID, world, 0, 0, 0);
            FMLClientHandler.instance().displayGuiScreen(player, new GuiManual(stack, data));
        }
    }

    private static String getBookName(int bookItemDamage) {
        return switch (bookItemDamage) {
            case 0 -> "tconstruct.manual.beginner";
            case 1 -> "tconstruct.manual.toolstation";
            case 2 -> "tconstruct.manual.smeltery";
            case 4 -> "tconstruct.manual.weaponry";
            default -> "tconstruct.manual.diary";
        };
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean par4) {
        switch (stack.getItemDamage()) {
            case 0:
                list.add("\u00a7o" + StatCollector.translateToLocal("manual1.tooltip"));
                break;
            case 1:
                list.add("\u00a7o" + StatCollector.translateToLocal("manual2.tooltip"));
                break;
            case 2:
                list.add("\u00a7o" + StatCollector.translateToLocal("manual3.tooltip"));
                break;
            case 4:
                list.add("\u00a7o" + StatCollector.translateToLocal("manual4.tooltip"));
                break;
            default:
                list.add("\u00a7o" + StatCollector.translateToLocal("manual5.tooltip"));
                break;
        }
    }
}
