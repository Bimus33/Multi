package ru.bim.hud;

import net.minecraft.client.Minecraft;
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

        if (!HudManager.showInventory) return;


        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        // Тень + фон для главного окна
        RenderUtil.drawShadow(WINDOW_X, WINDOW_Y, WINDOW_WIDTH, WINDOW_HEIGHT,
                2f, new Color(0, 255, 0, 180));
        RenderUtil.drawRound(WINDOW_X, WINDOW_Y, WINDOW_WIDTH, WINDOW_HEIGHT,
                CORNER_RADIUS, Color.BLACK);

        // Заголовок
        mc.font.draw(event.getMatrixStack(),
                "Инвентарь",
                WINDOW_X + 6,
                WINDOW_Y + 4,
                0xFFFFFF);

        // Слоты
        drawInventorySlots(event, mc);

        // Инфо-блок
        drawItemInfo(event, mc);
    }

    private static void drawInventorySlots(RenderGameOverlayEvent.Post event, Minecraft mc) {
        int startX = WINDOW_X + 8;
        int startY = WINDOW_Y + 16;

        for (int row = 0; row < SLOT_ROWS; row++) {
            for (int col = 0; col < SLOTS_PER_ROW; col++) {
                int slotX = startX + col * (SLOT_SIZE + SLOT_SPACING);
                int slotY = startY + row * (SLOT_SIZE + SLOT_SPACING);

                // Фон слота
                RenderUtil.drawRound(slotX, slotY, SLOT_SIZE, SLOT_SIZE, 3, Color.DARK_GRAY);

                // Предметы
                int slotIndex = col + row * SLOTS_PER_ROW;
                if (slotIndex < mc.player.inventory.items.size()) {
                    net.minecraft.item.ItemStack stack = mc.player.inventory.items.get(slotIndex);
                    if (!stack.isEmpty()) {
                        mc.getItemRenderer().renderAndDecorateItem(stack, slotX + 1, slotY + 1);

                        String countText = String.valueOf(stack.getCount());
                        int textX = slotX + (SLOT_SIZE - mc.font.width(countText)) / 2 + 1;
                        int textY = slotY + (SLOT_SIZE - 8) / 2 + 1;

                        mc.font.draw(event.getMatrixStack(), countText, textX, textY, 0xFFFFFF);
                    }
                }
            }
        }
    }

    private static void drawItemInfo(RenderGameOverlayEvent.Post event, Minecraft mc) {
        int infoX = WINDOW_X + WINDOW_WIDTH + 10;
        int infoY = WINDOW_Y;

        // Тень + фон для инфо-бокса
        RenderUtil.drawShadow(infoX, infoY, INFO_BOX_WIDTH, INFO_BOX_HEIGHT,
                2f, new Color(0, 255, 0, 180));
        RenderUtil.drawRound(infoX, infoY, INFO_BOX_WIDTH, INFO_BOX_HEIGHT,
                CORNER_RADIUS, Color.BLACK);

        int totalItems = 0;
        int uniqueItems = 0;
        for (net.minecraft.item.ItemStack stack : mc.player.inventory.items) {
            if (!stack.isEmpty()) {
                totalItems += stack.getCount();
                uniqueItems++;
            }
        }

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
