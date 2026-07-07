package com.j3ly.spawnman.gui;

import com.j3ly.duckylib.gui.*;
import com.j3ly.duckylib.gui.theme.DuckTheme;
import com.j3ly.spawnman.model.SpawnSet;
import com.j3ly.spawnman.storage.SpawnStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.stream.Collectors;

public class SpawnManScreen extends TabbedScreen {
    private static final int CONTENT_Y = HEADER_HEIGHT + TAB_HEIGHT + PADDING;

    private final SpawnStorage storage;
    private ScrollableList spawnList;
    private DuckTextField idField;
    private DuckTextField teamField;
    private DuckTextField editTeamField;
    private Notification notification;
    private int selectedIndex = -1;

    public SpawnManScreen(SpawnStorage storage, Screen parent) {
        super(Component.literal("Spawn Manager"), parent);
        this.storage = storage;
        initTabs();
    }

    private void initTabs() {
        addTab(Component.literal("Spawns"), (graphics, mx, my) -> renderSpawnsTab(graphics, mx, my));
        addTab(Component.literal("Create"), (graphics, mx, my) -> renderCreateTab(graphics, mx, my));
        addTab(Component.literal("Edit"), (graphics, mx, my) -> renderEditTab(graphics, mx, my));
    }

    @Override
    protected void init() {
        super.init();

        notification = new Notification(theme);

        spawnList = new ScrollableList(theme);
        spawnList.setBounds(PADDING, CONTENT_Y, width / 2 - 15, height - CONTENT_Y - PADDING);
        spawnList.setOnSelect(this::onSpawnSelected);

        idField = new DuckTextField(theme, Component.literal("Spawn ID"));
        idField.setBounds(PADDING + 10, CONTENT_Y + 20, 150, 20);

        teamField = new DuckTextField(theme, Component.literal("Team (optional)"));
        teamField.setBounds(PADDING + 10, CONTENT_Y + 60, 150, 20);

        editTeamField = new DuckTextField(theme, Component.literal("Team"));
        editTeamField.setBounds(width / 2 + PADDING, CONTENT_Y + 20, 150, 20);
    }

    private void refreshSpawnList() {
        spawnList.clear();
        List<SpawnSet> sets = storage.getAllSpawnSets().stream()
            .sorted((a, b) -> a.getId().compareToIgnoreCase(b.getId()))
            .collect(Collectors.toList());
        for (SpawnSet set : sets) {
            String type = set.isArea() ? "Area" : "Points";
            String teamInfo = set.hasTeam() ? " [Team: " + set.getTeam() + "]" : "";
            spawnList.addEntry(
                Component.literal(set.getId()),
                Component.literal(type + teamInfo + " | World: " + (set.getWorld() != null ? set.getWorld() : "any"))
            );
        }
        if (selectedIndex >= spawnList.getEntryCount()) {
            selectedIndex = -1;
        }
    }

    private void onSpawnSelected(int index) {
        selectedIndex = index;
    }

    private void renderSpawnsTab(GuiGraphics graphics, int mouseX, int mouseY) {
        if (spawnList == null) return;
        refreshSpawnList();
        spawnList.render(graphics, mouseX, mouseY);

        if (selectedIndex >= 0) {
            List<SpawnSet> sets = storage.getAllSpawnSets().stream()
                .sorted((a, b) -> a.getId().compareToIgnoreCase(b.getId()))
                .collect(Collectors.toList());
            if (selectedIndex < sets.size()) {
                SpawnSet set = sets.get(selectedIndex);
                int infoX = width / 2 + PADDING;
                int infoY = CONTENT_Y;
                if (font != null) {
                    graphics.drawString(font, Component.literal("§e" + set.getId()), infoX, infoY, theme.getTextPrimary());
                    infoY += 15;
                    graphics.drawString(font, Component.literal("World: " + (set.getWorld() != null ? set.getWorld() : "any")), infoX, infoY, theme.getTextSecondary());
                    infoY += 12;
                    graphics.drawString(font, Component.literal("Type: " + (set.isArea() ? "Area" : "Point (" + set.getPoints().size() + " spawns)")), infoX, infoY, theme.getTextSecondary());
                    infoY += 12;
                    graphics.drawString(font, Component.literal("Team: " + (set.hasTeam() ? set.getTeam() : "(none)")), infoX, infoY, theme.getTextSecondary());
                }
            }
        }
    }

