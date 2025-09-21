package ru.bim.hud;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.bim.util.RenderUtil;

import java.awt.*;

@Mod.EventBusSubscriber(modid = "multi", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class coordinate {

    private static final int PADDING = 5;
    private static final int CORNER_RADIUS = 3;
    private static final int ELEMENT_HEIGHT = 15;

    private static final Color PINK_COLOR = new Color(255, 0, 128);    // Розовый
    private static final Color TEAL_COLOR = new Color(0, 255, 200);    // Бирюзовый

    @SubscribeEvent
    public static void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        // Получаем координаты
        String coordText = getCoordinates();
        int textWidth = mc.font.width(coordText);
        int elementWidth = textWidth + PADDING * 2;

        // Позиция в левом верхнем углу (под пингом)
        int xPos = 500; // Отступ от левого края
        int yPos = 3; // Отступ от верхнего края (ниже пинга)

        // Получаем текущие анимированные цвета
        Color[] currentColors = getAnimatedColors();

        // Рисуем черный фон с плавными углами
        RenderUtil.drawRound(xPos, yPos, elementWidth, ELEMENT_HEIGHT, CORNER_RADIUS, Color.BLACK);

        // Рисуем текст с градиентом
        drawGradientText(event, coordText, xPos + PADDING, yPos + 4, currentColors[0], currentColors[1]);
    }

    private static String getCoordinates() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            // Получаем координаты игрока и округляем до целых чисел
            int x = (int) mc.player.getX();
            int y = (int) mc.player.getY();
            int z = (int) mc.player.getZ();

            // Форматируем координаты (без дробной части)
            return String.format("%d, %d, %d", x, y, z);
        }
        return "0, 0, 0";
    }

    private static Color[] getAnimatedColors() {
        long time = System.currentTimeMillis();
        float progress = (time % 2000) / 2000.0f; // 2 секунды на полный цикл

        // Используем только розовый и бирюзовый цвета с плавным переходом
        Color color1 = RenderUtil.interpolateColor(PINK_COLOR, TEAL_COLOR, progress);
        Color color2 = RenderUtil.interpolateColor(TEAL_COLOR, PINK_COLOR, progress);

        return new Color[]{color1, color2};
    }

    private static void drawGradientText(RenderGameOverlayEvent.Post event, String text, int x, int y, Color startColor, Color endColor) {
        Minecraft mc = Minecraft.getInstance();
        int currentX = x;

        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            String charStr = String.valueOf(character);

            // Вычисляем прогресс для градиента (0.0 - 1.0)
            float progress = text.length() > 1 ? (float) i / (text.length() - 1) : 0.5f;

            // Интерполируем цвет
            Color color = RenderUtil.interpolateColor(startColor, endColor, progress);

            // Рисуем символ с своим цветом
            mc.font.draw(event.getMatrixStack(), charStr, currentX, y, color.getRGB());

            // Сдвигаем позицию для следующего символа
            currentX += mc.font.width(charStr);
        }
    }
}