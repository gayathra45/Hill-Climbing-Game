import java.io.*;
import java.util.*;

/**
 * Persistent Scoreboard and High Score Management System.
 * 
 * Features:
 * - Persists player best scores to 'leaderboard.txt'.
 * - Remembers last entered player name in 'last_player.txt'.
 * - Sorts records using Selection Sort from HillClimbAlgorithms (KDU IT22013 Viva Requirement).
 * - Tracks player ranks, personal bests, and updates high scores in real time.
 */
public class ScoreboardManager {
    private static final String LEADERBOARD_FILE = "leaderboard.txt";
    private static final String LAST_PLAYER_FILE = "last_player.txt";
    private static final int MAX_SAVED_RECORDS = 50;

    private static final List<HillClimbAlgorithms.HighScoreRecord> records = new ArrayList<>();
    private static boolean initialized = false;

    public static synchronized void init() {
        if (initialized) return;
        loadRecords();
        initialized = true;
    }

    /**
     * Loads high score records from file or seeds initial leaderboard.
     */
    private static synchronized void loadRecords() {
        records.clear();
        File file = new File(LEADERBOARD_FILE);

        if (!file.exists()) {
            // Seed with realistic starting leaderboard records
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
            System.err.println("Notice: Could not load leaderboard from file, using memory storage: " + e.getMessage());
        }

        sortRecords();
    }

    /**
     * Saves sorted records to file.
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

    /**
     * Sorts leaderboard using Selection Sort algorithm from HillClimbAlgorithms.
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

    /**
     * Records or updates a player's score.
     * @return true if this was a new personal best score or new entry, false otherwise.
     */
    public static synchronized boolean recordScore(String playerName, int score, int distanceMeters, int coins) {
        init();
        if (playerName == null || playerName.trim().isEmpty()) {
            playerName = "Guest_Rider";
        }
        playerName = playerName.trim();
        saveLastPlayerName(playerName);

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
     * Retrieves the best score for a specific player.
     */
    public static synchronized int getPlayerBestScore(String playerName) {
        init();
        if (playerName == null) return 0;
        for (HillClimbAlgorithms.HighScoreRecord rec : records) {
            if (rec.getPlayerName().equalsIgnoreCase(playerName.trim())) {
                return rec.getScore();
            }
        }
        return 0;
    }

    /**
     * Retrieves the 1-based leaderboard rank for a player.
     */
    public static synchronized int getPlayerRank(String playerName) {
        init();
        if (playerName == null) return -1;
        for (int i = 0; i < records.size(); i++) {
            if (records.get(i).getPlayerName().equalsIgnoreCase(playerName.trim())) {
                return i + 1;
            }
        }
        return -1;
    }

    /**
     * Returns top records up to maxCount.
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

    /**
     * Gets the last entered driver name.
     */
    public static synchronized String getLastPlayerName() {
        File file = new File(LAST_PLAYER_FILE);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line = reader.readLine();
                if (line != null && !line.trim().isEmpty()) {
                    return line.trim();
                }
            } catch (Exception ignored) {}
        }
        return "Rider_Alex";
    }

    /**
     * Saves the last entered driver name.
     */
    public static synchronized void saveLastPlayerName(String playerName) {
        if (playerName == null || playerName.trim().isEmpty()) return;
        try (PrintWriter writer = new PrintWriter(new FileWriter(LAST_PLAYER_FILE))) {
            writer.println(playerName.trim());
        } catch (Exception ignored) {}
    }
}
