package com.tileshifter.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer; // New Import
import com.badlogic.gdx.graphics.Color; // New Import
import com.tileshifter.GameMode;
import com.tileshifter.TileShiftGame;

/**
 * Screen for selecting puzzle images after choosing a game mode
 */
public class ImageSelectionScreen implements Screen {
    private TileShiftGame game;
    private GameMode gameMode;
    private Array<String> imageFiles;
    private Array<Texture> thumbnails;
    private Array<Rectangle> imageButtons;
    private Rectangle backButton;

    private ShapeRenderer shapeRenderer; // For drawing 3D button effects
    private Texture solidBackgroundTexture; // Texture for solid button backgrounds
    private Rectangle pressedButton = null; // To track the currently pressed button
    private float pressTimer = 0f;
    private Rectangle hoveredButton = null; // To track the currently hovered button
    
    private static final float BUTTON_DEPTH = 8f; // Depth for 3D effect, slightly smaller than main menu
    private static final float PRESS_ANIMATION_DURATION = 0.1f;
    private static final float BUTTON_TOP_MARGIN = 30f; // New: Margin from the top for buttons
    
    private static final float THUMBNAIL_SIZE = 120f;
    private static final float PADDING = 20f;
    
    public ImageSelectionScreen(TileShiftGame game, GameMode gameMode) {
        this.game = game;
        this.gameMode = gameMode;

        // Initialize ShapeRenderer and solidBackgroundTexture
        shapeRenderer = new ShapeRenderer();
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        solidBackgroundTexture = new Texture(pixmap);
        pixmap.dispose();

        loadImageFiles();
        createThumbnails();
        setupButtons();
    }
    
    private void loadImageFiles() {
        imageFiles = new Array<>();
        
        // Get all JPG files from assets/images
        FileHandle imagesDir = Gdx.files.internal("assets/images");
        System.out.println("Images directory path: " + imagesDir.file().getAbsolutePath());
        if (imagesDir.exists()) {
            for (FileHandle file : imagesDir.list()) {
                if ((file.extension().equalsIgnoreCase("jpg") || 
                     file.extension().equalsIgnoreCase("jpeg")) && 
                    !file.name().equalsIgnoreCase("Mytholore.jpg")) { // Exclude Mytholore.jpg
                    imageFiles.add(file.name());
                }
            }
        }
    }
    
    private void createThumbnails() {
        thumbnails = new Array<>();
        
        for (String filename : imageFiles) {
            try {
                Texture texture = new Texture(Gdx.files.internal("assets/images/" + filename));
                thumbnails.add(texture);
            } catch (Exception e) {
                Gdx.app.error("ImageSelectionScreen", "Failed to load image: " + filename + " (Full path: " + Gdx.files.internal("assets/images/" + filename).path() + ")", e);
            }
        }
    }
    
