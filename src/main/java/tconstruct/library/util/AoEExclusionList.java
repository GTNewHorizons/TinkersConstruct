package tconstruct.library.util;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraftforge.common.config.Configuration;

public class AoEExclusionList {

    private static final Map<String, Set<String>> toolExclusionLists = new HashMap<>();
    private static Configuration config;

    public static void init(File configFile) {
        config = new Configuration(configFile);
        loadConfig();
    }

    private static void loadConfig() {
        config.load();

        String[] tools = { "tool.hammer", "tool.excavator", "tool.lumberaxe" };
        for (String tool : tools) {
            String[] exclusionArray = config.getStringList(
                    tool + "Exclusions",
                    "AOE_Exclusions",
                    new String[] { "examplemod:exampleblock", "examplemod:exampleblock:1" },
                    "Block IDs (with optional metadata) that should not be broken by " + tool + "'s AOE effect");
            Set<String> exclusionSet = new HashSet<>(Arrays.asList(exclusionArray));
            toolExclusionLists.put(tool, exclusionSet);
        }

        if (config.hasChanged()) {
            config.save();
        }
    }

    public static boolean isBlockExcluded(String tool, Block block, int metadata) {
        Set<String> exclusions = toolExclusionLists.get(tool);
        if (exclusions == null) {
            exclusions = toolExclusionLists.get("tool." + tool);
        }

        if (exclusions == null || exclusions.isEmpty()) {
            return false;
        }

        String blockId = Block.blockRegistry.getNameForObject(block);
        return exclusions.contains(blockId) || exclusions.contains(blockId + ":" + metadata);
    }
}
