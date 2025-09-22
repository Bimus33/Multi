package ru.bim.util;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MainWindow;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

public class RenderUtil {

    private static final Minecraft mc = Minecraft.getInstance();
    private static long startTime = System.currentTimeMillis();

    // Система анимаций
    private static final Map<String, AnimationState> animationStates = new HashMap<>();
    private static final float ANIMATION_SPEED = 0.002f;

    // Шейдер для теней
    private static final ShaderUtil shadowShader = new ShaderUtil(
            "#version 120\n" +
                    "uniform vec2 location, rectSize;\n" +
                    "uniform vec4 color;\n" +
                    "uniform float radius, blurStrength;\n" +
                    "uniform int shadowType;\n" +
                    "float roundSDF(vec2 p, vec2 b, float r) {\n" +
                    "    return length(max(abs(p) - b, 0.0)) - r;\n" +
                    "}\n" +
                    "void main() {\n" +
                    "    vec2 rectHalf = rectSize * .5;\n" +
                    "    vec2 pos = gl_TexCoord[0].st;\n" +
                    "    \n" +
                    "    // Расчет тени\n" +
                    "    float distance = roundSDF(rectHalf - (pos * rectSize), rectHalf - radius - 1., radius);\n" +
                    "    \n" +
                    "    if (shadowType == 0) { // Внешняя тень\n" +
                    "        float shadowAlpha = smoothstep(-blurStrength, blurStrength, -distance) * color.a;\n" +
                    "        gl_FragColor = vec4(color.rgb, shadowAlpha);\n" +
                    "    } else { // Внутренняя тень\n" +
                    "        float shadowAlpha = (1.0 - smoothstep(0.0, blurStrength, distance)) * color.a;\n" +
                    "        gl_FragColor = vec4(color.rgb, shadowAlpha);\n" +
                    "    }\n" +
                    "}", true);

    private static final ShaderUtil roundedShader = new ShaderUtil(
            "#version 120\n" +
                    "uniform vec2 location, rectSize;\n" +
                    "uniform vec4 color;\n" +
                    "uniform float radius;\n" +
                    "uniform bool blur;\n" +
                    "float roundSDF(vec2 p, vec2 b, float r) {\n" +
                    "    return length(max(abs(p) - b, 0.0)) - r;\n" +
                    "}\n" +
                    "void main() {\n" +
                    "    vec2 rectHalf = rectSize * .5;\n" +
                    "    float smoothedAlpha = (1.0-smoothstep(0.0, 1.0, roundSDF(rectHalf - (gl_TexCoord[0].st * rectSize), rectHalf - radius - 1., radius))) * color.a;\n" +
                    "    gl_FragColor = vec4(color.rgb, smoothedAlpha);\n" +
                    "}", true);

