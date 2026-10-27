package net.ciebus.pinmod.client.render;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.ciebus.pinmod.PinManager;
import net.ciebus.pinmod.common.config.PinConfig;
import net.ciebus.pinmod.common.data.PinData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class PinRenderer {

    private static final int WHITE = 0xFFFFFF;

    private static IntBuffer viewport = GLAllocation.createDirectIntBuffer(16);
    private static FloatBuffer modelview = GLAllocation.createDirectFloatBuffer(16);
    private static FloatBuffer projection = GLAllocation.createDirectFloatBuffer(16);
    private static FloatBuffer objectCoords = GLAllocation.createDirectFloatBuffer(16);

    @SubscribeEvent
    public void renderPin(RenderWorldLastEvent event) {
        for (PinData pin : PinManager.pins()) {
            if (pin.isExpired()) {
                continue;
            }
            updateScreenPosition(pin);
        }
    }

    private void updateScreenPosition(PinData pin) {
        if (Minecraft.getMinecraft().theWorld.provider.dimensionId != pin.dimId) {
            return;
        }

        EntityPlayer p = Minecraft.getMinecraft().thePlayer;
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelview);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projection);
        GL11.glGetInteger(GL11.GL_VIEWPORT, viewport);

        double eyeX = RenderManager.renderPosX;
        double eyeY = RenderManager.renderPosY;
        double eyeZ = RenderManager.renderPosZ;

        objectCoords.clear();
        GLU.gluProject(
                (float) (pin.x - eyeX),
                (float) (pin.y - eyeY),
                (float) (pin.z - eyeZ),
                modelview, projection, viewport, objectCoords);

        pin.screenX = objectCoords.get(0);
        pin.screenY = objectCoords.get(1);
        pin.screenZ = objectCoords.get(2);
        pin.hasScreenPos = true;

        // 世界空间竖线顶端：命中点沿世界 Y 轴抬高 lineHeight
        objectCoords.clear();
        GLU.gluProject(
                (float) (pin.x - eyeX),
                (float) (pin.y + PinConfig.lineHeight - eyeY),
                (float) (pin.z - eyeZ),
                modelview, projection, viewport, objectCoords);
        pin.screenTopX = objectCoords.get(0);
        pin.screenTopY = objectCoords.get(1);
        pin.hasTop = true;

        Vec3 look = p.getLookVec();
        pin.behind = (pin.x - eyeX) * look.xCoord
                + (pin.y - eyeY) * look.yCoord
                + (pin.z - eyeZ) * look.zCoord <= 0.0D;
    }

    @SubscribeEvent
    public void renderGui(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        // 隔离 GL 状态：HUD 的矢量/文字绘制不应影响同一帧的 GUI（如 Mod Options）
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        try {
            renderPins(mc, event);
        } finally {
            GL11.glPopAttrib();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        }
    }

    private void renderPins(Minecraft mc, RenderGameOverlayEvent.Post event) {
        int width = event.resolution.getScaledWidth();
        int height = event.resolution.getScaledHeight();
        double centerX = width / 2.0D;
        double centerY = height / 2.0D;
        double margin = 24.0D;
        double halfW = Math.max(1.0D, centerX - margin);
        double halfH = Math.max(1.0D, centerY - margin);
        FontRenderer fr = mc.fontRenderer;

        for (PinData pin : PinManager.pins()) {
            if (mc.theWorld.provider.dimensionId != pin.dimId) {
                continue;
            }
            if (!pin.hasScreenPos) {
                continue;
            }

            float alpha = pin.getAlpha();
            if (alpha <= 0.0F) {
                continue;
            }

            double distance = mc.thePlayer.getDistance(pin.x, pin.y, pin.z);
            double guiScale = event.resolution.getScaleFactor();
            double sx = pin.screenX / guiScale;
            double sy = (mc.displayHeight - pin.screenY) / guiScale;

            boolean onScreen = !pin.behind
                    && pin.screenZ >= 0.0D && pin.screenZ <= 1.0D
                    && sx >= 0.0D && sx <= width
                    && sy >= 0.0D && sy <= height;

            String name = pin.player;
            String dist = " " + (int) distance + "m";
            int alphaByte = Math.max(0, Math.min(255, (int) (alpha * 255.0F)));
            int colorName = (alphaByte << 24) | WHITE;
            int colorDist = (alphaByte << 24) | PinConfig.getDotTextColor();

            if (onScreen) {
                if (PinConfig.preset == PinConfig.PRESET_CALLOUT) {
                    drawCallout(fr, name, dist, (float) sx, (float) sy, distance, alpha,
                            colorName, colorDist, width, height);
                    continue;
                }

                float anchorX = (float) sx;
                float anchorY = (float) sy;
                if (PinConfig.lineEnabled) {
                    if (PinConfig.lineWorldSpace && pin.hasTop) {
                        anchorX = (float) clamp(pin.screenTopX / guiScale, 4.0D, width - 4.0D);
                        anchorY = (float) clamp((mc.displayHeight - pin.screenTopY) / guiScale,
                                4.0D, height - 4.0D);
                    } else {
                        anchorY = (float) (sy - Math.round(PinConfig.lineLength));
                    }
                }
                drawMarker(fr, name, dist, (float) sx, (float) sy, anchorX, anchorY,
                        distance, alpha, colorName, colorDist, width, height);
                continue;
            }

            boolean validProjection = !Double.isNaN(sx) && !Double.isNaN(sy)
                    && !Double.isInfinite(sx) && !Double.isInfinite(sy);

            double dirX;
            double dirY;

            if (!pin.behind && validProjection) {
                dirX = sx - centerX;
                dirY = sy - centerY;
            } else {
                double dx = pin.x - mc.thePlayer.posX;
                double dz = pin.z - mc.thePlayer.posZ;
                double targetYaw = Math.toDegrees(Math.atan2(-dx, dz));
                double relativeYaw = targetYaw - mc.thePlayer.rotationYaw;

                while (relativeYaw <= -180.0D) relativeYaw += 360.0D;
                while (relativeYaw > 180.0D) relativeYaw -= 360.0D;

                double rad = Math.toRadians(relativeYaw);
                dirX = Math.sin(rad);
                dirY = -Math.cos(rad);
            }

            double len = Math.sqrt(dirX * dirX + dirY * dirY);
            if (len < 1.0E-4D) {
                dirX = 0.0D;
                dirY = -1.0D;
            } else {
                dirX /= len;
                dirY /= len;
            }

            double edgeScale = Math.min(
                    Math.abs(dirX) < 1.0E-4D ? Double.MAX_VALUE : halfW / Math.abs(dirX),
                    Math.abs(dirY) < 1.0E-4D ? Double.MAX_VALUE : halfH / Math.abs(dirY)
            );

            double arrowX = centerX + dirX * edgeScale;
            double arrowY = centerY + dirY * edgeScale;

            // 屏外指示：菱形与标记同步
            double edgeScaleFactor = 1.0D + PinConfig.farGrowth * Math.min(1.5D, distance / 50.0D);
            int edgeDotPx = clamp((int) Math.round(PinConfig.dotSize * edgeScaleFactor), 4, 64);
            int edgeDotHalf = edgeDotPx / 2;

            if (PinConfig.outsideShape == PinConfig.OUTSIDE_SHAPE_DIAMOND) {
                // 菱形指示：只画菱形，不再叠加箭头
                if (PinConfig.showDot) {
                    GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
                    GL11.glDisable(GL11.GL_ALPHA_TEST);
                    GL11.glDisable(GL11.GL_CULL_FACE);
                    PinShapes.drawDiamondMarker((float) arrowX, (float) arrowY, edgeDotPx, alpha,
                            PinConfig.dotColor);
                    GL11.glPopAttrib();
                } else {
                    // 菱形开关关闭时，用方向小三角代替，保证总有指示
                    PinShapes.drawDirectionTip((float) arrowX, (float) arrowY,
                            (float) dirX, (float) dirY, 9.0F, alpha, PinConfig.dotColor);
                }
            } else {
                if (PinConfig.showDot) {
                    GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
                    GL11.glDisable(GL11.GL_ALPHA_TEST);
                    GL11.glDisable(GL11.GL_CULL_FACE);
                    PinShapes.drawDiamondMarker((float) arrowX, (float) arrowY, edgeDotPx, alpha,
                            PinConfig.dotColor);
                    GL11.glPopAttrib();
                }
                drawArrow(arrowX + dirX * (edgeDotHalf + 4.0D), arrowY + dirY * (edgeDotHalf + 4.0D),
                        dirX, dirY, 10.0D, alpha);
            }

            drawEdgeLabel(fr, name, dist, arrowX, arrowY, centerX, centerY,
                    edgeDotHalf, colorName, colorDist, width, height);
        }

        PinHudToast.render(event.resolution);
    }

    private static void drawMarker(FontRenderer fr, String name, String dist,
                                   float baseX, float baseY, float anchorX, float anchorY,
                                   double distance, float alpha,
                                   int colorName, int colorDist, int width, int height) {
        double distanceScale = 1.0D + PinConfig.farGrowth * Math.min(1.5D, distance / 50.0D);
        int dotPx = clamp((int) Math.round(PinConfig.dotSize * distanceScale), 4, 64);
        int linePxWidth = clamp((int) Math.round(PinConfig.lineWidth), 1, 8);

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);

        if (PinConfig.lineEnabled) {
            PinShapes.drawLine(baseX, baseY, anchorX, anchorY, linePxWidth,
                    PinConfig.lineColor, alpha, PinConfig.lineSolid);
        }

        if (PinConfig.showDot) {
            PinShapes.drawDiamondMarker(anchorX, anchorY, dotPx, alpha, PinConfig.dotColor);
        }

        drawSideLabel(fr, name, dist, anchorX, anchorY,
                PinConfig.showDot ? dotPx / 2.0F : 0.0F,
                colorName, colorDist, width, height);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPopAttrib();
    }

    private static void drawSideLabel(FontRenderer fr, String name, String dist,
                                      float anchorX, float anchorY, float dotHalf,
                                      int colorName, int colorDist, int width, int height) {
        int nameWidth = fr.getStringWidth(name);
        int totalWidth = nameWidth + fr.getStringWidth(dist);

        float offsetX = dotHalf + PinConfig.labelGap + PinConfig.labelOffsetX;
        float tx = anchorX + offsetX;
        // 左下角锚点：文字底边位于 anchorY + labelOffsetY + 5
        float ty = anchorY + PinConfig.labelOffsetY + 5.0F - fr.FONT_HEIGHT;
        if (tx + totalWidth > width - 4) {
            tx = anchorX - offsetX - totalWidth;
        }

        tx = (float) clamp(tx, 4.0D, Math.max(4.0D, width - 4.0D - totalWidth));
        ty = (float) clamp(ty, 4.0D, Math.max(4.0D, height - 4.0D - fr.FONT_HEIGHT));

        drawLabelText(fr, name, dist, tx, ty, colorName, colorDist);
    }

    /**
     * 引线标注预设：菱形钉在命中点，斜引线折成水平线，文字居中在水平线上方。
     */
    private static void drawCallout(FontRenderer fr, String name, String dist,
                                    float baseX, float baseY, double distance, float alpha,
                                    int colorName, int colorDist, int width, int height) {
        int nameWidth = fr.getStringWidth(name);
        int distWidth = fr.getStringWidth(dist);
        int textWidth = nameWidth + distWidth;

        double distanceScale = 1.0D + PinConfig.farGrowth * Math.min(1.5D, distance / 50.0D);
        int dotPx = clamp((int) Math.round(PinConfig.dotSize * distanceScale), 4, 64);
        int linePxWidth = clamp((int) Math.round(PinConfig.lineWidth), 1, 8);

        // 斜线高度由 lineHeight 决定，角度保持 45°（水平分量等于垂直分量）
        int rise = clamp((int) Math.round(PinConfig.lineHeight * 40.0F), 6, 120);

        // 靠近右边缘时整体镜像到左侧
        boolean flip = baseX > width * 0.55F;
        float sign = flip ? -1.0F : 1.0F;

        float elbowX = baseX + sign * rise;
        float elbowY = baseY - rise;

        float lineLength = Math.max(50.0F, textWidth + 28.0F);
        float available = flip ? (elbowX - 6.0F) : (width - 6.0F - elbowX);
        lineLength = Math.min(lineLength, Math.max(24.0F, available));
        float endX = elbowX + sign * lineLength;

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);

        if (PinConfig.lineEnabled) {
            PinShapes.drawPolyline(
                    new float[]{baseX, elbowX, endX},
                    new float[]{baseY, elbowY, elbowY},
                    3, linePxWidth, PinConfig.lineColor, alpha, PinConfig.lineSolid);
        }

        if (PinConfig.showDot) {
            PinShapes.drawDiamondMarker(baseX, baseY, dotPx, alpha, PinConfig.dotColor);
        }

        // 文字左下角锚定折线拐点，X/Y 偏移可调；翻转时整体镜像到左侧
        float tx = elbowX + sign * PinConfig.labelOffsetX;
        float ty = elbowY - PinConfig.labelGap + PinConfig.labelOffsetY - fr.FONT_HEIGHT;
        if (flip) {
            tx -= textWidth;
        }
        tx = (float) clamp(tx, 4.0D, Math.max(4.0D, width - 4.0D - textWidth));
        ty = (float) clamp(ty, 4.0D, Math.max(4.0D, height - 4.0D - fr.FONT_HEIGHT));
        drawLabelText(fr, name, dist, tx, ty, colorName, colorDist);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPopAttrib();
    }

    /**
     * 以子像素位置绘制标签，避免整数取整造成的逐帧跳动/锯齿感。
     */
    private static void drawLabelText(FontRenderer fr, String name, String dist,
                                      float x, float y, int colorName, int colorDist) {
        prepareTextRendering();

        int ix = (int) Math.floor(x);
        int iy = (int) Math.floor(y);
        float fractionX = x - ix;
        float fractionY = y - iy;

        GL11.glPushMatrix();
        GL11.glTranslatef(fractionX, fractionY, 0.0F);
        fr.drawStringWithShadow(name, ix, iy, colorName);
        fr.drawStringWithShadow(dist, ix + fr.getStringWidth(name), iy, colorDist);
        GL11.glPopMatrix();
    }

    /**
     * FontRenderer 依赖调用方已启用纹理与混合；矢量绘制会关闭 GL_TEXTURE_2D，
     * 不恢复的话字形会被画成无纹理的方块（屏幕内文字变方块的原因）。
     */
    private static void prepareTextRendering() {
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void drawEdgeLabel(FontRenderer fr, String name, String dist,
                                      double arrowX, double arrowY, double centerX, double centerY,
                                      int dotHalf, int colorName, int colorDist, int width, int height) {
        prepareTextRendering();

        int nameWidth = fr.getStringWidth(name);
        int totalWidth = nameWidth + fr.getStringWidth(dist);

        int offset = dotHalf + 8;
        int tx;
        if (arrowX < centerX) {
            tx = (int) arrowX + offset;
        } else if (arrowX > centerX) {
            tx = (int) arrowX - offset - totalWidth;
        } else {
            tx = (int) arrowX - totalWidth / 2;
        }

        int ty = arrowY > centerY
                ? (int) arrowY - dotHalf - 14
                : (int) arrowY + dotHalf + 10;
        tx = clamp(tx, 4, Math.max(4, width - 4 - totalWidth));
        ty = clamp(ty, 4, Math.max(4, height - 4 - 9));

        fr.drawStringWithShadow(name, tx, ty, colorName);
        fr.drawStringWithShadow(dist, tx + nameWidth, ty, colorDist);
    }

    private static void drawArrow(double x, double y, double dirX, double dirY, double size, float alpha) {
        // 箭头颜色与菱形同步
        float red = PinConfig.getDotRed();
        float green = PinConfig.getDotGreen();
        float blue = PinConfig.getDotBlue();

        double perpX = -dirY;
        double perpY = dirX;

        double tipX = x + dirX * size;
        double tipY = y + dirY * size;

        double baseX = x - dirX * size * 0.55D;
        double baseY = y - dirY * size * 0.55D;

        double wingX = perpX * size * 0.65D;
        double wingY = perpY * size * 0.65D;

        Tessellator tess = Tessellator.instance;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        double tailX = x - dirX * size * 1.35D;
        double tailY = y - dirY * size * 1.35D;

        GL11.glLineWidth(4.0F);
        GL11.glColor4f(0.02F, 0.03F, 0.02F, 0.55F * alpha);
        tess.startDrawing(GL11.GL_LINES);
        tess.addVertex(tailX, tailY, 0.0D);
        tess.addVertex(baseX, baseY, 0.0D);
        tess.draw();

        GL11.glLineWidth(2.0F);
        GL11.glColor4f(red, green, blue, 0.95F * alpha);
        tess.startDrawing(GL11.GL_LINES);
        tess.addVertex(tailX, tailY, 0.0D);
        tess.addVertex(baseX, baseY, 0.0D);
        tess.draw();
        GL11.glLineWidth(1.0F);

        GL11.glColor4f(0.02F, 0.03F, 0.02F, 0.65F * alpha);
        tess.startDrawing(GL11.GL_TRIANGLES);
        tess.addVertex(tipX, tipY, 0.0D);
        tess.addVertex(baseX + wingX * 1.35D, baseY + wingY * 1.35D, 0.0D);
        tess.addVertex(baseX - wingX * 1.35D, baseY - wingY * 1.35D, 0.0D);
        tess.draw();

        GL11.glColor4f(red, green, blue, 0.95F * alpha);
        tess.startDrawing(GL11.GL_TRIANGLES);
        tess.addVertex(tipX, tipY, 0.0D);
        tess.addVertex(baseX + wingX, baseY + wingY, 0.0D);
        tess.addVertex(baseX - wingX, baseY - wingY, 0.0D);
        tess.draw();

        double coreBaseX = tipX - dirX * size * 0.45D;
        double coreBaseY = tipY - dirY * size * 0.45D;
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.9F * alpha);
        tess.startDrawing(GL11.GL_TRIANGLES);
        tess.addVertex(tipX, tipY, 0.0D);
        tess.addVertex(coreBaseX + wingX * 0.35D, coreBaseY + wingY * 0.35D, 0.0D);
        tess.addVertex(coreBaseX - wingX * 0.35D, coreBaseY - wingY * 0.35D, 0.0D);
        tess.draw();

        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }

    private static int clamp(int value, int min, int max) {
        if (max < min) {
            return min;
        }
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    private static double clamp(double value, double min, double max) {
        if (max < min) {
            return min;
        }
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }
}
