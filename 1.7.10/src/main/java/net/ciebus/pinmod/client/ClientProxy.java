package net.ciebus.pinmod.client;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.ciebus.pinmod.client.init.KeyBindings;
import net.ciebus.pinmod.client.init.Renderers;
import net.ciebus.pinmod.common.CommonProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

public class ClientProxy extends CommonProxy {

    @Override
    public void onInit(FMLInitializationEvent event) {
        super.onInit(event);
        Renderers.init();
        KeyBindings.init();
    }

    @Override
    public void onPreInit(FMLPreInitializationEvent event) {
        super.onPreInit(event);
    }

    @Override
    public void playPinSound(final String playerName) {
        final Minecraft mc = Minecraft.getMinecraft();

        // 网络包处理线程不是客户端主线程，调度回主线程播放
        mc.func_152344_a(new Runnable() {
            @Override
            public void run() {
                if (mc.thePlayer == null) {
                    return;
                }

                // 非位置 UI 音效，避免 GTNH 的位置音效路由吞掉声音
                mc.getSoundHandler().playSound(
                        PositionedSoundRecord.func_147673_a(
                                new ResourceLocation("pinmod", "ping")));
            }
        });
    }
}
