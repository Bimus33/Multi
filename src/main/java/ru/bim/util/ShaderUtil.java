package ru.bim.util;

import org.lwjgl.opengl.GL20;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

public class ShaderUtil {
    private final int programId;
    private final boolean hasVertexShader;

    public ShaderUtil(String fragmentShaderCode, boolean hasVertexShader) {
        this.hasVertexShader = hasVertexShader;
        int vertexShader = hasVertexShader ? createShader("#version 120\nvoid main() {\n    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;\n    gl_TexCoord[0] = gl_MultiTexCoord0;\n}", GL_VERTEX_SHADER) : 0;
        int fragmentShader = createShader(fragmentShaderCode, GL_FRAGMENT_SHADER);

        programId = GL20.glCreateProgram();
        if (hasVertexShader) GL20.glAttachShader(programId, vertexShader);
        GL20.glAttachShader(programId, fragmentShader);
        GL20.glLinkProgram(programId);
    }

    private int createShader(String code, int type) {
        int shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, code);
        GL20.glCompileShader(shader);

        // Проверка компиляции шейдера
        int compiled = GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS);
        if (compiled == GL_FALSE) {
            String log = GL20.glGetShaderInfoLog(shader);
            System.err.println("Shader compilation error: " + log);
            GL20.glDeleteShader(shader);
            return 0;
        }

        return shader;
    }

    public void init() {
        GL20.glUseProgram(programId);
    }

    public void unload() {
        GL20.glUseProgram(0);
    }

    public void setUniformf(String name, float... values) {
        int location = GL20.glGetUniformLocation(programId, name);
        if (location == -1) return;

        switch (values.length) {
            case 1: GL20.glUniform1f(location, values[0]); break;
            case 2: GL20.glUniform2f(location, values[0], values[1]); break;
            case 3: GL20.glUniform3f(location, values[0], values[1], values[2]); break;
            case 4: GL20.glUniform4f(location, values[0], values[1], values[2], values[3]); break;
        }
    }

    public void setUniformi(String name, int value) {
        int location = GL20.glGetUniformLocation(programId, name);
        if (location == -1) return;
        GL20.glUniform1i(location, value);
    }

    public static void drawQuads(float x, float y, float width, float height) {
        glBegin(GL_QUADS);
        glTexCoord2f(0, 0);
        glVertex2f(x, y);
        glTexCoord2f(0, 1);
        glVertex2f(x, y + height);
        glTexCoord2f(1, 1);
        glVertex2f(x + width, y + height);
        glTexCoord2f(1, 0);
        glVertex2f(x + width, y);
        glEnd();
    }
}