package com.tictactoe.machine.model;

public class GameConfig {
    private int gridSize = 3;
    private Theme theme = Theme.SLATE;
    private GameMode gameMode = GameMode.CPU;
    private Difficulty difficulty = Difficulty.UNBEATABLE;
    private GameRule gameRule = GameRule.CLASSIC;
    private boolean soundOn = true;
    private boolean heatOn = false;
    private boolean power = true;

    private String player1Name = "Player 1";
    private String player2Name = "Player 2";
    private String player3Name = "Player 3";

    public String getPlayer1Name() { 
        return (player1Name != null && !player1Name.trim().isEmpty()) ? player1Name.trim() : "Player 1"; 
    }
    public void setPlayer1Name(String name) { this.player1Name = name; }

    public String getPlayer2Name() { 
        return (player2Name != null && !player2Name.trim().isEmpty()) ? player2Name.trim() : "Player 2"; 
    }
    public void setPlayer2Name(String name) { this.player2Name = name; }

    public String getPlayer3Name() { 
        return (player3Name != null && !player3Name.trim().isEmpty()) ? player3Name.trim() : "Player 3"; 
    }
    public void setPlayer3Name(String name) { this.player3Name = name; }

    public int getGridSize() { return gridSize; }
    public void setGridSize(int gridSize) { this.gridSize = gridSize; }

    public Theme getTheme() { return theme; }
    public void setTheme(Theme theme) { this.theme = theme; }

    public GameMode getGameMode() { return gameMode; }
    public void setGameMode(GameMode gameMode) { this.gameMode = gameMode; }

    public Difficulty getDifficulty() { return difficulty; }
    public void setDifficulty(Difficulty difficulty) { this.difficulty = difficulty; }

    public GameRule getGameRule() { return gameRule; }
    public void setGameRule(GameRule gameRule) { this.gameRule = gameRule; }

    public boolean isSoundOn() { return soundOn; }
    public void setSoundOn(boolean soundOn) { this.soundOn = soundOn; }

    public boolean isHeatOn() { return heatOn; }
    public void setHeatOn(boolean heatOn) { this.heatOn = heatOn; }

    public boolean isPower() { return power; }
    public void setPower(boolean power) { this.power = power; }

    public int getWinLength() {
        if (gridSize == 3) return 3;
        if (gridSize == 9) return 5;
        return 4;
    }
}
