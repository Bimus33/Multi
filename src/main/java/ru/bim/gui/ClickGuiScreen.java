package ru.bim.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.StringTextComponent;
import ru.bim.util.RenderUtil;
import java.awt.Color;

public class ClickGuiScreen extends Screen {

    private int x;
    private int y;
    private final int width = 450;
    private final int height = 350;
    private final int headerHeight = 20;
    private final int cornerRadius = 5;

    private boolean dragging = false;
    private int dragX, dragY;

    public ClickGuiScreen() {
        super(new StringTextComponent("Bim Client GUI"));
    }

    @Override
    public void init() {
        super.init();
        Minecraft mc = Minecraft.getInstance();
        // Правильное центрирование с учетом scaled размеров
        this.x = (mc.getWindow().getGuiScaledWidth() - width) / 2;
        this.y = (mc.getWindow().getGuiScaledHeight() - height) / 2;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        // Рендерим полупрозрачный фон

        // Основной фон окна с закругленными углами (темно-серый)
        RenderUtil.drawRound(x, y, width, height, cornerRadius, Color.BLACK);

        // Заголовок (темный)
        RenderUtil.drawRound(x, y, width, headerHeight, cornerRadius, Color.BLACK);

        // Текст заголовка
        drawCenteredString(matrixStack, this.font, "Bim Client", x + width / 2, y + 6, 0xFFFFFFFF);

        // Отображаем курсор мыши
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Проверяем, кликнули ли по заголовку
        if (button == 0 && isInHeader((int) mouseX, (int) mouseY)) {
            dragging = true;
            dragX = (int) mouseX - x;
            dragY = (int) mouseY - y;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            dragging = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragging && button == 0) {
            // Перемещаем окно
            this.x = (int) mouseX - this.dragX;
            this.y = (int) mouseY - this.dragY;

            // Ограничиваем позицию окна в пределах экрана
            clampWindowPosition();
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private boolean isInHeader(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width &&
                mouseY >= y && mouseY <= y + headerHeight;
    }

    private void clampWindowPosition() {
        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // Ограничиваем позицию, чтобы окно не выходило за пределы экрана
        x = Math.max(0, Math.min(x, screenWidth - width));
        y = Math.max(0, Math.min(y, screenHeight - height));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Закрытие GUI по клавише ESC
        if (keyCode == 256) {
            this.minecraft.setScreen(null);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    public boolean shouldRenderBackground() {
        return true;
    }

    public static void openGUI() {
        Minecraft.getInstance().setScreen(new ClickGuiScreen());
    }
}