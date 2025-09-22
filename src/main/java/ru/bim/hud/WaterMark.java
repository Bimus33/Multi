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
        String fullText = username + " | FPS: " + fps;

        int textWidth = fontRenderer.width(TextFormatting.stripFormatting(fullText));
        int x = 7;
        int y = 7;
        int padding = 4;

        // Зелёная тень 2 пикселя
        RenderUtil.drawShadow(x - padding, y - padding, textWidth + padding * 2, fontRenderer.lineHeight + padding * 2,
                2f, new Color(0, 255, 0, 180));
        RenderUtil.drawRound(x - padding, y - padding, textWidth + padding * 2, fontRenderer.lineHeight + padding * 2,
                3, Color.BLACK);

        Color[] currentColors = getAnimatedColors();
        drawGradientTextWithOutline(event, fullText, x, y, currentColors[0], currentColors[1], new Color(0, 0, 0, 150));

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
        colorTransition = (elapsed % 2.0f) / 2.0f;
        if (colorTransition >= 0.99f) {
            currentColorIndex = (currentColorIndex + 1) % gradientColors.length;
            startTime = currentTime;
            colorTransition = 0.0f;
        }
    }

    private void drawGradientTextWithOutline(RenderGameOverlayEvent.Post event, String text, int x, int y,
                                             Color startColor, Color endColor, Color outlineColor) {
        drawTextOutline(event, text, x, y, outlineColor);
        drawGradientText(event, text, x, y, startColor, endColor);
    }

    private void drawTextOutline(RenderGameOverlayEvent.Post event, String text, int x, int y, Color outlineColor) {
        int outlineColorRGB = outlineColor.getRGB();
        String cleanText = TextFormatting.stripFormatting(text);
        for (int offsetX = -1; offsetX <= 1; offsetX++) {
            for (int offsetY = -1; offsetY <= 1; offsetY++) {
                if (offsetX == 0 && offsetY == 0) continue;
                int currentX = x;
                for (int i = 0; i < cleanText.length(); i++) {
                    String charStr = String.valueOf(cleanText.charAt(i));
                    fontRenderer.draw(event.getMatrixStack(), charStr, currentX + offsetX, y + offsetY, outlineColorRGB);
                    currentX += fontRenderer.width(charStr);
                }
            }
        }
    }

    private void drawGradientText(RenderGameOverlayEvent.Post event, String text, int x, int y,
                                  Color startColor, Color endColor) {
        String cleanText = TextFormatting.stripFormatting(text);
        int currentX = x;
        for (int i = 0; i < cleanText.length(); i++) {
            String charStr = String.valueOf(cleanText.charAt(i));
            float progress = cleanText.length() > 1 ? (float) i / (cleanText.length() - 1) : 0.5f;
            Color color = RenderUtil.interpolateColor(startColor, endColor, progress);
            fontRenderer.drawShadow(event.getMatrixStack(), charStr, currentX, y, color.getRGB());
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
