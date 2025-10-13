package tconstruct.api.harvesting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CropHarvestHandlers {

    private static List<CropHarvestHandler> cropHarvestHandlers = new ArrayList<>();

    public static void registerCropHarvestHandler(CropHarvestHandler handler) {
        cropHarvestHandlers.add(handler);
    }

    public static List<CropHarvestHandler> getCropHarvestHandlers() {
        return Collections.unmodifiableList(cropHarvestHandlers);
    }
}
