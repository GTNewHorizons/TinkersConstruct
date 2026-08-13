package tconstruct.library.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;

/** Logic to keep track of the side of the block that was last hit. */
public class BlockSideHitListener {

    private static final Map<UUID, Integer> HIT_FACE = new HashMap<>();
    private static int clientSideHit = 1;
    private static boolean initialized = false;

    /** Initializes this listener. */
    public static void init() {
        if (initialized) return;

        initialized = true;
        BlockSideHitListener listener = new BlockSideHitListener();
        MinecraftForge.EVENT_BUS.register(listener);
        FMLCommonHandler.instance().bus().register(listener);
    }

    /** Called when the player left-clicks a block to store the face. */
    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent event) {
        if (event.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) {
            EntityPlayer player = event.entityPlayer;
            if (player.worldObj.isRemote) {
                clientSideHit = event.face;
            } else {
                HIT_FACE.put(player.getUniqueID(), event.face);
            }
        }
    }

    /** Called when a player leaves the server to clear the face. */
    @SubscribeEvent
    public void onLeaveServer(PlayerLoggedOutEvent event) {
        HIT_FACE.remove(event.player.getUniqueID());
    }

    /**
     * Gets the side this player last hit.
     *
     * @param player player whose last hit side is requested
     * @return side last hit
     */
    public static int getSideHit(EntityPlayer player) {
        if (player.worldObj.isRemote) return clientSideHit;
        Integer side = HIT_FACE.get(player.getUniqueID());
        return side == null ? 1 : side;
    }
}
