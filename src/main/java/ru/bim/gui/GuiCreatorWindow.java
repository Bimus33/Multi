package ru.bim.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import ru.bim.hud.HudManager;
import ru.bim.util.RenderUtil;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class GuiCreatorWindow {

    private int x, y;
    private final int width = 220;
    private final int height = 220;
    private final int headerHeight = 20;
    private final int cornerRadius = 5;

    private boolean dragging = false;
    private int dragX, dragY;

    private boolean visible = false;
    private String title = "Settings";

    // HUD-переключатели (state + animPos)
    private static class Toggle {
        boolean state;
        float animPos;

        Toggle(boolean state) {
            this.state = state;
            this.animPos = state ? 1f : 0f;
        }
    }

    private final Map<String, Toggle> toggles = new LinkedHashMap<>();

    public GuiCreatorWindow(int startX, int startY) {
        this.x = startX;
        this.y = startY;

        toggles.put("WaterMark", new Toggle(HudManager.showWatermark));
        toggles.put("Ping", new Toggle(HudManager.showPing));
        toggles.put("Coordinates", new Toggle(HudManager.showCoordinates));
        toggles.put("ArrayList", new Toggle(HudManager.showArrayList));
        toggles.put("Inventory", new Toggle(HudManager.showInventory));
        toggles.put("ActivePotion", new Toggle(HudManager.showActivePotion));
        toggles.put("TargetHUD", new Toggle(HudManager.showTargetHUD));
    }

    public void open(String windowTitle, int parentX, int parentY, int parentWidth) {
        this.title = windowTitle;
        this.x = parentX + parentWidth + 15;
        this.y = parentY + 30;
        this.visible = true;
    }

    public void close() { this.visible = false; }
    public boolean isVisible() { return visible; }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY) {
        if (!visible) return;

        // окно
        RenderUtil.drawShadow(x, y, width, height, 2f, new Color(0, 255, 0, 180));
        RenderUtil.drawRound(x, y, width, height, cornerRadius, Color.BLACK);

        // заголовок (зелёный как тень)
        RenderUtil.drawRound(x, y, width, headerHeight, cornerRadius, new Color(0, 255, 0, 180));
        Minecraft.getInstance().font.draw(matrixStack, title, x + 8, y + 6, 0xFFFFFF);

        // свитчи
        int startY = y + headerHeight + 12;
        int lineH = 22;
        int i = 0;
        for (Map.Entry<String, Toggle> entry : toggles.entrySet()) {
            int cy = startY + i * lineH;
            drawSwitch(matrixStack, entry.getKey(), entry.getValue(), cy);
            i++;
        }
    }

    private void drawSwitch(MatrixStack matrixStack, String name, Toggle toggle, int cy) {
        int switchW = 34;
        int switchH = 14;
        int margin = 10;

        int sx = x + width - switchW - margin;
        int sy = cy;

        // плавная анимация (инерция)
        float speed = 0.2f;
        float target = toggle.state ? 1f : 0f;
        toggle.animPos += (target - toggle.animPos) * speed;

        // фон
        Color bg = toggle.state ? new Color(0, 200, 0) : new Color(100, 100, 100);
        RenderUtil.drawRound(sx, sy, switchW, switchH, switchH / 2, bg);

        // бегунок
        int knobSize = switchH - 4;
        int knobX = (int) (sx + 2 + (switchW - knobSize - 4) * toggle.animPos);
        int knobY = sy + 2;
        RenderUtil.drawRound(knobX, knobY, knobSize, knobSize, knobSize / 2, Color.WHITE);

        // текст слева
        Minecraft.getInstance().font.draw(matrixStack, name, x + 10, sy + 3, 0xFFFFFF);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible) return false;

        // перетаскивание
        if (button == 0 && isInHeader((int) mouseX, (int) mouseY)) {
            dragging = true;
            dragX = (int) mouseX - x;
            dragY = (int) mouseY - y;
            return true;
        }

        // клики по свитчам
        int startY = y + headerHeight + 12;
        int lineH = 22;
        int switchW = 34;
        int switchH = 14;
        int margin = 10;
        int i = 0;
        for (String key : toggles.keySet()) {
            int cy = startY + i * lineH;
            int sx = x + width - switchW - margin;
            int sy = cy;
            if (mouseX >= sx && mouseX <= sx + switchW &&
                    mouseY >= sy && mouseY <= sy + switchH) {
                Toggle t = toggles.get(key);
                t.state = !t.state;
                updateHudManager(key, t.state);
                return true;
            }
            i++;
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

    private void updateHudManager(String key, boolean value) {
        switch (key) {
            case "WaterMark": HudManager.showWatermark = value; break;
            case "Ping": HudManager.showPing = value; break;
            case "Coordinates": HudManager.showCoordinates = value; break;
            case "ArrayList": HudManager.showArrayList = value; break;
            case "Inventory": HudManager.showInventory = value; break;
            case "ActivePotion": HudManager.showActivePotion = value; break;
            case "TargetHUD": HudManager.showTargetHUD = value; break;
        }
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
