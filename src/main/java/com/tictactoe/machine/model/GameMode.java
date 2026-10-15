package com.tictactoe.machine.model;

public enum GameMode {
    CPU("vs Computer"), TWO("2 Players"), THREE("3 Players"), AI_VS_AI("Computer vs Computer");

    public final String label;
    GameMode(String label) {
        this.label = label;
    }
}
