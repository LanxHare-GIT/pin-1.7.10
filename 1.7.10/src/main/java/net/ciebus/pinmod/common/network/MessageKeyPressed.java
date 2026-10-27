package net.ciebus.pinmod.common.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import net.ciebus.pinmod.common.data.PinData;

public class MessageKeyPressed implements IMessage {

    public boolean state;
    public double x;
    public double y;
    public double z;
    public int dimId;
    public String playerName;

    /**
     * true  = 玩家正常按键操作，客户端可以播放提示音
     * false = 玩家进服时同步已有 Pin，不播放提示音
     */
    public boolean notify;

    public int remainingMs;
    public int lifetimeMs;

    public MessageKeyPressed() {
        this.notify = true;
        this.remainingMs = PinData.DEFAULT_LIFETIME_MS;
        this.lifetimeMs = PinData.DEFAULT_LIFETIME_MS;
    }

    public MessageKeyPressed(boolean state, double x, double y, double z, String playerName, int dimId) {
        this(state, x, y, z, playerName, dimId, true,
                PinData.DEFAULT_LIFETIME_MS, PinData.DEFAULT_LIFETIME_MS);
    }

    public MessageKeyPressed(boolean state, double x, double y, double z,
                             String playerName, int dimId, boolean notify) {
        this(state, x, y, z, playerName, dimId, notify,
                PinData.DEFAULT_LIFETIME_MS, PinData.DEFAULT_LIFETIME_MS);
    }

    public MessageKeyPressed(boolean state, double x, double y, double z,
                             String playerName, int dimId, boolean notify, int remainingMs) {
        this(state, x, y, z, playerName, dimId, notify, remainingMs, remainingMs);
    }

    public MessageKeyPressed(boolean state, double x, double y, double z,
                             String playerName, int dimId, boolean notify,
                             int remainingMs, int lifetimeMs) {
        this.state = state;
        this.x = x;
        this.y = y;
        this.z = z;
        this.dimId = dimId;
        this.playerName = playerName;
        this.notify = notify;
        this.remainingMs = remainingMs;
        this.lifetimeMs = lifetimeMs;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.state = buf.readBoolean();
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.dimId = buf.readInt();
        this.playerName = ByteBufUtils.readUTF8String(buf);

        // 兼容旧版本：缺少字段时使用默认值
        this.notify = buf.isReadable() ? buf.readBoolean() : true;
        this.remainingMs = buf.isReadable() ? buf.readInt() : PinData.DEFAULT_LIFETIME_MS;
        this.lifetimeMs = buf.isReadable() ? buf.readInt()
                : Math.max(this.remainingMs, PinData.DEFAULT_LIFETIME_MS);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(state);
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeInt(dimId);
        ByteBufUtils.writeUTF8String(buf, playerName);
        buf.writeBoolean(notify);
        buf.writeInt(remainingMs);
        buf.writeInt(lifetimeMs);
    }
}
