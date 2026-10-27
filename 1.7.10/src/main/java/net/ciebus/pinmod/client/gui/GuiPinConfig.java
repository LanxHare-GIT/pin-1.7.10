package net.ciebus.pinmod.client.gui;

import cpw.mods.fml.client.config.GuiSlider;
import net.ciebus.pinmod.client.render.PinShapes;
import net.ciebus.pinmod.common.config.PinConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;

public class GuiPinConfig extends GuiScreen {

    private static final int PAGE_MAIN = 0;
    private static final int PAGE_MARKER = 1;
    private static final int PAGE_LINE = 2;
    private static final int PAGE_GENERAL = 3;
    private static final int PAGE_LABEL = 4;

    private final GuiScreen parent;
    private int page;

    private int preset;

    private boolean showDot;
    private int dotColor;
    private float dotSize;
    private float farGrowth;
    private boolean lineEnabled;
    private boolean lineWorldSpace;
    private boolean lineSolid;
    private float lineHeight;
    private int lineColor;
    private float lineWidth;
    private float lineLength;
    private int lifetimeSeconds;
    private int outsideShape;
    private int labelGap;
    private int labelOffsetX;
    private int labelOffsetY;

    private GuiButton showDotToggle;
    private GuiSlider dotSizeSlider;
    private GuiSlider farSlider;
    private GuiSlider dotRedSlider;
    private GuiSlider dotGreenSlider;
    private GuiSlider dotBlueSlider;

    private GuiButton lineToggle;
    private GuiButton worldToggle;
    private GuiButton solidToggle;
    private GuiSlider lineWidthSlider;
    private GuiSlider lineLengthSlider;
    private GuiSlider lineHeightSlider;
    private GuiSlider lineRedSlider;
    private GuiSlider lineGreenSlider;
    private GuiSlider lineBlueSlider;

    private GuiSlider lifetimeSlider;
    private GuiButton outsideShapeToggle;
    private GuiSlider labelGapSlider;
    private GuiSlider labelOffsetXSlider;
    private GuiSlider labelOffsetYSlider;

    public GuiPinConfig() {
        this(Minecraft.getMinecraft().currentScreen);
    }

    public GuiPinConfig(GuiScreen parent) {
        this.parent = parent;
        this.page = PAGE_MAIN;

        this.preset = PinConfig.preset;

        this.showDot = PinConfig.showDot;
        this.dotColor = PinConfig.dotColor;
        this.dotSize = PinConfig.dotSize;
        this.farGrowth = PinConfig.farGrowth;

        this.lineEnabled = PinConfig.lineEnabled;
        this.lineWorldSpace = PinConfig.lineWorldSpace;
        this.lineSolid = PinConfig.lineSolid;
        this.lineHeight = PinConfig.lineHeight;
        this.lineColor = PinConfig.lineColor;
        this.lineWidth = PinConfig.lineWidth;
        this.lineLength = PinConfig.lineLength;

        this.lifetimeSeconds = PinConfig.lifetimeSeconds;
        this.outsideShape = PinConfig.outsideShape;
        this.labelGap = PinConfig.labelGap;
        this.labelOffsetX = PinConfig.labelOffsetX;
        this.labelOffsetY = PinConfig.labelOffsetY;
    }

