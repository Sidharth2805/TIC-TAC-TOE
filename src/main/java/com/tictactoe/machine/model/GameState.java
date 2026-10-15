package com.tictactoe.machine.model;

import java.util.*;

public class GameState {
    private PlayerSymbol[] board;
    private List<Player> turnOrder = new ArrayList<>();
    private int turnIdx = 0;
    private boolean gameOver = false;

    // Statistics
    private int p1Wins = 0;
    private int p2Wins = 0;
    private int draws = 0;
    private int matches = 0;
    private int streak = 0;
    private int maxStreak = 0;
    private PlayerSymbol streakWho = PlayerSymbol.EMPTY;
    private PlayerSymbol lastWinner = PlayerSymbol.EMPTY;

    // Rule Tracking
    private final Map<PlayerSymbol, List<Integer>> symbolHistory = new HashMap<>();
    private final List<Integer> wallPositions = new ArrayList<>();
    private final List<Move> lastGameMoves = new ArrayList<>();
    private final List<Move> redoStack = new ArrayList<>();
    private final List<String> matchHistory = new ArrayList<>();

    // AI Performance Stats
    private int nodeCount = 0;
    private double lastCalcMs = -1;

    public GameState() {
        for (PlayerSymbol symbol : PlayerSymbol.values()) {
            symbolHistory.put(symbol, new ArrayList<>());
        }
    }

    public void initializeBoard(int size) {
        board = new PlayerSymbol[size * size];
        Arrays.fill(board, PlayerSymbol.EMPTY);
        symbolHistory.get(PlayerSymbol.X).clear();
        symbolHistory.get(PlayerSymbol.O).clear();
        symbolHistory.get(PlayerSymbol.T).clear();
        wallPositions.clear();
        lastGameMoves.clear();
        redoStack.clear();
        nodeCount = 0;
        lastCalcMs = -1;
        gameOver = false;
    }

    public PlayerSymbol[] getBoard() { return board; }
    public void setBoard(PlayerSymbol[] board) { this.board = board; }

    public List<Player> getTurnOrder() { return turnOrder; }
    public void setTurnOrder(List<Player> turnOrder) { this.turnOrder = turnOrder; }

    public int getTurnIdx() { return turnIdx; }
    public void setTurnIdx(int turnIdx) { this.turnIdx = turnIdx; }

    public boolean isGameOver() { return gameOver; }
    public void setGameOver(boolean gameOver) { this.gameOver = gameOver; }

    public int getP1Wins() { return p1Wins; }
    public void setP1Wins(int p1Wins) { this.p1Wins = p1Wins; }
    public void incrementP1Wins() { p1Wins++; }

    public int getP2Wins() { return p2Wins; }
    public void setP2Wins(int p2Wins) { this.p2Wins = p2Wins; }
    public void incrementP2Wins() { p2Wins++; }

    public int getDraws() { return draws; }
    public void setDraws(int draws) { this.draws = draws; }
    public void incrementDraws() { draws++; }

    public int getMatches() { return matches; }
    public void setMatches(int matches) { this.matches = matches; }
    public void incrementMatches() { matches++; }

    public int getStreak() { return streak; }
    public void setStreak(int streak) { this.streak = streak; }

    public int getMaxStreak() { return maxStreak; }
    public void setMaxStreak(int maxStreak) { this.maxStreak = maxStreak; }

    public PlayerSymbol getStreakWho() { return streakWho; }
    public void setStreakWho(PlayerSymbol streakWho) { this.streakWho = streakWho; }

    public PlayerSymbol getLastWinner() { return lastWinner; }
    public void setLastWinner(PlayerSymbol lastWinner) { this.lastWinner = lastWinner; }

    public Map<PlayerSymbol, List<Integer>> getSymbolHistory() { return symbolHistory; }
    public List<Integer> getWallPositions() { return wallPositions; }
    public List<Move> getLastGameMoves() { return lastGameMoves; }

    public List<Move> getRedoStack() { return redoStack; }
    public List<String> getMatchHistory() { return matchHistory; }

    public int getNodeCount() { return nodeCount; }
    public void setNodeCount(int nodeCount) { this.nodeCount = nodeCount; }

    public double getLastCalcMs() { return lastCalcMs; }
    public void setLastCalcMs(double lastCalcMs) { this.lastCalcMs = lastCalcMs; }

    public void resetStats() {
        p1Wins = 0;
        p2Wins = 0;
        draws = 0;
        matches = 0;
        streak = 0;
        maxStreak = 0;
        streakWho = PlayerSymbol.EMPTY;
        lastWinner = PlayerSymbol.EMPTY;
        matchHistory.clear();
    }

    public Player getCurrentPlayer() {
        if (turnOrder.isEmpty()) return null;
        return turnOrder.get(turnIdx);
    }
}
