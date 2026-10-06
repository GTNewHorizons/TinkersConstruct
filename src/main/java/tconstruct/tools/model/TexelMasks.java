package tconstruct.tools.model;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.IdentityHashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Which texels of an icon draw, read once from its texture file. */
@SideOnly(Side.CLIENT)
public final class TexelMasks {

    private TexelMasks() {}

    /** Vanilla's alpha test keeps alpha above 0.1: a texel draws from this alpha byte up. */
    public static final int OPAQUE = 26;
    private static final boolean[] UNREADABLE = new boolean[0];

    /** By icon, at the icon's own size; cleared on a restitch. */
    private static final Map<IIcon, boolean[]> MASKS = new IdentityHashMap<>();

    static void clear() {
        MASKS.clear();
    }

    /** Whether hole sits wholly inside mask: they overlap, and the overlap has mask set on all eight sides. */
    static boolean inset(boolean[] hole, boolean[] mask, int width, int height) {
        boolean overlap = false;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!hole[y * width + x] || !mask[y * width + x]) continue;
                overlap = true;
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        int nx = x + dx, ny = y + dy;
                        if (nx < 0 || ny < 0 || nx >= width || ny >= height || !mask[ny * width + nx]) return false;
                    }
                }
            }
        }
        return overlap;
    }

    /** A flipped icon is mirrored as it is drawn; an animated strip gives its first frame. */
    private static boolean[] maskOf(BufferedImage image, int width, int height, boolean flipU, boolean flipV) {
        int w = image.getWidth(), h = Math.min(image.getHeight(), w);
        boolean[] mask = new boolean[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int px = (flipU ? width - 1 - x : x) * w / width, py = (flipV ? height - 1 - y : y) * h / height;
                mask[y * width + x] = (image.getRGB(px, py) >>> 24) >= OPAQUE;
            }
        }
        return mask;
    }

    private static boolean[] spread(boolean[] own, int w, int h, int width, int height) {
        boolean[] grid = new boolean[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) grid[y * width + x] = own[(y * h / height) * w + x * w / width];
        }
        return grid;
    }

    /** The icon's mask on a width x height grid; null when its texture cannot be read. */
    static boolean[] onGrid(IIcon icon, int width, int height) {
        int w = icon.getIconWidth(), h = icon.getIconHeight();
        if (w <= 0 || h <= 0) return null;
        boolean[] own = MASKS.get(icon);
        if (own == null || (own != UNREADABLE && own.length != w * h)) {
            own = readTexels(icon, w, h);
            MASKS.put(icon, own);
        }
        if (own == UNREADABLE) return null;
        return w == width && h == height ? own : spread(own, w, h, width, height);
    }

    /** From the texture file, since a stitched icon keeps no pixels. */
    private static boolean[] readTexels(IIcon icon, int width, int height) {
        String name = icon.getIconName();
        if (name == null) return UNREADABLE;
        int colon = name.indexOf(':');
        ResourceLocation texture = new ResourceLocation(
                colon < 0 ? "minecraft" : name.substring(0, colon),
                "textures/items/" + name.substring(colon + 1) + ".png");
        try (InputStream in = Minecraft.getMinecraft().getResourceManager().getResource(texture).getInputStream()) {
            BufferedImage image = ImageIO.read(in);
            if (image == null) return UNREADABLE;
            return maskOf(image, width, height, icon.getMinU() > icon.getMaxU(), icon.getMinV() > icon.getMaxV());
        } catch (IOException | RuntimeException e) {
            return UNREADABLE;
        }
    }
}
