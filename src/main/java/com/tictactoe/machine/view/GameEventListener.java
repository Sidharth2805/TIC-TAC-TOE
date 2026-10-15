package com.tictactoe.machine.view;

import com.tictactoe.machine.model.WinResult;

public interface GameEventListener {
    void onBoardChanged();
    void onTurnChanged(String activePlayerLabel, boolean isThinking);
    void onGameOver(WinResult winResult);
    void onStatsUpdated();
    void onBlitzTick(int secondsRemaining);
}
