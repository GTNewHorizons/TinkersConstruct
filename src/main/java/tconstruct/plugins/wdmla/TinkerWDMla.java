package tconstruct.plugins.wdmla;

import net.minecraft.util.ResourceLocation;

import com.gtnewhorizons.wdmla.api.IWDMlaClientRegistration;
import com.gtnewhorizons.wdmla.api.IWDMlaCommonRegistration;
import com.gtnewhorizons.wdmla.api.IWDMlaPlugin;
import com.gtnewhorizons.wdmla.api.WDMlaPlugin;
import com.gtnewhorizons.wdmla.plugin.universal.FluidStorageProvider;
import com.gtnewhorizons.wdmla.plugin.universal.ItemStorageProvider;
import com.gtnewhorizons.wdmla.plugin.vanilla.TECustomNameHeaderProvider;

import mantle.blocks.abstracts.InventorySlab;
import mantle.pulsar.pulse.Pulse;
import tconstruct.TConstruct;
import tconstruct.armor.blocks.DryingRack;
import tconstruct.mechworks.blocks.BlockLandmine;
import tconstruct.smeltery.blocks.CastingChannelBlock;
import tconstruct.smeltery.blocks.LavaTankBlock;
import tconstruct.smeltery.blocks.SearedBlock;
import tconstruct.smeltery.blocks.SmelteryBlock;
import tconstruct.tools.blocks.CraftingStationBlock;
import tconstruct.tools.blocks.FurnaceSlab;
import tconstruct.tools.blocks.ToolStationBlock;

@Pulse(
        id = "Tinkers WDMla Compatibility",
        description = "Tinkers Construct compatibility for WDMla",
        modsRequired = "wdmla",
        forced = true)
@WDMlaPlugin
public class TinkerWDMla implements IWDMlaPlugin {

    @Override
    public void register(IWDMlaCommonRegistration registration) {
        registration.registerBlockDataProvider(DryingRackProvider.INSTANCE, DryingRack.class);
        registration.registerBlockDataProvider(TECustomNameHeaderProvider.INSTANCE, FurnaceSlab.class);
        registration.registerBlockDataProvider(FurnaceSlabProvider.INSTANCE, FurnaceSlab.class);
        registration.registerBlockDataProvider(SmelteryStatusProvider.INSTANCE, SmelteryBlock.class);

        registration.registerItemStorage(ItemStorageProvider.Extension.INSTANCE, CraftingStationBlock.class);
        registration.registerItemStorage(ItemStorageProvider.Extension.INSTANCE, ToolStationBlock.class);
        registration.registerItemStorage(ItemStorageProvider.Extension.INSTANCE, InventorySlab.class);
        registration.registerItemStorage(ItemStorageProvider.Extension.INSTANCE, SmelteryBlock.class);
        registration.registerItemStorage(ItemStorageProvider.Extension.INSTANCE, SearedBlock.class);

        registration.registerFluidStorage(SmelteryFluidProvider.INSTANCE, SmelteryBlock.class);
        registration.registerFluidStorage(FluidStorageProvider.Extension.INSTANCE, LavaTankBlock.class);
        registration.registerFluidStorage(FluidStorageProvider.Extension.INSTANCE, CastingChannelBlock.class);
        registration.registerFluidStorage(FluidStorageProvider.Extension.INSTANCE, SearedBlock.class);

        registration.registerProgress(SearedBlockProgressProvider.INSTANCE, SearedBlock.class);
    }

    @Override
    public void registerClient(IWDMlaClientRegistration registration) {
        registration.registerBlockComponent(LandmineHeaderProvider.INSTANCE, BlockLandmine.class);
        registration.registerBlockComponent(DryingRackProvider.INSTANCE, DryingRack.class);
        registration.registerBlockComponent(FurnaceSlabProvider.INSTANCE, FurnaceSlab.class);
        registration.registerBlockComponent(SmelteryStatusProvider.INSTANCE, SmelteryBlock.class);

        registration.registerFluidStorageClient(SmelteryFluidProvider.INSTANCE);

        registration.registerProgressClient(SearedBlockProgressProvider.INSTANCE);
    }

    public static ResourceLocation TiC(String uid) {
        return new ResourceLocation(TConstruct.modID.toLowerCase(), uid);
    }
}
