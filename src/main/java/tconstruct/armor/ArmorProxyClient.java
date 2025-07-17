package tconstruct.armor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import mantle.lib.client.MantleClientRegistry;
import tconstruct.armor.gui.ArmorExtendedGui;
import tconstruct.armor.gui.KnapsackGui;
import tconstruct.armor.items.TravelGear;
import tconstruct.armor.model.BeltModel;
import tconstruct.armor.model.BootBump;
import tconstruct.armor.model.HiddenPlayerModel;
import tconstruct.armor.model.WingModel;
import tconstruct.armor.player.ArmorExtended;
import tconstruct.armor.player.KnapsackInventory;
import tconstruct.armor.player.TPlayerStats;
import tconstruct.client.ArmorControls;
import tconstruct.client.HealthBarRenderer;
import tconstruct.client.tabs.InventoryTabArmorExtended;
import tconstruct.client.tabs.InventoryTabKnapsack;
import tconstruct.client.tabs.InventoryTabVanilla;
import tconstruct.client.tabs.TabRegistry;
import tconstruct.common.TProxyCommon;
import tconstruct.library.accessory.IAccessoryModel;
import tconstruct.library.client.TConstructClientRegistry;
import tconstruct.library.crafting.ModifyBuilder;
import tconstruct.tools.TinkerTools;
import tconstruct.util.config.PHConstruct;
import tconstruct.world.TinkerWorld;

public class ArmorProxyClient extends ArmorProxyCommon {

    Minecraft mc = Minecraft.getMinecraft();

    public static WingModel wings = new WingModel();
    public static BootBump bootbump = new BootBump();
    public static HiddenPlayerModel glove = new HiddenPlayerModel(0.25F, 4);
    public static HiddenPlayerModel vest = new HiddenPlayerModel(0.25f, 1);
    public static BeltModel belt = new BeltModel();
    public static TPlayerStats playerStats = new TPlayerStats();
    public static KnapsackInventory knapsack = new KnapsackInventory();
    public static ArmorExtended armorExtended = new ArmorExtended();

    @Override
    public void preInit() {
        controlInstance = new ArmorControls();
        MinecraftForge.EVENT_BUS.register(new TabRegistry.EventHandler());
    }

    @Override
    public void initialize() {
        registerGuiHandler();
        registerKeys();
        registerManualIcons();
        registerManualRecipes();
        MinecraftForge.EVENT_BUS.register(this);
        if (PHConstruct.coloredHeartRender) {
            final HealthBarRenderer healthBarRenderer = new HealthBarRenderer();
            MinecraftForge.EVENT_BUS.register(healthBarRenderer);
            FMLCommonHandler.instance().bus().register(healthBarRenderer);
        }
    }

    private void registerManualIcons() {
        MantleClientRegistry.registerManualIcon("travelgoggles", TinkerArmor.travelGoggles.getDefaultItem());
        MantleClientRegistry.registerManualIcon("travelvest", TinkerArmor.travelVest.getDefaultItem());
        MantleClientRegistry.registerManualIcon("travelwings", TinkerArmor.travelWings.getDefaultItem());
        MantleClientRegistry.registerManualIcon("travelboots", TinkerArmor.travelBoots.getDefaultItem());
        MantleClientRegistry.registerManualIcon("travelbelt", TinkerArmor.travelBelt.getDefaultItem());
        MantleClientRegistry.registerManualIcon("travelglove", TinkerArmor.travelGlove.getDefaultItem());
    }

