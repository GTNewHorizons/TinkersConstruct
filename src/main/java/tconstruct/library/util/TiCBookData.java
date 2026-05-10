package tconstruct.library.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mantle.books.BookData;
import tconstruct.library.client.TConstructClientRegistry;
import tconstruct.library.crafting.ToolBuilder;
import tconstruct.library.crafting.ToolRecipe;
import tconstruct.tools.TinkerTools;
import tconstruct.util.TiCRecipeHolder.RecipeType;

public class TiCBookData extends BookData {

    private static final String ToolPagesButtonTag = "tictoolbuttons";

    private final Map<Element, List<Element>> replaceMap = new HashMap<>();

    private Map<String, Integer> indexMap = new HashMap<>();

    private int bookColor = 0X8D754E;

    public int getBookColor() {
        return bookColor;
    }

    public TiCBookData setBookColor(int bookColor) {
        this.bookColor = bookColor;
        return this;
    }

    public TiCBookData setUnlocalizedName(String unlocalizedName) {
        this.unlocalizedName = unlocalizedName;
        return this;
    }

    public TiCBookData setToolTip(String toolTip) {
        this.toolTip = toolTip;
        return this;
    }

    public TiCBookData setModID(String modID) {
        this.modID = modID;
        return this;
    }

    public TiCBookData setItemImage(ResourceLocation itemImage) {
        this.itemImage = itemImage;
        return this;
    }

    public TiCBookData setDoc(Document doc) {
        this.doc = doc;
        this.processGenerate();

        this.setupIndex();
        return this;
    }

    private void setupIndex() {
        if (this.doc != null) {
            NodeList pages = this.doc.getElementsByTagName("page");
            int pagesSize = pages.getLength();
            String pageName;
            for (int idx = 0; idx < pagesSize; idx++) {
                Element e = (Element) pages.item(idx);
                if ((pageName = e.getAttribute("name")).length() != 0) {
                    this.indexMap.put(pageName, idx);
                }
            }
        }
    }

    public int getIndexFromName(String name) {
        if (this.indexMap.containsKey(name)) {
            return this.indexMap.get(name);
        } else {
            return -1;
        }
    }

    private void replaceElementWithMultiple(Element target, List<Element> newElements) {
        Node parent = target.getParentNode();
        Node nextSibling = target.getNextSibling();

        parent.removeChild(target);

        while (nextSibling != null && nextSibling.getNodeType() != Node.ELEMENT_NODE) {
            nextSibling = nextSibling.getNextSibling();
        }

        for (int i = 0; i < newElements.size(); i++) {
            parent.insertBefore(newElements.get(i), nextSibling);
        }
    }

    private void processGenerate() {
        this.replaceMap.clear();
        if (this.doc != null) {
            NodeList pages = this.doc.getElementsByTagName("page");
            int pagesSize = pages.getLength();
            for (int idx = 0; idx < pagesSize; idx++) {
                Element e = (Element) pages.item(idx);
                if (e.getAttribute("type").equals(ToolPagesButtonTag)) {
                    replaceMap.put(e, generateTools(e));
                }
            }
        }

        replaceMap.entrySet().forEach((e) -> replaceElementWithMultiple(e.getKey(), e.getValue()));

    }

    private List<Element> generateTools(Element parent) {
        List<Element> newPages = new ArrayList<>();
        newPages.add(parent);

        for (ToolRecipe r : ToolBuilder.instance.combos) {
            ItemStack head = r.getHeadList().size() != 0
                    ? new ItemStack(r.getHeadList().getFirst(), 1, TinkerTools.MaterialID.Cobalt)
                    : null;

            ItemStack handle = r.getHandleList().size() != 0
                    ? new ItemStack(r.getHandleList().getFirst(), 1, TinkerTools.MaterialID.Iron)
                    : null;

            ItemStack accessory = r.getAccessoryList().size() != 0
                    ? new ItemStack(r.getAccessoryList().getFirst(), 1, TinkerTools.MaterialID.Iron)
                    : null;

            ItemStack extra = r.getExtraList().size() != 0
                    ? new ItemStack(r.getExtraList().getFirst(), 1, TinkerTools.MaterialID.Ardite)
                    : null;

            ItemStack output = ToolBuilder.instance
                    .buildTool(head, handle, accessory, extra, r.getType().getLocalizedToolName());

            String toolUnlocalizedName = r.getType().getUnlocalizedToolName();

            TConstructClientRegistry.registerTiCToolRecipeIcon(
                    toolUnlocalizedName,
                    new ItemStack[][] { new ItemStack[] {head}, new ItemStack[] {handle}, new ItemStack[] {accessory}, new ItemStack[] {extra} },
                    output,
                    extra != null ? RecipeType.ToolForge : RecipeType.ToolStation);

            Element newB = this.doc.createElement("button");
            newB.setAttribute("to", toolUnlocalizedName);

            Element itemStack = this.doc.createElement("icon");
            itemStack.setTextContent(toolUnlocalizedName);

            Element desc = this.doc.createElement("text");
            desc.setTextContent(output.getDisplayName());

            newB.appendChild(itemStack);
            newB.appendChild(desc);
            parent.appendChild(newB);

            Element newP = this.doc.createElement("page");
            newP.setAttribute("type", "ticcrafting");
            newP.setAttribute("name", toolUnlocalizedName);

            itemStack = this.doc.createElement("icon");
            itemStack.setTextContent(toolUnlocalizedName);
            newP.appendChild(itemStack);
            newPages.add(newP);
        }
        parent.setAttribute("type", "navigation");

        return newPages;
    }

}
