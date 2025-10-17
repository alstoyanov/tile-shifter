package com.tileshifter;

import java.util.Random;

/**
 * Manages the Shift puzzle board where columns and rows can be shifted cyclically
 * Columns can be shifted up/down, rows can be shifted left/right
 */
public class ShiftPuzzleBoard extends PuzzleBoard {
    private Random random;
    
    public ShiftPuzzleBoard() {
        super(false); // No empty tile for Shift mode
        random = new Random();
    }
    
    /**
     * Shifts a column up (tiles move up, top tile wraps to bottom)
     * @param columnX The column index (0-3)
     */
    public void shiftColumnUp(int columnX) {
        if (columnX < 0 || columnX >= BOARD_SIZE) return;
        
        // Save the top tile
        Tile topTile = board[columnX][0];
        
        // Shift all tiles up
        for (int y = 0; y < BOARD_SIZE - 1; y++) {
            board[columnX][y] = board[columnX][y + 1];
            board[columnX][y].setGridPosition(columnX, y);
        }
        
        // Place the top tile at the bottom
        board[columnX][BOARD_SIZE - 1] = topTile;
        topTile.setGridPosition(columnX, BOARD_SIZE - 1);
        
        checkWinCondition();
    }
    
    /**
     * Shifts a column down (tiles move down, bottom tile wraps to top)
     * @param columnX The column index (0-3)
     */
    public void shiftColumnDown(int columnX) {
        if (columnX < 0 || columnX >= BOARD_SIZE) return;
        
        // Save the bottom tile
        Tile bottomTile = board[columnX][BOARD_SIZE - 1];
        
        // Shift all tiles down
        for (int y = BOARD_SIZE - 1; y > 0; y--) {
            board[columnX][y] = board[columnX][y - 1];
            board[columnX][y].setGridPosition(columnX, y);
        }
        
        // Place the bottom tile at the top
        board[columnX][0] = bottomTile;
        bottomTile.setGridPosition(columnX, 0);
        
        checkWinCondition();
    }
    
    /**
     * Shifts a row left (tiles move left, leftmost tile wraps to right)
     * @param rowY The row index (0-3)
     */
    public void shiftRowLeft(int rowY) {
        if (rowY < 0 || rowY >= BOARD_SIZE) return;
        
        // Save the leftmost tile
        Tile leftTile = board[0][rowY];
        
        // Shift all tiles left
        for (int x = 0; x < BOARD_SIZE - 1; x++) {
            board[x][rowY] = board[x + 1][rowY];
            board[x][rowY].setGridPosition(x, rowY);
        }
        
        // Place the leftmost tile at the right
        board[BOARD_SIZE - 1][rowY] = leftTile;
        leftTile.setGridPosition(BOARD_SIZE - 1, rowY);
        
        checkWinCondition();
    }
    
    /**
     * Shifts a row right (tiles move right, rightmost tile wraps to left)
     * @param rowY The row index (0-3)
     */
    public void shiftRowRight(int rowY) {
        if (rowY < 0 || rowY >= BOARD_SIZE) return;
        
        // Save the rightmost tile
        Tile rightTile = board[BOARD_SIZE - 1][rowY];
        
        // Shift all tiles right
        for (int x = BOARD_SIZE - 1; x > 0; x--) {
            board[x][rowY] = board[x - 1][rowY];
            board[x][rowY].setGridPosition(x, rowY);
        }
        
        // Place the rightmost tile at the left
        board[0][rowY] = rightTile;
        rightTile.setGridPosition(0, rowY);
        
        checkWinCondition();
    }
    
    /**
     * Shuffle the board using random shifts
     */
    @Override
    protected void shuffleBoard() {
        // Perform 30-50 random shifts to shuffle
        int numShifts = 30 + random.nextInt(21);
        for (int i = 0; i < numShifts; i++) {
            int operation = random.nextInt(4); // 0=up, 1=down, 2=left, 3=right
            int index = random.nextInt(BOARD_SIZE);
            
            switch (operation) {
                case 0: shiftColumnUp(index); break;
                case 1: shiftColumnDown(index); break;
                case 2: shiftRowLeft(index); break;
                case 3: shiftRowRight(index); break;
            }
        }
        
        isWon = false;
    }
    
    /**
     * For shift mode, we don't use the traditional solvability check
     */
    @Override
    protected boolean isSolvable(java.util.List<Tile> tiles) {
        return true; // All configurations are solvable in shift mode
    }
    
    /**
     * Override moveTile to disable it for shift mode
     */
    @Override
    public boolean moveTile(int x, int y) {
        return false; // Tiles cannot be moved individually in shift mode
    }
}

