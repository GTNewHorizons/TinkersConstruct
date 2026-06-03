package tconstruct.library.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.FallbackResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.util.ResourceLocation;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

// Stolen from CofHLib RegistryUtil
public final class TextureHelper {

    private TextureHelper() {}

    @SideOnly(Side.CLIENT)
    public static boolean textureExists(ResourceLocation texture) {
        final IResourceManager resMan = Minecraft.getMinecraft().getResourceManager();
        if (resMan instanceof SimpleReloadableResourceManager simple) {
            FallbackResourceManager fallback = simple.domainResourceManagers.get(texture.getResourceDomain());
            if (fallback != null) {
                for (IResourcePack rp : fallback.resourcePacks) {
                    if (rp != null && rp.resourceExists(texture)) {
                        return true;
                    }
                }
                return false;
            }
        }

        // Fallback
        try {
            resMan.getAllResources(texture);
            return true;
        } catch (Throwable t) { // pokemon!
            return false;
        }
    }

    @SideOnly(Side.CLIENT)
    public static boolean textureExists(String texture) {

        return textureExists(new ResourceLocation(texture));
    }

    @SideOnly(Side.CLIENT)
    public static boolean blockTextureExists(String texture) {

        int i = texture.indexOf(':');

        if (i > 0) {
            texture = texture.substring(0, i) + ":textures/blocks/" + texture.substring(i + 1);
        } else {
            texture = "textures/blocks/" + texture;
        }
        return textureExists(texture + ".png");
    }

    @SideOnly(Side.CLIENT)
    public static boolean itemTextureExists(String texture) {

        int i = texture.indexOf(':');

        if (i > 0) {
            texture = texture.substring(0, i) + ":textures/items/" + texture.substring(i + 1);
        } else {
            texture = "textures/items/" + texture;
        }
        return textureExists(texture + ".png");
    }
}
