package net.ciebus.pinmod.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;

/**
 * 极简取色器：一条色相条 + 一条亮度条。
 *
 * <p>饱和度固定为 100%，因此可选范围是「黑 → 纯色 → 白」，选不到灰色。
 * 亮度按 HSL 定义：0 为黑，0.5 为纯色，1 为白。
 */
public class ColorPicker extends GuiScreen {

    public interface Callback {
        void onColor(int color);
    }

    private static final int OK = 20;
    private static final int CANCEL = 21;
    private static final int BAR_WIDTH = 140;
    private static final int BAR_HEIGHT = 16;

    private final GuiScreen parent;
    private final Callback callback;
    private int color;

    private float hue;
    private float lightness;

    private int hueX;
    private int hueY;
    private int lightX;
    private int lightY;
    private int previewY;

    private boolean draggingHue;
    private boolean draggingLight;

    public ColorPicker(GuiScreen parent, int initialColor, Callback callback) {
        this.parent = parent;
        this.callback = callback;
        this.color = initialColor & 0xFFFFFF;

        float[] hsl = rgbToHsl(this.color);
        this.hue = hsl[0];
        this.lightness = hsl[2];
    }

    @Override
    public void initGui() {
        buttonList.clear();

        int center = width / 2;
        hueX = center - BAR_WIDTH / 2;
        hueY = 64;
        lightX = hueX;
        lightY = hueY + BAR_HEIGHT + 14;
        previewY = lightY + BAR_HEIGHT + 14;

        buttonList.add(new GuiButton(OK, center - 105, height - 30, 100, 20,
                StatCollector.translateToLocal("pin.gui.done")));
        buttonList.add(new GuiButton(CANCEL, center + 5, height - 30, 100, 20,
                StatCollector.translateToLocal("pin.gui.cancel")));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        int center = width / 2;
        int panelLeft = center - 124;
        int panelRight = center + 124;
        int panelTop = 30;
        int panelBottom = height - 40;

        drawRect(panelLeft - 1, panelTop - 1, panelRight + 1, panelBottom + 1, 0xFF2E2E2E);
        drawRect(panelLeft, panelTop, panelRight, panelBottom, 0xE6141414);
        drawRect(panelLeft, panelTop, panelRight, panelTop + 22, 0xFF1B1B1B);
        drawRect(panelLeft, panelTop + 22, panelRight, panelTop + 23, 0xFF2E2E2E);

        drawCenteredString(fontRendererObj, StatCollector.translateToLocal("pin.gui.color"),
                center, panelTop + 7, 0x5CF2A0);

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // 色相条：固定 S=100%、L=50%，从红到品红
        fillRect(hueX - 2, hueY - 2, hueX + BAR_WIDTH + 2, hueY + BAR_HEIGHT + 2, 0xFF202020);
        for (int i = 0; i < BAR_WIDTH; i++) {
            int rgb = hslToRgb(i / (float) (BAR_WIDTH - 1), 1.0F, 0.5F);
            fillRect(hueX + i, hueY, hueX + i + 1, hueY + BAR_HEIGHT, 0xFF000000 | rgb);
        }

        // 亮度条：跟随当前色相，黑 → 纯色 → 白
        fillRect(lightX - 2, lightY - 2, lightX + BAR_WIDTH + 2, lightY + BAR_HEIGHT + 2, 0xFF202020);
        for (int i = 0; i < BAR_WIDTH; i++) {
            int rgb = hslToRgb(hue, 1.0F, i / (float) (BAR_WIDTH - 1));
            fillRect(lightX + i, lightY, lightX + i + 1, lightY + BAR_HEIGHT, 0xFF000000 | rgb);
        }

        int hx = hueX + (int) (hue * (BAR_WIDTH - 1));
        fillRect(hx - 1, hueY - 4, hx + 2, hueY + BAR_HEIGHT + 4, 0xFFFFFFFF);
        int lx = lightX + (int) (lightness * (BAR_WIDTH - 1));
        fillRect(lx - 1, lightY - 4, lx + 2, lightY + BAR_HEIGHT + 4, 0xFFFFFFFF);

        // 颜色预览
        fillRect(hueX - 2, previewY - 2, hueX + BAR_WIDTH + 2, previewY + 26, 0xFF202020);
        fillRect(hueX, previewY, hueX + BAR_WIDTH, previewY + 24, 0xFF000000 | color);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPopAttrib();

        drawCenteredString(fontRendererObj, String.format("#%06X", color),
                width / 2, previewY + 30, 0xFFFFFF);
        drawCenteredString(fontRendererObj, StatCollector.translateToLocal("pin.gui.color.hint"),
                width / 2, previewY + 46, 0xAAAAAA);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private static void fillRect(int left, int top, int right, int bottom, int color) {
        float a = ((color >> 24) & 0xFF) / 255.0F;
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;

        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();
        tess.setColorRGBA_F(r, g, b, a);
        tess.addVertex(left, bottom, 0.0D);
        tess.addVertex(right, bottom, 0.0D);
        tess.addVertex(right, top, 0.0D);
        tess.addVertex(left, top, 0.0D);
        tess.draw();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == OK) {
            callback.onColor(color);
            mc.displayGuiScreen(parent);
        } else if (button.id == CANCEL) {
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {
            if (inside(mouseX, mouseY, hueX, hueY, BAR_WIDTH, BAR_HEIGHT)) {
                draggingHue = true;
                updateHue(mouseX);
                return;
            }
            if (inside(mouseX, mouseY, lightX, lightY, BAR_WIDTH, BAR_HEIGHT)) {
                draggingLight = true;
                updateLight(mouseX);
                return;
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {
        if (draggingHue) {
            updateHue(mouseX);
        } else if (draggingLight) {
            updateLight(mouseX);
        }
        super.mouseClickMove(mouseX, mouseY, mouseButton, timeSinceLastClick);
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int state) {
        if (state >= 0) {
            draggingHue = false;
            draggingLight = false;
        }
        super.mouseMovedOrUp(mouseX, mouseY, state);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == 1) {
            mc.displayGuiScreen(parent);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    private void updateHue(int mouseX) {
        hue = clamp01((mouseX - hueX) / (float) (BAR_WIDTH - 1));
        applyColor();
    }

    private void updateLight(int mouseX) {
        lightness = clamp01((mouseX - lightX) / (float) (BAR_WIDTH - 1));
        applyColor();
    }

    private void applyColor() {
        color = hslToRgb(hue, 1.0F, lightness);
    }

    private static boolean inside(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    private static float clamp01(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    /** HSL → RGB，饱和度固定 1.0 时使用。 */
    static int hslToRgb(float h, float s, float l) {
        float r;
        float g;
        float b;

        if (s <= 0.0F) {
            r = l;
            g = l;
            b = l;
        } else {
            float q = l < 0.5F ? l * (1.0F + s) : l + s - l * s;
            float p = 2.0F * l - q;
            r = hueToRgb(p, q, h + 1.0F / 3.0F);
            g = hueToRgb(p, q, h);
            b = hueToRgb(p, q, h - 1.0F / 3.0F);
        }

        int red = Math.round(clamp01(r) * 255.0F);
        int green = Math.round(clamp01(g) * 255.0F);
        int blue = Math.round(clamp01(b) * 255.0F);
        return (red << 16) | (green << 8) | blue;
    }

    private static float hueToRgb(float p, float q, float t) {
        float value = t;
        if (value < 0.0F) {
            value += 1.0F;
        }
        if (value > 1.0F) {
            value -= 1.0F;
        }
        if (value < 1.0F / 6.0F) {
            return p + (q - p) * 6.0F * value;
        }
        if (value < 1.0F / 2.0F) {
            return q;
        }
        if (value < 2.0F / 3.0F) {
            return p + (q - p) * (2.0F / 3.0F - value) * 6.0F;
        }
        return p;
    }

    /** RGB → HSL，用于把已有颜色换算成色相与亮度。 */
    static float[] rgbToHsl(int rgb) {
        float r = ((rgb >> 16) & 0xFF) / 255.0F;
        float g = ((rgb >> 8) & 0xFF) / 255.0F;
        float b = (rgb & 0xFF) / 255.0F;

        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float l = (max + min) / 2.0F;

        float h = 0.0F;
        float s = 0.0F;
        if (max - min > 1.0E-6F) {
            float d = max - min;
            s = l > 0.5F ? d / (2.0F - max - min) : d / (max + min);
            if (max == r) {
                h = (g - b) / d + (g < b ? 6.0F : 0.0F);
            } else if (max == g) {
                h = (b - r) / d + 2.0F;
            } else {
                h = (r - g) / d + 4.0F;
            }
            h /= 6.0F;
        }
        return new float[]{h, s, l};
    }
}
