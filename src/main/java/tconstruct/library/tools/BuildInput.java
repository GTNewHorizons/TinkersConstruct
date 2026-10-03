package tconstruct.library.tools;

import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.util.IIcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import tconstruct.library.crafting.ToolRecipe;

/** The parts a tool is built from, in ToolBuilder.buildTool's order, each with its InfiTool key and render pass. */
public enum BuildInput {

    HEAD("Head", 1),
    HANDLE("Handle", 0),
    ACCESSORY("Accessory", 2),
    EXTRA("Extra", 3);

    public static final BuildInput[] VALUES = values();
    private static final BuildInput[] BY_PASS = new BuildInput[VALUES.length];

    static {
        for (BuildInput input : VALUES) BY_PASS[input.renderPass] = input;
    }

    public final String key;
    public final String renderKey;
    public final int renderPass;

    BuildInput(String key, int renderPass) {
        this.key = key;
        this.renderKey = "Render" + key;
        this.renderPass = renderPass;
    }

    public int cell() {
        return ordinal() + 1;
    }

    public static BuildInput ofCell(int cell) {
        return cell >= 1 && cell <= VALUES.length ? VALUES[cell - 1] : null;
    }

    /** The kind of part ToolCore draws on this render pass, 0 to 3. */
    public static BuildInput ofPass(int pass) {
        return BY_PASS[pass];
    }

    public Item partOf(ToolCore tool) {
        return switch (this) {
            case HEAD -> tool.getHeadItem();
            case HANDLE -> tool.getHandleItem();
            case ACCESSORY -> tool.getAccessoryItem();
            case EXTRA -> tool.getExtraItem();
        };
    }

    @SideOnly(Side.CLIENT)
    public Map<Integer, IIcon> iconsOf(ToolCore tool) {
        return switch (this) {
            case HEAD -> tool.headIcons;
            case HANDLE -> tool.handleIcons;
            case ACCESSORY -> tool.accessoryIcons;
            case EXTRA -> tool.extraIcons;
        };
    }

    public boolean validIn(ToolRecipe recipe, Item item) {
        return switch (this) {
            case HEAD -> recipe.validHead(item);
            case HANDLE -> recipe.validHandle(item);
            case ACCESSORY -> recipe.validAccessory(item);
            case EXTRA -> recipe.validExtra(item);
        };
    }
}
