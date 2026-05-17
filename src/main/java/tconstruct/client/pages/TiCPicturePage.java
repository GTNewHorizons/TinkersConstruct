package tconstruct.client.pages;

import static mantle.lib.CoreRepo.logger;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import mantle.client.pages.BookPage;

public class TiCPicturePage extends BookPage {

    String text;
    String location;
    ResourceLocation background;

    @Override
    public void readPageFromXML(Element element) {
        NodeList nodes = element.getElementsByTagName("text");
        if (nodes != null) text = nodes.item(0).getTextContent();
        if (StatCollector.canTranslate(this.text)) this.text = StatCollector.translateToLocal(this.text);

        nodes = element.getElementsByTagName("location");
        if (nodes != null) {
            location = nodes.item(0).getTextContent();
            background = new ResourceLocation(location);
            if (background == null) {
                logger.warn(nodes.item(0).getTextContent() + " could not be found in the image cache(location)!");
            }
        }

    }

    @Override
    public void renderContentLayer(int localWidth, int localHeight, boolean isTranslatable) {
        manual.fonts.drawSplitString(text, localWidth + 8, localHeight + 6, 178, 0);
    }

    public void renderBackgroundLayer(int localWidth, int localHeight) {
        if (background != null) {
            manual.getMC().getTextureManager().bindTexture(background);

            manual.drawTexturedModalRect(
                    localWidth,
                    localHeight + (TiCBookPage.PAGECONTENTHEIGHT - 120) / 2,
                    0,
                    0,
                    170,
                    144);
        }
    }

}
