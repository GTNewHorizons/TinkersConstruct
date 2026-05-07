package tconstruct.library.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mantle.books.BookData;
import mantle.client.MProxyClient;
import mantle.client.RenderItemCopy;
import mantle.client.SmallFontRenderer;
import mantle.client.gui.GuiManual;
import mantle.client.pages.BookPage;
import tconstruct.TConstruct;
import tconstruct.client.pages.NavigationPage;
import tconstruct.library.util.TiCTurnPageButton.ButtonType;

@SideOnly(Side.CLIENT)
public class TiCGuiManual extends GuiManual {

    private static final int ANIMATIONDURATIONINMILLIS = 600;
    private static final float FLYIN_DURATION = 0.55f; // portion for fly-in
    private static final float OVERSHOOT_DURATION = 0.1f; // portion for overshoot
    private static final float EXTENT = 0.02f; // overshoot amount as fraction of total displacement
    private static final float GUIMAXPERCENTAGE = 0.75f; // The maximum percentage that the manual GUI can occupy

    ItemStack itemstackBook;
    Document manual;
    public RenderItemCopy renderitem = new RenderItemCopy();
    int bookImageWidth = 206;
    int bookImageHeight = 200;
    int bookTotalPages = 1;
    int currentPage;
    int maxPages;
    BookData bData;

    private TiCTurnPageButton buttonNextPage;
    private TiCTurnPageButton buttonPreviousPage;
    private TiCTurnPageButton buttonHomePage;
    private static final ResourceLocation bookRightBackGround = new ResourceLocation(
            "tinker",
            "textures/gui/bookrightbackground.png");
    private static final ResourceLocation bookLeftBackGround = new ResourceLocation(
            "tinker",
            "textures/gui/bookleftbackground.png");
    private static final ResourceLocation bookRightPage = new ResourceLocation(
            "tinker",
            "textures/gui/bookrightpage.png");
    private static final ResourceLocation bookLeftPage = new ResourceLocation(
            "tinker",
            "textures/gui/bookleftpage.png");

    private long guiOpenTime;

    public float scale = 1.0f;
    private int baseDrawingX;
    private int baseDrawingY;
    BookPage pageLeft;
    BookPage pageRight;

    public SmallFontRenderer fonts = MProxyClient.smallFontRenderer;

    private List<TiCNavigationButton> navigationButtonsList;

    public TiCGuiManual(ItemStack stack, BookData data) {
        super(stack, data);
        this.mc = Minecraft.getMinecraft();
        this.itemstackBook = stack;
        currentPage = 0; // Stack page
        manual = data.getDoc();
        if (data.font != null) this.fonts = data.font;
        this.bData = data;
        this.guiOpenTime = System.currentTimeMillis();

        // TConstructRegistry.toolMaterialStrings.forEach((str, tm) -> { System.out.println(str + " - " + tm.name());
        // });
        // PatternBuilder.instance.materials.forEach(k -> System.out.println(k.key + " - " +
        // k.item.getUnlocalizedName()));
        // PatternBuilder.instance.materialSets.forEach((str, mset) -> System.out.println(str + " - " + mset));

        // renderitem.renderInFrame = true;
    }

    /*
     * @Override publi c void setWorldAndResolution (Minecraft minecraft, int w, int h) { this.guiParticles = new
     * GuiParticle(minecraft); this.mc = minecraft; this.width = w; this.height = h; this.buttonList.clear();
     * this.initGui(); }
     */

    public void initGui() {
        maxPages = manual.getElementsByTagName("page").getLength();
        ticUpdateText();
        int xPos = this.width / 2;
        this.buttonList.add(
                this.buttonNextPage = new TiCTurnPageButton(
                        1,
                        xPos + bookImageWidth - 50,
                        (this.height + this.bookImageHeight) / 2 - 28,
                        ButtonType.nextPage));
        this.buttonList.add(
                this.buttonPreviousPage = new TiCTurnPageButton(
                        2,
                        xPos - bookImageWidth + 24,
                        (this.height + this.bookImageHeight) / 2 - 28,
                        ButtonType.previousPage));

        this.buttonList.add(
                this.buttonHomePage = new TiCTurnPageButton(
                        3,
                        xPos - bookImageWidth - 24,
                        this.height - this.bookImageHeight,
                        ButtonType.homePage));
        updateButtonVisibility();
    }

    private void updateButtonVisibility() {
        buttonPreviousPage.visible = currentPage > 0;
        buttonNextPage.visible = currentPage < maxPages - 2;
        buttonHomePage.visible = currentPage != 0;
    }

    protected void actionPerformed(GuiButton button) {
        if (button.enabled) {
            if (button instanceof TiCTurnPageButton) {
                changePage(button.id);
            } else if (button instanceof TiCNavigationButton nb) {
                TConstruct.logger.info("navigation to " + nb.target + "");
            }
            updateButtonVisibility();
            ticUpdateText();
        }
    }

