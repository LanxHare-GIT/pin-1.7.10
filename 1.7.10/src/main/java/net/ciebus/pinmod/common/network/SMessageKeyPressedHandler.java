package net.ciebus.pinmod.common.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.ciebus.pinmod.PinManager;
import net.ciebus.pinmod.common.config.PinConfig;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SMessageKeyPressedHandler implements IMessageHandler<MessageKeyPressed, IMessage> {

    private static final long PLACE_COOLDOWN_MS = 100L;
    private static final Map<String, Long> LAST_PLACEMENT =
            new ConcurrentHashMap<String, Long>();

    @Override
    public IMessage onMessage(MessageKeyPressed message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().playerEntity;
        if (player == null) {
            return null;
        }

        // 以服务端数据为准，防止客户端伪造玩家名和维度
        message.playerName = player.getDisplayName();
        message.dimId = player.worldObj.provider.dimensionId;

        // 客户端发来的都是真实按键操作，允许播放提示音
        message.notify = true;

        if (!message.state) {
            int viewDistanceChunks = MinecraftServer.getServer()
                    .getConfigurationManager()
                    .getViewDistance();

            double maxDistance = viewDistanceChunks * 16.0D;

            double eyeY = player.posY + player.getEyeHeight();
            double dx = message.x - player.posX;
            double dy = message.y - eyeY;
            double dz = message.z - player.posZ;

            if (dx * dx + dy * dy + dz * dz > maxDistance * maxDistance) {
                // Rejection is reported client-side as a HUD toast; the client only
                // sends placements that are inside its own view distance.
                return null;
            }

            long now = System.currentTimeMillis();
            Long last = LAST_PLACEMENT.get(message.playerName);
            if (last != null && now - last < PLACE_COOLDOWN_MS) {
                return null;
            }
            LAST_PLACEMENT.put(message.playerName, now);

            int lifetime = message.lifetimeMs;
            if (lifetime <= 0) {
                lifetime = PinConfig.getLifetimeMs();
            }
            lifetime = Math.max(1000, Math.min(30000, lifetime));

            message.lifetimeMs = lifetime;
            message.remainingMs = lifetime;
            PinManager.addPin(message.x, message.y, message.z,
                    message.playerName, message.dimId, message.remainingMs, message.lifetimeMs);
        } else {
            message.remainingMs = 0;
            message.lifetimeMs = 0;
            // 修复：删除时真正移除 Pin
            PinManager.removePin(message.playerName);
        }

        PacketHandler.INSTANCE1.sendToAll(message);
        return null;
    }
}
