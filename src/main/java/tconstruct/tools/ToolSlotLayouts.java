package tconstruct.tools;

import static tconstruct.library.client.ToolGuiElement.TURN_HALF;
import static tconstruct.library.client.ToolGuiElement.TURN_LEFT;
import static tconstruct.library.client.ToolGuiElement.TURN_RIGHT;
import static tconstruct.tools.TinkerTools.battleaxe;
import static tconstruct.tools.TinkerTools.battlesign;
import static tconstruct.tools.TinkerTools.broadsword;
import static tconstruct.tools.TinkerTools.chisel;
import static tconstruct.tools.TinkerTools.cleaver;
import static tconstruct.tools.TinkerTools.dagger;
import static tconstruct.tools.TinkerTools.excavator;
import static tconstruct.tools.TinkerTools.frypan;
import static tconstruct.tools.TinkerTools.hammer;
import static tconstruct.tools.TinkerTools.hatchet;
import static tconstruct.tools.TinkerTools.longsword;
import static tconstruct.tools.TinkerTools.lumberaxe;
import static tconstruct.tools.TinkerTools.mattock;
import static tconstruct.tools.TinkerTools.pickaxe;
import static tconstruct.tools.TinkerTools.rapier;
import static tconstruct.tools.TinkerTools.scythe;
import static tconstruct.tools.TinkerTools.shovel;

import tconstruct.library.tools.ToolCore;

/** Each tool's build slots, slot 1 first: their x, their y and, for some, their turns. TiC2's where it has one. */
final class ToolSlotLayouts {

    private ToolSlotLayouts() {}

    static int[][] of(ToolCore tool) {
        if (tool == null) return null;
        if (tool == pickaxe) return new int[][] { { 53, 15, 33 }, { 22, 60, 42 } };
        if (tool == shovel) return new int[][] { { 51, 33 }, { 24, 42 } };
        if (tool == hatchet) return new int[][] { { 31, 22 }, { 22, 53 } };
        if (tool == mattock) return new int[][] { { 31, 22, 51 }, { 22, 53, 34 }, { 0, 0, TURN_RIGHT } };
        if (tool == broadsword) return new int[][] { { 48, 12, 30 }, { 26, 62, 44 } };
        if (tool == longsword) return new int[][] { { 48, 12, 30 }, { 26, 62, 44 } };
        if (tool == rapier) return new int[][] { { 18, 52, 32 }, { 26, 62, 44 }, { TURN_LEFT, TURN_LEFT, TURN_LEFT } };
        if (tool == dagger) return new int[][] { { 48, 12, 30 }, { 26, 62, 44 } };
        if (tool == frypan) return new int[][] { { 34, 12 }, { 36, 62 } };
        if (tool == battlesign) return new int[][] { { 30, 30 }, { 34, 60 } };
        if (tool == chisel) return new int[][] { { 43, 17 }, { 31, 57 } };
        if (tool == hammer) return new int[][] { { 44, 21, 25, 57 }, { 29, 52, 16, 48 } };
        if (tool == lumberaxe) return new int[][] { { 33, 32, 53, 13 }, { 22, 46, 38, 62 } };
        if (tool == excavator) return new int[][] { { 45, 25, 25, 7 }, { 26, 46, 26, 62 } };
        if (tool == scythe) return new int[][] { { 36, 17, 56, 37 }, { 19, 54, 29, 47 } };
        if (tool == cleaver) return new int[][] { { 25, 9, 47, 33 }, { 36, 64, 30, 58 } };
        if (tool == battleaxe) return new int[][] { { 49, 19, 23, 42 }, { 48, 52, 22, 29 }, { TURN_HALF } };
        return null;
    }
}
