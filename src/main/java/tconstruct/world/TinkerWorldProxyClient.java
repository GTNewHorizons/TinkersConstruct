package tconstruct.world;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelSlime;
import net.minecraft.client.particle.EntityAuraFX;
import net.minecraft.client.particle.EntityBreakingFX;
import net.minecraft.client.particle.EntityBubbleFX;
import net.minecraft.client.particle.EntityCloudFX;
import net.minecraft.client.particle.EntityCritFX;
import net.minecraft.client.particle.EntityDropParticleFX;
import net.minecraft.client.particle.EntityEnchantmentTableParticleFX;
import net.minecraft.client.particle.EntityExplodeFX;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.particle.EntityFireworkSparkFX;
import net.minecraft.client.particle.EntityFlameFX;
import net.minecraft.client.particle.EntityFootStepFX;
import net.minecraft.client.particle.EntityHeartFX;
import net.minecraft.client.particle.EntityHugeExplodeFX;
import net.minecraft.client.particle.EntityLargeExplodeFX;
import net.minecraft.client.particle.EntityLavaFX;
import net.minecraft.client.particle.EntityNoteFX;
import net.minecraft.client.particle.EntityPortalFX;
import net.minecraft.client.particle.EntityReddustFX;
import net.minecraft.client.particle.EntitySmokeFX;
import net.minecraft.client.particle.EntitySnowShovelFX;
import net.minecraft.client.particle.EntitySpellParticleFX;
import net.minecraft.client.particle.EntitySplashFX;
import net.minecraft.client.particle.EntitySuspendFX;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.registry.VillagerRegistry;
import mantle.lib.client.MantleClientRegistry;
import tconstruct.tools.TinkerTools;
import tconstruct.world.entity.BlueSlime;
import tconstruct.world.entity.KingBlueSlime;
import tconstruct.world.model.BarricadeRender;
import tconstruct.world.model.OreberryRender;
import tconstruct.world.model.PunjiRender;
import tconstruct.world.model.RenderLandmine;
import tconstruct.world.model.SlimeChannelRender;
import tconstruct.world.model.SlimePadRender;
import tconstruct.world.model.SlimeRender;

public class TinkerWorldProxyClient extends TinkerWorldProxyCommon {

    @Override
    public void initialize() {
        registerRenderer();
        registerManualIcons();
        registerManualRecipes();
    }

    void registerRenderer() {
        RenderingRegistry.registerBlockHandler(new OreberryRender());
        RenderingRegistry.registerBlockHandler(new BarricadeRender());
        RenderingRegistry.registerBlockHandler(new RenderLandmine());
        RenderingRegistry.registerBlockHandler(new PunjiRender());
        RenderingRegistry.registerBlockHandler(new SlimeChannelRender());
        RenderingRegistry.registerBlockHandler(new SlimePadRender());

        // Entities
        SlimeRender slimeRender = new SlimeRender(new ModelSlime(16), new ModelSlime(0), 0.25F);
        RenderingRegistry.registerEntityRenderingHandler(BlueSlime.class, slimeRender);
        RenderingRegistry.registerEntityRenderingHandler(KingBlueSlime.class, slimeRender);

        VillagerRegistry.instance()
                .registerVillagerSkin(78943, new ResourceLocation("tinker", "textures/mob/villagertools.png"));
    }

    void registerManualIcons() {}

    void registerManualRecipes() {
        ItemStack netherrack = new ItemStack(Blocks.netherrack);
        ItemStack coal = new ItemStack(Items.coal);
        ItemStack log = new ItemStack(Blocks.log, 1, 0);

        ItemStack graveyardsoil = new ItemStack(TinkerTools.craftedSoil, 1, 3);
        ItemStack consecratedsoil = new ItemStack(TinkerTools.craftedSoil, 1, 4);

        MantleClientRegistry.registerManualSmallRecipe(
                "slimechannel",
                new ItemStack(TinkerWorld.slimeChannel, 1, 0),
                new ItemStack(TinkerWorld.slimeGel, 1, 0),
                new ItemStack(Items.redstone),
                null,
                null);
        MantleClientRegistry.registerManualSmallRecipe(
                "bouncepad",
                new ItemStack(TinkerWorld.slimePad, 1, 0),
                new ItemStack(TinkerWorld.slimeChannel),
                new ItemStack(Items.slime_ball),
                null,
                null);

        MantleClientRegistry.registerManualSmallRecipe(
                "graveyardsoil",
                graveyardsoil,
                new ItemStack(Blocks.dirt),
                new ItemStack(Items.rotten_flesh),
                new ItemStack(Items.dye, 1, 15),
                null);
        MantleClientRegistry.registerManualFurnaceRecipe("consecratedsoil", consecratedsoil, graveyardsoil);

        // Traps
        ItemStack reed = new ItemStack(Items.reeds);
        MantleClientRegistry.registerManualLargeRecipe(
                "punji",
                new ItemStack(TinkerWorld.punji, 5),
                reed,
                null,
                reed,
                null,
                reed,
                null,
                reed,
                null,
                reed);
        MantleClientRegistry
                .registerManualSmallRecipe("barricade", new ItemStack(TinkerWorld.barricadeOak), null, log, null, log);
    }

