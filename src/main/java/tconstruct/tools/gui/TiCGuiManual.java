package tconstruct.tools.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
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
import mantle.client.gui.TurnPageButton;
import mantle.client.pages.BookPage;
import tconstruct.TConstruct;

@SideOnly(Side.CLIENT)
public class TiCGuiManual extends GuiManual {

    private static final int ANIMATIONDURATIONINMILLIS = 600;
    private static final double FLYIN_DURATION = 0.55; // portion for fly-in
    private static final double OVERSHOOT_DURATION = 0.1; // portion for overshoot
    private static final double EXTENT = 0.02; // overshoot amount as fraction of total displacement

    ItemStack itemstackBook;
    Document manual;
    public RenderItemCopy renderitem = new RenderItemCopy();
    int bookImageWidth = 206;
    int bookImageHeight = 200;
    int bookTotalPages = 1;
    int currentPage;
    int maxPages;
    BookData bData;

    private boolean needUpdateAnimation;

    private TurnPageButton buttonNextPage;
    private TurnPageButton buttonPreviousPage;
    private static ResourceLocation bookRight;// = new ResourceLocation("mantle", "textures/gui/bookright.png");
    private static ResourceLocation bookLeft;// = new ResourceLocation("mantle", "textures/gui/bookleft.png");

    private long guiOpenTime;

    private int baseDrawingX;
    private int baseDrawingY;
    BookPage pageLeft;
    BookPage pageRight;

    public SmallFontRenderer fonts = MProxyClient.smallFontRenderer;

    public TiCGuiManual(ItemStack stack, BookData data) {
        super(stack, data);
        this.mc = Minecraft.getMinecraft();
        this.itemstackBook = stack;
        currentPage = 0; // Stack page
        manual = data.getDoc();
        if (data.font != null) this.fonts = data.font;
        bookLeft = data.leftImage;
        bookRight = data.rightImage;
        this.bData = data;
        this.guiOpenTime = System.currentTimeMillis();
        this.needUpdateAnimation = true;

        // renderitem.renderInFrame = true;
    }

    /*
     * @Override public void setWorldAndResolution (Minecraft minecraft, int w, int h) { this.guiParticles = new
     * GuiParticle(minecraft); this.mc = minecraft; this.width = w; this.height = h; this.buttonList.clear();
     * this.initGui(); }
     */

    @SuppressWarnings("unchecked")
    public void initGui() {
        maxPages = manual.getElementsByTagName("page").getLength();
        ticUpdateText();
        int xPos = this.width / 2; // TODO Width?
        // TODO buttonList
        this.buttonList.add(
                this.buttonNextPage = new TurnPageButton(
                        1,
                        xPos + bookImageWidth - 50,
                        (this.height + this.bookImageHeight) / 2 - 28,
                        true,
                        bData));
        this.buttonList.add(
                this.buttonPreviousPage = new TurnPageButton(
                        2,
                        xPos - bookImageWidth + 24,
                        (this.height + this.bookImageHeight) / 2 - 28,
                        false,
                        bData));
        updateButtonVisibility();
    }

    private void updateButtonVisibility() {
        buttonPreviousPage.visible = currentPage > 0;
        buttonNextPage.visible = currentPage < maxPages - 2;
    }

