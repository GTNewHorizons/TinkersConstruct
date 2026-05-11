package tconstruct.client.pages;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import mantle.books.BookData;
import tconstruct.TConstruct;
import tconstruct.library.client.TConstructClientRegistry;
import tconstruct.library.util.TiCBookData;
import tconstruct.library.util.TiCGuiManual;
import tconstruct.library.util.TiCNavigationButton;
import tconstruct.library.util.TiCNavigationButton.ButtonSize;
import tconstruct.util.McTextFormatter;

public class NavigationPage extends TiCBookPage {

    private static final String namePrefix = "tconstruct.manual.materialsandyou.navigation.";

    private ButtonSize BS;

    private String title;

    @Override
    public void readPageFromXML(Element element) {
        this.pageButtonList = new ArrayList<>();

        String size = element.getAttribute("size");
        this.BS = ButtonSize.getSize(size);

        String name = element.getAttribute("name");
        if (StatCollector.canTranslate(namePrefix + name)) {
            this.title = StatCollector.translateToLocal(namePrefix + name);
        } else {
            this.title = name;
        }

        NodeList buttonList = element.getElementsByTagName("button");
        int length = buttonList.getLength();
        for (int idx = 0; idx < length; idx++) {
            Element b = (Element) buttonList.item(idx);

            String color = b.getAttribute("color");
            String naviTo = b.getAttribute("to");
            String tempText = b.getElementsByTagName("text").item(0).getTextContent();
            if (StatCollector.canTranslate(tempText)) tempText = StatCollector.translateToLocal(tempText);

            String iconStr = b.getElementsByTagName("icon").item(0).getTextContent();
            if (iconStr.startsWith("material_")) {
                ItemStack[] iconStacks = (ItemStack[]) TConstructClientRegistry.getManualIcon(iconStr);
                this.pageButtonList.add(
                        new TiCNavigationButton(
                                0,
                                this.BS,
                                iconStacks,
                                tempText,
                                naviTo,
                                this,
                                color.length() != 0 ? Integer.parseInt(color) : 0x000000));
            } else {
                ItemStack iconStack = TConstructClientRegistry.getOrRegisterManualIcon(iconStr);
                this.pageButtonList.add(new TiCNavigationButton(0, this.BS, iconStack, tempText, naviTo, this));
            }
        }
    }

    public void updateButtonPositionAndRender(int startX, int startY, float scale, int mouseX, int mouseY,
            List<GuiButton> parentButtonList) {
        if (title != null) this.drawStrCenterAt(
                McTextFormatter.addUnderLine(title),
                startX + PAGECONTENTWIDTH / 2,
                startY + 4,
                1.0f,
                0x000000);

        int middleX = startX + PAGECONTENTWIDTH / 2;
        int middleY = startY + PAGECONTENTHEIGHT / 2;

        int buttonWidth = (int) (TiCNavigationButton.defaultWidth * BS.multi);
        int buttonHeight = (int) (TiCNavigationButton.defaultHeight * BS.multi);
        // int buttonGap = buttonWidth / 8;
        int buttonGap = 5;

        int buttonRows = this.pageButtonList.size() / this.BS.buttonEachRow;

        int buttonsGroupHeight = buttonRows * buttonHeight + (buttonRows - 1) * buttonGap;
        int buttonsGroupWidth = this.BS.buttonEachRow * buttonWidth + (this.BS.buttonEachRow - 1) * buttonGap;

        int buttonsGroupStartX = middleX - buttonsGroupWidth / 2;
        int buttonsGroupStartY = middleY - buttonsGroupHeight / 2;

        for (int idx = 0; idx < this.pageButtonList.size(); idx++) {
            TiCNavigationButton b = (TiCNavigationButton) this.pageButtonList.get(idx);
            int row = idx / this.BS.buttonEachRow;
            int column = idx % this.BS.buttonEachRow;

            int buttonX = buttonsGroupStartX + column * (buttonWidth + buttonGap);
            int buttonY = buttonsGroupStartY + row * (buttonHeight + buttonGap);

            b.id = idx + parentButtonList.size();
            b.xPosition = (int) (buttonX * scale);
            b.yPosition = (int) (buttonY * scale);
            b.drawButtonWithScale(manual.mc, mouseX, mouseY, scale, manual.fonts);
        }

        if (parentButtonList != null) {
            parentButtonList.addAll(this.pageButtonList);
        }
    }

    @Override
    public void actionPerformed(GuiButton button, BookData d) {
        TiCNavigationButton b = (TiCNavigationButton) button;
        if (d instanceof TiCBookData tcbd) {
            int pageIndex = tcbd.getIndexFromName(b.target);
            if (pageIndex != -1) {
                ((TiCGuiManual) manual).setCurrentPage(pageIndex);
            } else {
                TConstruct.logger.error("There's no page name " + b.target);
            }
        }

    }

}
