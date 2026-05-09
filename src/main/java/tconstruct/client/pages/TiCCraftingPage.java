package tconstruct.client.pages;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import tconstruct.library.client.TConstructClientRegistry;
import tconstruct.library.util.TiCTurnPageButton;
import tconstruct.library.util.TiCTurnPageButton.ButtonType;
import tconstruct.util.McTextFormatter;
import tconstruct.util.TiCRecipeHolder;
import tconstruct.util.TiCRecipeHolder.RecipeType;

public class TiCCraftingPage extends TiCButtonBookPage {

    String title;
    TiCRecipeHolder[] recipes;
    int maxRecipesSize = 1;

    int selectedIdx;
    TiCTurnPageButton previousRecipeButton;
    TiCTurnPageButton nextRecipeButton;

    private static final ResourceLocation craftingTableBackground = new ResourceLocation(
            "mantle",
            "textures/gui/bookcrafting.png");
    private static final ResourceLocation furnaceBackground = new ResourceLocation(
            "mantle",
            "textures/gui/bookfurnace.png");

    long lastUpdate;
    int counter;

    @Override
    public void readPageFromXML(Element element) {
        this.pageButtonList = new ArrayList<>();
        NodeList nodes = element.getElementsByTagName("icon");
        if (nodes != null) {
            this.recipes = TConstructClientRegistry.getOrRegisterRecipeIcon(nodes.item(0).getTextContent());
            this.title = TConstructClientRegistry.getOrRegisterManualIcon(nodes.item(0).getTextContent())
                    .getDisplayName();
        }
        this.selectedIdx = 0;

        this.pageButtonList
                .add(this.previousRecipeButton = new TiCTurnPageButton(0, 0, 0, ButtonType.previousPage, this));
        this.pageButtonList.add(this.nextRecipeButton = new TiCTurnPageButton(0, 0, 0, ButtonType.nextPage, this));

        this.lastUpdate = System.currentTimeMillis();
        this.counter = 0;
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
        this.maxRecipesSize = 1;

        this.previousRecipeButton.drawButtonWithScale(manual.mc, mouseX, mouseY, scale);
        this.nextRecipeButton.drawButtonWithScale(manual.mc, mouseX, mouseY, scale);

        if (this.selectedIdx >= 0 && this.selectedIdx < this.recipes.length) {

            TiCRecipeHolder selectedRecipe = this.recipes[this.selectedIdx];

            String craftingType = StatCollector
                    .translateToLocal("tconstruct.manual.materialsandyou.recipetype." + selectedRecipe.recipeType.type);

            // update displayed item
            if (this.maxRecipesSize > 1 && System.currentTimeMillis() - lastUpdate > 1000) {
                lastUpdate = System.currentTimeMillis();
                counter++;
                if (counter >= this.maxRecipesSize) counter = 0;
            }

            if (title != null) this.drawStrCenterAt(
                    McTextFormatter.addUnderLine(title),
                    startX + PAGECONTENTWIDTH / 2,
                    startY + 4,
                    1.0f,
                    0x000000);
            this.drawStrCenterAt(craftingType, startX + PAGECONTENTWIDTH / 2, startY + 15, 1.0f, 0x000000);

            if (selectedRecipe.recipeType == RecipeType.Furnace) {
                renderFurnaceRecipe(startX, startY, selectedRecipe);
            } else {
                switch (selectedRecipe.recipeSize) {
                    case 2 -> render22CraftingRecipe(startX, startY, selectedRecipe);
                    case 3 -> render33CraftingRecipe(startX, startY, selectedRecipe);
                }
            }

        }

        if (parentButtonList != null) {
            parentButtonList.addAll(pageButtonList);
        }
    }

    private void beforeRenderItem() {
        GL11.glScalef(2f, 2f, 2f);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.enableGUIStandardItemLighting();
        manual.renderitem.zLevel = 100;
    }

    private void afterRenderItem() {
        manual.renderitem.zLevel = 0;
        GL11.glScalef(0.5F, 0.5F, 0.5F);
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
    }