    void ticUpdateText() {
        if (maxPages % 2 == 1) {
            if (currentPage > maxPages) currentPage = maxPages;
        } else {
            if (currentPage >= maxPages) currentPage = maxPages - 2;
        }
        if (currentPage % 2 == 1) currentPage--;
        if (currentPage < 0) currentPage = 0;

        NodeList nList = manual.getElementsByTagName("page");

        Node node = nList.item(currentPage);
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;
            Class<? extends BookPage> clazz = MProxyClient.getPageClass(element.getAttribute("type"));
            if (clazz != null) {
                try {
                    pageLeft = clazz.getDeclaredConstructor().newInstance();
                    pageLeft.init(this, 0);
                    pageLeft.readPageFromXML(element);
                } catch (Exception e) {
                    TConstruct.logger.error(e);
                }
            } else {
                pageLeft = null;
            }
        }

        node = nList.item(currentPage + 1);
        if (node != null && node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;
            Class<? extends BookPage> clazz = MProxyClient.getPageClass(element.getAttribute("type"));
            if (clazz != null) {
                try {
                    pageRight = clazz.getDeclaredConstructor().newInstance();
                    pageRight.init(this, 1);
                    pageRight.readPageFromXML(element);
                } catch (Exception e) {
                    TConstruct.logger.error(e);
                }
            } else {
                pageLeft = null;
            }
        } else {
            pageRight = null;
        }
    }

    private void changePage(int buttonId) {
        if (buttonId == 1) {
            currentPage += 2;
        }
        if (buttonId == 2) {
            currentPage -= 2;
        }
        if (buttonId == 3) {
            currentPage = 0;
        }
    }

    public void setCurrentPage(int pageNum) {
        this.currentPage = Math.min(Math.max(pageNum % 2 == 1 ? pageNum - 1 : pageNum, 0), maxPages - 2);
        updateButtonVisibility();
        ticUpdateText();
    }

    public void drawScreen(int par1, int par2, float par3) {
        navigationButtonsList = new ArrayList<>();
        this.buttonList.subList(3, this.buttonList.size()).clear();

        this.scale = Math.max(
                0.95f,
                Math.min(
                        this.width * GUIMAXPERCENTAGE / (this.bookImageWidth * 2),
                        this.height * GUIMAXPERCENTAGE / this.bookImageHeight));

        float progress = (System.currentTimeMillis() - this.guiOpenTime) * 1.0f / ANIMATIONDURATIONINMILLIS;

        int[] point = this.getOvershootPosition(progress, scale);
        this.baseDrawingX = point[0];
        this.baseDrawingY = point[1];

        int drawX = (int) (this.baseDrawingX / scale);
        int drawY = (int) (this.baseDrawingY / scale);

        GL11.glPushMatrix();
        GL11.glScalef(scale, scale, 1.0f);

        int backGroundColor = 0xD89A1D;
        if (this.bData instanceof TiCBookData tbd) {
            backGroundColor = tbd.getBookColor();
        }

        float r = ((backGroundColor >> 16) & 0xFF) / 255.0f;
        float g = ((backGroundColor >> 8) & 0xFF) / 255.0f;
        float b = (backGroundColor & 0xFF) / 255.0f;

        GL11.glColor4f(r, g, b, 1.0F);
        this.mc.getTextureManager().bindTexture(bookRightBackGround);
        this.drawTexturedModalRect(drawX, drawY, 0, 0, this.bookImageWidth, this.bookImageHeight);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(bookRightPage);
        this.drawTexturedModalRect(drawX, drawY, 0, 0, this.bookImageWidth, this.bookImageHeight);

        drawX = drawX - this.bookImageWidth;
        int textureX = 256 - this.bookImageWidth;
        GL11.glColor4f(r, g, b, 1.0F);
        this.mc.getTextureManager().bindTexture(bookLeftBackGround);
        this.drawTexturedModalRect(drawX, drawY, textureX, 0, this.bookImageWidth, this.bookImageHeight);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(bookLeftPage);
        this.drawTexturedModalRect(drawX, drawY, textureX, 0, this.bookImageWidth, this.bookImageHeight);

        this.drawButtons(par1, par2, scale);

        if (pageLeft != null) pageLeft.renderBackgroundLayer(drawX + 16, drawY + 12);
        if (pageRight != null) pageRight.renderBackgroundLayer(drawX + 220, drawY + 12);
        if (pageLeft != null) {
            if (pageLeft instanceof NavigationPage np) {
                navigationButtonsList
                        .addAll(np.updateButtonPositionAndRender(drawX + 16, drawY + 12, scale, par1, par2));
            } else {
                pageLeft.renderContentLayer(drawX + 16, drawY + 12, bData.isTranslatable);
            }
        }
        if (pageRight != null) {
            if (pageRight instanceof NavigationPage np) {
                navigationButtonsList
                        .addAll(np.updateButtonPositionAndRender(drawX + 220, drawY + 12, scale, par1, par2));
            } else {
                pageRight.renderContentLayer(drawX + 220, drawY + 12, bData.isTranslatable);
            }
        }

        this.buttonList.addAll(navigationButtonsList);

        GL11.glPopMatrix();
        this.renderTooltips(par1, par2);
    }

    void renderTooltips(int mouseX, int mouseY) {
        List<String> tooltip = new ArrayList<String>();
        this.buttonList.forEach(b -> {
            if (b instanceof TiCGuiButton tgb && tgb.isHover(mouseX, mouseY)) {
                tooltip.addAll(tgb.getTooltips());
            }
        });
        this.drawHoveringText(tooltip, mouseX, mouseY, fontRendererObj);
    }

    /**
     * copy from {@link net.minecraft.client.gui.GuiScreen#drawScreen(int, int, float)}
     */
    public void drawButtons(int mouseX, int mouseY, float scale) {

        // base on `xPos + bookImageWidth - 50`, (206 - 50) / 206 ≈ 0.757 and (206 - 24) / 206 ≈ 0.883
        this.buttonNextPage.xPosition = this.baseDrawingX + (int) (this.bookImageWidth * scale * 0.8f);
        this.buttonPreviousPage.xPosition = this.baseDrawingX - (int) (this.bookImageWidth * scale * 0.883f);
        this.buttonHomePage.xPosition = this.baseDrawingX
                - (int) ((this.bookImageWidth + TiCTurnPageButton.ButtonType.homePage.textureWidth * 1.15f) * scale);

        // base on scale calculate the real y position of the bottom gui
        // and base on `(this.height + this.bookImageHeight) / 2 - 28`, the default button height is 13
        // 28 / 13 ≈ 2.15, so keep the same ratio
        int yPosition = this.baseDrawingY
                + (int) ((this.bookImageHeight - TiCTurnPageButton.ButtonType.nextPage.textureHeight * 2.747f) * scale);
        this.buttonNextPage.yPosition = yPosition;
        this.buttonPreviousPage.yPosition = yPosition;
        this.buttonHomePage.yPosition = this.baseDrawingY
                + (int) (TiCTurnPageButton.ButtonType.homePage.textureHeight * 0.5f);

        this.buttonNextPage.drawButtonWithScale(this.mc, mouseX, mouseY, scale);
        this.buttonPreviousPage.drawButtonWithScale(this.mc, mouseX, mouseY, scale);
        this.buttonHomePage.drawButtonWithScale(this.mc, mouseX, mouseY, scale);

        // copy from @GuiScreen.drawScreen
        // int k;

        // for (k = 0; k < this.buttonList.size(); ++k) {
        // ((GuiButton) this.buttonList.get(k)).drawButton(this.mc, mouseX, mouseY);
        // }

        // for (k = 0; k < this.labelList.size(); ++k) {
        // ((GuiLabel) this.labelList.get(k)).func_146159_a(this.mc, mouseX, mouseY);
        // }
    }

    public Minecraft getMC() {
        return mc;
    }

    public boolean doesGuiPauseGame() {
        return false;
    }

    /**
     * Computes the current position after applying an overshoot and bounce animation.
     *
     * @param progress global animation progress in [0,1], where 0 = start, 1 = end
     * @return the current (X, Y) point for the given progress
     */
    private int[] getOvershootPosition(float progress, float scale) {
        int endX = (this.width / 2);
        int startX = endX;

        int endY = (int) ((this.height - this.bookImageHeight * scale) / 2);
        int startY = (int) (this.height + this.bookImageHeight * scale);

        // Clamp progress to [0,1]
        float t = Math.min(Math.max(progress, 0.0f), 1.0f);

        float factor; // displacement factor (0 → 1+EXTENT → 1)

        if (t <= FLYIN_DURATION) {
            // Phase 1: linear fly in
            float phaseT = t / FLYIN_DURATION; // [0,1]
            factor = phaseT;
        } else if (t <= FLYIN_DURATION + OVERSHOOT_DURATION) {
            // Phase 2: overshoot (1 → 1+EXTENT) with ease out
            float phaseT = (t - FLYIN_DURATION) / OVERSHOOT_DURATION; // [0,1]
            float eased = (float) (1.0f - Math.pow(1.0f - phaseT, 2.0f));
            factor = 1.0f + EXTENT * eased;
        } else {
            // Phase 3: correct back to target (1+EXTENT → 1) linear
            float remaining = 1.0f - (FLYIN_DURATION + OVERSHOOT_DURATION);
            float phaseT = (t - (FLYIN_DURATION + OVERSHOOT_DURATION)) / remaining; // [0,1]
            float peak = 1.0f + EXTENT;
            factor = peak + (1.0f - peak) * phaseT;
        }

        // Clamp factor to reasonable range (should stay inside [0, 1+EXTENT])
        factor = Math.min(factor, 1.0f + EXTENT);

        // Linear interpolation
        int x = (int) (startX + (endX - startX) * factor);
        int y = (int) (startY + (endY - startY) * factor);

        return new int[] { x, y };
    }

}
