package tconstruct.smeltery.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.common.Loader;
import tconstruct.TConstruct;
import tconstruct.smeltery.inventory.ActiveContainer;
import tconstruct.smeltery.inventory.SmelteryContainer;
import tconstruct.smeltery.logic.SmelteryLogic;
import tconstruct.util.network.SmelteryPacket;

public class SmelteryGui extends ActiveContainerGui {

    public SmelteryLogic logic;
    private boolean isScrolling = false;
    private boolean wasClicking;
    private float currentScroll = 0.0F;
    private int slotPos = 0;
    private int prevSlotPos = 0;

    private final int columns;
    private final int smelterySize;
    public static final int maxRows = 8;

    public SmelteryGui(InventoryPlayer inventoryplayer, SmelteryLogic smeltery, World world, int x, int y, int z) {
        super((ActiveContainer) smeltery.getGuiContainer(inventoryplayer, world, x, y, z));
        logic = smeltery;
        smelterySize = smeltery.getBlockCapacity();
        smeltery.updateFuelDisplay();

        columns = ((SmelteryContainer) this.inventorySlots).columns;
        xSize = 254 + (columns - 3) * 22; // Adjust for column count
    }

    @Override
    public void initGui() {
        super.initGui();

        if (logic != null) logic.updateFuelDisplay();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float par3) {
        // the smeltery changed. we close the screen because updating the changes in all containers would be way too
        // complicated.
        if (logic.getBlockCapacity() != smelterySize) {
            mc.thePlayer.closeScreen();
            return;
        }

        super.drawScreen(mouseX, mouseY, par3);
        updateScrollbar(mouseX, mouseY, par3);
    }

