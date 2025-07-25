package tconstruct.armor;

import java.util.EnumSet;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.oredict.ShapedOreRecipe;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.ObjectHolder;
import mantle.pulsar.pulse.Handler;
import mantle.pulsar.pulse.Pulse;
import tconstruct.TConstruct;
import tconstruct.armor.blocks.DryingRack;
import tconstruct.armor.items.ArmorBasic;
import tconstruct.armor.items.DiamondApple;
import tconstruct.armor.items.HeartCanister;
import tconstruct.armor.items.Jerky;
import tconstruct.armor.items.Knapsack;
import tconstruct.armor.items.TravelBelt;
import tconstruct.armor.items.TravelGear;
import tconstruct.armor.items.TravelGlove;
import tconstruct.armor.items.TravelWings;
import tconstruct.blocks.logic.DryingRackLogic;
import tconstruct.library.TConstructRegistry;
import tconstruct.library.accessory.AccessoryCore;
import tconstruct.library.armor.ArmorPart;
import tconstruct.library.crafting.DryingRackRecipes;
import tconstruct.library.crafting.LiquidCasting;
import tconstruct.library.crafting.ModifyBuilder;
import tconstruct.modifiers.accessory.GloveSpeed;
import tconstruct.modifiers.armor.AModBoolean;
import tconstruct.modifiers.armor.AModInteger;
import tconstruct.modifiers.armor.AModLeadBoots;
import tconstruct.modifiers.armor.ActiveTinkerArmor;
import tconstruct.modifiers.armor.TravelModDoubleJump;
import tconstruct.modifiers.armor.TravelModRepair;
import tconstruct.modifiers.tools.ModAttack;
import tconstruct.tools.TinkerTools;
import tconstruct.util.config.PHConstruct;
import tconstruct.world.TinkerWorld;

@ObjectHolder(TConstruct.modID)
@Pulse(id = "Tinkers' Armory", description = "Modifyable armors, such as the traveller's gear.")
public class TinkerArmor {

    @SidedProxy(clientSide = "tconstruct.armor.ArmorProxyClient", serverSide = "tconstruct.armor.ArmorProxyCommon")
    public static ArmorProxyCommon proxy;

    public static Item diamondApple;
    public static Item jerky;
    public static Block dryingRack;
    // Wearables
    /*
     * public static Item heavyHelmet; public static Item heavyChestplate; public static Item heavyPants; public static
     * Item heavyBoots; public static Item glove;
     */
    public static Item knapsack;
    public static Item heartCanister;
    // Armor - basic
    public static Item helmetWood;
    public static Item chestplateWood;
    public static Item leggingsWood;
    public static Item bootsWood;
    public static ArmorMaterial materialWood;

    // Clothing - Travel Gear
    public static TravelGear travelGoggles;
    public static TravelGear travelWings;
    public static TravelGear travelVest;
    public static TravelGear travelBoots;
    public static AccessoryCore travelGlove;
    public static AccessoryCore travelBelt;

    public static ModAttack modAttackGlove;

    @Handler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit();

        new TinkerArmorEvents().registerEvents();
        FMLCommonHandler.instance().bus().register(new ArmorAbilities());

        TinkerArmor.dryingRack = new DryingRack().setBlockName("Armor.DryingRack");
        GameRegistry.registerBlock(TinkerArmor.dryingRack, "Armor.DryingRack");
        GameRegistry.registerTileEntity(DryingRackLogic.class, "Armor.DryingRack");
        TinkerArmor.diamondApple = new DiamondApple().setUnlocalizedName("tconstruct.apple.diamond");
        GameRegistry.registerItem(TinkerArmor.diamondApple, "diamondApple");
        boolean foodOverhaul = Loader.isModLoaded("HungerOverhaul") || Loader.isModLoaded("fc_food");

        TinkerArmor.jerky = new Jerky(foodOverhaul).setUnlocalizedName("tconstruct.jerky");
        GameRegistry.registerItem(TinkerArmor.jerky, "jerky");

