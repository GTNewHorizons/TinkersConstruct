package tconstruct.plugins.waila;

import net.minecraftforge.fluids.FluidStack;

import mcp.mobius.waila.api.IWailaRegistrar;
import tconstruct.TConstruct;
import tconstruct.smeltery.blocks.LavaTankBlock;
import tconstruct.smeltery.blocks.SmelteryBlock;
import tconstruct.smeltery.logic.CastingBasinLogic;
import tconstruct.smeltery.logic.CastingChannelLogic;
import tconstruct.smeltery.logic.CastingTableLogic;

public class WailaRegistrar {

    public static void wailaCallback(IWailaRegistrar registrar) {
        TConstruct.logger.info("[Waila-Compat] Got registrar: " + registrar);

        // Configs
        registrar.addConfig("Tinkers' Construct", "tcon.searedtank");
        registrar.addConfig("Tinkers' Construct", "tcon.castingchannel");
        registrar.addConfig("Tinkers' Construct", "tcon.basin");
        registrar.addConfig("Tinkers' Construct", "tcon.table");
        registrar.addConfig("Tinkers' Construct", "tcon.smeltery");

        // Tanks
        registrar.registerBodyProvider(new SearedTankDataProvider(), LavaTankBlock.class);
        registrar.registerBodyProvider(new CastingChannelDataProvider(), CastingChannelLogic.class);

        // Casting systems
        registrar.registerBodyProvider(new BasinDataProvider(), CastingBasinLogic.class);
        registrar.registerBodyProvider(new TableDataProvider(), CastingTableLogic.class);

        // Smeltery
        registrar.registerBodyProvider(new SmelteryDataProvider(), SmelteryBlock.class);
    }

    public static String fluidNameHelper(FluidStack f) {
        return f.getFluid().getLocalizedName();
    }
}
