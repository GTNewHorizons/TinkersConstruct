package tconstruct.tools.gui;

import static com.gtnewhorizon.gtnhlib.util.numberformatting.NumberFormatUtil.formatNumber;
import static tconstruct.tools.gui.StatRows.COLOR_ATTACK;
import static tconstruct.tools.gui.StatRows.COLOR_DRAWSPEED;
import static tconstruct.tools.gui.StatRows.COLOR_SPEED;
import static tconstruct.tools.gui.StatRows.RED;
import static tconstruct.tools.gui.StatRows.fractionColor;
import static tconstruct.tools.gui.StatRows.write;
import static tconstruct.tools.gui.StatRows.writeFraction;
import static tconstruct.util.McTextFormatter.addWhite;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;

import tconstruct.library.accessory.AccessoryCore;
import tconstruct.library.armor.ArmorCore;
import tconstruct.library.modifier.IModifyable;
import tconstruct.library.tools.AbilityHelper;
import tconstruct.library.tools.ToolCore;
import tconstruct.library.util.HarvestLevels;
import tconstruct.library.weaponry.AmmoWeapon;
import tconstruct.library.weaponry.IAmmo;
import tconstruct.library.weaponry.ProjectileWeapon;

/** The stats a readout lists for an item, one writer per kind of stat. */
final class StatReadout {

    private StatReadout() {}

    static void drawStatsBody(ItemStack stack, Item item, NBTTagCompound tags) {
        Collection<String> categories = new LinkedList<>();
        if (item instanceof IModifyable modifyable) {
            categories = Arrays.asList(modifyable.getTraits());
        }

        // does it have ammo instead of durability?
        if (item instanceof IAmmo) drawAmmo((IAmmo) item, stack);
        // regular durability?
        else if (item instanceof ToolCore || item instanceof ArmorCore) drawDurability(tags);

        // tools
        if (item instanceof ToolCore tool) {
            // DualHarvest tool?
            if (categories.contains("dualharvest")) drawDualHarvestStats(tool, tags);
            // or regular Harvest tool?
            else if (categories.contains("harvest")) drawHarvestStats(tool, tags);
            // weapon?
            if (categories.contains("weapon")) drawWeaponStats(tool, tags);
            // throwing weapon?
            if (categories.contains("thrown") && tool instanceof AmmoWeapon)
                drawThrowingWeaponStats((AmmoWeapon) tool, tags);
            // projectile weapon?
            if (categories.contains("bow") && tool instanceof ProjectileWeapon)
                drawProjectileWeaponStats((ProjectileWeapon) tool, tags, stack);
            // projectile?
            if (categories.contains("projectile")) drawProjectileStats(tags);
        }
        // armor
        if (item instanceof ArmorCore armor) {
            drawArmorStats(armor, tags, stack);
        }
        // Accessory
        if (item instanceof AccessoryCore accessory) {
            drawAccessoryStats(accessory, tags);
        }
    }

    private static void drawDurability(NBTTagCompound tags) {
        final int durability = tags.getInteger("Damage");
        final int maxDur = tags.getInteger("TotalDurability");
        final int availableDurability = maxDur - durability;
        final int color = fractionColor(availableDurability, maxDur);

        // big durabilities have to split to 2 lines
        if (maxDur >= 10000) {
            write(StatCollector.translateToLocal("gui.toolstation1"), color);
            writeFraction(addWhite("- "), availableDurability, maxDur, color);
        } else {
            writeFraction(StatCollector.translateToLocal("gui.toolstation2"), availableDurability, maxDur, color);
        }
    }

    private static void drawAmmo(IAmmo ammoItem, ItemStack stack) {
        final int max = ammoItem.getMaxAmmo(stack);
        final int current = ammoItem.getAmmoCount(stack);

        writeFraction(StatCollector.translateToLocal("gui.toolstation21"), current, max, fractionColor(current, max));
    }

    private static void drawHarvestStats(ToolCore tool, NBTTagCompound tags) {
        float mineSpeed = AbilityHelper.calcToolSpeed(tool, tags);
        float stoneboundSpeed = AbilityHelper.calcStoneboundBonus(tool, tags);

        write(StatCollector.translateToLocal("gui.toolstation14") + formatNumber(mineSpeed), COLOR_SPEED);
        if (stoneboundSpeed != 0) {
            String bloss = stoneboundSpeed > 0 ? StatCollector.translateToLocal("gui.toolstation4")
                    : StatCollector.translateToLocal("gui.toolstation5");
            write(bloss + formatNumber(stoneboundSpeed), RED);
        }
        write(
                StatCollector.translateToLocal("gui.toolstation15")
                        + HarvestLevels.getHarvestLevelName(tags.getInteger("HarvestLevel")));
    }

