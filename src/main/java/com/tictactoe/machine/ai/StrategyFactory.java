package com.tictactoe.machine.ai;

import com.tictactoe.machine.model.Difficulty;

public class StrategyFactory {
    public static AiStrategy getStrategy(Difficulty difficulty) {
        switch (difficulty) {
            case ROOKIE:
                return new RookieStrategy();
            case TACTICAL:
                return new TacticalStrategy();
            case UNBEATABLE:
                return new MinimaxStrategy();
            case CHAOS:
                return new ChaosStrategy();
            default:
                return new MinimaxStrategy();
        }
    }
}
