package tconstruct.tools.gui;

import net.minecraft.util.StatCollector;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** A rating in a tool's description ("Durability: High"); the language file lists the words that mean each one. */
@SideOnly(Side.CLIENT)
enum StatRating {

    LOW(0F, "gui.toolstation.rating.low"),
    AVERAGE(0.5F, "gui.toolstation.rating.average"),
    HIGH(1F, "gui.toolstation.rating.high");

    final int color;
    private final String wordsKey;

    /** {@code value} places the rating from 0, the worst, to 1, the best. */
    StatRating(float value, String wordsKey) {
        this.color = StatRows.valueToColor(value);
        this.wordsKey = wordsKey;
    }

    /** The rating a description's value opens with ("Low" in "Low, AoE"), or null when the value is no rating. */
    static StatRating of(String value) {
        String word = value.split(",", 2)[0].trim();
        for (StatRating rating : values()) {
            for (String name : StatCollector.translateToLocal(rating.wordsKey).split(",")) {
                if (name.trim().equalsIgnoreCase(word)) return rating;
            }
        }
        return null;
    }
}
