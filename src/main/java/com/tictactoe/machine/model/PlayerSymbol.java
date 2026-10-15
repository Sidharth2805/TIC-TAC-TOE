package com.tictactoe.machine.model;

public enum PlayerSymbol {
    X('X', "X"), 
    O('O', "O"), 
    T('T', "\u25B3"), 
    H('H', "\u25A0"), // Hazard/Wall symbol - solid block '■'
    EMPTY('\0', "");

    public final char charValue;
    public final String glyph;

    PlayerSymbol(char charValue, String glyph) {
        this.charValue = charValue;
        this.glyph = glyph;
    }

    public static PlayerSymbol fromChar(char c) {
        for (PlayerSymbol s : values()) {
            if (s.charValue == c) return s;
        }
        if (c == 0) return EMPTY;
        return EMPTY;
    }
}
