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
    
    protected Tile[][] board; // Changed to protected for subclass access
    protected int emptyX, emptyY; // Position of empty space (protected for subclasses)
    protected boolean isWon = false; // Protected for subclasses
    protected Random random; // Protected for subclasses
    protected boolean hasEmptyTile; // Flag to indicate if the board has an empty tile
    
    public PuzzleBoard() {
        this(true); // Default to classic mode with an empty tile
    }

    public PuzzleBoard(boolean hasEmptyTile) {
        this.hasEmptyTile = hasEmptyTile;
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
                if (hasEmptyTile && x == BOARD_SIZE - 1 && y == BOARD_SIZE - 1) {
                    // Last position is empty only if hasEmptyTile is true
                    board[x][y] = new Tile(x, y); // Create an empty tile
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
    protected void shuffleBoard() {
        // Create a list of all tiles (all 16 if no empty tile, 15 if one empty tile)
        List<Tile> tiles = new ArrayList<>();
        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                if (!hasEmptyTile || !board[x][y].isEmpty()) {
                    tiles.add(board[x][y]);
                }
            }
        }
        
        // Only shuffle if there's an empty tile and it's classic mode
        if (hasEmptyTile) {
            // Shuffle until we get a solvable configuration
            do {
                Collections.shuffle(tiles, random);
            } while (!isSolvable(tiles));
        } else {
            // For modes without empty tile, a simple shuffle is sufficient
            Collections.shuffle(tiles, random);
        }
        
        // Place shuffled tiles back on board
        int tileIndex = 0;
        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                if (hasEmptyTile && x == emptyX && y == emptyY) {
                    continue; // Skip empty position if present
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
    protected boolean isSolvable(List<Tile> tiles) {
        if (!hasEmptyTile) {
            return true; // No empty tile, so traditional solvability isn't a concern
        }

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
     * Attempt to move a tile at the given position (only for classic mode)
     */
    public boolean moveTile(int x, int y) {
        if (!hasEmptyTile) {
            return false; // Moving individual tiles is only for classic mode
        }

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
            board[x][y] = new Tile(emptyX, emptyY); // New empty tile at old empty position
            
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
     * Check if position is adjacent to empty space (only for classic mode)
     */
    private boolean isAdjacentToEmpty(int x, int y) {
        if (!hasEmptyTile) {
            return false; // Not applicable for boards without an empty tile
        }
        int dx = Math.abs(x - emptyX);
        int dy = Math.abs(y - emptyY);
        
        // Adjacent means exactly one unit away horizontally or vertically
        return (dx == 1 && dy == 0) || (dx == 0 && dy == 1);
    }
    
    /**
     * Check if puzzle is solved
     */
    protected void checkWinCondition() {
        isWon = true;
        
        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                Tile tile = board[x][y];
                // For boards with empty tile, empty tile must also be in correct position
                // For boards without empty tile, all 16 tiles must be in correct position
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
