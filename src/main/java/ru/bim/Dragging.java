package ru.bim;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ChatScreen;

public class Dragging {

    private final Minecraft mc = Minecraft.getInstance();

    private int x, y, width, height;
    private boolean dragging;
    private int dragOffsetX, dragOffsetY;

    public Dragging(int startX, int startY, int width, int height) {
        this.x = startX;
        this.y = startY;
        this.width = width;
        this.height = height;
    }

    /** Обновить состояние (нужно вызывать каждый кадр перед рендером) */
    public void update() {
        if (!(mc.screen instanceof ChatScreen)) {
            dragging = false;
            return;
        }

        // Используем масштаб GUI напрямую
        double mx = mc.mouseHandler.xpos() / mc.getWindow().getGuiScale();
        double my = mc.mouseHandler.ypos() / mc.getWindow().getGuiScale();

        boolean hovered = isHovered((int) mx, (int) my);

        if (hovered && mc.mouseHandler.isLeftPressed() && !dragging) {
            dragging = true;
            dragOffsetX = (int) mx - x;
            dragOffsetY = (int) my - y;
        }

        if (!mc.mouseHandler.isLeftPressed()) {
            dragging = false;
        }

        if (dragging) {
            x = (int) mx - dragOffsetX;
            y = (int) my - dragOffsetY;
        }
    }

    /** Проверка наведения */
    public boolean isHovered(int mx, int my) {
        return mx >= x && mx <= x + width &&
                my >= y && my <= y + height;
    }

    // Геттеры
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    // Можно менять размеры динамически
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }
}
