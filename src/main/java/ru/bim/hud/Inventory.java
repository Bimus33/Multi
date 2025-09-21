package ru.bim.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.bim.util.RenderUtil;

import java.awt.*;

@Mod.EventBusSubscriber(modid = "multi", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Inventory {

    private static final int WINDOW_X = 150;
    private static final int WINDOW_Y = 3;
    private static final int WINDOW_WIDTH = 195;
    private static final int WINDOW_HEIGHT = 85;
    private static final int SLOT_SIZE = 18;
    private static final int SLOT_SPACING = 2; // Отступ между слотами
    private static final int SLOTS_PER_ROW = 9;
    private static final int SLOT_ROWS = 3;
    private static final int CORNER_RADIUS = 3;
    private static final int INFO_BOX_WIDTH = 100;
    private static final int INFO_BOX_HEIGHT = 30;

    @SubscribeEvent
    public static void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        // Рисуем фон окна инвентаря с закругленными углами
        RenderUtil.drawRound(WINDOW_X, WINDOW_Y, WINDOW_WIDTH, WINDOW_HEIGHT, CORNER_RADIUS, Color.BLACK);

        // Рисуем рамку окна (второй слой с обводкой)
        RenderUtil.drawRound(WINDOW_X, WINDOW_Y, WINDOW_WIDTH, WINDOW_HEIGHT, CORNER_RADIUS, Color.BLACK);

        // Заголовок окна
        mc.font.draw(event.getMatrixStack(),
                "Инвентарь",
                WINDOW_X + 6,
                WINDOW_Y + 4,
                0xFFFFFF);

        // Рисуем слоты инвентаря
        drawInventorySlots(event, mc);

        // Рисуем информацию о предметах в черном окне
        drawItemInfo(event, mc);
    }

    private static void drawInventorySlots(RenderGameOverlayEvent event, Minecraft mc) {
        int startX = WINDOW_X + 8;
        int startY = WINDOW_Y + 16;

        // Рисуем слоты инвентаря (9x3) с отступами
        for (int row = 0; row < SLOT_ROWS; row++) {
            for (int col = 0; col < SLOTS_PER_ROW; col++) {
                // Добавляем отступы между слотами
                int slotX = startX + col * (SLOT_SIZE + SLOT_SPACING);
                int slotY = startY + row * (SLOT_SIZE + SLOT_SPACING);

                // Рисуем фон слота с закругленными углами
                RenderUtil.drawRound(slotX, slotY, SLOT_SIZE, SLOT_SIZE, 3, Color.DARK_GRAY);

                // Рисуем рамку слота (второй слой с обводкой)
                RenderUtil.drawRound(slotX, slotY, SLOT_SIZE, SLOT_SIZE, 3, Color.DARK_GRAY);

                // Получаем предмет в слоте
                int slotIndex = col + row * SLOTS_PER_ROW;
                if (slotIndex < mc.player.inventory.items.size()) {
                    net.minecraft.item.ItemStack stack = mc.player.inventory.items.get(slotIndex);

                    if (!stack.isEmpty()) {
                        // Рисуем иконку предмета
                        mc.getItemRenderer().renderAndDecorateItem(stack, slotX + 1, slotY + 1);

                        // Рисуем количество предметов поверх иконки
                        String countText = String.valueOf(stack.getCount());
                        // Центрируем текст по центру слота
                        int textX = slotX + (SLOT_SIZE - mc.font.width(countText)) / 2 + 1;
                        int textY = slotY + (SLOT_SIZE - 8) / 2 + 1;

                        mc.font.draw(event.getMatrixStack(),
                                countText,
                                textX,
                                textY,
                                0xFFFFFF);
                    }
                }
            }
        }
    }

    private static void drawItemInfo(RenderGameOverlayEvent event, Minecraft mc) {
        int infoX = WINDOW_X + WINDOW_WIDTH + 10;
        int infoY = WINDOW_Y;

        // Рисуем черное окно для информации с закругленными углами
        RenderUtil.drawRound(infoX, infoY, INFO_BOX_WIDTH, INFO_BOX_HEIGHT, CORNER_RADIUS, Color.BLACK);

        // Рисуем рамку окна информации
        RenderUtil.drawRound(infoX, infoY, INFO_BOX_WIDTH, INFO_BOX_HEIGHT, CORNER_RADIUS, Color.BLACK);

        // Информация о количестве предметов
        int totalItems = 0;
        int uniqueItems = 0;

        for (net.minecraft.item.ItemStack stack : mc.player.inventory.items) {
            if (!stack.isEmpty()) {
                totalItems += stack.getCount();
                uniqueItems++;
            }
        }

        // Центрируем текст в окне информации
        int centerX = infoX + INFO_BOX_WIDTH / 2;

        String totalText = "Предметы: " + totalItems;
        String uniqueText = "Уникальные: " + uniqueItems;

        mc.font.draw(event.getMatrixStack(),
                totalText,
                centerX - mc.font.width(totalText) / 2,
                infoY + 6,
                0xFFFFFF);

        mc.font.draw(event.getMatrixStack(),
                uniqueText,
                centerX - mc.font.width(uniqueText) / 2,
                infoY + 18,
                0xFFFFFF);
    }
}