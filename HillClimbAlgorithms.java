/**
 * Core Algorithm Implementations for Hill Climb Racing.
 * 
 * 1. Sorting: Selection Sort & Insertion Sort for high-score leaderboard & distance records.
 * 2. Searching:
 *    - Binary Search (O(log n)): Locating nearest collectible / checkpoint in sorted position array.
 *    - Linear Search: Terrain height collision point lookup.
 * 3. Recursion:
 *    - Recursive combo bonus multiplier: Stunt combo chains (e.g. Backflip + Wheelie + Air Time).
 *    - Recursive fractal terrain displacement (Hills generation).
 * 
 * IT22013: Data Structures and Algorithms Mini Game
 */
public class HillClimbAlgorithms {

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
     * 1. SORTING: Selection Sort (Descending by Score).
     * Time Complexity: O(n^2) Best, Average, Worst.
     * Space Complexity: O(1) in-place.
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
     * 2. SEARCHING: Binary Search on sorted Checkpoint / Distance thresholds.
     * Time Complexity: O(log n).
     * Space Complexity: O(1).
     * Finds the index of the nearest upcoming distance milestone (e.g., 250m, 500m, 1000m).
     */
    public static int binarySearchNextMilestone(int[] milestones, int count, int currentDistance) {
        int low = 0;
        int high = count - 1;
        int resultIdx = -1;

        while (low <= high) {
            int mid = low + (high - low) / 2;
            if (milestones[mid] > currentDistance) {
                resultIdx = mid;
                high = mid - 1; // Look for closer milestone to the left
            } else {
                low = mid + 1;
            }
        }
        return resultIdx;
    }

    /**
     * 3. SEARCHING: Linear Search for Ground Segment.
     * Finds the terrain polygon or spline segment that spans the vehicle X coordinate.
     * Time Complexity: O(n).
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
     * 4. RECURSION 1: Recursive Stunt Combo Multiplier.
     * Calculates exponential combo bonus score based on chain depth.
     * Base Case: comboDepth <= 1 returns base points.
     * Recursive Step: bonus(n) = base + bonus(n - 1) * 1.5.
     */
    public static int recursiveComboBonus(int comboDepth, int basePoints) {
        if (comboDepth <= 1) {
            return basePoints;
        }
        return (int) (basePoints + recursiveComboBonus(comboDepth - 1, basePoints) * 1.5);
    }

    /**
     * 5. RECURSION 2: Recursive Midpoint Displacement for Terrain Ruggedness.
     * Generates natural fractal bumps and slopes between two elevation points.
     */
    public static void recursiveDisplace(double[] heights, int left, int right, double roughness) {
        if (right - left <= 1) return;
        int mid = (left + right) / 2;
        double average = (heights[left] + heights[right]) / 2.0;
        double variation = (Math.random() - 0.5) * roughness * (right - left);
        heights[mid] = average + variation;

        recursiveDisplace(heights, left, mid, roughness * 0.65);
        recursiveDisplace(heights, mid, right, roughness * 0.65);
    }
}
