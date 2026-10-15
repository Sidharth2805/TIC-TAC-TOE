package com.tictactoe.machine.model;

public class Move {
    private final int index;
    private final PlayerSymbol symbol;
    private final long timestamp;

    public Move(int index, PlayerSymbol symbol) {
        this.index = index;
        this.symbol = symbol;
        this.timestamp = System.currentTimeMillis();
    }

    public int getIndex() { return index; }
    public PlayerSymbol getSymbol() { return symbol; }
    public long getTimestamp() { return timestamp; }
}
