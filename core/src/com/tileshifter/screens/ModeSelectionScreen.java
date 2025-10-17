package com.tileshifter.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer; // New Import
import com.tileshifter.GameMode;
import com.tileshifter.TileShiftGame;

/**
 * Screen for selecting the game mode before choosing an image
 */
public class ModeSelectionScreen implements Screen {
    private TileShiftGame game;
    private Rectangle classicButton;
    private Rectangle rotateButton;
    private Rectangle shiftButton;
    private Texture solidBackgroundTexture; // New: Texture for solid button backgrounds

    // Animation for button clicks
    private Rectangle pressedButton = null;
    private float pressTimer = 0f;
    private static final float PRESS_ANIMATION_DURATION = 0.1f;

    private ShapeRenderer shapeRenderer; // New: For drawing 3D button effects
    private Rectangle hoveredButton = null; // New: To track the currently hovered button
    
    private static final float BUTTON_WIDTH = 300f; // Larger button width
    private static final float BUTTON_HEIGHT = 100f; // Larger button height
    private static final float BUTTON_SPACING = 40f; // Increased spacing
    private static final float BUTTON_DEPTH = 10f; // New: Depth for 3D effect
    
    public ModeSelectionScreen(TileShiftGame game) {
        this.game = game;

        // Create 1x1 white pixel for drawing solid backgrounds
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        pixmap.fill();
        solidBackgroundTexture = new Texture(pixmap);
        pixmap.dispose();

        shapeRenderer = new ShapeRenderer(); // Initialize ShapeRenderer

        setupButtons();
    }
    
