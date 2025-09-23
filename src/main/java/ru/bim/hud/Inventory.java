package ru.bim.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.bim.util.RenderUtil;

import java.awt.*;

@Mod.EventBusSubscriber(modid = "multi", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Inventory {

    private static final Minecraft mc = Minecraft.getInstance();

    private static final int WIDTH = 195;
    private static final int HEIGHT = 85;
    private static final int SLOT_SIZE = 18;
    private static final int SLOT_SPACING = 2;
    private static final int ROWS = 3;
    private static final int COLS = 9;
    private static final int CORNER_RADIUS = 4;

    // Позиция окна
    private static int posX = 150;
    private static int posY = 3;

    // Перетаскивание
    private static boolean dragging = false;
    private static int dragOffsetX, dragOffsetY;

    @SubscribeEvent
    public static void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;
        if (!HudManager.showInventory) return;
        if (mc.level == null || mc.player == null) return;

        // Фон + тень
        RenderUtil.drawShadow(posX, posY, WIDTH, HEIGHT, 2f, new Color(0, 255, 0, 180));
        RenderUtil.drawRound(posX, posY, WIDTH, HEIGHT, CORNER_RADIUS, Color.BLACK);

        // Заголовок
        mc.font.draw(event.getMatrixStack(), "Инвентарь", posX + 6, posY + 6, 0xFFFFFF);

        int startX = posX + 8;
        int startY = posY + 20;

        // Слоты
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int slotX = startX + col * (SLOT_SIZE + SLOT_SPACING);
                int slotY = startY + row * (SLOT_SIZE + SLOT_SPACING);

                RenderUtil.drawRound(slotX, slotY, SLOT_SIZE, SLOT_SIZE, 3, Color.DARK_GRAY);

                int slotIndex = col + row * COLS;
                if (slotIndex < mc.player.inventory.items.size()) {
                    ItemStack stack = mc.player.inventory.items.get(slotIndex);
                    if (!stack.isEmpty()) {
                        mc.getItemRenderer().renderAndDecorateItem(stack, slotX + 1, slotY + 1);

                        String count = String.valueOf(stack.getCount());
                        int textX = slotX + SLOT_SIZE - mc.font.width(count) - 2;
                        int textY = slotY + SLOT_SIZE - 10;
                        mc.font.draw(event.getMatrixStack(), count, textX, textY, 0xFFFFFF);
                    }
                }
            }
        }
    }

    // === Перетаскивание как у ArrayList ===
    @SubscribeEvent
    public static void onMouseClick(GuiScreenEvent.MouseClickedEvent.Pre event) {
        if (!(mc.screen instanceof ChatScreen)) return;

        int mouseX = (int) event.getMouseX();
        int mouseY = (int) event.getMouseY();

        if (event.getButton() == 0) {
            // Перетаскивание за заголовок
            int titleWidth = mc.font.width("Инвентарь");
            int titleHeight = mc.font.lineHeight + 6;
            if (mouseX >= posX && mouseX <= posX + titleWidth &&
                    mouseY >= posY && mouseY <= posY + titleHeight) {
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
