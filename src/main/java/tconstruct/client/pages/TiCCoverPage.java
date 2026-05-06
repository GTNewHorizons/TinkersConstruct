package tconstruct.client.pages;

import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import mantle.client.pages.BookPage;

public class TiCCoverPage extends BookPage {

    // page height 165
    // page wdith 190

    String[] innerText;

    @Override
    public void readPageFromXML(Element element) {
        NodeList nodes = element.getElementsByTagName("text");
        String tempText = nodes.item(0).getTextContent();
        if (StatCollector.canTranslate(tempText)) tempText = StatCollector.translateToLocal(tempText);
        innerText = tempText.split("\\\\n");
    }

    private void drawStrCenterAt(String str, int X, int Y, float scale, int color) {
        manual.fonts.drawString(
                str,
                (int) (X / scale - manual.fonts.getStringWidth(str) / 2),
                (int) ((Y - manual.fonts.FONT_HEIGHT / 2) / scale),
                color);
    }

    @Override
    public void renderContentLayer(int startX, int startY, boolean isTranslatable) {

        int cousorX = 190 / 2;
        int cousorY = 165 * 3 / 7;

        float scale = 2.5f;
        GL11.glScalef(scale, scale, 1.0f);
        this.drawStrCenterAt(innerText[0], startX + cousorX, startY + cousorY, scale, 0x000000);
        cousorY += manual.fonts.FONT_HEIGHT * scale;
        GL11.glScalef(1.0f / scale, 1.0f / scale, 1.0f);

        scale = 1.0f;
        GL11.glScalef(scale, scale, 1.0f);
        this.drawStrCenterAt(innerText[1], startX + cousorX, startY + cousorY, scale, 0x000000);
        cousorY += manual.fonts.FONT_HEIGHT * scale;
        this.drawStrCenterAt(innerText[2], startX + cousorX, startY + cousorY, scale, 0x000000);
        cousorY += manual.fonts.FONT_HEIGHT * scale;
        GL11.glScalef(1.0f / scale, 1.0f / scale, 1.0f);

    }

}
