package com.tictactoe.machine.engine;

import com.tictactoe.machine.ai.AiStrategy;
import com.tictactoe.machine.ai.StrategyFactory;
import com.tictactoe.machine.model.*;
import com.tictactoe.machine.util.ObservableQueue;
import com.tictactoe.machine.util.SoundManager;
import com.tictactoe.machine.view.GameEventListener;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameEngine {
    private final GameConfig config;
    private final GameState state;
    private final ObservableQueue<String> terminalLog = new ObservableQueue<>();
    private final List<GameEventListener> listeners = new ArrayList<>();
    private final Random rnd = new Random();

    private Timeline blitzTimeline;
    private Timeline aiTimeline;
    private Timeline replayTimeline;
    private int blitzRemainingSeconds = 0;
    private boolean isThinking = false;

    public GameEngine(GameConfig config, GameState state) {
        this.config = config;
        this.state = state;
    }

    public void addListener(GameEventListener listener) {
        listeners.add(listener);
    }

    public GameConfig getConfig() { return config; }
    public GameState getState() { return state; }
    public ObservableQueue<String> getTerminalLog() { return terminalLog; }

    public void log(String message, char type) {
        String logLine = "> " + message;
        terminalLog.add(logLine);
    }

    public void newMatch() {
        stopTimers();
        int size = config.getGridSize();
        state.initializeBoard(size);
        TurnManager.setupPlayers(state, config);
        
        if (config.getGameRule() == GameRule.HAZARDS) {
            RuleEngine.placeHazards(state, size);
        }
        
        log("[NEW MATCH]: " + size + "x" + size + " grid \u00b7 " + config.getGameMode().label + " \u00b7 "
                + config.getDifficulty().name() + " \u00b7 " + config.getGameRule().name() + " rules.", 'S');
        
        notifyBoardChanged();
        notifyStatsUpdated();
        
        proceedIfAI();
    }

    public void makeMove(int cellIndex) {
        if (state.isGameOver() || !config.isPower()) return;
        if (!isHumanTurn()) return;
        if (state.getBoard()[cellIndex] != PlayerSymbol.EMPTY) return;

        stopBlitzTimer();
        Player activePlayer = state.getCurrentPlayer();
        placePiece(cellIndex, activePlayer.getSymbol());
        log(getPlayerLabel(activePlayer.getSymbol()) + " places at cell " + cellIndex + ".", 'I');
        afterMove();
    }

    private void placePiece(int cellIndex, PlayerSymbol symbol) {
        state.getRedoStack().clear();
        state.getBoard()[cellIndex] = symbol;
        state.getLastGameMoves().add(new Move(cellIndex, symbol));
        
        if (config.getGameRule() == GameRule.VANISH) {
            Integer vanishedCell = RuleEngine.applyVanish(state, symbol, cellIndex, config.getGridSize());
            if (vanishedCell != null) {
                log("[VANISH]: " + getPlayerLabel(symbol) + "'s oldest mark at cell " + vanishedCell + " vanished!", 'I');
            }
        }
        
        int freq = (symbol == PlayerSymbol.X) ? 520 : ((symbol == PlayerSymbol.O) ? 340 : 420);
        SoundManager.beep(freq);
    }

    private void afterMove() {
        notifyBoardChanged();
        
        WinResult win = WinChecker.checkWin(state.getBoard(), config.getGridSize(), config.getWinLength());
        if (win != null) {
            endGame(win);
            return;
        }
        
        boolean boardFull = true;
        for (PlayerSymbol symbol : state.getBoard()) {
            if (symbol == PlayerSymbol.EMPTY) {
                boardFull = false;
                break;
            }
        }
        if (boardFull) {
            endGame(null);
            return;
        }

        TurnManager.nextTurn(state);
        notifyBoardChanged();
        proceedIfAI();
    }

    private void proceedIfAI() {
        if (state.isGameOver() || !config.isPower()) return;

        Player activePlayer = state.getCurrentPlayer();
        if (activePlayer.getType() == PlayerType.HUMAN) {
            if (config.getGameRule() == GameRule.BLITZ) {
                startBlitzTimer();
            }
            isThinking = false;
            notifyTurnChanged();
            return;
        }

        isThinking = true;
        notifyTurnChanged();
        
        int delay = config.getGameMode() == GameMode.AI_VS_AI ? 550 : 650;
        
        aiTimeline = new Timeline(new KeyFrame(Duration.millis(delay), e -> {
            if (state.isGameOver() || !config.isPower()) return;
            
            AiStrategy strategy = StrategyFactory.getStrategy(config.getDifficulty());
            Integer moveIdx = strategy.computeMove(state, config, activePlayer.getSymbol());
            
            isThinking = false;
            if (moveIdx != null) {
                placePiece(moveIdx, activePlayer.getSymbol());
                int nodes = Math.max(state.getNodeCount(), 1);
                log("COMPUTER computes move for " + getPlayerLabel(activePlayer.getSymbol()) + " -> cell " + moveIdx + " [" + nodes + " nodes]", 'A');
                afterMove();
            }
        }));
        aiTimeline.play();
    }

    private void startBlitzTimer() {
        stopBlitzTimer();
        blitzRemainingSeconds = 10;
        notifyBlitzTick(blitzRemainingSeconds);
        
        blitzTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            blitzRemainingSeconds--;
            notifyBlitzTick(blitzRemainingSeconds);
            if (blitzRemainingSeconds <= 0) {
                stopBlitzTimer();
                autoMoveExpiredTime();
            }
        }));
        blitzTimeline.setCycleCount(10);
        blitzTimeline.play();
    }

    private void autoMoveExpiredTime() {
        if (state.isGameOver() || !config.isPower()) return;
        
        List<Integer> empties = new ArrayList<>();
        PlayerSymbol[] board = state.getBoard();
        for (int i = 0; i < board.length; i++) {
            if (board[i] == PlayerSymbol.EMPTY) empties.add(i);
        }
        
        if (!empties.isEmpty()) {
            int cell = empties.get(rnd.nextInt(empties.size()));
            Player activePlayer = state.getCurrentPlayer();
            placePiece(cell, activePlayer.getSymbol());
            log("Time expired. Auto-move for " + getPlayerLabel(activePlayer.getSymbol()) + " at cell " + cell + ".", 'A');
            afterMove();
        }
    }

    private void endGame(WinResult win) {
        state.setGameOver(true);
        stopBlitzTimelineOnly();
        state.incrementMatches();
        
        if (win != null) {
            SoundManager.beep(660);
            if (win.getSymbol() == PlayerSymbol.X) {
                state.incrementP1Wins();
            } else if (win.getSymbol() == PlayerSymbol.O) {
                state.incrementP2Wins();
            }
            
            if (state.getLastWinner() != PlayerSymbol.EMPTY && state.getLastWinner() == win.getSymbol()) {
                state.setStreak(state.getStreak() + 1);
            } else {
                state.setStreak(1);
                state.setLastWinner(win.getSymbol());
            }
            if (state.getStreak() > state.getMaxStreak()) {
                state.setMaxStreak(state.getStreak());
            }
            state.setStreakWho(win.getSymbol());
            log(getPlayerLabel(win.getSymbol()) + " completes the line. Round closed.", 'W');
        } else {
            SoundManager.beep(220);
            state.incrementDraws();
            state.setStreak(0);
            state.setLastWinner(PlayerSymbol.EMPTY);
            state.setStreakWho(PlayerSymbol.EMPTY);
            log("Board full - no winner. Draw recorded.", 'I');
        }

        // Record Match outcome
        String outcome;
        if (win != null) {
            outcome = "Match " + state.getMatches() + ": " + getPlayerLabel(win.getSymbol()) + " won";
        } else {
            outcome = "Match " + state.getMatches() + ": Draw game";
        }
        state.getMatchHistory().add(outcome);
        
        notifyBoardChanged();
        notifyStatsUpdated();
        notifyGameOver(win);
    }

    public void resetStats() {
        state.resetStats();
        log("[STATS RESET]: All counters cleared.", 'S');
        notifyStatsUpdated();
    }

    public void undoMove() {
        if (state.isGameOver() || !config.isPower()) return;
        List<Move> moves = state.getLastGameMoves();
        if (moves.isEmpty()) {
            log("[UNDO]: No moves to undo.", 'I');
            return;
        }

        stopBlitzTimer();

        int popCount = 1;
        if (config.getGameMode() == GameMode.CPU) {
            if (moves.size() >= 2) {
                popCount = 2;
            }
        }

        log("[UNDO]: Rolling back " + popCount + " move(s).", 'S');

        for (int i = 0; i < popCount; i++) {
            if (!moves.isEmpty()) {
                Move m = moves.remove(moves.size() - 1);
                state.getRedoStack().add(m); // Push to redo stack
            }
        }

        // Rebuild state
        int size = config.getGridSize();
        PlayerSymbol[] board = state.getBoard();
        java.util.Arrays.fill(board, PlayerSymbol.EMPTY);
        for (int w : state.getWallPositions()) {
            board[w] = PlayerSymbol.H;
        }

        state.getSymbolHistory().get(PlayerSymbol.X).clear();
        state.getSymbolHistory().get(PlayerSymbol.O).clear();
        state.getSymbolHistory().get(PlayerSymbol.T).clear();

        List<Move> remaining = new java.util.ArrayList<>(moves);
        moves.clear();
        for (Move m : remaining) {
            placePiece(m.getIndex(), m.getSymbol());
        }

        if (popCount == 1) {
            int prevIdx = (state.getTurnIdx() - 1 + state.getTurnOrder().size()) % state.getTurnOrder().size();
            state.setTurnIdx(prevIdx);
        }

        notifyBoardChanged();
        proceedIfAI();
    }

    public void redoMove() {
        if (state.isGameOver() || !config.isPower()) return;
        List<Move> redoStack = state.getRedoStack();
        if (redoStack.isEmpty()) {
            log("[REDO]: No moves to redo.", 'I');
            return;
        }

        stopBlitzTimer();

        int redoCount = 1;
        if (config.getGameMode() == GameMode.CPU) {
            if (redoStack.size() >= 2) {
                redoCount = 2;
            }
        }

        log("[REDO]: Re-applying " + redoCount + " move(s).", 'S');

        for (int i = 0; i < redoCount; i++) {
            if (!redoStack.isEmpty()) {
                // Popping from the end of the redo stack (which is the last undone moves)
                Move m = redoStack.remove(redoStack.size() - 1);
                
                // Re-apply on board
                state.getBoard()[m.getIndex()] = m.getSymbol();
                state.getLastGameMoves().add(m);
                
                // Re-add to rule engine symbol history if Vanish rules
                if (config.getGameRule() == GameRule.VANISH) {
                    List<Integer> hist = state.getSymbolHistory().get(m.getSymbol());
                    hist.add(m.getIndex());
                    int maxSyms = config.getGridSize() == 3 ? 3 : 4;
                    if (hist.size() > maxSyms) {
                        int vanishCell = hist.remove(0);
                        state.getBoard()[vanishCell] = PlayerSymbol.EMPTY;
                    }
                }
                
                // Toggle turn order
                int nextIdx = (state.getTurnIdx() + 1) % state.getTurnOrder().size();
                state.setTurnIdx(nextIdx);
            }
        }

        notifyBoardChanged();
        proceedIfAI();
    }

    public void replayLastGame() {
        List<Move> moves = new ArrayList<>(state.getLastGameMoves());
        if (moves.isEmpty()) {
            log("[REPLAY]: No completed round to replay yet.", 'I');
            return;
        }
        
        stopTimers();
        state.initializeBoard(config.getGridSize());
        
        if (config.getGameRule() == GameRule.HAZARDS) {
            RuleEngine.placeHazards(state, config.getGridSize());
        }
        
        log("[REPLAY]: Reconstructing last round...", 'S');
        notifyBoardChanged();
        
        final int[] index = {0};
        replayTimeline = new Timeline(new KeyFrame(Duration.millis(400), e -> {
            if (index[0] >= moves.size()) {
                replayTimeline.stop();
                return;
            }
            Move move = moves.get(index[0]);
            state.getBoard()[move.getIndex()] = move.getSymbol();
            index[0]++;
            notifyBoardChanged();
        }));
        replayTimeline.setCycleCount(moves.size());
        replayTimeline.play();
    }

    public void togglePower() {
        boolean powerState = !config.isPower();
        config.setPower(powerState);
        log(powerState ? "[SYSTEM]: Power restored. AI Core online." : "[SYSTEM]: Power down. Board locked.", 'S');
        if (!powerState) {
            stopTimers();
        }
        notifyBoardChanged();
    }

    public void toggleSound() {
        boolean soundState = !config.isSoundOn();
        config.setSoundOn(soundState);
        SoundManager.setSoundOn(soundState);
        notifyTurnChanged();
    }

    public void toggleHeatmap() {
        config.setHeatOn(!config.isHeatOn());
        notifyBoardChanged();
    }

    private void stopTimers() {
        stopBlitzTimer();
        if (aiTimeline != null) { aiTimeline.stop(); aiTimeline = null; }
        if (replayTimeline != null) { replayTimeline.stop(); replayTimeline = null; }
    }

    private void stopBlitzTimer() {
        stopBlitzTimelineOnly();
        notifyBlitzTick(0);
    }
    
    private void stopBlitzTimelineOnly() {
        if (blitzTimeline != null) {
            blitzTimeline.stop();
            blitzTimeline = null;
        }
    }

    public String getPlayerLabel(PlayerSymbol symbol) {
        if (config.getGameMode() == GameMode.CPU) {
            if (symbol == PlayerSymbol.X) return config.getPlayer1Name();
            return "COMPUTER";
        }
        if (config.getGameMode() == GameMode.AI_VS_AI) {
            if (symbol == PlayerSymbol.X) return "COMPUTER 1";
            return "COMPUTER 2";
        }
        if (symbol == PlayerSymbol.X) return config.getPlayer1Name();
        if (symbol == PlayerSymbol.O) return config.getPlayer2Name();
        return config.getPlayer3Name();
    }

    public void updatePlayerNames() {
        if (state.getTurnOrder() != null) {
            for (Player p : state.getTurnOrder()) {
                if (p.getSymbol() == PlayerSymbol.X) {
                    p.setName(config.getGameMode() == GameMode.AI_VS_AI ? "COMPUTER 1" : config.getPlayer1Name());
                } else if (p.getSymbol() == PlayerSymbol.O) {
                    if (config.getGameMode() == GameMode.CPU) {
                        p.setName("COMPUTER");
                    } else if (config.getGameMode() == GameMode.AI_VS_AI) {
                        p.setName("COMPUTER 2");
                    } else {
                        p.setName(config.getPlayer2Name());
                    }
                } else if (p.getSymbol() == PlayerSymbol.T) {
                    p.setName(config.getPlayer3Name());
                }
            }
        }
        notifyTurnChanged();
        notifyStatsUpdated();
    }

    private boolean isHumanTurn() {
        if (state.isGameOver()) return false;
        Player player = state.getCurrentPlayer();
        return player != null && player.getType() == PlayerType.HUMAN;
    }

    private void notifyBoardChanged() {
        for (GameEventListener l : listeners) l.onBoardChanged();
        notifyTurnChanged();
    }

    private void notifyTurnChanged() {
        Player p = state.getCurrentPlayer();
        String label = (p != null) ? getPlayerLabel(p.getSymbol()) : "";
        for (GameEventListener l : listeners) l.onTurnChanged(label, isThinking);
    }

    private void notifyGameOver(WinResult result) {
        for (GameEventListener l : listeners) l.onGameOver(result);
    }

    private void notifyStatsUpdated() {
        for (GameEventListener l : listeners) l.onStatsUpdated();
    }

    private void notifyBlitzTick(int seconds) {
        for (GameEventListener l : listeners) l.onBlitzTick(seconds);
    }
}