    protected void updateScrollbar(int mouseX, int mouseY, float par3) {
        if (smelterySize > columns * maxRows) {
            boolean mouseDown = Mouse.isButtonDown(0);
            int lefto = this.guiLeft;
            int topo = this.guiTop;
            int xScroll = lefto + 67 + (columns - 3) * 22; // Adjust for column count
            int yScroll = topo + 8;
            int scrollWidth = xScroll + 14;
            int scrollHeight = yScroll + 144;

            if (!this.wasClicking && mouseDown
                    && mouseX >= xScroll
                    && mouseY >= yScroll
                    && mouseX < scrollWidth
                    && mouseY < scrollHeight) {
                this.isScrolling = true;
            }

            if (!mouseDown) {
                this.isScrolling = false;
            }

            if (wasClicking && !isScrolling && slotPos != prevSlotPos) {
                prevSlotPos = slotPos;
            }

            this.wasClicking = mouseDown;

            if (this.isScrolling) {
                this.currentScroll = (mouseY - yScroll - 7.5F) / (scrollHeight - yScroll - 15.0F);

                if (this.currentScroll < 0.0F) {
                    this.currentScroll = 0.0F;
                }

                if (this.currentScroll > 1.0F) {
                    this.currentScroll = 1.0F;
                }

                int s = ((SmelteryContainer) this.inventorySlots).scrollTo(this.currentScroll);
                if (s != -1) slotPos = s;
            }
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        int baseX = 86 + (columns - 3) * 22;
        fontRendererObj.drawString(StatCollector.translateToLocal("crafters.Smeltery"), baseX, 5, 0x404040);
        fontRendererObj.drawString(
                StatCollector.translateToLocal("container.inventory"),
                baseX + 4,
                (ySize - 96) + 2,
                0x404040);

        int cornerX = (width - xSize) / 2 + 36 + (columns - 3) * 22;
        int cornerY = (height - ySize) / 2;

        int[] fluidHeights = calcLiquidHeights();
        int base = 0;
        for (int i = 0; i < fluidHeights.length; i++) {
            int leftX = cornerX + 54;
            int topY = (cornerY + 68) - fluidHeights[i] - base;

            if (mouseX >= leftX && mouseX <= leftX + 52 && mouseY >= topY && mouseY < topY + fluidHeights[i]) {
                drawFluidStackTooltip(
                        getLiquidTooltip(logic.moltenMetal.get(i)),
                        mouseX - cornerX + 36,
                        mouseY - cornerY);
            }
            base += fluidHeights[i];
        }

        // lava/fuel
        if (logic.fuelGague > 0) {
            int leftX = cornerX + 117;
            int topY = (cornerY + 68) - logic.getScaledFuelGague(52);
            int sizeX = 12;
            int sizeY = logic.getScaledFuelGague(52);
            if (mouseX >= leftX && mouseX <= leftX + sizeX && mouseY >= topY && mouseY < topY + sizeY) {
                drawFluidStackTooltip(getFuelTooltip(logic.getFuel()), mouseX - cornerX + 36, mouseY - cornerY);
            }
        }
    }

    private static final ResourceLocation background = new ResourceLocation("tinker", "textures/gui/smeltery.png");
    private static final ResourceLocation backgroundSide = new ResourceLocation(
            "tinker",
            "textures/gui/smelteryside.png");

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(background);
        int cornerX = (width - xSize) / 2 + 36 + (columns - 3) * 22;
        int cornerY = (height - ySize) / 2;
        drawTexturedModalRect(cornerX + 46, cornerY, 0, 0, 176, ySize);

        // Fuel - Lava
        this.mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        if (logic.fuelGague > 0) {
            FluidStack fuelStack = logic.getFuel();
            IIcon lavaIcon = fuelStack.getFluid().getStillIcon();
            if (lavaIcon == null) lavaIcon = Blocks.lava.getIcon(0, 0);
            int fuel = logic.getScaledFuelGague(52);
            int count = 0;
            while (fuel > 0) {
                int size = Math.min(fuel, 16);
                fuel -= size;
                drawLiquidRect(
                        cornerX + 117,
                        (cornerY + 68) - size - 16 * count,
                        lavaIcon,
                        12,
                        size,
                        fuelStack.getFluid().getColor(fuelStack));
                count++;
            }
        }

        if (logic.getCapacity() > 0) {
            // Liquids - molten metal
            int base = 0;
            int[] fluidHeights = calcLiquidHeights();

            // render the fluids
            int basePos = 54;
            for (int i = 0; i < logic.moltenMetal.size(); i++) {
                FluidStack liquid = logic.moltenMetal.get(i);
                IIcon icon = liquid.getFluid().getStillIcon();
                int color = liquid.getFluid().getColor(liquid);

                if (icon == null) continue;

                int height = fluidHeights[i];
                int h = height;
                while (h > 0) {
                    int v = Math.min(16, h);
                    // we render in 16x16 squares so the texture doesn't get distorted
                    drawLiquidRect(cornerX + basePos + 00, (cornerY + 68) - h - base, icon, 16, v, color);
                    drawLiquidRect(cornerX + basePos + 16, (cornerY + 68) - h - base, icon, 16, v, color);
                    drawLiquidRect(cornerX + basePos + 32, (cornerY + 68) - h - base, icon, 16, v, color);
                    drawLiquidRect(cornerX + basePos + 48, (cornerY + 68) - h - base, icon, 4, v, color);
                    h -= 16;
                }
                base += height;
            }
        }

        // Liquid gague
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        this.mc.getTextureManager().bindTexture(background);
        drawTexturedModalRect(cornerX + 54, cornerY + 16, 176, 76, 52, 52);

        // Side inventory
        int xleft = 46;
        xleft += 22 * (columns - 3); // we have to shift the whole thing to the left if we have more than 3 columns
        int h = smelterySize / columns;
        if (smelterySize % columns != 0) h++;

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(backgroundSide);
        if (smelterySize > 0) {
            if (h >= 8) {
                // standard 3 slots
                drawTexturedModalRect(cornerX - xleft, cornerY, 0, 0, 72, ySize - 8);
                // additional slots
                for (int i = 0; i < columns - 3; i++)
                    drawTexturedModalRect(cornerX - xleft + 72 + i * 22, cornerY, 50, 0, 22, ySize - 8);
                // right end
                drawTexturedModalRect(cornerX - 46 + 72, cornerY, 72, 0, 25, ySize - 8);

                int sx = cornerX + 32;
                int sy = (int) (cornerY + 8 + 127 * currentScroll);
                // highlighted scroll bar
                if (isScrolling || (mouseX >= sx && mouseX <= sx + 12 && mouseY >= sy && mouseY <= sy + 15))
                    drawTexturedModalRect(sx, sy, 122, 0, 12, 15);
                // scroll bar
                else drawTexturedModalRect(sx, sy, 98, 0, 12, 15);
            } else {
                int yd = 43 + 18 * (h - 3);
                // slots
                // standard 3 slots
                drawTexturedModalRect(cornerX - xleft, cornerY, 0, 0, 72, yd);
                // additional slots
                for (int i = 0; i < columns - 3; i++)
                    drawTexturedModalRect(cornerX - xleft + 72 + i * 22, cornerY, 50, 0, 22, yd);
                // right end
                drawTexturedModalRect(cornerX - 46 + 72, cornerY, 72, 0, 25, yd);

                // bottom end
                drawTexturedModalRect(cornerX - xleft, cornerY + yd, 0, 133, 72, 25);
                for (int i = 0; i < columns - 3; i++)
                    drawTexturedModalRect(cornerX - xleft + 72 + i * 22, cornerY + yd, 50, 133, 22, 25);
                drawTexturedModalRect(cornerX - 46 + 72, cornerY + yd, 72, 133, 25, 25);

                // grayed out scroll bar
                drawTexturedModalRect(cornerX + 32, (int) (cornerY + 8 + 127 * currentScroll), 110, 0, 12, 15);
            }
        }

        xleft -= 8;
        // Temperature
        int slotSize = smelterySize;
        if (slotSize > columns * maxRows) slotSize = columns * maxRows;
        int iter;
        for (iter = 0; iter < slotSize && iter + slotPos * columns < smelterySize; iter++) {
            int slotTemp = logic.getTempForSlot(iter + slotPos * columns) - 20;
            int maxTemp = logic.getMeltingPointForSlot(iter + slotPos * columns) - 20;
            if (slotTemp > 0 && maxTemp > 0) {
                int size = 16 * slotTemp / maxTemp + 1;
                drawTexturedModalRect(
                        cornerX - xleft + (iter % columns * 22),
                        cornerY + 8 + (iter / columns * 18) + 16 - size,
                        98,
                        15 + 16 - size,
                        5,
                        size);
            }
        }

        // hide nonexistant slots
        int maxSlots = Math.min(maxRows, h) * columns;
        for (; iter < maxSlots; iter++) {
            drawTexturedModalRect(
                    cornerX - xleft + (iter % columns * 22) - 1,
                    cornerY + 8 + (iter / columns * 18) - 1,
                    98,
                    47,
                    22,
                    18);
        }
    }

