package tconstruct.plugins.waila;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.registry.GameRegistry.ObjectHolder;
import mantle.pulsar.pulse.Handler;
import mantle.pulsar.pulse.Pulse;
import tconstruct.TConstruct;

@ObjectHolder(TConstruct.modID)
@Pulse(
        id = "Tinkers Waila Compatibility",
        description = "Tinkers Construct compatibility for Waila",
        modsRequired = "Waila",
        forced = true)
public class TinkerWaila {

    @Handler
    public void init(FMLInitializationEvent event) {
        if (Loader.isModLoaded("wdmla")) {
            return;
        }

        TConstruct.logger.info("Waila detected. Registering TConstruct tank blocks with Waila registry.");
        FMLInterModComms.sendMessage("Waila", "register", "tconstruct.plugins.waila.WailaRegistrar.wailaCallback");
    }
}
