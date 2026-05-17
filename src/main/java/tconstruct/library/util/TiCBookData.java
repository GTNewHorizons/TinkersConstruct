package tconstruct.library.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mantle.books.BookData;
import tconstruct.TConstruct;
import tconstruct.items.tools.Cutlass;
import tconstruct.library.TConstructRegistry;
import tconstruct.library.client.TConstructClientRegistry;
import tconstruct.library.crafting.CastingRecipe;
import tconstruct.library.crafting.ModifyBuilder;
import tconstruct.library.crafting.PatternBuilder;
import tconstruct.library.crafting.PatternBuilder.ItemKey;
import tconstruct.library.crafting.PatternBuilder.MaterialSet;
import tconstruct.library.crafting.ToolBuilder;
import tconstruct.library.crafting.ToolRecipe;
import tconstruct.library.modifier.ItemModifier;
import tconstruct.library.tools.DualMaterialToolPart;
import tconstruct.library.tools.ToolMaterial;
import tconstruct.library.weaponry.AmmoItem;
import tconstruct.tools.TinkerTools;
import tconstruct.tools.items.ToolShard;
import tconstruct.util.TiCRecipeHolder.RecipeType;
import tconstruct.util.config.PHConstruct;
import tconstruct.weaponry.ammo.ArrowAmmo;
import tconstruct.weaponry.ammo.BoltAmmo;
import tconstruct.weaponry.weapons.Crossbow;

public class TiCBookData extends BookData {

    private static final String ToolPagesButtonTag = "tictoolbuttons";
    private static final String MaterialsPagesButtonTag = "ticmaterialsbuttons";
    private static final String ModifiesPagesButtonTag = "ticmodifiesbuttons";