    private void setupButtons() {
        float centerX = TileShiftGame.VIRTUAL_WIDTH / 2;
        // Calculate total height of all buttons and spacing
        float totalButtonsHeight = 3 * BUTTON_HEIGHT + 2 * BUTTON_SPACING;
        // Adjusted Y-coordinate for the group of buttons to ensure ample space below the subtitle
        float startY = (TileShiftGame.VIRTUAL_HEIGHT / 2) + (totalButtonsHeight / 2) - BUTTON_HEIGHT - 60; // Adjusted significantly lower
        
        classicButton = new Rectangle(
            centerX - BUTTON_WIDTH / 2,
            startY,
            BUTTON_WIDTH,
            BUTTON_HEIGHT
        );
        
        rotateButton = new Rectangle(
            centerX - BUTTON_WIDTH / 2,
            startY - BUTTON_HEIGHT - BUTTON_SPACING,
            BUTTON_WIDTH,
            BUTTON_HEIGHT
        );
        
        shiftButton = new Rectangle(
            centerX - BUTTON_WIDTH / 2,
            startY - 2 * (BUTTON_HEIGHT + BUTTON_SPACING),
            BUTTON_WIDTH,
            BUTTON_HEIGHT
        );
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
        if (classicButton.contains(mouseX, mouseY)) {
            hoveredButton = classicButton;
        } else if (rotateButton.contains(mouseX, mouseY)) {
            hoveredButton = rotateButton;
        } else if (shiftButton.contains(mouseX, mouseY)) {
            hoveredButton = shiftButton;
        }

        // Clear screen
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.3f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Set up ShapeRenderer for 3D button drawing (sides and depth)
        shapeRenderer.setProjectionMatrix(game.camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Draw buttons with animation effect (3D effect drawn by ShapeRenderer)
        drawModeButton(classicButton, "Classic", "Slide tiles into empty space");
        drawModeButton(rotateButton, "Rotate", "Rotate 2x2 sub-boards");
        drawModeButton(shiftButton, "Shift", "Shift rows and columns");

        shapeRenderer.end();

        game.batch.begin(); // Start SpriteBatch for text rendering
        
        // Draw title
        String title = "Tile Shifter Puzzle";
        com.badlogic.gdx.graphics.g2d.GlyphLayout titleLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(game.font, title);
        game.font.draw(game.batch, title, 
            TileShiftGame.VIRTUAL_WIDTH / 2 - titleLayout.width / 2, 
            TileShiftGame.VIRTUAL_HEIGHT - 30); // Positioned high enough
        
        // Draw subtitle
        String subtitle = "Select Game Mode:";
        com.badlogic.gdx.graphics.g2d.GlyphLayout subtitleLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(game.font, subtitle);
        game.font.draw(game.batch, subtitle, 
            TileShiftGame.VIRTUAL_WIDTH / 2 - subtitleLayout.width / 2, 
            TileShiftGame.VIRTUAL_HEIGHT - 100); // Positioned clearly below title
        
        // Redraw button titles and descriptions over the 3D buttons (within SpriteBatch)
        drawButtonText(classicButton, "Classic", "Slide tiles into empty space");
        drawButtonText(rotateButton, "Rotate", "Rotate 2x2 sub-boards");
        drawButtonText(shiftButton, "Shift", "Shift rows and columns");
        
        game.batch.end();
        
        // Handle input
        handleInput();
    }
    
    private void drawModeButton(Rectangle button, String title, String description) {
        float currentButtonX = button.x;
        float currentButtonY = button.y;
        float currentButtonWidth = button.width;
        float currentButtonHeight = button.height;
        float currentButtonDepth = BUTTON_DEPTH;

        // Apply hover effect
        if (button == hoveredButton && pressedButton == null) {
            // Slightly scale up or change color on hover
            float hoverScale = 1.02f; // 2% larger
            currentButtonWidth = button.width * hoverScale;
            currentButtonHeight = button.height * hoverScale;
            currentButtonX = button.x + (button.width - currentButtonWidth) / 2;
            currentButtonY = button.y + (button.height - currentButtonHeight) / 2;
            // No change to depth on hover, only visual cues for top face and text
        }

        // Apply press animation if this button is currently pressed
        if (button == pressedButton) {
            float scale = 1.0f - (0.07f * (pressTimer / PRESS_ANIMATION_DURATION)); // Scale down by up to 7% (more noticeable)
            currentButtonWidth = button.width * scale;
            currentButtonHeight = button.height * scale;
            currentButtonX = button.x + (button.width - currentButtonWidth) / 2;
            currentButtonY = button.y + (button.height - currentButtonHeight) / 2;
            currentButtonDepth = BUTTON_DEPTH * (1.0f - (0.8f * (pressTimer / PRESS_ANIMATION_DURATION))); // Scale depth to 20% (more significant)
            currentButtonY -= (BUTTON_DEPTH - currentButtonDepth); // Shift down to simulate pressing
        }

        // Draw the bottom/back face (darkest) - this creates the deepest shadow
        shapeRenderer.setColor(0.05f, 0.1f, 0.15f, 1.0f); // Even darker color for deepest part
        shapeRenderer.rect(currentButtonX + currentButtonDepth, currentButtonY - currentButtonDepth, currentButtonWidth, currentButtonHeight);

        // Draw the right side face
        shapeRenderer.setColor(0.15f, 0.25f, 0.35f, 1.0f); // Darker side color
        shapeRenderer.rect(currentButtonX + currentButtonWidth, currentButtonY - currentButtonDepth, currentButtonDepth, currentButtonHeight + currentButtonDepth);

        // Draw the bottom side face
        shapeRenderer.setColor(0.2f, 0.3f, 0.4f, 1.0f); // Slightly lighter than right for bottom side
        shapeRenderer.rect(currentButtonX, currentButtonY - currentButtonDepth, currentButtonWidth + currentButtonDepth, currentButtonDepth);

        // Draw the top face (main button color) - this is the interactive part
        float topFaceColorFactor = 1.0f;
        if (button == hoveredButton && pressedButton == null) {
            topFaceColorFactor = 1.2f; // Brighter on hover
        }
        shapeRenderer.setColor(0.3f * topFaceColorFactor, 0.4f * topFaceColorFactor, 0.5f * topFaceColorFactor, 1.0f);
        shapeRenderer.rect(currentButtonX, currentButtonY, currentButtonWidth, currentButtonHeight);

        // Note: Text rendering happens in SpriteBatch, which is started after shapeRenderer.end()
        // So, we'll draw the text in a separate method `drawButtonText`
    }

    private void drawButtonText(Rectangle button, String title, String description) {
        float currentButtonX = button.x;
        float currentButtonY = button.y;
        float currentButtonWidth = button.width;
        float currentButtonHeight = button.height;
        float currentButtonDepth = BUTTON_DEPTH; // This needs to be consistent for text alignment

        // Apply hover effect (for text position)
        if (button == hoveredButton && pressedButton == null) {
            float hoverScale = 1.02f;
            currentButtonWidth = button.width * hoverScale;
            currentButtonHeight = button.height * hoverScale;
            currentButtonX = button.x + (button.width - currentButtonWidth) / 2;
            currentButtonY = button.y + (button.height - currentButtonHeight) / 2;
        }

        if (button == pressedButton) {
            float scale = 1.0f - (0.07f * (pressTimer / PRESS_ANIMATION_DURATION));
            currentButtonWidth = button.width * scale;
            currentButtonHeight = button.height * scale;
            currentButtonX = button.x + (button.width - currentButtonWidth) / 2;
            currentButtonY = button.y + (button.height - currentButtonHeight) / 2;
            currentButtonDepth = BUTTON_DEPTH * (1.0f - (0.8f * (pressTimer / PRESS_ANIMATION_DURATION)));
            currentButtonY -= (BUTTON_DEPTH - currentButtonDepth); // Shift down
        }

        // Draw button title (centered)
        game.font.setColor(1, 1, 1, 1f); // Reset color for text
        com.badlogic.gdx.graphics.g2d.GlyphLayout titleLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(game.font, title);
        game.font.draw(game.batch, title, 
            currentButtonX + currentButtonWidth / 2 - titleLayout.width / 2,
            currentButtonY + currentButtonHeight / 2 + titleLayout.height / 2 + 10); // Adjust Y for spacing with description
        
        // Draw button description (centered, smaller font effect by reducing opacity and size)
        game.font.setColor(0.8f, 0.8f, 0.8f, 1f); // Slightly lighter grey for description
        game.font.getData().setScale(0.7f); // Smaller scale for description BEFORE creating GlyphLayout
        com.badlogic.gdx.graphics.g2d.GlyphLayout descLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(game.font, description);
        game.font.draw(game.batch, description, 
            currentButtonX + currentButtonWidth / 2 - descLayout.width / 2,
            currentButtonY + currentButtonHeight / 2 - descLayout.height / 2 - 10); // Adjust Y for spacing with title
        game.font.getData().setScale(1.0f); // Reset font scale
        game.font.setColor(1, 1, 1, 1f); // Reset font color
    }
    
    private void handleInput() {
        if (Gdx.input.justTouched()) {
            // Unproject touch coordinates to world coordinates
            Vector3 touchPoint = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            game.viewport.unproject(touchPoint);

            float touchX = touchPoint.x;
            float touchY = touchPoint.y;
            
            GameMode selectedMode = null;
            
            if (classicButton.contains(touchX, touchY)) {
                selectedMode = GameMode.CLASSIC;
                pressedButton = classicButton;
            } else if (rotateButton.contains(touchX, touchY)) {
                selectedMode = GameMode.ROTATE;
                pressedButton = rotateButton;
            } else if (shiftButton.contains(touchX, touchY)) {
                selectedMode = GameMode.SHIFT;
                pressedButton = shiftButton;
            }
            
            if (selectedMode != null) {
                // Delay screen change to allow animation to play
                final GameMode finalSelectedMode = selectedMode;
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        game.setScreen(new ImageSelectionScreen(game, finalSelectedMode));
                    }
                });
            }
        }
    }
    
    @Override
    public void resize(int width, int height) {
        // Handled by TileShiftGame's resize method
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
        if (solidBackgroundTexture != null) {
            solidBackgroundTexture.dispose();
        }
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
    }
}

