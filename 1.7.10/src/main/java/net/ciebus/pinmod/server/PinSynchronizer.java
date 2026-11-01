package net.ciebus.pinmod.server;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import net.ciebus.pinmod.PinManager;
import net.ciebus.pinmod.common.data.PinData;
import net.ciebus.pinmod.common.network.MessageKeyPressed;
import net.ciebus.pinmod.common.network.PacketHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;

import java.util.List;

public class PinSynchronizer {

    /**
     * 只把消息发给与 dimId 同维度的玩家。
     * 私人维度（例如 GTNH 里给单个玩家的独立维度）之间必须完全隔离，
     * 否则别的维度会听到提示音却看不到标记。
     */
    public static void sendToDimension(MessageKeyPressed message, int dimId) {
        MinecraftServer server = MinecraftServer.getServer();
        if (server == null) {
            return;
        }

        List<EntityPlayerMP> players = server.getConfigurationManager().playerEntityList;
        for (EntityPlayerMP target : players) {
            if (target != null && target.worldObj != null
                    && target.worldObj.provider.dimensionId == dimId) {
                PacketHandler.INSTANCE1.sendTo(message, target);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.player instanceof EntityPlayerMP)) {
            return;
        }

        EntityPlayerMP player = (EntityPlayerMP) event.player;
        int playerDim = player.worldObj.provider.dimensionId;

        for (PinData pin : PinManager.pins()) {
            // 跨维度的标记不下发，玩家切维度时由客户端自行清理
            if (pin.dimId != playerDim) {
                continue;
            }

            int remaining = (int) Math.max(0L, pin.getRemainingMs());
            if (remaining <= 0) {
                continue;
            }

            MessageKeyPressed pindata = new MessageKeyPressed(
                    false, pin.x, pin.y, pin.z, pin.player, pin.dimId,
                    false, remaining, pin.lifetimeMs);
            PacketHandler.INSTANCE1.sendTo(pindata, player);
        }
    }

    @SubscribeEvent
    public void onPlayerLeaved(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        PinManager.clearPins();
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        for (PinData pin : PinManager.removeExpired()) {
            sendToDimension(new MessageKeyPressed(
                    true, pin.x, pin.y, pin.z, pin.player, pin.dimId,
                    false, 0, 0), pin.dimId);
        }
    }

    public static void init() {
        MinecraftForge.EVENT_BUS.register(new PinSynchronizer());
        FMLCommonHandler.instance().bus().register(new PinSynchronizer());
    }
}
