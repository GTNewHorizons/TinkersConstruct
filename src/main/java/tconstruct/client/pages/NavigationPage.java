package tconstruct.client.pages;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mantle.client.pages.BookPage;
import tconstruct.library.util.TiCNavigationButton;
import tconstruct.smeltery.TinkerSmeltery;
import tconstruct.tools.TinkerTools;

public class NavigationPage extends BookPage {

    private int buttonStartIdx = 10;

    private List<TiCNavigationButton> navigationButtonList;

    private static ItemStack[] icons = new ItemStack[] { new ItemStack(TinkerTools.craftingStationWood),
            new ItemStack(Items.iron_pickaxe), new ItemStack(Items.iron_ingot), new ItemStack(Items.redstone),
            new ItemStack(TinkerSmeltery.smeltery), new ItemStack(Items.bow) };

    @Override
    public void readPageFromXML(Element element) {
        navigationButtonList = new ArrayList<>();
        NodeList buttonList = element.getElementsByTagName("button");
        for (int idx = 0; idx < buttonList.getLength(); idx++) {
            Node b = buttonList.item(idx);

            String naviTo = ((Element) b).getAttribute("to");
            String tempText = b.getTextContent();
            if (StatCollector.canTranslate(tempText)) tempText = StatCollector.translateToLocal(tempText);

            navigationButtonList.add(
                    new TiCNavigationButton(
                            buttonStartIdx + navigationButtonList.size(),
                            TiCNavigationButton.ButtonSize.large,
                            icons[idx],
                            tempText,
                            naviTo));
        }
    }

    public List<TiCNavigationButton> updateButtonPositionAndRender(int startX, int startY, float scale, int mouseX,
            int mouseY) {
        // 2 row and 3 column

        int middleX = startX + 190 / 2;
        int middleY = startY + 165 / 2;
        int buttonGap = 5;

        int[] buttonYArray = new int[] {
                middleY - buttonGap
                        - (int) (TiCNavigationButton.defaultHeight * TiCNavigationButton.ButtonSize.large.multi),
                middleY + buttonGap };
        int[] buttonXArray = new int[] {
                middleX - buttonGap
                        - (int) (TiCNavigationButton.defaultWidth * TiCNavigationButton.ButtonSize.large.multi * 1.5f),
                middleX - (int) (TiCNavigationButton.defaultWidth * TiCNavigationButton.ButtonSize.large.multi * 0.5),
                middleX + buttonGap
                        + (int) (TiCNavigationButton.defaultWidth * TiCNavigationButton.ButtonSize.large.multi
                                * 0.5f) };

        for (int idx = 0; idx < navigationButtonList.size(); idx++) {
            TiCNavigationButton b = navigationButtonList.get(idx);
            int row = idx / 3;
            int column = idx % 3;

            b.xPosition = (int) (buttonXArray[column] * scale);
            b.yPosition = (int) (buttonYArray[row] * scale);
            b.drawButtonWithScale(manual.mc, mouseX, mouseY, scale, manual.fonts);
        }
        // navigationButtonList.forEach(b -> {
        // b.xPosition = (int) (startX * scale);
        // b.yPosition = (int) (startY * scale);
        // b.drawButtonWithScale(manual.mc, mouseX, mouseY, scale, manual.fonts);
        // });
        return navigationButtonList;
    }

    @Override
    public void renderContentLayer(int localwidth, int localheight, boolean isTranslatable) {
        this.updateButtonPositionAndRender(localheight, localheight, 1.0f, 0, 0);
    }

}