    @Override
    public void initGui() {
        buttonList.clear();

        int center = width / 2;
        int left = center - 160;
        int y = 60;

        if (page == PAGE_MAIN) {
            GuiButton vertical = new GuiButton(500, left, y, 150, 20, presetText(PinConfig.PRESET_VERTICAL));
            GuiButton callout = new GuiButton(501, left, y + 25, 150, 20, presetText(PinConfig.PRESET_CALLOUT));
            vertical.packedFGColour = preset == PinConfig.PRESET_VERTICAL ? 0x5CF2A0 : 0xFFFFFF;
            callout.packedFGColour = preset == PinConfig.PRESET_CALLOUT ? 0x5CF2A0 : 0xFFFFFF;

            buttonList.add(vertical);
            buttonList.add(callout);
            buttonList.add(new GuiButton(502, left, y + 53, 150, 20, tr("pin.gui.markerSettings")));
            buttonList.add(new GuiButton(503, left, y + 76, 150, 20, tr("pin.gui.lineSettings")));
            buttonList.add(new GuiButton(505, left, y + 99, 150, 20, tr("pin.gui.labelSettings")));
            buttonList.add(new GuiButton(504, left, y + 122, 150, 20, tr("pin.gui.generalSettings")));
        } else {
            buttonList.add(new GuiButton(203, center - 40, 32, 80, 20, tr("pin.gui.back")));

            if (page == PAGE_MARKER) {
                showDotToggle = new GuiButton(401, left, y, 150, 20, showDotText());
                dotSizeSlider = new GuiSlider(1, left, y + 25, 150, 20,
                        tr("pin.gui.dotSize") + ": ", "px",
                        4.0D, 24.0D, dotSize, false, true);
                farSlider = new GuiSlider(2, left, y + 50, 150, 20,
                        tr("pin.gui.farGrowth") + ": ", "%",
                        0.0D, 200.0D, farGrowth * 100.0F, false, true);
                dotRedSlider = new GuiSlider(3, left, y + 75, 150, 20,
                        "R: ", "", 0.0D, 255.0D, (dotColor >> 16) & 0xFF, false, true);
                dotGreenSlider = new GuiSlider(4, left, y + 100, 150, 20,
                        "G: ", "", 0.0D, 255.0D, (dotColor >> 8) & 0xFF, false, true);
                dotBlueSlider = new GuiSlider(5, left, y + 125, 150, 20,
                        "B: ", "", 0.0D, 255.0D, dotColor & 0xFF, false, true);

                buttonList.add(showDotToggle);
                buttonList.add(dotSizeSlider);
                buttonList.add(farSlider);
                buttonList.add(dotRedSlider);
                buttonList.add(dotGreenSlider);
                buttonList.add(dotBlueSlider);
            } else if (page == PAGE_LINE) {
                lineToggle = new GuiButton(400, left, y, 150, 20, lineToggleText());
                worldToggle = new GuiButton(402, left, y + 20, 73, 20, worldToggleText());
                solidToggle = new GuiButton(403, left + 77, y + 20, 73, 20, solidToggleText());
                lineWidthSlider = new GuiSlider(6, left, y + 40, 150, 20,
                        tr("pin.gui.lineWidth") + ": ", "px",
                        1.0D, 6.0D, lineWidth, false, true);
                if (lineWorldSpace || preset == PinConfig.PRESET_CALLOUT) {
                    lineHeightSlider = new GuiSlider(12, left, y + 60, 150, 20,
                            tr("pin.gui.lineHeight") + ": ", "m",
                            0.2D, 2.0D, lineHeight, false, true);
                    lineLengthSlider = null;
                } else {
                    lineLengthSlider = new GuiSlider(7, left, y + 60, 150, 20,
                            tr("pin.gui.lineLength") + ": ", "px",
                            8.0D, 64.0D, lineLength, false, true);
                    lineHeightSlider = null;
                }
                lineRedSlider = new GuiSlider(8, left, y + 80, 150, 20,
                        "R: ", "", 0.0D, 255.0D, (lineColor >> 16) & 0xFF, false, true);
                lineGreenSlider = new GuiSlider(9, left, y + 100, 150, 20,
                        "G: ", "", 0.0D, 255.0D, (lineColor >> 8) & 0xFF, false, true);
                lineBlueSlider = new GuiSlider(10, left, y + 120, 150, 20,
                        "B: ", "", 0.0D, 255.0D, lineColor & 0xFF, false, true);

                buttonList.add(lineToggle);
                buttonList.add(worldToggle);
                buttonList.add(solidToggle);
                buttonList.add(lineWidthSlider);
                if (lineWorldSpace || preset == PinConfig.PRESET_CALLOUT) {
                    buttonList.add(lineHeightSlider);
                } else {
                    buttonList.add(lineLengthSlider);
                }
                buttonList.add(lineRedSlider);
                buttonList.add(lineGreenSlider);
                buttonList.add(lineBlueSlider);
            } else if (page == PAGE_LABEL) {
                labelGapSlider = new GuiSlider(13, left, y, 150, 20,
                        tr("pin.gui.labelGap") + ": ", "px",
                        0.0D, 16.0D, labelGap, false, true);
                labelOffsetXSlider = new GuiSlider(14, left, y + 25, 150, 20,
                        tr("pin.gui.labelOffsetX") + ": ", "px",
                        -48.0D, 48.0D, labelOffsetX, false, true);
                labelOffsetYSlider = new GuiSlider(15, left, y + 50, 150, 20,
                        tr("pin.gui.labelOffsetY") + ": ", "px",
                        -48.0D, 48.0D, labelOffsetY, false, true);
                buttonList.add(labelGapSlider);
                buttonList.add(labelOffsetXSlider);
                buttonList.add(labelOffsetYSlider);
            } else {
                lifetimeSlider = new GuiSlider(11, left, y, 150, 20,
                        tr("pin.gui.autoRemove") + ": ", "s",
                        1.0D, 30.0D, lifetimeSeconds, false, true);
                outsideShapeToggle = new GuiButton(406, left, y + 25, 150, 20, outsideShapeText());
                buttonList.add(lifetimeSlider);
                buttonList.add(outsideShapeToggle);
            }
        }

        buttonList.add(new GuiButton(200, center - 100, height - 30, 65, 20, tr("pin.gui.done")));
        buttonList.add(new GuiButton(201, center - 30, height - 30, 65, 20, tr("pin.gui.reset")));
        buttonList.add(new GuiButton(202, center + 40, height - 30, 65, 20, tr("pin.gui.cancel")));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        int center = width / 2;
        drawRect(center - 160, 24, center + 160, height - 8, 0xAA000000);
        super.drawScreen(mouseX, mouseY, partialTicks);

        drawCenteredString(fontRendererObj, tr("pin.gui.title"), center, 14, 0xFFFFFF);

        if (page == PAGE_MAIN) {
            drawCenteredString(fontRendererObj, tr("pin.gui.presetTitle"), center, 44, 0xA0A0A0);
        } else if (page == PAGE_LABEL) {
            drawCenteredString(fontRendererObj, tr("pin.gui.labelHint"), center, 44, 0xA0A0A0);
        }

        int previewX = center + 20;
        int previewY = 58;
        int previewW = 100;
        int previewH = 100;
        drawRect(previewX, previewY, previewX + previewW, previewY + previewH, 0x66000000);
        fontRendererObj.drawStringWithShadow(tr("pin.gui.preview"),
                previewX + 5, previewY + 5, 0xFFFFFF);

        drawPreview(previewX, previewY, previewW, previewH);

        int dotPreviewColor = previewDotColor();
        int linePreviewColor = previewLineColor();

        drawRect(previewX + 6, previewY + previewH + 8,
                previewX + 20, previewY + previewH + 22, 0xFF000000 | dotPreviewColor);
        fontRendererObj.drawStringWithShadow(tr("pin.gui.dot"),
                previewX + 24, previewY + previewH + 10, 0xFFFFFF);

        drawRect(previewX + 6, previewY + previewH + 28,
                previewX + 20, previewY + previewH + 42, 0xFF000000 | linePreviewColor);
        fontRendererObj.drawStringWithShadow(tr("pin.gui.line"),
                previewX + 24, previewY + previewH + 30, 0xFFFFFF);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void drawPreview(int previewX, int previewY, int previewW, int previewH) {
        if (preset == PinConfig.PRESET_CALLOUT) {
            drawPreviewCallout(previewX, previewY, previewW, previewH);
            return;
        }

        int centerX = previewX + previewW / 2;
        int baseY = previewY + previewH - 18;

        float previewFar = previewFarGrowth();
        int dotPx = clamp((int) Math.round(previewDotSize() * (1.0F + previewFar * 0.5F)), 4, 32);
        int lineW = clamp((int) Math.round(previewLineWidth()), 1, 6);

        float linePx = 0.0F;
        float dirX = 0.0F;
        float dirY = -1.0F;
        if (previewLineEnabled()) {
            if (lineWorldSpace) {
                linePx = clamp(Math.round(previewLineHeight() * 50.0F), 10, 90);
                double angle = Math.toRadians(20.0D);
                dirX = (float) Math.sin(angle);
                dirY = (float) -Math.cos(angle);
            } else {
                linePx = clamp(Math.round(previewLineLength()), 0, 40);
            }
        }

        float anchorX = centerX + dirX * linePx;
        float anchorY = baseY + dirY * linePx;

        int dotColor = previewDotColor();
        int lineColor = previewLineColor();

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);

        if (linePx > 0.0F && previewLineEnabled()) {
            PinShapes.drawLine(centerX, baseY, anchorX, anchorY, lineW, lineColor, 1.0F, lineSolid);
        }

        if (showDot) {
            PinShapes.drawDiamondMarker(anchorX, anchorY, dotPx, 1.0F, dotColor);
        }

        preparePreviewText();
        int textX = Math.round(anchorX + dotPx / 2.0F + previewLabelGap() + previewLabelOffsetX());
        int textY = Math.round(anchorY + previewLabelOffsetY() + 5 - fontRendererObj.FONT_HEIGHT);
        textX = clamp(textX, previewX + 2,
                previewX + previewW - 2 - fontRendererObj.getStringWidth("Pin"));
        textY = clamp(textY, previewY + 12, previewY + previewH - 12);
        fontRendererObj.drawStringWithShadow("Pin", textX, textY, 0xFFFFFF);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPopAttrib();
    }

