import java.io.*;
import java.util.*;

/**
 * Persistent Scoreboard and High Score Management System.
 * 
 * ============================================================================
 * KDU IT22013 DSA VIVA EVALUATION SHEET:
 * ============================================================================
 * 1. Purpose: Manages persistent disk storage and high-score ranking.
 * 2. Data Structures Used:
 *    - Java List of HighScoreRecord models.
 * 3. Algorithm Linkage:
 *    - Calls HillClimbAlgorithms.selectionSortLeaderboard (O(n²)) to maintain
 *      descending order sorted rankings in real-time.
 * 4. File I/O:
 *    - Reads and writes to 'leaderboard.txt'.
 *    - Formatted as 'PlayerName:Score:DistanceMeters:TotalCoins'.
 * ============================================================================
 */
public class ScoreboardManager {
    private static final String LEADERBOARD_FILE = "leaderboard.txt";
    private static final int MAX_SAVED_RECORDS = 50;

    private static final List<HillClimbAlgorithms.HighScoreRecord> records = new ArrayList<>();
    private static boolean initialized = false;

    /**
     * Initializes the scoreboard and loads saved records from disk.
     */
    public static synchronized void init() {
        if (initialized) return;
        loadRecords();
        initialized = true;
    }

    // =========================================================================
    // [1] PERSISTENT FILE I/O (LOAD & SAVE)
    // =========================================================================

    /**
     * Loads high score records from 'leaderboard.txt' or seeds initial champions.
     */
    private static synchronized void loadRecords() {
        records.clear();
        File file = new File(LEADERBOARD_FILE);

        if (!file.exists()) {
            // Seed default hall of fame records if file does not exist
            records.add(new HillClimbAlgorithms.HighScoreRecord("Speed_Demon", 1250, 4200, 58200));
            records.add(new HillClimbAlgorithms.HighScoreRecord("KDU_Champion", 980, 3100, 42100));
            records.add(new HillClimbAlgorithms.HighScoreRecord("Rider_Alex", 640, 1850, 24500));
            records.add(new HillClimbAlgorithms.HighScoreRecord("Turbo_Max", 450, 1200, 16800));
            records.add(new HillClimbAlgorithms.HighScoreRecord("Rookie_Driver", 280, 850, 9200));
            saveRecords();
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split(":");
                if (parts.length >= 4) {
                    String name = parts[0].trim();
                    int score = Integer.parseInt(parts[1].trim());
                    int dist = Integer.parseInt(parts[2].trim());
                    int coins = Integer.parseInt(parts[3].trim());
                    records.add(new HillClimbAlgorithms.HighScoreRecord(name, dist, coins, score));
                }
            }
        } catch (Exception e) {
            System.err.println("Notice: Using memory storage: " + e.getMessage());
        }

        sortRecords();
    }

    /**
     * Saves sorted records back to 'leaderboard.txt'.
     */
    private static synchronized void saveRecords() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(LEADERBOARD_FILE))) {
            writer.println("# Hill Climb Racing - High Score Leaderboard");
            writer.println("# PlayerName:Score:DistanceMeters:TotalCoins");
            for (HillClimbAlgorithms.HighScoreRecord rec : records) {
                writer.println(rec.getPlayerName() + ":" + rec.getScore() + ":" + rec.getDistanceMeters() + ":" + rec.getTotalCoins());
            }
        } catch (Exception e) {
            System.err.println("Notice: Could not save leaderboard: " + e.getMessage());
        }
    }

    // =========================================================================
    // [2] SELECTION SORT INTEGRATION (DSA REQUIREMENT)
    // =========================================================================

    /**
     * Sorts leaderboard records using Selection Sort from HillClimbAlgorithms (O(n²)).
     */
    public static synchronized void sortRecords() {
        if (records.isEmpty()) return;
        HillClimbAlgorithms.HighScoreRecord[] arr = records.toArray(new HillClimbAlgorithms.HighScoreRecord[0]);
        HillClimbAlgorithms.selectionSortLeaderboard(arr, arr.length);
        records.clear();
        for (HillClimbAlgorithms.HighScoreRecord rec : arr) {
            records.add(rec);
        }
    }

    // =========================================================================
    // [3] SCORE RECORDING & STATS LOOKUP
    // =========================================================================

    /**
     * Records or updates a player's score.
     * @return true if this was a new personal best score, false otherwise.
     */
    public static synchronized boolean recordScore(String playerName, int score, int distanceMeters, int coins) {
        init();
        if (playerName == null || playerName.trim().isEmpty()) {
            playerName = "Driver";
        }
        playerName = playerName.trim();

        boolean isNewBest = false;
        int existingIdx = -1;

        for (int i = 0; i < records.size(); i++) {
            if (records.get(i).getPlayerName().equalsIgnoreCase(playerName)) {
                existingIdx = i;
                break;
            }
        }

        if (existingIdx != -1) {
            HillClimbAlgorithms.HighScoreRecord current = records.get(existingIdx);
            if (score > current.getScore()) {
                records.set(existingIdx, new HillClimbAlgorithms.HighScoreRecord(playerName, distanceMeters, coins, score));
                isNewBest = true;
            }
        } else {
            records.add(new HillClimbAlgorithms.HighScoreRecord(playerName, distanceMeters, coins, score));
            isNewBest = true;
        }

        sortRecords();

        // Limit size to top MAX_SAVED_RECORDS
        while (records.size() > MAX_SAVED_RECORDS) {
            records.remove(records.size() - 1);
        }

        saveRecords();
        return isNewBest;
    }

    /**
     * Retrieves the best score for a specific player name.
     */
    public static synchronized int getPlayerBestScore(String playerName) {
        init();
        if (playerName == null || playerName.trim().isEmpty()) return 0;
        String target = playerName.trim();
        for (HillClimbAlgorithms.HighScoreRecord rec : records) {
            if (rec.getPlayerName().equalsIgnoreCase(target)) {
                return rec.getScore();
            }
        }
        return 0;
    }

    /**
     * Gets 1-based leaderboard rank for a player.
     * @return rank position (1 for 1st place), or -1 if unranked.
     */
    public static synchronized int getPlayerRank(String playerName) {
        init();
        if (playerName == null || playerName.trim().isEmpty()) return -1;
        String target = playerName.trim();
        for (int i = 0; i < records.size(); i++) {
            if (records.get(i).getPlayerName().equalsIgnoreCase(target)) {
                return i + 1;
            }
        }
        return -1;
    }

    /**
     * Returns top N records for leaderboard display.
     */
    public static synchronized List<HillClimbAlgorithms.HighScoreRecord> getTopRecords(int maxCount) {
        init();
        int count = Math.min(maxCount, records.size());
        List<HillClimbAlgorithms.HighScoreRecord> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add(records.get(i));
        }
        return list;
    }
}
