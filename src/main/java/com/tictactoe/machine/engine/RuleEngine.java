package com.tictactoe.machine.engine;

import com.tictactoe.machine.model.GameState;
import com.tictactoe.machine.model.PlayerSymbol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RuleEngine {
    private static final Random rnd = new Random();

    public static void placeHazards(GameState state, int size) {
        int numHazards = size == 3 ? 1 : (size == 4 ? 2 : 3);
        List<Integer> cells = new ArrayList<>();
        for (int i = 0; i < size * size; i++) {
            cells.add(i);
        }
        Collections.shuffle(cells, rnd);
        for (int i = 0; i < numHazards; i++) {
            int cell = cells.get(i);
            state.getBoard()[cell] = PlayerSymbol.H;
            state.getWallPositions().add(cell);
        }
    }

    public static Integer applyVanish(GameState state, PlayerSymbol symbol, int cellIndex, int size) {
        List<Integer> history = state.getSymbolHistory().get(symbol);
        if (history == null) return null;
        
        history.add(cellIndex);
        int limit = size == 3 ? 3 : 4;
        if (history.size() > limit) {
            int oldestCell = history.remove(0);
            state.getBoard()[oldestCell] = PlayerSymbol.EMPTY;
            return oldestCell;
        }
        return null;
    }
}
