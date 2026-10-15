package com.tictactoe.machine.ai;

import com.tictactoe.machine.model.GameState;
import com.tictactoe.machine.model.PlayerSymbol;
import com.tictactoe.machine.model.GameConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RookieStrategy implements AiStrategy {
    private final Random rnd = new Random();

    @Override
    public Integer computeMove(GameState state, GameConfig config, PlayerSymbol aiSymbol) {
        state.setNodeCount(1);
        PlayerSymbol[] board = state.getBoard();
        List<Integer> empties = new ArrayList<>();
        for (int i = 0; i < board.length; i++) {
            if (board[i] == PlayerSymbol.EMPTY) empties.add(i);
        }
        if (empties.isEmpty()) return null;
        return empties.get(rnd.nextInt(empties.size()));
    }
}
