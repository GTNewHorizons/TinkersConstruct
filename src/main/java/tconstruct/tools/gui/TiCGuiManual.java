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

@SideOnly(Side.CLIENT)
public class TiCGuiManual extends GuiManual {

    ItemStack itemstackBook;
    Document manual;
    public RenderItemCopy renderitem = new RenderItemCopy();
    int bookImageWidth = 206;
    int bookImageHeight = 200;
    int bookTotalPages = 1;
    int currentPage;
    int maxPages;
    BookData bData;

    private TurnPageButton buttonNextPage;
    private TurnPageButton buttonPreviousPage;
    private static ResourceLocation bookRight;// = new ResourceLocation("mantle", "textures/gui/bookright.png");
    private static ResourceLocation bookLeft;// = new ResourceLocation("mantle", "textures/gui/bookleft.png");

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
        int xPos = (this.width) / 2; // TODO Width?
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
                    System.out.println(e);
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
                    System.out.println(e);
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
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(bookRight);

        // aligen to center
        int localWidth = (this.width / 2);
        int localHeight = ((this.height - this.bookImageHeight) / 2);

        this.drawTexturedModalRect(localWidth, localHeight, 0, 0, this.bookImageWidth, this.bookImageHeight);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(bookLeft);
        localWidth = localWidth - this.bookImageWidth;
        this.drawTexturedModalRect(
                localWidth,
                localHeight,
                256 - this.bookImageWidth,
                0,
                this.bookImageWidth,
                this.bookImageHeight);

        this.drawButtons(par1, par2);

        if (pageLeft != null) pageLeft.renderBackgroundLayer(localWidth + 16, localHeight + 12);
        if (pageRight != null) pageRight.renderBackgroundLayer(localWidth + 220, localHeight + 12);
        if (pageLeft != null) pageLeft.renderContentLayer(localWidth + 16, localHeight + 12, bData.isTranslatable);
        if (pageRight != null) pageRight.renderContentLayer(localWidth + 220, localHeight + 12, bData.isTranslatable);

    }

    /**
     * copy from {@link net.minecraft.client.gui.GuiScreen#drawScreen(int, int, float)}
     */
    public void drawButtons(int mouseX, int mouseY) {
        // copy from @GuiScreen.drawScreen
        int k;

        for (k = 0; k < this.buttonList.size(); ++k) {
            ((GuiButton) this.buttonList.get(k)).drawButton(this.mc, mouseX, mouseY);
        }

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
}
