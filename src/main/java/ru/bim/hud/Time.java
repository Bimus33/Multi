package ru.bim.hud;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.bim.util.RenderUtil;

import java.awt.Color;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Показывает текущее Московское время в небольшом окне с тенью,
 * окно можно перетаскивать мышкой при открытом чате.
 */
@Mod.EventBusSubscriber(modid = "multi", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Time {

    private static final Minecraft mc = Minecraft.getInstance();

    // Позиция окна (можно перетаскивать)
    private static int posX = 6;
    private static int posY = 6;

    private static boolean dragging = false;
    private static int dragOffsetX, dragOffsetY;

    // Оформление
    private static final int PADDING_X = 8;
    private static final int PADDING_Y = 6;
    private static final int CORNER_RADIUS = 6;
    private static final float SHADOW_SIZE = 2f;
    private static final Color SHADOW_COLOR = new Color(0, 255, 0, 180); // зелёная тень
    private static final Color BACKGROUND_COLOR = Color.BLACK; // чёрный фон
    private static final Color TEXT_COLOR = new Color(0xFFFFFF);

    // Формат времени/даты
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    // Московская зона
    private static final ZoneId MOSCOW = ZoneId.of("Europe/Moscow");

    @SubscribeEvent
    public static void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;
        if (mc.player == null) return;

        MatrixStack stack = event.getMatrixStack();

        // Получаем текущее московское время
        ZonedDateTime now;
        try {
            now = ZonedDateTime.now(MOSCOW);
        } catch (Exception e) {
            now = ZonedDateTime.now(); // fallback
        }

        String line1 = "Moscow";
        String line2 = now.format(TIME_FORMAT);
        String line3 = now.format(DATE_FORMAT);

        int maxWidth = Math.max(mc.font.width(line1),
                Math.max(mc.font.width(line2), mc.font.width(line3)));
        int boxWidth = maxWidth + PADDING_X * 2;
        int boxHeight = mc.font.lineHeight * 3 + PADDING_Y * 2 + 4;

        // Тень
        RenderUtil.drawShadow(posX, posY, boxWidth, boxHeight, SHADOW_SIZE, SHADOW_COLOR);

        // Фон
        RenderUtil.drawRound(posX, posY, boxWidth, boxHeight, CORNER_RADIUS, BACKGROUND_COLOR);

        // Текст
        int textX = posX + PADDING_X;
        int textY = posY + PADDING_Y;

        mc.font.drawShadow(stack, line1, textX, textY, TEXT_COLOR.getRGB());
        mc.font.drawShadow(stack, line2, textX, textY + mc.font.lineHeight + 2, TEXT_COLOR.getRGB());
        mc.font.drawShadow(stack, line3, textX, textY + (mc.font.lineHeight + 2) * 2, TEXT_COLOR.getRGB());
    }

    // === События мыши для перетаскивания ===
    @SubscribeEvent
    public static void onMouseClick(GuiScreenEvent.MouseClickedEvent.Pre event) {
        if (!(mc.screen instanceof ChatScreen)) return;

        int mouseX = (int) event.getMouseX();
        int mouseY = (int) event.getMouseY();

        // Проверяем клик по области окна
        if (event.getButton() == 0) {
            int width = 120; // примерная ширина для проверки
            int height = 50; // примерная высота для проверки
            if (mouseX >= posX && mouseX <= posX + width &&
                    mouseY >= posY && mouseY <= posY + height) {
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
        if (!(mc.screen instanceof ChatScreen)) return;
        if (dragging) {
            posX = (int) event.getMouseX() - dragOffsetX;
            posY = (int) event.getMouseY() - dragOffsetY;
        }
    }
}
