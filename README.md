# TIC-TAC-TOE MACHINE

An advanced, responsive desktop Tic-Tac-Toe game built with **Java 23** and **JavaFX 22**, adhering to clean desktop UI principles (crisp 4px/6px geometry, no purple gradients, and zero AI-slop design).

---

## Features

- **Dynamic Opponent Modes**:
  - `vs Computer` (Minimax AI with Alpha-Beta Pruning)
  - `2 Players` (Local multiplayer)
  - `3 Players` (Tri-player board battle with unique player marks `X`, `O`, `Δ`)
  - `Computer vs Computer` (Simulated autonomous matches)
- **Custom Player Names**: Change player names in real time with synchronized turn badges, stats cards, move history, and victory banners.
- **Multiple Game Rules**:
  - `Classic`: Traditional Tic-Tac-Toe rules.
  - `Vanish`: Memory grid mode where each player can keep a maximum of 3 pieces on the board; the oldest piece vanishes upon placing a 4th.
  - `Blitz`: Fast-paced countdown timer (10 seconds per turn).
  - `Hazards`: Dynamically generated obstacle walls (`■`) that block cells.
- **Responsive Screen Adaptation**:
  - `Landscape`: 3-column split view (Settings, Arena, Analytics).
  - `Portrait Wide`: Hero board on top with side-by-side drawer panels.
  - `Portrait Compact`: Hero board on top with segmented navigation tabs (`Game Settings` and `Actions & Stats`).
- **Board Sizes**: 3x3, 4x4, 5x5, and 9x9.
- **Difficulty Levels**: Rookie, Tactical, Impossible, and Chaos.
- **Visual Themes**: Slate Light, Obsidian Dark, Cyberpunk, and Classic Paper.
- **Full Custom Window Title Bar**: Minimize, Maximize/Restore, and Close controls with window dragging and double-click to maximize.

---

## Prerequisites

- **Java JDK 21+** (Java 23 recommended)
- **Apache Maven 3.8+**

---

## How to Build & Run

### 1. Compile and Run the Application
```powershell
mvn clean javafx:run
```

### 2. Run the Test Suite
```powershell
mvn test
```

### 3. Package as JAR
```powershell
mvn clean package
```

---

## Keyboard Shortcuts

| Shortcut | Action |
| :--- | :--- |
| `Ctrl + N` / `F2` | Start a new match |
| `Ctrl + Z` | Undo move |
| `Ctrl + Y` | Redo move |
| `M` | Toggle audio effects |
| `P` | Toggle power / lock state |
| `R` | Replay match history |
| `1` - `9` / `Numpad 1` - `9` | Place mark on 3x3 grid |
