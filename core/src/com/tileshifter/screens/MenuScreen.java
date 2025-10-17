package com.tileshifter.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.tileshifter.TileShiftGame;

/**
 * Menu screen for selecting puzzle images
 */
public class MenuScreen implements Screen {
    private TileShiftGame game;
    private Array<String> imageFiles;
    private Array<Texture> thumbnails;
    private Array<Rectangle> imageButtons;
    
    private static final float THUMBNAIL_SIZE = 120f;
    private static final float PADDING = 20f;
    
    public MenuScreen(TileShiftGame game) {
        this.game = game;
        loadImageFiles();
        createThumbnails();
        setupButtons();
    }
    
    private void loadImageFiles() {
        imageFiles = new Array<>();
        
        // Get all JPG files from assets/images
        FileHandle imagesDir = Gdx.files.internal("images");
        if (imagesDir.exists()) {
            for (FileHandle file : imagesDir.list()) {
                if (file.extension().equalsIgnoreCase("jpg") || 
                    file.extension().equalsIgnoreCase("jpeg")) {
                    imageFiles.add(file.name());
                }
            }
        }
    }
    
    private void createThumbnails() {
        thumbnails = new Array<>();
        
        for (String filename : imageFiles) {
            try {
                Texture texture = new Texture(Gdx.files.internal("images/" + filename));
                thumbnails.add(texture);
            } catch (Exception e) {
                Gdx.app.error("MenuScreen", "Failed to load image: " + filename, e);
            }
        }
    }
    
    private void setupButtons() {
        imageButtons = new Array<>();
        
        float startX = 50f;
        float startY = TileShiftGame.SCREEN_HEIGHT - 200f; // Move images down to avoid text overlap
        float currentX = startX;
        float currentY = startY;
        
        int imagesPerRow = (int) ((TileShiftGame.SCREEN_WIDTH - 100f) / (THUMBNAIL_SIZE + PADDING));
        
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
        // Clear screen
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.3f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        game.batch.begin();
        
        // Draw title
        game.font.draw(game.batch, "Tile Shifter Puzzle", 50, TileShiftGame.SCREEN_HEIGHT - 50);
        game.font.draw(game.batch, "Select an image to start:", 50, TileShiftGame.SCREEN_HEIGHT - 80);
        
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
            
            // Draw filename below thumbnail
            game.font.draw(game.batch, imageFiles.get(i), button.x, button.y - 10);
        }
        
        game.batch.end();
        
        // Handle input
        handleInput();
    }
    
    private void handleInput() {
        if (Gdx.input.justTouched()) {
            float touchX = Gdx.input.getX();
            float touchY = TileShiftGame.SCREEN_HEIGHT - Gdx.input.getY(); // Flip Y coordinate
            
            for (int i = 0; i < imageButtons.size; i++) {
                Rectangle button = imageButtons.get(i);
                if (button.contains(touchX, touchY)) {
                    // Start game with selected image
                    String selectedImage = imageFiles.get(i);
                    game.setScreen(new GameScreen(game, selectedImage));
                    break;
                }
            }
        }
    }
    
    @Override
    public void resize(int width, int height) {
        // Handle screen resize if needed
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
    }
}
