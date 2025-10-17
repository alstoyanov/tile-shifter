package com.tileshifter;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.Random;

/**
 * Manages the Rotate puzzle board where 2x2 sub-boards can be rotated clockwise
 * Five overlapping 2x2 sub-boards: top-left, top-right, bottom-left, bottom-right, and center
 */
public class RotatePuzzleBoard extends PuzzleBoard {
    private Random random;
    
    // Define the 5 sub-board starting positions (top-left corner of each 2x2 sub-board)
    private static final int[][] SUBBOARD_POSITIONS = {
        {0, 0}, // Top-left
        {0, 2}, // Bottom-left  
        {2, 0}, // Top-right
        {2, 2}, // Bottom-right
        {1, 1}  // Center
    };
    
    public RotatePuzzleBoard() {
        super(false); // No empty tile for Rotate mode
        random = new Random();
    }
    
    /**
     * Rotates a 2x2 sub-board clockwise
     * @param subBoardIndex Index of the sub-board (0-4)
     * @return true if rotation was successful
     */
    public boolean rotateSubBoard(int subBoardIndex) {
        if (subBoardIndex < 0 || subBoardIndex >= SUBBOARD_POSITIONS.length) {
            return false;
        }
        
        int startX = SUBBOARD_POSITIONS[subBoardIndex][0];
        int startY = SUBBOARD_POSITIONS[subBoardIndex][1];
        
        // Get the 4 tiles in the 2x2 sub-board
        Tile topLeft = board[startX][startY];
        Tile topRight = board[startX + 1][startY];
        Tile bottomLeft = board[startX][startY + 1];
        Tile bottomRight = board[startX + 1][startY + 1];
        
        // Rotate clockwise: TL -> TR -> BR -> BL -> TL
        board[startX][startY] = bottomLeft;
        board[startX + 1][startY] = topLeft;
        board[startX + 1][startY + 1] = topRight;
        board[startX][startY + 1] = bottomRight;
        
        // Update grid positions
        board[startX][startY].setGridPosition(startX, startY);
        board[startX + 1][startY].setGridPosition(startX + 1, startY);
        board[startX + 1][startY + 1].setGridPosition(startX + 1, startY + 1);
        board[startX][startY + 1].setGridPosition(startX, startY + 1);
        
        // Check win condition
        checkWinCondition();
        
        return true;
    }
    
    /**
     * Determines which sub-board (if any) contains the given grid coordinates
     * @param gridX X coordinate in the grid
     * @param gridY Y coordinate in the grid
     * @return Index of the sub-board (0-4), or -1 if not in any clickable sub-board
     */
    public int getSubBoardAtPosition(int gridX, int gridY) {
        for (int i = 0; i < SUBBOARD_POSITIONS.length; i++) {
            int startX = SUBBOARD_POSITIONS[i][0];
            int startY = SUBBOARD_POSITIONS[i][1];
            
            if (gridX >= startX && gridX < startX + 2 && 
                gridY >= startY && gridY < startY + 2) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Shuffle the board using random rotations
     */
    @Override
    protected void shuffleBoard() {
        // Perform 50-100 random rotations to shuffle
        int numRotations = 50 + random.nextInt(51);
        for (int i = 0; i < numRotations; i++) {
            int subBoardIndex = random.nextInt(SUBBOARD_POSITIONS.length);
            rotateSubBoard(subBoardIndex);
        }
        
        isWon = false;
    }
    
    /**
     * For rotate mode, we don't use the traditional solvability check
     */
    @Override
    protected boolean isSolvable(java.util.List<Tile> tiles) {
        return true; // All configurations are solvable in rotate mode
    }
    
    /**
     * Override moveTile to disable it for rotate mode
     */
    @Override
    public boolean moveTile(int x, int y) {
        return false; // Tiles cannot be moved in rotate mode, only rotated
    }
}

