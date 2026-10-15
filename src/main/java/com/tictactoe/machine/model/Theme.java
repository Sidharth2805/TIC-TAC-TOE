package com.tictactoe.machine.model;

public enum Theme {
    SLATE("Slate", "#0F172A", "#1E293B", "#F8FAFC", "#94A3B8", "#38BDF8", "#FB7185", "#10B981", "#0B0F19", "#38BDF8"),
    ZINC("Zinc", "#09090B", "#18181B", "#FAFAFA", "#71717A", "#60A5FA", "#F87171", "#34D399", "#09090B", "#A1A1AA"),
    NORD("Nord", "#2E3440", "#3B4252", "#ECEFF4", "#88C0D0", "#81A1C1", "#BF616A", "#A3BE8C", "#242933", "#8FBCBB"),
    MONOKAI("Monokai", "#1E1F1C", "#272822", "#F8F8F2", "#75715E", "#66D9EF", "#FD971F", "#A6E22E", "#191A16", "#A6E22E");

    public final String label;
    public final String bg;
    public final String panel;
    public final String text;
    public final String textDim;
    public final String accentX;
    public final String accentO;
    public final String accentWin;
    public final String termBg;
    public final String termText;

    Theme(String label, String bg, String panel, String text, String textDim, String accentX, String accentO,
          String accentWin, String termBg, String termText) {
        this.label = label;
        this.bg = bg;
        this.panel = panel;
        this.text = text;
        this.textDim = textDim;
        this.accentX = accentX;
        this.accentO = accentO;
        this.accentWin = accentWin;
        this.termBg = termBg;
        this.termText = termText;
    }
}
