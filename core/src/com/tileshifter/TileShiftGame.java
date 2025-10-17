package com.tileshifter;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.viewport.FitViewport; // Using FitViewport to maintain aspect ratio
import com.badlogic.gdx.utils.viewport.Viewport;
import com.tileshifter.screens.MenuScreen;

/**
 * Main game class for Tile Shifter puzzle game
 * Manages screens and global resources
 */
public class TileShiftGame extends Game {
    public SpriteBatch batch;
    public BitmapFont font;
    public OrthographicCamera camera;
    public Viewport viewport;
    
    public static final int VIRTUAL_WIDTH = 800; // Define a virtual width
    public static final int VIRTUAL_HEIGHT = 600; // Define a virtual height
    
    @Override
    public void create() {
        batch = new SpriteBatch();

        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera); // Initialize FitViewport
        viewport.apply(true);

        // Generate font from a TTF file
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Roboto-Regular.ttf"));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = 24; // Font size
        parameter.shadowOffsetX = 1;
        parameter.shadowOffsetY = 1;
        parameter.borderColor = com.badlogic.gdx.graphics.Color.DARK_GRAY;
        parameter.borderWidth = 1;
        font = generator.generateFont(parameter); // Generates a clean BitmapFont
        generator.dispose(); // Dispose the generator to avoid memory leaks
        
        // Start with the menu screen
        setScreen(new MenuScreen(this));
    }
    
    @Override
    public void render() {
        // Update the camera
        camera.update();
        // Apply the viewport to the GL context
        viewport.apply(true);

        // Clear screen
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Set the batch's projection matrix to the camera's combined matrix
        batch.setProjectionMatrix(camera.combined);

        // Render current screen
        super.render();
    }

    @Override
    public void resize(int width, int height) {
        // Update the viewport when the screen is resized
        viewport.update(width, height, true);
    }
    
    @Override
    public void dispose() {
        if (batch != null) {
            batch.dispose();
        }
        if (font != null) {
            font.dispose();
        }
        if (camera != null) {
            // Camera doesn't need explicit dispose, but including for completeness if it held resources
        }
        super.dispose();
    }
}
