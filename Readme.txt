================================================================================
                    HILL CLIMB RACING (COUNTRYSIDE STAGE 4)
               IT22013: Data Structures and Algorithms Mini Game
         Intake 42 - General Sir John Kotelawala Defence University (KDU)
================================================================================

1. PROJECT OVERVIEW
--------------------------------------------------------------------------------
Hill Climb Racing is a physics-based 2D side-scrolling hill climb driving game 
developed in Java. The player drives a customizable off-road vehicle (motocross 
bike / jeep) across rolling countryside hills, wooden rope suspension bridges, 
and steep valleys.

The player balances speed, vehicle tilt, fuel conservation, and stunts (backflips, 
wheelies, long air jumps) while collecting gold coins and reaching distance 
checkpoints without crashing upside down.

2. HOW TO COMPILE AND RUN THE GAME
--------------------------------------------------------------------------------
Prerequisites: Java Development Kit (JDK 8 or later - no external libraries needed).

Step 1: Open Terminal / Command Prompt and navigate to Hill Climb directory:
        cd "e:\Projects\DSA Game\Hill Climb"

Step 2: Compile all Java source files:
        javac *.java

Step 3: Run the game:
        java Main
        (Launches the rich 2D Graphical Game with 60 FPS physics loop, 
         rolling parallax hills, wooden suspension bridges, gold coin arcs, 
         and analog dashboard gauges!)

        * Optional: To run the DSA Algorithms and Data Structures test suite:
        java Main --algo

3. KEYBOARD CONTROLS (ARROWS & WASD)
--------------------------------------------------------------------------------
  [→] or [D]     : GAS / ACCELERATE & LEAN BACK (Wheelie on ground, air tilt)
  [←] or [A]     : BRAKE / REVERSE & LEAN FORWARD (Mid-air pitch down)
  [SPACE] or [↑] : NITRO TURBO BOOST (Blue exhaust rocket flame)
  [R]            : RESTART RUN (Instant respawn after crash or out of fuel)

4. COMPLETE DATA STRUCTURES IMPLEMENTED & REAL-WORLD JUSTIFICATION
--------------------------------------------------------------------------------
(Marks: 25% - Data Structures)

1. STACK (LIFO - Last-In, First-Out)
   - Class: CustomStack.java
   - Real-World Justification: 
     a) Stunt Combo History: High-speed air tricks (Backflip, Wheelie, Air Time) 
        are pushed onto the stack as they happen. The most recent stunt pops first 
        to calculate combo bonus multipliers.
     b) Fuel Boost Revert / Emergency Undo Buffer: Stores physical snapshots to 
        roll back state in case of emergency rewind features.
   - Core Operations: push(item), pop(), peek(), isEmpty(), size().

2. QUEUE (FIFO - First-In, First-Out)
   - Class: CustomQueue.java
   - Real-World Justification: 
     Procedural Terrain & Coin Stream Pipeline. Upcoming terrain segments (hills, 
     bridges, jump ramps) and arc-pattern coin clusters are enqueued in chronological 
     distance order ahead of the camera, and dequeued as the vehicle drives past 
     to maintain endless smooth scrolling.
   - Core Operations: enqueue(item), dequeue(), peek(), isEmpty(), size().

3. LINKED LIST (Dynamic Linear Structure)
   - Class: CustomLinkedList.java
   - Real-World Justification: 
     Active Collectibles & Terrain Spline Waypoints. As the vehicle drives forward, 
     coins, bonus stars, and fuel cans scattered across the terrain are checked for 
     proximity. When collected, items are removed in O(1) time without costly 
     array reallocation or shifting.
   - Core Operations: addLast(item), remove(item), removeFirst(), get(index), size().

4. ARRAYS (Static & Coordinate Buffers)
   - Code: double[] xVals, double[] yVals, HighScoreRecord[] records
   - Real-World Justification: High-speed raster rendering coordinates for Java2D 
     Path2D splines and leaderboard record sorting.

5. ALGORITHMS IMPLEMENTED & TIME COMPLEXITIES
--------------------------------------------------------------------------------
(Marks: 25% - Algorithm Application)

1. SORTING ALGORITHM: SELECTION SORT
   - Location: HillClimbAlgorithms.java -> selectionSortLeaderboard()
   - Application: Sorts driver high-score records and distance runs in descending order.
   - Time Complexity: Best: O(n^2), Average: O(n^2), Worst: O(n^2).
   - Space Complexity: O(1) in-place.

2. SEARCHING ALGORITHM 1: BINARY SEARCH
   - Location: HillClimbAlgorithms.java -> binarySearchNextMilestone()
   - Application: Finds the nearest upcoming distance checkpoint milestone 
     (e.g., 250m, 500m, 1000m) in logarithmic time.
   - Time Complexity: O(log n).
   - Space Complexity: O(1).

3. SEARCHING ALGORITHM 2: LINEAR SEARCH
   - Location: HillClimbAlgorithms.java -> linearSearchTerrainIndex()
   - Application: Scans terrain control nodes to find which ground segment spans 
     the vehicle coordinates for normal and collision queries.
   - Time Complexity: O(n).

4. RECURSIVE FUNCTION 1: RECURSIVE COMBO MULTIPLIER
   - Location: HillClimbAlgorithms.java -> recursiveComboBonus()
   - Application: Calculates compounding score multipliers for chaining multiple 
     stunts in a single jump (Backflip + Air Time + Wheelie Landing).
     * Base Case: comboDepth <= 1 returns base points.
     * Recursive Step: bonus(n) = base + bonus(n - 1) * 1.5.

5. RECURSIVE FUNCTION 2: RECURSIVE FRACTAL TERRAIN DISPLACEMENT
   - Location: HillClimbAlgorithms.java -> recursiveDisplace()
   - Application: Generates natural hill elevation variations using midpoint displacement.

6. SAMPLE VIVA-VOCE QUESTIONS & ANSWERS
--------------------------------------------------------------------------------
Q: Why did you choose a Stack for stunts?
A: In stunt-based racing, combo chains depend on the most recent action executed. 
   A LIFO Stack naturally allows the game engine to evaluate the latest trick 
   first and apply consecutive multiplier bonuses.

Q: Why did you choose a Queue for procedural terrain?
A: Procedural endless runners generate world segments sequentially along the X-axis. 
   The terrain ahead enters the queue first and leaves the queue first (FIFO) as 
   the camera passes it, conserving system memory.

Q: How is the physical suspension simulated?
A: The vehicle model tracks dual wheels (front and rear) using spring-damper equations. 
   Penetration beneath the continuous mathematical terrain height function exerts 
   normal restoring forces and angular torque to realistically simulate bumps, 
   hill climbing, and suspension bounce.
================================================================================