    private static void drawDualHarvestStats(ToolCore tool, NBTTagCompound tags) {
        float mineSpeed = AbilityHelper.calcDualToolSpeed(tool, tags, false);
        float mineSpeed2 = AbilityHelper.calcDualToolSpeed(tool, tags, true);
        float stoneboundSpeed = AbilityHelper.calcStoneboundBonus(tool, tags);

        write(StatCollector.translateToLocal("gui.toolstation12"), COLOR_SPEED);
        write(addWhite("- ") + formatNumber(mineSpeed) + ", " + formatNumber(mineSpeed2), COLOR_SPEED);
        if (stoneboundSpeed != 0) {
            String bloss = stoneboundSpeed > 0 ? StatCollector.translateToLocal("gui.toolstation4")
                    : StatCollector.translateToLocal("gui.toolstation5");
            write(bloss + formatNumber(stoneboundSpeed), RED);
        }

        write(StatCollector.translateToLocal("gui.toolstation13"));
        write(
                "- " + HarvestLevels.getHarvestLevelName(tags.getInteger("HarvestLevel"))
                        + ", "
                        + HarvestLevels.getHarvestLevelName(tags.getInteger("HarvestLevel2")));
    }

    private static void drawWeaponStats(ToolCore tool, NBTTagCompound tags) {
        // DAMAGE
        int attack = (tags.getInteger("Attack"));

        // factor in Stonebound
        float stoneboundDamage = -AbilityHelper.calcStoneboundBonus(tool, tags);
        attack += stoneboundDamage;
        attack *= tool.getDamageModifier();

        if (attack < 1) attack = 1;

        String heart = attack == 2 ? StatCollector.translateToLocal("gui.partcrafter8")
                : StatCollector.translateToLocal("gui.partcrafter9");
        if (attack % 2 == 0)
            write(StatCollector.translateToLocal("gui.toolstation3") + formatNumber(attack / 2) + heart, COLOR_ATTACK);
        else write(
                StatCollector.translateToLocal("gui.toolstation3") + formatNumber(attack / 2f) + heart,
                COLOR_ATTACK);

        if (stoneboundDamage != 0) {
            heart = stoneboundDamage == 2 ? StatCollector.translateToLocal("gui.partcrafter8")
                    : StatCollector.translateToLocal("gui.partcrafter9");
            String bloss = stoneboundDamage > 0 ? StatCollector.translateToLocal("gui.toolstation4")
                    : StatCollector.translateToLocal("gui.toolstation5");
            write(bloss + formatNumber(stoneboundDamage / 2f) + heart, RED);
        }
    }

    private static void drawThrowingWeaponStats(AmmoWeapon weapon, NBTTagCompound tags) {
        float attackf = (tags.getInteger("Attack"));
        attackf *= weapon.getDamageModifier();
        attackf *= weapon.getProjectileSpeed();

        if (attackf < 1) attackf = 1;

        int attack = (int) attackf;

        String heart = attack == 2 ? StatCollector.translateToLocal("gui.partcrafter8")
                : StatCollector.translateToLocal("gui.partcrafter9");
        if (attack % 2 == 0)
            write(StatCollector.translateToLocal("gui.toolstation23") + formatNumber(attack / 2) + heart, COLOR_ATTACK);
        else write(
                StatCollector.translateToLocal("gui.toolstation23") + formatNumber(attack / 2f) + heart,
                COLOR_ATTACK);
    }

    private static void drawProjectileWeaponStats(ProjectileWeapon weapon, NBTTagCompound tags, ItemStack stack) {
        // drawspeed
        final int drawSpeed = weapon.getWindupTime(stack);
        final float trueDraw = drawSpeed / 20f;
        write(StatCollector.translateToLocal("gui.toolstation6") + formatNumber(trueDraw) + "s", COLOR_DRAWSPEED);

        // flightspeed
        final float flightSpeed = weapon.getProjectileSpeed(stack);
        write(StatCollector.translateToLocal("gui.toolstation7") + formatNumber(flightSpeed) + "x");
    }

    private static void drawProjectileStats(NBTTagCompound tags) {
        // weight
        final float weight = tags.getFloat("Mass");
        write(StatCollector.translateToLocal("gui.toolstation8") + formatNumber(weight));

        // accuracy
        final float accuracy = tags.getFloat("Accuracy");
        write(StatCollector.translateToLocal("gui.toolstation9") + formatNumber(accuracy) + "%");

        // breakchance
        final float breakChance = tags.getFloat("BreakChance") * 100;
        write(StatCollector.translateToLocal("gui.toolstation22") + formatNumber(breakChance) + "%");
    }

    private static void drawArmorStats(ArmorCore armor, NBTTagCompound tags, ItemStack stack) {
        // Damage reduction
        double damageReduction = tags.getDouble("DamageReduction");
        if (damageReduction > 0)
            write(StatCollector.translateToLocal("gui.toolstation19") + formatNumber(damageReduction));

        // Protection
        double protection = armor.getProtection(stack);
        double maxProtection = tags.getDouble("MaxDefense");

        write(
                StatCollector.translateToLocal("gui.toolstation20") + formatNumber(protection)
                        + " / "
                        + formatNumber(maxProtection));
    }

    private static void drawAccessoryStats(AccessoryCore core, NBTTagCompound tags) {
        if (tags.hasKey("MiningSpeed")) {
            float mineSpeed = tags.getInteger("MiningSpeed");
            float trueSpeed = mineSpeed / (100f);
            write(StatCollector.translateToLocal("gui.toolstation16") + formatNumber(trueSpeed));
        }
    }
}
