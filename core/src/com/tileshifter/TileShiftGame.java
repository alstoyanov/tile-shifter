package com.tileshifter;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.tileshifter.screens.MenuScreen;

/**
 * Main game class for Tile Shifter puzzle game
 * Manages screens and global resources
 */
public class TileShiftGame extends Game {
    public SpriteBatch batch;
    public BitmapFont font;
    
    public static final int SCREEN_WIDTH = 800;
    public static final int SCREEN_HEIGHT = 600;
    
    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont(); // Default font
        font.getData().setScale(1.2f);
        
        // Start with the menu screen
        setScreen(new MenuScreen(this));
    }
    
    @Override
    public void render() {
        // Clear screen
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Render current screen
        super.render();
    }
    
    @Override
    public void dispose() {
        if (batch != null) {
            batch.dispose();
        }
        if (font != null) {
            font.dispose();
        }
        super.dispose();
    }
}
