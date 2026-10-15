package com.tictactoe.machine.ai;

import com.tictactoe.machine.engine.BoardEvaluator;
import com.tictactoe.machine.model.PlayerSymbol;

public class HeuristicEvaluator {
    public static double evaluate(PlayerSymbol[] board, int size, int winLen, PlayerSymbol aiSym, PlayerSymbol humanSym) {
        return BoardEvaluator.evaluate(board, size, winLen, aiSym, humanSym);
    }
}