    private void registerManualRecipes() {
        ItemStack feather = new ItemStack(Items.feather);
        ItemStack redstone = new ItemStack(Items.redstone);
        ItemStack goggles = TinkerArmor.travelGoggles.getDefaultItem();

        TConstructClientRegistry.registerManualModifier(
                "nightvision",
                goggles.copy(),
                new ItemStack(Items.flint_and_steel),
                new ItemStack(Items.potionitem, 1, 8198),
                new ItemStack(Items.golden_carrot),
                null);

        ItemStack vest = TinkerArmor.travelVest.getDefaultItem();
        TConstructClientRegistry.registerManualModifier(
                "dodge",
                vest.copy(),
                new ItemStack(Items.ender_eye),
                new ItemStack(Items.ender_pearl),
                new ItemStack(Items.sugar),
                null);
        TConstructClientRegistry.registerManualModifier(
                "stealth",
                vest.copy(),
                new ItemStack(Items.fermented_spider_eye),
                new ItemStack(Items.ender_eye),
                new ItemStack(Items.potionitem, 1, 8206),
                new ItemStack(Items.golden_carrot));

        ItemStack wings = TinkerArmor.travelWings.getDefaultItem();
        TConstructClientRegistry.registerManualModifier(
                "doublejumpwings",
                wings.copy(),
                new ItemStack(Items.ghast_tear),
                new ItemStack(TinkerWorld.slimeGel, 1, 0),
                new ItemStack(Blocks.piston),
                null);

        ItemStack[] recipe = new ItemStack[] { new ItemStack(TinkerWorld.slimeGel, 1, 0),
                new ItemStack(Items.ender_pearl), feather, feather, feather, feather, feather, feather };
        ItemStack modWings = ModifyBuilder.instance.modifyItem(wings, recipe);
        MantleClientRegistry.registerManualLargeRecipe(
                "featherfall",
                modWings.copy(),
                feather,
                new ItemStack(TinkerWorld.slimeGel, 1, 0),
                feather,
                feather,
                wings.copy(),
                feather,
                feather,
                new ItemStack(Items.ender_pearl),
                feather);

        ItemStack boots = TinkerArmor.travelBoots.getDefaultItem();
        TConstructClientRegistry.registerManualModifier(
                "doublejumpboots",
                boots.copy(),
                new ItemStack(Items.ghast_tear),
                new ItemStack(TinkerWorld.slimeGel, 1, 1),
                new ItemStack(Blocks.piston),
                null);
        TConstructClientRegistry.registerManualModifier(
                "waterwalk",
                boots.copy(),
                new ItemStack(Blocks.waterlily),
                new ItemStack(Blocks.waterlily));
        TConstructClientRegistry.registerManualModifier("leadboots", boots.copy(), new ItemStack(Blocks.iron_block));
        TConstructClientRegistry.registerManualModifier(
                "slimysoles",
                boots.copy(),
                new ItemStack(TinkerWorld.slimePad, 1, 0),
                new ItemStack(TinkerWorld.slimePad, 1, 0));

        ItemStack gloves = TinkerArmor.travelGlove.getDefaultItem();
        TConstructClientRegistry
                .registerManualModifier("glovehaste", gloves.copy(), redstone, new ItemStack(Blocks.redstone_block));
        // MantleClientRegistry.registerManualSmallRecipe("gloveclimb", gloves.copy(), new ItemStack(Items.slime_ball),
        // new ItemStack(Blocks.web), new ItemStack(TinkerTools.materials, 1, 25), null);
        TConstructClientRegistry.registerManualModifier(
                "gloveknuckles",
                gloves.copy(),
                new ItemStack(Items.quartz),
                new ItemStack(Blocks.quartz_block, 1, Short.MAX_VALUE));

        // moss
        ItemStack moss = new ItemStack(TinkerTools.materials, 1, 6);
        TConstructClientRegistry.registerManualModifier("mossgoggles", goggles.copy(), moss.copy());
        TConstructClientRegistry.registerManualModifier("mossvest", vest.copy(), moss.copy());
        TConstructClientRegistry.registerManualModifier("mosswings", wings.copy(), moss.copy());
        TConstructClientRegistry.registerManualModifier("mossboots", boots.copy(), moss.copy());
    }

