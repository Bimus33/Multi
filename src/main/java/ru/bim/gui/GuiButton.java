package ru.bim.gui;

import java.util.ArrayList;
import java.util.List;

public class GuiButton {
    private String id;
    private String text;
    private int x, y;
    private int width, height;
    private boolean visible;
    private Runnable onClickAction;

    // Статический список всех созданных кнопок
    private static List<GuiButton> allButtons = new ArrayList<>();

    // Конструкторы
    public GuiButton() {
        this("default_id", "Button", 0, 0, 100, 30);
    }

    public GuiButton(String id, String text, int x, int y, int width, int height) {
        this.id = id;
        this.text = text;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.visible = true;
        allButtons.add(this);
    }

    // Методы управления кнопкой
    public void setText(String text) {
        this.text = text;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setOnClickAction(Runnable action) {
        this.onClickAction = action;
    }

    public void click() {
        if (onClickAction != null && visible) {
            onClickAction.run();
        }
    }

    // Методы отрисовки (заглушки для реальной реализации)
    public void render() {
        if (visible) {
            System.out.println("Rendering button '" + text + "' at (" + x + ", " + y + ")");
            // Здесь будет реальная логика отрисовки
        }
    }

    // Геттеры
    public String getId() { return id; }
    public String getText() { return text; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public boolean isVisible() { return visible; }

    // Статические методы для управления всеми кнопками
    public static void addButton(GuiButton button) {
        if (button != null && !allButtons.contains(button)) {
            allButtons.add(button);
        }
    }

    public static void removeButton(String id) {
        allButtons.removeIf(button -> button.getId().equals(id));
    }

    public static GuiButton getButton(String id) {
        return allButtons.stream()
                .filter(button -> button.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public static List<GuiButton> getAllButtons() {
        return new ArrayList<>(allButtons);
    }

    public static void renderAllButtons() {
        for (GuiButton button : allButtons) {
            button.render();
        }
    }

    public static void clearAllButtons() {
        allButtons.clear();
    }

    // Утилитарный метод для создания кнопки по команде
    public static GuiButton createButton(String command) {
        // Пример команды: "add_button id=myBtn text=ClickMe x=10 y=20 width=80 height=30"
        try {
            String[] parts = command.split(" ");
            String id = "button_" + System.currentTimeMillis();
            String text = "Button";
            int x = 0, y = 0, width = 100, height = 30;

            for (String part : parts) {
                if (part.startsWith("id=")) {
                    id = part.substring(3);
                } else if (part.startsWith("text=")) {
                    text = part.substring(5);
                } else if (part.startsWith("x=")) {
                    x = Integer.parseInt(part.substring(2));
                } else if (part.startsWith("y=")) {
                    y = Integer.parseInt(part.substring(2));
                } else if (part.startsWith("width=")) {
                    width = Integer.parseInt(part.substring(6));
                } else if (part.startsWith("height=")) {
                    height = Integer.parseInt(part.substring(7));
                }
            }

            return new GuiButton(id, text, x, y, width, height);

        } catch (Exception e) {
            System.err.println("Error creating button from command: " + e.getMessage());
            return new GuiButton(); // Возвращаем кнопку по умолчанию
        }
    }
}