    private static final ShaderUtil gradientShader = new ShaderUtil(
            "#version 120\n" +
                    "uniform vec2 location, rectSize;\n" +
                    "uniform vec4 color1, color2, color3, color4;\n" +
                    "uniform float radius;\n" +
                    "uniform int gradientType;\n" +
                    "uniform float angle;\n" +
                    "uniform float time;\n" +
                    "uniform float animationProgress;\n" +
                    "uniform int animationType;\n" +
                    "float roundSDF(vec2 p, vec2 b, float r) {\n" +
                    "    return length(max(abs(p) - b, 0.0)) - r;\n" +
                    "}\n" +
                    "vec4 rainbowGradient(float progress) {\n" +
                    "    // Создаем радужный градиент с помощью тригонометрических функций\n" +
                    "    float r = sin(progress * 6.28318 + time * 0.001) * 0.5 + 0.5;\n" +
                    "    float g = sin(progress * 6.28318 + time * 0.001 + 2.094) * 0.5 + 0.5;\n" +
                    "    float b = sin(progress * 6.28318 + time * 0.001 + 4.188) * 0.5 + 0.5;\n" +
                    "    return vec4(r, g, b, 1.0);\n" +
                    "}\n" +
                    "vec4 applyAnimation(vec4 originalColor, vec2 pos, float progress) {\n" +
                    "    if (animationType == 0) return originalColor; // Нет анимации\n" +
                    "    \n" +
                    "    if (animationType == 1) { // Пульсация\n" +
                    "        float pulse = sin(time * 0.002 + progress * 3.14159) * 0.2 + 0.8;\n" +
                    "        return originalColor * vec4(pulse, pulse, pulse, 1.0);\n" +
                    "    }\n" +
                    "    else if (animationType == 2) { // Волна\n" +
                    "        float wave = sin(pos.x * 10.0 + time * 0.003) * 0.1 + 0.9;\n" +
                    "        return originalColor * vec4(wave, wave, wave, 1.0);\n" +
                    "    }\n" +
                    "    else if (animationType == 3) { // Циклическое изменение насыщенности\n" +
                    "        float sat = sin(time * 0.0015 + progress * 6.28318) * 0.3 + 0.7;\n" +
                    "        vec3 hsl = rgbToHsl(originalColor.rgb);\n" +
                    "        hsl.y = sat;\n" +
                    "        return vec4(hslToRgb(hsl), originalColor.a);\n" +
                    "    }\n" +
                    "    else if (animationType == 4) { // Сдвиг оттенка\n" +
                    "        vec3 hsl = rgbToHsl(originalColor.rgb);\n" +
                    "        hsl.x = mod(hsl.x + time * 0.0001, 1.0);\n" +
                    "        return vec4(hslToRgb(hsl), originalColor.a);\n" +
                    "    }\n" +
                    "    \n" +
                    "    return originalColor;\n" +
                    "}\n" +
                    "vec3 rgbToHsl(vec3 rgb) {\n" +
                    "    float cmax = max(max(rgb.r, rgb.g), rgb.b);\n" +
                    "    float cmin = min(min(rgb.r, rgb.g), rgb.b);\n" +
                    "    float delta = cmax - cmin;\n" +
                    "    \n" +
                    "    float h = 0.0;\n" +
                    "    if (delta != 0.0) {\n" +
                    "        if (cmax == rgb.r) h = mod((rgb.g - rgb.b) / delta, 6.0);\n" +
                    "        else if (cmax == rgb.g) h = (rgb.b - rgb.r) / delta + 2.0;\n" +
                    "        else h = (rgb.r - rgb.g) / delta + 4.0;\n" +
                    "        h /= 6.0;\n" +
                    "    }\n" +
                    "    \n" +
                    "    float l = (cmax + cmin) / 2.0;\n" +
                    "    float s = delta == 0.0 ? 0.0 : delta / (1.0 - abs(2.0 * l - 1.0));\n" +
                    "    \n" +
                    "    return vec3(h, s, l);\n" +
                    "}\n" +
                    "vec3 hslToRgb(vec3 hsl) {\n" +
                    "    float c = (1.0 - abs(2.0 * hsl.z - 1.0)) * hsl.y;\n" +
                    "    float x = c * (1.0 - abs(mod(hsl.x * 6.0, 2.0) - 1.0));\n" +
                    "    float m = hsl.z - c / 2.0;\n" +
                    "    \n" +
                    "    vec3 rgb;\n" +
                    "    if (hsl.x < 1.0/6.0) rgb = vec3(c, x, 0.0);\n" +
                    "    else if (hsl.x < 2.0/6.0) rgb = vec3(x, c, 0.0);\n" +
                    "    else if (hsl.x < 3.0/6.0) rgb = vec3(0.0, c, x);\n" +
                    "    else if (hsl.x < 4.0/6.0) rgb = vec3(0.0, x, c);\n" +
                    "    else if (hsl.x < 5.0/6.0) rgb = vec3(x, 0.0, c);\n" +
                    "    else rgb = vec3(c, 0.0, x);\n" +
                    "    \n" +
                    "    return rgb + vec3(m);\n" +
                    "}\n" +
                    "void main() {\n" +
                    "    vec2 rectHalf = rectSize * .5;\n" +
                    "    vec2 pos = gl_TexCoord[0].st;\n" +
                    "    \n" +
                    "    // Gradient calculation\n" +
                    "    vec4 gradientColor;\n" +
                    "    if (gradientType == 0) { // Horizontal\n" +
                    "        gradientColor = mix(mix(color1, color2, pos.x), mix(color3, color4, pos.x), animationProgress);\n" +
                    "    } else if (gradientType == 1) { // Vertical\n" +
                    "        gradientColor = mix(mix(color1, color2, pos.y), mix(color3, color4, pos.y), animationProgress);\n" +
                    "    } else if (gradientType == 2) { // Angular\n" +
                    "        float rad = radians(angle);\n" +
                    "        float gradientPos = (pos.x * cos(rad) + pos.y * sin(rad));\n" +
                    "        gradientColor = mix(mix(color1, color2, gradientPos), mix(color3, color4, gradientPos), animationProgress);\n" +
                    "    } else if (gradientType == 3) { // Radial\n" +
                    "        float distance = length(pos - vec2(0.5));\n" +
                    "        gradientColor = mix(mix(color1, color2, distance * 2.0), mix(color3, color4, distance * 2.0), animationProgress);\n" +
                    "    } else { // Rainbow\n" +
                    "        float progress;\n" +
                    "        if (gradientType == 5) progress = pos.x; // Horizontal rainbow\n" +
                    "        else if (gradientType == 6) progress = pos.y; // Vertical rainbow\n" +
                    "        else progress = length(pos - vec2(0.5)) * 2.0; // Radial rainbow\n" +
                    "        gradientColor = rainbowGradient(progress);\n" +
                    "    }\n" +
                    "    \n" +
                    "    // Применяем анимацию\n" +
                    "    gradientColor = applyAnimation(gradientColor, pos, animationProgress);\n" +
                    "    \n" +
                    "    float smoothedAlpha = (1.0-smoothstep(0.0, 1.0, roundSDF(rectHalf - (pos * rectSize), rectHalf - radius - 1., radius))) * gradientColor.a;\n" +
                    "    gl_FragColor = vec4(gradientColor.rgb, smoothedAlpha);\n" +
                    "}", true);

