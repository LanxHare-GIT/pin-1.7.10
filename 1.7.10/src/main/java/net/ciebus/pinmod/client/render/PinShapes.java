package net.ciebus.pinmod.client.render;

import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.opengl.GL11;

/**
 * 纯矢量绘制工具：菱形标记与任意角度线条。
 * 不依赖任何贴图，菱形与线条共用同一坐标，保证严格对齐。
 */
public final class PinShapes {

    private PinShapes() {
    }

    /**
     * 发光菱形标记：两层低透明度光晕 + 描边环 + 白色核心。
     *
     * @param cx    中心 X（GUI 坐标）
     * @param cy    中心 Y（GUI 坐标）
     * @param size  外接正方形边长（像素）
     * @param alpha 整体透明度
     * @param rgb   菱形颜色（0xRRGGBB）
     */
    public static void drawDiamondMarker(float cx, float cy, float size, float alpha, int rgb) {
        float radius = Math.max(2.0F, size / 2.0F);
        float red = ((rgb >> 16) & 0xFF) / 255.0F;
        float green = ((rgb >> 8) & 0xFF) / 255.0F;
        float blue = (rgb & 0xFF) / 255.0F;

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // 柔和光晕：用两层放大的实心菱形近似贴图的 alpha 渐变
        fillDiamond(cx, cy, radius * 1.60F, red, green, blue, alpha * 0.10F);
        fillDiamond(cx, cy, radius * 1.25F, red, green, blue, alpha * 0.18F);

        // 描边环
        float stroke = Math.max(1.0F, size / 12.0F);
        ringDiamond(cx, cy, radius, stroke, red, green, blue, alpha);

        // 中心白色核心（直径约为外接正方形的 0.42）
        fillDiamond(cx, cy, Math.max(1.0F, radius * 0.42F), 1.0F, 1.0F, 1.0F, alpha);
    }

    /**
     * 任意角度线段。
     *
     * @param solid true 时单层实心、无外发光；false 时保留柔和边缘
     */
    public static void drawLine(float x1, float y1, float x2, float y2,
                                float width, int rgb, float alpha, boolean solid) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        if (dx * dx + dy * dy < 1.0E-6F) {
            return;
        }

