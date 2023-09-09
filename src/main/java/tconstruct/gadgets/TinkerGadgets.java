package tconstruct.gadgets;

import java.util.Locale;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.ShapedOreRecipe;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import mantle.pulsar.pulse.Handler;
import mantle.pulsar.pulse.Pulse;
import tconstruct.TConstruct;
import tconstruct.gadgets.item.ItemSlimeBoots;
import tconstruct.gadgets.item.ItemSlimeSling;
import tconstruct.library.TConstructRegistry;
import tconstruct.world.TinkerWorld;

@Pulse(id = "Tinkers' Gadgets", description = "All the fun toys.", forced = true)
public class TinkerGadgets {

    public static final String PulseId = "TinkerGadgets";
    public static final Logger log = LogManager.getLogger(PulseId);

    // Gadgets
    public static ItemSlimeSling slimeSling;
    public static ItemSlimeBoots slimeBoots;

    @Handler
    public void preInit(FMLPreInitializationEvent event) {
        slimeSling = registerItem(new ItemSlimeSling(), "slimesling");
        slimeBoots = registerItem(new ItemSlimeBoots(), "slime_boots");
    }

    @Handler
    public void init(FMLInitializationEvent event) {
        if (!Loader.isModLoaded("dreamcraft")) {
            ItemStack slimeBlockGreen = new ItemStack(TinkerWorld.slimeGel, 1, 1);

            GameRegistry.addShapedRecipe(
                    new ItemStack(slimeBoots),
                    "   ",
                    "s s",
                    "b b",
                    's',
                    Items.slime_ball,
                    'b',
                    slimeBlockGreen);
            GameRegistry.addRecipe(
                    new ShapedOreRecipe(
                            new ItemStack(slimeSling),
                            "fbf",
                            "s s",
                            " s ",
                            'f',
                            Items.string,
                            's',
                            Items.slime_ball,
                            'b',
                            slimeBlockGreen));
        }
        TConstructRegistry.gadgetsTab.init(new ItemStack(slimeBoots));
    }

    @Handler
    public void postInit(FMLPostInitializationEvent evt) {}

    public static String resource(String res) {
        return String.format("%s:%s", "tinker", res);
    }

    public static ResourceLocation getResource(String res) {
        return new ResourceLocation("tinker", res);
    }

    public static String prefix(String name) {
        return String.format("%s.%s", TConstruct.modID, name.toLowerCase(Locale.US));
    }

    protected static <T extends Item> T registerItem(T item, String name) {
        if (!name.equals(name.toLowerCase(Locale.US))) {
            throw new IllegalArgumentException(
                    String.format("Unlocalized names need to be all lowercase! Item: %s", name));
        }
        item.setUnlocalizedName(prefix(name));
        GameRegistry.registerItem(item, name);
        return item;
    }
}
