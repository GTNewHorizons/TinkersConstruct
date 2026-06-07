package tconstruct.library.util;

import com.gtnewhorizon.gtnhlib.color.ColorResource;

public class ColorUtils {

    private static final ColorResource.Factory color = new ColorResource.Factory("tinker");

    public static final ColorResource
    // spotless:off
        materialName                = color.rgb("materialName",                 "0x404040"),
        materialDurability          = color.rgb("materialDurability",           "0x404040"),
        materialHandleModifier1     = color.rgb("materialHandleModifier1",      "0x404040"),
        materialHandleModifier2     = color.rgb("materialHandleModifier2",      "0x404040"),
        materialMiningSpeed         = color.rgb("materialMiningSpeed",          "0x404040"),
        materialHarvestLevel        = color.rgb("materialHarvestLevel",         "0x404040"),
        materialAttack1             = color.rgb("materialAttack1",              "0x404040"),
        materialAttack2             = color.rgb("materialAttack2",              "0x404040"),
        materialReinforced          = color.rgb("materialReinforced",           "0x404040"),
        materialStonebound          = color.rgb("materialStonebound",           "0x404040"),
        materialAbility             = color.rgb("materialAbility",              "0x404040"),
        materialBow                 = color.rgb("materialBow",                  "0x404040"),
        materialBowDrawSpeed        = color.rgb("materialBowDrawSpeed",         "0x404040"),
        materialBowFlightSpeedMax   = color.rgb("materialBowFlightSpeedMax",    "0x404040"),
        materialArrowMass           = color.rgb("materialArrowMass",            "0x404040"),
        materialArrowBreakChance    = color.rgb("materialArrowBreakChance",     "0x404040");
    // spotless:on
}
