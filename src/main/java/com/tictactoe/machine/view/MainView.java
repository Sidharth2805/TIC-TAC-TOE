package com.tictactoe.machine.view;

import com.tictactoe.machine.engine.GameEngine;
import com.tictactoe.machine.engine.WinChecker;
import com.tictactoe.machine.model.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class MainView extends VBox {
    private final GameEngine engine;
    private final Stage stage;

    // Custom Window Title Bar
    private HBox titleBar;
    private Label windowTitleLbl;
    private Circle powerDot;
    private Button minWindowBtn;
    private Button maxWindowBtn;
    private Button closeWindowBtn;

    // Adaptive Orientation & Layout Containers
    private enum OrientationMode {
        LANDSCAPE,
        PORTRAIT_WIDE,
        PORTRAIT_COMPACT
    }
    private OrientationMode currentMode = null;

    private BorderPane contentContainer;
    private HBox portraitTwoColHBox;
    private VBox compactDrawer;
    private ToggleButton tabSettingsBtn;
    private ToggleButton tabAnalyticsBtn;
    private StackPane compactTabContent;
    
    // Left Column (Settings Drawer)
    private ScrollPane leftScrollPane;
    private final ControlPanel controlPanel;

    // Center Column (Hero Board Arena)
    private VBox centerPane;
    private VBox turnIndicatorCard;
    private StackPane turnBadge;
    private Label turnBadgeSymbol;
    private Label turnHeaderLbl;
    private Label turnValueLbl;
    private ProgressBar turnProgressBar;
    private VBox boardCard;
    private final BoardView boardView;

    // Right Column (Analytics Drawer)
    private ScrollPane rightScrollPane;
    private VBox rightContainer;
    private final StatsPanel statsPanel;

    // ACTION CONSOLE
    private VBox actionCard;
    private Label actionTitle;
    private Button newGameBtn;
    private Button redoBtn;
    private Button undoBtn;
    private Button replayBtn;

    // Move History Box
    private VBox historyCard;
    private VBox historyListVBox;
    private ScrollPane historyScrollPane;

    // GAME RULES
    private VBox rulesCard;
    private Label rulesTitle;
    private Label rulesDescLbl;
    private final List<ToggleButton> ruleButtons = new ArrayList<>();

    // Bottom Status Bar
    private HBox statusBar;
    private Label statusReadyLbl;
    private Label statusEngineLbl;
    private Label statusMovesLbl;
    private Label statusDepthLbl;
    private Label statusMemLbl;
    private Label statusTimeLbl;

    // Drag coordinates
    private double xOffset = 0;
    private double yOffset = 0;

    public MainView(GameEngine engine, Stage stage) {
        this.engine = engine;
        this.stage = stage;

        // 1. Build Window Title Bar (38px)
        buildTitleBar();
        this.getChildren().add(titleBar);

        // 2. Setup Adaptive Layout Containers
        contentContainer = new BorderPane();
        VBox.setVgrow(contentContainer, Priority.ALWAYS);
        this.getChildren().add(contentContainer);

        portraitTwoColHBox = new HBox(12);
        portraitTwoColHBox.setPadding(new Insets(6, 12, 10, 12));

        compactDrawer = new VBox(8);
        compactDrawer.setPadding(new Insets(4, 12, 10, 12));

        HBox tabHeader = new HBox(8);
        tabHeader.setAlignment(Pos.CENTER);

        tabSettingsBtn = new ToggleButton("GAME SETTINGS");
        tabAnalyticsBtn = new ToggleButton("ACTIONS & STATS");
        ToggleGroup tabGroup = new ToggleGroup();
        tabSettingsBtn.setToggleGroup(tabGroup);
        tabAnalyticsBtn.setToggleGroup(tabGroup);
        tabSettingsBtn.setSelected(true);

        tabSettingsBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        tabAnalyticsBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        tabSettingsBtn.setPrefHeight(34);
        tabAnalyticsBtn.setPrefHeight(34);
        HBox.setHgrow(tabSettingsBtn, Priority.ALWAYS);
        HBox.setHgrow(tabAnalyticsBtn, Priority.ALWAYS);
        tabSettingsBtn.setMaxWidth(Double.MAX_VALUE);
        tabAnalyticsBtn.setMaxWidth(Double.MAX_VALUE);

        compactTabContent = new StackPane();
        VBox.setVgrow(compactTabContent, Priority.ALWAYS);

        tabSettingsBtn.setOnAction(e -> {
            tabSettingsBtn.setSelected(true);
            detachFromParent(rightScrollPane);
            compactTabContent.getChildren().setAll(leftScrollPane);
            applyTheme(engine.getConfig().getTheme());
        });
        tabAnalyticsBtn.setOnAction(e -> {
            tabAnalyticsBtn.setSelected(true);
            detachFromParent(leftScrollPane);
            compactTabContent.getChildren().setAll(rightScrollPane);
            applyTheme(engine.getConfig().getTheme());
        });

        tabHeader.getChildren().addAll(tabSettingsBtn, tabAnalyticsBtn);
        compactDrawer.getChildren().addAll(tabHeader, compactTabContent);

        // --- Left Sidebar (Dynamic Width: 25%, min 280, max 440) ---
        leftScrollPane = new ScrollPane();
        leftScrollPane.setFitToWidth(true);
        leftScrollPane.setFitToHeight(true);
        leftScrollPane.prefWidthProperty().bind(this.widthProperty().multiply(0.25));
        leftScrollPane.setMinWidth(280);
        leftScrollPane.setMaxWidth(440);
        leftScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        leftScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        controlPanel = new ControlPanel(engine);
        leftScrollPane.setContent(controlPanel);

        // --- Center Panel (Hero Board Arena) ---
        centerPane = new VBox(14);
        centerPane.setAlignment(Pos.CENTER);
        centerPane.setPadding(new Insets(14));
        HBox.setHgrow(centerPane, Priority.ALWAYS);

        // Turn indicator Card (Tournament badge design)
        turnIndicatorCard = new VBox(8);
        turnIndicatorCard.setPadding(new Insets(10, 18, 10, 18));
        turnIndicatorCard.setAlignment(Pos.CENTER);
        turnIndicatorCard.setMinWidth(240);
        turnIndicatorCard.setMaxWidth(400);
        turnIndicatorCard.prefWidthProperty().bind(
            javafx.beans.binding.Bindings.min(400, centerPane.widthProperty().subtract(36))
        );
        
        DropShadow cardShadow = new DropShadow();
        cardShadow.setColor(Color.web("#000000", 0.12));
        cardShadow.setRadius(8);
        cardShadow.setOffsetY(3);
        turnIndicatorCard.setEffect(cardShadow);

        turnBadge = new StackPane();
        turnBadge.setPrefSize(34, 34);
        turnBadge.setMinSize(34, 34);
        turnBadge.setMaxSize(34, 34);
        turnBadgeSymbol = new Label("X");
        turnBadgeSymbol.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        turnBadge.getChildren().add(turnBadgeSymbol);

        turnHeaderLbl = new Label("CURRENT MATCH TURN");
        turnHeaderLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        
        turnValueLbl = new Label("PLAYER 01");
        turnValueLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));

        VBox turnInfoVBox = new VBox(2, turnHeaderLbl, turnValueLbl);
        turnInfoVBox.setAlignment(Pos.CENTER_LEFT);

        HBox turnBadgeRow = new HBox(12, turnBadge, turnInfoVBox);
        turnBadgeRow.setAlignment(Pos.CENTER);

        turnProgressBar = new ProgressBar();
        turnProgressBar.setPrefWidth(Double.MAX_VALUE);
        turnProgressBar.setPrefHeight(4);
        turnProgressBar.setVisible(false);

        turnIndicatorCard.getChildren().addAll(turnBadgeRow, turnProgressBar);

        // Board Card
        boardCard = new VBox(14);
        boardCard.setPadding(new Insets(14));
        boardCard.setAlignment(Pos.CENTER);
        boardCard.setEffect(cardShadow);
        
        boardView = new BoardView(engine);
        boardCard.getChildren().add(boardView);

        centerPane.getChildren().addAll(turnIndicatorCard, boardCard);

        // Dynamic Board Centering and Square Resizing Calculation
        javafx.beans.binding.DoubleBinding boardSize = javafx.beans.binding.Bindings.createDoubleBinding(
            () -> {
                double w = centerPane.getWidth();
                double h = centerPane.getHeight() - turnIndicatorCard.getHeight() - 40;
                double sizeVal = Math.min(w - 28, h) - 10;
                return Math.max(sizeVal, 160);
            },
            centerPane.widthProperty(),
            centerPane.heightProperty(),
            turnIndicatorCard.heightProperty()
        );
        
        boardView.prefWidthProperty().bind(boardSize);
        boardView.prefHeightProperty().bind(boardSize);
        boardView.setMinWidth(160);
        boardView.setMinHeight(160);

        // --- Right Sidebar (Dynamic Width: 25%, min 280, max 450) ---
        rightScrollPane = new ScrollPane();
        rightScrollPane.setFitToWidth(true);
        rightScrollPane.setFitToHeight(true);
        rightScrollPane.prefWidthProperty().bind(this.widthProperty().multiply(0.25));
        rightScrollPane.setMinWidth(280);
        rightScrollPane.setMaxWidth(450);
        rightScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        rightScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        rightContainer = new VBox(12);
        rightContainer.setPadding(new Insets(12));
        rightScrollPane.setContent(rightContainer);

        // 1. ACTION CONSOLE
        actionCard = new VBox(8);
        actionCard.setPadding(new Insets(12));
        actionCard.setEffect(cardShadow);

        actionTitle = new Label("ACTION CONSOLE");
        actionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        
        newGameBtn = new Button("Start New Match");
        newGameBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        newGameBtn.setPrefHeight(36);
        newGameBtn.setMaxWidth(Double.MAX_VALUE);
        newGameBtn.setOnAction(e -> engine.newMatch());

        redoBtn = new Button("Redo");
        redoBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        redoBtn.setPrefHeight(32);
        redoBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(redoBtn, Priority.ALWAYS);
        redoBtn.setOnAction(e -> engine.redoMove());

        undoBtn = new Button("Undo");
        undoBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        undoBtn.setPrefHeight(32);
        undoBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(undoBtn, Priority.ALWAYS);
        undoBtn.setOnAction(e -> engine.undoMove());

        replayBtn = new Button("Replay");
        replayBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        replayBtn.setPrefHeight(32);
        replayBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(replayBtn, Priority.ALWAYS);
        replayBtn.setOnAction(e -> engine.replayLastGame());

        HBox actGrid = new HBox(6, redoBtn, undoBtn, replayBtn);
        actGrid.setAlignment(Pos.CENTER_LEFT);

        VBox actVBox = new VBox(8, newGameBtn, actGrid);
        actionCard.getChildren().addAll(actionTitle, actVBox);
        rightContainer.getChildren().add(actionCard);

        // 2. STATS
        statsPanel = new StatsPanel(engine);
        rightContainer.getChildren().add(statsPanel);

        // 3. GAME RULES
        rulesCard = new VBox(8);
        rulesCard.setPadding(new Insets(12));
        rulesCard.setEffect(cardShadow);

        rulesTitle = new Label("GAME RULES");
        rulesTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

        FlowPane rulesFlow = new FlowPane();
        rulesFlow.setHgap(6);
        rulesFlow.setVgap(6);

        rulesDescLbl = new Label();
        rulesDescLbl.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        rulesDescLbl.setWrapText(true);

        String[] ruleNames = new String[]{"Classic", "Vanish", "Blitz", "Hazards"};
        ToggleGroup rulesGroup = new ToggleGroup();
        for (int i = 0; i < ruleNames.length; i++) {
            final int idx = i;
            ToggleButton b = new ToggleButton(ruleNames[i]);
            b.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            b.setToggleGroup(rulesGroup);
            b.setSelected(i == engine.getConfig().getGameRule().ordinal());
            b.setPrefHeight(32);
            b.setMinWidth(72);
            b.setAlignment(Pos.CENTER);

            b.setOnAction(e -> {
                b.setSelected(true);
                GameRule selectedRule = GameRule.values()[idx];
                engine.getConfig().setGameRule(selectedRule);
                updateRuleButtons(engine.getConfig().getTheme());
                engine.log("[CONFIG]: House rule set to " + selectedRule.name() + ".", 'S');
                engine.newMatch();
            });
            ruleButtons.add(b);
            rulesFlow.getChildren().add(b);
        }
        rulesCard.getChildren().addAll(rulesTitle, rulesFlow, rulesDescLbl);
        rightContainer.getChildren().add(rulesCard);

        // 4. MOVE HISTORY (Clean scrollable timeline)
        historyCard = new VBox(6);
        historyCard.setPadding(new Insets(12));
        historyCard.setPrefHeight(220);
        historyCard.setMinHeight(160);
        VBox.setVgrow(historyCard, Priority.ALWAYS);
        historyCard.setEffect(cardShadow);

        Label histTitle = new Label("MOVE HISTORY");
        histTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        
        historyScrollPane = new ScrollPane();
        historyScrollPane.setFitToWidth(true);
        historyScrollPane.setFitToHeight(true);
        historyScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");

        historyListVBox = new VBox(4);
        historyScrollPane.setContent(historyListVBox);
        historyCard.getChildren().addAll(histTitle, historyScrollPane);
        rightContainer.getChildren().add(historyCard);

        // Initial layout application
        applyOrientation(OrientationMode.LANDSCAPE);
        currentMode = OrientationMode.LANDSCAPE;

        // Responsive orientation & resize listener (Landscape vs Portrait Wide vs Portrait Compact)
        javafx.beans.value.ChangeListener<Number> sizeListener = (obs, oldVal, newVal) -> {
            double w = this.getWidth();
            double h = this.getHeight();
            if (w <= 0 || h <= 0) return;

            OrientationMode mode;
            if (w >= 940 && w >= h) {
                mode = OrientationMode.LANDSCAPE;
            } else if (w >= 720) {
                mode = OrientationMode.PORTRAIT_WIDE;
            } else {
                mode = OrientationMode.PORTRAIT_COMPACT;
            }

            if (mode != currentMode) {
                currentMode = mode;
                applyOrientation(mode);
                applyTheme(engine.getConfig().getTheme());
            }

            // Responsive status bar items
            boolean showExtraMetrics = w >= 720;
            statusDepthLbl.setVisible(showExtraMetrics);
            statusDepthLbl.setManaged(showExtraMetrics);
            statusMemLbl.setVisible(showExtraMetrics);
            statusMemLbl.setManaged(showExtraMetrics);
            statusTimeLbl.setVisible(showExtraMetrics);
            statusTimeLbl.setManaged(showExtraMetrics);

            // Responsive title bar text
            if (w < 560) {
                windowTitleLbl.setText("TICTACTOE");
            } else {
                windowTitleLbl.setText("TIC-TAC-TOE MACHINE");
            }
        };

        this.widthProperty().addListener(sizeListener);
        this.heightProperty().addListener(sizeListener);

        // 5. Build Bottom Status Bar
        buildStatusBar();
        this.getChildren().add(statusBar);

        // 6. Register Keyboard Shortcuts Filter
        setupKeyboardShortcuts();

        // 7. Register Engine Listener
        engine.addListener(new GameEventListener() {
            @Override
            public void onBoardChanged() {
                WinResult win = WinChecker.checkWin(engine.getState().getBoard(), engine.getConfig().getGridSize(), engine.getConfig().getWinLength());
                boardView.refresh(win);
                updateMoveHistory();
                updateLiveStatusMetrics();
            }

            @Override
            public void onTurnChanged(String activePlayerLabel, boolean isThinking) {
                updateLiveStatusMetrics();
                Theme theme = engine.getConfig().getTheme();
                if (engine.getState().isGameOver()) {
                    // Overridden by onGameOver
                } else if (!engine.getConfig().isPower()) {
                    turnValueLbl.setText("POWER LOCKED");
                    turnBadgeSymbol.setText("-");
                    turnProgressBar.setVisible(false);
                } else {
                    Player p = engine.getState().getCurrentPlayer();
                    PlayerSymbol sym = p != null ? p.getSymbol() : PlayerSymbol.X;
                    turnBadgeSymbol.setText(sym.glyph);
                    
                    String symColor = (sym == PlayerSymbol.X) ? theme.accentX : ((sym == PlayerSymbol.O) ? theme.accentO : theme.accentWin);
                    turnBadgeSymbol.setStyle("-fx-text-fill: " + symColor + ";");
                    turnBadge.setStyle("-fx-background-color: " + symColor + "22; -fx-border-color: " + symColor + "; -fx-border-radius: 4px; -fx-background-radius: 4px; -fx-border-width: 1.5px;");
                    
                    if (isThinking) {
                        turnValueLbl.setText("CALCULATING...");
                        turnProgressBar.setVisible(true);
                        turnProgressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
                    } else if (engine.getConfig().getGameRule() == GameRule.BLITZ) {
                        turnValueLbl.setText(activePlayerLabel.toUpperCase());
                        turnProgressBar.setVisible(true);
                    } else {
                        turnValueLbl.setText(activePlayerLabel.toUpperCase());
                        turnProgressBar.setVisible(false);
                    }
                    turnValueLbl.setStyle("-fx-text-fill: " + symColor + "; -fx-font-weight: bold;");
                }
            }

            @Override
            public void onGameOver(WinResult winResult) {
                updateLiveStatusMetrics();
                Theme theme = engine.getConfig().getTheme();
                turnProgressBar.setVisible(false);
                if (winResult != null) {
                    PlayerSymbol sym = winResult.getSymbol();
                    String symColor = (sym == PlayerSymbol.X) ? theme.accentX : ((sym == PlayerSymbol.O) ? theme.accentO : theme.accentWin);
                    turnBadgeSymbol.setText(sym.glyph);
                    turnBadgeSymbol.setStyle("-fx-text-fill: " + symColor + ";");
                    turnBadge.setStyle("-fx-background-color: " + symColor + "33; -fx-border-color: " + symColor + "; -fx-border-radius: 4px; -fx-background-radius: 4px; -fx-border-width: 2px;");
                    turnValueLbl.setText("WINNER: " + engine.getPlayerLabel(winResult.getSymbol()).toUpperCase());
                    turnValueLbl.setStyle("-fx-text-fill: " + symColor + "; -fx-font-weight: bold;");
                } else {
                    turnBadgeSymbol.setText("=");
                    turnBadgeSymbol.setStyle("-fx-text-fill: " + theme.textDim + ";");
                    turnBadge.setStyle("-fx-background-color: " + theme.panel + "; -fx-border-color: " + theme.textDim + "44; -fx-border-radius: 4px; -fx-background-radius: 4px; -fx-border-width: 1px;");
                    turnValueLbl.setText("DRAW: GRID FULL");
                    turnValueLbl.setStyle("-fx-text-fill: " + theme.text + "; -fx-font-weight: bold;");
                }
            }

            @Override
            public void onStatsUpdated() {
                statsPanel.update(engine.getConfig().getTheme());
                controlPanel.update(engine.getConfig().getTheme());
                updateRuleButtons(engine.getConfig().getTheme());
                updateLiveStatusMetrics();
            }

            @Override
            public void onBlitzTick(int secondsRemaining) {
                Platform.runLater(() -> {
                    if (secondsRemaining > 0) {
                        turnValueLbl.setText("BLITZ: " + secondsRemaining + "s");
                        turnProgressBar.setVisible(true);
                        turnProgressBar.setProgress(secondsRemaining / 10.0);
                    } else {
                        turnProgressBar.setVisible(false);
                    }
                });
            }
        });

        // 8. Dynamic font scaling listener based on scene width
        this.widthProperty().addListener((obs, oldVal, newVal) -> {
            double w = newVal.doubleValue();
            double headingSize = w > 1400 ? 18 : (w > 1200 ? 16 : 14);
            turnValueLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, headingSize));
        });

        applyTheme(engine.getConfig().getTheme());
    }

    private void detachFromParent(javafx.scene.Node node) {
        if (node != null && node.getParent() instanceof Pane p) {
            p.getChildren().remove(node);
        }
    }

    private void applyOrientation(OrientationMode targetMode) {
        detachFromParent(leftScrollPane);
        detachFromParent(centerPane);
        detachFromParent(rightScrollPane);

        if (targetMode == OrientationMode.LANDSCAPE) {
            contentContainer.setTop(null);
            contentContainer.setBottom(null);

            leftScrollPane.prefWidthProperty().bind(contentContainer.widthProperty().multiply(0.25));
            leftScrollPane.setMinWidth(280);
            leftScrollPane.setMaxWidth(440);
            leftScrollPane.prefHeightProperty().unbind();

            rightScrollPane.prefWidthProperty().bind(contentContainer.widthProperty().multiply(0.25));
            rightScrollPane.setMinWidth(280);
            rightScrollPane.setMaxWidth(440);
            rightScrollPane.prefHeightProperty().unbind();

            centerPane.prefHeightProperty().unbind();
            centerPane.setMinHeight(0);

            contentContainer.setLeft(leftScrollPane);
            contentContainer.setCenter(centerPane);
            contentContainer.setRight(rightScrollPane);

        } else if (targetMode == OrientationMode.PORTRAIT_WIDE) {
            contentContainer.setLeft(null);
            contentContainer.setRight(null);
            contentContainer.setBottom(null);

            centerPane.prefHeightProperty().bind(contentContainer.heightProperty().multiply(0.50));
            centerPane.setMinHeight(280);
            contentContainer.setTop(centerPane);

            leftScrollPane.prefWidthProperty().unbind();
            rightScrollPane.prefWidthProperty().unbind();
            leftScrollPane.setMinWidth(0);
            leftScrollPane.setMaxWidth(Double.MAX_VALUE);
            rightScrollPane.setMinWidth(0);
            rightScrollPane.setMaxWidth(Double.MAX_VALUE);
            leftScrollPane.prefHeightProperty().unbind();
            rightScrollPane.prefHeightProperty().unbind();

            HBox.setHgrow(leftScrollPane, Priority.ALWAYS);
            HBox.setHgrow(rightScrollPane, Priority.ALWAYS);

            portraitTwoColHBox.getChildren().setAll(leftScrollPane, rightScrollPane);
            contentContainer.setCenter(portraitTwoColHBox);

        } else { // PORTRAIT_COMPACT
            contentContainer.setLeft(null);
            contentContainer.setRight(null);
            contentContainer.setBottom(null);

            centerPane.prefHeightProperty().bind(contentContainer.heightProperty().multiply(0.50));
            centerPane.setMinHeight(260);
            contentContainer.setTop(centerPane);

            leftScrollPane.prefWidthProperty().unbind();
            rightScrollPane.prefWidthProperty().unbind();
            leftScrollPane.setMinWidth(0);
            leftScrollPane.setMaxWidth(Double.MAX_VALUE);
            rightScrollPane.setMinWidth(0);
            rightScrollPane.setMaxWidth(Double.MAX_VALUE);
            leftScrollPane.prefHeightProperty().unbind();
            rightScrollPane.prefHeightProperty().unbind();

            if (tabSettingsBtn.isSelected()) {
                compactTabContent.getChildren().setAll(leftScrollPane);
            } else {
                compactTabContent.getChildren().setAll(rightScrollPane);
            }
            contentContainer.setCenter(compactDrawer);
        }
    }

    private void buildTitleBar() {
        titleBar = new HBox(12);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(0, 0, 0, 16));
        titleBar.setMinHeight(38);
        titleBar.setMaxHeight(38);
        titleBar.setPrefHeight(38);

        titleBar.setOnMousePressed(e -> {
            if (e.getSceneY() > 8 && e.getSceneX() > 8 && e.getSceneX() < stage.getWidth() - 140) {
                xOffset = e.getSceneX();
                yOffset = e.getSceneY();
            } else {
                xOffset = -1;
            }
        });
        titleBar.setOnMouseDragged(e -> {
            if (xOffset != -1 && !stage.isFullScreen() && !stage.isMaximized()) {
                stage.setX(Math.max(e.getScreenX() - xOffset, 0));
                stage.setY(Math.max(e.getScreenY() - yOffset, 0));
            }
        });
        titleBar.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && e.getSceneX() < stage.getWidth() - 140) {
                stage.setMaximized(!stage.isMaximized());
            }
        });

        powerDot = new Circle(4, Color.web("#10B981"));

        windowTitleLbl = new Label("TIC-TAC-TOE MACHINE");
        windowTitleLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Standard Windows Title Buttons (Minus, Maximize, Close)
        minWindowBtn = new Button("-");
        maxWindowBtn = new Button("□");
        closeWindowBtn = new Button("✕");

        List<Button> buttons = List.of(minWindowBtn, maxWindowBtn, closeWindowBtn);
        for (Button btn : buttons) {
            btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            btn.setPrefSize(46, 38);
            btn.setMinSize(46, 38);
            btn.setMaxSize(46, 38);
            btn.setFocusTraversable(false);
        }

        minWindowBtn.setOnAction(e -> stage.setIconified(true));
        maxWindowBtn.setOnAction(e -> stage.setMaximized(!stage.isMaximized()));
        closeWindowBtn.setOnAction(e -> {
            Platform.exit();
            System.exit(0);
        });

        stage.maximizedProperty().addListener((obs, oldVal, newVal) -> {
            maxWindowBtn.setText(newVal ? "☐" : "□");
        });

        HBox controls = new HBox(0, minWindowBtn, maxWindowBtn, closeWindowBtn);
        controls.setAlignment(Pos.CENTER_RIGHT);

        titleBar.getChildren().addAll(powerDot, windowTitleLbl, spacer, controls);
    }

    private void buildStatusBar() {
        statusBar = new HBox(16);
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.setPadding(new Insets(4, 16, 4, 16));
        statusBar.setMinHeight(24);
        statusBar.setMaxHeight(24);
        statusBar.setPrefHeight(24);

        statusReadyLbl = new Label("READY");
        statusEngineLbl = new Label("SYSTEM: ACTIVE");

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        statusMovesLbl = new Label("MOVES: 0");
        statusDepthLbl = new Label("DEPTH: 8");
        statusMemLbl = new Label("MEM: -- MB");
        statusTimeLbl = new Label("CALC: --");

        List<Label> labels = List.of(statusReadyLbl, statusEngineLbl, statusMovesLbl, statusDepthLbl, statusMemLbl, statusTimeLbl);
        for (Label l : labels) {
            l.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        }

        statusBar.getChildren().addAll(statusReadyLbl, statusEngineLbl, spacer, statusMovesLbl, statusDepthLbl, statusMemLbl, statusTimeLbl);
    }

    private void setupKeyboardShortcuts() {
        this.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            KeyCode code = e.getCode();
            boolean isCtrl = e.isControlDown() || e.isMetaDown();
            
            if (isCtrl && code == KeyCode.Z) {
                engine.undoMove();
                e.consume();
            } else if ((isCtrl && code == KeyCode.Y) || (isCtrl && e.isShiftDown() && code == KeyCode.Z)) {
                engine.redoMove();
                e.consume();
            } else if ((isCtrl && code == KeyCode.N) || code == KeyCode.F2) {
                engine.newMatch();
                e.consume();
            } else if (code == KeyCode.M && !isCtrl) {
                engine.toggleSound();
                controlPanel.update(engine.getConfig().getTheme());
                e.consume();
            } else if (code == KeyCode.P && !isCtrl) {
                engine.togglePower();
                controlPanel.update(engine.getConfig().getTheme());
                e.consume();
            } else if (code == KeyCode.R && !isCtrl) {
                engine.replayLastGame();
                e.consume();
            } else if (engine.getConfig().getGridSize() == 3) {
                int cell = -1;
                switch (code) {
                    case NUMPAD7: case DIGIT1: cell = 0; break;
                    case NUMPAD8: case DIGIT2: cell = 1; break;
                    case NUMPAD9: case DIGIT3: cell = 2; break;
                    case NUMPAD4: case DIGIT4: cell = 3; break;
                    case NUMPAD5: case DIGIT5: cell = 4; break;
                    case NUMPAD6: case DIGIT6: cell = 5; break;
                    case NUMPAD1: case DIGIT7: cell = 6; break;
                    case NUMPAD2: case DIGIT8: cell = 7; break;
                    case NUMPAD3: case DIGIT9: cell = 8; break;
                    default: break;
                }
                if (cell != -1) {
                    engine.makeMove(cell);
                    e.consume();
                }
            }
        });
    }

    private void updateMoveHistory() {
        Platform.runLater(() -> {
            historyListVBox.getChildren().clear();
            List<Move> moves = engine.getState().getLastGameMoves();
            Theme theme = engine.getConfig().getTheme();
            String lblStyle = ThemeManager.getLabelStyle(theme) + " -fx-font-size: 11px; -fx-font-family: 'Consolas', monospace;";
            
            for (int i = 0; i < moves.size(); i++) {
                Move m = moves.get(i);
                Label stepLbl = new Label(String.format("[%02d] %s -> cell %02d", 
                        (i + 1), engine.getPlayerLabel(m.getSymbol()), m.getIndex()));
                stepLbl.setStyle(lblStyle);
                historyListVBox.getChildren().add(stepLbl);
            }
        });
    }

    private void updateLiveStatusMetrics() {
        Platform.runLater(() -> {
            GameState state = engine.getState();
            GameConfig config = engine.getConfig();
            
            long freeMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            double memMb = freeMem / (1024.0 * 1024.0);
            statusMemLbl.setText(String.format("MEM: %.1f MB", memMb));

            statusDepthLbl.setText("DEPTH: " + (config.getGridSize() == 3 ? "9" : (config.getGridSize() == 4 ? "3" : "2")));
            statusTimeLbl.setText("CALC: " + (state.getLastCalcMs() >= 0 ? String.format("%.1fms", state.getLastCalcMs()) : "--"));
            statusMovesLbl.setText("MOVES: " + state.getLastGameMoves().size());
        });
    }

    public void applyTheme(Theme theme) {
        String rootStyle = ThemeManager.getRootStyle(theme);
        this.setStyle(rootStyle);

        // Clear backgrounds
        leftScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        rightScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        leftScrollPane.getContent().setStyle("-fx-background-color: transparent;");
        rightScrollPane.getContent().setStyle("-fx-background-color: transparent;");
        
        centerPane.setStyle("-fx-background-color: transparent;");
        rightContainer.setStyle("-fx-background-color: transparent;");

        contentContainer.setStyle("-fx-background-color: transparent;");
        portraitTwoColHBox.setStyle("-fx-background-color: transparent;");
        compactDrawer.setStyle("-fx-background-color: transparent;");

        // Compact Tab Buttons styling
        String tabActiveStyle = "-fx-background-color: " + theme.accentX + "; -fx-text-fill: " + theme.bg + "; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-font-weight: bold;";
        String tabInactiveStyle = "-fx-background-color: transparent; -fx-border-color: " + theme.textDim + "33; -fx-border-radius: 4px; -fx-background-radius: 4px; -fx-text-fill: " + theme.text + ";";
        tabSettingsBtn.setStyle(tabSettingsBtn.isSelected() ? tabActiveStyle : tabInactiveStyle);
        tabAnalyticsBtn.setStyle(tabAnalyticsBtn.isSelected() ? tabActiveStyle : tabInactiveStyle);

        // Window Title Bar (Crisp contrast)
        titleBar.setStyle("-fx-background-color: " + theme.termBg + ";" +
                "-fx-border-color: " + theme.textDim + "26;" +
                "-fx-border-width: 0 0 1px 0;");
        windowTitleLbl.setStyle("-fx-text-fill: " + theme.text + ";");

        // Window Title Control Buttons
        String winBtnStyle = "-fx-background-color: transparent; -fx-background-radius: 0; -fx-text-fill: " + theme.text + ";";
        minWindowBtn.setStyle(winBtnStyle);
        maxWindowBtn.setStyle(winBtnStyle);
        closeWindowBtn.setStyle(winBtnStyle);

        minWindowBtn.setOnMouseEntered(e -> minWindowBtn.setStyle("-fx-background-color: #334155; -fx-text-fill: #FFFFFF; -fx-background-radius: 0;"));
        minWindowBtn.setOnMouseExited(e -> minWindowBtn.setStyle(winBtnStyle));
        maxWindowBtn.setOnMouseEntered(e -> maxWindowBtn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: #FFFFFF; -fx-background-radius: 0;"));
        maxWindowBtn.setOnMouseExited(e -> maxWindowBtn.setStyle(winBtnStyle));
        closeWindowBtn.setOnMouseEntered(e -> closeWindowBtn.setStyle("-fx-background-color: #E11D48; -fx-text-fill: #FFFFFF; -fx-background-radius: 0;"));
        closeWindowBtn.setOnMouseExited(e -> closeWindowBtn.setStyle(winBtnStyle));

        // Sub Card styling
        String panelStyle = ThemeManager.getPanelStyle(theme);
        turnIndicatorCard.setStyle(panelStyle);
        boardCard.setStyle(panelStyle + "; -fx-padding: 16px;");
        actionCard.setStyle(panelStyle);
        historyCard.setStyle(panelStyle);
        rulesCard.setStyle(panelStyle);

        turnHeaderLbl.setStyle(ThemeManager.getLabelDimStyle(theme));
        turnValueLbl.setStyle(ThemeManager.getLabelStyle(theme));

        Player p = engine.getState().getCurrentPlayer();
        PlayerSymbol curSym = p != null ? p.getSymbol() : PlayerSymbol.X;
        String curSymCol = (curSym == PlayerSymbol.X) ? theme.accentX : ((curSym == PlayerSymbol.O) ? theme.accentO : theme.accentWin);
        turnBadgeSymbol.setStyle("-fx-text-fill: " + curSymCol + ";");
        turnBadge.setStyle("-fx-background-color: " + curSymCol + "22; -fx-border-color: " + curSymCol + "; -fx-border-radius: 4px; -fx-background-radius: 4px; -fx-border-width: 1.5px;");

        actionTitle.setStyle(ThemeManager.getLabelDimStyle(theme));
        historyCard.getChildren().get(0).setStyle(ThemeManager.getLabelDimStyle(theme));
        rulesTitle.setStyle(ThemeManager.getLabelDimStyle(theme));

        // Crisp 4px desktop buttons
        newGameBtn.setStyle("-fx-background-color: " + theme.accentWin + "; -fx-text-fill: " + theme.termBg + "; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-font-weight: bold;");
        
        String actionBtnStyle = "-fx-background-color: transparent; -fx-border-color: " + theme.textDim + "44; -fx-border-radius: 4px; -fx-background-radius: 4px; -fx-text-fill: " + theme.text + ";";
        redoBtn.setStyle(actionBtnStyle);
        undoBtn.setStyle(actionBtnStyle);
        replayBtn.setStyle(actionBtnStyle);

        updateRuleButtons(theme);

        // Update Sub Panels
        controlPanel.update(theme);
        statsPanel.update(theme);
        boardView.refresh(WinChecker.checkWin(engine.getState().getBoard(), engine.getConfig().getGridSize(), engine.getConfig().getWinLength()));

        // Bottom status bar
        statusBar.setStyle("-fx-background-color: " + theme.termBg + ";" +
                "-fx-border-color: " + theme.textDim + "26;" +
                "-fx-border-width: 1px 0 0 0;");

        String statusLblStyle = "-fx-text-fill: " + theme.textDim + ";";
        statusReadyLbl.setStyle("-fx-text-fill: " + theme.accentWin + ";");
        statusEngineLbl.setStyle(statusLblStyle);
        statusMovesLbl.setStyle(statusLblStyle);
        statusDepthLbl.setStyle(statusLblStyle);
        statusMemLbl.setStyle(statusLblStyle);
        statusTimeLbl.setStyle(statusLblStyle);

        updateMoveHistory();
        updateLiveStatusMetrics();
    }

    private void updateRuleButtons(Theme theme) {
        GameRule currentRule = engine.getConfig().getGameRule();
        int selectedRuleIdx = currentRule.ordinal();
        
        for (int i = 0; i < ruleButtons.size(); i++) {
            ToggleButton b = ruleButtons.get(i);
            boolean isSel = (i == selectedRuleIdx);
            b.setSelected(isSel);
            if (isSel) {
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

        if (rulesDescLbl != null) {
            rulesDescLbl.setStyle(ThemeManager.getLabelDimStyle(theme) + " -fx-font-size: 11px;");
            switch (currentRule) {
                case CLASSIC:
                    rulesDescLbl.setText("Classic Tic-Tac-Toe: Standard rules. Line up marks to win.");
                    break;
                case VANISH:
                    rulesDescLbl.setText("Vanish Mode: Memory grid. Max 3 pieces per player; oldest piece disappears.");
                    break;
                case BLITZ:
                    rulesDescLbl.setText("Blitz Mode: Speed match! 10-second timer per move before auto-play.");
                    break;
                case HAZARDS:
                    rulesDescLbl.setText("Hazards Mode: Obstacle grid. Red blocked wall cells cannot be played.");
                    break;
            }
        }
    }
}
