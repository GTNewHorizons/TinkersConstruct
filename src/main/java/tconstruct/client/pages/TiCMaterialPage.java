package tconstruct.client.pages;

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import tconstruct.library.TConstructRegistry;
import tconstruct.library.client.TConstructClientRegistry;
import tconstruct.library.tools.ArrowMaterial;
import tconstruct.library.tools.BowMaterial;
import tconstruct.library.tools.ToolMaterial;
import tconstruct.library.util.HarvestLevels;
import tconstruct.modifiers.tools.ModReinforced;
import tconstruct.util.FontColorHelper;
import tconstruct.util.McTextFormatter;

public class TiCMaterialPage extends TiCBookPage {

    public ToolMaterial tm;
    public ArrowMaterial am;
    public BowMaterial bm;

    public ItemStack[] materialStacks;

    private static final String namePrefix = "tconstruct.manual.materialsandyou.material.";

    String materialExtraDesc = null;

    long lastUpdate;
    int counter;

    @Override
    public void readPageFromXML(Element element) {
        NodeList nodes = element.getElementsByTagName("text");
        if (nodes != null) {
            String materialName = nodes.item(0).getTextContent();
            tm = TConstructRegistry.getMaterial(materialName);

            String materialId = nodes.item(1).getTextContent();
            if (materialId.length() != 0) {
                int id = Integer.parseInt(materialId);
                am = TConstructRegistry.getArrowMaterial(id);
                bm = TConstructRegistry.getBowMaterial(id);
            }

            materialStacks = (ItemStack[]) TConstructClientRegistry.getManualIcon("material_" + materialName);

            if (StatCollector.canTranslate(namePrefix + materialName))
                this.materialExtraDesc = StatCollector.translateToLocal(namePrefix + materialName);
        }
        lastUpdate = System.currentTimeMillis();
    }

    @Override
    public void render(int startX, int startY, float scale, int mouseX, int mouseY, List<GuiButton> parentButtonList) {
        this.widgetsList.clear();
        if (tm != null) this.drawStrCenterAt(
                McTextFormatter.addBold(McTextFormatter.addUnderLine(tm.localizedName())),
                startX + PAGECONTENTWIDTH / 2,
                startY + 4,
                1.0f,
                FontColorHelper.adjustForegroundKeepHue(BACKGROUNDCOLOR, tm.primaryColor()));

        int stringStartX = (int) ((startX + (this.side == 0 ? 30 : 10)) * scale);
        int stringStartY = (int) ((startY + 20) * scale);
        int lineHeight = (int) (manual.fonts.FONT_HEIGHT * scale);

        drawStrAt(
                StatCollector.translateToLocal("gui.partcrafter4") + tm.durability,
                stringStartX,
                stringStartY,
                scale);
        stringStartY += lineHeight;

        drawStrAt(
                StatCollector.translateToLocal("gui.partcrafter5") + tm.handleModifier + "x",
                stringStartX,
                stringStartY,
                scale);
        stringStartY += lineHeight;

        drawStrAt(
                StatCollector.translateToLocal("gui.partcrafter11") + Math.round(tm.durability * tm.handleModifier),
                stringStartX,
                stringStartY,
                scale);
        stringStartY += lineHeight;

        drawStrAt(
                StatCollector.translateToLocal("gui.partcrafter6") + tm.miningspeed / 100F,
                stringStartX,
                stringStartY,
                scale);
        stringStartY += lineHeight;

        drawStrAt(
                StatCollector.translateToLocal("gui.partcrafter7") + HarvestLevels.getHarvestLevelName(tm.harvestLevel),
                stringStartX,
                stringStartY,
                scale);
        stringStartY += lineHeight;

        String heart = tm.attack == 2 ? StatCollector.translateToLocal("gui.partcrafter8")
                : StatCollector.translateToLocal("gui.partcrafter9");
        if (tm.attack() % 2 == 0) {
            drawStrAt(
                    StatCollector.translateToLocal("gui.partcrafter10") + tm.attack / 2 + heart,
                    stringStartX,
                    stringStartY,
                    scale);
        } else {
            drawStrAt(
                    StatCollector.translateToLocal("gui.partcrafter10") + tm.attack / 2F + heart,
                    stringStartX,
                    stringStartY,
                    scale);
        }
        stringStartY += (lineHeight * 1.2);

        if (tm.reinforced > 0) {
            drawStrAt(
                    McTextFormatter.addDarkPurple(ModReinforced.getReinforcedString(tm.reinforced)),
                    stringStartX,
                    stringStartY,
                    scale);
            stringStartY += lineHeight;
        }

        if (tm.shoddy() != 0) {
            String abilityStr = StatCollector.translateToLocal(
                    tm.shoddy() > 0 ? "manual.page.material8" : "manual.page.material9") + ": " + Math.abs(tm.shoddy());
            abilityStr = tm.shoddy() > 0 ? McTextFormatter.addDarkRed(abilityStr)
                    : McTextFormatter.addDarkGreen(abilityStr);
            drawStrAt(abilityStr, stringStartX, stringStartY, scale);
            stringStartY += (lineHeight * 1.2);
        } else if (tm.ability().length() != 0) {
            drawStrAt(McTextFormatter.addBold(tm.style() + tm.ability()), stringStartX, stringStartY, scale);
            stringStartY += (lineHeight * 1.2);
        }

        beforeRenderItem(1.0F);

        for (int idx = 0; idx < 10; idx++) {
            if (counter + idx >= this.materialStacks.length) {
                break;
            }
            renderItemStackIntoPage(
                    materialStacks[counter + idx],
                    startX + (this.side == 0 ? 5 : (PAGECONTENTWIDTH - 21)),
                    startY + 2 + idx * 16,
                    scale);
        }

        afterRenderItem(1.0F);

        // update displayed item
        if (this.materialStacks.length > 10 && System.currentTimeMillis() - lastUpdate > 1000) {
            lastUpdate = System.currentTimeMillis();
            counter += 10;
            if (counter >= this.materialStacks.length) counter = 0;
        }
    }

}
