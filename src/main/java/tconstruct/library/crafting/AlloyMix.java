package tconstruct.library.crafting;

import java.util.*;

import net.minecraftforge.fluids.FluidStack;

public class AlloyMix {

    public final FluidStack result;
    public final List<FluidStack> mixers;

    public AlloyMix(FluidStack output, List<FluidStack> inputs) {
        result = output;
        mixers = inputs;
    }

    /*
     * public boolean matches(List liquids) { ArrayList list = new ArrayList(mixers); return false; }
     */

    public FluidStack mix(ArrayList<FluidStack> liquids) {
        ArrayList<FluidStack> copyMix = new ArrayList<>(mixers);
        ArrayList<Integer> effectiveAmount = new ArrayList<>();

        for (FluidStack liquid : liquids) {
            for (FluidStack mixer : copyMix) {
                if (mixer.isFluidEqual(liquid)) {
                    // do we actually have enough of that liquid?
                    if (liquid.amount < mixer.amount) break;

                    int eAmt = liquid.amount / mixer.amount;
                    effectiveAmount.add(eAmt);
                    copyMix.remove(mixer);
                    break;
                }
            }
        }

        if (copyMix.size() > 0) return null;

        // Remove old liquids
        int low = getLowestAmount(effectiveAmount);
        ArrayList<FluidStack> copyMix2 = new ArrayList<>(mixers);

        for (int i = 0; i < liquids.size(); i++) {
            FluidStack liquid = liquids.get(i);
            for (FluidStack mixer : copyMix2) {
                // if (mixer.itemID == liquid.itemID && mixer.itemMeta ==
                // liquid.itemMeta)
                if (mixer.isFluidEqual(liquid)) {
                    int eAmt = low * mixer.amount;
                    liquid.amount -= eAmt;
                    if (liquid.amount <= 0) {
                        liquids.remove(liquid);
                        i--;
                    }
                    copyMix2.remove(mixer);
                    break;
                }
            }
        }

        FluidStack ret = result.copy();
        ret.amount *= low;
        return ret;
    }

    int getLowestAmount(ArrayList<Integer> list) {
        int first = list.get(0);
        for (int i = 1; i < list.size(); i++) {
            int compare = list.get(i);
            if (first > compare) {
                first = compare;
            }
        }
        return first;
    }
}
