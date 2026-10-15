package com.tictactoe.machine.view;

import com.tictactoe.machine.engine.GameEngine;
import com.tictactoe.machine.model.Player;
import com.tictactoe.machine.model.PlayerSymbol;
import com.tictactoe.machine.model.Theme;
import javafx.animation.ScaleTransition;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

public class CellView extends Button {
    private final int index;
    private final GameEngine engine;
    private boolean isWinCell = false;
    private boolean isHeatHighlighted = false;
    private PlayerSymbol currentSymbol = PlayerSymbol.EMPTY;
    private Theme currentTheme;
    private boolean isCellActive = false;

    public CellView(int index, GameEngine engine) {
        this.index = index;
        this.engine = engine;
        
        // Flexible resizing grid cell boundaries
        this.setMinSize(30, 30);
        this.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        
        // Responsive font sizing observer
        this.widthProperty().addListener((obs, oldVal, newVal) -> updateFontSize());
        this.heightProperty().addListener((obs, oldVal, newVal) -> updateFontSize());

        // Interactive ghost piece hover preview
        this.setOnMouseEntered(e -> {
            if (isCellActive && currentTheme != null) {
                if (currentSymbol == PlayerSymbol.EMPTY && !isHeatHighlighted) {
                    Player p = engine.getState().getCurrentPlayer();
                    if (p != null) {
                        this.setText(p.getSymbol().glyph);
                    }
                }
                applyStyle(true);
            }
        });
        this.setOnMouseExited(e -> {
            if (currentTheme != null) {
                if (currentSymbol == PlayerSymbol.EMPTY && !isHeatHighlighted) {
                    this.setText("");
                }
                applyStyle(false);
            }
        });
    }

    public int getIndex() { return index; }

    private void updateFontSize() {
        double w = this.getWidth();
        double h = this.getHeight();
        double size = Math.min(w, h);
        if (size <= 0) size = 110;
        
        if (isHeatHighlighted && currentSymbol == PlayerSymbol.EMPTY) {
            this.setFont(Font.font("Segoe UI", FontWeight.BOLD, size * 0.18));
        } else {
            this.setFont(Font.font("Segoe UI", FontWeight.BOLD, size * 0.48));
        }
    }

    public void update(PlayerSymbol symbol, Theme theme, boolean power, boolean gameOver, boolean isWin, 
                       Double heatProb, Boolean isBest, Boolean isWorst) {
        this.currentSymbol = symbol;
        this.currentTheme = theme;
        boolean wasWin = this.isWinCell;
        this.isWinCell = isWin;
        this.isHeatHighlighted = (heatProb != null);

        if (symbol == PlayerSymbol.H) {
            this.isCellActive = false;
            this.setDisable(true);
            this.setOpacity(0.90);
            this.setText(symbol.glyph);
            Tooltip.install(this, new Tooltip("HAZARD: Blocked wall cell (Cannot be played)"));
        } else {
            this.isCellActive = (symbol == PlayerSymbol.EMPTY) && power && !gameOver;
            this.setDisable(!isCellActive && symbol == PlayerSymbol.EMPTY && !gameOver);
            this.setOpacity(isCellActive ? 1.0 : (symbol == PlayerSymbol.EMPTY ? 0.40 : 1.0));

            if (isHeatHighlighted && isCellActive) {
                int pct = (int) Math.round(heatProb * 100);
                this.setText(pct + "%");
                Tooltip.install(this, new Tooltip("Evaluated Probability: " + pct + "%"));
            } else {
                this.setText(symbol.glyph);
                Tooltip.install(this, null);
            }
        }

        applyStyle(this.isHover() && isCellActive);
        updateFontSize();

        if (isWin && !wasWin) {
            pulseWin();
        }
    }

    private void pulseWin() {
        ScaleTransition st = new ScaleTransition(Duration.millis(250), this);
        st.setFromX(1.0);
        st.setFromY(1.0);
        st.setToX(1.05);
        st.setToY(1.05);
        st.setAutoReverse(true);
        st.setCycleCount(4);
        st.play();
    }

    private void applyStyle(boolean hoverState) {
        if (currentTheme == null) return;

        StringBuilder sb = new StringBuilder();
        // Crisp 6px modern rounded-square corners
        sb.append("-fx-background-radius: 6px;");
        sb.append("-fx-border-radius: 6px;");

        // Background color
        if (currentSymbol == PlayerSymbol.H) {
            sb.append("-fx-background-color: #EF444422;"); // Red translucent hazard block
        } else if (isWinCell) {
            sb.append("-fx-background-color: ").append(currentTheme.accentWin).append("33;");
        } else if (isHeatHighlighted && isCellActive) {
            sb.append("-fx-background-color: ").append(currentTheme.panel).append(";");
        } else if (hoverState) {
            sb.append("-fx-background-color: ").append(currentTheme.panel).append(";");
        } else {
            sb.append("-fx-background-color: ").append(currentTheme.bg).append(";");
        }

        // Text color & Ghost Preview Opacity
        String textFill;
        if (currentSymbol == PlayerSymbol.H) {
            textFill = "#EF4444";
        } else if (hoverState && currentSymbol == PlayerSymbol.EMPTY) {
            Player p = engine.getState().getCurrentPlayer();
            if (p != null && p.getSymbol() == PlayerSymbol.O) {
                textFill = currentTheme.accentO + "55";
            } else if (p != null && p.getSymbol() == PlayerSymbol.T) {
                textFill = currentTheme.accentWin + "55";
            } else {
                textFill = currentTheme.accentX + "55";
            }
        } else if (currentSymbol == PlayerSymbol.X) {
            textFill = currentTheme.accentX;
        } else if (currentSymbol == PlayerSymbol.O) {
            textFill = currentTheme.accentO;
        } else if (currentSymbol == PlayerSymbol.T) {
            textFill = currentTheme.accentWin;
        } else {
            textFill = currentTheme.text;
        }
        sb.append("-fx-text-fill: ").append(textFill).append(";");

        // Border styling
        String borderColor;
        double borderWidth;
        if (currentSymbol == PlayerSymbol.H) {
            borderColor = "#EF444488";
            borderWidth = 2.0;
        } else if (isWinCell) {
            borderColor = currentTheme.accentWin;
            borderWidth = 2.5;
        } else if (hoverState) {
            Player p = engine.getState().getCurrentPlayer();
            if (p != null && p.getSymbol() == PlayerSymbol.O) {
                borderColor = currentTheme.accentO;
            } else if (p != null && p.getSymbol() == PlayerSymbol.T) {
                borderColor = currentTheme.accentWin;
            } else {
                borderColor = currentTheme.accentX;
            }
            borderWidth = 2.0;
        } else if (isHeatHighlighted && isCellActive) {
            borderColor = currentTheme.accentWin;
            borderWidth = 1.5;
        } else {
            borderColor = currentTheme.textDim + "33";
            borderWidth = 1.5;
        }

        sb.append("-fx-border-color: ").append(borderColor).append(";");
        sb.append("-fx-border-width: ").append(borderWidth).append("px;");

        this.setStyle(sb.toString());
        this.setEffect(null);
    }
}
