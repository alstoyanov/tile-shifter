# Tile Shifter Puzzle Game

A feature-rich puzzle game built with LibGDX framework in Java. Players can choose from three different game modes to solve 4x4 grid puzzles with beautiful images.

## Features

- **Three Game Modes**: Classic sliding puzzle, Rotate mode, and Shift mode
- **4x4 Grid Puzzle**: 16 tiles arranged in various challenging configurations
- **Multiple Images**: Choose from several beautiful album cover images
- **Smart Shuffle**: Mode-specific shuffling ensures solvable puzzles
- **Intuitive UI**: Mode-specific visual indicators and controls
- **Help System**: Full image preview and mode-specific instructions
- **Win Detection**: Automatic detection when puzzle is solved
- **Cross-Platform**: Runs on desktop (Windows, Mac, Linux)

## Game Modes

### Classic Mode
The traditional 15-puzzle sliding tile game:
- Click/tap on a tile adjacent to the empty space to move it
- Only tiles horizontally or vertically adjacent to the empty space can move
- Complete the puzzle by arranging all tiles in the correct order

### Rotate Mode
Rotate 2x2 sub-boards to solve the puzzle:
- Click any tile within a 2x2 sub-board to rotate it clockwise
- Five overlapping 2x2 sub-boards can be rotated:
  - Top-Left, Top-Right, Bottom-Left, Bottom-Right, and Center
- Yellow borders indicate the rotatable sub-boards
- No empty space - all 16 tiles are visible

### Shift Mode
Shift entire rows and columns cyclically:
- Click arrow buttons (^ v < >) to shift columns/rows
- Tiles wrap around when shifted (top tile goes to bottom, etc.)
- No empty space - all 16 tiles are visible
- Strategic shifting of multiple rows/columns needed to solve

## Common Controls

- **Reset button**: Shuffle the puzzle for a new game
- **Back button**: Return to image selection
- **Help button**: View the complete puzzle image
- **Instructions button**: View mode-specific game instructions

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
├── core/                                    # Core game logic (platform-independent)
│   └── src/com/tileshifter/
│       ├── TileShiftGame.java               # Main game class
│       ├── GameMode.java                    # Game mode enumeration
│       ├── Tile.java                        # Individual tile representation
│       ├── PuzzleBoard.java                 # Base board logic (Classic mode)
│       ├── RotatePuzzleBoard.java           # Rotate mode logic
│       ├── ShiftPuzzleBoard.java            # Shift mode logic
│       └── screens/
│           ├── ModeSelectionScreen.java     # Game mode selection screen
│           ├── ImageSelectionScreen.java    # Image selection screen
│           └── GameScreen.java              # Main gameplay screen
├── desktop/                                 # Desktop launcher
│   └── src/com/tileshifter/
│       └── DesktopLauncher.java             # Desktop application entry point
├── assets/
│   ├── images/                              # Puzzle images (JPG format)
│   └── fonts/                               # Font files (TTF format)
└── build.gradle                             # Build configuration
```

## Technical Details

- **Framework**: LibGDX 1.12.1
- **Language**: Java 8+
- **Graphics**: SpriteBatch rendering with TextureRegions
- **Input**: Touch/mouse input handling with viewport coordinate projection
- **Fonts**: FreeType font generation with mipmaps for smooth scaling
- **Algorithms**: 
  - Classic Mode: Solvable shuffle using inversion counting for 4x4 grids
  - Rotate Mode: Random rotations of 5 overlapping 2x2 sub-boards
  - Shift Mode: Random cyclic shifts of rows and columns
- **Architecture**: Polymorphic board classes (PuzzleBoard, RotatePuzzleBoard, ShiftPuzzleBoard)

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