    @Override
    protected void registerGuiHandler() {
        super.registerGuiHandler();
        TProxyCommon.registerClientGuiHandler(inventoryGui, this);
        TProxyCommon.registerClientGuiHandler(armorGuiID, this);
        TProxyCommon.registerClientGuiHandler(knapsackGuiID, this);
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == ArmorProxyCommon.inventoryGui) {
            return new GuiInventory(player);
        }
        if (ID == ArmorProxyCommon.armorGuiID) {
            ArmorProxyClient.armorExtended.init(Minecraft.getMinecraft().thePlayer);
            return new ArmorExtendedGui(player.inventory, ArmorProxyClient.armorExtended);
        }
        if (ID == ArmorProxyCommon.knapsackGuiID) {
            ArmorProxyClient.knapsack.init(Minecraft.getMinecraft().thePlayer);
            return new KnapsackGui(player.inventory, ArmorProxyClient.knapsack);
        }
        return null;
    }

    @Override
    public void registerTickHandler() {
        FMLCommonHandler.instance().bus().register(new ArmorTickHandler());
    }

    /* Keybindings */
    public static ArmorControls controlInstance;

    @Override
    public void registerKeys() {
        controlInstance.registerKeys();

        TabRegistry.registerTab(new InventoryTabVanilla());
        TabRegistry.registerTab(new InventoryTabArmorExtended());
        TabRegistry.registerTab(new InventoryTabKnapsack());
    }

    @SubscribeEvent
    public void goggleZoom(FOVUpdateEvent event) {
        if (ArmorControls.zoom) {
            ItemStack helmet = event.entity.getCurrentArmor(3);
            if (helmet != null && helmet.getItem() instanceof TravelGear) {
                event.newfov = 0.3f;
            }
        }
        // ItemStack feet = player.getCurrentArmor(0);
        // event.newfov = 1.0f;
    }

    /* Armor rendering */
    @SubscribeEvent
    public void adjustArmor(RenderPlayerEvent.SetArmorModel event) {
        switch (event.slot) {
            case 1:
                ArmorProxyClient.vest.onGround = event.renderer.modelBipedMain.onGround;
                ArmorProxyClient.vest.isRiding = event.renderer.modelBipedMain.isRiding;
                ArmorProxyClient.vest.isChild = event.renderer.modelBipedMain.isChild;
                ArmorProxyClient.vest.isSneak = event.renderer.modelBipedMain.isSneak;
            case 2:
                ArmorProxyClient.wings.onGround = event.renderer.modelBipedMain.onGround;
                ArmorProxyClient.wings.isRiding = event.renderer.modelBipedMain.isRiding;
                ArmorProxyClient.wings.isChild = event.renderer.modelBipedMain.isChild;
                ArmorProxyClient.wings.isSneak = event.renderer.modelBipedMain.isSneak;

                ArmorProxyClient.glove.onGround = event.renderer.modelBipedMain.onGround;
                ArmorProxyClient.glove.isRiding = event.renderer.modelBipedMain.isRiding;
                ArmorProxyClient.glove.isChild = event.renderer.modelBipedMain.isChild;
                ArmorProxyClient.glove.isSneak = event.renderer.modelBipedMain.isSneak;
                ArmorProxyClient.glove.heldItemLeft = event.renderer.modelBipedMain.heldItemLeft;
                ArmorProxyClient.glove.heldItemRight = event.renderer.modelBipedMain.heldItemRight;

                ArmorProxyClient.belt.onGround = event.renderer.modelBipedMain.onGround;
                ArmorProxyClient.belt.isRiding = event.renderer.modelBipedMain.isRiding;
                ArmorProxyClient.belt.isChild = event.renderer.modelBipedMain.isChild;
                ArmorProxyClient.belt.isSneak = event.renderer.modelBipedMain.isSneak;

                if (PHConstruct.showTravellerAccessories) renderArmorExtras(event);

                break;
            case 3:
                ArmorProxyClient.bootbump.onGround = event.renderer.modelBipedMain.onGround;
                ArmorProxyClient.bootbump.isRiding = event.renderer.modelBipedMain.isRiding;
                ArmorProxyClient.bootbump.isChild = event.renderer.modelBipedMain.isChild;
                ArmorProxyClient.bootbump.isSneak = event.renderer.modelBipedMain.isSneak;
                break;
        }
    }

    // --- WitchingGadgets Translucent II enchant support
    private static int translucentID = -6;

    public static int getTranslucentID() {
        if (translucentID == -6) setTranslucentID();
        return translucentID;
    }

    private static void setTranslucentID() {
        for (Enchantment ench : Enchantment.enchantmentsList) {
            if (ench != null && ench.getName().equals("enchantment.wg.invisibleGear")) {
                translucentID = ench.effectId;
                return;
            }
        }
        translucentID = -1;
    }

    public static int getTranslucencyLevel(ItemStack stack) {
        int translucent = getTranslucentID();
        if (translucent > 0) return EnchantmentHelper.getEnchantmentLevel(translucent, stack);
        else return 0;
    }

    // ---

    void renderArmorExtras(RenderPlayerEvent.SetArmorModel event) {

        EntityPlayer player = event.entityPlayer;
        // todo: synchronize extra armor with other clients. Until then, only draw locally
        if (player != Minecraft.getMinecraft().thePlayer) return;
        float partialTick = event.partialRenderTick;
        float yawOffset = this.interpolateRotation(player.prevRenderYawOffset, player.renderYawOffset, partialTick);
        float yawRotation = this.interpolateRotation(player.prevRotationYawHead, player.rotationYawHead, partialTick);
        float pitch;
        final float zeropointsixtwofive = 0.0625F;

        if (player.isRiding() && player.ridingEntity instanceof EntityLivingBase) {
            EntityLivingBase entitylivingbase1 = (EntityLivingBase) player.ridingEntity;
            yawOffset = this.interpolateRotation(
                    entitylivingbase1.prevRenderYawOffset,
                    entitylivingbase1.renderYawOffset,
                    partialTick);
            pitch = MathHelper.wrapAngleTo180_float(yawRotation - yawOffset);

            if (pitch < -85.0F) {
                pitch = -85.0F;
            }

            if (pitch >= 85.0F) {
                pitch = 85.0F;
            }

            yawOffset = yawRotation - pitch;

            if (pitch * pitch > 2500.0F) {
                yawOffset += pitch * 0.2F;
            }
        }

        pitch = this.handleRotationFloat(player, partialTick);
        float bodyRotation = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTick;
        float limbSwing = player.prevLimbSwingAmount
                + (player.limbSwingAmount - player.prevLimbSwingAmount) * partialTick;
        float limbSwingMod = player.limbSwing - player.limbSwingAmount * (1.0F - partialTick);
        // TPlayerStats stats = TPlayerStats.get(player);
        ArmorExtended armor = ArmorProxyClient.armorExtended; // TODO: Do this for every player, not just the client
        if (armor != null && armor.inventory[1] != null) {
            if (getTranslucencyLevel(armor.inventory[1]) != 2
                    && !(player.isInvisible() && getTranslucencyLevel(armor.inventory[1]) > 0)) {
                Item item = armor.inventory[1].getItem();
                ModelBiped model = item.getArmorModel(player, armor.inventory[1], 4);

                if (item instanceof IAccessoryModel) {
                    this.mc.getTextureManager()
                            .bindTexture(((IAccessoryModel) item).getWearbleTexture(player, armor.inventory[1], 1));
                    model.setLivingAnimations(player, limbSwingMod, limbSwing, partialTick);
                    model.render(
                            player,
                            limbSwingMod,
                            limbSwing,
                            pitch,
                            yawRotation - yawOffset,
                            bodyRotation,
                            zeropointsixtwofive);
                }
            }
        }

        if (armor != null && armor.inventory[3] != null) {
            if (getTranslucencyLevel(armor.inventory[3]) != 2
                    && !(player.isInvisible() && getTranslucencyLevel(armor.inventory[3]) > 0)) {
                Item item = armor.inventory[3].getItem();
                ModelBiped model = item.getArmorModel(player, armor.inventory[3], 5);

                if (item instanceof IAccessoryModel) {
                    this.mc.getTextureManager()
                            .bindTexture(((IAccessoryModel) item).getWearbleTexture(player, armor.inventory[1], 1));
                    model.setLivingAnimations(player, limbSwingMod, limbSwing, partialTick);
                    model.render(
                            player,
                            limbSwingMod,
                            limbSwing,
                            pitch,
                            yawRotation - yawOffset,
                            bodyRotation,
                            zeropointsixtwofive);
                }
            }
        }
    }

    private float interpolateRotation(float par1, float par2, float par3) {
        float f3;

        f3 = par2 - par1;
        while (f3 < -180.0F) {
            f3 += 360.0F;
        }

        while (f3 >= 180.0F) {
            f3 -= 360.0F;
        }

        return par1 + par3 * f3;
    }

    protected float handleRotationFloat(EntityLivingBase par1EntityLivingBase, float par2) {
        return (float) par1EntityLivingBase.ticksExisted + par2;
    }

    @Override
    public void updatePlayerStats(TPlayerStats stats) {

        playerStats.copyFrom(stats, false);
        armorExtended = stats.armor;
        knapsack = stats.knapsack;
        // dumpTPlayerStats(stats);
    }
}
