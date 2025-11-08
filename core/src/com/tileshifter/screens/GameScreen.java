package com.tileshifter.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer; // New Import
import com.tileshifter.GameMode;
import com.tileshifter.PuzzleBoard;
import com.tileshifter.RotatePuzzleBoard;
import com.tileshifter.ShiftPuzzleBoard;
import com.tileshifter.Tile;
import com.tileshifter.TileShiftGame;

/**
 * Main game screen where the puzzle is played
 */
public class GameScreen implements Screen {
    private TileShiftGame game;
    private GameMode gameMode;
    private PuzzleBoard puzzleBoard;
    private Texture puzzleTexture;
    private Texture brandLogo; // To display Mytholore.jpg on instructions screen
    
    // UI elements
    private Rectangle backButton;
    private Rectangle resetButton;
    private Rectangle helpButton; // New: Button to show full image
    private Rectangle instructionsButton; // New: Button to show instructions
    private Rectangle winMessageArea;
    
    // Shift mode UI elements (arrow buttons for columns/rows)
    private Rectangle[] columnUpButtons;
    private Rectangle[] columnDownButtons;
    private Rectangle[] rowLeftButtons;
    private Rectangle[] rowRightButtons;

    // Rotate mode UI elements (circular buttons for 2x2 sub-board rotations)
    private Rectangle[] rotateButtons; // 5 buttons for the 5 sub-boards
    private Texture circleButtonTexture; // Texture for circular buttons
    private Texture rotationIconTexture; // New: Texture for rotation icon

    // New 3D button and animation fields
    private ShapeRenderer shapeRenderer; // For drawing 3D button effects
    private Texture solidBackgroundTexture; // For general solid backgrounds
    private Rectangle pressedButton = null;
    private float pressTimer = 0f;
    private Rectangle hoveredButton = null; // New: To track the currently hovered button

    private static final float BUTTON_DEPTH = 8f; // Depth for 3D effect
    private static final float PRESS_ANIMATION_DURATION = 0.1f;
    private static final float BUTTON_TOP_MARGIN = 30f; // New: Consistent top margin for buttons

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
    