    // Класс для хранения состояния анимации
    private static class AnimationState {
        public float progress = 0.0f;
        public long lastUpdate = System.currentTimeMillis();
        public boolean reverse = false;
    }

    // Enum для типов анимаций
    public enum AnimationType {
        NONE,           // Без анимации
        PULSE,          // Пульсация
        WAVE,           // Волна
        SATURATION,     // Изменение насыщенности
        HUE_SHIFT       // Сдвиг оттенка
    }

    // Enum для типов теней
    public enum ShadowType {
        OUTER,          // Внешняя тень
        INNER           // Внутренняя тень
    }

    // ========== МЕТОДЫ ДЛЯ ТЕНЕЙ ==========

    /**
     * Отрисовка тени с закругленными углами
     */
    public static void drawAnimatedGradientRound(double x, double y, double width, double height, double radius,
                                                 Color color1, Color color2, Color color3, Color color4,
                                                 GradientType gradientType, float angle,
                                                 AnimationType animationType, String animationId) {
        drawAnimatedGradientRound((float) x, (float) y, (float) width, (float) height, (float) radius,
                color1, color2, color3, color4, gradientType, angle, animationType, animationId);
    }

    public static void drawAnimatedGradientRound(float x, float y, float width, float height, float radius,
                                                 Color color1, Color color2, Color color3, Color color4,
                                                 GradientType gradientType, float angle,
                                                 AnimationType animationType, String animationId) {
        GlStateManager._enableBlend();
        GlStateManager._blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        gradientShader.init();

        setupRoundedRectUniforms(x, y, width, height, radius, gradientShader);

        gradientShader.setUniformf("color1", color1.getRed() / 255f, color1.getGreen() / 255f, color1.getBlue() / 255f, color1.getAlpha() / 255f);
        gradientShader.setUniformf("color2", color2.getRed() / 255f, color2.getGreen() / 255f, color2.getBlue() / 255f, color2.getAlpha() / 255f);
        gradientShader.setUniformf("color3", color3.getRed() / 255f, color3.getGreen() / 255f, color3.getBlue() / 255f, color3.getAlpha() / 255f);
        gradientShader.setUniformf("color4", color4.getRed() / 255f, color4.getGreen() / 255f, color4.getBlue() / 255f, color4.getAlpha() / 255f);

        gradientShader.setUniformi("gradientType", gradientType.ordinal());
        gradientShader.setUniformf("angle", angle);

        AnimationState state = animationStates.computeIfAbsent(animationId, k -> new AnimationState());
        updateAnimationState(state);

        gradientShader.setUniformf("time", (System.currentTimeMillis() - startTime));
        gradientShader.setUniformf("animationProgress", state.progress);
        gradientShader.setUniformi("animationType", animationType.ordinal());

        ShaderUtil.drawQuads(x - 1, y - 1, width + 2, height + 2);
        gradientShader.unload();
        GlStateManager._disableBlend();
    }


