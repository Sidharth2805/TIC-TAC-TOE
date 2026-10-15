package com.tictactoe.machine.engine;

import com.tictactoe.machine.model.PlayerSymbol;
import com.tictactoe.machine.model.WinResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WinChecker {
    private static final Map<String, List<int[]>> lineCache = new HashMap<>();

    public static synchronized List<int[]> getLines(int size, int winLen) {
        String key = size + "_" + winLen;
        return lineCache.computeIfAbsent(key, k -> computeLines(size, winLen));
    }

    private static List<int[]> computeLines(int size, int winLen) {
        List<int[]> lines = new ArrayList<>();
        // Horizontal lines
        for (int r = 0; r < size; r++) {
            for (int c = 0; c <= size - winLen; c++) {
                int[] line = new int[winLen];
                for (int k = 0; k < winLen; k++) {
                    line[k] = r * size + (c + k);
                }
                lines.add(line);
            }
        }
        // Vertical lines
        for (int c = 0; c < size; c++) {
            for (int r = 0; r <= size - winLen; r++) {
                int[] line = new int[winLen];
                for (int k = 0; k < winLen; k++) {
                    line[k] = (r + k) * size + c;
                }
                lines.add(line);
            }
        }
        // Diagonals
        for (int r = 0; r <= size - winLen; r++) {
            for (int c = 0; c <= size - winLen; c++) {
                int[] line1 = new int[winLen];
                int[] line2 = new int[winLen];
                for (int k = 0; k < winLen; k++) {
                    line1[k] = (r + k) * size + (c + k);
                    line2[k] = (r + k) * size + (c + winLen - 1 - k);
                }
                lines.add(line1);
                lines.add(line2);
            }
        }
        return lines;
    }

    public static WinResult checkWin(PlayerSymbol[] board, int size, int winLen) {
        if (board == null) return null;
        for (int[] line : getLines(size, winLen)) {
            PlayerSymbol first = board[line[0]];
            if (first == PlayerSymbol.EMPTY || first == PlayerSymbol.H) continue;
            boolean won = true;
            for (int idx : line) {
                if (board[idx] != first) {
                    won = false;
                    break;
                }
            }
            if (won) {
                return new WinResult(first, line);
            }
        }
        return null;
    }
}
