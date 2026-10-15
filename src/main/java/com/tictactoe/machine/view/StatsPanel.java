package com.tictactoe.machine.view;

import com.tictactoe.machine.engine.GameEngine;
import com.tictactoe.machine.model.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class StatsPanel extends HBox {
    private final GameEngine engine;

    private Label p1NameLbl, p1WinsLbl, p1Sub;
    private Label p2NameLbl, p2WinsLbl, p2Sub;
    private Label drawsLbl, drawsTitle, drawsSub;

    private VBox card1, card2, card3;

    public StatsPanel(GameEngine engine) {
        this.engine = engine;
        this.setSpacing(10);
        this.setAlignment(Pos.CENTER);
        
        // Card 1: Player
        card1 = createStatCard();
        p1NameLbl = new Label("PLAYER 1");
        p1NameLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        p1WinsLbl = new Label("00");
        p1WinsLbl.setFont(Font.font("Consolas", FontWeight.BOLD, 26));
        p1Sub = new Label("WINS");
        p1Sub.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 10));
        card1.getChildren().addAll(p1NameLbl, p1WinsLbl, p1Sub);

        // Card 2: Computer
        card2 = createStatCard();
        p2NameLbl = new Label("COMPUTER");
        p2NameLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        p2WinsLbl = new Label("00");
        p2WinsLbl.setFont(Font.font("Consolas", FontWeight.BOLD, 26));
        p2Sub = new Label("WINS");
        p2Sub.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 10));
        card2.getChildren().addAll(p2NameLbl, p2WinsLbl, p2Sub);

        // Card 3: Draws
        card3 = createStatCard();
        drawsTitle = new Label("DRAWS");
        drawsTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        drawsLbl = new Label("00");
        drawsLbl.setFont(Font.font("Consolas", FontWeight.BOLD, 26));
        drawsSub = new Label("MATCHES");
        drawsSub.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 10));
        card3.getChildren().addAll(drawsTitle, drawsLbl, drawsSub);

        this.getChildren().addAll(card1, card2, card3);
        HBox.setHgrow(card1, Priority.ALWAYS);
        HBox.setHgrow(card2, Priority.ALWAYS);
        HBox.setHgrow(card3, Priority.ALWAYS);
    }

    private VBox createStatCard() {
        VBox card = new VBox(4);
        card.setPadding(new Insets(10, 8, 10, 8));
        card.setAlignment(Pos.CENTER);
        
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#000000", 0.08));
        shadow.setRadius(6);
        shadow.setOffsetY(2);
        card.setEffect(shadow);

        return card;
    }

    public void update(Theme theme) {
        GameState state = engine.getState();
        GameConfig config = engine.getConfig();

        String panelStyle = ThemeManager.getPanelStyle(theme);
        card1.setStyle(panelStyle);
        card2.setStyle(panelStyle);
        card3.setStyle(panelStyle);

        String lblDimStyle = ThemeManager.getLabelDimStyle(theme);

        p1NameLbl.setStyle(lblDimStyle);
        p1Sub.setStyle(lblDimStyle);

        p2NameLbl.setStyle(lblDimStyle);
        p2Sub.setStyle(lblDimStyle);

        drawsTitle.setStyle(lblDimStyle);
        drawsSub.setStyle(lblDimStyle);

        p1WinsLbl.setStyle("-fx-text-fill: " + theme.accentX + "; -fx-font-family: 'Consolas'; -fx-font-weight: bold; -fx-font-size: 26px;");
        p2WinsLbl.setStyle("-fx-text-fill: " + theme.accentO + "; -fx-font-family: 'Consolas'; -fx-font-weight: bold; -fx-font-size: 26px;");
        drawsLbl.setStyle("-fx-text-fill: " + theme.text + "; -fx-font-family: 'Consolas'; -fx-font-weight: bold; -fx-font-size: 26px;");

        // Player names (customizable) and Computer
        if (config.getGameMode() == GameMode.AI_VS_AI) {
            p1NameLbl.setText("COMPUTER 1");
            p2NameLbl.setText("COMPUTER 2");
        } else if (config.getGameMode() == GameMode.CPU) {
            p1NameLbl.setText(config.getPlayer1Name().toUpperCase());
            p2NameLbl.setText("COMPUTER");
        } else {
            p1NameLbl.setText(config.getPlayer1Name().toUpperCase());
            p2NameLbl.setText(config.getPlayer2Name().toUpperCase());
        }
        p1WinsLbl.setText(String.format("%02d", state.getP1Wins()));
        p2WinsLbl.setText(String.format("%02d", state.getP2Wins()));
        drawsLbl.setText(String.format("%02d", state.getDraws()));

        int matchesCount = state.getMatches();
        drawsSub.setText(matchesCount + " TOTAL");
    }
}
