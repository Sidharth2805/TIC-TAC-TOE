package com.tictactoe.machine.view;

import com.tictactoe.machine.engine.GameEngine;
import com.tictactoe.machine.model.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class TerminalPanel extends VBox {
    private final GameEngine engine;
    private final TextArea logArea;
    private final Label terminalTitle;
    private final Label nodesLbl;
    private final Label calcLbl;
    private final HBox header;

    public TerminalPanel(GameEngine engine) {
        this.engine = engine;
        this.setSpacing(6);
        this.setPadding(new Insets(10));
        
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#000000", 0.08));
        shadow.setRadius(10);
        shadow.setOffsetY(3);
        this.setEffect(shadow);

        header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        
        terminalTitle = new Label(">_ AI BRAIN TERMINAL LOG");
        terminalTitle.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
        
        nodesLbl = new Label("NODES: --");
        nodesLbl.setFont(Font.font("Consolas", FontWeight.NORMAL, 11));
        
        calcLbl = new Label("CALC: --");
        calcLbl.setFont(Font.font("Consolas", FontWeight.NORMAL, 11));
        
        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        HBox statsBox = new HBox(12, nodesLbl, calcLbl);
        statsBox.setAlignment(Pos.CENTER_RIGHT);
        
        header.getChildren().addAll(terminalTitle, spacer, statsBox);

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefRowCount(7);
        logArea.setMinHeight(120);
        logArea.setMaxHeight(120);
        
        // Console default launch logs
        logArea.appendText("✔ AI Core Loaded\n");
        logArea.appendText("✔ Minimax Strategy Active\n");
        logArea.appendText("✔ Alpha-Beta Pruning Ready\n");
        logArea.appendText("✔ Depth Constraints Enabled\n");

        this.getChildren().addAll(header, logArea);

        engine.getTerminalLog().addListener(logLine -> {
            Platform.runLater(() -> {
                logArea.appendText(logLine + "\n");
                logArea.selectPositionCaret(logArea.getLength());
                logArea.deselect();
            });
        });
    }

    public void update(Theme theme) {
        GameState state = engine.getState();

        this.setStyle(ThemeManager.getPanelStyle(theme));
        
        terminalTitle.setStyle("-fx-text-fill: " + theme.text + ";");
        nodesLbl.setStyle("-fx-text-fill: " + theme.textDim + ";");
        calcLbl.setStyle("-fx-text-fill: " + theme.textDim + ";");
        
        logArea.setStyle("-fx-control-inner-background: #000000;" +
                          "-fx-text-fill: #10B981;" +
                          "-fx-font-family: 'Consolas', 'Courier New', monospace;" +
                          "-fx-font-size: 11px;" +
                          "-fx-background-radius: 8px;" +
                          "-fx-border-radius: 8px;" +
                          "-fx-border-color: " + theme.textDim + "22;");

        nodesLbl.setText("NODES: " + (state.getNodeCount() > 0 ? state.getNodeCount() : "--"));
        calcLbl.setText("CALC: " + (state.getLastCalcMs() >= 0 ? String.format("%.1fms", state.getLastCalcMs()) : "--"));
    }
}
