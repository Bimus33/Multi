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

    public WaterMark() {
        // Конструктор для ясности
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;

        renderWatermark(event);
    }

    private void renderWatermark(RenderGameOverlayEvent.Post event) {
        if (mc.player == null) return;

        String username = mc.player.getGameProfile().getName();
        int fps = getFPS();
        String fpsText = TextFormatting.WHITE + "FPS: " + TextFormatting.WHITE + fps;
        String text = TextFormatting.WHITE + username + TextFormatting.GRAY + " | " + fpsText;

        int textWidth = fontRenderer.width(text);
        int x = 5; // Отступ от левого края
        int y = 5; // Отступ от верхнего края

        // Рисуем закругленный фон
        RenderUtil.drawRound(x - 2, y - 2, textWidth + 4, fontRenderer.lineHeight + 4, 3, Color.BLACK);

        // Рисуем текст
        fontRenderer.drawShadow(event.getMatrixStack(), text, x, y, 0xFFFFFF);
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