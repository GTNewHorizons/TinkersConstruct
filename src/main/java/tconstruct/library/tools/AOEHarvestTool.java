package tconstruct.library.tools;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;

import tconstruct.library.util.AoEExclusionList;

public abstract class AOEHarvestTool extends HarvestTool {

    public int breakRadius;
    public int breakDepth;

    public AOEHarvestTool(int baseDamage, int breakRadius, int breakDepth) {
        super(baseDamage);

        this.breakRadius = breakRadius;
        this.breakDepth = breakDepth;
    }

    protected String getAOEToolName() {
        return "tool." + getToolName().toLowerCase();
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, int x, int y, int z, EntityPlayer player) {
        // only effective materials matter. We don't want to aoe when breaking dirt with a hammer.
        String toolName = "tool." + getAOEToolName().toLowerCase();
        Block block = player.worldObj.getBlock(x, y, z);
        int meta = player.worldObj.getBlockMetadata(x, y, z);
        if (block == null || !isEffective(block, meta) || !stack.hasTagCompound())
            return super.onBlockStartBreak(stack, x, y, z, player);

        // tool broken?
        NBTTagCompound toolTags = stack.getTagCompound().getCompoundTag("InfiTool");
        if (toolTags == null || toolTags.getBoolean("Broken")) return super.onBlockStartBreak(stack, x, y, z, player);

        if (player.isSneaking()) return super.onBlockStartBreak(stack, x, y, z, player);

        MovingObjectPosition mop = AbilityHelper.raytraceFromEntity(player.worldObj, player, false, 4.5d);
        if (mop == null) return super.onBlockStartBreak(stack, x, y, z, player);
        int sideHit = mop.sideHit;
        // int sideHit = Minecraft.getMinecraft().objectMouseOver.sideHit;

        // we successfully destroyed a block. time to do AOE!
        int xRange = breakRadius;
        int yRange = breakRadius;
        int zRange = breakDepth;
        switch (sideHit) {
            case 0:
            case 1:
                yRange = breakDepth;
                zRange = breakRadius;
                break;
            case 2:
            case 3:
                xRange = breakRadius;
                zRange = breakDepth;
                break;
            case 4:
            case 5:
                xRange = breakDepth;
                zRange = breakRadius;
                break;
        }

        for (int xPos = x - xRange; xPos <= x + xRange; xPos++) for (int yPos = y - yRange; yPos <= y + yRange; yPos++)
            for (int zPos = z - zRange; zPos <= z + zRange; zPos++) {
                // don't break the originally already broken block, duh
                if (xPos == x && yPos == y && zPos == z) continue;

                Block targetBlock = player.worldObj.getBlock(xPos, yPos, zPos);
                int targetMeta = player.worldObj.getBlockMetadata(xPos, yPos, zPos);

                if (!AoEExclusionList.isBlockExcluded(toolName, targetBlock, targetMeta)) {
                    if (!super.onBlockStartBreak(stack, xPos, yPos, zPos, player))
                        breakExtraBlock(player.worldObj, xPos, yPos, zPos, sideHit, player, x, y, z);
                }
            }

        return super.onBlockStartBreak(stack, x, y, z, player);
    }
}
