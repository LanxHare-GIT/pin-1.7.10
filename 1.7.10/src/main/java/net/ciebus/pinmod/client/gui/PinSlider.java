package net.ciebus.pinmod.client.gui;

import cpw.mods.fml.client.config.GuiSlider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

/**
 * 自绘滑块：深色底 + 底部细轨道 + 绿色进度，文字居中。
 * 复用 Forge GuiSlider 的拖动逻辑，只替换外观。
 */
public class PinSlider extends GuiSlider {

    public PinSlider(int id, int x, int y, int width, int height, String prefix, String suffix,
                     double min, double max, double value, boolean drawString, boolean common) {
        super(id, x, y, width, height, prefix, suffix, min, max, value, drawString, common);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (!this.visible) {
            return;
        }

        boolean hover = mouseX >= xPosition && mouseY >= yPosition
                && mouseX < xPosition + width && mouseY < yPosition + height;

        Gui.drawRect(xPosition, yPosition, xPosition + width, yPosition + height, 0xFF1E1E1E);
        Gui.drawRect(xPosition, yPosition + height - 3, xPosition + width, yPosition + height, 0xFF2A2A2A);

        int fill = (int) Math.round(sliderValue * width);
        if (fill > 0) {
            Gui.drawRect(xPosition, yPosition + height - 3, xPosition + fill, yPosition + height,
                    hover ? 0xFF6CF2A0 : 0xFF4FD08C);
        }

        int textColor = this.enabled ? 0xFFFFFF : 0x808080;
        mc.fontRenderer.drawStringWithShadow(displayString,
                xPosition + width / 2 - mc.fontRenderer.getStringWidth(displayString) / 2,
                yPosition + (height - 8) / 2 - 2, textColor);
    }
}
