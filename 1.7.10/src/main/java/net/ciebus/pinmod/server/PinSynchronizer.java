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
import net.minecraftforge.common.MinecraftForge;

public class PinSynchronizer {

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.player instanceof EntityPlayerMP)) {
            return;
        }

        EntityPlayerMP player = (EntityPlayerMP) event.player;
        for (PinData pin : PinManager.pins()) {
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
            PacketHandler.INSTANCE1.sendToAll(new MessageKeyPressed(
                    true, pin.x, pin.y, pin.z, pin.player, pin.dimId,
                    false, 0, 0));
        }
    }

    public static void init() {
        MinecraftForge.EVENT_BUS.register(new PinSynchronizer());
        FMLCommonHandler.instance().bus().register(new PinSynchronizer());
    }
}
