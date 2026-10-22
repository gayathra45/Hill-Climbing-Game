import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * 60 FPS Hill Climb Racing Game Engine Panel.
 * 
 * Features:
 * - Dynamic camera with velocity look-ahead.
 * - Arc-pattern coin cluster spawning (matching the photo!).
 * - Interactive arrow keys and WASD driving with pedal animations.
 * - Stunt combo multiplier evaluation using the CustomStack and recursive bonus formula.
 */
public class GamePanel extends JPanel {
    private static final long serialVersionUID = 1L;

    // Core Game Systems
    private Terrain terrain;
    private Vehicle vehicle;
    private ParticleManager particles;
    private CustomLinkedList<Collectible> collectibles;

    // Camera
    private double camX = 0;
    private double camY = 0;

    // Game States
    public enum GameState {
        LOGIN,
        PLAYING,
        SCOREBOARD
    }

    private GameState gameState = GameState.LOGIN;
    private GameState previousState = GameState.LOGIN;
    private String playerName = "Rider_Alex";
    private int playerBestScore = 0;
    private boolean isNewBestRecord = false;
    private boolean scoreRecorded = false;
    private JTextField nameField;

    // Game Stats (Fresh run starts from 0)
    private int score = 0;
    private int coins = 0;
    private int stage = 1;
    private int distanceMeters = 0;
    private String currentStunt = null;
    private int stuntDisplayTimer = 0;

    // Controls State (combining keyboard and mouse pedals)
    private boolean keyGasPressed = false;
    private boolean keyBrakePressed = false;
    private boolean mouseGasPressed = false;
    private boolean mouseBrakePressed = false;
    private boolean gasPressed = false;
    private boolean brakePressed = false;

    // Game state
    private boolean isPaused = false;
    private boolean gameOver = false;
    private String gameOverReason = "";
    private int crashAnimationTimer = 85;

