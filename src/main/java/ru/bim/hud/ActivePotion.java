package ru.bim.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import ru.bim.util.RenderUtil;

import java.awt.Color;
import java.util.Collection;

public class ActivePotion {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final FontRenderer fontRenderer = mc.font;

    public ActivePotion() {
        // Конструктор для регистрации
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;
        if (mc.player == null) return;

        renderActivePotions(event);
    }

    private void renderActivePotions(RenderGameOverlayEvent.Post event) {
        Collection<EffectInstance> activeEffects = mc.player.getActiveEffects();
        int x = 5; // Отступ от левого края
        int y = 25; // Отступ от верхнего края (ниже ватермарка)

        // Находим максимальную ширину среди всех эффектов
        int maxWidth = calculateMaxWidth(activeEffects);

        // Заголовок
        String title = TextFormatting.WHITE + "Active Effects";
        int titleWidth = Math.max(fontRenderer.width(title), maxWidth);

        // Рисуем черный фон для заголовка
        RenderUtil.drawRound(x - 2, y - 2, titleWidth + 4, fontRenderer.lineHeight + 4, 3,
                new Color(0xFF000000, true));

        // Рисуем заголовок белым цветом
        fontRenderer.drawShadow(event.getMatrixStack(), title, x, y, 0xFFFFFF);

        y += fontRenderer.lineHeight + 8; // Отступ после заголовка 1 пиксель

        if (activeEffects.isEmpty()) {
            // Если эффектов нет, показываем сообщение
            String noEffects = TextFormatting.WHITE + "No active effects";
            int noEffectsWidth = Math.max(fontRenderer.width(noEffects), maxWidth);

            // Черный фон
            RenderUtil.drawRound(x - 2, y - 2, noEffectsWidth + 4, fontRenderer.lineHeight + 4, 3,
                    new Color(0xFF000000, true));

            // Белый текст
            fontRenderer.drawShadow(event.getMatrixStack(), noEffects, x, y, 0xFFFFFF);
        } else {
            // Перебираем все активные эффекты
            for (EffectInstance effect : activeEffects) {
                if (effect != null && effect.getEffect() != null) {
                    renderEffect(event, effect, x, y, maxWidth);
                    y += fontRenderer.lineHeight + 7; // Отступ между эффектами 1 пиксель
                }
            }
        }
    }

    private int calculateMaxWidth(Collection<EffectInstance> activeEffects) {
        int maxWidth = fontRenderer.width("Active Effects"); // Минимальная ширина по заголовку

        if (!activeEffects.isEmpty()) {
            for (EffectInstance effect : activeEffects) {
                if (effect != null && effect.getEffect() != null) {
                    String effectText = getEffectDisplayName(effect.getEffect()) + " " +
                            getAmplifierText(effect.getAmplifier()) +
                            TextFormatting.GRAY + " (" + formatDuration(effect.getDuration()) + ")";
                    int width = fontRenderer.width(effectText);
                    if (width > maxWidth) {
                        maxWidth = width;
                    }
                }
            }
        }
        return maxWidth;
    }

    private void renderEffect(RenderGameOverlayEvent.Post event, EffectInstance effect, int x, int y, int maxWidth) {
        Effect potionEffect = effect.getEffect();
        String effectName = getEffectDisplayName(potionEffect);
        String duration = formatDuration(effect.getDuration());
        String amplifier = getAmplifierText(effect.getAmplifier());

        String text = TextFormatting.WHITE + effectName + " " + amplifier +
                TextFormatting.GRAY + " (" + duration + ")";

        // Рисуем черный фон с максимальной шириной
        RenderUtil.drawRound(x - 2, y - 2, maxWidth + 4, fontRenderer.lineHeight + 4, 3,
                new Color(0xFF000000, true));

        // Рисуем текст эффекта белым цветом
        fontRenderer.drawShadow(event.getMatrixStack(), text, x, y, 0xFFFFFF);
    }

    private String getEffectDisplayName(Effect effect) {
        // Получаем отображаемое имя эффекта
        return effect.getDisplayName().getString();
    }

    private String formatDuration(int ticks) {
        // Конвертируем тики в секунды и минуты
        int seconds = (ticks / 20) % 60;
        int minutes = ticks / 1200;

        if (minutes > 0) {
            return String.format("%d:%02d", minutes, seconds);
        } else {
            return seconds + "s";
        }
    }

    private String getAmplifierText(int amplifier) {
        if (amplifier == 0) {
            return "";
        } else if (amplifier == 1) {
            return TextFormatting.WHITE + "II";
        } else if (amplifier == 2) {
            return TextFormatting.WHITE + "III";
        } else {
            return TextFormatting.WHITE + "IV";
        }
    }
}