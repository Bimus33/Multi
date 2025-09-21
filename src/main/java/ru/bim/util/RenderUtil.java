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

    // Основной метод для анимированных градиентов
    public static void drawAnimatedGradientRound(double x, double y, double width, double height, double radius,
                                                 Color color1, Color color2, Color color3, Color color4,
                                                 GradientType gradientType, float angle, AnimationType animationType, String animationId) {
        drawAnimatedGradientRound((float) x, (float) y, (float) width, (float) height, (float) radius,
                color1, color2, color3, color4, gradientType, angle, animationType, animationId);
    }

    public static void drawAnimatedGradientRound(float x, float y, float width, float height, float radius,
                                                 Color color1, Color color2, Color color3, Color color4,
                                                 GradientType gradientType, float angle, AnimationType animationType, String animationId) {
        GlStateManager._enableBlend();
        GlStateManager._blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        gradientShader.init();

        // Получаем или создаем состояние анимации
        AnimationState state = animationStates.computeIfAbsent(animationId, k -> new AnimationState());
        updateAnimationState(state);

        setupRoundedRectUniforms(x, y, width, height, radius, gradientShader);
        gradientShader.setUniformf("color1", color1.getRed() / 255f, color1.getGreen() / 255f, color1.getBlue() / 255f, color1.getAlpha() / 255f);
        gradientShader.setUniformf("color2", color2.getRed() / 255f, color2.getGreen() / 255f, color2.getBlue() / 255f, color2.getAlpha() / 255f);
        gradientShader.setUniformf("color3", color3.getRed() / 255f, color3.getGreen() / 255f, color3.getBlue() / 255f, color3.getAlpha() / 255f);
        gradientShader.setUniformf("color4", color4.getRed() / 255f, color4.getGreen() / 255f, color4.getBlue() / 255f, color4.getAlpha() / 255f);
        gradientShader.setUniformi("gradientType", gradientType.ordinal());
        gradientShader.setUniformf("angle", angle);
        gradientShader.setUniformf("time", (float) (System.currentTimeMillis() - startTime));
        gradientShader.setUniformf("animationProgress", state.progress);
        gradientShader.setUniformi("animationType", animationType.ordinal());

        ShaderUtil.drawQuads(x - 1, y - 1, width + 2, height + 2);
        gradientShader.unload();
        GlStateManager._disableBlend();
    }

    // Методы с анимацией по умолчанию
    public static void drawAnimatedGradientRound(double x, double y, double width, double height, double radius,
                                                 Color color1, Color color2, Color color3, Color color4,
                                                 GradientType gradientType, float angle) {
        drawAnimatedGradientRound(x, y, width, height, radius, color1, color2, color3, color4,
                gradientType, angle, AnimationType.PULSE, "default");
    }

    // Радужные градиенты с анимацией
    public static void drawRainbowGradient(double x, double y, double width, double height, double radius, GradientType type, AnimationType animationType, String animationId) {
        drawAnimatedGradientRound((float) x, (float) y, (float) width, (float) height, (float) radius,
                Color.RED, Color.GREEN, Color.BLUE, Color.WHITE, type, 0, animationType, animationId);
    }

    // Быстрые методы для анимированных градиентов
    public static void drawAnimatedHorizontalGradient(double x, double y, double width, double height, double radius,
                                                      Color color1, Color color2, Color color3, Color color4,
                                                      AnimationType animationType, String animationId) {
        drawAnimatedGradientRound(x, y, width, height, radius, color1, color2, color3, color4,
                GradientType.HORIZONTAL, 0, animationType, animationId);
    }

    public static void drawAnimatedVerticalGradient(double x, double y, double width, double height, double radius,
                                                    Color color1, Color color2, Color color3, Color color4,
                                                    AnimationType animationType, String animationId) {
        drawAnimatedGradientRound(x, y, width, height, radius, color1, color2, color3, color4,
                GradientType.VERTICAL, 0, animationType, animationId);
    }

    // Простые анимированные градиенты (2 цвета)
    public static void drawSimpleAnimatedGradient(double x, double y, double width, double height, double radius,
                                                  Color color1, Color color2, GradientType gradientType,
                                                  AnimationType animationType, String animationId) {
        // Создаем промежуточные цвета для плавной анимации
        Color color1a = interpolateColor(color1, color2, 0.3f);
        Color color2a = interpolateColor(color1, color2, 0.7f);

        drawAnimatedGradientRound(x, y, width, height, radius, color1, color1a, color2a, color2,
                gradientType, 0, animationType, animationId);
    }

    // Обновление состояния анимации
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

    // Сброс анимации
    public static void resetAnimation(String animationId) {
        animationStates.remove(animationId);
    }

    // Установка прогресса анимации вручную
    public static void setAnimationProgress(String animationId, float progress) {
        AnimationState state = animationStates.computeIfAbsent(animationId, k -> new AnimationState());
        state.progress = Math.max(0.0f, Math.min(1.0f, progress));
    }

    private static void setupRoundedRectUniforms(float x, float y, float width, float height, float radius, ShaderUtil shader) {
        MainWindow window = mc.getWindow();
        float scaleFactor = (float) window.getGuiScale();
        shader.setUniformf("location", x * scaleFactor,
                (window.getHeight() - (height * scaleFactor)) - (y * scaleFactor));
        shader.setUniformf("rectSize", width * scaleFactor, height * scaleFactor);
        shader.setUniformf("radius", radius * scaleFactor);
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

    // Генерация цветов радуги
    public static Color getRainbowColor(float progress, float saturation, float brightness) {
        float hue = (progress + (System.currentTimeMillis() % 10000) / 10000f) % 1f;
        return Color.getHSBColor(hue, saturation, brightness);
    }

    public static Color getRainbowColor(float progress) {
        return getRainbowColor(progress, 1f, 1f);
    }
}