package com.tictactoe.machine.model;

public class Player {
    private final String id;
    private String name;
    private final PlayerType type;
    private final PlayerSymbol symbol;

    public Player(String id, String name, PlayerType type, PlayerSymbol symbol) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.symbol = symbol;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public PlayerType getType() { return type; }
    public PlayerSymbol getSymbol() { return symbol; }
}
