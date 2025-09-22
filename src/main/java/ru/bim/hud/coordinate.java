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

    private static final Color PINK_COLOR = new Color(255, 0, 128);
    private static final Color TEAL_COLOR = new Color(0, 255, 200);

    @SubscribeEvent
    public static void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        if (!HudManager.showCoordinates) return;
        String coordText = getCoordinates();
        int textWidth = mc.font.width(coordText);
        int elementWidth = textWidth + PADDING * 2;

        int xPos = 500;
        int yPos = 3;

        Color[] currentColors = getAnimatedColors();

        RenderUtil.drawShadow(xPos, yPos, elementWidth, ELEMENT_HEIGHT,
                2f, new Color(0, 255, 0, 180));
        RenderUtil.drawRound(xPos, yPos, elementWidth, ELEMENT_HEIGHT, CORNER_RADIUS, Color.BLACK);

        drawGradientText(event, coordText, xPos + PADDING, yPos + 4, currentColors[0], currentColors[1]);
    }

    private static String getCoordinates() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            int x = (int) mc.player.getX();
            int y = (int) mc.player.getY();
            int z = (int) mc.player.getZ();
            return String.format("%d, %d, %d", x, y, z);
        }
        return "0, 0, 0";
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
