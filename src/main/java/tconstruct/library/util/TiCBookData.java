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
import tconstruct.items.tools.Cutlass;
import tconstruct.library.client.TConstructClientRegistry;
import tconstruct.library.crafting.ToolBuilder;
import tconstruct.library.crafting.ToolRecipe;
import tconstruct.library.tools.DualMaterialToolPart;
import tconstruct.library.weaponry.AmmoItem;
import tconstruct.tools.TinkerTools;
import tconstruct.util.TiCRecipeHolder.RecipeType;
import tconstruct.weaponry.ammo.ArrowAmmo;
import tconstruct.weaponry.ammo.BoltAmmo;

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

        this.setupIndexAndMakeSureIsOdd();
        return this;
    }

    private void setupIndexAndMakeSureIsOdd() {
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
            if (pagesSize % 2 == 1) {
                this.doc.getDocumentElement().appendChild(this.doc.createElement("page"));
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
            // hide cutless as default
            if (r.getType() instanceof Cutlass) {
                continue;
            }

            ItemStack head = r.getHeadList().size() != 0
                    ? new ItemStack(r.getHeadList().getFirst(), 1, TinkerTools.MaterialID.Cobalt)
                    : null;

            ItemStack handle = r.getHandleList().size() != 0
                    ? new ItemStack(r.getHandleList().getFirst(), 1, TinkerTools.MaterialID.Wood)
                    : null;

            ItemStack accessory = r.getAccessoryList().size() != 0
                    ? new ItemStack(r.getAccessoryList().getFirst(), 1, TinkerTools.MaterialID.Iron)
                    : null;

            ItemStack extra = r.getExtraList().size() != 0
                    ? new ItemStack(r.getExtraList().getFirst(), 1, TinkerTools.MaterialID.Ardite)
                    : null;

            if (r.getType() instanceof BoltAmmo) {
                head = new ItemStack(r.getHeadList().getFirst(), 1, TinkerTools.MaterialID.Iron);
                accessory = new ItemStack(r.getAccessoryList().getFirst(), 1, 0);
            }

            ItemStack output = ToolBuilder.instance
                    .buildTool(head, handle, accessory, extra, r.getType().getLocalizedToolName());

            if (output != null) {
                output.getTagCompound().getCompoundTag("InfiTool").setBoolean("Built", true);
                if (r.getType() instanceof ArrowAmmo) {
                    // special display for arrow ammo
                    handle = new ItemStack(TinkerTools.toolRod, 1, TinkerTools.MaterialID.Wood);
                } else if (r.getType() instanceof BoltAmmo) {
                    // special display for bolt ammo
                    head = DualMaterialToolPart.createDualMaterial(
                            r.getHeadList().getFirst(),
                            TinkerTools.MaterialID.Wood,
                            TinkerTools.MaterialID.Iron);
                    handle = null;
                }

                if (r.getType() instanceof AmmoItem ai) {
                    ai.setAmmo(ai.getMaxAmmo(output), output);
                }

                String toolUnlocalizedName = r.getType().getUnlocalizedToolName();

                TConstructClientRegistry.registerTiCToolRecipeIcon(
                        toolUnlocalizedName,
                        new ItemStack[][] { new ItemStack[] { head }, new ItemStack[] { accessory },
                                new ItemStack[] { handle }, new ItemStack[] { extra } },
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

        }
        parent.setAttribute("type", "navigation");

        return newPages;
    }

}
