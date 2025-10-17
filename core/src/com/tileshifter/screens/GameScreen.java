package com.tileshifter.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.tileshifter.PuzzleBoard;
import com.tileshifter.Tile;
import com.tileshifter.TileShiftGame;

/**
 * Main game screen where the puzzle is played
 */
public class GameScreen implements Screen {
    private TileShiftGame game;
    private PuzzleBoard puzzleBoard;
    private Texture puzzleTexture;
    private String imageName;
    
    // UI elements
    private Rectangle backButton;
    private Rectangle resetButton;
    private Rectangle winMessageArea;
    
    // Board rendering properties
    private float boardStartX, boardStartY;
    private float tileSize;
    private static final float BOARD_PADDING = 50f;
    
    // Win state
    private boolean showWinMessage = false;
    private float winMessageTimer = 0f;
    
    public GameScreen(TileShiftGame game, String imageName) {
        this.game = game;
        this.imageName = imageName;
        
        // Load the puzzle image
        puzzleTexture = new Texture(Gdx.files.internal("images/" + imageName));
        
        // Initialize puzzle board
        puzzleBoard = new PuzzleBoard();
        puzzleBoard.initializeBoard(puzzleTexture);
        
        setupUI();
        calculateBoardLayout();
    }
    
    private void setupUI() {
        // Back button (top-left)
        backButton = new Rectangle(20, TileShiftGame.SCREEN_HEIGHT - 60, 100, 40);
        
        // Reset button (top-right)
        resetButton = new Rectangle(TileShiftGame.SCREEN_WIDTH - 120, TileShiftGame.SCREEN_HEIGHT - 60, 100, 40);
        
        // Win message area (center)
        winMessageArea = new Rectangle(
            TileShiftGame.SCREEN_WIDTH / 2 - 150, 
            TileShiftGame.SCREEN_HEIGHT / 2 - 50, 
            300, 100
        );
    }
    
    private void calculateBoardLayout() {
        // Calculate board size to fit in screen with padding
        float availableWidth = TileShiftGame.SCREEN_WIDTH - 2 * BOARD_PADDING;
        float availableHeight = TileShiftGame.SCREEN_HEIGHT - 2 * BOARD_PADDING - 80; // Extra space for UI
        
        float maxBoardSize = Math.min(availableWidth, availableHeight);
        tileSize = maxBoardSize / PuzzleBoard.BOARD_SIZE;
        
        // Center the board
        float boardSize = tileSize * PuzzleBoard.BOARD_SIZE;
        boardStartX = (TileShiftGame.SCREEN_WIDTH - boardSize) / 2;
        boardStartY = (TileShiftGame.SCREEN_HEIGHT - boardSize) / 2 - 20; // Slightly lower for UI
        
        // Set initial render positions for all tiles
        updateTileRenderPositions();
    }
    
    private void updateTileRenderPositions() {
        for (int y = 0; y < PuzzleBoard.BOARD_SIZE; y++) {
            for (int x = 0; x < PuzzleBoard.BOARD_SIZE; x++) {
                Tile tile = puzzleBoard.getTile(x, y);
                if (tile != null) {
                    float renderX = boardStartX + x * tileSize;
                    float renderY = boardStartY + y * tileSize;
                    tile.setRenderPosition(renderX, renderY);
                }
            }
        }
    }
    
    @Override
    public void show() {
        // Called when this screen becomes the current screen
    }
    
    @Override
    public void render(float delta) {
        // Update game logic
        puzzleBoard.update(delta);
        
        // Check for win condition
        if (puzzleBoard.isWon() && !showWinMessage) {
            showWinMessage = true;
            winMessageTimer = 0f;
        }
        
        if (showWinMessage) {
            winMessageTimer += delta;
        }
        
        // Clear screen
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        game.batch.begin();
        
        // Draw UI buttons
        drawButton(backButton, "Back");
        drawButton(resetButton, "Reset");
        
        // Draw puzzle board
        drawPuzzleBoard();
        
        // Draw win message if needed
        if (showWinMessage) {
            drawWinMessage();
        }
        
        // Draw game info and instructions
        game.font.draw(game.batch, "Image: " + imageName, 20, 50);
        game.font.draw(game.batch, "Click tiles adjacent to empty space to move them", 20, 30);
        
        // Show move count or other game stats
        if (!showWinMessage) {
            game.font.draw(game.batch, "Arrange tiles to complete the image", 20, 10);
        }
        
        game.batch.end();
        
        // Handle input
        handleInput();
    }
    
