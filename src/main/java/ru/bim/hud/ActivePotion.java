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

    private final Color pinkColor = new Color(255, 0, 128);    // Розовый
    private final Color tealColor = new Color(0, 255, 200);    // Бирюзовый

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

        // Получаем текущие анимированные цвета
        Color[] currentColors = getAnimatedColors();

        // Заголовок
        String title = "Active Effects";
        int titleWidth = Math.max(fontRenderer.width(title), maxWidth);

        // Рисуем черный фон для заголовка
        RenderUtil.drawRound(x - 2, y - 2, titleWidth + 4, fontRenderer.lineHeight + 4, 3, Color.BLACK);

        // Рисуем заголовок с градиентом
        drawGradientText(event, title, x, y, currentColors[0], currentColors[1]);

        y += fontRenderer.lineHeight + 8; // Отступ после заголовка

        if (activeEffects.isEmpty()) {
            // Если эффектов нет, показываем сообщение
            String noEffects = "No active effects";
            int noEffectsWidth = Math.max(fontRenderer.width(noEffects), maxWidth);

            // Черный фон для сообщения
            RenderUtil.drawRound(x - 2, y - 2, noEffectsWidth + 4, fontRenderer.lineHeight + 4, 3, Color.BLACK);

            // Градиентный текст
            drawGradientText(event, noEffects, x, y, currentColors[0], currentColors[1]);
        } else {
            // Перебираем все активные эффекты
            for (EffectInstance effect : activeEffects) {
                if (effect != null && effect.getEffect() != null) {
                    renderEffect(event, effect, x, y, maxWidth, currentColors);
                    y += fontRenderer.lineHeight + 7; // Отступ между эффектами
                }
            }
        }
    }

    private Color[] getAnimatedColors() {
        long time = System.currentTimeMillis();
        float progress = (time % 2000) / 2000.0f; // 2 секунды на полный цикл

        // Используем только розовый и бирюзовый цвета с плавным переходом
        Color color1 = RenderUtil.interpolateColor(pinkColor, tealColor, progress);
        Color color2 = RenderUtil.interpolateColor(tealColor, pinkColor, progress);

        return new Color[]{color1, color2};
    }

    private int calculateMaxWidth(Collection<EffectInstance> activeEffects) {
        int maxWidth = fontRenderer.width("Active Effects"); // Минимальная ширина по заголовку

        if (!activeEffects.isEmpty()) {
            for (EffectInstance effect : activeEffects) {
                if (effect != null && effect.getEffect() != null) {
                    String effectText = getEffectDisplayName(effect.getEffect()) + " " +
                            getAmplifierText(effect.getAmplifier()) +
                            " (" + formatDuration(effect.getDuration()) + ")";
                    int width = fontRenderer.width(effectText);
                    if (width > maxWidth) {
                        maxWidth = width;
                    }
                }
            }
        }
        return maxWidth;
    }

    private void renderEffect(RenderGameOverlayEvent.Post event, EffectInstance effect, int x, int y, int maxWidth, Color[] colors) {
        Effect potionEffect = effect.getEffect();
        String effectName = getEffectDisplayName(potionEffect);
        String duration = formatDuration(effect.getDuration());
        String amplifier = getAmplifierText(effect.getAmplifier());

        String text = effectName + " " + amplifier + " (" + duration + ")";

        // Рисуем черный фон для эффекта
        RenderUtil.drawRound(x - 2, y - 2, maxWidth + 4, fontRenderer.lineHeight + 4, 3,
                Color.BLACK);

        // Рисуем текст эффекта с градиентом
        drawGradientText(event, text, x, y, colors[0], colors[1]);
    }

    private void drawGradientText(RenderGameOverlayEvent.Post event, String text, int x, int y, Color startColor, Color endColor) {
        int currentX = x;

        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            String charStr = String.valueOf(character);

            // Вычисляем прогресс для градиента (0.0 - 1.0)
            float progress = text.length() > 1 ? (float) i / (text.length() - 1) : 0.5f;

            // Интерполируем цвет
            Color color = RenderUtil.interpolateColor(startColor, endColor, progress);

            // Рисуем символ с своим цветом
            fontRenderer.drawShadow(event.getMatrixStack(), charStr, currentX, y, color.getRGB());

            // Сдвигаем позицию для следующего символа
            currentX += fontRenderer.width(charStr);
        }
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
            return "II";
        } else if (amplifier == 2) {
            return "III";
        } else {
            return "IV";
        }
    }
}