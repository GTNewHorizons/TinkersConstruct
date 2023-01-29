/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */

package exterminatorJeff.undergroundBiomes.api;

import net.minecraft.block.Block;
import net.minecraft.world.World;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;

/**
 * Striped down version, get the full API from here: https://github.com/Zeno410/UndergroundBiomes1.7API This is an
 * interface for the class that can create Underground Biomes versions of arbitary ores It creates three new blocks for
 * each texturized ore.
 * 
 * @author Zeno410
 */
public interface UBOreTexturizer {

    // usage: Block is the ore block.
    // Overlay name is the fully qualified name, e.g. modname:overlayName
    // that static vars are fully qualified names for all the textures in the UB pack, just pass as is
    // the event isn't needed per se, but if this is called anytime else, the blocks will not "stick"
    void setupUBOre(Block oreBlock, String overlayName, FMLPreInitializationEvent event);

    void setupUBOre(Block oreBlock, int metadata, String overlayName, FMLPreInitializationEvent event);

    void setupUBOre(Block oreBlock, int metadata, String overlayName, String blockName,
            FMLPreInitializationEvent event);

    void requestUBOreSetup(Block oreBlock, String overlayName) throws BlocksAreAlreadySet;

    void requestUBOreSetup(Block oreBlock, int metadata, String overlayName) throws BlocksAreAlreadySet;

    void requestUBOreSetup(Block oreBlock, int metadata, String overlayName, String blockName)
            throws BlocksAreAlreadySet;

    void redoOres(int xInBlockCoordinates, int zInBlockCoordinates, World serverSideWorld);

    String amber_overlay = "undergroundbiomes:amber_overlay";
    String cinnabar_overlay = "undergroundbiomes:cinnabar_overlay";
    String coal_overlay = "undergroundbiomes:coal_overlay";
    String copper_overlay = "undergroundbiomes:copper_overlay";
    String diamond_overlay = "undergroundbiomes:diamond_overlay";
    String emerald_overlay = "undergroundbiomes:emerald_overlay";
    String gold_overlay = "undergroundbiomes:gold_overlay";
    String iron_overlay = "undergroundbiomes:iron_overlay";
    String lapis_overlay = "undergroundbiomes:lapis_overlay";
    String lead_overlay = "undergroundbiomes:lead_overlay";
    String olivine_peridot_overlay = "undergroundbiomes:olivine-peridot_overlay";
    String redstone_overlay = "undergroundbiomes:redstone_overlay";
    String ruby_overlay = "undergroundbiomes:ruby_overlay";
    String sapphire_overlay = "undergroundbiomes:sapphire_overlay";
    String tin_overlay = "undergroundbiomes:tin_overlay";
    String uranium_overlay = "undergroundbiomes:uranium_overlay";

    class BlocksAreAlreadySet extends RuntimeException {

        // this is thrown if UB has already run its pre-initialization step and can no longer register blocks
        public final Block oreBlock;
        public final String overlayName;

        public BlocksAreAlreadySet(Block oreBlock, String overlayName) {
            this.oreBlock = oreBlock;
            this.overlayName = overlayName;
        }

        @Override
        public String toString() {
            String blockDescription = "undefined block";
            String overlayDescription = "undefined overlay";
            if (oreBlock != null) blockDescription = oreBlock.getUnlocalizedName();
            if (overlayName != null) overlayDescription = overlayName;
            return "Attempt to create Underground Biomes ore for " + blockDescription
                    + " with "
                    + overlayDescription
                    + " after blocks have already been defined";
        }
    }
}
