package tconstruct.tools.items;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.w3c.dom.Document;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import mantle.books.BookDataStore;
import tconstruct.TConstruct;
import tconstruct.client.TProxyClient;
import tconstruct.library.util.TiCBookData;

/**
 * This class is now just a constructor with side effects, so a glorified method call. TODO: Clean up when breaking API
 * change is deemed acceptable.
 */
public class ManualInfo {

    public ManualInfo() {
        Side side = FMLCommonHandler.instance().getEffectiveSide();
        BookDataStore.addBook(
                initManual(
                        new TiCBookData(),
                        "tconstruct.manual.beginner",
                        "\u00a7o" + StatCollector.translateToLocal("manual1.tooltip"),
                        side == Side.CLIENT ? TProxyClient.volume1 : null,
                        "tinker:tinkerbook_diary",
                        0xD89A1D));
        BookDataStore.addBook(
                initManual(
                        new TiCBookData(),
                        "tconstruct.manual.toolstation",
                        "\u00a7o" + StatCollector.translateToLocal("manual2.tooltip"),
                        side == Side.CLIENT ? TProxyClient.volume2 : null,
                        "tinker:tinkerbook_toolstation",
                        0xD61E17));
        BookDataStore.addBook(
                initManual(
                        new TiCBookData(),
                        "tconstruct.manual.smeltery",
                        "\u00a7o" + StatCollector.translateToLocal("manual3.tooltip"),
                        side == Side.CLIENT ? TProxyClient.smelter : null,
                        "tinker:tinkerbook_smeltery",
                        0xB6B6B6));
        BookDataStore.addBook(
                initManual(
                        new TiCBookData(),
                        "tconstruct.manual.diary",
                        "\u00a7o" + StatCollector.translateToLocal("manual4.tooltip"),
                        side == Side.CLIENT ? TProxyClient.diary : null,
                        "tinker:tinkerbook_blue",
                        0x21BBDC));
        BookDataStore.addBook(
                initManual(
                        new TiCBookData(),
                        "tconstruct.manual.weaponry",
                        "\u00a7o" + StatCollector.translateToLocal("manual5.tooltip"),
                        side == Side.CLIENT ? TProxyClient.weaponry : null,
                        "tinker:tinkerbook_green",
                        0x27CD1B));
    }

    public TiCBookData initManual(TiCBookData data, String unlocName, String toolTip, Document xmlDoc, String itemImage,
            int color) {
        return data.setUnlocalizedName(unlocName).setToolTip(unlocName).setModID(TConstruct.modID)
                .setItemImage(new ResourceLocation(data.modID, itemImage)).setDoc(xmlDoc).setBookColor(color);
    }
}
