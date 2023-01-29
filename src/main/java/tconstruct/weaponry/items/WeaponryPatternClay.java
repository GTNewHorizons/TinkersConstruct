package tconstruct.weaponry.items;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import tconstruct.tools.items.Pattern;
import tconstruct.util.Reference;

public class WeaponryPatternClay extends Pattern {

    private static final String[] patternName = new String[] { "", "", "", "bowlimb" };

    public WeaponryPatternClay(String patternType, String name) {
        super(patternName, getPatternNames(patternName, patternType), "patterns/");

        this.setUnlocalizedName(Reference.prefix(name));
    }

    public static String[] getPatternNames(String[] patternName, String partType) {
        String[] names = new String[patternName.length];
        for (int i = 0; i < patternName.length; i++)
            if (!(patternName[i].equals(""))) names[i] = partType + patternName[i];
            else names[i] = "";
        return names;
    }

    @Override
    public void getSubItems(Item b, CreativeTabs tab, List list) {
        for (int i = 0; i < patternName.length; i++) {
            // if (i != 23)
            if (!(patternName[i].equals(""))) list.add(new ItemStack(b, 1, i));
        }
    }

    @Override
    public int getPatternCost(ItemStack pattern) {
        switch (pattern.getItemDamage()) {
            case 0:
                return 1; // shuriken
            case 1:
                return 8; // crossbow limb
            case 2:
                return 10; // crossbow body
            case 3:
                return 3; // bowlimb
        }
        return 0;
    }
}
