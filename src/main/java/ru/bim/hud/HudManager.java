package ru.bim.hud;

import java.util.LinkedHashMap;
import java.util.Map;

public class HudManager {
    public static boolean showWatermark = true;
    public static boolean showPing = true;
    public static boolean showCoordinates = true;
    public static boolean showArrayList = true;
    public static boolean showInventory = true;
    public static boolean showActivePotion = true;
    public static boolean showTargetHUD = true;

    // Список функций (и HUD, и геймплейные модули)
    public static Map<String, Boolean> modules = new LinkedHashMap<>();

    static {
        // HUD элементы
        modules.put("WaterMark", showWatermark);
        modules.put("Ping", showPing);
        modules.put("Coordinates", showCoordinates);
        modules.put("ArrayList", showArrayList);
        modules.put("Inventory", showInventory);
        modules.put("ActivePotion", showActivePotion);
        modules.put("TargetHUD", showTargetHUD);

        // Игровые функции (добавляй свои)
        modules.put("Sprint", false);
        modules.put("Fly", false);
        modules.put("KillAura", false);
        modules.put("ESP", false);
    }
}
