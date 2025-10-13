package tconstruct.tools.items;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.w3c.dom.Document;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import mantle.books.BookData;
import mantle.books.BookDataStore;
import tconstruct.TConstruct;
import tconstruct.client.TProxyClient;

/**
 * This class is now just a constructor with side effects, so a glorified method call. TODO: Clean up when breaking API
 * change is deemed acceptable.
 */
public class ManualInfo {

    public ManualInfo() {
        Side side = FMLCommonHandler.instance().getEffectiveSide();
        initManual(
                new BookData(),
                "tconstruct.manual.beginner",
                "\u00a7o" + StatCollector.translateToLocal("manual1.tooltip"),
                side == Side.CLIENT ? TProxyClient.volume1 : null,
                "tinker:tinkerbook_diary");
        initManual(
                new BookData(),
                "tconstruct.manual.toolstation",
                "\u00a7o" + StatCollector.translateToLocal("manual2.tooltip"),
                side == Side.CLIENT ? TProxyClient.volume2 : null,
                "tinker:tinkerbook_toolstation");
        initManual(
                new BookData(),
                "tconstruct.manual.smeltery",
                "\u00a7o" + StatCollector.translateToLocal("manual3.tooltip"),
                side == Side.CLIENT ? TProxyClient.smelter : null,
                "tinker:tinkerbook_smeltery");
        initManual(
                new BookData(),
                "tconstruct.manual.diary",
                "\u00a7o" + StatCollector.translateToLocal("manual4.tooltip"),
                side == Side.CLIENT ? TProxyClient.diary : null,
                "tinker:tinkerbook_blue");
        initManual(
                new BookData(),
                "tconstruct.manual.weaponry",
                "\u00a7o" + StatCollector.translateToLocal("manual5.tooltip"),
                side == Side.CLIENT ? TProxyClient.weaponry : null,
                "tinker:tinkerbook_green");
    }

    public BookData initManual(BookData data, String unlocName, String toolTip, Document xmlDoc, String itemImage) {
        data.unlocalizedName = unlocName;
        data.toolTip = unlocName;
        data.modID = TConstruct.modID;
        data.itemImage = new ResourceLocation(data.modID, itemImage);
        data.doc = xmlDoc;
        BookDataStore.addBook(data);
        return data;
    }
}
