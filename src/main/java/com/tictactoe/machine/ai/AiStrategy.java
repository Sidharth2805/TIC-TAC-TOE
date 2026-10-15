package com.tictactoe.machine.ai;

import com.tictactoe.machine.model.GameState;
import com.tictactoe.machine.model.PlayerSymbol;
import com.tictactoe.machine.model.GameConfig;

public interface AiStrategy {
    Integer computeMove(GameState state, GameConfig config, PlayerSymbol aiSymbol);
}
