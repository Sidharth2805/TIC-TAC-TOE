package com.tictactoe.machine.ai;

import com.tictactoe.machine.engine.WinChecker;
import com.tictactoe.machine.model.GameState;
import com.tictactoe.machine.model.PlayerSymbol;
import com.tictactoe.machine.model.GameConfig;
import com.tictactoe.machine.model.WinResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MinimaxStrategy implements AiStrategy {
    private final Random rnd = new Random();
    private int nodes = 0;

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

        // Find opponent symbol
        PlayerSymbol humanSymbol = PlayerSymbol.EMPTY;
        for (com.tictactoe.machine.model.Player p : state.getTurnOrder()) {
            if (p.getSymbol() != aiSymbol) {
                humanSymbol = p.getSymbol();
                break;
            }
        }

        // Fast check: Immediate win or block
        Integer winMove = findImmediate(board, size, winLen, aiSymbol, empties);
        if (winMove != null) {
            state.setNodeCount(empties.size());
            return winMove;
        }
        if (humanSymbol != PlayerSymbol.EMPTY) {
            Integer blockMove = findImmediate(board, size, winLen, humanSymbol, empties);
            if (blockMove != null) {
                state.setNodeCount(empties.size() * 2);
                return blockMove;
            }
        }

        int depth = size == 3 ? 9 : (size == 4 ? 3 : 2);
        nodes = 0;
        double bestScore = Double.NEGATIVE_INFINITY;
        List<Integer> bestMoves = new ArrayList<>();
        
        long t0 = System.nanoTime();
        for (int idx : empties) {
            PlayerSymbol[] copy = board.clone();
            copy[idx] = aiSymbol;
            double score = minimax(copy, size, winLen, depth - 1, false,
                    Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, aiSymbol, humanSymbol);
            if (score > bestScore) {
                bestScore = score;
                bestMoves.clear();
                bestMoves.add(idx);
            } else if (score == bestScore) {
                bestMoves.add(idx);
            }
        }
        double elapsedMs = (System.nanoTime() - t0) / 1_000_000.0;
        state.setNodeCount(nodes);
        state.setLastCalcMs(elapsedMs);

        if (bestMoves.isEmpty()) return empties.get(0);
        return bestMoves.get(rnd.nextInt(bestMoves.size()));
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

    private double minimax(PlayerSymbol[] b, int size, int winLen, int depth, boolean isMax,
                           double alpha, double beta, PlayerSymbol aiSym, PlayerSymbol humanSym) {
        nodes++;
        WinResult win = WinChecker.checkWin(b, size, winLen);
        if (win != null) {
            return win.getSymbol() == aiSym ? (1000.0 + depth) : -(1000.0 + depth);
        }
        
        List<Integer> empties = new ArrayList<>();
        for (int i = 0; i < b.length; i++) {
            if (b[i] == PlayerSymbol.EMPTY) empties.add(i);
        }
        if (empties.isEmpty()) return 0;
        if (depth == 0) {
            return HeuristicEvaluator.evaluate(b, size, winLen, aiSym, humanSym);
        }

        if (isMax) {
            double best = Double.NEGATIVE_INFINITY;
            for (int idx : empties) {
                b[idx] = aiSym;
                double v = minimax(b, size, winLen, depth - 1, false, alpha, beta, aiSym, humanSym);
                b[idx] = PlayerSymbol.EMPTY;
                best = Math.max(best, v);
                alpha = Math.max(alpha, v);
                if (alpha >= beta) break;
            }
            return best;
        } else {
            double best = Double.POSITIVE_INFINITY;
            for (int idx : empties) {
                b[idx] = humanSym;
                double v = minimax(b, size, winLen, depth - 1, true, alpha, beta, aiSym, humanSym);
                b[idx] = PlayerSymbol.EMPTY;
                best = Math.min(best, v);
                beta = Math.min(beta, v);
                if (alpha >= beta) break;
            }
            return best;
        }
    }
}
