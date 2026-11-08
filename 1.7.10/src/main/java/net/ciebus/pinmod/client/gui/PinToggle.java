package net.ciebus.pinmod.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;

/**
 * 开关按钮：深色底，左侧状态圆点（开启绿色 / 关闭灰色）。
 */
public class PinToggle extends GuiButton {

    private boolean on;

    public PinToggle(int id, int x, int y, int width, int height, String text, boolean on) {
        super(id, x, y, width, height, text);
        this.on = on;
    }

    public void setOn(boolean on) {
        this.on = on;
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

        int dotColor = on ? 0xFF5CF2A0 : 0xFF606060;
        if (on) {
            Gui.drawRect(xPosition + 4, yPosition + 4, xPosition + 16, yPosition + height - 4, 0x335CF2A0);
        }
        Gui.drawRect(xPosition + 8, yPosition + height / 2 - 3, xPosition + 14, yPosition + height / 2 + 3, dotColor);

        mc.fontRenderer.drawStringWithShadow(displayString, xPosition + 20,
                yPosition + (height - 8) / 2, this.enabled ? 0xFFFFFF : 0x808080);
    }
}
