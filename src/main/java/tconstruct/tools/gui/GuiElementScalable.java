package tconstruct.tools.gui;

import net.minecraft.client.renderer.Tessellator;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/*
 * Taken from Mantle 1.12-1.3.3.49 under the MIT License The MIT License (MIT) Copyright (c) 2013-2014 Slime Knights
 * (mDiyo, fuj1n, Sunstrike, progwml6, pillbox, alexbegt) Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions: The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software. THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

@SideOnly(Side.CLIENT)
public class GuiElementScalable extends GuiElementDuex {

    public GuiElementScalable(int x, int y, int w, int h, int texW, int texH) {
        super(x, y, w, h, texW, texH);
    }

    public GuiElementScalable(int x, int y, int w, int h) {
        super(x, y, w, h);
    }

    public int drawScaledX(int xPos, int yPos, int width) {
        return drawScaled(xPos, yPos, width, h);
    }

    public int drawScaledY(int xPos, int yPos, int height) {
        drawScaled(xPos, yPos, w, height);
        return w;
    }

    public int drawScaled(int xPos, int yPos, int width, int height) {
        if (width <= 0 || height <= 0) return width;

        Tessellator tessellator = Tessellator.instance;
        int tileWidth = w == 1 ? width : w;
        int tileHeight = h == 1 ? height : h;
        float minU = (float) x / texW;
        float minV = (float) y / texH;

        if (!batching) tessellator.startDrawingQuads();
        for (int yOffset = 0; yOffset < height; yOffset += tileHeight) {
            int drawHeight = Math.min(tileHeight, height - yOffset);
            float maxV = (float) (y + Math.min(h, drawHeight)) / texH;
            int top = yPos + yOffset;
            for (int xOffset = 0; xOffset < width; xOffset += tileWidth) {
                int drawWidth = Math.min(tileWidth, width - xOffset);
                float maxU = (float) (x + Math.min(w, drawWidth)) / texW;
                int left = xPos + xOffset;
                tessellator.addVertexWithUV(left, top + drawHeight, 0, minU, maxV);
                tessellator.addVertexWithUV(left + drawWidth, top + drawHeight, 0, maxU, maxV);
                tessellator.addVertexWithUV(left + drawWidth, top, 0, maxU, minV);
                tessellator.addVertexWithUV(left, top, 0, minU, minV);
            }
        }
        if (!batching) tessellator.draw();
        return width;
    }

    @Override
    public GuiElementScalable shift(int xd, int yd) {
        GuiElementScalable element = new GuiElementScalable(this.x + xd, this.y + yd, this.w, this.h);
        element.setTextureSize(texW, texH);
        return element;
    }
}
