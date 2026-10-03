package tconstruct.tools.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Each block's slot layouts, drawn for 18 px cells, and how they stretch to the screen's 20 px cells. */
@SideOnly(Side.CLIENT)
public final class StationSlotLayouts {

    private StationSlotLayouts() {}

    // Where the slots a layout does not use park while they hold something
    static final int PARK_X = 85, PARK_Y = 62, PARK_PITCH = 22;

    // a cell is a 1 px border around the 18 px vanilla hovers and clicks; TiC2's layouts and 3.7x tool picture are
    // drawn for 18 px cells, so both stretch by 20/18 about the picture's corner and each slot keeps its place
    static final int CELL = 20, TIC2_CELL = 18, ITEM_INSET = (CELL - 16) / 2;
    static final float GHOST_SCALE = 3.7F * CELL / TIC2_CELL;

    /** The station's layouts, x row then y row; the tabletop renderer reads type 0, the pentagon. */
    public static int[][] stationLayout(int type) {
        switch (type) {
            case 0:
                // the tool at the center, then the five material slots clockwise from the bottom left
                return new int[][] { { 33, 15, 11, 33, 55, 51 }, { 42, 62, 37, 19, 37, 62 } };
            case 1: // Three parts
                return new int[][] { { 56, 56, 56 }, { 19, 55, 37 } };
            case 2: // Two parts
                return new int[][] { { 56, 56, 14 }, { 28, 46, 37 } };
            case 3: // Double head
                return new int[][] { { 38, 47, 56 }, { 28, 46, 28 } };
            case 7: // Three parts reverse
                return new int[][] { { 56, 56, 56 }, { 19, 37, 55 } };
            default:
                return null;
        }
    }

    static int[][] forgeLayout(int type) {
        switch (type) {
            case 0:
                return stationLayout(0);
            case 1: // Three parts
                return new int[][] { { 56, 56, 56, 14 }, { 19, 55, 37, 37 } };
            case 2: // Two parts
                return new int[][] { { 56, 56, 14, 14 }, { 28, 46, 28, 46 } };
            case 3: // Double head
                return new int[][] { { 38, 47, 56, 14 }, { 28, 46, 28, 37 } };
            case 4: // Four parts
                return new int[][] { { 47, 38, 56, 47 }, { 19, 37, 37, 55 } };
            case 5: // Four parts, double head
                return new int[][] { { 38, 47, 56, 47 }, { 19, 55, 19, 37 } };
            case 6: // Double head
                return new int[][] { { 38, 38, 20, 56 }, { 28, 46, 28, 28 } };
            case 7: // Three parts reverse
                return new int[][] { { 56, 56, 56, 14 }, { 19, 37, 55, 37 } };
            case 8: // Double head middle
                return new int[][] { { 20, 38, 56, 38 }, { 28, 46, 28, 28 } };
            case 9: // Four parts, crossbow.
                return new int[][] { { 38, 56, 47, 47 }, { 37, 37, 55, 19 } };
            default:
                return null;
        }
    }

    static int stretch(int tic2Cell, int corner) {
        float middle = tic2Cell + TIC2_CELL / 2F;
        return Math.round(corner + (middle - corner) * CELL / TIC2_CELL) - CELL / 2;
    }
}
