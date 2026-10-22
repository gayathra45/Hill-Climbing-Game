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

    // Game Stats
    private int score = 340;
    private int coins = 24280;
    private int stage = 4;
    private int distanceMeters = 0;
    private String currentStunt = null;
    private int stuntDisplayTimer = 0;

    // Controls
    private boolean gasPressed = false;
    private boolean brakePressed = false;

    // Game state
    private boolean gameOver = false;
    private String gameOverReason = "";

    public GamePanel() {
        setBackground(new Color(125, 211, 252));
        setPreferredSize(new Dimension(1024, 576));
        setFocusable(true);

        initGame();
        setupInput();
        startGameLoop();
    }

    private void initGame() {
        terrain = new Terrain();
        particles = new ParticleManager();
        collectibles = new CustomLinkedList<>();

        // Start vehicle at beginning of road
        vehicle = new Vehicle(120, terrain.getHeightAt(120) - 25);

        // Spawn arc coin clusters along hills and bridges (Just like in the photo!)
        spawnCoinArcs();

        // Spawn fuel cans at regular intervals
        for (double fx = 650; fx <= 12000; fx += 800) {
            collectibles.addLast(new Collectible(fx, terrain.getHeightAt(fx) - 30, Collectible.Type.FUEL));
        }

        // Spawn bonus stars
        for (double sx = 950; sx <= 12000; sx += 1200) {
            collectibles.addLast(new Collectible(sx, terrain.getHeightAt(sx) - 75, Collectible.Type.BONUS_STAR));
        }

        score = 340;
        coins = 24280;
        stage = 4;
        distanceMeters = 0;
        gameOver = false;
        gameOverReason = "";
    }

    private void spawnCoinArcs() {
        // Coin arc over the first bridge (X: 620 to 920)
        double[] arcOffsets = {620, 660, 700, 740, 780, 820, 860, 900};
        for (int i = 0; i < arcOffsets.length; i++) {
            double cx = arcOffsets[i];
            double cy = terrain.getHeightAt(cx) - 60 - Math.sin((double) i / (arcOffsets.length - 1) * Math.PI) * 35;
            Collectible.Type t = (i < 2) ? Collectible.Type.COIN_25 : Collectible.Type.COIN_5;
            collectibles.addLast(new Collectible(cx, cy, t));
        }

        // Additional repeating jump coin arcs over rolling hill crests
        for (double base = 1200; base < 12000; base += 950) {
            for (int i = 0; i < 7; i++) {
                double cx = base + i * 45;
                double cy = terrain.getHeightAt(cx) - 50 - Math.sin((double) i / 6.0 * Math.PI) * 35;
                Collectible.Type t = (i == 3) ? Collectible.Type.COIN_25 : Collectible.Type.COIN_5;
                collectibles.addLast(new Collectible(cx, cy, t));
            }
        }
    }

    private void setupInput() {
        KeyAdapter ka = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) {
                    gasPressed = true;
                    vehicle.gasInput = true;
                } else if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) {
                    brakePressed = true;
                    vehicle.brakeInput = true;
                } else if (code == KeyEvent.VK_SPACE || code == KeyEvent.VK_UP || code == KeyEvent.VK_W) {
                    vehicle.isBoosting = true;
                } else if (code == KeyEvent.VK_R) {
                    initGame();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int code = e.getKeyCode();
                if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) {
                    gasPressed = false;
                    vehicle.gasInput = false;
                } else if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) {
                    brakePressed = false;
                    vehicle.brakeInput = false;
                } else if (code == KeyEvent.VK_SPACE || code == KeyEvent.VK_UP || code == KeyEvent.VK_W) {
                    vehicle.isBoosting = false;
                }
            }
        };

        addKeyListener(ka);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
            }
        });
    }

    private void startGameLoop() {
        Timer timer = new Timer(16, e -> {
            updateGame();
            repaint();
        });
        timer.start();
    }

    private void updateGame() {
        if (!gameOver) {
            vehicle.update(terrain, particles);
            particles.update();

            // Distance travelled
            distanceMeters = Math.max(distanceMeters, (int) (vehicle.x / 10.0));

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
                gameOver = true;
                gameOverReason = "DRIVER DOWN! HEAD IMPACT";
            } else if (vehicle.fuel <= 0 && Math.abs(vehicle.vx) < 0.1 && vehicle.rearGrounded) {
                gameOver = true;
                gameOverReason = "OUT OF FUEL!";
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

        // 5. Render Dashboard Gauges, Pedals, Score & Stage HUD
        DashboardHUD.render(g2, w, h, vehicle, score, coins, stage, distanceMeters, 
                           currentStunt, gasPressed, brakePressed);

        // 6. Game Over Screen
        if (gameOver) {
            g2.setColor(new Color(15, 23, 42, 220));
            g2.fillRect(0, 0, w, h);

            g2.setFont(new Font("Impact", Font.BOLD, 46));
            g2.setColor(new Color(239, 68, 68));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(gameOverReason, (w - fm.stringWidth(gameOverReason)) / 2, h / 2 - 40);

            g2.setFont(new Font("Impact", Font.BOLD, 26));
            g2.setColor(Color.WHITE);
            String distStr = "DISTANCE REACHED: " + distanceMeters + " METERS";
            g2.drawString(distStr, (w - g2.getFontMetrics().stringWidth(distStr)) / 2, h / 2 + 10);

            g2.setFont(new Font("Impact", Font.BOLD, 22));
            g2.setColor(new Color(251, 191, 36));
            String restartStr = "PRESS [R] TO PLAY AGAIN";
            g2.drawString(restartStr, (w - g2.getFontMetrics().stringWidth(restartStr)) / 2, h / 2 + 55);
        }

        g2.dispose();
    }
}
