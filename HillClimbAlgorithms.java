/**
 * Core Algorithm Implementations for Hill Climb Racing.
 * 
 * ============================================================================
 * KDU IT22013 DSA VIVA EVALUATION SHEET:
 * ============================================================================
 * 1. SORTING ALGORITHM: Selection Sort
 *    - Purpose: Real-time sorting of High-Score Leaderboard records in descending order.
 *    - Real-World Justification:
 *      * Operates in-place with O(1) auxiliary memory.
 *      * Performs the minimal number of memory writes/swaps: exactly (n - 1) swaps,
 *        which is optimal when modifying persistent disk files or structured records.
 *      * Highly deterministic and simple to trace during viva examinations.
 *    - Time Complexity: Best: O(n²), Average: O(n²), Worst: O(n²).
 *    - Space Complexity: O(1) in-place.
 * 
 * 2. SEARCHING ALGORITHM: Binary Search
 *    - Purpose: Logarithmic lookup of the next upcoming distance checkpoint milestone.
 *    - Real-World Justification:
 *      * Distance milestones (250m, 500m, 750m, 1000m, 1500m...) are naturally ordered.
 *      * Reduces search time from O(n) to O(log n) every frame during distance updates.
 *    - Time Complexity: O(log n).
 *    - Space Complexity: O(1).
 * 
 * 3. RECURSION: Recursive Stunt Combo Bonus Evaluator
 *    - Purpose: Calculates exponential bonus score when multiple stunts chain together.
 *    - Base Case: comboDepth <= 1 returns base points.
 *    - Recursive Step: bonus(n) = base + bonus(n - 1) * 1.5.
 *    - Time Complexity: O(n) where n is chain depth.
 *    - Space Complexity: O(n) call stack frames.
 * ============================================================================
 */
public class HillClimbAlgorithms {

    /**
     * High Score Leaderboard Record Model.
     */
    public static class HighScoreRecord {
        private String playerName;
        private int distanceMeters;
        private int totalCoins;
        private int score;

        public HighScoreRecord(String playerName, int distanceMeters, int totalCoins, int score) {
            this.playerName = playerName;
            this.distanceMeters = distanceMeters;
            this.totalCoins = totalCoins;
            this.score = score;
        }

        public String getPlayerName() { return playerName; }
        public int getDistanceMeters() { return distanceMeters; }
        public int getTotalCoins() { return totalCoins; }
        public int getScore() { return score; }
    }

    /**
     * [ALGORITHM 1: SELECTION SORT]
     * Sorts Leaderboard Records in Descending Order (Highest Score at Index 0).
     * 
     * How it works:
     * 1. Finds the maximum element in the unsorted portion of the array.
     * 2. Swaps it with the first unsorted position.
     * 3. Repeats for all (count - 1) elements.
     * 
     * Time Complexity: O(n²) comparisons, O(n) swaps.
     * Space Complexity: O(1) in-place auxiliary memory.
     */
    public static void selectionSortLeaderboard(HighScoreRecord[] records, int count) {
        for (int i = 0; i < count - 1; i++) {
            int maxIdx = i;
            for (int j = i + 1; j < count; j++) {
                if (records[j].getScore() > records[maxIdx].getScore()) {
                    maxIdx = j;
                }
            }
            if (maxIdx != i) {
                HighScoreRecord temp = records[i];
                records[i] = records[maxIdx];
                records[maxIdx] = temp;
            }
        }
    }

    /**
     * [ALGORITHM 2: BINARY SEARCH]
     * Searches sorted milestone thresholds array to find the next target checkpoint.
     * 
     * How it works:
     * 1. Divides the search interval in half every iteration.
     * 2. If middle milestone > currentDistance, narrows search to left half to find closest.
     * 3. Otherwise narrows search to right half.
     * 
     * Time Complexity: O(log n).
     * Space Complexity: O(1).
     * @return index of next milestone, or -1 if passed all.
     */
    public static int binarySearchNextMilestone(int[] milestones, int count, int currentDistance) {
        int low = 0;
        int high = count - 1;
        int resultIdx = -1;

        while (low <= high) {
            int mid = low + (high - low) / 2;
            if (milestones[mid] > currentDistance) {
                resultIdx = mid;
                high = mid - 1; // Narrow to closer milestone on left
            } else {
                low = mid + 1;  // Narrow to right
            }
        }
        return resultIdx;
    }

    /**
     * [ALGORITHM 3: LINEAR SEARCH]
     * Finds the ground segment in terrain coordinate arrays that spans vehicle X.
     * 
     * Time Complexity: O(n).
     * Space Complexity: O(1).
     * @return segment index, or -1 if outside bounds.
     */
    public static int linearSearchTerrainIndex(double[] xCoords, int count, double vehicleX) {
        for (int i = 0; i < count - 1; i++) {
            if (vehicleX >= xCoords[i] && vehicleX <= xCoords[i + 1]) {
                return i;
            }
        }
        return -1;
    }

    /**
     * [ALGORITHM 4: RECURSION]
     * Evaluates exponential bonus score when multiple stunts chain together.
     * 
     * Recursive Formula:
     * - Base Case: depth <= 1 -> returns basePoints.
     * - Recursive Step: basePoints + recursiveComboBonus(depth - 1) * 1.5.
     * 
     * Time Complexity: O(n) where n is depth.
     * Space Complexity: O(n) activation stack frames.
     */
    public static int recursiveComboBonus(int comboDepth, int basePoints) {
        // Base Case: Terminal condition
        if (comboDepth <= 1) {
            return basePoints;
        }
        // Recursive Call: Sub-problem evaluation
        return (int) (basePoints + recursiveComboBonus(comboDepth - 1, basePoints) * 1.5);
    }
}
