package ru.bim.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.bim.util.RenderUtil;

import java.awt.*;
import java.util.Comparator;
import java.util.List;

@Mod.EventBusSubscriber(modid = "multi", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ArrayList {

    private static final int PADDING = 8;
    private static final int CORNER_RADIUS = 2;
    private static final int LINE_HEIGHT = 14;

    private static final Color PINK_COLOR = new Color(255, 0, 128);
    private static final Color TEAL_COLOR = new Color(0, 255, 200);

    // позиция списка
    private static int posX = 5;
    private static int posY = 160;

    // для перетаскивания
    private static boolean dragging = false;
    private static int dragOffsetX;
    private static int dragOffsetY;

    @SubscribeEvent
    public static void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;
        if (!HudManager.showArrayList) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // Собираем только HUD элементы
        List<String> activeHud = new java.util.ArrayList<>();
        if (HudManager.showWatermark) activeHud.add("WaterMark");
        if (HudManager.showPing) activeHud.add("Ping");
        if (HudManager.showCoordinates) activeHud.add("Coordinates");
        if (HudManager.showArrayList) activeHud.add("ArrayList");
        if (HudManager.showInventory) activeHud.add("Inventory");
        if (HudManager.showActivePotion) activeHud.add("ActivePotion");
        if (HudManager.showTargetHUD) activeHud.add("TargetHUD");

        if (activeHud.isEmpty()) return;

        // сортировка
        activeHud.sort(
                Comparator.comparingInt(String::length).reversed()
                        .thenComparing(Comparator.naturalOrder())
        );

        // Цвета для текста
        Color[] currentColors = getAnimatedColors();

        // рисуем каждый модуль отдельным боксом
        int y = posY;
        for (String module : activeHud) {
            int textWidth = mc.font.width(module);
            int boxWidth = textWidth + PADDING * 2;
            int boxHeight = LINE_HEIGHT;

            int x = posX;

            RenderUtil.drawShadow(x, y, boxWidth, boxHeight, 2f, new Color(0, 255, 0, 180));
            RenderUtil.drawRound(x, y, boxWidth, boxHeight, CORNER_RADIUS, Color.BLACK);

            drawGradientText(event, module, x + PADDING, y + (LINE_HEIGHT - 10) / 2,
                    currentColors[0], currentColors[1]);

            y += LINE_HEIGHT + 1;
        }
    }

    // обработка мыши
    @SubscribeEvent
    public static void onMouseClick(GuiScreenEvent.MouseClickedEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.screen instanceof ChatScreen)) return; // только в чате

        int mouseX = (int) event.getMouseX();
        int mouseY = (int) event.getMouseY();

        // проверяем клик по области заголовка (верхний бокс)
        if (event.getButton() == 0) {
            if (mouseX >= posX && mouseX <= posX + 100 && mouseY >= posY && mouseY <= posY + LINE_HEIGHT) {
                dragging = true;
                dragOffsetX = mouseX - posX;
                dragOffsetY = mouseY - posY;
            }
        }
    }

    @SubscribeEvent
    public static void onMouseRelease(GuiScreenEvent.MouseReleasedEvent.Pre event) {
        dragging = false;
    }

    @SubscribeEvent
    public static void onMouseDrag(GuiScreenEvent.MouseDragEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.screen instanceof ChatScreen)) return;
        if (dragging) {
            posX = (int) event.getMouseX() - dragOffsetX;
            posY = (int) event.getMouseY() - dragOffsetY;
        }
    }

    private static Color[] getAnimatedColors() {
        long time = System.currentTimeMillis();
        float progress = (time % 2000) / 2000.0f;
        return new Color[]{
                RenderUtil.interpolateColor(PINK_COLOR, TEAL_COLOR, progress),
                RenderUtil.interpolateColor(TEAL_COLOR, PINK_COLOR, progress)
        };
    }

    private static void drawGradientText(RenderGameOverlayEvent.Post event, String text, int x, int y,
                                         Color startColor, Color endColor) {
        Minecraft mc = Minecraft.getInstance();
        int currentX = x;
        for (int i = 0; i < text.length(); i++) {
            String charStr = String.valueOf(text.charAt(i));
            float progress = text.length() > 1 ? (float) i / (text.length() - 1) : 0.5f;
            Color color = RenderUtil.interpolateColor(startColor, endColor, progress);
            mc.font.draw(event.getMatrixStack(), charStr, currentX, y, color.getRGB());
            currentX += mc.font.width(charStr);
        }
    }
}
