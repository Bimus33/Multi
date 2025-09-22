package ru.bim.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.StringTextComponent;
import ru.bim.hud.HudManager;
import ru.bim.util.RenderUtil;

import java.awt.Color;
import java.util.*;

/**
 * Click GUI screen: вкладки, контент и новая вкладка "Theme" с множеством цветных кнопок.
 * - каждая кнопка Theme показывает два цвета (градиент): основной (усреднённый) и верхнюю полосу второго цвета
 * - выбранная тема подсвечивается рамкой
 * - при клике по Theme-кнопке selectedThemeIndex обновляется
 *
 * Примечание: это простой визуальный имитатор градиента (полоса сверху) — если в RenderUtil есть метод
 * drawAnimatedGradientRound/подобный — можно заменить для более гладкого градиента.
 */
public class ClickGuiScreen extends Screen {

    private int x;
    private int y;
    private final int width = 500;
    private final int height = 400;
    private final int headerHeight = 28;
    private final int cornerRadius = 6;

    private boolean dragging = false;
    private int dragX, dragY;

    private final String[] tabs = {"Render", "Visual", "Config", "Theme", "Settings"};
    private String activeTab = "Render";

    private final Map<String, List<String>> tabContents = new HashMap<>();
    private final GuiCreatorWindow settingsWindow = new GuiCreatorWindow(0, 0);

    // Themes: пары цветов (start, end). Можно добавить любое количество тем.
    private final List<Color[]> themes = new ArrayList<>();
    private int selectedThemeIndex = 0;

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

        // Добавляем содержимое для Theme (заменяем на реальные темы)
        List<String> themeButtons = new ArrayList<>();
        for (int i = 1; i <= 14; i++) themeButtons.add("Theme " + i);
        tabContents.put("Theme", themeButtons);

        // Заполняем палитру тем (пары цветов)
        themes.add(new Color[]{new Color(255, 105, 180), new Color(0, 206, 209)}); // розовый -> бирюзовый
        themes.add(new Color[]{new Color(255, 94, 98), new Color(255, 195, 113)}); // красно-оранж
        themes.add(new Color[]{new Color(141, 63, 255), new Color(0, 199, 255)}); // фиолет -> голубой
        themes.add(new Color[]{new Color(255, 0, 128), new Color(0, 255, 200)}); // розовый -> teal
        themes.add(new Color[]{new Color(0, 255, 128), new Color(0, 150, 0)}); // салатовый -> зелёный
        themes.add(new Color[]{new Color(255, 215, 0), new Color(255, 69, 0)}); // золотой -> оранжевый
        themes.add(new Color[]{new Color(0, 120, 255), new Color(0, 255, 200)}); // синий -> бирюзовый
        themes.add(new Color[]{new Color(255, 128, 0), new Color(255, 0, 128)}); // оранж -> розовый
        themes.add(new Color[]{new Color(200, 200, 200), new Color(120, 120, 120)}); // серый градиент
        themes.add(new Color[]{new Color(123, 31, 162), new Color(3, 169, 244)}); // deep purple -> light blue
        themes.add(new Color[]{new Color(255, 0, 0), new Color(255, 128, 0)}); // red -> orange
        themes.add(new Color[]{new Color(0, 255, 255), new Color(0, 150, 136)}); // cyan -> teal
        themes.add(new Color[]{new Color(150, 255, 150), new Color(50, 200, 50)}); // light green -> green
        themes.add(new Color[]{new Color(255, 102, 178), new Color(102, 204, 255)}); // pink -> light blue

        // ensure theme list size >= themeButtons size (if fewer, cycle)
        while (themes.size() < tabContents.get("Theme").size()) {
            themes.addAll(new ArrayList<>(themes));
        }
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
        // основное окно: тень + фон
        RenderUtil.drawShadow(x, y, width, height, 2f, new Color(0, 255, 0, 180));
        RenderUtil.drawRound(x, y, width, height, cornerRadius, Color.BLACK);

        // заголовок (без тени)
        RenderUtil.drawRound(x, y, width, headerHeight, cornerRadius, Color.BLACK);
        drawCenteredString(matrixStack, this.font, "Bim Client", x + width / 2, y + 8, 0xFFFFFFFF);

        // левая колонка вкладок
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

        // контент выбранной вкладки
        List<String> content = tabContents.getOrDefault(activeTab, Collections.emptyList());
        int contentX = tabX + tabW + 25;
        int contentY = y + headerHeight + 20;
        int btnW = 150;
        int btnH = 32;
        int colSpacing = 14;
        int rowSpacing = 12;

