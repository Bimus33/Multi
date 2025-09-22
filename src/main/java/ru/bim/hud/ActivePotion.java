package ru.bim.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import ru.bim.util.RenderUtil;

import java.awt.*;
import java.util.Collection;

public class ActivePotion {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final FontRenderer fontRenderer = mc.font;

    private final Color pinkColor = new Color(255, 0, 128); // Розовый
    private final Color tealColor = new Color(0, 255, 200); // Бирюзовый

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;
        if (mc.player == null) return;
        renderActivePotions(event);
    }

    private void renderActivePotions(RenderGameOverlayEvent.Post event) {
        Collection<EffectInstance> activeEffects = mc.player.getActiveEffects();
        int x = 5;
        int y = 25;
        if (!HudManager.showActivePotion) return;

        int maxWidth = calculateMaxWidth(activeEffects);
        Color[] currentColors = getAnimatedColors();

        // Заголовок
        String title = "Active Effects";
        int titleWidth = Math.max(fontRenderer.width(title), maxWidth);

        RenderUtil.drawShadow(x - 2, y - 2, titleWidth + 4, fontRenderer.lineHeight + 4,
                2f, new Color(0, 255, 0, 180));
        RenderUtil.drawRound(x - 2, y - 2, titleWidth + 4, fontRenderer.lineHeight + 4,
                3, Color.BLACK);

        drawGradientText(event, title, x, y, currentColors[0], currentColors[1]);
        y += fontRenderer.lineHeight + 8;

        if (activeEffects.isEmpty()) {
            String noEffects = "No active effects";
            int noEffectsWidth = Math.max(fontRenderer.width(noEffects), maxWidth);

            RenderUtil.drawShadow(x - 2, y - 2, noEffectsWidth + 4, fontRenderer.lineHeight + 6,
                    2f, new Color(0, 255, 0, 180));
            RenderUtil.drawRound(x - 2, y - 2, noEffectsWidth + 4, fontRenderer.lineHeight + 8,
                    3, Color.BLACK);

            drawGradientText(event, noEffects, x, y, currentColors[0], currentColors[1]);
        } else {
            for (EffectInstance effect : activeEffects) {
                if (effect != null && effect.getEffect() != null) {
                    renderEffect(event, effect, x, y, maxWidth, currentColors);
                    y += fontRenderer.lineHeight + 7;
                }
            }
        }
    }

    private int calculateMaxWidth(Collection<EffectInstance> activeEffects) {
        int maxWidth = fontRenderer.width("Active Effects");
        for (EffectInstance effect : activeEffects) {
            if (effect != null && effect.getEffect() != null) {
                String text = getEffectDisplayName(effect.getEffect()) + " " +
                        getAmplifierText(effect.getAmplifier()) +
                        " (" + formatDuration(effect.getDuration()) + ")";
                maxWidth = Math.max(maxWidth, fontRenderer.width(text));
            }
        }
        return maxWidth;
    }

    private void renderEffect(RenderGameOverlayEvent.Post event, EffectInstance effect, int x, int y, int maxWidth, Color[] colors) {
        String text = getEffectDisplayName(effect.getEffect()) + " " +
                getAmplifierText(effect.getAmplifier()) +
                " (" + formatDuration(effect.getDuration()) + ")";

        RenderUtil.drawShadow(x - 2, y - 2, maxWidth + 4, fontRenderer.lineHeight + 4,
                2f, new Color(0, 255, 0, 180));
        RenderUtil.drawRound(x - 2, y - 2, maxWidth + 4, fontRenderer.lineHeight + 4,
                3, Color.BLACK);

        drawGradientText(event, text, x, y, colors[0], colors[1]);
    }

    private void drawGradientText(RenderGameOverlayEvent.Post event, String text, int x, int y,
                                  Color startColor, Color endColor) {
        int currentX = x;
        for (int i = 0; i < text.length(); i++) {
            String charStr = String.valueOf(text.charAt(i));
            float progress = text.length() > 1 ? (float) i / (text.length() - 1) : 0.5f;
            Color color = RenderUtil.interpolateColor(startColor, endColor, progress);
            fontRenderer.drawShadow(event.getMatrixStack(), charStr, currentX, y, color.getRGB());
            currentX += fontRenderer.width(charStr);
        }
    }

    private Color[] getAnimatedColors() {
        long time = System.currentTimeMillis();
        float progress = (time % 2000) / 2000.0f;
        return new Color[]{
                RenderUtil.interpolateColor(pinkColor, tealColor, progress),
                RenderUtil.interpolateColor(tealColor, pinkColor, progress)
        };
    }

    private String getEffectDisplayName(Effect effect) {
        return effect.getDisplayName().getString();
    }

    private String formatDuration(int ticks) {
        int seconds = (ticks / 20) % 60;
        int minutes = ticks / 1200;
        return minutes > 0 ? String.format("%d:%02d", minutes, seconds) : seconds + "s";
    }

    private String getAmplifierText(int amplifier) {
        switch (amplifier) {
            case 1: return "II";
            case 2: return "III";
            case 3: return "IV";
            default: return "";
        }
    }
}