    private void drawButton(Rectangle button, String text) {
        // Draw button text centered without border artifacts
        com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(game.font, text);
        float textX = button.x + button.width / 2 - layout.width / 2;
        float textY = button.y + button.height / 2 + layout.height / 2;
        
        game.font.draw(game.batch, text, textX, textY);
    }
    
    private void drawPuzzleBoard() {
        // Draw all tiles without unnecessary borders
        for (int y = 0; y < PuzzleBoard.BOARD_SIZE; y++) {
            for (int x = 0; x < PuzzleBoard.BOARD_SIZE; x++) {
                Tile tile = puzzleBoard.getTile(x, y);
                
                if (tile != null && !tile.isEmpty()) {
                    float renderX = tile.getRenderPosition().x;
                    float renderY = tile.getRenderPosition().y;
                    
                    game.batch.draw(tile.getTextureRegion(), renderX, renderY, tileSize, tileSize);
                }
            }
        }
    }
    
    
    private void drawWinMessage() {
        String winText = "Congratulations!";
        String subText = "Puzzle Solved!";
        
        com.badlogic.gdx.graphics.g2d.GlyphLayout winLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(game.font, winText);
        com.badlogic.gdx.graphics.g2d.GlyphLayout subLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(game.font, subText);
        
        float winTextX = winMessageArea.x + winMessageArea.width / 2 - winLayout.width / 2;
        float winTextY = winMessageArea.y + winMessageArea.height / 2 + 20;
        
        float subTextX = winMessageArea.x + winMessageArea.width / 2 - subLayout.width / 2;
        float subTextY = winTextY - 30;
        
        game.font.draw(game.batch, winText, winTextX, winTextY);
        game.font.draw(game.batch, subText, subTextX, subTextY);
        
        if (winMessageTimer > 2f) {
            game.font.draw(game.batch, "Click Reset for new game", subTextX - 20, subTextY - 30);
        }
    }
    
    private void handleInput() {
        if (Gdx.input.justTouched()) {
            float touchX = Gdx.input.getX();
            float touchY = TileShiftGame.SCREEN_HEIGHT - Gdx.input.getY(); // Flip Y coordinate
            
            // Check button clicks
            if (backButton.contains(touchX, touchY)) {
                // Go back to menu
                game.setScreen(new MenuScreen(game));
                return;
            }
            
            if (resetButton.contains(touchX, touchY)) {
                // Reset the puzzle
                puzzleBoard.reset();
                showWinMessage = false;
                winMessageTimer = 0f;
                updateTileRenderPositions();
                return;
            }
            
            // Check tile clicks (only if not animating)
            if (!puzzleBoard.isAnimating()) {
                handleTileClick(touchX, touchY);
            }
        }
    }
    
    private void handleTileClick(float touchX, float touchY) {
        // Convert touch coordinates to grid coordinates
        if (touchX >= boardStartX && touchX < boardStartX + tileSize * PuzzleBoard.BOARD_SIZE &&
            touchY >= boardStartY && touchY < boardStartY + tileSize * PuzzleBoard.BOARD_SIZE) {
            
            int gridX = (int) ((touchX - boardStartX) / tileSize);
            int gridY = (int) ((touchY - boardStartY) / tileSize);
            
            // Attempt to move the tile
            if (puzzleBoard.moveTile(gridX, gridY)) {
                // Tile moved successfully, update all tile render positions
                updateTileRenderPositions();
            }
        }
    }
    

    @Override
    public void resize(int width, int height) {
        // Recalculate layout if screen is resized
        calculateBoardLayout();
    }
    
    @Override
    public void pause() {
        // Handle pause
    }
    
    @Override
    public void resume() {
        // Handle resume
    }
    
    @Override
    public void hide() {
        // Called when this screen is no longer the current screen
    }
    
    @Override
    public void dispose() {
        // Dispose of resources
        if (puzzleTexture != null) {
            puzzleTexture.dispose();
        }
    }
}
