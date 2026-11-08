package net.ciebus.pinmod.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;

/**
 * 颜色按钮：左侧色块 + 右侧文字，点击打开取色器。
 */
public class PinColorButton extends GuiButton {

    private int color;

    public PinColorButton(int id, int x, int y, int width, int height, String text, int color) {
        super(id, x, y, width, height, text);
        this.color = color & 0xFFFFFF;
    }

    public void setColor(int color) {
        this.color = color & 0xFFFFFF;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (!this.visible) {
            return;
        }

        boolean hover = mouseX >= xPosition && mouseY >= yPosition
                && mouseX < xPosition + width && mouseY < yPosition + height;

        Gui.drawRect(xPosition, yPosition, xPosition + width, yPosition + height,
                hover ? 0xFF2E2E2E : 0xFF232323);

        Gui.drawRect(xPosition + 4, yPosition + 4, xPosition + 18, yPosition + height - 4, 0xFF2E2E2E);
        Gui.drawRect(xPosition + 5, yPosition + 5, xPosition + 17, yPosition + height - 5,
                0xFF000000 | color);

        mc.fontRenderer.drawStringWithShadow(displayString, xPosition + 24,
                yPosition + (height - 8) / 2, this.enabled ? 0xFFFFFF : 0x808080);
    }
}