        // Wearables
        TinkerArmor.heartCanister = new HeartCanister().setUnlocalizedName("tconstruct.canister");
        TinkerArmor.knapsack = new Knapsack().setUnlocalizedName("tconstruct.storage");
        GameRegistry.registerItem(TinkerArmor.heartCanister, "heartCanister");
        GameRegistry.registerItem(TinkerArmor.knapsack, "knapsack");

        LiquidCasting basinCasting = TConstruct.getBasinCasting();
        TinkerArmor.materialWood = EnumHelper.addArmorMaterial("WOOD", 2, new int[] { 1, 2, 2, 1 }, 3);
        TinkerArmor.helmetWood = new ArmorBasic(TinkerArmor.materialWood, 0, "wood")
                .setUnlocalizedName("tconstruct.helmetWood");
        TinkerArmor.chestplateWood = new ArmorBasic(TinkerArmor.materialWood, 1, "wood")
                .setUnlocalizedName("tconstruct.chestplateWood");
        TinkerArmor.leggingsWood = new ArmorBasic(TinkerArmor.materialWood, 2, "wood")
                .setUnlocalizedName("tconstruct.leggingsWood");
        TinkerArmor.bootsWood = new ArmorBasic(TinkerArmor.materialWood, 3, "wood")
                .setUnlocalizedName("tconstruct.bootsWood");
        GameRegistry.registerItem(TinkerArmor.helmetWood, "helmetWood");
        GameRegistry.registerItem(TinkerArmor.chestplateWood, "chestplateWood");
        GameRegistry.registerItem(TinkerArmor.leggingsWood, "leggingsWood");
        GameRegistry.registerItem(TinkerArmor.bootsWood, "bootsWood");
        TConstructRegistry.addItemStackToDirectory("diamondApple", new ItemStack(TinkerArmor.diamondApple, 1, 0));

        TConstructRegistry.addItemStackToDirectory("canisterEmpty", new ItemStack(TinkerArmor.heartCanister, 1, 0));
        TConstructRegistry.addItemStackToDirectory("miniRedHeart", new ItemStack(TinkerArmor.heartCanister, 1, 1));
        TConstructRegistry.addItemStackToDirectory("canisterRedHeart", new ItemStack(TinkerArmor.heartCanister, 1, 2));

        travelGoggles = (TravelGear) new TravelGear(ArmorPart.Head).setUnlocalizedName("tconstruct.travelgoggles");
        travelVest = (TravelGear) new TravelGear(ArmorPart.Chest).setUnlocalizedName("tconstruct.travelvest");
        travelWings = (TravelGear) new TravelWings().setUnlocalizedName("tconstruct.travelwings");
        travelBoots = (TravelGear) new TravelGear(ArmorPart.Feet).setUnlocalizedName("tconstruct.travelboots");
        travelGlove = (AccessoryCore) new TravelGlove().setUnlocalizedName("tconstruct.travelgloves");
        travelBelt = (AccessoryCore) new TravelBelt().setUnlocalizedName("tconstruct.travelbelt");

