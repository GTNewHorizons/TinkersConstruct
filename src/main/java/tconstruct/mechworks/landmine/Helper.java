package tconstruct.mechworks.landmine;

import net.minecraftforge.common.util.ForgeDirection;

/**
 *
 * @author fuj1n
 *
 */
public class Helper {

    public static ForgeDirection convertMetaToForgeOrientation(int metadata) {
        switch (metadata) {
            case 6:
            case 5:
                return ForgeDirection.DOWN;
            case 7:
            case 0:
                return ForgeDirection.UP;
            case 1:
                return ForgeDirection.WEST;
            case 3:
                return ForgeDirection.NORTH;
            case 2:
                return ForgeDirection.EAST;
            case 4:
                return ForgeDirection.SOUTH;
        }

        return ForgeDirection.UNKNOWN;
    }
}
