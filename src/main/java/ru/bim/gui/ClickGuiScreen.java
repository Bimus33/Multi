package ru.bim.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.StringTextComponent;
import ru.bim.hud.HudManager;
import ru.bim.util.RenderUtil;

import java.awt.Color;
import java.util.*;

public class ClickGuiScreen extends Screen {

    private int x;
    private int y;
    private final int width = 500;
    private final int height = 400;
    private final int headerHeight = 28;
    private final int cornerRadius = 6;

    private boolean dragging = false;
    private int dragX, dragY;

    private final String[] tabs = {"Combat", "Movement", "Render", "Misc", "Settings"};
    private String activeTab = "Combat";

    private final Map<String, List<String>> tabContents = new HashMap<>();
    private final GuiCreatorWindow settingsWindow = new GuiCreatorWindow(0, 0);

    public ClickGuiScreen() {
        super(new StringTextComponent("Bim Client GUI"));

        for (String tab : tabs) {
            List<String> buttons = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                buttons.add(tab + " " + i);
            }
            tabContents.put(tab, buttons);
        }

        // Добавляем кнопку HUD в Render-вкладку
        tabContents.get("Render").add("HUD");
    }

    @Override
    public void init() {
        super.init();
        Minecraft mc = Minecraft.getInstance();
        this.x = (mc.getWindow().getGuiScaledWidth() - width) / 2;
        this.y = (mc.getWindow().getGuiScaledHeight() - height) / 2;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        RenderUtil.drawShadow(x, y, width, height, 2f, new Color(0, 255, 0, 180));
        RenderUtil.drawRound(x, y, width, height, cornerRadius, Color.BLACK);

        RenderUtil.drawRound(x, y, width, headerHeight, cornerRadius, Color.BLACK);
        drawCenteredString(matrixStack, this.font, "Bim Client", x + width / 2, y + 8, 0xFFFFFFFF);

        int tabX = x + 12;
        int tabY = y + headerHeight + 12;
        int tabW = 100;
        int tabH = 28;
        int spacing = 6;

        for (int i = 0; i < tabs.length; i++) {
            String tab = tabs[i];
            boolean selected = tab.equals(activeTab);

            Color bg = selected ? new Color(0, 150, 0) : new Color(30, 30, 30);
            int bx = tabX;
            int by = tabY + i * (tabH + spacing);

            RenderUtil.drawShadow(bx, by, tabW, tabH, 2f, new Color(0, 255, 0, 180));
            RenderUtil.drawRound(bx, by, tabW, tabH, 4, bg);

            this.font.draw(matrixStack, tab,
                    bx + (tabW - this.font.width(tab)) / 2f,
                    by + (tabH - this.font.lineHeight) / 2f,
                    0xFFFFFFFF);
        }

        List<String> content = tabContents.getOrDefault(activeTab, Collections.emptyList());
        int contentX = tabX + tabW + 25;
        int contentY = y + headerHeight + 20;
        int btnW = 150;
        int btnH = 28;
        int colSpacing = 14;
        int rowSpacing = 10;

        for (int i = 0; i < content.size(); i++) {
            int col = i % 2;
            int row = i / 2;
            int bx = contentX + col * (btnW + colSpacing);
            int by = contentY + row * (btnH + rowSpacing);

            RenderUtil.drawShadow(bx, by, btnW, btnH, 2f, new Color(0, 255, 0, 180));
            RenderUtil.drawRound(bx, by, btnW, btnH, 4, new Color(50, 50, 50));
            this.font.draw(matrixStack, content.get(i),
                    bx + (btnW - this.font.width(content.get(i))) / 2f,
                    by + (btnH - this.font.lineHeight) / 2f,
                    0xFFFFFFFF);
        }

        settingsWindow.render(matrixStack, mouseX, mouseY);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isInHeader((int) mouseX, (int) mouseY)) {
            dragging = true;
            dragX = (int) mouseX - x;
            dragY = (int) mouseY - y;
            return true;
        }

        int tabX = x + 12;
        int tabY = y + headerHeight + 12;
        int tabW = 100;
        int tabH = 28;
        int spacing = 6;
        for (int i = 0; i < tabs.length; i++) {
            int bx = tabX;
            int by = tabY + i * (tabH + spacing);
            if (mouseX >= bx && mouseX <= bx + tabW && mouseY >= by && mouseY <= by + tabH) {
                activeTab = tabs[i];
                return true;
            }
        }

        if (button == 1) {
            List<String> content = tabContents.getOrDefault(activeTab, Collections.emptyList());
            int contentX = tabX + tabW + 25;
            int contentY = y + headerHeight + 20;
            int btnW = 150;
            int btnH = 28;
            int colSpacing = 14;
            int rowSpacing = 10;

            for (int i = 0; i < content.size(); i++) {
                int col = i % 2;
                int row = i / 2;
                int bx = contentX + col * (btnW + colSpacing);
                int by = contentY + row * (btnH + rowSpacing);
                if (mouseX >= bx && mouseX <= bx + btnW && mouseY >= by && mouseY <= by + btnH) {
                    String btnName = content.get(i);
                    if (btnName.equals("HUD")) {
                        settingsWindow.open("HUD Settings", x, y, width);
                    }
                    return true;
                }
            }
        }

        if (settingsWindow.mouseClicked(mouseX, mouseY, button)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) dragging = false;
        settingsWindow.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragging && button == 0) {
            this.x = (int) mouseX - this.dragX;
            this.y = (int) mouseY - this.dragY;
            clampWindowPosition();
            return true;
        }
        if (settingsWindow.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;
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
        x = Math.max(0, Math.min(x, screenWidth - width));
        y = Math.max(0, Math.min(y, screenHeight - height));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.minecraft.setScreen(null);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() { return false; }
    @Override
    public boolean shouldCloseOnEsc() { return true; }
    public boolean shouldRenderBackground() { return true; }

    public static void openGUI() {
        Minecraft.getInstance().setScreen(new ClickGuiScreen());
    }
}
