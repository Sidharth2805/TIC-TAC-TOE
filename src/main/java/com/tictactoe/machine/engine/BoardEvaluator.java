package com.tictactoe.machine.engine;

import com.tictactoe.machine.model.PlayerSymbol;
import java.util.List;

public class BoardEvaluator {
    public static double evaluate(PlayerSymbol[] board, int size, int winLen, PlayerSymbol aiSym, PlayerSymbol humanSym) {
        double score = 0;
        List<int[]> lines = WinChecker.getLines(size, winLen);
        for (int[] line : lines) {
            int aiCount = 0;
            int humanCount = 0;
            boolean hasHazard = false;
            for (int idx : line) {
                PlayerSymbol symbol = board[idx];
                if (symbol == aiSym) {
                    aiCount++;
                } else if (symbol == humanSym) {
                    humanCount++;
                } else if (symbol == PlayerSymbol.H) {
                    hasHazard = true;
                }
            }
            if (hasHazard || (aiCount > 0 && humanCount > 0)) {
                continue;
            }
            if (aiCount > 0) {
                score += Math.pow(10, aiCount);
            }
            if (humanCount > 0) {
                score -= Math.pow(10, humanCount);
            }
        }
        return score;
    }
}
