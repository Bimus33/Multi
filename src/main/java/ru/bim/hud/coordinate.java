package ru.bim.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
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

    // === Позиция и перетаскивание ===
    private static int posX = 500;
    private static int posY = 3;
    private static boolean dragging = false;
    private static int dragOffsetX, dragOffsetY;

    @SubscribeEvent
    public static void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        if (!HudManager.showCoordinates) return;

        String coordText = getCoordinates();
        int textWidth = mc.font.width(coordText);
        int elementWidth = textWidth + PADDING * 2;

        Color[] currentColors = getAnimatedColors();

        RenderUtil.drawShadow(posX, posY, elementWidth, ELEMENT_HEIGHT,
                2f, new Color(0, 255, 0, 180));
        RenderUtil.drawRound(posX, posY, elementWidth, ELEMENT_HEIGHT, CORNER_RADIUS, Color.BLACK);

        drawGradientText(event, coordText, posX + PADDING, posY + 4, currentColors[0], currentColors[1]);
    }

    // === События мыши для перетаскивания ===
    @SubscribeEvent
    public static void onMouseClick(GuiScreenEvent.MouseClickedEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.screen instanceof ChatScreen)) return;

        String coordText = getCoordinates();
        int textWidth = mc.font.width(coordText);
        int elementWidth = textWidth + PADDING * 2;

        int mouseX = (int) event.getMouseX();
        int mouseY = (int) event.getMouseY();

        if (event.getButton() == 0 &&
                mouseX >= posX && mouseX <= posX + elementWidth &&
                mouseY >= posY && mouseY <= posY + ELEMENT_HEIGHT) {
            dragging = true;
            dragOffsetX = mouseX - posX;
            dragOffsetY = mouseY - posY;
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

    // === Логика ===
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
