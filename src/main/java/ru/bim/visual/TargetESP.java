package ru.bim.visual;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.Comparator;
import java.util.List;

public class TargetESP {

    private static final ResourceLocation TARGET_TEXTURE = new ResourceLocation("multi", "assets/textures/target.png");
    private static final float TARGET_SIZE = 1.0f;
    private static Entity currentTarget = null;

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // Получаем ближайшую сущность (включая мирных)
        Entity target = getNearestEntity(mc);
        currentTarget = target;

        if (target != null) {
            renderTargetOnEntity(target, event.getPartialTicks(), mc);
        }
    }

    private Entity getNearestEntity(Minecraft mc) {
        // Получаем все живые сущности вокруг игрока (включая мирных)
        List<LivingEntity> entities = mc.level.getEntitiesOfClass(LivingEntity.class,
                new AxisAlignedBB(mc.player.blockPosition()).inflate(20.0D), // Радиус 20 блоков
                entity -> entity != mc.player && entity.isAlive() // Исключаем только игрока и мертвых
        );

        if (entities.isEmpty()) {
            return null;
        }

        // Сортируем по расстоянию и берем ближайшую
        return entities.stream()
                .min(Comparator.comparingDouble(entity -> entity.distanceTo(mc.player)))
                .orElse(null);
    }

    private void renderTargetOnEntity(Entity entity, float partialTicks, Minecraft mc) {
        // Получаем позицию сущности с учетом частичных тиков
        Vector3d interpolatedPos = getInterpolatedPosition(entity, partialTicks);
        double x = interpolatedPos.x;
        double y = interpolatedPos.y + entity.getBbHeight() + 0.5; // Над головой
        double z = interpolatedPos.z;

        // Получаем позицию камеры
        Vector3d view = mc.gameRenderer.getMainCamera().getPosition();
        double cameraX = view.x;
        double cameraY = view.y;
        double cameraZ = view.z;

        // Настраиваем рендер
        mc.getTextureManager().bind(TARGET_TEXTURE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder(); // Исправлено: getBuffer() вместо getBuilder()

        // Начинаем построение квада
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        // Вычисляем координаты
        float halfSize = TARGET_SIZE / 2.0f;

        // Верхний левый угол
        buffer.vertex(x - halfSize - cameraX, y + halfSize - cameraY, z - cameraZ)
                .uv(0.0f, 0.0f).endVertex();
        // Верхний правый угол
        buffer.vertex(x + halfSize - cameraX, y + halfSize - cameraY, z - cameraZ)
                .uv(1.0f, 0.0f).endVertex();
        // Нижний правый угол
        buffer.vertex(x + halfSize - cameraX, y - halfSize - cameraY, z - cameraZ)
                .uv(1.0f, 1.0f).endVertex();
        // Нижний левый угол
        buffer.vertex(x - halfSize - cameraX, y - halfSize - cameraY, z - cameraZ)
                .uv(0.0f, 1.0f).endVertex();

        tessellator.end();

        // Восстанавливаем настройки рендера
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
    }

    private Vector3d getInterpolatedPosition(Entity entity, float partialTicks) {
        // Интерполируем позицию для плавного отображения
        double x = entity.xo + (entity.getX() - entity.xo) * partialTicks;
        double y = entity.yo + (entity.getY() - entity.yo) * partialTicks;
        double z = entity.zo + (entity.getZ() - entity.zo) * partialTicks;

        return new Vector3d(x, y, z);
    }

    // Метод для получения текущей цели (может пригодиться для других функций)
    public static Entity getCurrentTarget() {
        return currentTarget;
    }

    // Метод для принудительной установки цели
    public static void setCurrentTarget(Entity target) {
        currentTarget = target;
    }

    // Метод для очистки цели
    public static void clearTarget() {
        currentTarget = null;
    }

    // Метод для проверки, есть ли активная цель
    public static boolean hasTarget() {
        return currentTarget != null;
    }

    // Метод для получения расстояния до цели
    public static double getTargetDistance(Minecraft mc) {
        if (currentTarget == null || mc.player == null) return -1;
        return mc.player.distanceTo(currentTarget);
    }
}