package net.ciebus.pinmod.common.data;

public class PinData {

    public static final int DEFAULT_LIFETIME_MS = 5000;

    public double x;
    public double y;
    public double z;
    public String player;
    public int dimId;

    // 每帧由 PinRenderer 更新，用于屏幕方向箭头
    public double screenX;
    public double screenY;
    public double screenZ;
    public boolean behind;
    public boolean hasScreenPos;

    // 世界空间竖线顶端（命中点沿世界 Y 轴抬高 lineHeight）的屏幕投影
    public double screenTopX;
    public double screenTopY;
    public boolean hasTop;

    public final int lifetimeMs;
    public long createdAt;

    public PinData(double x, double y, double z, String player, int dimId) {
        this(x, y, z, player, dimId, DEFAULT_LIFETIME_MS, DEFAULT_LIFETIME_MS);
    }

    public PinData(double x, double y, double z, String player, int dimId, int remainingMs) {
        this(x, y, z, player, dimId, remainingMs, DEFAULT_LIFETIME_MS);
    }

    public PinData(double x, double y, double z, String player, int dimId,
                   int remainingMs, int lifetimeMs) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.player = player;
        this.dimId = dimId;
        this.lifetimeMs = Math.max(1, lifetimeMs);

        int remaining = Math.max(0, Math.min(this.lifetimeMs, remainingMs));
        this.createdAt = System.currentTimeMillis() - (this.lifetimeMs - remaining);
    }

    public long getRemainingMs() {
        return createdAt + lifetimeMs - System.currentTimeMillis();
    }

    public boolean isExpired() {
        return getRemainingMs() <= 0L;
    }

    public float getAlpha() {
        long now = System.currentTimeMillis();
        long remaining = createdAt + lifetimeMs - now;
        if (remaining <= 0L) {
            return 0.0F;
        }

        long age = now - createdAt;
        float fadeIn = Math.min(1.0F, age / 150.0F);
        float fadeOut = remaining >= 500L ? 1.0F : remaining / 500.0F;
        return fadeIn * fadeOut;
    }
}
