package com.tileshifter.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
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
    private Texture brandLogo; // New: To display Mytholore.jpg on instructions screen
    
    // UI elements
    private Rectangle backButton;
    private Rectangle resetButton;
    private Rectangle helpButton; // New: Button to show full image
    private Rectangle instructionsButton; // New: Button to show instructions
    private Rectangle winMessageArea;
    
    // Board rendering properties
    private float boardStartX, boardStartY;
    private float tileSize;
    private static final float BOARD_PADDING = 50f;
    
    // Win state
    private boolean showWinMessage = false;
    private float winMessageTimer = 0f;

    // New: State for showing full image or instructions
    private boolean showingFullImage = false;
    private boolean showingInstructions = false;
    
    public GameScreen(TileShiftGame game, String imageName) {
        this.game = game;
        this.imageName = imageName;
        
        // Load the puzzle image
        puzzleTexture = new Texture(Gdx.files.internal("images/" + imageName));

        // Load the brand logo for the instructions screen
        brandLogo = new Texture(Gdx.files.internal("images/Mytholore.jpg"));
        
        // Initialize puzzle board
        puzzleBoard = new PuzzleBoard();
        puzzleBoard.initializeBoard(puzzleTexture);
        
        setupUI();
        calculateBoardLayout();
    }
    
    private void setupUI() {
        // Back button (top-left)
        backButton = new Rectangle(20, TileShiftGame.VIRTUAL_HEIGHT - 60, 100, 40);
        
        // Reset button (top-right)
        resetButton = new Rectangle(TileShiftGame.VIRTUAL_WIDTH - 120, TileShiftGame.VIRTUAL_HEIGHT - 60, 100, 40);

        // Help button (show full image) - next to reset
        helpButton = new Rectangle(TileShiftGame.VIRTUAL_WIDTH - 240, TileShiftGame.VIRTUAL_HEIGHT - 60, 100, 40);

        // Instructions button - next to help
        instructionsButton = new Rectangle(TileShiftGame.VIRTUAL_WIDTH - 360, TileShiftGame.VIRTUAL_HEIGHT - 60, 100, 40);
        
        // Win message area (center)
        winMessageArea = new Rectangle(
            TileShiftGame.VIRTUAL_WIDTH / 2 - 150, 
            TileShiftGame.VIRTUAL_HEIGHT / 2 - 50, 
            300, 100
        );
    }
    
    private void calculateBoardLayout() {
        // Calculate board size to fit in screen with padding
        float availableWidth = TileShiftGame.VIRTUAL_WIDTH - 2 * BOARD_PADDING;
        float availableHeight = TileShiftGame.VIRTUAL_HEIGHT - 2 * BOARD_PADDING - 80; // Extra space for UI
        
        float maxBoardSize = Math.min(availableWidth, availableHeight);
        tileSize = maxBoardSize / PuzzleBoard.BOARD_SIZE;
        
        // Center the board
        float boardSize = tileSize * PuzzleBoard.BOARD_SIZE;
        boardStartX = (TileShiftGame.VIRTUAL_WIDTH - boardSize) / 2;
        boardStartY = (TileShiftGame.VIRTUAL_HEIGHT - boardSize) / 2 - 20; // Slightly lower for UI
        
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
        drawButton(helpButton, "Help"); // Draw Help button
        drawButton(instructionsButton, "Inst."); // Draw Instructions button
        
        // Draw puzzle board
        drawPuzzleBoard();
        
        // Draw win message if needed
        if (showWinMessage) {
            drawWinMessage();
        }
        
        // Draw game info and instructions
        // game.font.draw(game.batch, "Image: " + imageName, 20, 50); // Removed filename display
        // game.font.draw(game.batch, "Click tiles adjacent to empty space to move them", 20, 30);
        // Removed static instructions at the bottom
        // Show move count or other game stats
        if (!showWinMessage) {
            // game.font.draw(game.batch, "Arrange tiles to complete the image", 20, 10); // Removed final instruction line
        }

        // New: Draw full image or instructions overlay
        if (showingFullImage) {
            drawFullImageOverlay();
        } else if (showingInstructions) {
            drawInstructionsOverlay();
        }
        
        game.batch.end();
        
        // Handle input (only if no overlay is active)
        if (!showingFullImage && !showingInstructions) {
            handleInput();
        } else {
            handleOverlayInput(); // New input handler for overlays
        }
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
            // Unproject touch coordinates to world coordinates
            Vector3 touchPoint = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            game.viewport.unproject(touchPoint);

            float touchX = touchPoint.x;
            float touchY = touchPoint.y;
            
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

            // New: Handle Help button click
            if (helpButton.contains(touchX, touchY)) {
                showingFullImage = true;
                return;
            }

            // New: Handle Instructions button click
            if (instructionsButton.contains(touchX, touchY)) {
                showingInstructions = true;
                return;
            }
            
            // Check tile clicks (only if not animating)
            if (!puzzleBoard.isAnimating()) {
                handleTileClick(touchX, touchY);
            }
        }
    }

    // New: Handles input when an overlay (full image or instructions) is active
    private void handleOverlayInput() {
        if (Gdx.input.justTouched()) {
            showingFullImage = false; // Dismiss full image on any touch
            showingInstructions = false; // Dismiss instructions on any touch
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
        if (brandLogo != null) {
            brandLogo.dispose();
        }
    }

    // New: Draws the full puzzle image as an overlay
    private void drawFullImageOverlay() {
        game.batch.setColor(1, 1, 1, 0.8f); // Slightly transparent overlay
        game.batch.draw(puzzleTexture, 
            TileShiftGame.VIRTUAL_WIDTH / 2 - puzzleTexture.getWidth() / 2, 
            TileShiftGame.VIRTUAL_HEIGHT / 2 - puzzleTexture.getHeight() / 2,
            puzzleTexture.getWidth(), puzzleTexture.getHeight());
        game.batch.setColor(1, 1, 1, 1f); // Reset color
    }

    // New: Draws game instructions as an overlay
    private void drawInstructionsOverlay() {
        // Draw a solid background behind instructions
        game.batch.setColor(0f, 0f, 0f, 1.0f); // Solid black background
        // Create a 1x1 white texture for drawing colored rectangles
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        pixmap.fill();
        com.badlogic.gdx.graphics.Texture backgroundTexture = new com.badlogic.gdx.graphics.Texture(pixmap);
        pixmap.dispose();

        game.batch.draw(backgroundTexture, 0, 0, TileShiftGame.VIRTUAL_WIDTH, TileShiftGame.VIRTUAL_HEIGHT);
        game.batch.setColor(1, 1, 1, 1f); // Reset color for drawing text
        backgroundTexture.dispose(); // Dispose after drawing

        // Draw the brand logo above the instructions text
        if (brandLogo != null) {
            float logoWidth = brandLogo.getWidth() / 1.5f; // Scale down for instructions screen
            float logoHeight = brandLogo.getHeight() / 1.5f;
            float logoX = TileShiftGame.VIRTUAL_WIDTH / 2 - logoWidth / 2;
            float logoY = TileShiftGame.VIRTUAL_HEIGHT - logoHeight - 20; // Adjusted logo position higher
            game.batch.draw(brandLogo, logoX, logoY, logoWidth, logoHeight);
        }

        String instructionsText = "HOW TO PLAY:\n\n" +
                                  "1. Click a tile adjacent to the empty space to move it.\n" +
                                  "2. Arrange all tiles to complete the image.\n" +
                                  "3. Use \"Reset\" to shuffle for a new game.\n" +
                                  "4. Use \"Back\" to return to image selection.";
        
        // Calculate text position to center it
        com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(game.font, instructionsText);
        float textX = TileShiftGame.VIRTUAL_WIDTH / 2 - layout.width / 2;
        float textY = TileShiftGame.VIRTUAL_HEIGHT - 200 - layout.height / 2; // Adjusted text position lower

        game.font.draw(game.batch, instructionsText, textX, textY);
        game.font.draw(game.batch, "\n\nTap anywhere to close", textX, textY - layout.height - 30);
    }
    
}
