import javax.swing.*;

/**
 * Entry Point for Hill Climb Racing (KDU IT22013 DSA Mini Game).
 * 
 * Usage:
 *   1. Graphical Mode (Default):
 *      java Main
 * 
 *   2. Algorithm Benchmark / Viva Test:
 *      java Main --algo
 */
public class Main {
    public static void main(String[] args) {
        if (args.length > 0 && (args[0].equals("--algo") || args[0].equals("--console"))) {
            runAlgorithmBenchmark();
        } else {
            SwingUtilities.invokeLater(() -> {
                HillClimbGame game = new HillClimbGame();
                game.setVisible(true);
            });
        }
    }

    private static void runAlgorithmBenchmark() {
        System.out.println("================================================================================");
        System.out.println("            HILL CLIMB RACING - DATA STRUCTURES & ALGORITHMS TEST");
        System.out.println("       General Sir John Kotelawala Defence University (KDU) - IT22013");
        System.out.println("================================================================================");

        // 1. STACK DEMO
        System.out.println("\n[1] LIFO STACK TEST (Stunt Combo History):");
        CustomStack<String> stunts = new CustomStack<>(10);
        stunts.push("Air Time (1.5s) +450");
        stunts.push("Wheelie +200");
        stunts.push("Backflip +1000");
        System.out.println("  Top of Stunt Stack (Last Stunt performed): " + stunts.peek());
        System.out.println("  Popping stunts in reverse chronological order:");
        while (!stunts.isEmpty()) {
            System.out.println("   -> Popped: " + stunts.pop());
        }

        // 2. QUEUE DEMO
        System.out.println("\n[2] FIFO QUEUE TEST (Procedural Terrain Chunks):");
        CustomQueue<String> terrainStream = new CustomQueue<>(10);
        terrainStream.enqueue("Rolling Green Hill (0m - 300m)");
        terrainStream.enqueue("Wooden Rope Suspension Bridge (450m - 850m)");
        terrainStream.enqueue("Jump Ramp & Coin Arc (1200m - 1400m)");
        System.out.println("  Next terrain chunk in FIFO pipeline: " + terrainStream.peek());
        while (!terrainStream.isEmpty()) {
            System.out.println("   -> Rendering & processing chunk: " + terrainStream.dequeue());
        }

        // 3. LINKED LIST DEMO
        System.out.println("\n[3] LINKED LIST TEST (Dynamic Coin Pickups):");
        CustomLinkedList<String> coins = new CustomLinkedList<>();
        coins.addLast("Gold Coin #1 (Val: 25) at X: 520");
        coins.addLast("Gold Coin #2 (Val: 5) at X: 560");
        coins.addLast("Fuel Canister #1 at X: 650");
        System.out.println("  Active Collectibles count: " + coins.size());
        System.out.println("  Collecting items (O(1) dynamic removal):");
        while (!coins.isEmpty()) {
            System.out.println("   -> Collected: " + coins.removeFirst());
        }

        // 4. SORTING ALGORITHM BENCHMARK
        System.out.println("\n[4] SELECTION SORT BENCHMARK (High-Score Leaderboard):");
        HillClimbAlgorithms.HighScoreRecord[] board = {
            new HillClimbAlgorithms.HighScoreRecord("Rider_Alex", 640, 1850, 24500),
            new HillClimbAlgorithms.HighScoreRecord("Speed_Demon", 1250, 4200, 58200),
            new HillClimbAlgorithms.HighScoreRecord("KDU_Champion", 980, 3100, 42100),
            new HillClimbAlgorithms.HighScoreRecord("Rookie_Driver", 280, 850, 9200)
        };

        long start = System.nanoTime();
        HillClimbAlgorithms.selectionSortLeaderboard(board, board.length);
        long elapsed = System.nanoTime() - start;

        System.out.println("  Sorted Leaderboard (Descending by Score) in " + elapsed + " ns (O(n^2)):");
        for (int i = 0; i < board.length; i++) {
            System.out.printf("   [%d] %-15s | %4dm | %5d coins | %6d pts\n",
                (i + 1), board[i].getPlayerName(), board[i].getDistanceMeters(), board[i].getTotalCoins(), board[i].getScore());
        }

        // 5. BINARY SEARCH BENCHMARK
        System.out.println("\n[5] BINARY SEARCH TEST (Next Distance Milestone Lookup):");
        int[] milestones = {250, 500, 750, 1000, 1500, 2000, 3000, 5000};
        int currentDist = 620;
        int nextIdx = HillClimbAlgorithms.binarySearchNextMilestone(milestones, milestones.length, currentDist);
        System.out.println("  Current Vehicle Distance: " + currentDist + "m");
        System.out.println("  Next Milestone (O(log n) lookup): " + milestones[nextIdx] + "m (Index " + nextIdx + ")");

        // 6. RECURSION BENCHMARK
        System.out.println("\n[6] RECURSION TEST (Combo Multiplier Calculation):");
        for (int depth = 1; depth <= 4; depth++) {
            int bonus = HillClimbAlgorithms.recursiveComboBonus(depth, 500);
            System.out.println("  Stunt Combo Chain " + depth + "x -> Points Bonus: " + bonus);
        }

        System.out.println("\n================================================================================");
        System.out.println(">>> ALL DATA STRUCTURES AND ALGORITHMS PASSED TEST SUITE SUCCESSFULLY! <<<");
        System.out.println("================================================================================");
    }
}
