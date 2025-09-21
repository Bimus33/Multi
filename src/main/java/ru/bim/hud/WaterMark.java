package ru.bim.hud;

import ru.bim.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import java.awt.Color;

public class WaterMark {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final FontRenderer fontRenderer = mc.font;

    private long startTime = System.currentTimeMillis();
    private final Color[] gradientColors = {
            new Color(255, 0, 128),    // Розовый
            new Color(0, 255, 200)     // Бирюзовый
    };
    private int currentColorIndex = 0;
    private float colorTransition = 0.0f;

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;

        renderWatermark(event);
    }

    private void renderWatermark(RenderGameOverlayEvent.Post event) {
        if (mc.player == null) return;

        String username = mc.player.getGameProfile().getName();
        int fps = getFPS();
        String fpsText = "FPS: " + fps;
        String fullText = username + " | " + fpsText;

        // Правильно вычисляем ширину текста без форматирующих кодов
        int textWidth = fontRenderer.width(TextFormatting.stripFormatting(fullText));
        int x = 7; // Отступ от левого края
        int y = 7; // Отступ от верхнего края
        int padding = 4;

        // Черный фон с закругленными углами
        RenderUtil.drawRound(x - padding, y - padding, textWidth + padding * 2, fontRenderer.lineHeight + padding * 2, 3, Color.BLACK);

        // Обводка вокруг фона
        // RenderUtil.drawRoundOutline(x - padding, y - padding, textWidth + padding * 2, fontRenderer.lineHeight + padding * 2, 3, 1, new Color(50, 50, 50, 200));

        // Получаем текущие цвета для анимации
        Color[] currentColors = getAnimatedColors();

        // Рисуем текст с градиентом и обводкой
        drawGradientTextWithOutline(event, fullText, x, y, currentColors[0], currentColors[1], new Color(0, 0, 0, 150));

        // Обновляем анимацию
        updateAnimation();
    }

    private Color[] getAnimatedColors() {
        Color currentColor = gradientColors[currentColorIndex];
        Color nextColor = gradientColors[(currentColorIndex + 1) % gradientColors.length];

        Color interpolatedStart = RenderUtil.interpolateColor(currentColor, nextColor, colorTransition);
        Color interpolatedEnd = RenderUtil.interpolateColor(
                gradientColors[(currentColorIndex + 1) % gradientColors.length],
                gradientColors[(currentColorIndex + 2) % gradientColors.length],
                colorTransition
        );

        return new Color[]{interpolatedStart, interpolatedEnd};
    }

    private void updateAnimation() {
        long currentTime = System.currentTimeMillis();
        float elapsed = (currentTime - startTime) / 1000.0f;

        // Анимация перехода цветов (1 секунда на переход)
        colorTransition = (elapsed % 2.0f) / 2.0f;

        // Если переход завершен, переходим к следующему цвету
        if (colorTransition >= 0.99f) {
            currentColorIndex = (currentColorIndex + 1) % gradientColors.length;
            startTime = currentTime;
            colorTransition = 0.0f;
        }
    }

    private void drawGradientTextWithOutline(RenderGameOverlayEvent.Post event, String text, int x, int y,
                                             Color startColor, Color endColor, Color outlineColor) {
        // Сначала рисуем обводку
        drawTextOutline(event, text, x, y, outlineColor);

        // Затем рисуем градиентный текст поверх
        drawGradientText(event, text, x, y, startColor, endColor);
    }

    private void drawTextOutline(RenderGameOverlayEvent.Post event, String text, int x, int y, Color outlineColor) {
        int outlineColorRGB = outlineColor.getRGB();
        String cleanText = TextFormatting.stripFormatting(text);

        // Рисуем обводку вокруг каждого символа (8 направлений)
        for (int offsetX = -1; offsetX <= 1; offsetX++) {
            for (int offsetY = -1; offsetY <= 1; offsetY++) {
                if (offsetX == 0 && offsetY == 0) continue; // Пропускаем центральную позицию

                int currentX = x;
                for (int i = 0; i < cleanText.length(); i++) {
                    char character = cleanText.charAt(i);
                    String charStr = String.valueOf(character);

                    fontRenderer.draw(event.getMatrixStack(), charStr, currentX + offsetX, y + offsetY, outlineColorRGB);
                    currentX += fontRenderer.width(charStr);
                }
            }
        }
    }

    private void drawGradientText(RenderGameOverlayEvent.Post event, String text, int x, int y, Color startColor, Color endColor) {
        String cleanText = TextFormatting.stripFormatting(text);
        int currentX = x;

        for (int i = 0; i < cleanText.length(); i++) {
            char character = cleanText.charAt(i);
            String charStr = String.valueOf(character);

            // Вычисляем прогресс для градиента (0.0 - 1.0)
            float progress = cleanText.length() > 1 ? (float) i / (cleanText.length() - 1) : 0.5f;

            // Интерполируем цвет
            Color color = RenderUtil.interpolateColor(startColor, endColor, progress);

            // Рисуем символ с своим цветом
            fontRenderer.drawShadow(event.getMatrixStack(), charStr, currentX, y, color.getRGB());

            // Сдвигаем позицию для следующего символа
            currentX += fontRenderer.width(charStr);
        }
    }

    private int getFPS() {
        try {
            java.lang.reflect.Field fpsField = Minecraft.class.getDeclaredField("fps");
            fpsField.setAccessible(true);
            return (int) fpsField.get(mc);
        } catch (Exception e) {
            return 60;
        }
    }
}