    private void renderCreateTab(GuiGraphics graphics, int mouseX, int mouseY) {
        if (font != null) {
            graphics.drawString(font, Component.literal("§6Create a new spawn set"), PADDING + 10, CONTENT_Y + 5, theme.getTextPrimary());
            graphics.drawString(font, Component.literal("Use /smpos1 and /smpos2 to mark positions first"), PADDING + 10, CONTENT_Y + 90, theme.getTextSecondary());
            graphics.drawString(font, Component.literal("Then use /smspawn set area <id> [team] in-game"), PADDING + 10, CONTENT_Y + 105, theme.getTextSecondary());
        }
        idField.render(graphics, mouseX, mouseY);
        teamField.render(graphics, mouseX, mouseY);

        int btnX = PADDING + 10;
        int btnY = CONTENT_Y + 100;
        boolean hovered = mouseX >= btnX && mouseX <= btnX + 80 && mouseY >= btnY && mouseY <= btnY + 20;
        int bg = hovered ? theme.getAccent() : theme.getPrimary();
        DuckScreen.fillStatic(graphics, btnX, btnY, btnX + 80, btnY + 20, bg);
        graphics.renderOutline(btnX, btnY, 80, 20, theme.getBorder());
        if (font != null) {
            graphics.drawCenteredString(font, Component.literal("Create"), btnX + 40, btnY + 4, theme.getTextPrimary());
        }

        if (notification != null) {
            notification.render(graphics, width, height);
        }
    }

    private void renderEditTab(GuiGraphics graphics, int mouseX, int mouseY) {
        if (font != null) {
            graphics.drawString(font, Component.literal("§6Edit selected spawn set"), PADDING + 10, CONTENT_Y + 5, theme.getTextPrimary());
            graphics.drawString(font, Component.literal("Select a spawn from the Spawns tab first"), PADDING + 10, CONTENT_Y + 50, theme.getTextSecondary());
        }

        if (selectedIndex >= 0) {
            List<SpawnSet> sets = storage.getAllSpawnSets().stream()
                .sorted((a, b) -> a.getId().compareToIgnoreCase(b.getId()))
                .collect(Collectors.toList());
            if (selectedIndex < sets.size()) {
                SpawnSet set = sets.get(selectedIndex);
                if (font != null) {
                    graphics.drawString(font, Component.literal("Editing: §e" + set.getId()), PADDING + 10, CONTENT_Y + 70, theme.getTextPrimary());
                }
                editTeamField.render(graphics, mouseX, mouseY);

                int btnX = width / 2 + PADDING;
                int btnY = CONTENT_Y + 60;
                boolean hovered = mouseX >= btnX && mouseX <= btnX + 80 && mouseY >= btnY && mouseY <= btnY + 20;
                int bg = hovered ? theme.getAccent() : theme.getPrimary();
                DuckScreen.fillStatic(graphics, btnX, btnY, btnX + 80, btnY + 20, bg);
                graphics.renderOutline(btnX, btnY, 80, 20, theme.getBorder());
                if (font != null) {
                    graphics.drawCenteredString(font, Component.literal("Save"), btnX + 40, btnY + 4, theme.getTextPrimary());
                }
            }
        }

        if (notification != null) {
            notification.render(graphics, width, height);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (activeTab == 0) {
            if (spawnList.mouseClicked(mouseX, mouseY, button)) return true;
        }
        if (activeTab == 1) {
            if (idField.mouseClicked(mouseX, mouseY, button)) return true;
            if (teamField.mouseClicked(mouseX, mouseY, button)) return true;

            int btnX = PADDING + 10;
            int btnY = CONTENT_Y + 100;
            if (mouseX >= btnX && mouseX <= btnX + 80 && mouseY >= btnY && mouseY <= btnY + 20) {
                String id = idField.getText().trim();
                if (id.isEmpty()) {
                    if (notification != null) {
                        notification.show(Component.literal("§cSpawn ID is required"), Notification.Type.ERROR);
                    }
                    return true;
                }
                notification.show(Component.literal("§aUse /smspawn set area " + id + " " + teamField.getText().trim() + " in-game"), Notification.Type.SUCCESS);
                return true;
            }
        }
        if (activeTab == 2) {
            if (editTeamField.mouseClicked(mouseX, mouseY, button)) return true;

            int btnX = width / 2 + PADDING;
            int btnY = CONTENT_Y + 60;
            if (mouseX >= btnX && mouseX <= btnX + 80 && mouseY >= btnY && mouseY <= btnY + 20 && selectedIndex >= 0) {
                List<SpawnSet> sets = storage.getAllSpawnSets().stream()
                    .sorted((a, b) -> a.getId().compareToIgnoreCase(b.getId()))
                    .collect(Collectors.toList());
                if (selectedIndex < sets.size()) {
                    String team = editTeamField.getText().trim();
                    sets.get(selectedIndex).setTeam(team.isEmpty() ? null : team);
                    storage.save();
                    if (notification != null) {
                        notification.show(Component.literal("§aTeam updated"), Notification.Type.SUCCESS);
                    }
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (activeTab == 1) {
            if (idField.keyPressed(keyCode, scanCode, modifiers)) return true;
            if (teamField.keyPressed(keyCode, scanCode, modifiers)) return true;
        }
        if (activeTab == 2) {
            if (editTeamField.keyPressed(keyCode, scanCode, modifiers)) return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (activeTab == 1) {
            if (idField.charTyped(codePoint, modifiers)) return true;
            if (teamField.charTyped(codePoint, modifiers)) return true;
        }
        if (activeTab == 2) {
            if (editTeamField.charTyped(codePoint, modifiers)) return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (activeTab == 0) {
            if (spawnList.mouseScrolled(mouseX, mouseY, delta)) return true;
        }
        return false;
    }
}