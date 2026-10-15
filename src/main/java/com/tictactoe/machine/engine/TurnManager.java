package com.tictactoe.machine.engine;

import com.tictactoe.machine.model.Player;
import com.tictactoe.machine.model.GameMode;
import com.tictactoe.machine.model.PlayerSymbol;
import com.tictactoe.machine.model.PlayerType;
import com.tictactoe.machine.model.GameState;

import com.tictactoe.machine.model.GameConfig;

import java.util.ArrayList;
import java.util.List;

public class TurnManager {
    public static void setupPlayers(GameState state, GameConfig config) {
        List<Player> players = new ArrayList<>();
        switch (config.getGameMode()) {
            case CPU:
                players.add(new Player("p1", config.getPlayer1Name(), PlayerType.HUMAN, PlayerSymbol.X));
                players.add(new Player("p2", "COMPUTER", PlayerType.AI, PlayerSymbol.O));
                break;
            case TWO:
                players.add(new Player("p1", config.getPlayer1Name(), PlayerType.HUMAN, PlayerSymbol.X));
                players.add(new Player("p2", config.getPlayer2Name(), PlayerType.HUMAN, PlayerSymbol.O));
                break;
            case THREE:
                players.add(new Player("p1", config.getPlayer1Name(), PlayerType.HUMAN, PlayerSymbol.X));
                players.add(new Player("p2", config.getPlayer2Name(), PlayerType.HUMAN, PlayerSymbol.O));
                players.add(new Player("p3", config.getPlayer3Name(), PlayerType.HUMAN, PlayerSymbol.T));
                break;
            case AI_VS_AI:
                players.add(new Player("ai1", "COMPUTER 1", PlayerType.AI, PlayerSymbol.X));
                players.add(new Player("ai2", "COMPUTER 2", PlayerType.AI, PlayerSymbol.O));
                break;
        }
        state.setTurnOrder(players);
        state.setTurnIdx(0);
    }

    public static void setupPlayers(GameState state, GameMode mode) {
        GameConfig dummy = new GameConfig();
        dummy.setGameMode(mode);
        setupPlayers(state, dummy);
    }

    public static void nextTurn(GameState state) {
        int nextIdx = (state.getTurnIdx() + 1) % state.getTurnOrder().size();
        state.setTurnIdx(nextIdx);
    }
}
