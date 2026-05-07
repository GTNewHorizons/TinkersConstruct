package tconstruct.client.pages;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import tconstruct.TConstruct;
import tconstruct.library.client.TConstructClientRegistry;
import tconstruct.library.util.TiCNavigationButton;
import tconstruct.library.util.TiCNavigationButton.ButtonSize;

public class NavigationPage extends TiCButtonBookPage {

    private static final ButtonSize BS = ButtonSize.large;

    @Override
    public void readPageFromXML(Element element) {
        this.pageButtonList = new ArrayList<>();
        NodeList buttonList = element.getElementsByTagName("button");
        for (int idx = 0; idx < buttonList.getLength(); idx++) {
            Element b = (Element) buttonList.item(idx);

            String naviTo = b.getAttribute("to");
            String tempText = b.getElementsByTagName("text").item(0).getTextContent();
            if (StatCollector.canTranslate(tempText)) tempText = StatCollector.translateToLocal(tempText);

            String iconStr = b.getElementsByTagName("icon").item(0).getTextContent();
            ItemStack iconStack = TConstructClientRegistry.getOrRegisterManualIcon(iconStr);

            this.pageButtonList.add(new TiCNavigationButton(0, BS, iconStack, tempText, naviTo, this));
        }
    }

    public void updateButtonPositionAndRender(int startX, int startY, float scale, int mouseX, int mouseY,
            List<GuiButton> parentButtonList) {
        // 2 row and 3 column

        int middleX = startX + PAGECONTENTWIDTH / 2;
        int middleY = startY + PAGECONTENTHEIGHT / 2;
        int buttonGap = 5;

        int[] buttonYArray = new int[] { middleY - buttonGap - (int) (TiCNavigationButton.defaultHeight * BS.multi),
                middleY + buttonGap };
        int[] buttonXArray = new int[] {
                middleX - buttonGap - (int) (TiCNavigationButton.defaultWidth * BS.multi * 1.5f),
                middleX - (int) (TiCNavigationButton.defaultWidth * BS.multi * 0.5),
                middleX + buttonGap + (int) (TiCNavigationButton.defaultWidth * BS.multi * 0.5f) };

        for (int idx = 0; idx < this.pageButtonList.size(); idx++) {
            TiCNavigationButton b = (TiCNavigationButton) this.pageButtonList.get(idx);
            int row = idx / 3;
            int column = idx % 3;

            b.id = idx + parentButtonList.size();
            b.xPosition = (int) (buttonXArray[column] * scale);
            b.yPosition = (int) (buttonYArray[row] * scale);
            b.drawButtonWithScale(manual.mc, mouseX, mouseY, scale, manual.fonts);
        }

        if (parentButtonList != null) {
            parentButtonList.addAll(this.pageButtonList);
        }
    }

    @Override
    public void actionPerformed(GuiButton button) {
        TiCNavigationButton b = (TiCNavigationButton) button;
        TConstruct.logger.info(b.target + " is clicked");

    }

}
