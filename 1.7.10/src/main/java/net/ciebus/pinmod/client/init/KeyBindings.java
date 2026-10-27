package net.ciebus.pinmod.client.init;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import net.ciebus.pinmod.PinManager;
import net.ciebus.pinmod.client.render.PinHudToast;
import net.ciebus.pinmod.common.config.PinConfig;
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
        Vec3 hit = pickSolidBlock(player, maxDistance);
        if (hit == null) {
            // 没命中可碰撞方块，或超出自己的视距
            PinHudToast.show("pin.hud.noBlock");
            return;
        }

        boolean state = PinManager.isToDelete(hit.xCoord, hit.yCoord, hit.zCoord,
                player.getDisplayName());

        int lifetime = state ? 0 : PinConfig.getLifetimeMs();

        PacketHandler.INSTANCE2.sendToServer(new MessageKeyPressed(
                state, hit.xCoord, hit.yCoord, hit.zCoord,
                player.getDisplayName(),
                player.worldObj.provider.dimensionId,
                true, lifetime, lifetime));
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

    /**
     * 只接受真正命中可碰撞方块的点。
     * Entity.rayTrace 会以 returnLastUncollidableBlock=true 调用射线检测，
     * 没有方块时会把“穿过的最后一个无碰撞方块”当成命中（草、雪、空气类方块），
     * 会表现为标记落在空气里，所以这里改用显式参数的世界射线检测。
     */
    private static Vec3 pickSolidBlock(EntityPlayer player, double maxDistance) {
        Vec3 start = player.getPosition(1.0F);
        Vec3 look = player.getLook(1.0F);
        Vec3 end = start.addVector(look.xCoord * maxDistance,
                look.yCoord * maxDistance, look.zCoord * maxDistance);

        World world = player.worldObj;
        MovingObjectPosition mop = world.func_147447_a(start, end, false, false, false);

        if (mop == null || mop.hitVec == null) {
            return null;
        }
        return mop.hitVec;
    }

}