    public boolean isInit = false;

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
        return this;
    }

    @Override
    public Document getDoc() {
        this.isInit = false;
        if (!this.isInit) {
            this.isInit = true;
            this.processGenerate();
            this.setupIndexAndMakeSureIsOdd();
        }
        return this.doc;
    }

    private void setupIndexAndMakeSureIsOdd() {
        if (this.doc != null) {
            NodeList pages = this.doc.getElementsByTagName("page");
            int pagesSize = pages.getLength();
            String pageName;
            for (int idx = 0; idx < pagesSize; idx++) {
                Element e = (Element) pages.item(idx);
                if ((pageName = e.getAttribute("name")).length() != 0 && !this.indexMap.containsKey(pageName)) {
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
                } else if (e.getAttribute("type").equals(MaterialsPagesButtonTag)) {
                    replaceMap.put(e, generateMaterials(e));
                } else if (e.getAttribute("type").equals(ModifiesPagesButtonTag)) {
                    replaceMap.put(e, generateModifes(e));
                }
            }
            replaceMap.entrySet().forEach((e) -> replaceElementWithMultiple(e.getKey(), e.getValue()));
        }
    }

    private List<Element> generateTools(Element parent) {
        List<Element> navigationPages = new ArrayList<>();
        List<Element> newPages = new ArrayList<>();
        parent.setAttribute("type", "navigation");

        String sizeStr = parent.getAttribute("capacity");
        int size = 5 * 5;
        int counter = 0;
        if (sizeStr.length() != 0) {
            size = Integer.parseInt(sizeStr);
            size *= size;
        }

        Element target = (Element) parent.cloneNode(false);

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
            } else if (r.getType() instanceof Crossbow) {
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
                target.appendChild(newB);

                counter++;
                if (counter >= size) {
                    navigationPages.add(target);
                    target = (Element) parent.cloneNode(false);
                    counter = 0;
                }

                Element newP = this.doc.createElement("page");
                newP.setAttribute("type", "ticcrafting");
                newP.setAttribute("name", toolUnlocalizedName);

                itemStack = this.doc.createElement("icon");
                itemStack.setTextContent(toolUnlocalizedName);
                newP.appendChild(itemStack);
                newPages.add(newP);
            }

        }
        if (counter != 0) {
            navigationPages.add(target);
        }

        navigationPages.addAll(newPages);

        return navigationPages;
    }

    private List<Element> generateMaterials(Element parent) {

        Set<String> materialNames = new HashSet<String>();

        List<Element> navigationPages = new ArrayList<>();
        List<Element> newPages = new ArrayList<>();
        parent.setAttribute("type", "navigation");
        Element target = (Element) parent.cloneNode(false);

        String sizeStr = parent.getAttribute("capacity");
        int size = 5 * 5;
        int counter = 0;
        if (sizeStr.length() != 0) {
            size = Integer.parseInt(sizeStr);
            size *= size;
        }

        String formatedName;
        ToolMaterial material;
        for (int matID : TConstructRegistry.toolMaterials.keySet()) {
            List<ItemStack> toolParts = new ArrayList<>();

            material = TConstructRegistry.toolMaterials.get(matID);

            if (material == null) {
                TConstruct.logger.error(matID + " is null");
                continue;
            }

            if (materialNames.contains(material.materialName)) {
                continue;
            } else {
                materialNames.add(material.materialName);
            }

            for (ItemKey key : PatternBuilder.instance.materials) {
                MaterialSet set = PatternBuilder.instance.materialSets.get(key.key);
                if (set.materialID == matID) {
                    toolParts.add(new ItemStack(key.item, 1, key.damage));
                }
            }

            for (List<?> list : TConstructRegistry.patternPartMapping.keySet()) {
                if ((Integer) list.get(2) == matID) {
                    toolParts.add(TConstructRegistry.patternPartMapping.get(list));
                }
            }

            if (!PHConstruct.craftMetalTools) {
                for (CastingRecipe recipe : TConstructRegistry.getTableCasting().getCastingRecipes()) {
                    ItemStack castResult = recipe.getResult();
                    if (castResult.getItem() instanceof IToolPart) {
                        if (((IToolPart) castResult.getItem()).getMaterialID(castResult) == matID) {
                            toolParts.add(castResult);
                        }
                    }
                }
            }

            Map<Object, ItemStack> seen = new HashMap<>();
            // TODO how to change to ItemStack.areItemStacksEqual?
            List<ItemStack> newToolParts = toolParts.stream().filter(
                    i -> (!(i.getItem() instanceof ToolShard || i.getItem().delegate.name().endsWith("ToolPartChunk"))))
                    .filter(p -> {
                        String k = p.getItem() + "@" + p.getItemDamage();
                        if (seen.containsKey(k)) {
                            return false;
                        } else {
                            seen.put(k, p);
                            return true;
                        }
                    }).collect(Collectors.toList());

            if (newToolParts.size() == 0 && toolParts.size() != 0) {
                newToolParts.add(toolParts.get(0));
            }

            toolParts = newToolParts;

            formatedName = "material_" + material.materialName;
            ItemStack[] iconStack = toolParts.toArray(new ItemStack[0]);
            TConstructClientRegistry.registerManualIcon(formatedName, iconStack);

            Element newB = this.doc.createElement("button");
            newB.setAttribute("to", formatedName);
            newB.setAttribute("color", String.valueOf(material.primaryColor()));

            Element icons = this.doc.createElement("icon");
            icons.setTextContent(formatedName);

            Element desc = this.doc.createElement("text");
            desc.setTextContent(material.style() + material.prefixName());

            newB.appendChild(icons);
            newB.appendChild(desc);
            target.appendChild(newB);

            Element newP = this.doc.createElement("page");
            newP.setAttribute("type", "ticmaterial");
            newP.setAttribute("name", formatedName);

            Element materialName = this.doc.createElement("text");
            materialName.setTextContent(material.materialName);
            newP.appendChild(materialName);

            materialName = this.doc.createElement("text");
            materialName.setTextContent(String.valueOf(matID));
            newP.appendChild(materialName);

            newPages.add(newP);

            counter++;
            if (counter >= size) {
                navigationPages.add(target);
                target = (Element) parent.cloneNode(false);
                counter = 0;
            }

        }

        if (counter != 0) {
            navigationPages.add(target);
        }

        navigationPages.addAll(newPages);

        return navigationPages;
    }

    private List<Element> generateModifes(Element parent) {
        List<Element> navigationPages = new ArrayList<>();
        List<Element> newPages = new ArrayList<>();
        parent.setAttribute("type", "navigation");

        String sizeStr = parent.getAttribute("capacity");
        int size = 5 * 5;
        int counter = 0;
        if (sizeStr.length() != 0) {
            size = Integer.parseInt(sizeStr);
            size *= size;
        }

        Element target = (Element) parent.cloneNode(false);

        for (ItemModifier im : ModifyBuilder.instance.itemModifiers) {

            if (im.tooltipName != null) {
                String locString = "modifier.tooltip."
                        + EnumChatFormatting.getTextWithoutFormattingCodes(im.tooltipName).replace(" ", "");
                TConstruct.logger.info(
                        im.effectIndex + ": "
                                + im.key
                                + "================================================="
                                + StatCollector.translateToLocal(locString));
            } else {
                TConstruct.logger
                        .info(im.effectIndex + ": " + im.key + "=================================================");
            }
            List<String> collect = im.stacks.stream().map(ItemStack::getDisplayName).collect(Collectors.toList());
            TConstruct.logger.info(Arrays.toString(collect.toArray(new String[0])));

            // Element newB = this.doc.createElement("button");
            // newB.setAttribute("to", toolUnlocalizedName);
            //
            // Element itemStack = this.doc.createElement("icon");
            // itemStack.setTextContent(toolUnlocalizedName);
            //
            // Element desc = this.doc.createElement("text");
            // desc.setTextContent(output.getDisplayName());
            //
            // newB.appendChild(itemStack);
            // newB.appendChild(desc);
            // target.appendChild(newB);
            //
            // counter++;
            // if (counter >= size) {
            // navigationPages.add(target);
            // target = (Element) parent.cloneNode(false);
            // counter = 0;
            // }
            //
            // Element newP = this.doc.createElement("page");
            // newP.setAttribute("type", "ticcrafting");
            // newP.setAttribute("name", toolUnlocalizedName);
            //
            // itemStack = this.doc.createElement("icon");
            // itemStack.setTextContent(toolUnlocalizedName);
            // newP.appendChild(itemStack);
            // newPages.add(newP);

        }
        // if (counter != 0) {
        // navigationPages.add(target);
        // }

        navigationPages.addAll(newPages);

        return navigationPages;
    }

}
