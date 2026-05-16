package tconstruct.client.pages;

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import tconstruct.library.TConstructRegistry;
import tconstruct.library.client.TConstructClientRegistry;
import tconstruct.library.tools.ArrowMaterial;
import tconstruct.library.tools.BowMaterial;
import tconstruct.library.tools.ToolMaterial;
import tconstruct.library.util.HarvestLevels;
import tconstruct.library.util.StringWithPosition;
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

        GL11.glPushMatrix();
        GL11.glScaled(1.05f, 1.05f, 1.0f);

        float realScale = scale * 1.05f;
        int stringStartX = (int) ((startX + (this.side == 0 ? 30 : 10)) * realScale);
        int stringStartY = (int) ((startY + 20) * realScale);

        int lineHeight = (int) (manual.fonts.FONT_HEIGHT * realScale);

        String str = StatCollector.translateToLocalFormatted(
                "gui.partcrafter.durability",
                McTextFormatter.addDarkGreen(String.valueOf(tm.durability)));
        drawStrAt(str, stringStartX, stringStartY, realScale);
        if (StatCollector.canTranslate("gui.partcrafter.durability.desc")) addNewTooltipsString(
                StatCollector.translateToLocal("gui.partcrafter.durability.desc"),
                stringStartX,
                stringStartY,
                manual.fonts.getStringWidth(str) * realScale,
                lineHeight);
        stringStartY += lineHeight;

        str = StatCollector.translateToLocalFormatted(
                "gui.partcrafter.handlemodifier",
                McTextFormatter.addGold(String.valueOf(tm.handleModifier)));
        drawStrAt(str, stringStartX, stringStartY, realScale);
        if (StatCollector.canTranslate("gui.partcrafter.handlemodifier.desc")) addNewTooltipsString(
                StatCollector.translateToLocal("gui.partcrafter.handlemodifier.desc"),
                stringStartX,
                stringStartY,
                manual.fonts.getStringWidth(str) * realScale,
                lineHeight);
        stringStartY += lineHeight;

        str = StatCollector.translateToLocalFormatted(
                "gui.partcrafter.miningspeed",
                McTextFormatter.addBlue(String.valueOf(tm.miningspeed / 100F)));
        drawStrAt(str, stringStartX, stringStartY, realScale);
        if (StatCollector.canTranslate("gui.partcrafter.miningspeed.desc")) addNewTooltipsString(
                StatCollector.translateToLocal("gui.partcrafter.miningspeed.desc"),
                stringStartX,
                stringStartY,
                manual.fonts.getStringWidth(str) * realScale,
                lineHeight);
        stringStartY += lineHeight;

        str = StatCollector.translateToLocalFormatted(
                "gui.partcrafter.mininglevel",
                HarvestLevels.getHarvestLevelName(tm.harvestLevel));
        drawStrAt(str, stringStartX, stringStartY, realScale);
        if (StatCollector.canTranslate("gui.partcrafter.mininglevel.desc")) addNewTooltipsString(
                StatCollector.translateToLocal("gui.partcrafter.mininglevel.desc"),
                stringStartX,
                stringStartY,
                manual.fonts.getStringWidth(str) * realScale,
                lineHeight);
        stringStartY += lineHeight;

        str = StatCollector.translateToLocalFormatted(
                "gui.partcrafter.attack",
                McTextFormatter.addDarkRed(String.valueOf(tm.attack)));
        drawStrAt(str, stringStartX, stringStartY, realScale);
        if (StatCollector.canTranslate("gui.partcrafter.attack.desc")) addNewTooltipsString(
                StatCollector.translateToLocal("gui.partcrafter.attack.desc"),
                stringStartX,
                stringStartY,
                manual.fonts.getStringWidth(str) * realScale,
                lineHeight);
        stringStartY += (lineHeight * 1.2);

        if (tm.reinforced > 0) {
            drawStrAt(
                    McTextFormatter.addDarkPurple(
                            McTextFormatter.addUnderLine(ModReinforced.getReinforcedString(tm.reinforced))),
                    stringStartX,
                    stringStartY,
                    realScale);

            String reinforcedDesc = StatCollector.translateToLocal("tool.reinforced.desc");
            addNewTooltipsString(
                    reinforcedDesc,
                    stringStartX,
                    stringStartY,
                    manual.fonts.getStringWidth(ModReinforced.getReinforcedString(tm.reinforced)) * realScale,
                    lineHeight);

            stringStartY += (lineHeight * 1.2);
        }

        if (tm.shoddy() != 0) {
            String abilityStr = McTextFormatter.addUnderLine(tm.ability() + ": " + Math.abs(tm.shoddy()));
            if (tm.shoddy() > 0) {
                abilityStr = McTextFormatter.addDarkRed(abilityStr);
            } else {
                abilityStr = McTextFormatter.addDarkGreen(abilityStr);
            }
            drawStrAt(abilityStr, stringStartX, stringStartY, realScale);

            String abilityDesc = tm.abilityDesc();
            if (abilityDesc.length() != 0) {
                addNewTooltipsString(
                        abilityDesc,
                        stringStartX,
                        stringStartY,
                        manual.fonts.getStringWidth(abilityStr) * realScale,
                        lineHeight);
            } else {
                addNewTooltipsString(
                        StatCollector.translateToLocalFormatted("tool.nodesc", tm.ability()),
                        stringStartX,
                        stringStartY,
                        manual.fonts.getStringWidth(abilityStr) * realScale,
                        lineHeight);
            }
            stringStartY += (lineHeight * 1.2);
        } else if (tm.ability().length() != 0) {
            String abilityStr = tm.style() + McTextFormatter.addUnderLine(tm.ability());
            drawStrAt(abilityStr, stringStartX, stringStartY, realScale);

            String abilityDesc = tm.abilityDesc();
            if (abilityDesc.length() != 0) {
                addNewTooltipsString(
                        abilityDesc,
                        stringStartX,
                        stringStartY,
                        manual.fonts.getStringWidth(abilityStr) * realScale,
                        lineHeight);
            } else {
                addNewTooltipsString(
                        StatCollector.translateToLocalFormatted("tool.nodesc", tm.ability()),
                        stringStartX,
                        stringStartY,
                        manual.fonts.getStringWidth(abilityStr) * realScale,
                        lineHeight);
            }
            stringStartY += (lineHeight * 1.2);
        }

        GL11.glPopMatrix();

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

    private void addNewTooltipsString(String str, int stringStartX, int stringStartY, float width, int height) {
        addNewTooltipsString(str, stringStartX, stringStartY, (int) width, height);
    }

    private void addNewTooltipsString(String str, int stringStartX, int stringStartY, int width, int height) {
        widgetsList.add(new StringWithPosition(str, (int) (stringStartX), (int) (stringStartY), (int) (width), height));
    }
}
