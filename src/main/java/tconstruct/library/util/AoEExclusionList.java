package tconstruct.library.util;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraftforge.common.config.Configuration;

public class AoEExclusionList {

    private static final Map<String, String[]> toolExclusionLists = new HashMap<>();
    private static Configuration config;

    public static void init(File configFile) {
        config = new Configuration(configFile);
        loadConfig();
    }

    private static void loadConfig() {
        config.load();

        String[] tools = { "tool.hammer", "tool.excavator", "tool.lumberaxe" };
        for (String tool : tools) {
            String[] exclusionList = config.getStringList(
                tool + "Exclusions",
                "AOE_Exclusions",
                new String[] {},
                "Block IDs that should not be broken by " + tool + "'s AOE effect");
            toolExclusionLists.put(tool, exclusionList);
            System.out.println("Loaded exclusion list for " + tool + ": " + Arrays.toString(exclusionList));
        }

        if (config.hasChanged()) {
            config.save();
        }

        // Print out all loaded exclusion lists
        for (Map.Entry<String, String[]> entry : toolExclusionLists.entrySet()) {
            System.out.println("Exclusion list for " + entry.getKey() + ": " + Arrays.toString(entry.getValue()));
        }
    }

    public static boolean isBlockExcluded(String tool, Block block) {
        String[] exclusions = toolExclusionLists.get(tool);
        if (exclusions == null) {
            // Try with "tool." prefix if it's not present
            if (!tool.startsWith("tool.")) {
                exclusions = toolExclusionLists.get("tool." + tool);
            } else {
                // Try without "tool." prefix if it's present
                exclusions = toolExclusionLists.get(tool.substring(5) + "Exclusions");
            }
        }

        if (exclusions == null) {
            System.out.println("No exclusion list found for tool: " + tool);
            return false;
        }

        String blockId = Block.blockRegistry.getNameForObject(block);
        boolean isExcluded = Arrays.asList(exclusions).contains(blockId);

        System.out.println(
            "Checking exclusion for " + tool
                + ": Block "
                + blockId
                + " is "
                + (isExcluded ? "excluded" : "not excluded"));

        return isExcluded;
    }
}
