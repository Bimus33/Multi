package ru.bim.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import ru.bim.util.RenderUtil;

import java.awt.*;

public class GuiCreatorWindow {

    private int x, y;
    private final int width = 200;
    private final int height = 150;
    private final int headerHeight = 18;
    private final int cornerRadius = 5;

    private boolean dragging = false;
    private int dragX, dragY;

    private boolean visible = false;
    private String title = "Settings";

    public GuiCreatorWindow(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }

    /** Вызов при ПКМ по кнопке */
    public void open(String moduleName, int parentX, int parentY, int parentWidth) {
        this.title = moduleName + " Settings";
        this.x = parentX + parentWidth + 15; // появляемся правее GUI
        this.y = parentY + 30;
        this.visible = true;
    }

    public void close() {
        this.visible = false;
    }

    public boolean isVisible() {
        return visible;
    }

    /** Рендер окна */
    public void render(MatrixStack matrixStack, int mouseX, int mouseY) {
        if (!visible) return;

        // Фон + тень
        RenderUtil.drawShadow(x, y, width, height, 2f, new Color(0, 255, 0, 180));
        RenderUtil.drawRound(x, y, width, height, cornerRadius, Color.BLACK);

        // Заголовок (без тени)
        RenderUtil.drawRound(x, y, width, headerHeight, cornerRadius, new Color(30, 30, 30));
        Minecraft.getInstance().font.draw(matrixStack, title, x + 8, y + 5, 0xFFFFFF);

        // Пример контента
        Minecraft.getInstance().font.draw(matrixStack, "Настройки скоро будут...", x + 10, y + 35, 0xAAAAAA);
    }

    /** Обработка клика */
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible) return false;

        // Перетаскивание
        if (button == 0 && isInHeader((int) mouseX, (int) mouseY)) {
            dragging = true;
            dragX = (int) mouseX - x;
            dragY = (int) mouseY - y;
            return true;
        }

        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) dragging = false;
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragging && button == 0) {
            this.x = (int) mouseX - this.dragX;
            this.y = (int) mouseY - this.dragY;
            clampWindowPosition();
            return true;
        }
        return false;
    }

    private boolean isInHeader(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width &&
                mouseY >= y && mouseY <= y + headerHeight;
    }

    private void clampWindowPosition() {
        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        x = Math.max(0, Math.min(x, screenWidth - width));
        y = Math.max(0, Math.min(y, screenHeight - height));
    }
}
