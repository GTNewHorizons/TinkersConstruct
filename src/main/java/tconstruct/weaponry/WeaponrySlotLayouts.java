package tconstruct.weaponry;

import static tconstruct.library.client.ToolGuiElement.MIRROR;
import static tconstruct.library.client.ToolGuiElement.MIRROR_DIAGONAL;
import static tconstruct.library.client.ToolGuiElement.TURN_HALF;
import static tconstruct.library.client.ToolGuiElement.TURN_LEFT;
import static tconstruct.library.client.ToolGuiElement.TURN_RIGHT;
import static tconstruct.weaponry.TinkerWeaponry.arrowAmmo;
import static tconstruct.weaponry.TinkerWeaponry.boltAmmo;
import static tconstruct.weaponry.TinkerWeaponry.crossbow;
import static tconstruct.weaponry.TinkerWeaponry.javelin;
import static tconstruct.weaponry.TinkerWeaponry.longbow;
import static tconstruct.weaponry.TinkerWeaponry.shortbow;
import static tconstruct.weaponry.TinkerWeaponry.shuriken;
import static tconstruct.weaponry.TinkerWeaponry.throwingknife;

import tconstruct.library.tools.ToolCore;

/** Each weapon's build slots, slot 1 first: their x, their y and, for some, their turns. TiC2's where it has one. */
final class WeaponrySlotLayouts {

    private WeaponrySlotLayouts() {}

    static int[][] of(ToolCore tool) {
        if (tool == shortbow) return new int[][] { { 36, 38, 14 }, { 23, 47, 45 }, { 0, 0, MIRROR_DIAGONAL } };
        if (tool == arrowAmmo) return new int[][] { { 50, 32, 14 }, { 23, 41, 59 }, { MIRROR, 0 } };
        if (tool == throwingknife) return new int[][] { { 37, 15 }, { 34, 58 } };
        if (tool == javelin) return new int[][] { { 14, 32, 50 }, { 23, 41, 59 }, { 0, TURN_LEFT, TURN_LEFT } };
        if (tool == longbow)
            return new int[][] { { 44, 38, 10, 17 }, { 19, 47, 53, 26 }, { 0, 0, MIRROR_DIAGONAL, 0 } };
        if (tool == crossbow) return new int[][] { { 44, 38, 18, 14 }, { 19, 47, 51, 23 } };
        if (tool == boltAmmo) return new int[][] { { 40, 20 }, { 37, 53 } };
        if (tool == shuriken)
            return new int[][] { { 20, 44, 20, 44 }, { 29, 29, 53, 53 }, { TURN_LEFT, 0, TURN_HALF, TURN_RIGHT } };
        return null;
    }
}