        GameRegistry.registerItem(travelGoggles, "travelGoggles");
        GameRegistry.registerItem(travelVest, "travelVest");
        GameRegistry.registerItem(travelWings, "travelWings");
        GameRegistry.registerItem(travelBoots, "travelBoots");
        GameRegistry.registerItem(travelGlove, "travelGlove");
        GameRegistry.registerItem(travelBelt, "travelBelt");
    }

    @Handler
    public void init(FMLInitializationEvent event) {
        if (!PHConstruct.disableAllRecipes) {
            craftingTableRecipes();
            addRecipesForDryingRack();
        }
        registerModifiers();
        TConstructRegistry.equipableTab.init(travelGoggles.getDefaultItem());
        proxy.initialize();
    }

    @Handler
    public void postInit(FMLPostInitializationEvent evt) {
        proxy.registerTickHandler();
    }

    private void craftingTableRecipes() {

        // Armor Recipes
        Object[] helm = new String[] { "www", "w w" };
        Object[] chest = new String[] { "w w", "www", "www" };
        Object[] pants = new String[] { "www", "w w", "w w" };
        Object[] shoes = new String[] { "w w", "w w" };
        GameRegistry.addRecipe(new ShapedOreRecipe(TinkerArmor.helmetWood, helm, 'w', "logWood"));
        GameRegistry.addRecipe(new ShapedOreRecipe(TinkerArmor.chestplateWood, chest, 'w', "logWood"));
        GameRegistry.addRecipe(new ShapedOreRecipe(TinkerArmor.leggingsWood, pants, 'w', "logWood"));
        GameRegistry.addRecipe(new ShapedOreRecipe(TinkerArmor.bootsWood, shoes, 'w', "logWood"));

        // Accessories
        GameRegistry.addRecipe(
                new ShapedOreRecipe(new ItemStack(TinkerArmor.heartCanister, 1, 0), "##", "##", '#', "ingotAluminum"));
        GameRegistry.addRecipe(
                new ShapedOreRecipe(new ItemStack(TinkerArmor.heartCanister, 1, 0), "##", "##", '#', "ingotAluminium"));
        GameRegistry.addRecipe(
                new ShapedOreRecipe(
                        new ItemStack(TinkerArmor.heartCanister, 1, 0),
                        " # ",
                        "#B#",
                        " # ",
                        '#',
                        "ingotTin",
                        'B',
                        Items.bone));

        GameRegistry.addRecipe(
                new ShapedOreRecipe(
                        new ItemStack(TinkerArmor.diamondApple),
                        " d ",
                        "d#d",
                        " d ",
                        'd',
                        "gemDiamond",
                        '#',
                        new ItemStack(Items.apple)));
        GameRegistry.addShapelessRecipe(
                new ItemStack(TinkerArmor.heartCanister, 1, 2),
                new ItemStack(TinkerArmor.diamondApple),
                new ItemStack(TinkerTools.materials, 1, 8),
                new ItemStack(TinkerArmor.heartCanister, 1, 0),
                new ItemStack(TinkerArmor.heartCanister, 1, 1));
        GameRegistry.addShapelessRecipe(
                new ItemStack(TinkerArmor.heartCanister, 1, 1),
                new ItemStack(TinkerArmor.heartCanister, 1, 3));
        GameRegistry.addShapelessRecipe(
                new ItemStack(TinkerArmor.heartCanister, 1, 4),
                new ItemStack(TinkerArmor.heartCanister, 1, 2),
                new ItemStack(TinkerArmor.heartCanister, 1, 3),
                new ItemStack(Items.golden_apple, 1, 1));

        GameRegistry.addRecipe(
                new ShapedOreRecipe(
                        new ItemStack(TinkerArmor.knapsack, 1, 0),
                        "###",
                        "rmr",
                        "###",
                        '#',
                        new ItemStack(Items.leather),
                        'r',
                        new ItemStack(TinkerTools.toughRod, 1, 2),
                        'm',
                        "ingotGold"));
        ItemStack aluBrass = new ItemStack(TinkerTools.materials, 1, 14);
        GameRegistry.addRecipe(
                new ItemStack(TinkerArmor.knapsack, 1, 0),
                "###",
                "rmr",
                "###",
                '#',
                new ItemStack(Items.leather),
                'r',
                new ItemStack(TinkerTools.toughRod, 1, 2),
                'm',
                aluBrass);
        // Drying Rack Recipes
        GameRegistry
                .addRecipe(new ShapedOreRecipe(new ItemStack(TinkerArmor.dryingRack, 1, 0), "bbb", 'b', "slabWood"));

        // Temporary recipes
        ItemStack leather = new ItemStack(Items.leather);
        ItemStack string = new ItemStack(Items.string);
        GameRegistry.addRecipe(
                new ShapedOreRecipe(
                        travelGoggles.getDefaultItem(),
                        "# #",
                        "q#q",
                        "g g",
                        '#',
                        leather,
                        'q',
                        "blockGlass",
                        'g',
                        "ingotGold"));
        GameRegistry.addRecipe(
                new ShapedOreRecipe(
                        travelWings.getDefaultItem(),
                        "g g",
                        "i#i",
                        "i i",
                        '#',
                        Items.ender_pearl,
                        'g',
                        "ingotGold",
                        'i',
                        "ingotBronze"));
        GameRegistry.addRecipe(
                new ShapedOreRecipe(
                        travelVest.getDefaultItem(),
                        "#w#",
                        "#i#",
                        "#w#",
                        '#',
                        leather,
                        'i',
                        "ingotAluminum",
                        'w',
                        new ItemStack(Blocks.wool, 1, Short.MAX_VALUE)));
        GameRegistry.addRecipe(
                new ShapedOreRecipe(
                        travelBoots.getDefaultItem(),
                        "#s#",
                        "#i#",
                        "#s#",
                        '#',
                        leather,
                        's',
                        string,
                        'i',
                        "ingotAluminum"));
        GameRegistry.addShapedRecipe(travelGlove.getDefaultItem(), "  #", "###", " ##", '#', leather);
        GameRegistry.addRecipe(
                new ShapedOreRecipe(
                        travelBelt.getDefaultItem(),
                        "###",
                        "ici",
                        "###",
                        '#',
                        leather,
                        'c',
                        "chestWood",
                        'i',
                        "ingotAluminum"));
    }

    protected static void addRecipesForDryingRack() {
        // Drying rack
        DryingRackRecipes.addDryingRecipe(Items.beef, 20 * 60 * 5, new ItemStack(TinkerArmor.jerky, 1, 0));
        DryingRackRecipes.addDryingRecipe(Items.chicken, 20 * 60 * 5, new ItemStack(TinkerArmor.jerky, 1, 1));
        DryingRackRecipes.addDryingRecipe(Items.porkchop, 20 * 60 * 5, new ItemStack(TinkerArmor.jerky, 1, 2));
        DryingRackRecipes.addDryingRecipe(Items.fish, 20 * 60 * 5, new ItemStack(TinkerArmor.jerky, 1, 4));
        DryingRackRecipes.addDryingRecipe(Items.rotten_flesh, 20 * 60 * 5, new ItemStack(TinkerArmor.jerky, 1, 5));
        DryingRackRecipes.addDryingRecipe(
                new ItemStack(TinkerWorld.strangeFood, 1, 0),
                20 * 60 * 5,
                new ItemStack(TinkerArmor.jerky, 1, 6));
        DryingRackRecipes.addDryingRecipe(
                new ItemStack(TinkerWorld.strangeFood, 1, 1),
                20 * 60 * 5,
                new ItemStack(TinkerArmor.jerky, 1, 7));

    }

    private void registerModifiers() {
        ItemStack redstoneItem = new ItemStack(Items.redstone);
        ItemStack redstoneBlock = new ItemStack(Blocks.redstone_block);
        // Travel gear modifiers
        // MultiType
        ModifyBuilder.registerModifier(
                new TravelModDoubleJump(
                        EnumSet.of(ArmorPart.Legs, ArmorPart.Feet),
                        new ItemStack[] { new ItemStack(Items.ghast_tear), new ItemStack(TinkerWorld.slimeGel, 1, 0),
                                new ItemStack(Blocks.piston) }));
        ModifyBuilder.registerModifier(
                new TravelModDoubleJump(
                        EnumSet.of(ArmorPart.Legs, ArmorPart.Feet),
                        new ItemStack[] { new ItemStack(Items.ghast_tear), new ItemStack(TinkerWorld.slimeGel, 1, 1),
                                new ItemStack(Blocks.piston) }));
        ModifyBuilder.registerModifier(
                new AModInteger(
                        4,
                        "Moss",
                        EnumSet.of(ArmorPart.Legs, ArmorPart.Feet, ArmorPart.Chest, ArmorPart.Head),
                        new ItemStack[] { new ItemStack(TinkerTools.materials, 1, 6) },
                        3,
                        "\u00a72",
                        StatCollector.translateToLocal("modifier.tool.moss")));
        ModifyBuilder.registerModifier(new TravelModRepair());
        TConstructRegistry.registerActiveArmorMod(new ActiveTinkerArmor());

        // Head
        ModifyBuilder.registerModifier(
                new AModBoolean(
                        0,
                        "Night Vision",
                        EnumSet.of(ArmorPart.Head),
                        new ItemStack[] { new ItemStack(Items.flint_and_steel),
                                new ItemStack(Items.potionitem, 1, 8198), new ItemStack(Items.golden_carrot) },
                        "\u00a78",
                        "Night Vision"));

        // Chest
        ModifyBuilder.registerModifier(
                new AModInteger(
                        0,
                        "Perfect Dodge",
                        EnumSet.of(ArmorPart.Chest),
                        new ItemStack[] { new ItemStack(Items.ender_eye), new ItemStack(Items.ender_pearl),
                                new ItemStack(Items.sugar) },
                        1,
                        "\u00a7d",
                        "Perfect Dodge"));
        ModifyBuilder.registerModifier(
                new AModBoolean(
                        1,
                        "Stealth",
                        EnumSet.of(ArmorPart.Chest),
                        new ItemStack[] { new ItemStack(Items.fermented_spider_eye), new ItemStack(Items.ender_eye),
                                new ItemStack(Items.potionitem, 1, 8206), new ItemStack(Items.golden_carrot) },
                        "\u00a78",
                        "Stealth"));

        // Wings
        ItemStack feather = new ItemStack(Items.feather);
        ModifyBuilder.registerModifier(
                new AModInteger(
                        1,
                        "Feather Fall",
                        EnumSet.of(ArmorPart.Legs),
                        new ItemStack[] { new ItemStack(TinkerWorld.slimeGel, 1, 0), new ItemStack(Items.ender_pearl),
                                feather, feather, feather, feather, feather, feather },
                        1,
                        "\u00a7f",
                        "Feather Fall"));
        ModifyBuilder.registerModifier(
                new AModInteger(
                        1,
                        "Feather Fall",
                        EnumSet.of(ArmorPart.Legs),
                        new ItemStack[] { new ItemStack(TinkerWorld.slimeGel, 1, 1), new ItemStack(Items.ender_pearl),
                                feather, feather, feather, feather, feather, feather },
                        1,
                        "\u00a7f",
                        "Feather Fall"));

        // Feet
        ModifyBuilder.registerModifier(
                new AModBoolean(
                        1,
                        "WaterWalk",
                        EnumSet.of(ArmorPart.Feet),
                        new ItemStack[] { new ItemStack(Blocks.waterlily), new ItemStack(Blocks.waterlily) },
                        "\u00a79",
                        "Water-Walking"));
        ModifyBuilder.registerModifier(new AModLeadBoots(new ItemStack[] { new ItemStack(Blocks.iron_block) }));
        ModifyBuilder.registerModifier(
                new AModInteger(
                        3,
                        "Slimy Soles",
                        EnumSet.of(ArmorPart.Feet),
                        new ItemStack[] { new ItemStack(TinkerWorld.slimePad, 1, 0),
                                new ItemStack(TinkerWorld.slimePad, 1, 0) },
                        1,
                        "\u00a7a",
                        "Slimy Soles"));

        // Glove
        ModifyBuilder.registerModifier(
                new GloveSpeed(1, new ItemStack[] { redstoneItem, redstoneBlock }, new int[] { 1, 9 }));
        modAttackGlove = new ModAttack(
                "Quartz",
                2,
                new ItemStack[] { new ItemStack(Items.quartz), new ItemStack(Blocks.quartz_block, 1, Short.MAX_VALUE) },
                new int[] { 1, 4 },
                50,
                50,
                "Accessory");
    }
}
