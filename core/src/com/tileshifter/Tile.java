package com.tileshifter;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

/**
 * Represents a single tile in the puzzle
 * Contains texture region, current position, and target position for animations
 */
public class Tile {
    private TextureRegion textureRegion;
    private int correctX, correctY; // The correct position for this tile
    private int currentX, currentY; // Current grid position
    private Vector2 renderPosition; // Actual render position (for animations)
    private Vector2 targetPosition; // Target render position
    private boolean isEmpty; // True if this is the empty space
    private int tileId; // Unique identifier for this tile
    
    // Animation properties
    private static final float ANIMATION_SPEED = 8.0f;
    private boolean isAnimating = false;
    
    public Tile(TextureRegion textureRegion, int correctX, int correctY, int tileId) {
        this.textureRegion = textureRegion;
        this.correctX = correctX;
        this.correctY = correctY;
        this.currentX = correctX;
        this.currentY = correctY;
        this.tileId = tileId;
        this.isEmpty = false;
        
        this.renderPosition = new Vector2();
        this.targetPosition = new Vector2();
    }
    
    // Constructor for empty tile
    public Tile(int correctX, int correctY) {
        this.correctX = correctX;
        this.correctY = correctY;
        this.currentX = correctX;
        this.currentY = correctY;
        this.isEmpty = true;
        this.tileId = -1; // Special ID for empty tile
        
        this.renderPosition = new Vector2();
        this.targetPosition = new Vector2();
    }
    
    public void updateAnimation(float deltaTime) {
        if (isAnimating) {
            // Move towards target position
            float dx = targetPosition.x - renderPosition.x;
            float dy = targetPosition.y - renderPosition.y;
            
            if (Math.abs(dx) < 1f && Math.abs(dy) < 1f) {
                // Animation complete
                renderPosition.set(targetPosition);
                isAnimating = false;
            } else {
                // Continue animation
                renderPosition.x += dx * ANIMATION_SPEED * deltaTime;
                renderPosition.y += dy * ANIMATION_SPEED * deltaTime;
            }
        }
    }
    
    public void setGridPosition(int x, int y) {
        this.currentX = x;
        this.currentY = y;
    }
    
    public void setRenderPosition(float x, float y) {
        this.renderPosition.set(x, y);
        this.targetPosition.set(x, y);
        this.isAnimating = false;
    }
    
    public void animateToPosition(float x, float y) {
        this.targetPosition.set(x, y);
        this.isAnimating = true;
    }
    
    public boolean isInCorrectPosition() {
        return currentX == correctX && currentY == correctY;
    }
    
    // Getters
    public TextureRegion getTextureRegion() { return textureRegion; }
    public int getCorrectX() { return correctX; }
    public int getCorrectY() { return correctY; }
    public int getCurrentX() { return currentX; }
    public int getCurrentY() { return currentY; }
    public Vector2 getRenderPosition() { return renderPosition; }
    public boolean isEmpty() { return isEmpty; }
    public int getTileId() { return tileId; }
    public boolean isAnimating() { return isAnimating; }
}
