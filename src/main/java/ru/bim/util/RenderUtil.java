package ru.bim.util;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MainWindow;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

import static org.lwjgl.opengl.GL11.*;

public class RenderUtil {

    private static final Minecraft mc = Minecraft.getInstance();

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

    private static void setupRoundedRectUniforms(float x, float y, float width, float height, float radius, ShaderUtil shader) {
        MainWindow window = mc.getWindow();
        float scaleFactor = (float) window.getGuiScale();
        shader.setUniformf("location", x * scaleFactor,
                (window.getHeight() - (height * scaleFactor)) - (y * scaleFactor));
        shader.setUniformf("rectSize", width * scaleFactor, height * scaleFactor);
        shader.setUniformf("radius", radius * scaleFactor);
    }

    private static float[] getColorComponents(Color color) {
        return new float[] {
                color.getRed() / 255f,
                color.getGreen() / 255f,
                color.getBlue() / 255f,
                color.getAlpha() / 255f
        };
    }
}