package tconstruct.tools.gui;

import static com.gtnewhorizon.gtnhlib.util.numberformatting.NumberFormatUtil.formatNumber;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
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

public final class ToolStationGuiHelper {

    // non-instantiable
    private ToolStationGuiHelper() {}

    private static final FontRenderer fontRendererObj = Minecraft.getMinecraft().fontRenderer;
    private static int xPos, yPos;
    private static int clampY = Integer.MAX_VALUE;
    /** Widest line the current panel can show; a longer one wraps rather than leave the frame. */
    private static int clampWidth = Integer.MAX_VALUE;
    /** Where a title is centred, relative to xPos. */
    private static int titleCenter = 55;

    /**
     * Width of the station GUI's two side panels. Sized for the longest stock lines that a 1366x768 screen at GUI scale
     * 3 can still hold: "- Bane of Arthropods (4/4)" (143 px) and a nine-digit ammo line before grouping (144 px), plus
     * the panel's 8 px left and 3 px right text insets. With grouped numbers and " / " a durability or ammo line of two
     * nine- or ten-digit figures ("- 2,147,483,647 / 2,147,483,647", 156 px) is wider than the 145 px of text the panel
     * holds and wraps after the " / " (the 167 px panel that would hold it no longer fits that screen); a two-digit
     * Bane tier and an unusually long translation wrap too, see flush().
     */
    public static final int PANEL_WIDTH = 156;

    /** Rows of the panel being laid out; {@link #flush()} draws them once the whole panel is known. */
    private static final List<String> pending = new ArrayList<>();

    /** A blank row. */
    private static void newline() {
        pending.add("");
    }

    private static void write(String s) {
        pending.add(s);
    }

    /**
     * Draws the queued rows. A row wider than the panel is wrapped (at a space when it has one) rather than run past
     * the frame, but only when the wrapped panel still fits its row budget: wrapping must never hide a row that used to
     * show. It fires for a durability or ammo line of two nine- or ten-digit grouped figures, which breaks at the space
     * after the slash, and for an unusually long translation.
     */
    private static void flush() {
        List<String> rows = pending;
        if (clampWidth != Integer.MAX_VALUE) {
            List<String> wrapped = new ArrayList<>();
            for (String s : pending) {
                if (fontRendererObj.getStringWidth(s) > clampWidth) {
                    for (Object part : fontRendererObj.listFormattedStringToWidth(s, clampWidth)) {
                        wrapped.add((String) part);
                    }
                } else {
                    wrapped.add(s);
                }
            }
            int last = wrapped.size();
            while (last > 0 && wrapped.get(last - 1).isEmpty()) last--;
            if (wrapped.size() == pending.size() || yPos + 10 * (last - 1) <= clampY) rows = wrapped;
        }
        for (String row : rows) {
            if (yPos <= clampY && !row.isEmpty()) {
                fontRendererObj.drawString(row, xPos, yPos, 0xffffffff);
            }
            yPos += 10;
        }
        pending.clear();
    }

    public static void drawToolStats(ItemStack stack, int x, int y) {
        Item item = stack.getItem();
        NBTTagCompound tags = resolveTags(stack);

        clampY = Integer.MAX_VALUE;
        clampWidth = Integer.MAX_VALUE;
        titleCenter = 55;
        pending.clear();
        xPos = x;
        yPos = y + 8;

        drawTitle(stack);
        drawStatsBody(stack, item, tags);
        newline();
        drawModifiers(tags);
        flush();
    }

    /** Two-panel variant: stats clamped to the first region, modifiers written into the second. */
    public static void drawToolStatsSplit(ItemStack stack, int x, int y, int limitY, int x2, int y2, int limitY2) {
        Item item = stack.getItem();
        NBTTagCompound tags = resolveTags(stack);

        clampY = limitY;
        clampWidth = PANEL_WIDTH - 11;
        titleCenter = PANEL_WIDTH / 2 - 8;
        pending.clear();
        xPos = x;
        yPos = y + 8;

        drawTitle(stack);
        drawStatsBody(stack, item, tags);
        flush();

        clampY = limitY2;
        xPos = x2;
        yPos = y2 + 8;
        drawModifiers(tags);
        flush();
        clampY = Integer.MAX_VALUE;
        clampWidth = Integer.MAX_VALUE;
        titleCenter = 55;
    }

    private static NBTTagCompound resolveTags(ItemStack stack) {
        NBTTagCompound tags = stack.getTagCompound();
        if (stack.getItem() instanceof IModifyable modifyable) {
            tags = tags.getCompoundTag(modifyable.getBaseTagName());
        }
        return tags;
    }

    private static void drawTitle(ItemStack stack) {
        String name = stack.getItem() instanceof ToolCore ? ((ToolCore) stack.getItem()).getLocalizedToolName()
                : stack.getDisplayName();
        drawCenteredString(fontRendererObj, "\u00A7n" + name, xPos + titleCenter, yPos, 0xffffffff);
        newline();
        newline();
    }

    private static void drawStatsBody(ItemStack stack, Item item, NBTTagCompound tags) {
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

        // big durabilities have to split to 2 lines
        if (maxDur >= 10000) {
            write(StatCollector.translateToLocal("gui.toolstation1"));
            write("- " + formatNumber(availableDurability) + " / " + formatNumber(maxDur));
        } else {
            write(
                    StatCollector.translateToLocal("gui.toolstation2") + formatNumber(availableDurability)
                            + " / "
                            + formatNumber(maxDur));
        }
    }

