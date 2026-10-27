package net.ciebus.pinmod.common.config;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.config.Configuration;

public final class PinConfig {

    public static final int PRESET_VERTICAL = 0;
    public static final int PRESET_CALLOUT = 1;
    public static final int DEFAULT_PRESET = PRESET_VERTICAL;

    public static final boolean DEFAULT_SHOW_DOT = true;
    public static final int DEFAULT_DOT_COLOR = 0x5CF2A0;
    public static final float DEFAULT_DOT_SIZE = 8.0F;
    public static final float DEFAULT_FAR_GROWTH = 1.0F;

    public static final boolean DEFAULT_LINE_ENABLED = true;
    public static final boolean DEFAULT_LINE_WORLD_SPACE = false;
    public static final boolean DEFAULT_LINE_SOLID = false;
    public static final float DEFAULT_LINE_HEIGHT = 0.8F;
    public static final int DEFAULT_LINE_COLOR = 0xFFFFFF;
    public static final float DEFAULT_LINE_WIDTH = 2.0F;
    public static final float DEFAULT_LINE_LENGTH = 24.0F;

    public static final int OUTSIDE_SHAPE_ARROW = 0;
    public static final int OUTSIDE_SHAPE_DIAMOND = 1;
    public static final int DEFAULT_OUTSIDE_SHAPE = OUTSIDE_SHAPE_ARROW;

    public static final int DEFAULT_LIFETIME_SECONDS = 5;
    public static final int DEFAULT_LABEL_GAP = 6;
    public static final int DEFAULT_LABEL_OFFSET_X = 0;
    public static final int DEFAULT_LABEL_OFFSET_Y = 0;

    public static int preset = DEFAULT_PRESET;

    public static boolean showDot = DEFAULT_SHOW_DOT;
    public static int dotColor = DEFAULT_DOT_COLOR;
    public static float dotSize = DEFAULT_DOT_SIZE;
    public static float farGrowth = DEFAULT_FAR_GROWTH;

    public static boolean lineEnabled = DEFAULT_LINE_ENABLED;
    public static boolean lineWorldSpace = DEFAULT_LINE_WORLD_SPACE;
    public static boolean lineSolid = DEFAULT_LINE_SOLID;
    public static float lineHeight = DEFAULT_LINE_HEIGHT;
    public static int lineColor = DEFAULT_LINE_COLOR;
    public static float lineWidth = DEFAULT_LINE_WIDTH;
    public static float lineLength = DEFAULT_LINE_LENGTH;

    public static int lifetimeSeconds = DEFAULT_LIFETIME_SECONDS;
    public static int outsideShape = DEFAULT_OUTSIDE_SHAPE;
    public static int labelGap = DEFAULT_LABEL_GAP;
    public static int labelOffsetX = DEFAULT_LABEL_OFFSET_X;
    public static int labelOffsetY = DEFAULT_LABEL_OFFSET_Y;

    private static Configuration config;

    private PinConfig() {
    }

    public static void load(FMLPreInitializationEvent event) {
        config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();

        preset = config.getInt("preset", "display", DEFAULT_PRESET, PRESET_VERTICAL, PRESET_CALLOUT,
                "0 = vertical marker, 1 = callout leader line.");
        outsideShape = config.getInt("outsideShape", "display", DEFAULT_OUTSIDE_SHAPE,
                OUTSIDE_SHAPE_ARROW, OUTSIDE_SHAPE_DIAMOND,
                "Off-screen indicator shape: 0 = arrow, 1 = diamond.");

        showDot = config.getBoolean("showDot", "marker", DEFAULT_SHOW_DOT,
                "Draw the diamond marker. When disabled the label is anchored to the line top.");
        dotColor = config.getInt("dotColor", "marker", DEFAULT_DOT_COLOR, 0, 0xFFFFFF,
                "Diamond marker color as RGB hex.");
        dotSize = config.getFloat("dotSize", "marker", DEFAULT_DOT_SIZE, 4.0F, 24.0F,
                "Diamond marker base size in screen pixels.");
        farGrowth = config.getFloat("farGrowth", "marker", DEFAULT_FAR_GROWTH, 0.0F, 2.0F,
                "How strongly the marker grows with distance. 0 = fixed size.");

        lineEnabled = config.getBoolean("lineEnabled", "line", DEFAULT_LINE_ENABLED,
                "Draw a thin line below the diamond marker.");
        lineWorldSpace = config.getBoolean("lineWorldSpace", "line", DEFAULT_LINE_WORLD_SPACE,
                "Draw the line perpendicular to the ground (world Y axis) instead of the screen.");
        lineSolid = config.getBoolean("lineSolid", "line", DEFAULT_LINE_SOLID,
                "Draw a solid line without the soft glow edge.");
        lineHeight = config.getFloat("lineHeight", "line", DEFAULT_LINE_HEIGHT, 0.2F, 2.0F,
                "Line length in blocks when world-space mode is enabled.");
        lineColor = config.getInt("lineColor", "line", DEFAULT_LINE_COLOR, 0, 0xFFFFFF,
                "Line color as RGB hex.");
        lineWidth = config.getFloat("lineWidth", "line", DEFAULT_LINE_WIDTH, 1.0F, 6.0F,
                "Line thickness in screen pixels.");
        lineLength = config.getFloat("lineLength", "line", DEFAULT_LINE_LENGTH, 8.0F, 64.0F,
                "Line length in screen pixels.");

        lifetimeSeconds = config.getInt("lifetimeSeconds", "gameplay", DEFAULT_LIFETIME_SECONDS, 1, 30,
                "Auto-remove delay in seconds.");
        labelGap = config.getInt("labelGap", "label", DEFAULT_LABEL_GAP, 0, 16,
                "Gap between the label and the diamond (vertical preset) or the horizontal line (callout preset).");
        labelOffsetX = config.getInt("labelOffsetX", "label", DEFAULT_LABEL_OFFSET_X, -48, 48,
                "Horizontal offset of the label from the callout elbow / diamond.");
        labelOffsetY = config.getInt("labelOffsetY", "label", DEFAULT_LABEL_OFFSET_Y, -48, 48,
                "Vertical offset of the label from the callout line / diamond center.");

        config.save();
    }

