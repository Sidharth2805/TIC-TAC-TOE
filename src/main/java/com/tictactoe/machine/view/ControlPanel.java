package com.tictactoe.machine.view;

import com.tictactoe.machine.engine.GameEngine;
import com.tictactoe.machine.model.*;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class ControlPanel extends VBox {
    private final GameEngine engine;

    // Player Name Inputs
    private TextField p1NameField;
    private TextField p2NameField;
    private TextField p3NameField;
    private Label p1BadgeLbl;
    private Label p2BadgeLbl;
    private Label p3BadgeLbl;
    private HBox p3Row;
    private boolean isUpdatingFields = false;

    // Toggle switch for heatmap
    private ToggleSwitch heatSwitch;
    private Label heatSwitchValLbl;

    // Labeled groups
    private final List<Label> sectionHeaders = new ArrayList<>();
    private final List<ToggleButton> optionButtons = new ArrayList<>();
    private final List<RadioButton> difficultyRadios = new ArrayList<>();

    // SYSTEM Buttons
    private ToggleButton soundBtn;
    private ToggleButton powerBtn;
    private final List<ToggleButton> themeToggleButtons = new ArrayList<>();

    private Label diffLbl;
    private Label heatTitle;
    private ToggleButton btn3x3;

    public ControlPanel(GameEngine engine) {
        this.engine = engine;
        this.setSpacing(14);
        this.setPadding(new Insets(12));

        // Section 1: SYSTEM
        VBox systemBox = createSection("SYSTEM");
        
        soundBtn = new ToggleButton("AUDIO: ON");
        soundBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        soundBtn.setPrefHeight(34);
        soundBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(soundBtn, Priority.ALWAYS);
        soundBtn.setOnAction(e -> {
            engine.toggleSound();
            soundBtn.setText(engine.getConfig().isSoundOn() ? "AUDIO: ON" : "AUDIO: OFF");
        });

        powerBtn = new ToggleButton("POWER: ON");
        powerBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        powerBtn.setPrefHeight(34);
        powerBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(powerBtn, Priority.ALWAYS);
        powerBtn.setOnAction(e -> {
            engine.togglePower();
            powerBtn.setText(engine.getConfig().isPower() ? "POWER: ON" : "POWER: OFF");
        });

        HBox sysGrid = new HBox(8, soundBtn, powerBtn);
        sysGrid.setAlignment(Pos.CENTER_LEFT);

        Label themeLbl = new Label("Theme Select");
        themeLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        sectionHeaders.add(themeLbl);
        
        // Theme Select - 2x2 grid of box buttons
        GridPane themeGrid = new GridPane();
        themeGrid.setHgap(6);
        themeGrid.setVgap(6);
        ColumnConstraints tc1 = new ColumnConstraints();
        tc1.setPercentWidth(50);
        ColumnConstraints tc2 = new ColumnConstraints();
        tc2.setPercentWidth(50);
        themeGrid.getColumnConstraints().addAll(tc1, tc2);

        ToggleGroup themeGroup = new ToggleGroup();
        Theme[] themes = Theme.values();
        for (int i = 0; i < themes.length; i++) {
            final Theme t = themes[i];
            ToggleButton tb = new ToggleButton(t.label);
            tb.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            tb.setToggleGroup(themeGroup);
            tb.setSelected(t == engine.getConfig().getTheme());
            tb.setPrefHeight(32);
            tb.setMaxWidth(Double.MAX_VALUE);
            tb.setAlignment(Pos.CENTER);
            tb.setOnAction(e -> {
                tb.setSelected(true);
                engine.getConfig().setTheme(t);
                engine.log("[DISPLAY]: Theme switched to " + t.label.toUpperCase() + ".", 'S');
                if (this.getScene() != null && this.getScene().getRoot() instanceof MainView) {
                    ((MainView) this.getScene().getRoot()).applyTheme(t);
                }
            });
            themeToggleButtons.add(tb);
            
            int r = i / 2;
            int c = i % 2;
            themeGrid.add(tb, c, r);
        }

        systemBox.getChildren().addAll(sysGrid, themeLbl, themeGrid);

        // Section 2: GAME MODE
        VBox gameModeBox = createSection("GAME MODE");
        String[] modes = new String[]{"vs Computer", "2 Players", "3 Players", "Computer vs Computer"};
        Pane modeFlow = buildFlowGroup(modes, sel -> {
            GameMode selectedMode = GameMode.values()[sel];
            engine.getConfig().setGameMode(selectedMode);
            engine.log("[CONFIG]: Opponent mode set to " + selectedMode.label + ".", 'S');
            engine.newMatch();
        }, engine.getConfig().getGameMode().ordinal(), 12);
        gameModeBox.getChildren().add(modeFlow);

        // Section: PLAYER NAMES
        VBox playersBox = createSection("PLAYER NAMES");

        p1BadgeLbl = new Label("P1 (X)");
        p1BadgeLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        p1BadgeLbl.setMinWidth(48);

        p1NameField = new TextField(engine.getConfig().getPlayer1Name());
        p1NameField.setPromptText("Player 1");
        p1NameField.setFont(Font.font("Segoe UI", 12));
        p1NameField.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(p1NameField, Priority.ALWAYS);
        p1NameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (isUpdatingFields) return;
            String trimmed = newVal == null ? "" : newVal.trim();
            if (!trimmed.isEmpty()) {
                engine.getConfig().setPlayer1Name(trimmed);
                engine.updatePlayerNames();
            }
        });

        HBox p1Row = new HBox(8, p1BadgeLbl, p1NameField);
        p1Row.setAlignment(Pos.CENTER_LEFT);

        p2BadgeLbl = new Label("P2 (O)");
        p2BadgeLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        p2BadgeLbl.setMinWidth(48);

        p2NameField = new TextField(engine.getConfig().getPlayer2Name());
        p2NameField.setPromptText("Player 2");
        p2NameField.setFont(Font.font("Segoe UI", 12));
        p2NameField.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(p2NameField, Priority.ALWAYS);
        p2NameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (isUpdatingFields) return;
            String trimmed = newVal == null ? "" : newVal.trim();
            if (!trimmed.isEmpty()) {
                engine.getConfig().setPlayer2Name(trimmed);
                engine.updatePlayerNames();
            }
        });

        HBox p2Row = new HBox(8, p2BadgeLbl, p2NameField);
        p2Row.setAlignment(Pos.CENTER_LEFT);

        p3BadgeLbl = new Label("P3 (Δ)");
        p3BadgeLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        p3BadgeLbl.setMinWidth(48);

        p3NameField = new TextField(engine.getConfig().getPlayer3Name());
        p3NameField.setPromptText("Player 3");
        p3NameField.setFont(Font.font("Segoe UI", 12));
        p3NameField.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(p3NameField, Priority.ALWAYS);
        p3NameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (isUpdatingFields) return;
            String trimmed = newVal == null ? "" : newVal.trim();
            if (!trimmed.isEmpty()) {
                engine.getConfig().setPlayer3Name(trimmed);
                engine.updatePlayerNames();
            }
        });

        p3Row = new HBox(8, p3BadgeLbl, p3NameField);
        p3Row.setAlignment(Pos.CENTER_LEFT);

        playersBox.getChildren().addAll(p1Row, p2Row, p3Row);

        // Section 3: COMPUTER
        VBox aiBox = createSection("COMPUTER");
        
        diffLbl = new Label("Difficulty");
        diffLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        sectionHeaders.add(diffLbl);

        VBox diffRadioList = new VBox(6);
        ToggleGroup diffGroup = new ToggleGroup();
        String[] diffLabels = new String[]{"Rookie", "Tactical", "Impossible", "Chaos"};
        for (int i = 0; i < diffLabels.length; i++) {
            final int idx = i;
            RadioButton rb = new RadioButton(diffLabels[i]);
            rb.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            rb.setToggleGroup(diffGroup);
            rb.setSelected(i == engine.getConfig().getDifficulty().ordinal());
            rb.setOnAction(e -> {
                Difficulty d = Difficulty.values()[idx];
                engine.getConfig().setDifficulty(d);
                engine.log("[COMPUTER]: Difficulty set to " + d.name() + ".", 'S');
            });
            difficultyRadios.add(rb);
            diffRadioList.getChildren().add(rb);
        }

        HBox heatBox = new HBox(12);
        heatBox.setAlignment(Pos.CENTER_LEFT);
        heatTitle = new Label("HeatMap Prediction");
        heatTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        sectionHeaders.add(heatTitle);

        heatSwitch = new ToggleSwitch();
        heatSwitch.setSwitchedOn(engine.getConfig().isHeatOn());
        heatSwitchValLbl = new Label(engine.getConfig().isHeatOn() ? "ON" : "OFF");
        heatSwitchValLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        sectionHeaders.add(heatSwitchValLbl);

        heatSwitch.setOnMouseClicked(e -> {
            engine.toggleHeatmap();
            boolean on = engine.getConfig().isHeatOn();
            heatSwitch.setSwitchedOn(on);
            heatSwitchValLbl.setText(on ? "ON" : "OFF");
            update(engine.getConfig().getTheme());
        });

        heatBox.getChildren().addAll(heatSwitch, heatSwitchValLbl);

        HBox aiCoreLayout = new HBox(24);
        aiCoreLayout.setAlignment(Pos.CENTER_LEFT);
        VBox diffVBox = new VBox(6, diffLbl, diffRadioList);
        VBox heatVBox = new VBox(8, heatTitle, heatBox);
        aiCoreLayout.getChildren().addAll(diffVBox, heatVBox);
        aiBox.getChildren().add(aiCoreLayout);

        // Section 4: BOARD SIZE
        VBox boardBox = createSection("BOARD SIZE");
        String[] sizes = new String[]{"3x3", "4x4", "5x5", "9x9"};
        int initIdx = 0;
        int currentSz = engine.getConfig().getGridSize();
        if (currentSz == 4) initIdx = 1;
        else if (currentSz == 5) initIdx = 2;
        else if (currentSz == 9) initIdx = 3;

        Pane boardFlow = buildFlowGroup(sizes, sel -> {
            int selectedSize = (sel == 0) ? 3 : ((sel == 1) ? 4 : ((sel == 2) ? 5 : 9));
            engine.getConfig().setGridSize(selectedSize);
            engine.log("[CONFIG]: Grid resized to " + selectedSize + "x" + selectedSize + ".", 'S');
            engine.newMatch();
        }, initIdx, 12);
        boardBox.getChildren().add(boardFlow);

        this.getChildren().addAll(systemBox, gameModeBox, playersBox, aiBox, boardBox);
    }

    private VBox createSection(String title) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(10, 12, 10, 12));
        
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#000000", 0.08));
        shadow.setRadius(6);
        shadow.setOffsetY(2);
        card.setEffect(shadow);

        Label header = new Label(title);
        header.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        sectionHeaders.add(header);

        card.getChildren().add(header);
        return card;
    }

    private Pane buildFlowGroup(String[] options, java.util.function.Consumer<Integer> onSelect, int initial, double fontSize) {
        FlowPane flow = new FlowPane();
        flow.setHgap(6);
        flow.setVgap(6);
        
        ToggleGroup group = new ToggleGroup();
        for (int i = 0; i < options.length; i++) {
            final int idx = i;
            ToggleButton b = new ToggleButton(options[i]);
            b.setFont(Font.font("Segoe UI", FontWeight.BOLD, fontSize));
            b.setToggleGroup(group);
            b.setSelected(i == initial);
            b.setPrefHeight(32);
            b.setMinWidth(72);
            b.setAlignment(Pos.CENTER);
            
            if (options[i].equals("3x3")) {
                btn3x3 = b;
            }

            // Direct single-click execution
            b.setOnAction(e -> {
                b.setSelected(true);
                onSelect.accept(idx);
                update(engine.getConfig().getTheme());
            });
            optionButtons.add(b);
            flow.getChildren().add(b);
        }
        return flow;
    }

    public void update(Theme theme) {
        GameConfig config = engine.getConfig();

        String panelStyle = ThemeManager.getPanelStyle(theme);
        for (javafx.scene.Node n : this.getChildren()) {
            if (n instanceof VBox) {
                n.setStyle(panelStyle);
            }
        }

        String lblDimStyle = ThemeManager.getLabelDimStyle(theme);
        for (Label h : sectionHeaders) {
            h.setStyle(lblDimStyle);
        }

        // Player Names State & Styling
        isUpdatingFields = true;
        try {
            GameMode mode = config.getGameMode();
            if (mode == GameMode.CPU) {
                p1NameField.setDisable(false);
                if (!p1NameField.isFocused()) p1NameField.setText(config.getPlayer1Name());
                p2BadgeLbl.setText("P2 (O)");
                p2NameField.setText("Computer");
                p2NameField.setDisable(true);
                p3Row.setVisible(false);
                p3Row.setManaged(false);
            } else if (mode == GameMode.TWO) {
                p1NameField.setDisable(false);
                if (!p1NameField.isFocused()) p1NameField.setText(config.getPlayer1Name());
                p2BadgeLbl.setText("P2 (O)");
                p2NameField.setDisable(false);
                if (!p2NameField.isFocused()) p2NameField.setText(config.getPlayer2Name());
                p3Row.setVisible(false);
                p3Row.setManaged(false);
            } else if (mode == GameMode.THREE) {
                p1NameField.setDisable(false);
                if (!p1NameField.isFocused()) p1NameField.setText(config.getPlayer1Name());
                p2BadgeLbl.setText("P2 (O)");
                p2NameField.setDisable(false);
                if (!p2NameField.isFocused()) p2NameField.setText(config.getPlayer2Name());
                p3BadgeLbl.setText("P3 (Δ)");
                p3Row.setVisible(true);
                p3Row.setManaged(true);
                p3NameField.setDisable(false);
                if (!p3NameField.isFocused()) p3NameField.setText(config.getPlayer3Name());
            } else if (mode == GameMode.AI_VS_AI) {
                p1NameField.setText("Computer 1");
                p1NameField.setDisable(true);
                p2BadgeLbl.setText("P2 (O)");
                p2NameField.setText("Computer 2");
                p2NameField.setDisable(true);
                p3Row.setVisible(false);
                p3Row.setManaged(false);
            }
        } finally {
            isUpdatingFields = false;
        }

        String inputStyle = "-fx-background-color: " + theme.termBg + ";" +
                            "-fx-text-fill: " + theme.text + ";" +
                            "-fx-prompt-text-fill: " + theme.textDim + ";" +
                            "-fx-border-color: " + theme.textDim + "44;" +
                            "-fx-border-radius: 4px;" +
                            "-fx-background-radius: 4px;" +
                            "-fx-padding: 5px 8px;";
        p1NameField.setStyle(inputStyle);
        p2NameField.setStyle(inputStyle);
        p3NameField.setStyle(inputStyle);

        p1BadgeLbl.setStyle("-fx-text-fill: " + theme.accentX + "; -fx-font-weight: bold; -fx-font-size: 11px;");
        p2BadgeLbl.setStyle("-fx-text-fill: " + theme.accentO + "; -fx-font-weight: bold; -fx-font-size: 11px;");
        p3BadgeLbl.setStyle("-fx-text-fill: " + theme.accentWin + "; -fx-font-weight: bold; -fx-font-size: 11px;");

        // Disable 3x3 option for 3 players
        boolean is3Players = (config.getGameMode() == GameMode.THREE);
        if (btn3x3 != null) {
            btn3x3.setDisable(is3Players);
        }
        if (is3Players && config.getGridSize() == 3) {
            config.setGridSize(4);
            engine.log("[CONFIG]: 3x3 Grid size disabled for 3 players. Auto-switched to 4x4.", 'S');
            Platform.runLater(engine::newMatch);
        }

        // AI Core Lockout
        boolean isAiActive = (config.getGameMode() == GameMode.CPU || config.getGameMode() == GameMode.AI_VS_AI);
        diffLbl.setDisable(!isAiActive);
        heatTitle.setDisable(!isAiActive);
        heatSwitch.setDisable(!isAiActive);
        heatSwitchValLbl.setDisable(!isAiActive);

        if (!isAiActive && config.isHeatOn()) {
            engine.toggleHeatmap();
        }

        for (RadioButton rb : difficultyRadios) {
            rb.setStyle(ThemeManager.getLabelStyle(theme) + " -fx-font-weight: bold; -fx-font-size: 12px;");
            rb.setDisable(!isAiActive);
            rb.setOpacity(isAiActive ? 1.0 : 0.4);
        }

        // Option Buttons Styling with crisp active & hover states
        for (ToggleButton b : optionButtons) {
            if (b.isSelected()) {
                b.setStyle("-fx-background-color: " + theme.accentX + ";" +
                           "-fx-text-fill: " + theme.bg + ";" +
                           "-fx-background-radius: 4px;" +
                           "-fx-border-radius: 4px;" +
                           "-fx-font-weight: bold;" +
                           "-fx-padding: 4px 8px;");
            } else {
                b.setStyle("-fx-background-color: transparent;" +
                           "-fx-border-color: " + theme.textDim + "33;" +
                           "-fx-border-radius: 4px;" +
                           "-fx-background-radius: 4px;" +
                           "-fx-text-fill: " + theme.text + ";" +
                           "-fx-padding: 4px 8px;");
            }
        }

        // Theme Toggle Buttons Styling
        for (ToggleButton b : themeToggleButtons) {
            if (b.isSelected()) {
                b.setStyle("-fx-background-color: " + theme.accentWin + ";" +
                           "-fx-text-fill: " + theme.termBg + ";" +
                           "-fx-background-radius: 4px;" +
                           "-fx-border-radius: 4px;" +
                           "-fx-font-weight: bold;");
            } else {
                b.setStyle("-fx-background-color: transparent;" +
                           "-fx-border-color: " + theme.textDim + "33;" +
                           "-fx-border-radius: 4px;" +
                           "-fx-background-radius: 4px;" +
                           "-fx-text-fill: " + theme.text + ";");
            }
        }

        String actionBtnStyle = "-fx-background-color: transparent; -fx-border-color: " + theme.textDim + "33; -fx-border-radius: 4px; -fx-background-radius: 4px; -fx-text-fill: " + theme.text + ";";
        soundBtn.setStyle(actionBtnStyle);
        powerBtn.setStyle(actionBtnStyle);
        
        soundBtn.setSelected(config.isSoundOn());
        soundBtn.setText(config.isSoundOn() ? "AUDIO: ON" : "AUDIO: OFF");
        powerBtn.setSelected(config.isPower());
        powerBtn.setText(config.isPower() ? "POWER: ON" : "POWER: OFF");

        heatSwitch.setSwitchedOn(config.isHeatOn());
        heatSwitchValLbl.setText(config.isHeatOn() ? "ON" : "OFF");
    }

    public static class ToggleSwitch extends HBox {
        private final Pane trigger = new Pane();
        private boolean switchedOn = false;

        public ToggleSwitch() {
            this.setSpacing(6);
            this.setAlignment(Pos.CENTER_LEFT);
            
            Pane track = new Pane();
            track.setPrefSize(38, 20);
            track.setMinSize(38, 20);
            track.setMaxSize(38, 20);
            track.setStyle("-fx-background-color: #71717A; -fx-background-radius: 3px;");

            trigger.setPrefSize(16, 16);
            trigger.setMinSize(16, 16);
            trigger.setMaxSize(16, 16);
            trigger.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 2px;");
            trigger.setTranslateX(2);
            trigger.setTranslateY(2);

            track.getChildren().add(trigger);
            this.getChildren().add(track);
        }

        public boolean isSwitchedOn() { return switchedOn; }

        public void setSwitchedOn(boolean on) {
            this.switchedOn = on;
            TranslateTransition tt = new TranslateTransition(Duration.millis(100), trigger);
            if (on) {
                tt.setToX(20);
                this.getChildren().get(0).setStyle("-fx-background-color: #10B981; -fx-background-radius: 3px;");
            } else {
                tt.setToX(2);
                this.getChildren().get(0).setStyle("-fx-background-color: #71717A; -fx-background-radius: 3px;");
            }
            tt.play();
        }
    }
}
