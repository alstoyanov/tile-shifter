# Development Guide

This document provides information for developers who want to modify or extend the Tile Shifter puzzle game.

## Project Architecture

### Core Components

1. **TileShiftGame** - Main game class that manages screens and global resources
2. **Tile** - Represents individual puzzle pieces with animation support
3. **PuzzleBoard** - Manages game logic, tile movement, and win detection
4. **MenuScreen** - Image selection interface
5. **GameScreen** - Main gameplay interface

### Key Algorithms

#### Solvable Shuffle Algorithm
The game uses inversion counting to ensure every generated puzzle is solvable:
- For a 4x4 grid, a puzzle is solvable if `(inversions + empty_row_from_bottom)` is odd
- This prevents impossible puzzle configurations

#### Tile Movement Logic
- Only tiles adjacent (horizontally/vertically) to the empty space can move
- Movement validation prevents diagonal moves and out-of-bounds operations
- Animation system provides smooth visual feedback

## Adding New Features

### New Puzzle Sizes
To support different grid sizes:
1. Modify `BOARD_SIZE` constant in `PuzzleBoard.java`
2. Update solvability algorithm for the new grid size
3. Adjust UI layout calculations in `GameScreen.java`

### New Image Formats
To support additional image formats:
1. Update file extension checks in `MenuScreen.loadImageFiles()`
2. Ensure LibGDX supports the format (PNG, BMP, etc.)

### Enhanced UI
Current UI uses text-based rendering for simplicity. To add proper graphics:
1. Create texture assets for buttons, borders, and backgrounds
2. Replace text-based drawing methods with texture rendering
3. Add hover effects and better visual feedback

### Sound Effects
To add audio:
1. Add LibGDX audio dependencies to `build.gradle`
2. Load sound files in asset loading phase
3. Trigger sounds on tile moves, wins, and button clicks

### Mobile Support
The game is designed to work on mobile, but to optimize:
1. Add Android module to the project
2. Adjust touch input sensitivity
3. Optimize UI scaling for different screen sizes
4. Add haptic feedback for mobile devices

## Code Style Guidelines

- Use clear, descriptive variable and method names
- Add JavaDoc comments for public methods
- Separate game logic from rendering code
- Dispose of resources properly to prevent memory leaks
- Use LibGDX best practices for asset management

## Testing

### Manual Testing Checklist
- [ ] All images load correctly in menu
- [ ] Tiles move only when adjacent to empty space
- [ ] Win condition triggers correctly
- [ ] Reset button works properly
- [ ] Back button returns to menu
- [ ] No memory leaks after multiple games
- [ ] Animations are smooth and complete

### Automated Testing
Consider adding unit tests for:
- Solvability algorithm
- Tile movement validation
- Win condition detection
- Board state management

## Performance Considerations

- Texture loading is done once per image selection
- Animation updates use delta time for frame-rate independence
- SpriteBatch is used efficiently with begin/end calls
- Proper resource disposal prevents memory leaks

## Common Issues and Solutions

### Images Not Loading
- Ensure JPG files are in `assets/images/` directory
- Check file permissions and naming conventions
- Verify LibGDX can read the image format

### Animation Stuttering
- Check delta time calculations
- Ensure consistent frame rate
- Verify no blocking operations in render loop

### Build Issues
- Ensure Java 8+ is installed
- Check Gradle wrapper permissions on Unix systems
- Verify all dependencies are properly configured

## Contributing

When contributing to the project:
1. Follow existing code style and patterns
2. Test thoroughly on multiple platforms
3. Update documentation for new features
4. Ensure backward compatibility when possible
