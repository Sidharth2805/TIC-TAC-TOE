package com.tictactoe.machine.engine;

import com.tictactoe.machine.model.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class GameRuleTest {

    @Test
    public void testClassicRule() {
        GameConfig config = new GameConfig();
        config.setGameRule(GameRule.CLASSIC);
        GameState state = new GameState();
        GameEngine engine = new GameEngine(config, state);
        engine.newMatch();

        assertEquals(GameRule.CLASSIC, config.getGameRule());
        assertEquals(0, state.getWallPositions().size());
    }

    @Test
    public void testVanishRuleRemovesOldestMark() {
        GameConfig config = new GameConfig();
        config.setGameRule(GameRule.VANISH);
        config.setGridSize(3);
        GameState state = new GameState();
        state.initializeBoard(3);

        // Player X places 3 marks (limit for 3x3 is 3)
        assertNull(RuleEngine.applyVanish(state, PlayerSymbol.X, 0, 3));
        assertNull(RuleEngine.applyVanish(state, PlayerSymbol.X, 1, 3));
        assertNull(RuleEngine.applyVanish(state, PlayerSymbol.X, 2, 3));

        List<Integer> xHistory = state.getSymbolHistory().get(PlayerSymbol.X);
        assertEquals(3, xHistory.size());

        // 4th mark causes oldest (cell 0) to vanish!
        Integer vanished = RuleEngine.applyVanish(state, PlayerSymbol.X, 4, 3);
        assertEquals(Integer.valueOf(0), vanished);
        assertEquals(PlayerSymbol.EMPTY, state.getBoard()[0]);
        assertEquals(3, xHistory.size());
        assertTrue(xHistory.contains(1));
        assertTrue(xHistory.contains(2));
        assertTrue(xHistory.contains(4));
    }

    @Test
    public void testHazardsRulePlacesBlockedCells() {
        GameConfig config = new GameConfig();
        config.setGameRule(GameRule.HAZARDS);
        config.setGridSize(3);
        GameState state = new GameState();
        GameEngine engine = new GameEngine(config, state);
        engine.newMatch();

        assertEquals(GameRule.HAZARDS, config.getGameRule());
        assertTrue(state.getWallPositions().size() > 0, "Hazard positions must be populated");
        for (int cell : state.getWallPositions()) {
            assertEquals(PlayerSymbol.H, state.getBoard()[cell]);
        }
    }

    @Test
    public void testPlayerNamesAndComputerLabeling() {
        GameConfig config = new GameConfig();
        config.setGameMode(GameMode.CPU);
        config.setPlayer1Name("Neo");
        GameState state = new GameState();
        GameEngine engine = new GameEngine(config, state);
        engine.newMatch();

        assertEquals("Neo", engine.getPlayerLabel(PlayerSymbol.X));
        assertEquals("COMPUTER", engine.getPlayerLabel(PlayerSymbol.O));

        // Dynamically update player name
        config.setPlayer1Name("Trinity");
        engine.updatePlayerNames();
        assertEquals("Trinity", engine.getPlayerLabel(PlayerSymbol.X));

        // Switch to 2 Players
        config.setGameMode(GameMode.TWO);
        config.setPlayer2Name("Morpheus");
        engine.newMatch();
        assertEquals("Trinity", engine.getPlayerLabel(PlayerSymbol.X));
        assertEquals("Morpheus", engine.getPlayerLabel(PlayerSymbol.O));
    }
}
