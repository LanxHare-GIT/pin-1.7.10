package net.ciebus.pinmod.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;

/**
 * 屏幕中下方的短暂提示，用于代替聊天栏输出。
 */
public final class PinHudToast {

    private static final long DURATION_MS = 1800L;
    private static final long FADE_MS = 300L;

    private static String text;
    private static int color;
    private static long shownAt;

    private PinHudToast() {
    }

    /**
     * @param translationKey 语言文件键
     * @param args           格式化参数
     */
    public static void show(String translationKey, Object... args) {
        String raw = StatCollector.translateToLocalFormatted(translationKey, args);
        text = raw;
        color = 0xFFFF5555;
        shownAt = System.currentTimeMillis();
    }

    public static void clear() {
        text = null;
    }

    public static void render(ScaledResolution resolution) {
        if (text == null) {
            return;
        }

        long elapsed = System.currentTimeMillis() - shownAt;
        if (elapsed > DURATION_MS) {
            text = null;
            return;
        }

        float alpha = 1.0F;
        long remaining = DURATION_MS - elapsed;
        if (remaining < FADE_MS) {
            alpha = remaining / (float) FADE_MS;
        }

        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fr = mc.fontRenderer;
        int width = resolution.getScaledWidth();
        int height = resolution.getScaledHeight();

        int textWidth = fr.getStringWidth(text);
        int x = (width - textWidth) / 2;
        int y = height - 58;

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        int alphaByte = Math.max(0, Math.min(255, (int) (alpha * 140.0F)));
        Gui.drawRect(x - 6, y - 4, x + textWidth + 6, y + fr.FONT_HEIGHT + 3,
                (alphaByte << 24));

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        int textAlpha = Math.max(0, Math.min(255, (int) (alpha * 255.0F)));
        int tinted = (textAlpha << 24) | (color & 0xFFFFFF);
        fr.drawStringWithShadow(text, x, y, tinted);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPopAttrib();
    }
}