    Minecraft mc = Minecraft.getMinecraft();

    @Override
    public void spawnParticle(String particle, double xPos, double yPos, double zPos, double velX, double velY,
            double velZ) {
        this.doSpawnParticle(particle, xPos, yPos, zPos, velX, velY, velZ);
    }

    public EntityFX doSpawnParticle(String par1Str, double par2, double par4, double par6, double par8, double par10,
            double par12) {
        if (this.mc == null) this.mc = Minecraft.getMinecraft();

        if (this.mc.renderViewEntity != null && this.mc.effectRenderer != null) {
            int i = this.mc.gameSettings.particleSetting;

            if (i == 1 && mc.theWorld.rand.nextInt(3) == 0) {
                i = 2;
            }

            double d6 = this.mc.renderViewEntity.posX - par2;
            double d7 = this.mc.renderViewEntity.posY - par4;
            double d8 = this.mc.renderViewEntity.posZ - par6;
            EntityFX entityfx = null;

            switch (par1Str) {
                case "hugeexplosion":
                    this.mc.effectRenderer.addEffect(
                            entityfx = new EntityHugeExplodeFX(mc.theWorld, par2, par4, par6, par8, par10, par12));
                    break;
                case "largeexplode":
                    this.mc.effectRenderer.addEffect(
                            entityfx = new EntityLargeExplodeFX(
                                    mc.renderEngine,
                                    mc.theWorld,
                                    par2,
                                    par4,
                                    par6,
                                    par8,
                                    par10,
                                    par12));
                    break;
                case "fireworksSpark":
                    this.mc.effectRenderer.addEffect(
                            entityfx = new EntityFireworkSparkFX(
                                    mc.theWorld,
                                    par2,
                                    par4,
                                    par6,
                                    par8,
                                    par10,
                                    par12,
                                    this.mc.effectRenderer));
                    break;
            }

            if (entityfx != null) {
                return entityfx;
            } else {
                double d9 = 16.0D;

                if (d6 * d6 + d7 * d7 + d8 * d8 > d9 * d9) {
                    return null;
                } else if (i > 1) {
                    return null;
                } else {
                    switch (par1Str) {
                        case "bubble":
                            entityfx = new EntityBubbleFX(mc.theWorld, par2, par4, par6, par8, par10, par12);
                            break;
                        case "suspended":
                            entityfx = new EntitySuspendFX(mc.theWorld, par2, par4, par6, par8, par10, par12);
                            break;
                        case "depthsuspend":
                        case "townaura":
                            entityfx = new EntityAuraFX(mc.theWorld, par2, par4, par6, par8, par10, par12);
                            break;
                        case "crit":
                            entityfx = new EntityCritFX(mc.theWorld, par2, par4, par6, par8, par10, par12);
                            break;
                        case "smoke":
                            entityfx = new EntitySmokeFX(mc.theWorld, par2, par4, par6, par8, par10, par12);
                            break;
                        case "mobSpell":
                            entityfx = new EntitySpellParticleFX(mc.theWorld, par2, par4, par6, 0.0D, 0.0D, 0.0D);
                            entityfx.setRBGColorF((float) par8, (float) par10, (float) par12);
                            break;
                        case "mobSpellAmbient":
                            entityfx = new EntitySpellParticleFX(mc.theWorld, par2, par4, par6, 0.0D, 0.0D, 0.0D);
                            entityfx.setAlphaF(0.15F);
                            entityfx.setRBGColorF((float) par8, (float) par10, (float) par12);
                            break;
                        case "spell":
                            entityfx = new EntitySpellParticleFX(mc.theWorld, par2, par4, par6, par8, par10, par12);
                            break;
                        case "instantSpell":
                            entityfx = new EntitySpellParticleFX(mc.theWorld, par2, par4, par6, par8, par10, par12);
                            ((EntitySpellParticleFX) entityfx).setBaseSpellTextureIndex(144);
                            break;
                        case "witchMagic":
                            entityfx = new EntitySpellParticleFX(mc.theWorld, par2, par4, par6, par8, par10, par12);
                            ((EntitySpellParticleFX) entityfx).setBaseSpellTextureIndex(144);
                            float f = mc.theWorld.rand.nextFloat() * 0.5F + 0.35F;
                            entityfx.setRBGColorF(1.0F * f, 0.0F * f, 1.0F * f);
                            break;
                        case "note":
                            entityfx = new EntityNoteFX(mc.theWorld, par2, par4, par6, par8, par10, par12);
                            break;
                        case "portal":
                            entityfx = new EntityPortalFX(mc.theWorld, par2, par4, par6, par8, par10, par12);
                            break;
                        case "enchantmenttable":
                            entityfx = new EntityEnchantmentTableParticleFX(
                                    mc.theWorld,
                                    par2,
                                    par4,
                                    par6,
                                    par8,
                                    par10,
                                    par12);
                            break;
                        case "explode":
                            entityfx = new EntityExplodeFX(mc.theWorld, par2, par4, par6, par8, par10, par12);
                            break;
                        case "flame":
                            entityfx = new EntityFlameFX(mc.theWorld, par2, par4, par6, par8, par10, par12);
                            break;
                        case "lava":
                            entityfx = new EntityLavaFX(mc.theWorld, par2, par4, par6);
                            break;
                        case "footstep":
                            entityfx = new EntityFootStepFX(mc.renderEngine, mc.theWorld, par2, par4, par6);
                            break;
                        case "splash":
                            entityfx = new EntitySplashFX(mc.theWorld, par2, par4, par6, par8, par10, par12);
                            break;
                        case "largesmoke":
                            entityfx = new EntitySmokeFX(mc.theWorld, par2, par4, par6, par8, par10, par12, 2.5F);
                            break;
                        case "cloud":
                            entityfx = new EntityCloudFX(mc.theWorld, par2, par4, par6, par8, par10, par12);
                            break;
                        case "reddust":
                            entityfx = new EntityReddustFX(
                                    mc.theWorld,
                                    par2,
                                    par4,
                                    par6,
                                    (float) par8,
                                    (float) par10,
                                    (float) par12);
                            break;
                        case "snowballpoof":
                            entityfx = new EntityBreakingFX(mc.theWorld, par2, par4, par6, Items.snowball);
                            break;
                        case "dripWater":
                            entityfx = new EntityDropParticleFX(mc.theWorld, par2, par4, par6, Material.water);
                            break;
                        case "dripLava":
                            entityfx = new EntityDropParticleFX(mc.theWorld, par2, par4, par6, Material.lava);
                            break;
                        case "snowshovel":
                            entityfx = new EntitySnowShovelFX(mc.theWorld, par2, par4, par6, par8, par10, par12);
                            break;
                        case "blueslime":
                            entityfx = new EntityBreakingFX(mc.theWorld, par2, par4, par6, TinkerWorld.strangeFood);
                            break;
                        case "heart":
                            entityfx = new EntityHeartFX(mc.theWorld, par2, par4, par6, par8, par10, par12);
                            break;
                        case "angryVillager":
                            entityfx = new EntityHeartFX(mc.theWorld, par2, par4 + 0.5D, par6, par8, par10, par12);
                            entityfx.setParticleTextureIndex(81);
                            entityfx.setRBGColorF(1.0F, 1.0F, 1.0F);
                            break;
                        case "happyVillager":
                            entityfx = new EntityAuraFX(mc.theWorld, par2, par4, par6, par8, par10, par12);
                            entityfx.setParticleTextureIndex(82);
                            entityfx.setRBGColorF(1.0F, 1.0F, 1.0F);
                            break;
                    }

                    if (entityfx != null) {
                        this.mc.effectRenderer.addEffect(entityfx);
                    }

                    return entityfx;
                }
            }
        } else {
            return null;
        }
    }
}
