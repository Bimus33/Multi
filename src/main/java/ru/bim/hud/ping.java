package ru.bim.hud;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.bim.util.RenderUtil;

import java.awt.*;

@Mod.EventBusSubscriber(modid = "multi", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ping {

    private static final int PADDING = 5;
    private static final int CORNER_RADIUS = 3;
    private static final int TEXT_COLOR = 0xFFFFFF;
    private static final int ELEMENT_HEIGHT = 13;

    @SubscribeEvent
    public static void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        // Получаем пинг
        String pingText = getPingText();
        int textWidth = mc.font.width(pingText);
        int elementWidth = textWidth + PADDING * 2;

        // Позиция в левом верхнем углу
        int xPos = 100; // Отступ от левого края
        int yPos = 3; // Отступ от верхнего края

        // Рисуем фон с плавными углами
        RenderUtil.drawRound(xPos, yPos, elementWidth, ELEMENT_HEIGHT, CORNER_RADIUS, Color.BLACK);

        // Рисуем текст (белый цвет)
        mc.font.draw(event.getMatrixStack(), pingText, xPos + PADDING, yPos + 4, TEXT_COLOR);
    }

    private static String getPingText() {
        Minecraft mc = Minecraft.getInstance();

        if (mc.getConnection() != null && mc.player != null) {
            try {
                net.minecraft.client.network.play.NetworkPlayerInfo info =
                        mc.getConnection().getPlayerInfo(mc.player.getUUID());
                if (info != null) {
                    int latency = info.getLatency();
                    // Всегда белый цвет текста
                    return latency + "ms";
                }
            } catch (Exception e) {
                // В случае ошибки возвращаем N/A
            }
        }
        return "N/A"; // Без цветовых кодов
    }
}