package ru.bim.hud;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.bim.util.RenderUtil;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

@Mod.EventBusSubscriber(modid = "multi", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ArrayList {

    private static final int PADDING = 4;
    private static final int CORNER_RADIUS = 3;
    private static final int LINE_HEIGHT = 12;

    private static final Color PINK_COLOR = new Color(255, 0, 128);
    private static final Color TEAL_COLOR = new Color(0, 255, 200);

    // Пример списка (заменишь на свои активные модули/данные)
    private static final List<String> modules = Arrays.asList(
            "Sprint",
            "Fly",
            "KillAura",
            "ESP"
    );

    @SubscribeEvent
    public static void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;
        if (!HudManager.showArrayList) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        int x = 350; // Позиция по X
        int y = 5;   // Позиция по Y

        // Определяем ширину окна по самому длинному тексту
        int maxWidth = 0;
        for (String module : modules) {
            maxWidth = Math.max(maxWidth, mc.font.width(module));
        }
        int boxWidth = maxWidth + PADDING * 2;
        int boxHeight = modules.size() * LINE_HEIGHT + PADDING;

        // Тень + фон
        RenderUtil.drawShadow(x, y, boxWidth, boxHeight, 2f, new Color(0, 255, 0, 180));
        RenderUtil.drawRound(x, y, boxWidth, boxHeight, CORNER_RADIUS, Color.BLACK);

        // Цвета для текста
        Color[] currentColors = getAnimatedColors();

        // Рисуем список
        int offsetY = y + PADDING;
        for (String module : modules) {
            drawGradientText(event, module, x + PADDING, offsetY, currentColors[0], currentColors[1]);
            offsetY += LINE_HEIGHT;
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
