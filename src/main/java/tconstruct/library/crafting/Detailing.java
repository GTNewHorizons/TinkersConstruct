package tconstruct.library.crafting;

import java.util.*;

import net.minecraft.block.Block;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;

import tconstruct.library.tools.ToolCore;
import cpw.mods.fml.common.registry.GameRegistry;

public class Detailing {

    public List<DetailInput> detailing = new ArrayList<>();

    public void addDetailing(Object input, int inputMeta, Object output, int outputMeta, ToolCore tool) {
        ItemStack iID, oID;

        if (input instanceof Block) iID = new ItemStack(((Block) input));
        else if (input instanceof Item) iID = new ItemStack(((Item) input));
        else if (input instanceof ItemStack) iID = ((ItemStack) input);
        else throw new RuntimeException("Invalid detail input!");

        if (output instanceof Block) oID = new ItemStack((Block) output);
        else if (output instanceof Item) oID = new ItemStack((Item) output);
        else if (output instanceof ItemStack) oID = (ItemStack) output;
        else throw new RuntimeException("Invalid detail output!");

        this.addDetailing(new DetailInput(iID, inputMeta, oID, outputMeta), tool);
    }

    public void addDetailing(DetailInput details, ToolCore tool) {
        detailing.add(details);
        ItemStack toolstack = new ItemStack(tool, 1, Short.MAX_VALUE);

        NBTTagCompound compound = new NBTTagCompound();
        NBTTagCompound toolTag = new NBTTagCompound();
        toolTag.setInteger("RenderHandle", 0);
        toolTag.setInteger("RenderHead", 2);
        toolTag.setInteger("RenderAccessory", 2);
        toolTag.setInteger("Damage", 0);
        toolTag.setInteger("TotalDurability", 100);
        compound.setTag("InfiTool", toolTag);
        toolstack.setTagCompound(compound);
        addShapelessToolRecipe(
                new ItemStack(details.output.getItem(), 1, details.outputMeta),
                toolstack,
                new ItemStack(details.input.getItem(), 1, details.inputMeta));
    }

    public void addShapelessToolRecipe(ItemStack par1ItemStack, Object... par2ArrayOfObj) {
        ArrayList<ItemStack> arraylist = new ArrayList<>();
        for (Object object1 : par2ArrayOfObj) {
            if (object1 instanceof ItemStack) {
                arraylist.add(((ItemStack) object1).copy());
            } else if (object1 instanceof Item) {
                arraylist.add(new ItemStack((Item) object1));
            } else {
                if (!(object1 instanceof Block)) {
                    throw new RuntimeException("Invalid shapeless tool recipe!");
                }

                arraylist.add(new ItemStack((Block) object1));
            }
        }
        GameRegistry.addRecipe(new ShapelessToolRecipe(par1ItemStack, arraylist));
    }

    public DetailInput getDetailing(Block block, int inputMeta) {
        for (DetailInput detail : detailing) {
            if (Item.getItemFromBlock(block) == detail.input.getItem() && inputMeta == detail.inputMeta) {
                return detail;
            }
        }
        return null;
    }

    public static class DetailInput {

        public ItemStack input;
        public int inputMeta;
        public ItemStack output;
        public int outputMeta;

        public DetailInput(ItemStack input, int inputMeta, ItemStack output, int outputMeta) {
            this.input = input;
            this.inputMeta = inputMeta;
            this.output = output;
            this.outputMeta = outputMeta;
        }
    }
}
