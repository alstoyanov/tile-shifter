# Game Modes Documentation

This document provides detailed information about the three game modes available in Tile Shifter.

## Overview

Tile Shifter now offers three distinct puzzle-solving experiences:
1. **Classic Mode** - The traditional 15-puzzle
2. **Rotate Mode** - Rotate overlapping 2x2 sub-boards
3. **Shift Mode** - Cyclically shift entire rows and columns

## Classic Mode

### Description
The traditional sliding tile puzzle where one space is empty and tiles can slide into it.

### Gameplay
- 15 tiles + 1 empty space
- Click a tile adjacent to the empty space to slide it
- Only horizontally or vertically adjacent tiles can move
- Goal: Arrange all tiles in correct order

### Implementation Details
- Class: `PuzzleBoard.java`
- Shuffle: Uses inversion counting to ensure solvability
- Win detection: All tiles must be in their correct positions, including the empty space

### Strategy Tips
- Start by solving one row or column at a time
- Work from top-left to bottom-right
- The last 2x2 section is the trickiest

## Rotate Mode

### Description
Five overlapping 2x2 sub-boards that can be rotated clockwise to solve the puzzle.

### Gameplay
- All 16 tiles visible (no empty space)
- Click any tile to rotate its containing 2x2 sub-board clockwise
- Five sub-boards overlap in a specific pattern:
  1. **Top-Left**: Rows 0-1, Columns 0-1
  2. **Top-Right**: Rows 0-1, Columns 2-3
  3. **Bottom-Left**: Rows 2-3, Columns 0-1
  4. **Bottom-Right**: Rows 2-3, Columns 2-3
  5. **Center**: Rows 1-2, Columns 1-2
- Yellow borders indicate the rotatable regions
- Goal: Arrange all tiles in correct order through rotations

#### Sub-board Layout Diagram
```
4x4 Grid (0-3 indices):
┌───┬───┬───┬───┐
│ 0 │ 1 │ 2 │ 3 │  Row 0
├───┼───┼───┼───┤
│ 4 │ 5 │ 6 │ 7 │  Row 1
├───┼───┼───┼───┤
│ 8 │ 9 │10 │11 │  Row 2
├───┼───┼───┼───┤
│12 │13 │14 │15 │  Row 3
└───┴───┴───┴───┘

Rotatable 2x2 Sub-boards:
1. Top-Left     [0,1,4,5]
2. Top-Right    [2,3,6,7]
3. Bottom-Left  [8,9,12,13]
4. Bottom-Right [10,11,14,15]
5. Center       [5,6,9,10]

Visual representation:
┌─────────┬─────────┐
│  TL(1)  │  TR(2)  │
│  0  1   │  2  3   │
│  4  5───┼──6  7   │
├────│Ctr│ │    ├───┤
│  8 │(5)│ │10  │11 │
│  9──────┘ └────┤   │
│ BL(3)    BR(4) │   │
│12 13    14 15  │   │
└────────────────┴───┘
```

### Implementation Details
- Class: `RotatePuzzleBoard.java`
- Extends: `PuzzleBoard`
- Shuffle: 50-100 random rotations
- Rotation: Clockwise transformation of 2x2 grid
  ```
  TL TR    BL TL
  BL BR -> BR TR
  ```

### Strategy Tips
- The center sub-board is key as it overlaps with all corner sub-boards
- Rotating a corner affects only that corner and the center
- Sometimes you need to "undo" progress to make new moves possible
- All configurations created by rotation are solvable through rotation

## Shift Mode

### Description
Entire rows and columns can be shifted cyclically using arrow buttons.

### Gameplay
- All 16 tiles visible (no empty space)
- Arrow buttons surround the board:
  - **^ buttons** above each column: shift that column UP
  - **v buttons** below each column: shift that column DOWN
  - **< buttons** left of each row: shift that row LEFT
  - **> buttons** right of each row: shift that row RIGHT
- Tiles wrap around (e.g., shifting up moves top tile to bottom)
- Goal: Arrange all tiles in correct order through shifting

### Implementation Details
- Class: `ShiftPuzzleBoard.java`
- Extends: `PuzzleBoard`
- Shuffle: 30-50 random shifts
- Shift operations:
  - Column up: `tiles[col][0..3]` → `tiles[col][1..3, 0]`
  - Column down: `tiles[col][0..3]` → `tiles[col][3, 0..2]`
  - Row left: `tiles[0..3][row]` → `tiles[1..3, 0][row]`
  - Row right: `tiles[0..3][row]` → `tiles[3, 0..2][row]`

### Strategy Tips
- Think in terms of "fixing" one row or column at a time
- Use perpendicular shifts to position tiles before final shifts
- The cyclic nature means you can always get a tile back to a position
- Count how many shifts needed for each tile

## Architecture

### Class Hierarchy
```
PuzzleBoard (base class, implements Classic mode)
├── RotatePuzzleBoard (extends for Rotate mode)
└── ShiftPuzzleBoard (extends for Shift mode)
```

### Key Methods
All board classes implement:
- `initializeBoard(Texture)` - Sets up the puzzle from an image
- `reset()` - Reshuffles the board
- `isWon()` - Checks if puzzle is solved
- `update(float)` - Updates animations
- `getTile(int, int)` - Gets tile at position

Mode-specific methods:
- **PuzzleBoard**: `moveTile(int, int)` - Slide tile into empty space
- **RotatePuzzleBoard**: `rotateSubBoard(int)` - Rotate a 2x2 sub-board
- **ShiftPuzzleBoard**: `shiftColumnUp/Down(int)`, `shiftRowLeft/Right(int)` - Shift operations

### UI Components

#### Mode Selection Screen
- `ModeSelectionScreen.java`
- Displays three buttons for mode selection
- Shows brief description of each mode

#### Image Selection Screen
- `ImageSelectionScreen.java`
- Displays thumbnail grid of available images
- Shows selected mode in title
- Includes back button to return to mode selection

#### Game Screen
- `GameScreen.java`
- Adapts UI based on selected mode:
  - **Classic**: Standard board with empty space
  - **Rotate**: Yellow borders indicating rotatable regions
  - **Shift**: Arrow buttons surrounding the board
- Common elements: Back, Reset, Help, Instructions buttons
- Mode-specific instructions in overlay

## Adding New Game Modes

To add a new game mode:

1. Add the mode to `GameMode.java` enum
2. Create a new class extending `PuzzleBoard`
3. Override `shuffleBoard()` for mode-specific shuffling
4. Override `isSolvable()` if needed (may always return true)
5. Implement mode-specific move methods
6. Update `GameScreen.java`:
   - Add UI elements in `setupUI()`
   - Add drawing logic in `render()`
   - Add input handling in `handleInput()`
   - Add instructions in `drawInstructionsOverlay()`
7. Add mode to `ModeSelectionScreen.java`

## Testing Notes

Each mode has been tested for:
- ✓ Proper tile initialization
- ✓ Correct shuffle algorithm
- ✓ Accurate move/rotate/shift operations
- ✓ Win detection accuracy
- ✓ UI responsiveness
- ✓ Proper screen transitions
- ✓ Help and instructions display

