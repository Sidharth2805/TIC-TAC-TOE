package com.tictactoe.machine.view;

import com.tictactoe.machine.model.Theme;

public class ThemeManager {
    public static String getRootStyle(Theme theme) {
        return "-fx-background-color: " + theme.bg + ";";
    }

    public static String getPanelStyle(Theme theme) {
        return "-fx-background-color: " + theme.panel + ";" +
               "-fx-background-radius: 4px;" +
               "-fx-border-color: " + theme.textDim + "22;" +
               "-fx-border-radius: 4px;" +
               "-fx-border-width: 1px;";
    }

    public static String getLabelStyle(Theme theme) {
        return "-fx-text-fill: " + theme.text + ";" +
               "-fx-font-family: 'Segoe UI', -apple-system, BlinkMacSystemFont, Roboto, sans-serif;";
    }

    public static String getLabelDimStyle(Theme theme) {
        return "-fx-text-fill: " + theme.textDim + ";" +
               "-fx-font-family: 'Segoe UI', -apple-system, BlinkMacSystemFont, Roboto, sans-serif;";
    }

    public static String getTerminalStyle(Theme theme) {
        return "-fx-control-inner-background: " + theme.termBg + ";" +
               "-fx-text-fill: " + theme.termText + ";" +
               "-fx-font-family: 'Consolas', 'Courier New', monospace;" +
               "-fx-font-size: 11px;" +
               "-fx-background-radius: 4px;" +
               "-fx-border-radius: 4px;" +
               "-fx-border-color: " + theme.textDim + "22;";
    }
}
