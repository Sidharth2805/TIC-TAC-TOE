package com.tictactoe.machine.ai;

import com.tictactoe.machine.engine.WinChecker;
import com.tictactoe.machine.model.GameState;
import com.tictactoe.machine.model.PlayerSymbol;
import com.tictactoe.machine.model.GameConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TacticalStrategy implements AiStrategy {
    private final Random rnd = new Random();

    @Override
    public Integer computeMove(GameState state, GameConfig config, PlayerSymbol aiSymbol) {
        PlayerSymbol[] board = state.getBoard();
        int size = config.getGridSize();
        int winLen = config.getWinLength();
        
        List<Integer> empties = new ArrayList<>();
        for (int i = 0; i < board.length; i++) {
            if (board[i] == PlayerSymbol.EMPTY) empties.add(i);
        }
        if (empties.isEmpty()) return null;

        state.setNodeCount(empties.size() * 2);

        // Find opposing symbol
        PlayerSymbol humanSymbol = PlayerSymbol.EMPTY;
        for (com.tictactoe.machine.model.Player p : state.getTurnOrder()) {
            if (p.getSymbol() != aiSymbol) {
                humanSymbol = p.getSymbol();
                break;
            }
        }

        // 1. Immediate Win
        Integer winMove = findImmediate(board, size, winLen, aiSymbol, empties);
        if (winMove != null) return winMove;

        // 2. Immediate Block
        if (humanSymbol != PlayerSymbol.EMPTY) {
            Integer blockMove = findImmediate(board, size, winLen, humanSymbol, empties);
            if (blockMove != null) return blockMove;
        }

        // 3. Center-ish or Random
        if (rnd.nextDouble() < 0.5) {
            return pickCenterish(size, empties);
        }
        return empties.get(rnd.nextInt(empties.size()));
    }

    private Integer findImmediate(PlayerSymbol[] board, int size, int winLen, PlayerSymbol symbol, List<Integer> empties) {
        for (int idx : empties) {
            PlayerSymbol[] copy = board.clone();
            copy[idx] = symbol;
            if (WinChecker.checkWin(copy, size, winLen) != null) {
                return idx;
            }
        }
        return null;
    }

    private int pickCenterish(int size, List<Integer> empties) {
        double mid = (size - 1) / 2.0;
        int best = empties.get(0);
        double bd = Double.MAX_VALUE;
        for (int idx : empties) {
            int r = idx / size;
            int c = idx % size;
            double d = Math.abs(r - mid) + Math.abs(c - mid);
            if (d < bd) {
                bd = d;
                best = idx;
            }
        }
        return best;
    }
}
