package tconstruct.smeltery;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StatCollector;

import tconstruct.library.tools.AbilityHelper;

public class SmelteryDamageSource extends DamageSource {

    public SmelteryDamageSource() {
        super("smeltery");
    }

    @Override
    public IChatComponent func_151519_b(EntityLivingBase par1EntityLiving) {
        String type = switch (AbilityHelper.random.nextInt(4)) {
            case 0 -> "one.";
            case 1 -> "two.";
            case 2 -> "three.";
            case 3 -> "four.";
            default -> "";
        };
        EntityLivingBase entityliving1 = par1EntityLiving.func_94060_bK();
        String s = "death." + type + this.damageType;
        String s1 = s + ".player";
        return entityliving1 != null && StatCollector.canTranslate(s1)
                ? new ChatComponentTranslation(s1, par1EntityLiving.func_145748_c_(), entityliving1.func_145748_c_())
                : new ChatComponentTranslation(s, par1EntityLiving.func_145748_c_());
    }
}
