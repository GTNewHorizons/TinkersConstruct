package tconstruct.library.client;

import net.minecraft.util.StatCollector;

import tconstruct.library.TConstructRegistry;
import tconstruct.library.tools.ToolCore;

public class ToolGuiElement extends GuiElement {

    public final int slotType;
    public final int[] iconsX;
    public final int[] iconsY;
    public final String title;
    public final String body;
    /** The tool this tab builds; null on the repair tab, and when no single tool matches the title. */
    public final ToolCore tool;
    public final int[] slotX;
    public final int[] slotY;
    private final int[] slotTurns;

    public static final int TURN_LEFT = 1, TURN_HALF = 2, TURN_RIGHT = 3, MIRROR = 4;
    /** Mirrors the shape across the diagonal from its top-left corner to its bottom-right one. */
    public static final int MIRROR_DIAGONAL = MIRROR | TURN_LEFT;

    public ToolGuiElement(int st, int bx, int by, int[] xi, int[] yi, String t, String b, String d, String tex) {
        this(st, bx, by, xi, yi, t, b, d, tex, st != 0 ? findTool(t) : null);
    }

    public ToolGuiElement(int st, int bx, int by, int[] xi, int[] yi, String t, String b, String d, String tex,
            ToolCore tc) {
        this(st, bx, by, xi, yi, t, b, d, tex, tc, null);
    }

    /** layout: the slots' x, their y and, optionally, their turns, slot 1 first; null keeps the slotType's layout. */
    public ToolGuiElement(int st, int bx, int by, int[] xi, int[] yi, String t, String b, String d, String tex,
            ToolCore tc, int[][] layout) {
        super(bx, by, d, tex);
        slotType = st;
        iconsX = xi;
        iconsY = yi;
        title = t;
        body = b;
        tool = tc;
        slotX = layout != null ? layout[0] : null;
        slotY = layout != null ? layout[1] : null;
        slotTurns = layout != null && layout.length > 2 ? layout[2] : null;
    }

    /** Quarter turns left for the part in build slot cell + 1; {@link #MIRROR} flips it left to right first. */
    public int turnOf(int cell) {
        return slotTurns != null && cell < slotTurns.length ? slotTurns[cell] : 0;
    }

    // addons pass the title as a lang key or already translated; null unless exactly one tool matches
    private static ToolCore findTool(String title) {
        ToolCore found = null;
        for (ToolCore tool : TConstructRegistry.getToolMapping()) {
            String name = tool.getToolName();
            if (name == null) continue;
            String key = "gui.toolstation." + name.toLowerCase() + ".name";
            if (!tool.getUnlocalizedToolName().equals(title) && !tool.getLocalizedToolName().equals(title)
                    && !key.equals(title)
                    && !StatCollector.translateToLocal(key).equals(title))
                continue;
            if (found != null) return null;
            found = tool;
        }
        return found;
    }
}