    protected int[] calcLiquidHeights() {
        int[] fluidHeights = new int[logic.moltenMetal.size()];
        int cap = logic.getCapacity();
        if (logic.getTotalLiquid() > cap) cap = logic.getTotalLiquid();
        for (int i = 0; i < logic.moltenMetal.size(); i++) {
            FluidStack liquid = logic.moltenMetal.get(i);

            float h = (float) liquid.amount / (float) cap;
            fluidHeights[i] = Math.max(3, (int) Math.ceil(h * 52f));
        }

        // check if we have enough height to render everything
        int sum;
        do {
            sum = 0;
            int biggest = -1;
            int m = 0;
            for (int i = 0; i < fluidHeights.length; i++) {
                sum += fluidHeights[i];
                if (logic.moltenMetal.get(i).amount > biggest) {
                    biggest = logic.moltenMetal.get(i).amount;
                    m = i;
                }
            }

            // remove a pixel from the biggest one
            if (sum > 52) fluidHeights[m]--;
        } while (sum > 52);

        return fluidHeights;
    }

    protected void drawFluidStackTooltip(List<String> tooltip, int x, int y) {
        this.zLevel = 100;
        for (int k = 0; k < tooltip.size(); ++k) {
            tooltip.set(k, EnumChatFormatting.GRAY + tooltip.get(k));
        }
        this.drawToolTip(tooltip, x, y);
        this.zLevel = 0;
    }

