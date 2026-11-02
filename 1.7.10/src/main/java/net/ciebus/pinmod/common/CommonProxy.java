package net.ciebus.pinmod.common;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.ciebus.pinmod.common.config.PinConfig;
import net.ciebus.pinmod.common.network.MessageKeyPressed;
import net.ciebus.pinmod.common.network.PacketHandler;
import net.ciebus.pinmod.server.PinSynchronizer;

public class CommonProxy {

    public void onPreInit(FMLPreInitializationEvent event) {
        PinConfig.load(event);
        PacketHandler.init();
    }

    public void onInit(FMLInitializationEvent event) {
        PinSynchronizer.init();
    }

    public void playPinSound(String playerName) {
    }

    /**
     * 是否接受这条标记消息。
     * 默认接受；客户端会覆盖为“只接受当前维度的标记”，
     * 这样客户端专用的 Minecraft / EntityClientPlayerMP 引用不会出现在
     * 服务端也会加载的类里（否则专用服务器会 NoClassDefFoundError）。
     */
    public boolean shouldAcceptPin(MessageKeyPressed message) {
        return true;
    }

}