    private void setupButtons() {
        imageButtons = new Array<>();
        
        // Back button (positioned higher and aligned with GameScreen back button)
        backButton = new Rectangle(50f, TileShiftGame.VIRTUAL_HEIGHT - BUTTON_TOP_MARGIN - 50, 120f, 50f); // Adjusted X to 50f, width to 120f, height to 50f
        
        float startX = 50f;
        float startY = TileShiftGame.VIRTUAL_HEIGHT - 150 - THUMBNAIL_SIZE; // Adjusted to be lower and clear of titles/back button
        float currentX = startX;
        float currentY = startY;
        
        int imagesPerRow = (int) ((TileShiftGame.VIRTUAL_WIDTH - 100f) / (THUMBNAIL_SIZE + PADDING));
        
        for (int i = 0; i < imageFiles.size; i++) {
            Rectangle button = new Rectangle(currentX, currentY, THUMBNAIL_SIZE, THUMBNAIL_SIZE);
            imageButtons.add(button);
            
            currentX += THUMBNAIL_SIZE + PADDING;
            
            if ((i + 1) % imagesPerRow == 0) {
                currentX = startX;
                currentY -= THUMBNAIL_SIZE + PADDING;
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
        }

        // Clear screen
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.3f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Set up ShapeRenderer for 3D button drawing (sides and depth)
        shapeRenderer.setProjectionMatrix(game.camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Draw back button (3D effect drawn by ShapeRenderer)
        drawBackButton(backButton, "Back");

        shapeRenderer.end(); // End ShapeRenderer batch
        
        game.batch.begin(); // Start SpriteBatch for text and thumbnails rendering

        // Draw back button text (over the 3D button)
        drawBackButtonText(backButton, "Back");

        // Draw title with game mode (positioned higher)
        String modeText = gameMode == GameMode.CLASSIC ? "Classic Mode" :
                         gameMode == GameMode.ROTATE ? "Rotate Mode" : "Shift Mode";
        game.font.draw(game.batch, "Tile Shifter Puzzle - " + modeText, 190, TileShiftGame.VIRTUAL_HEIGHT - 30); // Adjusted X to avoid back button
        
        // Draw subtitle (positioned higher, below title)
        game.font.draw(game.batch, "Select an image to start:", 190, TileShiftGame.VIRTUAL_HEIGHT - 80); // Adjusted X to avoid back button
        
        // Draw thumbnails
        for (int i = 0; i < thumbnails.size; i++) {
            Texture thumbnail = thumbnails.get(i);
            Rectangle button = imageButtons.get(i);
            
            // Calculate aspect ratio to maintain proportions
            float aspectRatio = (float) thumbnail.getWidth() / thumbnail.getHeight();
            float drawWidth, drawHeight;
            
            if (aspectRatio > 1) {
                drawWidth = THUMBNAIL_SIZE;
                drawHeight = THUMBNAIL_SIZE / aspectRatio;
            } else {
                drawWidth = THUMBNAIL_SIZE * aspectRatio;
                drawHeight = THUMBNAIL_SIZE;
            }
            
            // Center the image in the button area
            float drawX = button.x + (THUMBNAIL_SIZE - drawWidth) / 2;
            float drawY = button.y + (THUMBNAIL_SIZE - drawHeight) / 2;
            
            game.batch.draw(thumbnail, drawX, drawY, drawWidth, drawHeight);
        }
        
        game.batch.end();
        
        // Handle input
        handleInput();
    }
    
    private void drawBackButton(Rectangle button, String text) {
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

        // Draw the bottom/back face (darkest) - this creates the deepest shadow
        shapeRenderer.setColor(0.05f, 0.1f, 0.15f, 1.0f); // Very dark color for the deepest part
        shapeRenderer.rect(currentButtonX + currentButtonDepth, currentButtonY - currentButtonDepth, currentButtonWidth, currentButtonHeight);

        // Draw the right side face
        shapeRenderer.setColor(0.15f, 0.25f, 0.35f, 1.0f); // Darker side color
        shapeRenderer.rect(currentButtonX + currentButtonWidth, currentButtonY - currentButtonDepth, currentButtonDepth, currentButtonHeight + currentButtonDepth);

        // Draw the bottom side face
        shapeRenderer.setColor(0.2f, 0.3f, 0.4f, 1.0f); // Slightly lighter than right for bottom side
        shapeRenderer.rect(currentButtonX, currentButtonY - currentButtonDepth, currentButtonWidth + currentButtonDepth, currentButtonDepth);

        // Draw the top face (main button color)
        float topFaceColorFactor = 1.0f;
        if (button == hoveredButton && pressedButton == null) {
            topFaceColorFactor = 1.3f; // Brighter on hover
        }
        shapeRenderer.setColor(0.3f * topFaceColorFactor, 0.4f * topFaceColorFactor, 0.5f * topFaceColorFactor, 1.0f);
        shapeRenderer.rect(currentButtonX, currentButtonY, currentButtonWidth, currentButtonHeight);
    }

    private void drawBackButtonText(Rectangle button, String text) {
        float currentButtonX = button.x;
        float currentButtonY = button.y;
        float currentButtonWidth = button.width;
        float currentButtonHeight = button.height;
        float currentButtonDepth = BUTTON_DEPTH; // Consistent with drawBackButton for text alignment

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

    private void handleInput() {
        if (Gdx.input.justTouched()) {
            // Unproject touch coordinates to world coordinates
            Vector3 touchPoint = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            game.viewport.unproject(touchPoint);

            float touchX = touchPoint.x;
            float touchY = touchPoint.y;
            
            // Check back button
            if (backButton.contains(touchX, touchY)) {
                pressedButton = backButton; // Set pressed button for animation
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        game.setScreen(new ModeSelectionScreen(game));
                    }
                });
                return;
            }
            
            // Check image selection
            for (int i = 0; i < imageButtons.size; i++) {
                Rectangle button = imageButtons.get(i);
                if (button.contains(touchX, touchY)) {
                    // Start game with selected image and mode
                    String selectedImage = imageFiles.get(i);
                    game.setScreen(new GameScreen(game, selectedImage, gameMode));
                    break;
                }
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
        for (Texture thumbnail : thumbnails) {
            thumbnail.dispose();
        }
        if (solidBackgroundTexture != null) {
            solidBackgroundTexture.dispose();
        }
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
    }
}