    public static void save() {
        if (config == null) {
            return;
        }

        config.get("display", "preset", DEFAULT_PRESET).set(preset);
        config.get("display", "outsideShape", DEFAULT_OUTSIDE_SHAPE).set(outsideShape);

        config.get("marker", "showDot", DEFAULT_SHOW_DOT).set(showDot);
        config.get("marker", "dotColor", DEFAULT_DOT_COLOR).set(dotColor);
        config.get("marker", "dotSize", (double) DEFAULT_DOT_SIZE).set((double) dotSize);
        config.get("marker", "farGrowth", (double) DEFAULT_FAR_GROWTH).set((double) farGrowth);

        config.get("line", "lineEnabled", DEFAULT_LINE_ENABLED).set(lineEnabled);
        config.get("line", "lineWorldSpace", DEFAULT_LINE_WORLD_SPACE).set(lineWorldSpace);
        config.get("line", "lineSolid", DEFAULT_LINE_SOLID).set(lineSolid);
        config.get("line", "lineHeight", (double) DEFAULT_LINE_HEIGHT).set((double) lineHeight);
        config.get("line", "lineColor", DEFAULT_LINE_COLOR).set(lineColor);
        config.get("line", "lineWidth", (double) DEFAULT_LINE_WIDTH).set((double) lineWidth);
        config.get("line", "lineLength", (double) DEFAULT_LINE_LENGTH).set((double) lineLength);

        config.get("gameplay", "lifetimeSeconds", DEFAULT_LIFETIME_SECONDS).set(lifetimeSeconds);
        config.get("label", "labelGap", DEFAULT_LABEL_GAP).set(labelGap);
        config.get("label", "labelOffsetX", DEFAULT_LABEL_OFFSET_X).set(labelOffsetX);
        config.get("label", "labelOffsetY", DEFAULT_LABEL_OFFSET_Y).set(labelOffsetY);

        config.save();
    }

    public static void resetToDefaults() {
        preset = DEFAULT_PRESET;
        outsideShape = DEFAULT_OUTSIDE_SHAPE;
        showDot = DEFAULT_SHOW_DOT;
        dotColor = DEFAULT_DOT_COLOR;
        dotSize = DEFAULT_DOT_SIZE;
        farGrowth = DEFAULT_FAR_GROWTH;

        lineEnabled = DEFAULT_LINE_ENABLED;
        lineWorldSpace = DEFAULT_LINE_WORLD_SPACE;
        lineSolid = DEFAULT_LINE_SOLID;
        lineHeight = DEFAULT_LINE_HEIGHT;
        lineColor = DEFAULT_LINE_COLOR;
        lineWidth = DEFAULT_LINE_WIDTH;
        lineLength = DEFAULT_LINE_LENGTH;

        lifetimeSeconds = DEFAULT_LIFETIME_SECONDS;
        labelGap = DEFAULT_LABEL_GAP;
        labelOffsetX = DEFAULT_LABEL_OFFSET_X;
        labelOffsetY = DEFAULT_LABEL_OFFSET_Y;
    }

    public static int getLifetimeMs() {
        return lifetimeSeconds * 1000;
    }

    public static float getDotRed() {
        return ((dotColor >> 16) & 0xFF) / 255.0F;
    }

    public static float getDotGreen() {
        return ((dotColor >> 8) & 0xFF) / 255.0F;
    }

    public static float getDotBlue() {
        return (dotColor & 0xFF) / 255.0F;
    }

    public static float getLineRed() {
        return ((lineColor >> 16) & 0xFF) / 255.0F;
    }

    public static float getLineGreen() {
        return ((lineColor >> 8) & 0xFF) / 255.0F;
    }

    public static float getLineBlue() {
        return (lineColor & 0xFF) / 255.0F;
    }

    public static int getDotTextColor() {
        int red = (dotColor >> 16) & 0xFF;
        int green = (dotColor >> 8) & 0xFF;
        int blue = dotColor & 0xFF;

        red = (red + 255) / 2;
        green = (green + 255) / 2;
        blue = (blue + 255) / 2;
        return (red << 16) | (green << 8) | blue;
    }
}
