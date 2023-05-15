package tconstruct.util;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ICrashCallable;
import mantle.crash.CallableSuppConfig;

public class EnvironmentChecks {

    private EnvironmentChecks() {} // Singleton

    /**
     * Checks for conflicting stuff in environment; adds callable to any crash logs if so. Note: This code adds
     * additional data to crashlogs. It does not trigger any crashes.
     */
    // private static List<String> incompatibilities = new ArrayList<>();
    // private static EnvironmentChecks instance = new EnvironmentChecks();
    public static void verifyEnvironmentSanity() {
        // Bukkit/Magic Launcher/Optifine are caught by Mantle, so we no longer
        // check for those.
        ICrashCallable callable = new CallableSuppConfig("TConstruct");
        FMLCommonHandler.instance().registerCrashCallable(callable);
    }

    public static String modCompatDetails(String type, boolean consoleFormat) {
        String n = consoleFormat ? System.getProperty("line.separator") : "\n";
        return "";
    }
}
