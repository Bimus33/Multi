package ru.bim;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import ru.bim.visual.Particle;

public class HitParticleHandler {

    @SubscribeEvent
    public void onEntityAttack(AttackEntityEvent event) {
        if (!Particle.isEnabled()) return; // Проверяем, включены ли партиклы

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        Entity target = event.getTarget();
        if (target == null) return;

        Vector3d hitPos = target.position().add(0, target.getBbHeight() / 2, 0);

        Particle.spawnOnEntityHit(
                (ClientWorld) mc.level,
                target.getX(), target.getY(), target.getZ(),
                hitPos.x, hitPos.y, hitPos.z
        );
    }
}