    private void render22CraftingRecipe(int startX, int startY, TiCRecipeHolder selectedRecipe) {

        ItemStack[][] inputStacks = selectedRecipe.inputStacks;
        ItemStack outputStack = selectedRecipe.outputStack;

        beforeRenderItem();

        manual.renderitem.renderItemAndEffectIntoGUI(
                manual.fonts,
                manual.getMC().renderEngine,
                outputStack,
                (startX + 126) / 2,
                (startY + 68) / 2);
        if (outputStack.stackSize > 1) manual.renderitem.renderItemOverlayIntoGUI(
                manual.fonts,
                manual.getMC().renderEngine,
                outputStack,
                (startX + 126) / 2,
                (startY + 68) / 2,
                String.valueOf(outputStack.stackSize));
        for (int i = 0; i < inputStacks.length; i++) {
            if (inputStacks[i] != null) manual.renderitem.renderItemAndEffectIntoGUI(
                    manual.fonts,
                    manual.getMC().renderEngine,
                    inputStacks[i][this.counter % inputStacks[i].length],
                    (startX + 14 + 36 * (i % 2)) / 2,
                    (startY + 36 * (i / 2) + 52) / 2);
        }

        afterRenderItem();

        manual.getMC().getTextureManager().bindTexture(craftingTableBackground);
        manual.drawTexturedModalRect(startX + 8, startY + 46, 0, 116, 154, 78);
    }

    private void render33CraftingRecipe(int startX, int startY, TiCRecipeHolder selectedRecipe) {

        ItemStack[][] inputStacks = selectedRecipe.inputStacks;
        ItemStack outputStack = selectedRecipe.outputStack;

        beforeRenderItem();

        int biasX = startX + (side != 1 ? 6 : 0);
        manual.renderitem.renderItemAndEffectIntoGUI(
                manual.fonts,
                manual.getMC().renderEngine,
                outputStack,
                (biasX + 138) / 2,
                (startY + 70) / 2);
        if (outputStack.stackSize > 1) manual.renderitem.renderItemOverlayIntoGUI(
                manual.fonts,
                manual.getMC().renderEngine,
                outputStack,
                (biasX + 138) / 2,
                (startY + 68) / 2,
                String.valueOf(outputStack.stackSize));
        for (int i = 0; i < inputStacks.length; i++) {
            if (inputStacks[i] != null) manual.renderitem.renderItemAndEffectIntoGUI(
                    manual.fonts,
                    manual.getMC().renderEngine,
                    inputStacks[i][this.counter % inputStacks[i].length],
                    (biasX - 2 + 36 * (i % 3)) / 2,
                    (startY + 36 * (i / 3) + 34) / 2);
        }

        afterRenderItem();

        manual.getMC().getTextureManager().bindTexture(craftingTableBackground);
        manual.drawTexturedModalRect(startX + (side != 1 ? 6 : 0) - 8, startY + 28, 0, 0, 183, 114);
    }

    private void renderFurnaceRecipe(int startX, int startY, TiCRecipeHolder selectedRecipe) {
        ItemStack[][] inputStacks = selectedRecipe.inputStacks;
        ItemStack outputStack = selectedRecipe.outputStack;

        beforeRenderItem();

        manual.renderitem.renderItemAndEffectIntoGUI(
                manual.fonts,
                manual.getMC().renderEngine,
                TConstructClientRegistry.getOrRegisterManualIcon("minecraft:coal"),
                (startX + 38) / 2,
                (startY + 110) / 2);
        manual.renderitem.renderItemAndEffectIntoGUI(
                manual.fonts,
                manual.getMC().renderEngine,
                outputStack,
                (startX + 106) / 2,
                (startY + 74) / 2);
        manual.renderitem.renderItemAndEffectIntoGUI(
                manual.fonts,
                manual.getMC().renderEngine,
                inputStacks[0][0],
                (startX + 38) / 2,
                (startY + 38) / 2);

        if (outputStack.stackSize > 1) manual.renderitem.renderItemOverlayIntoGUI(
                manual.fonts,
                manual.getMC().renderEngine,
                outputStack,
                (startX + 106) / 2,
                (startY + 74) / 2,
                String.valueOf(outputStack.stackSize));

        afterRenderItem();

        manual.getMC().getTextureManager().bindTexture(furnaceBackground);
        manual.drawTexturedModalRect(startX + 32, startY + 32, 0, 0, 111, 114);
    }

    @Override
    public void actionPerformed(GuiButton button) {
        TiCTurnPageButton b = (TiCTurnPageButton) button;
        if (b == this.previousRecipeButton) {
            this.selectedIdx -= 1;
        } else if (b == this.nextRecipeButton) {
            this.selectedIdx += 1;
        }
        this.selectedIdx = Math.max(0, Math.min(this.recipes.length - 1, selectedIdx));
        this.updateVisible();
    }

    void updateVisible() {
        this.previousRecipeButton.visible = this.selectedIdx != 0;
        this.nextRecipeButton.visible = this.recipes.length != 0 && this.selectedIdx != this.recipes.length - 1;
    }

}
