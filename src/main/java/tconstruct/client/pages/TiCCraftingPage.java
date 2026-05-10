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

import mantle.books.BookData;
import tconstruct.library.client.TConstructClientRegistry;
import tconstruct.library.util.TiCTurnPageButton;
import tconstruct.library.util.TiCTurnPageButton.ButtonType;
import tconstruct.util.ItemStackWithPosition;
import tconstruct.util.McTextFormatter;
import tconstruct.util.TiCRecipeHolder;
import tconstruct.util.TiCRecipeHolder.RecipeType;

public class TiCCraftingPage extends TiCBookPage {

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

    private static final ResourceLocation toolStationForgeBackground = new ResourceLocation(
            "tinker",
            "textures/gui/bookmodifyandstation.png");

    long lastUpdate;
    int counter;

    final static ItemStack fuel = TConstructClientRegistry.getOrRegisterManualIcon("minecraft:coal");
    
    final static int[][] toolStationOrForgePosition = new int[][] {{28, 28}, {28, 3}, {3, 24}, {7, 50}, {49, 50}, {53, 24}};

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
        this.pageItemStackList.clear();

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
                renderFurnaceRecipe(startX, startY, selectedRecipe, scale);
            } else if (selectedRecipe.recipeType == RecipeType.ToolForge
                    || selectedRecipe.recipeType == RecipeType.ToolStation) {
                        renderToolStationOrForgeRecipe(startX, startY, selectedRecipe, scale);
                    } else {
                        switch (selectedRecipe.recipeSize) {
                            case 2 -> render22CraftingRecipe(startX, startY, selectedRecipe, scale);
                            case 3 -> render33CraftingRecipe(startX, startY, selectedRecipe, scale);
                        }
                    }

        }

        if (parentButtonList != null) {
            parentButtonList.addAll(pageButtonList);
        }
    }

    private void beforeRenderItem() {
    	beforeRenderItem(2.0F);
    }
    
    private void beforeRenderItem(float scale) {
    	beforeRenderItem(scale, 100);
    }
    
    private void beforeRenderItem(float scale, int z) {
        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glScalef(scale, scale, 2.0F);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.enableGUIStandardItemLighting();
        manual.renderitem.zLevel = z;
    }

    private void afterRenderItem() {
    	afterRenderItem(2.0F);
    }
    
    private void afterRenderItem(float scale) {
        manual.renderitem.zLevel = 0;
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }

    private void render22CraftingRecipe(int startX, int startY, TiCRecipeHolder selectedRecipe, float scale) {
        ItemStack[][] inputStacks = selectedRecipe.inputStacks;
        ItemStack outputStack = selectedRecipe.outputStack;

        manual.getMC().getTextureManager().bindTexture(craftingTableBackground);
        manual.drawTexturedModalRect(startX + 8, startY + 46, 0, 116, 154, 78);

        beforeRenderItem();

        renderItemStackIntoPage(outputStack, (startX + 126) / 2, (startY + 68) / 2);
        this.pageItemStackList
                .add(new ItemStackWithPosition(outputStack, (startX + 126) / 2, (startY + 68) / 2, 2 * scale));

        for (int i = 0; i < inputStacks.length; i++) {
            if (inputStacks[i] != null && inputStacks[i][0] != null) {
                ItemStack renderStack = inputStacks[i][this.counter % inputStacks[i].length];
                renderItemStackIntoPage(
                        renderStack,
                        (startX + 14 + 36 * (i % 2)) / 2,
                        (startY + 36 * (i / 2) + 52) / 2);
                this.pageItemStackList.add(
                        new ItemStackWithPosition(
                                renderStack,
                                (startX + 14 + 36 * (i % 2)) / 2,
                                (startY + 36 * (i / 2) + 52) / 2,
                                2 * scale));
            }
        }

        afterRenderItem();
    }

    private void render33CraftingRecipe(int startX, int startY, TiCRecipeHolder selectedRecipe, float scale) {
        ItemStack[][] inputStacks = selectedRecipe.inputStacks;
        ItemStack outputStack = selectedRecipe.outputStack;

        manual.getMC().getTextureManager().bindTexture(craftingTableBackground);
        manual.drawTexturedModalRect(startX + (side != 1 ? 6 : 0) - 8, startY + 28, 0, 0, 183, 114);

        beforeRenderItem();

        int biasX = startX + (side != 1 ? 6 : 0);

        renderItemStackIntoPage(outputStack, (biasX + 138) / 2, (startY + 70) / 2);
        this.pageItemStackList
                .add(new ItemStackWithPosition(outputStack, (biasX + 138) / 2, (startY + 70) / 2, 2 * scale));

        for (int i = 0; i < inputStacks.length; i++) {
            if (inputStacks[i] != null && inputStacks[i][0] != null) {
                ItemStack renderStack = inputStacks[i][this.counter % inputStacks[i].length];
                renderItemStackIntoPage(renderStack, (biasX - 2 + 36 * (i % 3)) / 2, (startY + 36 * (i / 3) + 34) / 2);
                this.pageItemStackList.add(
                        new ItemStackWithPosition(
                                renderStack,
                                (biasX - 2 + 36 * (i % 3)) / 2,
                                (startY + 36 * (i / 3) + 34) / 2,
                                2 * scale));
            }
        }

        afterRenderItem();
    }

    private void renderFurnaceRecipe(int startX, int startY, TiCRecipeHolder selectedRecipe, float scale) {
        ItemStack[][] inputStacks = selectedRecipe.inputStacks;
        ItemStack outputStack = selectedRecipe.outputStack;

        manual.getMC().getTextureManager().bindTexture(furnaceBackground);
        manual.drawTexturedModalRect(startX + 32, startY + 32, 0, 0, 111, 114);

        beforeRenderItem();

        renderItemStackIntoPage(fuel, (startX + 38) / 2, (startY + 110) / 2);
        renderItemStackIntoPage(outputStack, (startX + 106) / 2, (startY + 74) / 2);
        renderItemStackIntoPage(inputStacks[0][0], (startX + 38) / 2, (startY + 38) / 2);

        this.pageItemStackList.add(new ItemStackWithPosition(fuel, (startX + 38) / 2, (startY + 110) / 2, 2 * scale));
        this.pageItemStackList
                .add(new ItemStackWithPosition(outputStack, (startX + 106) / 2, (startY + 74) / 2, 2 * scale));
        this.pageItemStackList
                .add(new ItemStackWithPosition(inputStacks[0][0], (startX + 38) / 2, (startY + 38) / 2, 2 * scale));

        afterRenderItem();
    }

    private void renderToolStationOrForgeRecipe(int startX, int startY, TiCRecipeHolder selectedRecipe, float scale) {
        ItemStack[][] inputStacks = selectedRecipe.inputStacks;
        ItemStack outputStack = selectedRecipe.outputStack;

        int recipeStartX = startX + PAGECONTENTWIDTH - 72 - 15;
        int recipeStartY = startY + PAGECONTENTHEIGHT - 69 - 15;

        manual.getMC().getTextureManager().bindTexture(toolStationForgeBackground);
        manual.drawTexturedModalRect(recipeStartX + 12, recipeStartY + 12, 208, (selectedRecipe.recipeType == RecipeType.ToolForge ? 48 : 0), 48, 48);
        
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.85F);
        manual.drawTexturedModalRect(recipeStartX, recipeStartY, 0, 0, 72, 69);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        
        int idx = 0;
        
        beforeRenderItem(1.0F);
        
        renderItemStackIntoPage(outputStack, recipeStartX + toolStationOrForgePosition[idx][0], recipeStartY + toolStationOrForgePosition[idx][1]);
        this.pageItemStackList
                .add(new ItemStackWithPosition(outputStack, recipeStartX + toolStationOrForgePosition[idx][0], recipeStartY + toolStationOrForgePosition[idx][1], scale));
        idx += 1;
        
        for(ItemStack[] l : inputStacks) {
        	if(l[0] != null) {
                renderItemStackIntoPage(l[0], recipeStartX + toolStationOrForgePosition[idx][0], recipeStartY + toolStationOrForgePosition[idx][1]);
                this.pageItemStackList
                        .add(new ItemStackWithPosition(l[0], recipeStartX + toolStationOrForgePosition[idx][0], recipeStartY + toolStationOrForgePosition[idx][1], scale));
                idx += 1;
        	}
        }
        
        afterRenderItem(1.0F);
    }

    @Override
    public void actionPerformed(GuiButton button, BookData d) {
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