    private static void drawAmmo(IAmmo ammoItem, ItemStack stack) {
        final int max = ammoItem.getMaxAmmo(stack);
        final int current = ammoItem.getAmmoCount(stack);

        write(StatCollector.translateToLocal("gui.toolstation21") + formatNumber(current) + " / " + formatNumber(max));
    }

    private static void drawModifiers(NBTTagCompound tags) {
        int modifiers = tags.getInteger("Modifiers");
        // remaining modifiers
        if (modifiers != 0) write(StatCollector.translateToLocal("gui.toolstation18") + formatNumber(modifiers));

        // Modifier-header (if we have modifiers)
        if (tags.hasKey("ModifierTip1")) {
            write(StatCollector.translateToLocal("gui.toolstation17"));

            String tooltip = "ModifierTip";
            int tipNum = 1;
            while (tags.hasKey(tooltip + tipNum)) {
                String tipName = tags.getString(tooltip + tipNum);
                String locString = "modifier.toolstation." + tipName;
                // strip out the '(X of Y)' in some for the localization strings.. sigh
                int bracket = tipName.indexOf("(");
                if (bracket > 0) locString = "modifier.toolstation." + tipName.substring(0, bracket);
                locString = EnumChatFormatting.getTextWithoutFormattingCodes(locString.replace(" ", ""));

                if (StatCollector.canTranslate(locString)) {
                    tipName = tipName.replace(
                            EnumChatFormatting.getTextWithoutFormattingCodes(tipName),
                            StatCollector.translateToLocal(locString));
                    // re-add the X/Y
                    if (bracket > 0) tipName += " " + tags.getString(tooltip + tipNum).substring(bracket);
                }
                write("- " + tipName);
                tipNum++;
            }
        }
    }

    private static void drawHarvestStats(ToolCore tool, NBTTagCompound tags) {
        float mineSpeed = AbilityHelper.calcToolSpeed(tool, tags);
        float stoneboundSpeed = AbilityHelper.calcStoneboundBonus(tool, tags);

        write(StatCollector.translateToLocal("gui.toolstation14") + formatNumber(mineSpeed));
        if (stoneboundSpeed != 0) {
            String bloss = stoneboundSpeed > 0 ? StatCollector.translateToLocal("gui.toolstation4")
                    : StatCollector.translateToLocal("gui.toolstation5");
            write(bloss + formatNumber(stoneboundSpeed));
        }
        write(
                StatCollector.translateToLocal("gui.toolstation15")
                        + HarvestLevels.getHarvestLevelName(tags.getInteger("HarvestLevel")));
    }

    private static void drawDualHarvestStats(ToolCore tool, NBTTagCompound tags) {
        float mineSpeed = AbilityHelper.calcDualToolSpeed(tool, tags, false);
        float mineSpeed2 = AbilityHelper.calcDualToolSpeed(tool, tags, true);
        float stoneboundSpeed = AbilityHelper.calcStoneboundBonus(tool, tags);

        write(StatCollector.translateToLocal("gui.toolstation12"));
        write("- " + formatNumber(mineSpeed) + ", " + formatNumber(mineSpeed2));
        if (stoneboundSpeed != 0) {
            String bloss = stoneboundSpeed > 0 ? StatCollector.translateToLocal("gui.toolstation4")
                    : StatCollector.translateToLocal("gui.toolstation5");
            write(bloss + formatNumber(stoneboundSpeed));
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
            write(StatCollector.translateToLocal("gui.toolstation3") + formatNumber(attack / 2) + heart);
        else write(StatCollector.translateToLocal("gui.toolstation3") + formatNumber(attack / 2f) + heart);

        if (stoneboundDamage != 0) {
            heart = stoneboundDamage == 2 ? StatCollector.translateToLocal("gui.partcrafter8")
                    : StatCollector.translateToLocal("gui.partcrafter9");
            String bloss = stoneboundDamage > 0 ? StatCollector.translateToLocal("gui.toolstation4")
                    : StatCollector.translateToLocal("gui.toolstation5");
            write(bloss + formatNumber(stoneboundDamage / 2f) + heart);
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
            write(StatCollector.translateToLocal("gui.toolstation23") + formatNumber(attack / 2) + heart);
        else write(StatCollector.translateToLocal("gui.toolstation23") + formatNumber(attack / 2f) + heart);
    }

    private static void drawProjectileWeaponStats(ProjectileWeapon weapon, NBTTagCompound tags, ItemStack stack) {
        // drawspeed
        final int drawSpeed = weapon.getWindupTime(stack);
        final float trueDraw = drawSpeed / 20f;
        write(StatCollector.translateToLocal("gui.toolstation6") + formatNumber(trueDraw) + "s");

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

    /**
     * Renders the specified text to the screen, center-aligned. Copied out of GUI
     */
    public static void drawCenteredString(FontRenderer fontRendererIn, String text, int x, int y, int color) {
        fontRendererIn.drawStringWithShadow(text, x - fontRendererIn.getStringWidth(text) / 2, y, color);
    }
}
