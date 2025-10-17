package com.tileshifter;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

/**
 * Desktop launcher for the Tile Shifter puzzle game
 */
public class DesktopLauncher {
    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setForegroundFPS(60);
        config.setTitle("Tile Shifter Puzzle");
        config.setWindowedMode(800, 600);
        config.setResizable(true);
        
        new Lwjgl3Application(new TileShiftGame(), config);
    }
}