    public GamePanel() {
        setLayout(null);
        setBackground(new Color(125, 211, 252));
        setPreferredSize(new Dimension(1024, 576));
        setFocusable(true);

        // Initialize persistent scoreboard & load last pilot
        ScoreboardManager.init();
        playerName = ScoreboardManager.getLastPlayerName();
        playerBestScore = ScoreboardManager.getPlayerBestScore(playerName);

        // High-tech Driver Name Input Field on Login Card
        nameField = new JTextField(playerName);
        nameField.setFont(new Font("Impact", Font.PLAIN, 22));
        nameField.setHorizontalAlignment(JTextField.CENTER);
        nameField.setBackground(new Color(15, 23, 42));
        nameField.setForeground(new Color(253, 224, 71));
        nameField.setCaretColor(Color.WHITE);
        nameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(56, 189, 248), 2, true),
            BorderFactory.createEmptyBorder(6, 14, 6, 14)
        ));
        nameField.addActionListener(e -> startRaceFromLogin());
        add(nameField);

        initGame();
        setupInput();
        startGameLoop();
    }

    @Override
    public void doLayout() {
        super.doLayout();
        if (nameField != null) {
            int mw = 520;
            int mh = 370;
            int mx = (getWidth() - mw) / 2;
            int my = (getHeight() - mh) / 2;
            nameField.setBounds(mx + (mw - 320) / 2, my + 148, 320, 44);
        }
    }

    public void startRaceFromLogin() {
        String input = nameField.getText().trim();
        if (input.isEmpty()) input = "Guest_Rider";
        playerName = input;
        ScoreboardManager.saveLastPlayerName(playerName);
        playerBestScore = ScoreboardManager.getPlayerBestScore(playerName);

        nameField.setVisible(false);
        nameField.setEnabled(false);
        gameState = GameState.PLAYING;
        restartGame();
        requestFocusInWindow();
        repaint();
    }

    public void openLeaderboard() {
        if (gameState != GameState.SCOREBOARD) {
            previousState = gameState;
            gameState = GameState.SCOREBOARD;
            if (nameField != null) {
                nameField.setVisible(false);
                nameField.setEnabled(false);
            }
            repaint();
        }
    }

    public void closeLeaderboard() {
        if (gameState == GameState.SCOREBOARD) {
            gameState = previousState;
            if (gameState == GameState.LOGIN) {
                if (nameField != null) {
                    nameField.setVisible(true);
                    nameField.setEnabled(true);
                    nameField.requestFocusInWindow();
                }
            } else {
                requestFocusInWindow();
            }
            repaint();
        }
    }

    public void changeDriver() {
        gameState = GameState.LOGIN;
        isPaused = false;
        gameOver = false;
        if (nameField != null) {
            nameField.setText(playerName);
            nameField.setVisible(true);
            nameField.setEnabled(true);
            nameField.selectAll();
            nameField.requestFocusInWindow();
        }
        repaint();
    }

    public void restartGame() {
        // Save current round score to the scoreboard before resetting
        if (score > 0 && !scoreRecorded) {
            ScoreboardManager.recordScore(playerName, score, distanceMeters, coins);
            scoreRecorded = true;
        }
        playerBestScore = ScoreboardManager.getPlayerBestScore(playerName);
        initGame();
        requestFocusInWindow();
        repaint();
    }

    private void initGame() {
        terrain = new Terrain();
        particles = new ParticleManager();
        collectibles = new CustomLinkedList<>();

        // Start vehicle at beginning of road with full fuel
        vehicle = new Vehicle(120, terrain.getHeightAt(120) - 25);
        vehicle.fuel = 100.0;

        // Spawn arc coin clusters along hills and bridges
        spawnCoinArcs();

        // Spawn fuel cans at regular intervals across extended map
        for (double fx = 650; fx <= 50000; fx += 800) {
            collectibles.addLast(new Collectible(fx, terrain.getHeightAt(fx) - 32, Collectible.Type.FUEL));
        }

        // Spawn bonus stars
        for (double sx = 950; sx <= 50000; sx += 1200) {
            collectibles.addLast(new Collectible(sx, terrain.getHeightAt(sx) - 75, Collectible.Type.BONUS_STAR));
        }

        // Fresh round: Clear previous round details completely
        score = 0;
        coins = 0;
        stage = 1;
        distanceMeters = 0;
        currentStunt = null;
        stuntDisplayTimer = 0;
        camX = 0;
        camY = 0;

        gameOver = false;
        gameOverReason = "";
        crashAnimationTimer = 85;
        isPaused = false;
        scoreRecorded = false;
        isNewBestRecord = false;
        keyGasPressed = false;
        keyBrakePressed = false;
        mouseGasPressed = false;
        mouseBrakePressed = false;
        gasPressed = false;
        brakePressed = false;
    }

    private void spawnCoinArcs() {
        // 1. Rainbow Jump Arc over the bridge (Exact match to the reference photo!)
        // In the photo: 7 coins arching over the wooden suspension bridge (600 to 940)
        double bStartX = 600;
        double bEndX = 940;
        double startY = terrain.getHeightAt(bStartX);
        double endY = terrain.getHeightAt(bEndX);

        for (int i = 0; i < 7; i++) {
            double t = (double) i / 6.0;
            double cx = bStartX + (bEndX - bStartX) * (0.08 + 0.84 * t);
            double baseLineY = startY + (endY - startY) * t;
            double cy = baseLineY - 48.0 - Math.sin(t * Math.PI) * 72.0;
            Collectible.Type tType = (i < 2) ? Collectible.Type.COIN_25 : Collectible.Type.COIN_5;
            collectibles.addLast(new Collectible(cx, cy, tType));
        }

        // 2. Beautiful Hill-Hugging Coin Chains along rolling crests:
        // Every coin follows the terrain slope smoothly at an exact uniform height (terrain.getHeightAt(cx) - 36.0)
        for (double base = 1150; base < 50000; base += 750) {
            if (terrain.isBridgeAt(base) || terrain.isBridgeAt(base + 240)) {
                // Jump arc across bridge
                for (int i = 0; i < 6; i++) {
                    double t = (double) i / 5.0;
                    double cx = base + i * 42;
                    double cy = terrain.getHeightAt(base) - 45.0 - Math.sin(t * Math.PI) * 60.0;
                    Collectible.Type tType = (i == 2 || i == 3) ? Collectible.Type.COIN_25 : Collectible.Type.COIN_5;
                    collectibles.addLast(new Collectible(cx, cy, tType));
                }
            } else {
                // Smooth terrain-contour-following coin chain (hugs the hill curves perfectly!)
                for (int i = 0; i < 6; i++) {
                    double cx = base + i * 40;
                    double cy = terrain.getHeightAt(cx) - 36.0; // Exactly hugs the hill contour!
                    Collectible.Type tType = (i == 2) ? Collectible.Type.COIN_25 : Collectible.Type.COIN_5;
                    collectibles.addLast(new Collectible(cx, cy, tType));
                }
            }
        }
    }

    private void togglePause() {
        if (gameOver) return;
        isPaused = !isPaused;
        if (isPaused) {
            keyGasPressed = false;
            keyBrakePressed = false;
            mouseGasPressed = false;
            mouseBrakePressed = false;
            vehicle.isBoosting = false;
            syncInputs();
        }
    }

    private void syncInputs() {
        gasPressed = keyGasPressed || mouseGasPressed;
        brakePressed = keyBrakePressed || mouseBrakePressed;
        vehicle.gasInput = gasPressed;
        vehicle.brakeInput = brakePressed;
    }

    private void setupInput() {
        KeyAdapter ka = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();

                // 1. If currently on Leaderboard Scoreboard screen
                if (gameState == GameState.SCOREBOARD) {
                    if (code == KeyEvent.VK_ESCAPE || code == KeyEvent.VK_L || code == KeyEvent.VK_ENTER) {
                        closeLeaderboard();
                    }
                    return;
                }

                // 2. If currently on Driver Name Login screen
                if (gameState == GameState.LOGIN) {
                    if (code == KeyEvent.VK_ENTER) {
                        startRaceFromLogin();
                    } else if (code == KeyEvent.VK_L) {
                        openLeaderboard();
                    }
                    return;
                }

                // 3. If Game Over screen
                if (gameOver) {
                    if (code == KeyEvent.VK_R) {
                        restartGame();
                    } else if (code == KeyEvent.VK_L) {
                        openLeaderboard();
                    } else if (code == KeyEvent.VK_C) {
                        changeDriver();
                    }
                    return;
                }

                // 4. In-Game Controls
                if (code == KeyEvent.VK_P || code == KeyEvent.VK_ESCAPE) {
                    togglePause();
                    repaint();
                } else if (code == KeyEvent.VK_L) {
                    openLeaderboard();
                } else if (code == KeyEvent.VK_C) {
                    changeDriver();
                } else if (!isPaused) {
                    if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) {
                        keyGasPressed = true;
                        syncInputs();
                    } else if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) {
                        keyBrakePressed = true;
                        syncInputs();
                    } else if (code == KeyEvent.VK_SPACE || code == KeyEvent.VK_UP || code == KeyEvent.VK_W) {
                        vehicle.isBoosting = true;
                    } else if (code == KeyEvent.VK_V) {
                        vehicle.switchVehicle();
                    }
                }
                if (code == KeyEvent.VK_R) {
                    restartGame();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int code = e.getKeyCode();
                if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) {
                    keyGasPressed = false;
                    syncInputs();
                } else if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) {
                    keyBrakePressed = false;
                    syncInputs();
                } else if (code == KeyEvent.VK_SPACE || code == KeyEvent.VK_UP || code == KeyEvent.VK_W) {
                    vehicle.isBoosting = false;
                }
            }
        };

        addKeyListener(ka);

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                int mx = e.getX();
                int my = e.getY();
                int w = getWidth();
                int h = getHeight();

                // 1. Leaderboard Scoreboard State
                if (gameState == GameState.SCOREBOARD) {
                    if (DashboardHUD.isScoreboardCloseButtonClicked(w, h, mx, my) || 
                        mx < (w - 700) / 2 || mx > (w + 700) / 2 || my < (h - 480) / 2 || my > (h + 480) / 2) {
                        closeLeaderboard();
                    }
                    return;
                }

                // 2. Login Screen State
                if (gameState == GameState.LOGIN) {
                    if (DashboardHUD.isStartRaceButtonClicked(w, h, mx, my)) {
                        startRaceFromLogin();
                    } else if (DashboardHUD.isLoginLeaderboardButtonClicked(w, h, mx, my)) {
                        openLeaderboard();
                    }
                    return;
                }

                // 3. Game Over Screen State
                if (gameOver) {
                    if (DashboardHUD.isGameOverPlayAgainClicked(w, h, mx, my)) {
                        restartGame();
                    } else if (DashboardHUD.isGameOverLeaderboardClicked(w, h, mx, my)) {
                        openLeaderboard();
                    } else if (DashboardHUD.isGameOverChangeDriverClicked(w, h, mx, my)) {
                        changeDriver();
                    }
                    return;
                }

                // 4. In-Game Trophy (Leaderboard) Button
                if (DashboardHUD.isTrophyButtonClicked(w, mx, my)) {
                    openLeaderboard();
                    return;
                }

                // 5. In-Game Pause / Play Button
                if (DashboardHUD.isPauseButtonClicked(w, mx, my)) {
                    togglePause();
                    repaint();
                    return;
                }

                // 6. In Pause Menu
                if (isPaused) {
                    if (DashboardHUD.isResumeButtonClicked(w, h, mx, my)) {
                        togglePause();
                        repaint();
                    } else if (DashboardHUD.isPauseLeaderboardClicked(w, h, mx, my)) {
                        openLeaderboard();
                        repaint();
                    }
                    return;
                }

                // 7. Driving Foot Pedals: Brake and Gas
                if (DashboardHUD.isBrakePedalHovered(h, mx, my)) {
                    mouseBrakePressed = true;
                    syncInputs();
                    repaint();
                } else if (DashboardHUD.isGasPedalHovered(w, h, mx, my)) {
                    mouseGasPressed = true;
                    syncInputs();
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mouseBrakePressed = false;
                mouseGasPressed = false;
                syncInputs();
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (isPaused || gameOver || gameState != GameState.PLAYING) return;
                int mx = e.getX();
                int my = e.getY();
                int w = getWidth();
                int h = getHeight();

                boolean inBrake = DashboardHUD.isBrakePedalHovered(h, mx, my);
                boolean inGas = DashboardHUD.isGasPedalHovered(w, h, mx, my);

                if (inBrake != mouseBrakePressed || inGas != mouseGasPressed) {
                    mouseBrakePressed = inBrake;
                    mouseGasPressed = inGas;
                    syncInputs();
                    repaint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                int mx = e.getX();
                int my = e.getY();
                int w = getWidth();
                int h = getHeight();

                if (gameState == GameState.SCOREBOARD) {
                    if (DashboardHUD.isScoreboardCloseButtonClicked(w, h, mx, my)) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    } else {
                        setCursor(Cursor.getDefaultCursor());
                    }
                } else if (gameState == GameState.LOGIN) {
                    if (DashboardHUD.isStartRaceButtonClicked(w, h, mx, my) ||
                        DashboardHUD.isLoginLeaderboardButtonClicked(w, h, mx, my)) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    } else {
                        setCursor(Cursor.getDefaultCursor());
                    }
                } else if (gameOver) {
                    if (DashboardHUD.isGameOverPlayAgainClicked(w, h, mx, my) ||
                        DashboardHUD.isGameOverLeaderboardClicked(w, h, mx, my) ||
                        DashboardHUD.isGameOverChangeDriverClicked(w, h, mx, my)) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    } else {
                        setCursor(Cursor.getDefaultCursor());
                    }
                } else if (DashboardHUD.isTrophyButtonClicked(w, mx, my) ||
                           DashboardHUD.isPauseButtonClicked(w, mx, my) ||
                           (isPaused && (DashboardHUD.isResumeButtonClicked(w, h, mx, my) || DashboardHUD.isPauseLeaderboardClicked(w, h, mx, my))) ||
                           DashboardHUD.isBrakePedalHovered(h, mx, my) ||
                           DashboardHUD.isGasPedalHovered(w, h, mx, my)) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };

        addMouseListener(ma);
        addMouseMotionListener(ma);
    }

    private void startGameLoop() {
        Timer timer = new Timer(16, e -> {
            updateGame();
            repaint();
        });
        timer.start();
    }

    private void updateGame() {
        if (gameState != GameState.PLAYING || isPaused) {
            return;
        }

        if (!gameOver) {
            vehicle.update(terrain, particles);
            particles.update();

            // Distance travelled
            distanceMeters = Math.max(distanceMeters, (int) (vehicle.x / 10.0));

            // Progressive Multi-Stage Difficulty:
            // Stage 1: 0m - 1000m (Normal)
            // Stage 2: 1000m - 2200m (Intermediate)
            // Stage 3: 2200m - 3600m (Hard)
            // Stage 4: 3600m+ (Extreme)
            int prevStage = stage;
            if (distanceMeters < 1000) {
                stage = 1;
            } else if (distanceMeters < 2200) {
                stage = 2;
            } else if (distanceMeters < 3600) {
                stage = 3;
            } else {
                stage = 4;
            }

            if (stage > prevStage) {
                String stageName = (stage == 2) ? "STAGE 2: ROLLING RIDGES" : (stage == 3) ? "STAGE 3: CANYON CLIFFS" : "STAGE 4: MOUNTAIN APEX";
                currentStunt = "LEVEL UP! " + stageName + " +2000";
                stuntDisplayTimer = 95;
                score += 2000;
                particles.addCoinSparkles(vehicle.x, vehicle.y - 20);
                particles.addFloatingText(vehicle.x, vehicle.y - 45, stageName, new Color(253, 224, 71));
            }

            // Check coin and fuel collisions
            checkCollisions();

            // Pull stunts from CustomStack
            if (!vehicle.stuntStack.isEmpty()) {
                currentStunt = vehicle.stuntStack.pop();
                stuntDisplayTimer = 65;

                if (currentStunt.contains("+")) {
                    String[] parts = currentStunt.split("\\+");
                    try {
                        int basePts = Integer.parseInt(parts[1].trim());
                        int comboBonus = HillClimbAlgorithms.recursiveComboBonus(2, basePts);
                        score += comboBonus;
                    } catch (Exception ex) {
                        score += 150;
                    }
                }
            }

            if (stuntDisplayTimer > 0) {
                stuntDisplayTimer--;
                if (stuntDisplayTimer == 0) currentStunt = null;
            }

            // Game over checks
            if (vehicle.crashed) {
                if (crashAnimationTimer > 0) {
                    crashAnimationTimer--;
                } else {
                    gameOver = true;
                    gameOverReason = "DRIVER DOWN! ACCIDENT";
                }
            } else if (vehicle.fuel <= 0 && Math.abs(vehicle.vx) < 0.1 && vehicle.rearGrounded) {
                gameOver = true;
                gameOverReason = "OUT OF FUEL!";
            }
        }

        // Record persistent high score on Game Over
        if (gameOver && !scoreRecorded) {
            isNewBestRecord = ScoreboardManager.recordScore(playerName, score, distanceMeters, coins);
            playerBestScore = ScoreboardManager.getPlayerBestScore(playerName);
            scoreRecorded = true;
            if (isNewBestRecord) {
                particles.addCoinSparkles(vehicle.x, vehicle.y - 30);
                particles.addFloatingText(vehicle.x, vehicle.y - 60, "NEW BEST SCORE!", new Color(253, 224, 71));
            }
        }

        // Camera smoothly follows vehicle with lookahead
        double targetCamX = vehicle.x - getWidth() * 0.32 + vehicle.vx * 8.0;
        double targetCamY = vehicle.y - getHeight() * 0.60;
        camX += (targetCamX - camX) * 0.08;
        camY += (targetCamY - camY) * 0.08;

        // Ground clamping for camera
        camY = Math.max(0, Math.min(240, camY));
    }

    private void checkCollisions() {
        CustomLinkedList.Node<Collectible> curr = collectibles.getHead();
        while (curr != null) {
            Collectible c = curr.data;
            if (!c.isCollected()) {
                c.update();
                double dist = Math.hypot(vehicle.x - c.getX(), vehicle.y - c.getY());
                if (dist < 42.0) {
                    c.setCollected(true);
                    if (c.getType() == Collectible.Type.FUEL) {
                        vehicle.fuel = Math.min(100.0, vehicle.fuel + 60.0);
                        currentStunt = "FUEL CANISTER! +100%";
                        stuntDisplayTimer = 50;
                        score += 100;
                        particles.addCoinSparkles(c.getX(), c.getY());
                        particles.addFloatingText(c.getX(), c.getY() - 12, "+FUEL", new Color(56, 189, 248));
                    } else if (c.getType() == Collectible.Type.BONUS_STAR) {
                        score += 500;
                        currentStunt = "STAR BONUS! +500 PTS";
                        stuntDisplayTimer = 50;
                        particles.addCoinSparkles(c.getX(), c.getY());
                        particles.addFloatingText(c.getX(), c.getY() - 12, "+500", new Color(244, 114, 182));
                    } else {
                        coins += c.getValue();
                        score += c.getValue() * 10;
                        particles.addCoinSparkles(c.getX(), c.getY());
                        particles.addFloatingText(c.getX(), c.getY() - 12, "+" + c.getValue(), new Color(253, 224, 71));
                    }
                }
            }
            curr = curr.next;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        int w = getWidth();
        int h = getHeight();

        // 1. Render Terrain, Countryside Parallax, and Bridges
        terrain.render(g2, w, h, camX, camY);

        // 2. Render Collectibles (Coins, Stars, Fuel)
        CustomLinkedList.Node<Collectible> curr = collectibles.getHead();
        while (curr != null) {
            curr.data.render(g2, camX, camY);
            curr = curr.next;
        }

        // 3. Render Vehicle (Bike, Wheels, Driver)
        vehicle.render(g2, camX, camY);

        // 4. Render Particles (Dirt, Smoke, Sparkles)
        particles.render(g2, camX, camY);

        // 5. Render HUD, Login Screen, Scoreboard Overlay or Game Over Screen
        if (gameState == GameState.LOGIN) {
            DashboardHUD.drawLoginScreen(g2, w, h);
        } else if (gameState == GameState.PLAYING) {
            DashboardHUD.render(g2, w, h, vehicle, score, coins, stage, distanceMeters, 
                               currentStunt, gasPressed, brakePressed, isPaused, playerName, playerBestScore);

            if (gameOver) {
                DashboardHUD.drawGameOverScreen(g2, w, h, gameOverReason, score, distanceMeters, coins, 
                                               playerName, playerBestScore, isNewBestRecord);
            }
        } else if (gameState == GameState.SCOREBOARD) {
            DashboardHUD.drawScoreboardOverlay(g2, w, h, playerName);
        }

        g2.dispose();
    }
}
