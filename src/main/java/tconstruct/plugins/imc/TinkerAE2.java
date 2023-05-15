package tconstruct.plugins.imc;

import java.util.*;

import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.registry.GameRegistry.ObjectHolder;
import mantle.pulsar.pulse.*;
import tconstruct.TConstruct;

@ObjectHolder(TConstruct.modID)
@Pulse(
        id = "Tinkers AE2 Compatibility",
        description = "Tinkers Construct compatibility for Applied Energistics 2",
        modsRequired = "appliedenergistics2",
        forced = true)
public class TinkerAE2 {

    private static final List<String> spatialIOLogics = Arrays.asList(
            "EssenceExtractorLogic",
            "GolemPedestalLogic", // TODO What happened to these?
            "MultiServantLogic"); // TODO Should Mantle handle this?

    private static final List<String> spatialIOSmelteryLogics = Arrays.asList(
            "AdaptiveSmelteryLogic",
            "AqueductLogic",
            "CastingBasinLogic",
            "CastingChannelLogic",
            "CastingTableLogic",
            "FaucetLogic",
            "LavaTankLogic",
            "SmelteryDrainLogic",
            "SmelteryLogic",
            "TankAirLogic",
            "TowerFurnaceLogic");

    private static final List<String> spatialIOToolLogics = Arrays.asList(
            "CraftingStationLogic",
            "FrypanLogic",
            "PartBuilderLogic",
            "PatternChestLogic",
            "StencilTableLogic",
            "ToolForgeLogic",
            "ToolStationLogic");

    @Handler
    public void init(FMLInitializationEvent event) {
        TConstruct.logger.info("AE2 detected. Registering for Spatial IO.");

        addtoSpatialWhitelist("tconstruct.blocks.logic.DryingRackLogic");
        addtoSpatialWhitelist("tconstruct.mechworks.logic.TileEntityLandmine");

        for (String s : spatialIOSmelteryLogics) {
            addtoSpatialWhitelist("tconstruct.smeltery.logic." + s);
        }

        for (String s : spatialIOToolLogics) {
            addtoSpatialWhitelist("tconstruct.tools.logic." + s);
        }
    }

    public void addtoSpatialWhitelist(String teClass) {
        FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial", teClass);
    }
}
