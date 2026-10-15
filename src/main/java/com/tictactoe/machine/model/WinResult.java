package com.tictactoe.machine.model;

public class WinResult {
    private final PlayerSymbol symbol;
    private final int[] cells;

    public WinResult(PlayerSymbol symbol, int[] cells) {
        this.symbol = symbol;
        this.cells = cells;
    }

    public PlayerSymbol getSymbol() { return symbol; }
    public int[] getCells() { return cells; }
}
