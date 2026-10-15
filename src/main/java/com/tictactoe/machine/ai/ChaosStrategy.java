package com.tictactoe.machine.ai;

import com.tictactoe.machine.model.GameState;
import com.tictactoe.machine.model.PlayerSymbol;
import com.tictactoe.machine.model.GameConfig;

import java.util.Random;

public class ChaosStrategy implements AiStrategy {
    private final Random rnd = new Random();
    private final AiStrategy rookie = new RookieStrategy();
    private final AiStrategy tactical = new TacticalStrategy();

    @Override
    public Integer computeMove(GameState state, GameConfig config, PlayerSymbol aiSymbol) {
        if (rnd.nextDouble() < 0.45) {
            return rookie.computeMove(state, config, aiSymbol);
        } else {
            return tactical.computeMove(state, config, aiSymbol);
        }
    }
}
