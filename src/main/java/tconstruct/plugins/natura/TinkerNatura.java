package tconstruct.plugins.natura;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import mantle.pulsar.pulse.Handler;
import mantle.pulsar.pulse.Pulse;
import tconstruct.TConstruct;
import tconstruct.api.harvesting.CropHarvestHandlers;

@GameRegistry.ObjectHolder(TConstruct.modID)
@Pulse(
        id = "Tinkers Natura Compatibility",
        description = "Tinkers Construct compatibility for Natura",
        modsRequired = "Natura",
        forced = true)
public class TinkerNatura {

    @Handler
    public void init(FMLInitializationEvent event) {
        CropHarvestHandlers.registerCropHarvestHandler(new NaturaCropHarvestHandler());
    }
}
