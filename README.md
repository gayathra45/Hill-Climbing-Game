# 🏁 Hill Climb Racing 2 - DSA Mini Game
**General Sir John Kotelawala Defence University (KDU)**  
**Department of Information Technology - IT22013 Data Structures & Algorithms Project**

---

## 🚀 How to Run the Game in Command Prompt (CMD)

Open your terminal or Command Prompt in this folder and run:

```bash
# 1. Compile all Java code
javac *.java

# 2. Launch the Game
java Main
```

### To Run the DSA Viva Benchmark Test Suite:
```bash
java Main --algo
```

---

## 📁 File Structure & Architectural Guide

```
Hill Climb/
├── [1] DATA STRUCTURES & ALGORITHMS (KDU VIVA CORE)
├── CustomStack.java         # LIFO Stack: Records and pops stunt combos in reverse order
├── CustomQueue.java         # FIFO Queue: Procedural terrain chunk streaming pipeline
├── CustomLinkedList.java    # Doubly Linked List: Dynamic O(1) coin & fuel pickups
├── HillClimbAlgorithms.java # Selection Sort O(n²), Binary Search O(log n), Recursion
│
├── [2] GAME ENGINE & LOOP
├── Main.java                # Application entry point & CLI benchmark runner
├── HillClimbGame.java       # JFrame application window container
├── GamePanel.java           # Main 60 FPS update loop, collision detection & state machine
│
├── [3] PHYSICS & WORLD SIMULATION
├── Vehicle.java             # Hooke's law suspension, weight transfer, rollovers, fuel burn
├── Terrain.java             # Procedural sine/cosine hills, bridges & jumps
├── Collectible.java         # Coins ($5, $25), Fuel canisters (+65%), Bonus stars
├── ParticleManager.java     # Particle FX (dirt spray, exhaust, grinding sparks, floating text)
│
├── [4] UI & HIGH SCORES
├── DashboardHUD.java        # Triple analog gauges, pedals, HUD cards, vector icons
├── ScoreboardManager.java   # File I/O manager syncing leaderboard.txt
│
└── [5] DATA & DOCUMENTATION
    ├── leaderboard.txt      # Persistent high score records
    └── README.md            # Complete Project Guide & Documentation
```


---

## 🎮 Game Controls

| Action | Primary Key | Alternative Key | Mouse / Touchpad |
| :--- | :--- | :--- | :--- |
| **Accelerate / Gas** | `D` | `Right Arrow (→)` | Click/Hold **Right Green Pedal** |
| **Brake / Reverse** | `A` | `Left Arrow (←)` | Click/Hold **Left Red Pedal** |
| **Nitro Boost** | `Space` | `Up Arrow (↑)` / `W` | — |
| **Switch Vehicle** | `V` | — | Cycles Dirt Bike, Monster Truck, Racing Buggy |
| **Pause / Resume** | `P` | `ESC` | Click **Pause Button** in top-right HUD |
| **Restart Game** | `R` | — | Click **Play Again** on Game Over screen |
| **Leaderboard** | `L` | — | Click **Trophy Button** in top-right HUD |
