package tconstruct.plugins.imc;

import static tconstruct.smeltery.TinkerSmeltery.*;

import net.minecraftforge.fluids.Fluid;

import cpw.mods.fml.common.event.*;
import mantle.pulsar.pulse.*;
import tconstruct.TConstruct;

@Pulse(
        id = "Tinkers Mystcraft Compatibility",
        forced = true,
        modsRequired = "Mystcraft",
        pulsesRequired = "Tinkers' Smeltery")
public class TinkerMystcraft {

    @Handler
    public void init(FMLInitializationEvent event) {
        final Fluid[] fluids = new Fluid[] {
                // precious tinker fluids
                moltenGoldFluid, moltenSteelFluid, moltenEmeraldFluid, moltenQuartzFluid, moltenArditeFluid,
                moltenCobaltFluid,
                // all alloys
                pigIronFluid, moltenBronzeFluid, moltenAlumiteFluid, moltenAlubrassFluid, moltenManyullynFluid,
                // precious TE fluids
                moltenEnderFluid, moltenSilverFluid, moltenInvarFluid, moltenElectrumFluid, moltenShinyFluid,
                moltenSignalumFluid, moltenLumiumFluid, moltenMithrilFluid, moltenEnderiumFluid };

        TConstruct.logger.info("Mystcraft detected. Blacklisting Mystcraft fluid symbols.");
        for (Fluid fluid : fluids) {
            if (fluid == null) continue;
            FMLInterModComms.sendMessage("Mystcraft", "blacklistfluid", fluid.getName());
        }
    }
}
