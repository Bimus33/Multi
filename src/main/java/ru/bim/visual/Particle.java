package ru.bim.visual;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Particle extends SpriteTexturedParticle {

    private final IAnimatedSprite spriteSet;
    private static boolean enabled = true; // Партиклы включены по умолчанию

    protected Particle(ClientWorld world, double x, double y, double z,
                       double motionX, double motionY, double motionZ,
                       IAnimatedSprite spriteSet) {
        super(world, x, y, z, motionX, motionY, motionZ);

        this.spriteSet = spriteSet;
        this.gravity = 0.0f;
        this.lifetime = 20 + this.random.nextInt(10);
        this.quadSize = 0.2f + this.random.nextFloat() * 0.3f;
        this.hasPhysics = false;

        // Цвет свечения (золотистый/желтый)
        this.rCol = 1.0f;
        this.gCol = 0.8f + this.random.nextFloat() * 0.2f;
        this.bCol = 0.2f + this.random.nextFloat() * 0.2f;
        this.alpha = 1.0f;

        // Случайная начальная скорость
        this.xd = (this.random.nextDouble() - 0.5) * 0.1;
        this.yd = (this.random.nextDouble() - 0.5) * 0.1;
        this.zd = (this.random.nextDouble() - 0.5) * 0.1;

        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        this.move(this.xd, this.yd, this.zd);
        this.xd *= 0.95;
        this.yd *= 0.95;
        this.zd *= 0.95;

        if (this.age > this.lifetime * 0.7) {
            this.alpha = 1.0f - (float)(this.age - this.lifetime * 0.7) / (this.lifetime * 0.3f);
        }

        this.setSpriteFromAge(this.spriteSet);
    }

    @Override
    public IParticleRenderType getRenderType() {
        return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements IParticleFactory<BasicParticleType> {
        private final IAnimatedSprite sprites;

        public Factory(IAnimatedSprite spriteSet) {
            this.sprites = spriteSet;
        }

        public net.minecraft.client.particle.Particle createParticle(BasicParticleType typeIn, ClientWorld worldIn,
                                                                     double x, double y, double z,
                                                                     double xSpeed, double ySpeed, double zSpeed) {
            return new Particle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }

    // Статический метод для создания партиклов при ударе
    public static void spawnHitParticles(ClientWorld world, double x, double y, double z, int count) {
        if (world == null || !enabled) return; // Проверяем, включены ли партиклы

        for (int i = 0; i < count; i++) {
            double angle = Math.random() * Math.PI * 2;
            double speed = 0.1 + Math.random() * 0.2;

            double motionX = Math.cos(angle) * speed;
            double motionY = (Math.random() - 0.2) * 0.1;
            double motionZ = Math.sin(angle) * speed;

            world.addParticle(
                    createParticleType(),
                    x, y + 0.5, z,
                    motionX, motionY, motionZ
            );
        }
    }

    // Метод для создания партиклов при ударе по сущности
    public static void spawnOnEntityHit(ClientWorld world, double entityX, double entityY, double entityZ,
                                        double hitX, double hitY, double hitZ) {
        if (world == null || !enabled) return; // Проверяем, включены ли партиклы

        int count = 10 + world.random.nextInt(6);
        for (int i = 0; i < count; i++) {
            double dirX = (entityX - hitX) * 0.1;
            double dirY = (entityY - hitY) * 0.1 + 0.1;
            double dirZ = (entityZ - hitZ) * 0.1;

            dirX += (world.random.nextDouble() - 0.5) * 0.05;
            dirY += (world.random.nextDouble() - 0.5) * 0.05;
            dirZ += (world.random.nextDouble() - 0.5) * 0.05;

            world.addParticle(
                    createParticleType(),
                    hitX, hitY + 0.2, hitZ,
                    dirX, dirY, dirZ
            );
        }
    }

    private static BasicParticleType createParticleType() {
        return new BasicParticleType(false);
    }

    // Методы для управления состоянием партиклов
    public static void enable() {
        enabled = true;
    }

    public static void disable() {
        enabled = false;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void toggle() {
        enabled = !enabled;
    }
}