package net.ciebus.pinmod.common.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.ciebus.pinmod.Pin;
import net.ciebus.pinmod.PinManager;

/**
 * 这个 handler 会被专用服务器加载（PacketHandler 在双端注册），
 * 因此这里不能出现任何客户端专用类型（Minecraft / EntityClientPlayerMP 等），
 * 维度判断交给 Pin.proxy.shouldAcceptPin() 在客户端实现里完成。
 */
public class CMessageKeyPressedHandler implements IMessageHandler<MessageKeyPressed, IMessage> {

    @Override
    public IMessage onMessage(MessageKeyPressed message, MessageContext ctx) {
        if (!message.state) {
            // 不同维度（包括私人维度）互不可见、互不发声
            if (!Pin.proxy.shouldAcceptPin(message)) {
                return null;
            }

            PinManager.addPin(message.x, message.y, message.z,
                    message.playerName, message.dimId,
                    message.remainingMs, message.lifetimeMs);

            if (message.notify) {
                Pin.proxy.playPinSound(message.playerName);
            }
        } else {
            PinManager.removePin(message.playerName);
        }
        return null;
    }
}