    private List<String> getFuelTooltip(FluidStack liquid) {
        ArrayList<String> list = new ArrayList<>();
        list.add("\u00A7f" + StatCollector.translateToLocal("gui.smeltery.fuel"));
        list.add("mB: " + liquid.amount);
        return list;
    }

    private List<String> getLiquidTooltip(FluidStack liquid) {
        ArrayList<String> list = new ArrayList<>();
        String name = liquid.getFluid().getLocalizedName(liquid);
        list.add("\u00A7f" + name);
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            list.add("mB: " + liquid.amount);
        } else if (name.equals(StatCollector.translateToLocal("fluid.emerald.liquid"))) {
            list.add(StatCollector.translateToLocal("gui.smeltery.emerald") + liquid.amount / 640f);
        } else if (name.equals(StatCollector.translateToLocal("fluid.quartz.molten"))) {
            list.add(StatCollector.translateToLocal("gui.smeltery.quartz") + liquid.amount / 250f);
        } else if (name.equals(StatCollector.translateToLocal("fluid.glass.molten"))) {
            int blocks = liquid.amount / 1000;
            if (blocks > 0) list.add(StatCollector.translateToLocal("gui.smeltery.glass.block") + blocks);
            int panels = (liquid.amount % 1000) / 250;
            if (panels > 0) list.add(StatCollector.translateToLocal("gui.smeltery.glass.pannel") + panels);
            int mB = (liquid.amount % 1000) % 250;
            if (mB > 0) list.add("mB: " + mB);
        } else if (name.equals(StatCollector.translateToLocal("fluid.stone.seared"))) {
            if (Loader.isModLoaded("dreamcraft")) {
                int blocks = liquid.amount / 360; // in gtnh each seared stone block is 360 mb of fluid
                if (blocks > 0) list.add(StatCollector.translateToLocal("gui.smeltery.glass.block") + blocks);
                // we also have no casting recipe for seared bricks
                int mB = liquid.amount % 360;
                if (mB > 0) list.add("mB: " + mB);
            } else {
                int blocks = liquid.amount / TConstruct.ingotLiquidValue;
                if (blocks > 0) list.add(StatCollector.translateToLocal("gui.smeltery.glass.block") + blocks);
                int ingots = (liquid.amount % TConstruct.ingotLiquidValue) / (TConstruct.ingotLiquidValue / 4);
                if (ingots > 0) list.add(StatCollector.translateToLocal("gui.smeltery.metal.ingot") + ingots);
                int mB = (liquid.amount % TConstruct.ingotLiquidValue) % (TConstruct.ingotLiquidValue / 4);
                if (mB > 0) list.add("mB: " + mB);
            }
        } else if (isMolten(name)) {
            int ingots = liquid.amount / TConstruct.ingotLiquidValue;
            if (ingots > 0) list.add(StatCollector.translateToLocal("gui.smeltery.metal.ingot") + ingots);
            int mB = liquid.amount % TConstruct.ingotLiquidValue;
            if (mB > 0) {
                int nuggets = mB / TConstruct.nuggetLiquidValue;
                int junk = (mB % TConstruct.nuggetLiquidValue);
                if (nuggets > 0) list.add(StatCollector.translateToLocal("gui.smeltery.metal.nugget") + nuggets);
                if (junk > 0) list.add("mB: " + junk);
            }
        } else {
            list.add("mB: " + liquid.amount);
        }
        return list;
    }

    private boolean isMolten(String fluidName) {
        boolean molten = false;
        String[] moltenNames = StatCollector.translateToLocal("gui.smeltery.molten.check").split(",");
        for (String moltenName : moltenNames) {
            if (fluidName.contains(moltenName.trim())) {
                molten = true;
                break;
            }
        }
        return molten;
    }

    private void drawToolTip(List<String> tooltip, int x, int y) {
        if (!tooltip.isEmpty()) {
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            int k = 0;

            for (String s : tooltip) {
                int l = this.fontRendererObj.getStringWidth(s);
                if (l > k) {
                    k = l;
                }
            }

            int i1 = x + 12;
            int j1 = y - 12;
            int k1 = 8;

            if (tooltip.size() > 1) {
                k1 += 2 + (tooltip.size() - 1) * 10;
            }

            if (i1 + k > this.width) {
                i1 -= 28 + k;
            }

            if (j1 + k1 + 6 > this.height) {
                j1 = this.height - k1 - 6;
            }

            this.zLevel = 300.0F;
            itemRender.zLevel = 300.0F;
            int l1 = -267386864;
            this.drawGradientRect(i1 - 3, j1 - 4, i1 + k + 3, j1 - 3, l1, l1);
            this.drawGradientRect(i1 - 3, j1 + k1 + 3, i1 + k + 3, j1 + k1 + 4, l1, l1);
            this.drawGradientRect(i1 - 3, j1 - 3, i1 + k + 3, j1 + k1 + 3, l1, l1);
            this.drawGradientRect(i1 - 4, j1 - 3, i1 - 3, j1 + k1 + 3, l1, l1);
            this.drawGradientRect(i1 + k + 3, j1 - 3, i1 + k + 4, j1 + k1 + 3, l1, l1);
            int i2 = 1347420415;
            int j2 = (i2 & 16711422) >> 1 | i2 & -16777216;
            this.drawGradientRect(i1 - 3, j1 - 3 + 1, i1 - 3 + 1, j1 + k1 + 3 - 1, i2, j2);
            this.drawGradientRect(i1 + k + 2, j1 - 3 + 1, i1 + k + 3, j1 + k1 + 3 - 1, i2, j2);
            this.drawGradientRect(i1 - 3, j1 - 3, i1 + k + 3, j1 - 3 + 1, i2, i2);
            this.drawGradientRect(i1 - 3, j1 + k1 + 2, i1 + k + 3, j1 + k1 + 3, j2, j2);

            for (int k2 = 0; k2 < tooltip.size(); ++k2) {
                String s1 = tooltip.get(k2);
                this.fontRendererObj.drawStringWithShadow(s1, i1, j1, -1);

                if (k2 == 0) {
                    j1 += 2;
                }

                j1 += 10;
            }

            this.zLevel = 0.0F;
            itemRender.zLevel = 0.0F;
            RenderHelper.enableGUIStandardItemLighting();
        }
    }

    public void drawLiquidRect(int startU, int startV, IIcon icon, int endU, int endV, int color) {
        float top = icon.getInterpolatedV(16 - endV);
        float bottom = icon.getMaxV();
        float left = icon.getMinU();
        float right = icon.getInterpolatedU(endU);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setColorOpaque_I(color);
        tessellator.addVertexWithUV(startU + 0, startV + endV, this.zLevel, left, bottom); // Bottom left
        tessellator.addVertexWithUV(startU + endU, startV + endV, this.zLevel, right, bottom); // Bottom right
        tessellator.addVertexWithUV(startU + endU, startV + 0, this.zLevel, right, top); // Top right
        tessellator.addVertexWithUV(startU + 0, startV + 0, this.zLevel, left, top); // Top left
        tessellator.draw();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        int cornerX = (width - xSize) / 2 + 36 + (columns - 3) * 22;
        int cornerY = (height - ySize) / 2;
        int fluidToBeBroughtUp = -1;

        int[] fluidHeights = calcLiquidHeights();
        int base = 0;
        for (int i = 0; i < fluidHeights.length; i++) {
            int leftX = cornerX + 54;
            int topY = (cornerY + 68) - fluidHeights[i] - base;

            if (mouseX >= leftX && mouseX <= leftX + 52 && mouseY >= topY && mouseY < topY + fluidHeights[i]) {
                fluidToBeBroughtUp = logic.moltenMetal.get(i).getFluidID();

                TConstruct.packetPipeline.sendToServer(
                        new SmelteryPacket(
                                logic.getWorldObj().provider.dimensionId,
                                logic.xCoord,
                                logic.yCoord,
                                logic.zCoord,
                                isShiftKeyDown(),
                                fluidToBeBroughtUp));
            }
            base += fluidHeights[i];
        }
    }
}
