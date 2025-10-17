package com.tileshifter;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Manages the puzzle board state, tile movement, and win detection
 */
public class PuzzleBoard {
    public static final int BOARD_SIZE = 4;
    public static final int TOTAL_TILES = BOARD_SIZE * BOARD_SIZE;
    
    private Tile[][] board;
    private int emptyX, emptyY; // Position of empty space
    private boolean isWon = false;
    private Random random;
    
    public PuzzleBoard() {
        board = new Tile[BOARD_SIZE][BOARD_SIZE];
        random = new Random();
    }
    
    /**
     * Initialize the board with an image split into 4x4 tiles
     */
    public void initializeBoard(Texture texture) {
        int tileWidth = texture.getWidth() / BOARD_SIZE;
        int tileHeight = texture.getHeight() / BOARD_SIZE;
        
        int tileId = 0;
        
        // Create tiles from texture regions
        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                if (x == BOARD_SIZE - 1 && y == BOARD_SIZE - 1) {
                    // Last position is empty
                    board[x][y] = new Tile(x, y);
                    emptyX = x;
                    emptyY = y;
                } else {
                    // Create texture region for this tile
                    TextureRegion region = new TextureRegion(texture, 
                        x * tileWidth, y * tileHeight, tileWidth, tileHeight);
                    board[x][y] = new Tile(region, x, y, tileId++);
                }
            }
        }
        
        // Shuffle the board
        shuffleBoard();
    }
    
    /**
     * Shuffle the board ensuring a solvable configuration
     */
    private void shuffleBoard() {
        // Create a list of all tiles except empty
        List<Tile> tiles = new ArrayList<>();
        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                if (!board[x][y].isEmpty()) {
                    tiles.add(board[x][y]);
                }
            }
        }
        
        // Shuffle until we get a solvable configuration
        do {
            Collections.shuffle(tiles, random);
        } while (!isSolvable(tiles));
        
        // Place shuffled tiles back on board
        int tileIndex = 0;
        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                if (x == emptyX && y == emptyY) {
                    continue; // Skip empty position
                }
                
                Tile tile = tiles.get(tileIndex++);
                board[x][y] = tile;
                tile.setGridPosition(x, y);
            }
        }
        
        isWon = false;
    }
    
    /**
     * Check if a tile configuration is solvable using inversion count
     */
    private boolean isSolvable(List<Tile> tiles) {
        int inversions = 0;
        
        // Count inversions
        for (int i = 0; i < tiles.size() - 1; i++) {
            for (int j = i + 1; j < tiles.size(); j++) {
                Tile tile1 = tiles.get(i);
                Tile tile2 = tiles.get(j);
                
                int pos1 = tile1.getCorrectY() * BOARD_SIZE + tile1.getCorrectX();
                int pos2 = tile2.getCorrectY() * BOARD_SIZE + tile2.getCorrectX();
                
                if (pos1 > pos2) {
                    inversions++;
                }
            }
        }
        
        // For 4x4 grid, puzzle is solvable if:
        // Grid width is even and (inversion count + row of empty space) is odd
        int emptyRowFromBottom = BOARD_SIZE - emptyY;
        return (inversions + emptyRowFromBottom) % 2 == 1;
    }
    
    /**
     * Attempt to move a tile at the given position
     */
    public boolean moveTile(int x, int y) {
        if (x < 0 || x >= BOARD_SIZE || y < 0 || y >= BOARD_SIZE) {
            return false;
        }
        
        Tile tile = board[x][y];
        if (tile.isEmpty()) {
            return false; // Can't move empty space
        }
        
        // Check if tile is adjacent to empty space
        if (isAdjacentToEmpty(x, y)) {
            // Swap tile with empty space
            board[emptyX][emptyY] = tile;
            board[x][y] = new Tile(x, y); // New empty tile
            
            tile.setGridPosition(emptyX, emptyY);
            emptyX = x;
            emptyY = y;
            
            // Check win condition
            checkWinCondition();
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Check if position is adjacent to empty space
     */
    private boolean isAdjacentToEmpty(int x, int y) {
        int dx = Math.abs(x - emptyX);
        int dy = Math.abs(y - emptyY);
        
        // Adjacent means exactly one unit away horizontally or vertically
        return (dx == 1 && dy == 0) || (dx == 0 && dy == 1);
    }
    
    /**
     * Check if puzzle is solved
     */
    private void checkWinCondition() {
        isWon = true;
        
        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                Tile tile = board[x][y];
                if (!tile.isInCorrectPosition()) {
                    isWon = false;
                    return;
                }
            }
        }
    }
    
    /**
     * Update animations for all tiles
     */
    public void update(float deltaTime) {
        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                board[x][y].updateAnimation(deltaTime);
            }
        }
    }
    
    /**
     * Reset the board with a new shuffle
     */
    public void reset() {
        shuffleBoard();
    }
    
    // Getters
    public Tile getTile(int x, int y) {
        if (x < 0 || x >= BOARD_SIZE || y < 0 || y >= BOARD_SIZE) {
            return null;
        }
        return board[x][y];
    }
    
    public int getEmptyX() { return emptyX; }
    public int getEmptyY() { return emptyY; }
    public boolean isWon() { return isWon; }
    
    /**
     * Check if any tile is currently animating
     */
    public boolean isAnimating() {
        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                if (board[x][y].isAnimating()) {
                    return true;
                }
            }
        }
        return false;
    }
}
