package tconstruct.smeltery.items;

import java.util.List;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.*;
import tconstruct.tools.items.Pattern;

public class ClayPattern extends Pattern {

    public ClayPattern(String patternType, String folder) {
        super(patternName, getPatternNames(patternType), folder);
    }

    protected static String[] getPatternNames(String partType) {
        String[] names = new String[patternName.length];
        for (int i = 0; i < patternName.length; i++)
            if (!(patternName[i].equals(""))) names[i] = partType + patternName[i];
            else names[i] = "";
        return names;
    }

    private static final String[] patternName = new String[] {
        "ingot",
        "rod",
        "pickaxe",
        "shovel",
        "axe",
        "swordblade",
        "largeguard",
        "mediumguard",
        "crossbar",
        "binding",
        "frypan",
        "sign",
        "knifeblade",
        "chisel",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "fullguard",
        "",
        "",
        "arrowhead",
        "gem",
        "nugget"
    };

    @Override
    public void getSubItems(Item p_150895_1_, CreativeTabs p_150895_2_, List p_150895_3_) {
        for (int i = 0; i < patternName.length; i++)
            if (!(patternName[i].equals(""))) p_150895_3_.add(new ItemStack(p_150895_1_, 1, i));
    }
}
