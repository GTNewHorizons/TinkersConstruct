package tconstruct.library.util;

import net.minecraft.util.StatCollector;

public final class LocalizedColors {

    private static final String KEY_PREFIX = "tconstruct.nei.color.";

    public static int MATERIAL_NAME = 0x404040;
    public static int MATERIAL_DURABILITY = 0x404040;
    public static int MATERIAL_HANDLE_MODIFIER1 = 0x404040;
    public static int MATERIAL_HANDLE_MODIFIER2 = 0x404040;
    public static int MATERIAL_MINING_SPEED = 0x404040;
    public static int MATERIAL_HARVEST_LEVEL = 0x404040;
    public static int MATERIAL_ATTACK1 = 0x404040;
    public static int MATERIAL_ATTACK2 = 0x404040;
    public static int MATERIAL_REINFORCED = 0x404040;
    public static int MATERIAL_STONEBOUND = 0x404040;
    public static int MATERIAL_ABILITY = 0x404040;
    public static int MATERIAL_BOW = 0x404040;
    public static int MATERIAL_BOW_DRAW_SPEED = 0x404040;
    public static int MATERIAL_BOW_FLIGHT_SPEED_MAX = 0x404040;
    public static int MATERIAL_ARROW_MASS = 0x404040;
    public static int MATERIAL_ARROW_BREAK_CHANCE = 0x404040;

    private LocalizedColors() {}

    public static void reload() {
        MATERIAL_NAME = get("materialName", MATERIAL_NAME);
        MATERIAL_DURABILITY = get("materialDurability", MATERIAL_DURABILITY);
        MATERIAL_HANDLE_MODIFIER1 = get("materialHandleModifier1", MATERIAL_HANDLE_MODIFIER1);
        MATERIAL_HANDLE_MODIFIER2 = get("materialHandleModifier2", MATERIAL_HANDLE_MODIFIER2);
        MATERIAL_MINING_SPEED = get("materialMiningSpeed", MATERIAL_MINING_SPEED);
        MATERIAL_HARVEST_LEVEL = get("materialHarvestLevel", MATERIAL_HARVEST_LEVEL);
        MATERIAL_ATTACK1 = get("materialAttack1", MATERIAL_ATTACK1);
        MATERIAL_ATTACK2 = get("materialAttack2", MATERIAL_ATTACK2);
        MATERIAL_REINFORCED = get("materialReinforced", MATERIAL_REINFORCED);
        MATERIAL_STONEBOUND = get("materialStonebound", MATERIAL_STONEBOUND);
        MATERIAL_ABILITY = get("materialAbility", MATERIAL_ABILITY);
        MATERIAL_BOW = get("materialBow", MATERIAL_BOW);
        MATERIAL_BOW_DRAW_SPEED = get("materialBowDrawSpeed", MATERIAL_BOW_DRAW_SPEED);
        MATERIAL_BOW_FLIGHT_SPEED_MAX = get("materialBowFlightSpeedMax", MATERIAL_BOW_FLIGHT_SPEED_MAX);
        MATERIAL_ARROW_MASS = get("materialArrowMass", MATERIAL_ARROW_MASS);
        MATERIAL_ARROW_BREAK_CHANCE = get("materialArrowBreakChance", MATERIAL_ARROW_BREAK_CHANCE);
    }

    private static int get(String key, int fallback) {
        String full = KEY_PREFIX + key;

        if (!StatCollector.canTranslate(full)) return fallback;

        String raw = StatCollector.translateToLocal(full).trim();

        if (raw.startsWith("0x") || raw.startsWith("0X")) raw = raw.substring(2);
        else if (raw.startsWith("#")) raw = raw.substring(1);

        try {
            return (int) Long.parseLong(raw, 16);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}
