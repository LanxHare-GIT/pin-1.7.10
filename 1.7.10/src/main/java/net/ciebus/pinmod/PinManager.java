package net.ciebus.pinmod;

import net.ciebus.pinmod.common.data.PinData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PinManager {

    // TODO: Load from database
    private static final Map<String, PinData> PINS = new ConcurrentHashMap<String, PinData>();

    private static final Collection<PinData> PINS_VIEW = Collections.unmodifiableCollection(PINS.values());

    public static void addPin(double x, double y, double z, String player, int dimId) {
        addPin(x, y, z, player, dimId, PinData.DEFAULT_LIFETIME_MS, PinData.DEFAULT_LIFETIME_MS);
    }

    public static void addPin(double x, double y, double z, String player, int dimId, int remainingMs) {
        addPin(x, y, z, player, dimId, remainingMs, remainingMs);
    }

    public static void addPin(double x, double y, double z, String player, int dimId,
                              int remainingMs, int lifetimeMs) {
        PINS.put(player, new PinData(x, y, z, player, dimId, remainingMs, lifetimeMs));
    }

    public static void removePin(String player) {
        PINS.remove(player);
    }

    public static void clearPins() {
        PINS.clear();
    }

    public static List<PinData> removeExpired() {
        List<PinData> expired = new ArrayList<PinData>();
        for (PinData pin : PINS.values()) {
            if (pin.isExpired()) {
                expired.add(pin);
            }
        }
        for (PinData pin : expired) {
            if (PINS.get(pin.player) == pin) {
                PINS.remove(pin.player);
            }
        }
        return expired;
    }

    public static boolean isToDelete(double hitX, double hitY, double hitZ, String player) {
        PinData pin = PINS.get(player);
        if (pin != null) {
            return Math.round(hitX) == Math.round(pin.x) && Math.round(hitY) == Math.round(pin.y) && Math.round(hitZ) == Math.round(pin.z);
        }
        return false;
    }

    public static Collection<? extends PinData> pins() {
        return PINS_VIEW;
    }

}