    public static void drawShadow(double x, double y, double width, double height, double blur, Color color) {
        drawShadow((float) x, (float) y, (float) width, (float) height, (float) blur, color, ShadowType.OUTER, 5f);
    }

    public static void drawShadow(float x, float y, float width, float height, float blur, Color color) {
        drawShadow(x, y, width, height, blur, color, ShadowType.OUTER, 5f);
    }

    public static void drawShadow(double x, double y, double width, double height, double blur,
                                  Color color, ShadowType shadowType, float radius) {
        drawShadow((float) x, (float) y, (float) width, (float) height, (float) blur, color, shadowType, radius);
    }

    public static void drawShadow(float x, float y, float width, float height, float blur,
                                  Color color, ShadowType shadowType, float radius) {
        GlStateManager._enableBlend();
        GlStateManager._blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        shadowShader.init();

        setupRoundedRectUniforms(x, y, width, height, radius, shadowShader);
        shadowShader.setUniformf("color", color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        shadowShader.setUniformf("blurStrength", blur);
        shadowShader.setUniformi("shadowType", shadowType.ordinal());

        ShaderUtil.drawQuads(x - blur, y - blur, width + blur * 2, height + blur * 2);
        shadowShader.unload();
        GlStateManager._disableBlend();
    }

    /**
     * Многослойная тень с градиентом
     */
    public static void drawMultiShadow(double x, double y, double width, double height,
                                       Color[] colors, float[] blurs, float radius) {
        for (int i = 0; i < colors.length; i++) {
            drawShadow(x - blurs[i], y - blurs[i], width + blurs[i] * 2, height + blurs[i] * 2,
                    blurs[i], colors[i], ShadowType.OUTER, radius);
        }
    }

    /**
     * Быстрый метод для стандартной тени
     */
    public static void drawQuickShadow(double x, double y, double width, double height) {
        drawShadow(x, y, width, height, 8f, new Color(0, 0, 0, 100));
    }

    /**
     * Анимированная тень с пульсацией
     */
    public static void drawAnimatedShadow(double x, double y, double width, double height,
                                          Color baseColor, float minBlur, float maxBlur, String animationId) {
        AnimationState state = animationStates.computeIfAbsent(animationId, k -> new AnimationState());
        updateAnimationState(state);

        float currentBlur = minBlur + (maxBlur - minBlur) * state.progress;
        int alpha = (int) (baseColor.getAlpha() * (0.7f + 0.3f * state.progress));

        drawShadow(x, y, width, height, currentBlur, withAlpha(baseColor, alpha));
    }

    /**
     * Градиентная тень
     */
    public static void drawGradientShadow(double x, double y, double width, double height,
                                          Color startColor, Color endColor, float blur, GradientType gradientType) {
        // Создаем промежуточные цвета для градиента
        Color color1 = interpolateColor(startColor, endColor, 0.0f);
        Color color2 = interpolateColor(startColor, endColor, 0.33f);
        Color color3 = interpolateColor(startColor, endColor, 0.66f);
        Color color4 = interpolateColor(startColor, endColor, 1.0f);

        drawAnimatedGradientRound(x, y, width, height, 0, color1, color2, color3, color4, gradientType, 0, AnimationType.NONE, "shadow_gradient");
    }

    // ========== СУЩЕСТВУЮЩИЕ МЕТОДЫ (остаются без изменений) ==========

    public static void drawRound(double x, double y, double width, double height, double radius, Color color) {
        drawRound((float) x, (float) y, (float) width, (float) height, (float) radius, false, color);
    }

    public static void drawRound(float x, float y, float width, float height, float radius, boolean blur, Color color) {
        GlStateManager._enableBlend();
        GlStateManager._blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        roundedShader.init();

        setupRoundedRectUniforms(x, y, width, height, radius, roundedShader);
        roundedShader.setUniformi("blur", blur ? 1 : 0);
        roundedShader.setUniformf("color", color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);

        ShaderUtil.drawQuads(x - 1, y - 1, width + 2, height + 2);
        roundedShader.unload();
        GlStateManager._disableBlend();
    }

    // Остальные методы остаются без изменений...
    // [Здесь должны быть все остальные методы из вашего оригинального кода]

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    private static void setupRoundedRectUniforms(float x, float y, float width, float height, float radius, ShaderUtil shader) {
        MainWindow window = mc.getWindow();
        float scaleFactor = (float) window.getGuiScale();
        shader.setUniformf("location", x * scaleFactor,
                (window.getHeight() - (height * scaleFactor)) - (y * scaleFactor));
        shader.setUniformf("rectSize", width * scaleFactor, height * scaleFactor);
        shader.setUniformf("radius", radius * scaleFactor);
    }

    private static void updateAnimationState(AnimationState state) {
        long currentTime = System.currentTimeMillis();
        float delta = (currentTime - state.lastUpdate) * ANIMATION_SPEED;
        state.lastUpdate = currentTime;

        if (state.reverse) {
            state.progress -= delta;
            if (state.progress <= 0.0f) {
                state.progress = 0.0f;
                state.reverse = false;
            }
        } else {
            state.progress += delta;
            if (state.progress >= 1.0f) {
                state.progress = 1.0f;
                state.reverse = true;
            }
        }
    }

    // Enum для типов градиентов
    public enum GradientType {
        HORIZONTAL,   // Горизонтальный градиент
        VERTICAL,     // Вертикальный градиент
        ANGULAR,      // Угловой градиент
        RADIAL,       // Радиальный градиент
        RAINBOW_H,    // Горизонтальный радужный
        RAINBOW_V,    // Вертикальный радужный
        RAINBOW_R     // Радиальный радужный
    }

    // Утилиты для работы с цветами
    public static Color interpolateColor(Color color1, Color color2, float progress) {
        progress = Math.max(0, Math.min(1, progress));
        int red = (int) (color1.getRed() + (color2.getRed() - color1.getRed()) * progress);
        int green = (int) (color1.getGreen() + (color2.getGreen() - color1.getGreen()) * progress);
        int blue = (int) (color1.getBlue() + (color2.getBlue() - color1.getBlue()) * progress);
        int alpha = (int) (color1.getAlpha() + (color2.getAlpha() - color1.getAlpha()) * progress);
        return new Color(red, green, blue, alpha);
    }

    public static Color withAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    public static Color withAlpha(Color color, float alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (alpha * 255));
    }
}