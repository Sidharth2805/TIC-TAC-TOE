package com.tictactoe.machine.view;

import com.tictactoe.machine.ai.HeuristicEvaluator;
import com.tictactoe.machine.engine.GameEngine;
import com.tictactoe.machine.model.*;
import javafx.geometry.Pos;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;

import java.util.HashMap;
import java.util.Map;

public class BoardView extends GridPane {
    private final GameEngine engine;
    private CellView[] cells;

    public BoardView(GameEngine engine) {
        this.engine = engine;
        this.setAlignment(Pos.CENTER);
        rebuild();
    }

    public void rebuild() {
        this.getChildren().clear();
        this.getColumnConstraints().clear();
        this.getRowConstraints().clear();

        int size = engine.getConfig().getGridSize();
        int gap = size == 3 ? 10 : (size == 4 ? 8 : (size == 5 ? 6 : 4));
        this.setHgap(gap);
        this.setVgap(gap);
        
        cells = new CellView[size * size];

        // Apply dynamic sizing constraints
        for (int i = 0; i < size; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(100.0 / size);
            col.setHgrow(Priority.ALWAYS);
            this.getColumnConstraints().add(col);

            RowConstraints row = new RowConstraints();
            row.setPercentHeight(100.0 / size);
            row.setVgrow(Priority.ALWAYS);
            this.getRowConstraints().add(row);
        }

        for (int i = 0; i < size * size; i++) {
            final int cellIdx = i;
            CellView cell = new CellView(cellIdx, engine);
            cell.setOnAction(e -> engine.makeMove(cellIdx));
            cells[i] = cell;
            
            int r = i / size;
            int c = i % size;
            this.add(cell, c, r);
        }

        updateTheme(engine.getConfig().getTheme());
    }

    public void updateTheme(Theme theme) {
        this.setStyle("-fx-background-color: " + theme.termBg + ";" +
                      "-fx-background-radius: 8px;" +
                      "-fx-border-color: " + theme.textDim + "26;" +
                      "-fx-border-radius: 8px;" +
                      "-fx-border-width: 1px;" +
                      "-fx-padding: 10px;");
    }

    public void refresh(WinResult win) {
        GameConfig config = engine.getConfig();
        GameState state = engine.getState();
        if (state.getBoard() == null) {
            return;
        }
        int size = config.getGridSize();
        int winLen = config.getWinLength();

        if (cells == null || cells.length != size * size) {
            rebuild();
        }

        updateTheme(config.getTheme());

        // Calculate heatmap values
        Map<Integer, Double> heatScores = new HashMap<>();
        double maxScore = Double.NEGATIVE_INFINITY;
        double minScore = Double.POSITIVE_INFINITY;

        if (config.isHeatOn() && !state.isGameOver() && config.isPower()) {
            Player active = state.getCurrentPlayer();
            if (active != null) {
                PlayerSymbol aiSym = active.getSymbol();
                PlayerSymbol otherSym = PlayerSymbol.EMPTY;
                for (Player p : state.getTurnOrder()) {
                    if (p.getSymbol() != aiSym) {
                        otherSym = p.getSymbol();
                        break;
                    }
                }
                if (otherSym != PlayerSymbol.EMPTY) {
                    for (int i = 0; i < state.getBoard().length; i++) {
                        if (state.getBoard()[i] == PlayerSymbol.EMPTY) {
                            PlayerSymbol[] copy = state.getBoard().clone();
                            copy[i] = aiSym;
                            double score = HeuristicEvaluator.evaluate(copy, size, winLen, aiSym, otherSym);
                            heatScores.put(i, score);
                            
                            if (score > maxScore) maxScore = score;
                            if (score < minScore) minScore = score;
                        }
                    }
                }
            }
        }

        // Refresh cell properties
        for (int i = 0; i < cells.length; i++) {
            PlayerSymbol symbol = state.getBoard()[i];
            boolean isWinCell = false;
            if (win != null) {
                for (int c : win.getCells()) {
                    if (c == i) {
                        isWinCell = true;
                        break;
                    }
                }
            }
            
            if (heatScores.containsKey(i)) {
                double score = heatScores.get(i);
                double prob = 1.0 / (1.0 + Math.exp(-score / 150.0));
                
                boolean isBest = (score == maxScore);
                boolean isWorst = (score == minScore) && (minScore != maxScore);
                
                cells[i].update(symbol, config.getTheme(), config.isPower(), state.isGameOver(), isWinCell, prob, isBest, isWorst);
            } else {
                cells[i].update(symbol, config.getTheme(), config.isPower(), state.isGameOver(), isWinCell, null, false, false);
            }
        }
    }
}
