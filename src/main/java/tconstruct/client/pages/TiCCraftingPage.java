package tconstruct.client.pages;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import tconstruct.library.client.TConstructClientRegistry;
import tconstruct.library.util.TiCTurnPageButton;
import tconstruct.library.util.TiCTurnPageButton.ButtonType;
import tconstruct.util.McTextFormatter;

public class TiCCraftingPage extends TiCButtonBookPage {

    String title;
    ItemStack target;
    IRecipe[] recipes;

    int selectedIdx;
    TiCTurnPageButton previousRecipeButton;
    TiCTurnPageButton nextRecipeButton;

    private static final ResourceLocation background = new ResourceLocation("mantle", "textures/gui/bookcrafting.png");

    @Override
    public void readPageFromXML(Element element) {
        this.pageButtonList = new ArrayList<>();
        NodeList nodes = element.getElementsByTagName("text");

        if (nodes != null) this.title = nodes.item(0).getTextContent();
        if (StatCollector.canTranslate(this.title)) this.title = StatCollector.translateToLocal(this.title);

        nodes = element.getElementsByTagName("icon");
        if (nodes != null) {
            this.recipes = TConstructClientRegistry.getOrRegisterRecipeIcon(nodes.item(0).getTextContent());
            this.target = TConstructClientRegistry.getOrRegisterManualIcon(nodes.item(0).getTextContent());
        }
        this.selectedIdx = 0;

        this.pageButtonList
                .add(this.previousRecipeButton = new TiCTurnPageButton(0, 0, 0, ButtonType.previousPage, this));
        this.pageButtonList.add(this.nextRecipeButton = new TiCTurnPageButton(0, 0, 0, ButtonType.nextPage, this));
    }

    private void drawStrCenterAt(String str, int X, int Y, float scale, int color) {
        manual.fonts.drawString(
                str,
                (int) (X / scale - manual.fonts.getStringWidth(str) / 2),
                (int) ((Y - manual.fonts.FONT_HEIGHT / 2) / scale),
                color);
    }

    @Override
    public void updateButtonPosition(int startX, int startY, float scale, int mouseX, int mouseY,
            List<GuiButton> parentButtonList) {
        this.previousRecipeButton.id = parentButtonList.size();
        this.nextRecipeButton.id = parentButtonList.size() + 1;

        this.previousRecipeButton.xPosition = (int) ((startX + this.previousRecipeButton.getWidth() * 0.5f) * scale);
        this.nextRecipeButton.xPosition = (int) ((startX + PAGECONTENTWIDTH - this.nextRecipeButton.getWidth() * 1.5f)
                * scale);

        this.previousRecipeButton.yPosition = (int) ((startY + 15) * scale);
        this.nextRecipeButton.yPosition = (int) ((startY + 15) * scale);

        this.updateVisible();
    }

