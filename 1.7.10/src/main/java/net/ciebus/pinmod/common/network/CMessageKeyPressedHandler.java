package net.ciebus.pinmod.common.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.ciebus.pinmod.Pin;
import net.ciebus.pinmod.PinManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

public class CMessageKeyPressedHandler implements IMessageHandler<MessageKeyPressed, IMessage> {

    @Override
    public IMessage onMessage(MessageKeyPressed message, MessageContext ctx) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;

        if (!message.state) {
            // 只接收当前维度的标记：不同维度（包括私人维度）互不可见、互不发声
            if (player == null || player.worldObj == null
                    || player.worldObj.provider.dimensionId != message.dimId) {
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