    public GameScreen(TileShiftGame game, String imageName, GameMode gameMode) {
        this.game = game;
        this.gameMode = gameMode;
        
        // Load the puzzle image
        puzzleTexture = new Texture(Gdx.files.internal("assets/images/" + imageName));

        // Load the brand logo for the instructions screen
        brandLogo = new Texture(Gdx.files.internal("assets/images/Mytholore.jpg"));
        
        // Initialize ShapeRenderer and solidBackgroundTexture
        shapeRenderer = new ShapeRenderer();
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        solidBackgroundTexture = new Texture(pixmap);
        pixmap.dispose();

        // Initialize puzzle board based on game mode
        switch (gameMode) {
            case ROTATE:
                puzzleBoard = new RotatePuzzleBoard();
                break;
            case SHIFT:
                puzzleBoard = new ShiftPuzzleBoard();
                break;
            case CLASSIC:
            default:
                puzzleBoard = new PuzzleBoard();
                break;
        }
        puzzleBoard.initializeBoard(puzzleTexture);
        
        // Create circular button texture for Rotate mode
        com.badlogic.gdx.graphics.Pixmap circlePixmap = new com.badlogic.gdx.graphics.Pixmap(64, 64, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        circlePixmap.setColor(0.7f, 0.7f, 0.7f, 0.9f); // Brighter, more opaque grey
        circlePixmap.fillCircle(32, 32, 30);
        circleButtonTexture = new Texture(circlePixmap);
        circlePixmap.dispose();

        // Create rotation icon texture
        com.badlogic.gdx.graphics.Pixmap rotationIconPixmap = new com.badlogic.gdx.graphics.Pixmap(32, 32, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        rotationIconPixmap.setColor(Color.YELLOW); // Bright yellow for visibility
        // Draw a simple clockwise arrow
        rotationIconPixmap.fillTriangle(16, 2, 22, 10, 10, 10); // Arrowhead
        rotationIconPixmap.fillRectangle(14, 10, 4, 10); // Arrow shaft
        rotationIconPixmap.fillCircle(16, 24, 6); // Base of arrow
        rotationIconTexture = new Texture(rotationIconPixmap);
        rotationIconPixmap.dispose();
        
        setupUI();
        calculateBoardLayout();
    }
    
    private void setupUI() {
        // Common button dimensions for main UI buttons
        float uiButtonWidth = 120; // Increased width for better touch target
        float uiButtonHeight = 50; // Increased height
        float uiButtonSpacing = 10;
        // Adjusted Y-coordinate for main UI buttons to be consistent and avoid overlap
        float uiButtonY = TileShiftGame.VIRTUAL_HEIGHT - BUTTON_TOP_MARGIN - uiButtonHeight; // Adjusted to be higher

        // Back button (top-left)
        backButton = new Rectangle(BOARD_PADDING, uiButtonY, uiButtonWidth, uiButtonHeight);
        
        // Reset button (top-right)
        resetButton = new Rectangle(TileShiftGame.VIRTUAL_WIDTH - BOARD_PADDING - uiButtonWidth, uiButtonY, uiButtonWidth, uiButtonHeight);

        // Help button (show full image) - next to reset
        helpButton = new Rectangle(TileShiftGame.VIRTUAL_WIDTH - BOARD_PADDING - 2 * uiButtonWidth - uiButtonSpacing, uiButtonY, uiButtonWidth, uiButtonHeight);

        // Instructions button - next to help
        instructionsButton = new Rectangle(TileShiftGame.VIRTUAL_WIDTH - BOARD_PADDING - 3 * uiButtonWidth - 2 * uiButtonSpacing, uiButtonY, uiButtonWidth, uiButtonHeight);
        
        // Win message area (center)
        winMessageArea = new Rectangle(
            TileShiftGame.VIRTUAL_WIDTH / 2 - 150, 
            TileShiftGame.VIRTUAL_HEIGHT / 2 - 50, 
            300, 100
        );
        
        // Initialize shift mode buttons (will be positioned in calculateBoardLayout)
        if (gameMode == GameMode.SHIFT) {
            columnUpButtons = new Rectangle[PuzzleBoard.BOARD_SIZE];
            columnDownButtons = new Rectangle[PuzzleBoard.BOARD_SIZE];
            rowLeftButtons = new Rectangle[PuzzleBoard.BOARD_SIZE];
            rowRightButtons = new Rectangle[PuzzleBoard.BOARD_SIZE];
        } else if (gameMode == GameMode.ROTATE) {
            rotateButtons = new Rectangle[5]; // 5 sub-boards
        }
    }
    
    private void calculateBoardLayout() {
        // Calculate board size to fit in screen with padding
        float availableWidth = TileShiftGame.VIRTUAL_WIDTH - 2 * BOARD_PADDING;
        float availableHeight = TileShiftGame.VIRTUAL_HEIGHT - 2 * BOARD_PADDING - 80; // Extra space for UI
        
        // Reserve extra space for shift mode buttons
        if (gameMode == GameMode.SHIFT) {
            availableWidth -= 80; // Space for left/right buttons
            availableHeight -= 80; // Space for up/down buttons
        }
        
        float maxBoardSize = Math.min(availableWidth, availableHeight);
        tileSize = maxBoardSize / PuzzleBoard.BOARD_SIZE;
        
        // Center the board
        float boardSize = tileSize * PuzzleBoard.BOARD_SIZE;
        boardStartX = (TileShiftGame.VIRTUAL_WIDTH - boardSize) / 2;
        boardStartY = (TileShiftGame.VIRTUAL_HEIGHT - boardSize) / 2 - 20; // Slightly lower for UI
        
        // Position shift mode arrow buttons
        if (gameMode == GameMode.SHIFT) {
            float buttonSize = 30f;
            
            // Column up/down buttons
            for (int col = 0; col < PuzzleBoard.BOARD_SIZE; col++) {
                float centerX = boardStartX + col * tileSize + tileSize / 2 - buttonSize / 2;
                
                columnUpButtons[col] = new Rectangle(
                    centerX,
                    boardStartY + boardSize + 10,
                    buttonSize,
                    buttonSize
                );
                
                columnDownButtons[col] = new Rectangle(
                    centerX,
                    boardStartY - buttonSize - 10,
                    buttonSize,
                    buttonSize
                );
            }
            
            // Row left/right buttons
            for (int row = 0; row < PuzzleBoard.BOARD_SIZE; row++) {
                float centerY = boardStartY + row * tileSize + tileSize / 2 - buttonSize / 2;
                
                rowLeftButtons[row] = new Rectangle(
                    boardStartX - buttonSize - 10,
                    centerY,
                    buttonSize,
                    buttonSize
                );
                
                rowRightButtons[row] = new Rectangle(
                    boardStartX + boardSize + 10,
                    centerY,
                    buttonSize,
                    buttonSize
                );
            }
        } else if (gameMode == GameMode.ROTATE) {
            // Define the 5 sub-board starting grid positions (MUST MATCH RotatePuzzleBoard.SUBBOARD_POSITIONS order)
            int[][] subBoardGridPositions = {
                {0, 0}, // Index 0: Top-left
                {0, 2}, // Index 1: Bottom-left
                {2, 0}, // Index 2: Top-right
                {2, 2}, // Index 3: Bottom-right
                {1, 1}  // Index 4: Center
            };
            
            float buttonSize = 30f; // Made smaller for better visibility
            
            for (int i = 0; i < subBoardGridPositions.length; i++) {
                int gridX = subBoardGridPositions[i][0];
                int gridY = subBoardGridPositions[i][1];

                // Calculate center of the 2x2 sub-board
                float subBoardCenterX = boardStartX + gridX * tileSize + tileSize;
                float subBoardCenterY = boardStartY + gridY * tileSize + tileSize;

                // Position button centered within the sub-board's 2x2 area
                float buttonX = subBoardCenterX - buttonSize / 2;
                float buttonY = subBoardCenterY - buttonSize / 2;
                
                rotateButtons[i] = new Rectangle(
                    buttonX,
                    buttonY,
                    buttonSize,
                    buttonSize
                );
            }
        }
        
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
        // Update press animation timer
        if (pressedButton != null) {
            pressTimer += delta;
            if (pressTimer >= PRESS_ANIMATION_DURATION) {
                pressedButton = null;
                pressTimer = 0f;
            }
        }

        // Update hovered button
        Vector3 mouseCoords = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        game.viewport.unproject(mouseCoords);
        float mouseX = mouseCoords.x;
        float mouseY = mouseCoords.y;

        hoveredButton = null; // Reset hovered button
        if (backButton.contains(mouseX, mouseY)) {
            hoveredButton = backButton;
        } else if (resetButton.contains(mouseX, mouseY)) {
            hoveredButton = resetButton;
        } else if (helpButton.contains(mouseX, mouseY)) {
            hoveredButton = helpButton;
        } else if (instructionsButton.contains(mouseX, mouseY)) {
            hoveredButton = instructionsButton;
        }

        // Clear screen
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Set up ShapeRenderer for 3D button drawing (sides and depth)
        shapeRenderer.setProjectionMatrix(game.camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Draw main UI buttons (3D effect drawn by ShapeRenderer)
        drawMainUIButton(backButton, "Back");
        drawMainUIButton(resetButton, "Reset");
        drawMainUIButton(helpButton, "Help");
        drawMainUIButton(instructionsButton, "Inst.");

        shapeRenderer.end(); // End ShapeRenderer batch
        
        game.batch.begin(); // Start SpriteBatch for text and textures rendering
        
        // Draw UI button texts (over the 3D buttons)
        drawMainUIButtonText(backButton, "Back");
        drawMainUIButtonText(resetButton, "Reset");
        drawMainUIButtonText(helpButton, "Help");
        drawMainUIButtonText(instructionsButton, "Inst.");
        
        // Draw shift mode arrow buttons (these don't have the full 3D effect yet)
        if (gameMode == GameMode.SHIFT) {
            drawShiftButtons();
        }
        
        // Draw puzzle board first
        drawPuzzleBoard();

        // Then draw rotate mode buttons on top
        if (gameMode == GameMode.ROTATE) {
            game.batch.setColor(Color.WHITE); // Ensure full color and opacity
            drawRotateButtons();
        }
        
        // Draw win message if needed
        if (showWinMessage) {
            drawWinMessage();
        }
        
        // End the main game.batch here, before any overlays are drawn, so overlays can manage their own batches.
        game.batch.end(); 

        // New: Draw full image or instructions overlay
        if (showingFullImage) {
            drawFullImageOverlay();
        } else if (showingInstructions) {
            drawInstructionsOverlay();
        }
        
        // Handle input (only if no overlay is active)
        if (!showingFullImage && !showingInstructions) {
            handleInput();
        } else {
            handleOverlayInput(); // New input handler for overlays
        }
    }
    
    private void drawButton(Rectangle button, String text) {
        // This method will no longer be used for main UI buttons, but might be for shift/rotate arrows
        // Draw button text centered without border artifacts
        com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(game.font, text);
        float textX = button.x + button.width / 2 - layout.width / 2;
        float textY = button.y + button.height / 2 + layout.height / 2;
        
        game.font.draw(game.batch, text, textX, textY);
    }

    // New: Draws a 3D button structure with hover/press animation
    private void drawMainUIButton(Rectangle button, String text) {
        float currentButtonX = button.x;
        float currentButtonY = button.y;
        float currentButtonWidth = button.width;
        float currentButtonHeight = button.height;
        float currentButtonDepth = BUTTON_DEPTH;

        // Apply hover effect
        if (button == hoveredButton && pressedButton == null) {
            float hoverScale = 1.05f; // Slightly larger on hover
            currentButtonWidth = button.width * hoverScale;
            currentButtonHeight = button.height * hoverScale;
            currentButtonX = button.x + (button.width - currentButtonWidth) / 2;
            currentButtonY = button.y + (button.height - currentButtonHeight) / 2;
        }

        // Apply press animation if this button is currently pressed
        if (button == pressedButton) {
            float scale = 1.0f - (0.1f * (pressTimer / PRESS_ANIMATION_DURATION)); // Scale down by up to 10% (more noticeable)
            currentButtonWidth = button.width * scale;
            currentButtonHeight = button.height * scale;
            currentButtonX = button.x + (button.width - currentButtonWidth) / 2;
            currentButtonY = button.y + (button.height - currentButtonHeight) / 2;
            currentButtonDepth = BUTTON_DEPTH * (1.0f - (0.9f * (pressTimer / PRESS_ANIMATION_DURATION))); // Scale depth to 10% (very significant)
            currentButtonY -= (BUTTON_DEPTH - currentButtonDepth); // Shift down to simulate pressing
        }

        // Draw the bottom/back face (darkest)
        shapeRenderer.setColor(0.05f, 0.1f, 0.15f, 1.0f); // Very dark color
        shapeRenderer.rect(currentButtonX + currentButtonDepth, currentButtonY - currentButtonDepth, currentButtonWidth, currentButtonHeight);

        // Draw the right side face
        shapeRenderer.setColor(0.15f, 0.25f, 0.35f, 1.0f); // Darker side color
        shapeRenderer.rect(currentButtonX + currentButtonWidth, currentButtonY - currentButtonDepth, currentButtonDepth, currentButtonHeight + currentButtonDepth);

        // Draw the bottom side face
        shapeRenderer.setColor(0.2f, 0.3f, 0.4f, 1.0f); // Slightly lighter for bottom side
        shapeRenderer.rect(currentButtonX, currentButtonY - currentButtonDepth, currentButtonWidth + currentButtonDepth, currentButtonDepth);

        // Draw the top face (main button color)
        float topFaceColorFactor = 1.0f;
        if (button == hoveredButton && pressedButton == null) {
            topFaceColorFactor = 1.3f; // Brighter on hover
        }
        shapeRenderer.setColor(0.3f * topFaceColorFactor, 0.4f * topFaceColorFactor, 0.5f * topFaceColorFactor, 1.0f);
        shapeRenderer.rect(currentButtonX, currentButtonY, currentButtonWidth, currentButtonHeight);
    }

    // New: Draws text for a 3D button with hover/press animation
    private void drawMainUIButtonText(Rectangle button, String text) {
        float currentButtonX = button.x;
        float currentButtonY = button.y;
        float currentButtonWidth = button.width;
        float currentButtonHeight = button.height;
        float currentButtonDepth = BUTTON_DEPTH; // Consistent for text alignment

        // Apply hover effect
        if (button == hoveredButton && pressedButton == null) {
            float hoverScale = 1.05f;
            currentButtonWidth = button.width * hoverScale;
            currentButtonHeight = button.height * hoverScale;
            currentButtonX = button.x + (button.width - currentButtonWidth) / 2;
            currentButtonY = button.y + (button.height - currentButtonHeight) / 2;
        }

        // Apply press animation
        if (button == pressedButton) {
            float scale = 1.0f - (0.1f * (pressTimer / PRESS_ANIMATION_DURATION));
            currentButtonWidth = button.width * scale;
            currentButtonHeight = button.height * scale;
            currentButtonX = button.x + (button.width - currentButtonWidth) / 2;
            currentButtonY = button.y + (button.height - currentButtonHeight) / 2;
            currentButtonDepth = BUTTON_DEPTH * (1.0f - (0.9f * (pressTimer / PRESS_ANIMATION_DURATION)));
            currentButtonY -= (BUTTON_DEPTH - currentButtonDepth); // Shift down
        }

        // Draw text (centered)
        game.font.setColor(1, 1, 1, 1f); // Reset color for text
        com.badlogic.gdx.graphics.g2d.GlyphLayout textLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(game.font, text);
        game.font.draw(game.batch, text,
            currentButtonX + currentButtonWidth / 2 - textLayout.width / 2,
            currentButtonY + currentButtonHeight / 2 + textLayout.height / 2);
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
    
    private void drawShiftButtons() {
        // Draw column up/down buttons
        for (int col = 0; col < PuzzleBoard.BOARD_SIZE; col++) {
            drawButton(columnUpButtons[col], "^");
            drawButton(columnDownButtons[col], "v");
        }
        
        // Draw row left/right buttons
        for (int row = 0; row < PuzzleBoard.BOARD_SIZE; row++) {
            drawButton(rowLeftButtons[row], "<");
            drawButton(rowRightButtons[row], ">");
        }
    }
    
    private void drawRotateButtons() {
        // Draw the 5 circular buttons for rotating sub-boards
        for (int i = 0; i < rotateButtons.length; i++) {
            Rectangle button = rotateButtons[i];

            // Draw the circular texture (background)
            game.batch.draw(circleButtonTexture, button.x, button.y, button.width, button.height);
            
            // Draw the rotation icon centered on the button
            float iconSize = button.width * 0.7f; // Make icon slightly smaller than button
            float iconX = button.x + (button.width - iconSize) / 2;
            float iconY = button.y + (button.height - iconSize) / 2;
            game.batch.draw(rotationIconTexture, iconX, iconY, iconSize, iconSize);
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
                pressedButton = backButton; // Set pressed button for animation
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        game.setScreen(new ImageSelectionScreen(game, gameMode));
                    }
                });
                return;
            }
            
            if (resetButton.contains(touchX, touchY)) {
                pressedButton = resetButton; // Set pressed button for animation
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        puzzleBoard.reset();
                        showWinMessage = false;
                        winMessageTimer = 0f;
                        updateTileRenderPositions();
                    }
                });
                return;
            }

            // New: Handle Help button click
            if (helpButton.contains(touchX, touchY)) {
                pressedButton = helpButton; // Set pressed button for animation
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        showingFullImage = true;
                    }
                });
                return;
            }

            // New: Handle Instructions button click
            if (instructionsButton.contains(touchX, touchY)) {
                pressedButton = instructionsButton; // Set pressed button for animation
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        showingInstructions = true;
                    }
                });
                return;
            }
            
            // Check shift mode arrow button clicks
            if (gameMode == GameMode.SHIFT) {
                if (handleShiftButtonClick(touchX, touchY)) {
                    return;
                }
            } else if (gameMode == GameMode.ROTATE) {
                if (handleRotateButtonClick(touchX, touchY)) {
                    return;
                }
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
    
    private boolean handleShiftButtonClick(float touchX, float touchY) {
        ShiftPuzzleBoard shiftBoard = (ShiftPuzzleBoard) puzzleBoard;
        
        // Check column up buttons
        for (int col = 0; col < PuzzleBoard.BOARD_SIZE; col++) {
            if (columnUpButtons[col].contains(touchX, touchY)) {
                shiftBoard.shiftColumnUp(col);
                updateTileRenderPositions();
                return true;
            }
        }
        
        // Check column down buttons
        for (int col = 0; col < PuzzleBoard.BOARD_SIZE; col++) {
            if (columnDownButtons[col].contains(touchX, touchY)) {
                shiftBoard.shiftColumnDown(col);
                updateTileRenderPositions();
                return true;
            }
        }
        
        // Check row left buttons
        for (int row = 0; row < PuzzleBoard.BOARD_SIZE; row++) {
            if (rowLeftButtons[row].contains(touchX, touchY)) {
                shiftBoard.shiftRowLeft(row);
                updateTileRenderPositions();
                return true;
            }
        }
        
        // Check row right buttons
        for (int row = 0; row < PuzzleBoard.BOARD_SIZE; row++) {
            if (rowRightButtons[row].contains(touchX, touchY)) {
                shiftBoard.shiftRowRight(row);
                updateTileRenderPositions();
                return true;
            }
        }
        
        return false;
    }
    
    private boolean handleRotateButtonClick(float touchX, float touchY) {
        RotatePuzzleBoard rotateBoard = (RotatePuzzleBoard) puzzleBoard;

        for (int i = 0; i < rotateButtons.length; i++) {
            if (rotateButtons[i].contains(touchX, touchY)) {
                rotateBoard.rotateSubBoard(i);
                updateTileRenderPositions();
                return true;
            }
        }
        return false;
    }
    
    private void handleTileClick(float touchX, float touchY) {
        // Convert touch coordinates to grid coordinates
        if (touchX >= boardStartX && touchX < boardStartX + tileSize * PuzzleBoard.BOARD_SIZE &&
            touchY >= boardStartY && touchY < boardStartY + tileSize * PuzzleBoard.BOARD_SIZE) {
            
            int gridX = (int) ((touchX - boardStartX) / tileSize);
            int gridY = (int) ((touchY - boardStartY) / tileSize);
            
            if (gameMode == GameMode.CLASSIC) { // Only classic mode uses tile sliding
                // Classic mode: attempt to move the tile
                if (puzzleBoard.moveTile(gridX, gridY)) {
                    // Tile moved successfully, update all tile render positions
                    updateTileRenderPositions();
                }
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
        if (circleButtonTexture != null) {
            circleButtonTexture.dispose();
        }
        if (rotationIconTexture != null) {
            rotationIconTexture.dispose();
        }
        if (solidBackgroundTexture != null) {
            solidBackgroundTexture.dispose();
        }
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
    }

    // New: Draws the full puzzle image as an overlay
    private void drawFullImageOverlay() {
        game.batch.begin(); // Start batch for overlay
        game.batch.setProjectionMatrix(game.camera.combined); // Ensure correct projection for SpriteBatch
        game.batch.setColor(1, 1, 1, 0.8f); // Slightly transparent overlay
        game.batch.draw(puzzleTexture, 
            TileShiftGame.VIRTUAL_WIDTH / 2 - puzzleTexture.getWidth() / 2, 
            TileShiftGame.VIRTUAL_HEIGHT / 2 - puzzleTexture.getHeight() / 2,
            puzzleTexture.getWidth(), puzzleTexture.getHeight());
        game.batch.setColor(1, 1, 1, 1f); // Reset color
        game.batch.end(); // End batch for overlay
    }

    // New: Draws game instructions as an overlay
    private void drawInstructionsOverlay() {
        // Draw a solid background behind instructions using ShapeRenderer
        shapeRenderer.setProjectionMatrix(game.camera.combined); // Ensure correct projection
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.8f); // Semi-transparent black background
        shapeRenderer.rect(0, 0, TileShiftGame.VIRTUAL_WIDTH, TileShiftGame.VIRTUAL_HEIGHT);
        shapeRenderer.end();

        game.batch.begin(); // Start batch for overlay logo and text
        game.batch.setProjectionMatrix(game.camera.combined); // Ensure correct projection for SpriteBatch
        // Draw the brand logo above the instructions text
        if (brandLogo != null) {
            float logoWidth = brandLogo.getWidth() / 1.5f; // Scale down for instructions screen
            float logoHeight = brandLogo.getHeight() / 1.5f;
            float logoX = TileShiftGame.VIRTUAL_WIDTH / 2 - logoWidth / 2;
            float logoY = TileShiftGame.VIRTUAL_HEIGHT - logoHeight - 20; // Adjusted logo position higher
            game.batch.draw(brandLogo, logoX, logoY, logoWidth, logoHeight);
        }

        String instructionsText;
        
        if (gameMode == GameMode.ROTATE) {
            instructionsText = "HOW TO PLAY (ROTATE MODE):\n\n" +
                              "1. Click the rotation icon buttons to rotate the adjacent 2x2.\n" +
                              "2. Five overlapping 2x2 sub-boards can be rotated:\n" +
                              "   - Top-Left, Top-Right, Bottom-Left, Bottom-Right, Center\n" +
                              "3. Arrange all tiles to complete the image.\n" +
                              "4. Use \"Reset\" to shuffle for a new game.\n" +
                              "5. Use \"Back\" to return to image selection.";
        } else if (gameMode == GameMode.SHIFT) {
            instructionsText = "HOW TO PLAY (SHIFT MODE):\n\n" +
                              "1. Click arrow buttons to shift entire rows or columns.\n" +
                              "2. ^ and v buttons shift columns up/down (with wrapping).\n" +
                              "3. < and > buttons shift rows left/right (with wrapping).\n" +
                              "4. Arrange all tiles to complete the image.\n" +
                              "5. Use \"Reset\" to shuffle for a new game.\n" +
                              "6. Use \"Back\" to return to image selection.";
        } else {
            instructionsText = "HOW TO PLAY (CLASSIC MODE):\n\n" +
                              "1. Click a tile adjacent to the empty space to move it.\n" +
                              "2. Arrange all tiles to complete the image.\n" +
                              "3. Use \"Reset\" to shuffle for a new game.\n" +
                              "4. Use \"Back\" to return to image selection.\n\n";
        }
        
        // Calculate text position to center it
        com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(game.font, instructionsText);
        float textX = TileShiftGame.VIRTUAL_WIDTH / 2 - layout.width / 2;
        float textY = TileShiftGame.VIRTUAL_HEIGHT - 200 - layout.height / 2; // Adjusted text position lower
        
        game.font.draw(game.batch, instructionsText, textX, textY);
        game.batch.end(); // End batch for overlay
    }
    
}