    @Override
    public void render(int startX, int startY, float scale, int mouseX, int mouseY, List<GuiButton> parentButtonList) {

        this.previousRecipeButton.drawButtonWithScale(manual.mc, mouseX, mouseY, scale);
        this.nextRecipeButton.drawButtonWithScale(manual.mc, mouseX, mouseY, scale);

        IRecipe selectedRecipe = this.recipes[this.selectedIdx];
        String craftingType = "";
        ItemStack[] inputs = new ItemStack[0];
        int recipeSize = 0;
        if (selectedRecipe instanceof ShapedOreRecipe sor) {
            craftingType = "ShapedOreRecipe";
            List<ItemStack> a = new ArrayList<>();
            for (Object b : Arrays.asList(sor.getInput())) {
                if (b instanceof List l) {
                    a.add((ItemStack) l.get(0));
                } else if (b instanceof ItemStack l) {
                    a.add(l);
                } else {
                    a.add(null);
                }
            }
            inputs = a.toArray(inputs);
            recipeSize = getWidth(sor);
        } else if (selectedRecipe instanceof ShapelessOreRecipe sor) {
            craftingType = "ShapelessOreRecipe";
            List<ItemStack> a = new ArrayList<>();
            for (Object b : Arrays.asList(sor.getInput())) {
                if (b instanceof List l) {
                    a.add((ItemStack) l.get(0));
                } else if (b instanceof ItemStack l) {
                    a.add(l);
                }
            }
            inputs = a.toArray(inputs);
        } else if (selectedRecipe instanceof ShapedRecipes sr) {
            craftingType = "ShapedRecipes";
            inputs = sr.recipeItems;
            recipeSize = Math.max(sr.recipeWidth, sr.recipeHeight);
        } else if (selectedRecipe instanceof ShapelessRecipes sr) {
            craftingType = "ShapelessRecipes";
            inputs = sr.recipeItems.toArray(inputs);
        }
        recipeSize = recipeSize == 0 ? (inputs.length > 4 ? 3 : 2) : recipeSize;

        craftingType = StatCollector.translateToLocal("tconstruct.manual.materialsandyou.recipetype." + craftingType);

        if (title != null) this.drawStrCenterAt(
                McTextFormatter.addUnderLine(title),
                startX + PAGECONTENTWIDTH / 2,
                startY + 4,
                1.0f,
                0x000000);
        this.drawStrCenterAt(craftingType, startX + PAGECONTENTWIDTH / 2, startY + 15, 1.0f, 0x000000);

        GL11.glScalef(2f, 2f, 2f);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.enableGUIStandardItemLighting();
        manual.renderitem.zLevel = 100;

        if (recipeSize == 2) {
            manual.renderitem.renderItemAndEffectIntoGUI(
                    manual.fonts,
                    manual.getMC().renderEngine,
                    target,
                    (startX + 126) / 2,
                    (startY + 68) / 2);
            if (target.stackSize > 1) manual.renderitem.renderItemOverlayIntoGUI(
                    manual.fonts,
                    manual.getMC().renderEngine,
                    target,
                    (startX + 126) / 2,
                    (startY + 68) / 2,
                    String.valueOf(target.stackSize));
            for (int i = 0; i < inputs.length; i++) {
                if (inputs[i] != null) manual.renderitem.renderItemAndEffectIntoGUI(
                        manual.fonts,
                        manual.getMC().renderEngine,
                        inputs[i],
                        (startX + 14 + 36 * (i % 2)) / 2,
                        (startY + 36 * (i / 2) + 52) / 2);
            }
        }

        if (recipeSize == 3) {
            int biasX = startX + (side != 1 ? 6 : 0);
            manual.renderitem.renderItemAndEffectIntoGUI(
                    manual.fonts,
                    manual.getMC().renderEngine,
                    target,
                    (biasX + 138) / 2,
                    (startY + 70) / 2);
            if (target.stackSize > 1) manual.renderitem.renderItemOverlayIntoGUI(
                    manual.fonts,
                    manual.getMC().renderEngine,
                    target,
                    (biasX + 126) / 2,
                    (startY + 68) / 2,
                    String.valueOf(target.stackSize));
            for (int i = 0; i < inputs.length; i++) {
                if (inputs[i] != null) manual.renderitem.renderItemAndEffectIntoGUI(
                        manual.fonts,
                        manual.getMC().renderEngine,
                        inputs[i],
                        (biasX - 2 + 36 * (i % 3)) / 2,
                        (startY + 36 * (i / 3) + 34) / 2);
            }
        }

        manual.renderitem.zLevel = 0;
        GL11.glScalef(0.5F, 0.5F, 0.5F);
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);

        this.renderBackgroundLayer(startX, startY, recipeSize);

        if (parentButtonList != null) {
            parentButtonList.addAll(pageButtonList);
        }
    }

    public void renderBackgroundLayer(int localwidth, int localheight, int recipeSize) {
        if (recipeSize == 2) drawBackground(recipeSize, localwidth, localheight);

        if (recipeSize == 3) drawBackground(recipeSize, localwidth + (side != 1 ? 6 : 0), localheight);
    }

    public void drawBackground(int size, int localWidth, int localHeight) {
        manual.getMC().getTextureManager().bindTexture(background);
        if (size == 2) manual.drawTexturedModalRect(localWidth + 8, localHeight + 46, 0, 116, 154, 78);
        if (size == 3) manual.drawTexturedModalRect(localWidth - 8, localHeight + 28, 0, 0, 183, 114);
    }

    @Override
    public void actionPerformed(GuiButton button) {
        TiCTurnPageButton b = (TiCTurnPageButton) button;
        if (b == this.previousRecipeButton) {
            this.selectedIdx -= 1;
        } else if (b == this.nextRecipeButton) {
            this.selectedIdx += 1;
        }
        this.selectedIdx = Math.min(this.recipes.length - 1, Math.max(selectedIdx, 0));
        this.updateVisible();
    }

    void updateVisible() {
        this.previousRecipeButton.visible = this.selectedIdx != 0;
        this.nextRecipeButton.visible = this.selectedIdx != this.recipes.length - 1;
    }

    private static int getWidth(ShapedOreRecipe sor) {
        try {
            Field widthField = sor.getClass().getDeclaredField("width");
            widthField.setAccessible(true);
            int width = widthField.getInt(sor);
            Field heightField = sor.getClass().getDeclaredField("height");
            heightField.setAccessible(true);
            int height = heightField.getInt(sor);
            return Math.max(width, height);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return -1;
        }
    }

}
