package ru.bim.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.AbstractGui;
import ru.bim.util.RenderUtil;

import java.awt.Color;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TargetHUD extends AbstractGui {

    private final Minecraft mc;
    private LivingEntity currentTarget;
    private long lastTargetTime;
    private static final long TARGET_TIMEOUT = 2000; // 2 секунды

    // Настройки HUD
    private int x = 500;
    private int y = 500;
    private int width = 120;
    private int height = 40;
    private int healthBarHeight = 8;
    private int textMargin = 2;

    public TargetHUD() {
        this.mc = Minecraft.getInstance();
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.TEXT) return;
        if (mc.player == null || mc.level == null) return;
        if (!HudManager.showTargetHUD) return;

        updateTarget();

        if (currentTarget != null && System.currentTimeMillis() - lastTargetTime < TARGET_TIMEOUT) {
            renderTargetHUD(event.getMatrixStack());
        }
    }

    private void updateTarget() {
        if (mc.player == null || mc.level == null) return;

        List<LivingEntity> entities = mc.level.getEntitiesOfClass(
                        LivingEntity.class,
                        new AxisAlignedBB(
                                mc.player.getX() - 20, mc.player.getY() - 10, mc.player.getZ() - 20,
                                mc.player.getX() + 20, mc.player.getY() + 10, mc.player.getZ() + 20
                        )
                ).stream()
                .filter(entity -> entity != mc.player && entity.isAlive())
                .sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(mc.player)))
                .collect(Collectors.toList());

        if (!entities.isEmpty()) {
            currentTarget = entities.get(0);
            lastTargetTime = System.currentTimeMillis();
        }
    }

    private void renderTargetHUD(MatrixStack matrixStack) {
        if (currentTarget == null) return;

        RenderSystem.pushMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Тень зелёного цвета толщиной 2 пикселя
        RenderUtil.drawShadow(x, y, width, height, 2f, new Color(0, 255, 0, 180));

        // Фон HUD — чёрный, непрозрачный
        RenderUtil.drawRound(x, y, width, height, 6, Color.BLACK);

        // Имя цели
        String name = currentTarget.getName().getString();
        mc.font.drawShadow(matrixStack, name, x + textMargin, y + textMargin, 0xFFFFFFFF);

        // Здоровье текст
        float health = currentTarget.getHealth();
        float maxHealth = currentTarget.getMaxHealth();
        String healthText = String.format("%.1f/%.1f", health, maxHealth);
        int textWidth = mc.font.width(healthText);
        mc.font.drawShadow(matrixStack, healthText, x + width - textWidth - textMargin, y + textMargin, 0xFFFFFFFF);

        // Полоса здоровья
        int barY = y + mc.font.lineHeight + textMargin * 2;
        int barWidth = width - textMargin * 2;
        float healthPercent = Math.max(0, Math.min(1, health / maxHealth));

        renderGradientHealthBar(matrixStack, x + textMargin, barY, barWidth, healthBarHeight, healthPercent);

        // Дополнительная информация
        String distance = String.format("%.1fm", currentTarget.distanceTo(mc.player));
        int distanceY = barY + healthBarHeight + textMargin;
        mc.font.drawShadow(matrixStack, distance, x + textMargin, distanceY, 0xFFFFFFFF);

        RenderSystem.disableBlend();
        RenderSystem.popMatrix();
    }

    private void renderGradientHealthBar(MatrixStack matrixStack, int x, int y, int width, int height, float percent) {
        int fillWidth = (int) (width * percent);

        // Фон полосы (без тени и без обводки)
        RenderUtil.drawRound(x, y, width, height, 4, new Color(50, 50, 50, 200));

        if (fillWidth > 0) {
            RenderUtil.drawRound(x, y, fillWidth, height, 4,
                    getSmoothHealthColor(percent));
        }
    }

    private Color getSmoothHealthColor(float percent) {
        float hue = percent * 0.4f;
        return Color.getHSBColor(hue, 0.9f, 0.9f);
    }

    private void drawRect(MatrixStack matrixStack, int x, int y, int width, int height, Color color) {
        fill(matrixStack, x, y, x + width, y + height, color.getRGB());
    }

    // Методы управления HUD
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void setTarget(LivingEntity target) {
        this.currentTarget = target;
        this.lastTargetTime = System.currentTimeMillis();
    }

    public LivingEntity getCurrentTarget() {
        if (System.currentTimeMillis() - lastTargetTime < TARGET_TIMEOUT) {
            return currentTarget;
        }
        return null;
    }

    public boolean isVisible() {
        return currentTarget != null && System.currentTimeMillis() - lastTargetTime < TARGET_TIMEOUT;
    }
}
