package net.ciebus.pinmod.client.init;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import net.ciebus.pinmod.PinManager;
import net.ciebus.pinmod.client.render.PinHudToast;
import net.ciebus.pinmod.common.config.PinConfig;
import net.ciebus.pinmod.common.data.PinData;
import net.ciebus.pinmod.common.network.MessageKeyPressed;
import net.ciebus.pinmod.common.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;

public final class KeyBindings {

    public static final KeyBinding pinKey = new KeyBinding(
            "key.pin.place", Keyboard.KEY_R, "key.categories.pin");

    public static void init() {
        ClientRegistry.registerKeyBinding(pinKey);
        FMLCommonHandler.instance().bus().register(new KeyBindings());
        MinecraftForge.EVENT_BUS.register(new KeyBindings());
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (!pinKey.isPressed()) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.thePlayer;
        if (player == null || player.worldObj == null) {
            return;
        }

        double maxDistance = getClientReach(mc);
        Vec3 eye = player.getPosition(1.0F);
        Vec3 look = player.getLook(1.0F);
        int dimId = player.worldObj.provider.dimensionId;

        Pick pick = pick(player, eye, look, maxDistance);
        FMLLog.info("[Pin] pick=%s", pick == null ? "none" : "block");

        // 视线圆柱：命中自己已有的标记则删除
        PinData existing = PinManager.pickPin(eye, look, maxDistance);
        if (existing != null) {
            PacketHandler.INSTANCE2.sendToServer(new MessageKeyPressed(
                    true, existing.x, existing.y, existing.z,
                    player.getDisplayName(), dimId, true, 0, 0));
            return;
        }

        if (pick == null) {
            PinHudToast.show("pin.hud.noBlock");
            return;
        }

        int lifetime = PinConfig.getLifetimeMs();
        PacketHandler.INSTANCE2.sendToServer(new MessageKeyPressed(
                false, pick.x, pick.y, pick.z,
                player.getDisplayName(), dimId, true, lifetime, lifetime));
    }

    /**
     * 客户端射线长度 = 玩家自己的视距（区块数 × 16）。
     */
    private static double getClientReach(Minecraft mc) {
        int chunks = mc.gameSettings.renderDistanceChunks;
        if (chunks < 1) {
            chunks = 1;
        }
        return chunks * 16.0D;
    }

    private static final class Pick {
        final double x;
        final double y;
        final double z;

        Pick(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    /**
     * 方块射线拾取。
     *
     * <p>用 {@code func_147447_a(..., false, false, false)}：最后一个参数为 false，
     * 不能把「穿过的最后一个无碰撞方块」当成命中，否则标记会落在空气里。
     */
    private static Pick pick(EntityPlayer player, Vec3 eye, Vec3 look, double maxDistance) {
        Vec3 end = eye.addVector(look.xCoord * maxDistance,
                look.yCoord * maxDistance, look.zCoord * maxDistance);

        World world = player.worldObj;
        MovingObjectPosition blockHit = world.func_147447_a(eye, end, false, false, false);
        if (blockHit == null || blockHit.hitVec == null) {
            return null;
        }
        return new Pick(blockHit.hitVec.xCoord, blockHit.hitVec.yCoord,
                blockHit.hitVec.zCoord);
    }
}
