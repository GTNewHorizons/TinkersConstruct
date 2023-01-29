package tconstruct.common;

import java.util.HashMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import cpw.mods.fml.common.network.IGuiHandler;

public class TProxyCommon implements IGuiHandler {

    public void initialize() {}

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        IGuiHandler handler = serverGuiHandlers.get(ID);
        if (handler != null) return handler.getServerGuiElement(ID, player, world, x, y, z);
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        IGuiHandler handler = clientGuiHandlers.get(ID);
        if (handler != null) return handler.getClientGuiElement(ID, player, world, x, y, z);
        return null;
    }

    private static final HashMap<Integer, IGuiHandler> serverGuiHandlers = new HashMap<>();
    private static final HashMap<Integer, IGuiHandler> clientGuiHandlers = new HashMap<>();

    public static void registerServerGuiHandler(int gui, IGuiHandler handler) {
        serverGuiHandlers.put(gui, handler);
    }

    public static void registerClientGuiHandler(int gui, IGuiHandler handler) {
        clientGuiHandlers.put(gui, handler);
    }
}