        float red = ((rgb >> 16) & 0xFF) / 255.0F;
        float green = ((rgb >> 8) & 0xFF) / 255.0F;
        float blue = (rgb & 0xFF) / 255.0F;

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        if (solid) {
            quad(x1, y1, x2, y2, width, red, green, blue, alpha);
        } else {
            quad(x1, y1, x2, y2, width + 2.0F, red, green, blue, alpha * 0.18F);
            quad(x1, y1, x2, y2, width, red, green, blue, alpha * 0.95F);
        }
    }

    /**
     * 多段折线，拐角用 miter 连接，避免两条独立线段在拐点留下三角形缺口。
     */
    public static void drawPolyline(float[] xs, float[] ys, int count,
                                    float width, int rgb, float alpha, boolean solid) {
        if (count < 2) {
            return;
        }

        float red = ((rgb >> 16) & 0xFF) / 255.0F;
        float green = ((rgb >> 8) & 0xFF) / 255.0F;
        float blue = (rgb & 0xFF) / 255.0F;

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        if (solid) {
            polylineStrip(xs, ys, count, width, red, green, blue, alpha);
        } else {
            polylineStrip(xs, ys, count, width + 2.0F, red, green, blue, alpha * 0.18F);
            polylineStrip(xs, ys, count, width, red, green, blue, alpha * 0.95F);
        }
    }

    private static void polylineStrip(float[] xs, float[] ys, int count, float width,
                                      float red, float green, float blue, float alpha) {
        int n = count;
        float half = width * 0.5F;
        float[] nx = new float[n];
        float[] ny = new float[n];
        float[] scale = new float[n];

        for (int i = 0; i < n; i++) {
            float dirX;
            float dirY;

            if (i == 0) {
                dirX = xs[1] - xs[0];
                dirY = ys[1] - ys[0];
            } else if (i == n - 1) {
                dirX = xs[n - 1] - xs[n - 2];
                dirY = ys[n - 1] - ys[n - 2];
            } else {
                float ax = xs[i] - xs[i - 1];
                float ay = ys[i] - ys[i - 1];
                float bx = xs[i + 1] - xs[i];
                float by = ys[i + 1] - ys[i];
                float la = (float) Math.sqrt(ax * ax + ay * ay);
                float lb = (float) Math.sqrt(bx * bx + by * by);
                if (la < 1.0E-4F || lb < 1.0E-4F) {
                    nx[i] = 0.0F;
                    ny[i] = 0.0F;
                    scale[i] = 1.0F;
                    continue;
                }
                ax /= la;
                ay /= la;
                bx /= lb;
                by /= lb;

                float n1x = -ay;
                float n1y = ax;
                float n2x = -by;
                float n2y = bx;
                float mx = n1x + n2x;
                float my = n1y + n2y;
                float lm = (float) Math.sqrt(mx * mx + my * my);
                if (lm < 1.0E-4F) {
                    nx[i] = n1x;
                    ny[i] = n1y;
                    scale[i] = 1.0F;
                } else {
                    mx /= lm;
                    my /= lm;
                    float dot = mx * n1x + my * n1y;
                    if (dot < 0.3F) {
                        dot = 0.3F;
                    }
                    nx[i] = mx;
                    ny[i] = my;
                    scale[i] = 1.0F / dot;
                }
                continue;
            }

            float len = (float) Math.sqrt(dirX * dirX + dirY * dirY);
            if (len < 1.0E-4F) {
                nx[i] = 0.0F;
                ny[i] = 0.0F;
                scale[i] = 1.0F;
            } else {
                nx[i] = -dirY / len;
                ny[i] = dirX / len;
                scale[i] = 1.0F;
            }
        }

        Tessellator tess = Tessellator.instance;
        tess.startDrawing(GL11.GL_TRIANGLES);
        tess.setColorRGBA_F(red, green, blue, clamp01(alpha));
        for (int i = 0; i < n - 1; i++) {
            float ox1 = nx[i] * half * scale[i];
            float oy1 = ny[i] * half * scale[i];
            float ox2 = nx[i + 1] * half * scale[i + 1];
            float oy2 = ny[i + 1] * half * scale[i + 1];

            float lx1 = xs[i] + ox1;
            float ly1 = ys[i] + oy1;
            float rx1 = xs[i] - ox1;
            float ry1 = ys[i] - oy1;
            float lx2 = xs[i + 1] + ox2;
            float ly2 = ys[i + 1] + oy2;
            float rx2 = xs[i + 1] - ox2;
            float ry2 = ys[i + 1] - oy2;

            tess.addVertex(lx1, ly1, 0.0D);
            tess.addVertex(rx1, ry1, 0.0D);
            tess.addVertex(rx2, ry2, 0.0D);
            tess.addVertex(lx1, ly1, 0.0D);
            tess.addVertex(rx2, ry2, 0.0D);
            tess.addVertex(lx2, ly2, 0.0D);
        }
        tess.draw();
    }

    /**
     * 实心方向小三角（带深色描边），用于屏外指示。
     */
    public static void drawDirectionTip(float x, float y, float dirX, float dirY, float size,
                                        float alpha, int rgb) {
        float red = ((rgb >> 16) & 0xFF) / 255.0F;
        float green = ((rgb >> 8) & 0xFF) / 255.0F;
        float blue = (rgb & 0xFF) / 255.0F;

        float perpX = -dirY;
        float perpY = dirX;

        float tipX = x + dirX * size;
        float tipY = y + dirY * size;
        float baseX = x - dirX * size * 0.45F;
        float baseY = y - dirY * size * 0.45F;
        float wingX = perpX * size * 0.62F;
        float wingY = perpY * size * 0.62F;

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        Tessellator tess = Tessellator.instance;

        // 深色外框
        tess.startDrawing(GL11.GL_TRIANGLES);
        tess.setColorRGBA_F(0.02F, 0.03F, 0.02F, clamp01(alpha * 0.65F));
        tess.addVertex(tipX + dirX * 1.2F, tipY + dirY * 1.2F, 0.0D);
        tess.addVertex(baseX + wingX * 1.45F, baseY + wingY * 1.45F, 0.0D);
        tess.addVertex(baseX - wingX * 1.45F, baseY - wingY * 1.45F, 0.0D);
        tess.draw();

        // 主色三角
        tess.startDrawing(GL11.GL_TRIANGLES);
        tess.setColorRGBA_F(red, green, blue, clamp01(alpha * 0.95F));
        tess.addVertex(tipX, tipY, 0.0D);
        tess.addVertex(baseX + wingX, baseY + wingY, 0.0D);
        tess.addVertex(baseX - wingX, baseY - wingY, 0.0D);
        tess.draw();
    }

    /** 实心菱形（两个三角形）。 */
    private static void fillDiamond(float cx, float cy, float radius,
                                    float red, float green, float blue, float alpha) {
        Tessellator tess = Tessellator.instance;
        tess.startDrawing(GL11.GL_TRIANGLES);
        tess.setColorRGBA_F(red, green, blue, clamp01(alpha));
        tess.addVertex(cx, cy - radius, 0.0D);
        tess.addVertex(cx + radius, cy, 0.0D);
        tess.addVertex(cx, cy + radius, 0.0D);
        tess.addVertex(cx, cy - radius, 0.0D);
        tess.addVertex(cx, cy + radius, 0.0D);
        tess.addVertex(cx - radius, cy, 0.0D);
        tess.draw();
    }

    /** 菱形描边环（4 段四边形，无缺口）。 */
    private static void ringDiamond(float cx, float cy, float outer, float stroke,
                                    float red, float green, float blue, float alpha) {
        float inner = Math.max(0.5F, outer - stroke);
        float[] ox = {0.0F, outer, 0.0F, -outer};
        float[] oy = {-outer, 0.0F, outer, 0.0F};
        float[] ix = {0.0F, inner, 0.0F, -inner};
        float[] iy = {-inner, 0.0F, inner, 0.0F};

        Tessellator tess = Tessellator.instance;
        tess.startDrawing(GL11.GL_QUADS);
        tess.setColorRGBA_F(red, green, blue, clamp01(alpha));
        for (int i = 0; i < 4; i++) {
            int j = (i + 1) & 3;
            tess.addVertex(cx + ox[i], cy + oy[i], 0.0D);
            tess.addVertex(cx + ox[j], cy + oy[j], 0.0D);
            tess.addVertex(cx + ix[j], cy + iy[j], 0.0D);
            tess.addVertex(cx + ix[i], cy + iy[i], 0.0D);
        }
        tess.draw();
    }

    /** 以线段为中心、沿法线展开的四边形。 */
    private static void quad(float x1, float y1, float x2, float y2, float width,
                             float red, float green, float blue, float alpha) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        float nx = -dy / length * width * 0.5F;
        float ny = dx / length * width * 0.5F;

        Tessellator tess = Tessellator.instance;
        tess.startDrawing(GL11.GL_QUADS);
        tess.setColorRGBA_F(red, green, blue, clamp01(alpha));
        tess.addVertex(x1 + nx, y1 + ny, 0.0D);
        tess.addVertex(x1 - nx, y1 - ny, 0.0D);
        tess.addVertex(x2 - nx, y2 - ny, 0.0D);
        tess.addVertex(x2 + nx, y2 + ny, 0.0D);
        tess.draw();
    }

    private static float clamp01(float value) {
        if (value < 0.0F) {
            return 0.0F;
        }
        if (value > 1.0F) {
            return 1.0F;
        }
        return value;
    }
}
