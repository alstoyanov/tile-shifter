# Tile Shifter Puzzle Game

A classic 15-puzzle sliding tile game built with LibGDX framework in Java. Players slide tiles in a 4x4 grid to complete beautiful images.

## Features

- **4x4 Grid Puzzle**: Classic sliding puzzle with 15 tiles and 1 empty space
- **Multiple Images**: Choose from several beautiful album cover images
- **Smart Shuffle**: Ensures every puzzle is solvable using inversion counting algorithm
- **Smooth Animations**: Tiles slide smoothly when moved
- **Win Detection**: Automatic detection when puzzle is solved
- **Cross-Platform**: Runs on desktop (Windows, Mac, Linux)

## Game Mechanics

- Click/tap on a tile adjacent to the empty space to move it
- Only tiles horizontally or vertically adjacent to the empty space can move
- Complete the puzzle by arranging all tiles in the correct order
- Use the Reset button to shuffle the puzzle again
- Use the Back button to return to image selection

## How to Build and Run

### Prerequisites
- Java 8 or higher
- Gradle (or use the included Gradle wrapper)

### Building
```bash
# On Windows
gradlew.bat desktop:build

# On Mac/Linux  
./gradlew desktop:build
```

### Running
```bash
# Easy way - use the provided scripts:
# On Windows
run-game.bat
# or
run-game-simple.bat

# On Mac/Linux
./run-game.sh

# Manual way (if scripts don't work):
# On Windows
cd desktop
..\gradlew.bat run

# On Mac/Linux
cd desktop
../gradlew run
```

## Project Structure

```
tile-shifter/
├── core/                          # Core game logic (platform-independent)
│   └── src/com/tileshifter/
│       ├── TileShiftGame.java     # Main game class
│       ├── Tile.java              # Individual tile representation
│       ├── PuzzleBoard.java       # Board logic and tile movement
│       └── screens/
│           ├── MenuScreen.java    # Image selection screen
│           └── GameScreen.java    # Main gameplay screen
├── desktop/                       # Desktop launcher
│   └── src/com/tileshifter/
│       └── DesktopLauncher.java   # Desktop application entry point
├── assets/
│   └── images/                    # Puzzle images (JPG format)
└── build.gradle                   # Build configuration
```

## Technical Details

- **Framework**: LibGDX 1.12.1
- **Language**: Java 8+
- **Graphics**: SpriteBatch rendering with TextureRegions
- **Input**: Touch/mouse input handling for both desktop and mobile
- **Algorithm**: Solvable shuffle using inversion counting for 4x4 grids
- **Animation**: Smooth tile movement with configurable speed

## Adding New Images

To add new puzzle images:
1. Place JPG files in the `assets/images/` directory
2. Images will automatically appear in the selection menu
3. Recommended image size: 512x512 pixels or larger
4. Square images work best for the 4x4 grid

## Code Quality Features

- Clean separation of concerns (logic vs rendering)
- Proper LibGDX lifecycle management
- Memory leak prevention with proper asset disposal
- Well-commented code with clear class responsibilities
- Modular design for easy extension

## License

This project is licensed under the MIT License - see the LICENSE file for details.