    private void drawPreviewCallout(int previewX, int previewY, int previewW, int previewH) {
        int baseX = previewX + 22;
        int baseY = previewY + previewH - 26;
        int rise = clamp(Math.round(previewLineHeight() * 40.0F), 4, 40);
        int elbowX = baseX + rise;
        int elbowY = baseY - rise;
        int endX = elbowX + 30;

        float previewFar = previewFarGrowth();
        int dotPx = clamp((int) Math.round(previewDotSize() * (1.0F + previewFar * 0.5F)), 4, 20);
        int lineW = clamp((int) Math.round(previewLineWidth()), 1, 6);

        int dotColor = previewDotColor();
        int lineColor = previewLineColor();

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);

        if (previewLineEnabled()) {
            PinShapes.drawPolyline(
                    new float[]{baseX, elbowX, endX},
                    new float[]{baseY, elbowY, elbowY},
                    3, lineW, lineColor, 1.0F, lineSolid);
        }

        if (showDot) {
            PinShapes.drawDiamondMarker(baseX, baseY, dotPx, 1.0F, dotColor);
        }

        preparePreviewText();
        int textX = Math.round(elbowX + previewLabelOffsetX());
        int textY = Math.round(elbowY - previewLabelGap() + previewLabelOffsetY()
                - fontRendererObj.FONT_HEIGHT);
        textX = clamp(textX, previewX + 2,
                previewX + previewW - 2 - fontRendererObj.getStringWidth("Pin"));
        textY = clamp(textY, previewY + 12, previewY + previewH - 12);
        fontRendererObj.drawStringWithShadow("Pin", textX, textY, 0xFFFFFF);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPopAttrib();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 203) {
            captureCurrentPage();
            page = PAGE_MAIN;
            initGui();
            return;
        }

        if (button.id == 500) {
            preset = PinConfig.PRESET_VERTICAL;
            initGui();
            return;
        }

        if (button.id == 501) {
            preset = PinConfig.PRESET_CALLOUT;
            initGui();
            return;
        }

        if (button.id == 502) {
            page = PAGE_MARKER;
            initGui();
            return;
        }

        if (button.id == 503) {
            page = PAGE_LINE;
            initGui();
            return;
        }

        if (button.id == 504) {
            page = PAGE_GENERAL;
            initGui();
            return;
        }

        if (button.id == 505) {
            page = PAGE_LABEL;
            initGui();
            return;
        }

        if (button.id == 400) {
            lineEnabled = !lineEnabled;
            lineToggle.displayString = lineToggleText();
            return;
        }

        if (button.id == 401) {
            showDot = !showDot;
            showDotToggle.displayString = showDotText();
            return;
        }

        if (button.id == 402) {
            captureCurrentPage();
            lineWorldSpace = !lineWorldSpace;
            initGui();
            return;
        }

        if (button.id == 403) {
            lineSolid = !lineSolid;
            solidToggle.displayString = solidToggleText();
            return;
        }

        if (button.id == 406) {
            outsideShape = outsideShape == PinConfig.OUTSIDE_SHAPE_ARROW
                    ? PinConfig.OUTSIDE_SHAPE_DIAMOND
                    : PinConfig.OUTSIDE_SHAPE_ARROW;
            outsideShapeToggle.displayString = outsideShapeText();
            return;
        }

        if (button.id == 200) {
            captureCurrentPage();
            applyToConfig();
            mc.displayGuiScreen(parent);
        } else if (button.id == 201) {
            resetLocalDefaults();
            page = PAGE_MAIN;
            initGui();
        } else if (button.id == 202) {
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == 1) {
            if (page == PAGE_MAIN) {
                mc.displayGuiScreen(parent);
            } else {
                captureCurrentPage();
                page = PAGE_MAIN;
                initGui();
            }
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    private void captureCurrentPage() {
        if (page == PAGE_MARKER) {
            if (dotSizeSlider != null) {
                dotSize = (float) dotSizeSlider.getValue();
            }
            if (farSlider != null) {
                farGrowth = (float) farSlider.getValue() / 100.0F;
            }
            if (dotRedSlider != null) {
                dotColor = (int) dotRedSlider.getValue() << 16
                        | (int) dotGreenSlider.getValue() << 8
                        | (int) dotBlueSlider.getValue();
            }
        } else if (page == PAGE_LINE) {
            if (lineWidthSlider != null) {
                lineWidth = (float) lineWidthSlider.getValue();
            }
            if (lineLengthSlider != null) {
                lineLength = (float) lineLengthSlider.getValue();
            }
            if (lineHeightSlider != null) {
                lineHeight = (float) lineHeightSlider.getValue();
            }
            if (lineRedSlider != null) {
                lineColor = (int) lineRedSlider.getValue() << 16
                        | (int) lineGreenSlider.getValue() << 8
                        | (int) lineBlueSlider.getValue();
            }
        } else if (page == PAGE_LABEL) {
            if (labelGapSlider != null) {
                labelGap = (int) labelGapSlider.getValue();
            }
            if (labelOffsetXSlider != null) {
                labelOffsetX = (int) labelOffsetXSlider.getValue();
            }
            if (labelOffsetYSlider != null) {
                labelOffsetY = (int) labelOffsetYSlider.getValue();
            }
        } else if (page == PAGE_GENERAL) {
            if (lifetimeSlider != null) {
                lifetimeSeconds = (int) lifetimeSlider.getValue();
            }
        }
    }

    private void applyToConfig() {
        PinConfig.preset = preset;
        PinConfig.showDot = showDot;
        PinConfig.dotColor = dotColor;
        PinConfig.dotSize = dotSize;
        PinConfig.farGrowth = farGrowth;
        PinConfig.lineEnabled = lineEnabled;
        PinConfig.lineWorldSpace = lineWorldSpace;
        PinConfig.lineSolid = lineSolid;
        PinConfig.lineHeight = lineHeight;
        PinConfig.lineColor = lineColor;
        PinConfig.lineWidth = lineWidth;
        PinConfig.lineLength = lineLength;
        PinConfig.lifetimeSeconds = lifetimeSeconds;
        PinConfig.outsideShape = outsideShape;
        PinConfig.labelGap = labelGap;
        PinConfig.labelOffsetX = labelOffsetX;
        PinConfig.labelOffsetY = labelOffsetY;
        PinConfig.save();
    }

    private void resetLocalDefaults() {
        preset = PinConfig.DEFAULT_PRESET;
        showDot = PinConfig.DEFAULT_SHOW_DOT;
        dotColor = PinConfig.DEFAULT_DOT_COLOR;
        dotSize = PinConfig.DEFAULT_DOT_SIZE;
        farGrowth = PinConfig.DEFAULT_FAR_GROWTH;
        lineEnabled = PinConfig.DEFAULT_LINE_ENABLED;
        lineWorldSpace = PinConfig.DEFAULT_LINE_WORLD_SPACE;
        lineSolid = PinConfig.DEFAULT_LINE_SOLID;
        lineHeight = PinConfig.DEFAULT_LINE_HEIGHT;
        lineColor = PinConfig.DEFAULT_LINE_COLOR;
        lineWidth = PinConfig.DEFAULT_LINE_WIDTH;
        lineLength = PinConfig.DEFAULT_LINE_LENGTH;
        lifetimeSeconds = PinConfig.DEFAULT_LIFETIME_SECONDS;
        outsideShape = PinConfig.DEFAULT_OUTSIDE_SHAPE;
        labelGap = PinConfig.DEFAULT_LABEL_GAP;
        labelOffsetX = PinConfig.DEFAULT_LABEL_OFFSET_X;
        labelOffsetY = PinConfig.DEFAULT_LABEL_OFFSET_Y;
    }

    private int previewDotColor() {
        if (page == PAGE_MARKER && dotRedSlider != null) {
            return ((int) dotRedSlider.getValue() << 16)
                    | ((int) dotGreenSlider.getValue() << 8)
                    | (int) dotBlueSlider.getValue();
        }
        return dotColor;
    }

    private int previewLineColor() {
        if (page == PAGE_LINE && lineRedSlider != null) {
            return ((int) lineRedSlider.getValue() << 16)
                    | ((int) lineGreenSlider.getValue() << 8)
                    | (int) lineBlueSlider.getValue();
        }
        return lineColor;
    }

    private float previewDotSize() {
        return page == PAGE_MARKER && dotSizeSlider != null
                ? (float) dotSizeSlider.getValue() : dotSize;
    }

    private float previewFarGrowth() {
        return page == PAGE_MARKER && farSlider != null
                ? (float) farSlider.getValue() / 100.0F : farGrowth;
    }

    private float previewLineWidth() {
        return page == PAGE_LINE && lineWidthSlider != null
                ? (float) lineWidthSlider.getValue() : lineWidth;
    }

    private float previewLineLength() {
        return page == PAGE_LINE && !lineWorldSpace && preset != PinConfig.PRESET_CALLOUT
                && lineLengthSlider != null
                ? (float) lineLengthSlider.getValue() : lineLength;
    }

    private float previewLineHeight() {
        boolean heightMode = lineWorldSpace || preset == PinConfig.PRESET_CALLOUT;
        return page == PAGE_LINE && heightMode && lineHeightSlider != null
                ? (float) lineHeightSlider.getValue() : lineHeight;
    }

    private int previewLabelGap() {
        return page == PAGE_LABEL && labelGapSlider != null
                ? (int) labelGapSlider.getValue() : labelGap;
    }

    private int previewLabelOffsetX() {
        return page == PAGE_LABEL && labelOffsetXSlider != null
                ? (int) labelOffsetXSlider.getValue() : labelOffsetX;
    }

    private int previewLabelOffsetY() {
        return page == PAGE_LABEL && labelOffsetYSlider != null
                ? (int) labelOffsetYSlider.getValue() : labelOffsetY;
    }

    private static void preparePreviewText() {
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private boolean previewLineEnabled() {
        return lineEnabled;
    }

    private String presetText(int value) {
        String key = value == PinConfig.PRESET_CALLOUT
                ? "pin.gui.preset.callout" : "pin.gui.preset.vertical";
        return tr("pin.gui.preset") + ": " + tr(key);
    }

    private String lineToggleText() {
        return tr("pin.gui.lineEnabled") + ": "
                + tr(lineEnabled ? "pin.gui.on" : "pin.gui.off");
    }

    private String showDotText() {
        return tr("pin.gui.showDot") + ": "
                + tr(showDot ? "pin.gui.on" : "pin.gui.off");
    }

    private String worldToggleText() {
        return tr("pin.gui.lineWorld") + ": "
                + tr(lineWorldSpace ? "pin.gui.on" : "pin.gui.off");
    }

    private String solidToggleText() {
        return tr("pin.gui.lineSolid") + ": "
                + tr(lineSolid ? "pin.gui.on" : "pin.gui.off");
    }

    private String outsideShapeText() {
        String key = outsideShape == PinConfig.OUTSIDE_SHAPE_DIAMOND
                ? "pin.gui.outside.diamond" : "pin.gui.outside.arrow";
        return tr("pin.gui.outsideShape") + ": " + tr(key);
    }

    private static String tr(String key) {
        return StatCollector.translateToLocal(key);
    }

    private static int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }
}
