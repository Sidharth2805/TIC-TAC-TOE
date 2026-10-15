package com.tictactoe.machine.model;

public class BoardPosition {
    private final int index;
    private final int row;
    private final int col;

    public BoardPosition(int index, int size) {
        this.index = index;
        this.row = index / size;
        this.col = index % size;
    }

    public BoardPosition(int row, int col, int size) {
        this.row = row;
        this.col = col;
        this.index = row * size + col;
    }

    public int getIndex() { return index; }
    public int getRow() { return row; }
    public int getCol() { return col; }
}