        for (int i = 0; i < content.size(); i++) {
            int col = i % 2;
            int row = i / 2;
            int bx = contentX + col * (btnW + colSpacing);
            int by = contentY + row * (btnH + rowSpacing);

            // если это вкладка Theme — рисуем градиентные кнопки
            if ("Theme".equals(activeTab)) {
                // берем пару цветов для этой темы
                Color[] pair = themes.get(i % themes.size());
                Color c1 = pair[0];
                Color c2 = pair[1];

                // усреднённый цвет для основного фона кнопки
                Color avg = averageColor(c1, c2);

                // фон + тень
                RenderUtil.drawShadow(bx, by, btnW, btnH, 2f, new Color(0, 255, 0, 140));
                RenderUtil.drawRound(bx, by, btnW, btnH, 6, avg);

                // имитация градиента — полоса сверху меньшей высоты с c2 (полупрозрачная)
                int topH = Math.max(6, btnH / 3);
                RenderUtil.drawRound(bx, by, btnW, topH, 6, new Color(c2.getRed(), c2.getGreen(), c2.getBlue(), 140));

                // рамка-выделение для выбранной темы
                if (selectedThemeIndex == i) {
                    // тонкая рамка светлее c2
                    RenderUtil.drawRound(bx - 2, by - 2, btnW + 4, btnH + 4, 8, new Color(c2.getRed(), c2.getGreen(), c2.getBlue(), 120));
                }

                // надпись (цвет подложки — белый/тёмный в зависимости от яркости)
                int textColor = getContrastColor(avg);
                this.font.draw(matrixStack, content.get(i),
                        bx + (btnW - this.font.width(content.get(i))) / 2f,
                        by + (btnH - this.font.lineHeight) / 2f,
                        textColor);

            } else {
                // обычные кнопки для остальных вкладок
                RenderUtil.drawShadow(bx, by, btnW, btnH, 2f, new Color(0, 255, 0, 140));
                RenderUtil.drawRound(bx, by, btnW, btnH, 6, new Color(50, 50, 50));
                this.font.draw(matrixStack, content.get(i),
                        bx + (btnW - this.font.width(content.get(i))) / 2f,
                        by + (btnH - this.font.lineHeight) / 2f,
                        0xFFFFFFFF);
            }
        }

        // рендер окна с настройками
        settingsWindow.render(matrixStack, mouseX, mouseY);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // перетаскивание окна
        if (button == 0 && isInHeader((int) mouseX, (int) mouseY)) {
            dragging = true;
            dragX = (int) mouseX - x;
            dragY = (int) mouseY - y;
            return true;
        }

        // клики по вкладкам слева
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

        // ПКМ по кнопкам (как раньше) — оставляем поведение
        if (button == 1) {
            List<String> content = tabContents.getOrDefault(activeTab, Collections.emptyList());
            int contentX = tabX + tabW + 25;
            int contentY = y + headerHeight + 20;
            int btnW = 150;
            int btnH = 32;
            int colSpacing = 14;
            int rowSpacing = 12;

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

        // ЛКМ по Theme-кнопке меняет выбранную тему
        if (button == 0 && "Theme".equals(activeTab)) {
            List<String> content = tabContents.getOrDefault(activeTab, Collections.emptyList());
            int contentX = tabX + tabW + 25;
            int contentY = y + headerHeight + 20;
            int btnW = 150;
            int btnH = 32;
            int colSpacing = 14;
            int rowSpacing = 12;

            for (int i = 0; i < content.size(); i++) {
                int col = i % 2;
                int row = i / 2;
                int bx = contentX + col * (btnW + colSpacing);
                int by = contentY + row * (btnH + rowSpacing);
                if (mouseX >= bx && mouseX <= bx + btnW && mouseY >= by && mouseY <= by + btnH) {
                    // выбираем тему
                    selectedThemeIndex = i;
                    return true;
                }
            }
        }

        // обработка окна настроек
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

    // ---- вспомогательные методы ----

    private static Color averageColor(Color a, Color b) {
        int r = (a.getRed() + b.getRed()) / 2;
        int g = (a.getGreen() + b.getGreen()) / 2;
        int bl = (a.getBlue() + b.getBlue()) / 2;
        return new Color(r, g, bl);
    }

    private static int getContrastColor(Color bg) {
        // простой яркостной контраст: если ярко — тёмный текст, иначе белый
        double luminance = (0.299 * bg.getRed() + 0.587 * bg.getGreen() + 0.114 * bg.getBlue()) / 255;
        return luminance > 0.6 ? 0x202020 : 0xFFFFFF;
    }
}