    protected void actionPerformed(GuiButton button) {
        if (button.enabled) {
            changePage(button.id);
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
    }

    public void drawScreen(int par1, int par2, float par3) {
        // aligen to center
        // int localWidth = (this.width / 2);
        // int localHeight = ((this.height - this.bookImageHeight) / 2);

        float scale = Math.max(
                1.0f,
                Math.min(this.width * 0.8f / (this.bookImageWidth * 2), this.height * 0.8f / this.bookImageHeight));

        if (this.needUpdateAnimation) {
            float progress = (System.currentTimeMillis() - this.guiOpenTime) * 1.0f / ANIMATIONDURATIONINMILLIS;
            int[] point = this.getOvershootPosition(progress);
            this.baseDrawingX = point[0];
            this.baseDrawingY = point[1];
            if (progress >= 1.0f) this.needUpdateAnimation = false;
        }

        int drawX = (int) (this.baseDrawingX / scale);
        int drawY = (int) (this.baseDrawingY / scale / scale);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glScalef(scale, scale, 1.0f);

        this.mc.getTextureManager().bindTexture(bookRight);
        this.drawTexturedModalRect(drawX, drawY, 0, 0, this.bookImageWidth, this.bookImageHeight);

        this.mc.getTextureManager().bindTexture(bookLeft);
        drawX = drawX - this.bookImageWidth;
        this.drawTexturedModalRect(
                drawX,
                drawY,
                256 - this.bookImageWidth,
                0,
                this.bookImageWidth,
                this.bookImageHeight);

        if (!this.needUpdateAnimation) this.drawButtons(par1, par2, drawX + this.bookImageWidth, drawY);

        if (pageLeft != null) pageLeft.renderBackgroundLayer(drawX + 16, drawY + 12);
        if (pageRight != null) pageRight.renderBackgroundLayer(drawX + 220, drawY + 12);
        if (pageLeft != null) pageLeft.renderContentLayer(drawX + 16, drawY + 12, bData.isTranslatable);
        if (pageRight != null) pageRight.renderContentLayer(drawX + 220, drawY + 12, bData.isTranslatable);
    }

    /**
     * copy from {@link net.minecraft.client.gui.GuiScreen#drawScreen(int, int, float)}
     */
    public void drawButtons(int mouseX, int mouseY, int x, int y) {

        this.buttonNextPage.xPosition = (int) (x + this.bookImageWidth * 0.8);
        this.buttonPreviousPage.xPosition = (int) (x - this.bookImageWidth * 0.9);

        this.buttonNextPage.yPosition = (int) (y + this.bookImageHeight * 0.85);
        this.buttonPreviousPage.yPosition = (int) (y + this.bookImageHeight * 0.85);

        this.buttonNextPage.drawButton(this.mc, mouseX, mouseY);
        this.buttonPreviousPage.drawButton(this.mc, mouseX, mouseY);

        // copy from @GuiScreen.drawScreen
        int k;

        // for (k = 0; k < this.buttonList.size(); ++k) {
        // ((GuiButton) this.buttonList.get(k)).drawButton(this.mc, mouseX, mouseY);
        // }

        for (k = 0; k < this.labelList.size(); ++k) {
            ((GuiLabel) this.labelList.get(k)).func_146159_a(this.mc, mouseX, mouseY);
        }
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
    private int[] getOvershootPosition(float progress) {

        int endX = (this.width / 2);
        int startX = endX;

        int endY = (this.height - this.bookImageHeight) / 2;
        int startY = this.height + this.bookImageHeight;

        // Clamp progress to [0,1]
        double t = Math.min(Math.max(progress, 0.0), 1.0);

        double factor; // displacement factor (0 → 1+EXTENT → 1)

        if (t <= FLYIN_DURATION) {
            // Phase 1: linear fly in
            double phaseT = t / FLYIN_DURATION; // [0,1]
            factor = phaseT;
        } else if (t <= FLYIN_DURATION + OVERSHOOT_DURATION) {
            // Phase 2: overshoot (1 → 1+EXTENT) with ease out
            double phaseT = (t - FLYIN_DURATION) / OVERSHOOT_DURATION; // [0,1]
            double eased = 1.0 - Math.pow(1.0 - phaseT, 2);
            factor = 1.0 + EXTENT * eased;
        } else {
            // Phase 3: correct back to target (1+EXTENT → 1) linear
            double remaining = 1.0 - (FLYIN_DURATION + OVERSHOOT_DURATION);
            double phaseT = (t - (FLYIN_DURATION + OVERSHOOT_DURATION)) / remaining; // [0,1]
            double peak = 1.0 + EXTENT;
            factor = peak + (1.0 - peak) * phaseT;
        }

        // Clamp factor to reasonable range (should stay inside [0, 1+EXTENT])
        factor = Math.min(factor, 1.0 + EXTENT);

        // Linear interpolation
        int x = (int) (startX + (endX - startX) * factor);
        int y = (int) (startY + (endY - startY) * factor);

        return new int[] { x, y };
    }

}
