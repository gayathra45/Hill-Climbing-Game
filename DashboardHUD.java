import java.awt.*;
import java.awt.geom.*;

/**
 * Photorealistic Hill Climb Racing Dashboard HUD.
 * 
 * Realistic Visual Enhancements:
 * 1. Triple Analog Instrument Gauges (RPM, Main Fuel/Speedometer, Boost):
 *    - Convex curved domed glass lens reflections.
 *    - Brushed metallic bevel rings and recessed gauge dials.
 *    - High-contrast color-coded arc (Red/Orange/Yellow/Green), calibrated tick marks, and needle drop shadows.
 * 2. Heavy-Duty Aluminum Foot Pedals (Gas & Brake):
 *    - Brushed metal finish with bevelled chamfer edges and recessed knurled rubber traction grip studs.
 *    - Smooth 3D depression animation with floor shadow.
 * 3. Glassmorphic Frosted Pill Panels:
 *    - Score pill with "x2 HOT" combustion badge and coin counter.
 *    - Cyan "STAGE 4" championship banner and low-fuel alarm.
 *    - Target distance milestone indicators with checkered flag and mountain summit pins.
 */
public class DashboardHUD {

    public static final int BTN_ROW_Y = 108;
    public static final int BTN_ROW_H = 36;
    public static final int ACTION_BTN_W = 70;

    public static final int PEDAL_W = 78;
    public static final int PEDAL_H = 112;
    public static final int PEDAL_MARGIN_X = 35;
    public static final int PEDAL_MARGIN_BOTTOM = 24;

    public static boolean isTrophyButtonClicked(int screenW, int mouseX, int mouseY) {
        int tx = screenW - 175;
        int ty = BTN_ROW_Y;
        return mouseX >= tx && mouseX <= tx + ACTION_BTN_W && mouseY >= ty && mouseY <= ty + BTN_ROW_H;
    }

    public static boolean isPauseButtonClicked(int screenW, int mouseX, int mouseY) {
        int px = screenW - 95;
        int py = BTN_ROW_Y;
        return mouseX >= px && mouseX <= px + ACTION_BTN_W && mouseY >= py && mouseY <= py + BTN_ROW_H;
    }

    public static boolean isBrakePedalHovered(int screenH, int mouseX, int mouseY) {
        int padY = screenH - PEDAL_H - PEDAL_MARGIN_BOTTOM;
        return mouseX >= PEDAL_MARGIN_X && mouseX <= PEDAL_MARGIN_X + PEDAL_W &&
               mouseY >= padY && mouseY <= padY + PEDAL_H;
    }

    public static boolean isGasPedalHovered(int screenW, int screenH, int mouseX, int mouseY) {
        int padY = screenH - PEDAL_H - PEDAL_MARGIN_BOTTOM;
        int gx = screenW - PEDAL_W - PEDAL_MARGIN_X;
        return mouseX >= gx && mouseX <= gx + PEDAL_W &&
               mouseY >= padY && mouseY <= padY + PEDAL_H;
    }

    public static boolean isResumeButtonClicked(int screenW, int screenH, int mouseX, int mouseY) {
        int mw = 420;
        int mh = 260;
        int mx = (screenW - mw) / 2;
        int my = (screenH - mh) / 2;
        int rw = 220;
        int rh = 44;
        int rx = mx + (mw - rw) / 2;
        int ry = my + 105;
        return mouseX >= rx && mouseX <= rx + rw && mouseY >= ry && mouseY <= ry + rh;
    }

    public static boolean isPauseLeaderboardClicked(int screenW, int screenH, int mouseX, int mouseY) {
        int mw = 420;
        int mh = 260;
        int mx = (screenW - mw) / 2;
        int my = (screenH - mh) / 2;
        int lw = 220;
        int lh = 38;
        int lx = mx + (mw - lw) / 2;
        int ly = my + 158;
        return mouseX >= lx && mouseX <= lx + lw && mouseY >= ly && mouseY <= ly + lh;
    }

    public static boolean isScoreboardCloseButtonClicked(int screenW, int screenH, int mouseX, int mouseY) {
        int mw = 700;
        int mh = 480;
        int mx = (screenW - mw) / 2;
        int my = (screenH - mh) / 2;
        int bw = 240;
        int bh = 42;
        int bx = mx + (mw - bw) / 2;
        int by = my + mh - 58;
        return mouseX >= bx && mouseX <= bx + bw && mouseY >= by && mouseY <= by + bh;
    }

    public static boolean isStartRaceButtonClicked(int screenW, int screenH, int mouseX, int mouseY) {
        int mw = 520;
        int mh = 370;
        int mx = (screenW - mw) / 2;
        int my = (screenH - mh) / 2;
        int bw = 260;
        int bh = 46;
        int bx = mx + (mw - bw) / 2;
        int by = my + 215;
        return mouseX >= bx && mouseX <= bx + bw && mouseY >= by && mouseY <= by + bh;
    }

    public static boolean isLoginLeaderboardButtonClicked(int screenW, int screenH, int mouseX, int mouseY) {
        int mw = 520;
        int mh = 370;
        int mx = (screenW - mw) / 2;
        int my = (screenH - mh) / 2;
        int lw = 260;
        int lh = 38;
        int lx = mx + (mw - lw) / 2;
        int ly = my + 272;
        return mouseX >= lx && mouseX <= lx + lw && mouseY >= ly && mouseY <= ly + lh;
    }

    public static boolean isGameOverPlayAgainClicked(int screenW, int screenH, int mouseX, int mouseY) {
        int mw = 580;
        int mh = 390;
        int mx = (screenW - mw) / 2;
        int my = (screenH - mh) / 2;
        int bw = 155;
        int bh = 42;
        int by = my + mh - 65;
        int b1x = mx + 30;
        return mouseX >= b1x && mouseX <= b1x + bw && mouseY >= by && mouseY <= by + bh;
    }

    public static boolean isGameOverLeaderboardClicked(int screenW, int screenH, int mouseX, int mouseY) {
        int mw = 580;
        int mh = 390;
        int mx = (screenW - mw) / 2;
        int my = (screenH - mh) / 2;
        int bw = 155;
        int bh = 42;
        int by = my + mh - 65;
        int b2x = mx + 30 + 175;
        return mouseX >= b2x && mouseX <= b2x + bw && mouseY >= by && mouseY <= by + bh;
    }

    public static boolean isGameOverChangeDriverClicked(int screenW, int screenH, int mouseX, int mouseY) {
        int mw = 580;
        int mh = 390;
        int mx = (screenW - mw) / 2;
        int my = (screenH - mh) / 2;
        int bw = 155;
        int bh = 42;
        int by = my + mh - 65;
        int b3x = mx + 30 + 350;
        return mouseX >= b3x && mouseX <= b3x + bw && mouseY >= by && mouseY <= by + bh;
    }

    public static void render(Graphics2D g2, int screenW, int screenH, Vehicle vehicle, 
                              int score, int coins, int stage, int distanceMeters, 
                              String currentStunt, boolean gasPressed, boolean brakePressed) {
        render(g2, screenW, screenH, vehicle, score, coins, stage, distanceMeters, 
               currentStunt, gasPressed, brakePressed, false, "Rider", 0);
    }

    public static void render(Graphics2D g2, int screenW, int screenH, Vehicle vehicle, 
                              int score, int coins, int stage, int distanceMeters, 
                              String currentStunt, boolean gasPressed, boolean brakePressed, boolean isPaused) {
        render(g2, screenW, screenH, vehicle, score, coins, stage, distanceMeters, 
               currentStunt, gasPressed, brakePressed, isPaused, "Rider", 0);
    }

    public static void render(Graphics2D g2, int screenW, int screenH, Vehicle vehicle, 
                              int score, int coins, int stage, int distanceMeters, 
                              String currentStunt, boolean gasPressed, boolean brakePressed, 
                              boolean isPaused, String playerName, int bestScore) {

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 1. TOP LEFT: SCORE, HOT MULTIPLIER, COINS, ACTIVE VEHICLE, DRIVER & BEST SCORE
        drawScorePanel(g2, score, coins, vehicle, playerName, bestScore);

        // 2. TOP CENTER: STAGE & BONUS
        drawStageBanner(g2, screenW, stage, vehicle.fuel);

        // 3. TOP RIGHT: DISTANCE MILESTONES, LEADERBOARD TROPHY & PAUSE BUTTON
        drawMilestones(g2, screenW, distanceMeters, isPaused);

        // 4. BOTTOM CORNERS: REALISTIC METAL FOOT PEDALS
        drawPedals(g2, screenW, screenH, brakePressed, gasPressed);

        // 5. BOTTOM CENTER: TRIPLE ANALOG INSTRUMENT GAUGES
        drawGauges(g2, screenW, screenH, vehicle);

        // 6. STUNT POPUP BANNER (LIFO Stack Top Event)
        if (currentStunt != null && !currentStunt.isEmpty()) {
            drawStuntPopup(g2, screenW, currentStunt);
        }

        // 7. KEYBOARD & MOUSE CONTROLS HELPER OVERLAY
        drawControlsHint(g2, screenW, screenH);

        // 8. PAUSED MODAL OVERLAY
        if (isPaused) {
            drawPauseOverlay(g2, screenW, screenH);
        }
    }

    private static void drawScorePanel(Graphics2D g2, int score, int coins, Vehicle vehicle, String playerName, int bestScore) {
        int x = 20;
        int y = 20;

        // Frosted glassmorphic pill background with subtle glow
        g2.setColor(new Color(15, 23, 42, 200));
        g2.fillRoundRect(x, y, 168, 50, 25, 25);
        g2.setColor(new Color(255, 255, 255, 40));
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(x, y, 168, 50, 25, 25);
        g2.setStroke(new BasicStroke(1));

        g2.setFont(new Font("Impact", Font.PLAIN, 12));
        g2.setColor(new Color(148, 163, 184));
        g2.drawString("SCORE", x + 18, y + 18);

        g2.setFont(new Font("Impact", Font.BOLD, 24));
        g2.setColor(Color.WHITE);
        g2.drawString(String.valueOf(score), x + 18, y + 42);

        // Red "x2 HOT" multiplier badge
        g2.setColor(new Color(239, 68, 68));
        g2.setFont(new Font("Impact", Font.BOLD, 15));
        g2.drawString("X2", x + 102, y + 27);
        g2.setColor(new Color(251, 146, 60));
        g2.setFont(new Font("Impact", Font.PLAIN, 12));
        g2.drawString("HOT", x + 122, y + 27);

        // Coin counter row below
        int cy = y + 58;
        g2.setColor(new Color(15, 23, 42, 200));
        g2.fillRoundRect(x, cy, 146, 34, 17, 17);
        g2.setColor(new Color(255, 255, 255, 35));
        g2.drawRoundRect(x, cy, 146, 34, 17, 17);

        // 3D Gold coin emblem
        GradientPaint coinGrad = new GradientPaint(x + 8, cy + 6, new Color(253, 224, 71), x + 28, cy + 26, new Color(217, 119, 6));
        g2.setPaint(coinGrad);
        g2.fillOval(x + 9, cy + 6, 22, 22);
        g2.setColor(new Color(146, 64, 14));
        g2.drawOval(x + 9, cy + 6, 22, 22);
        g2.setColor(new Color(120, 53, 15));
        g2.setFont(new Font("Impact", Font.BOLD, 13));
        g2.drawString("$", x + 17, cy + 22);

        // Coin number
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Impact", Font.BOLD, 19));
        g2.drawString(String.format("%,d", coins), x + 38, cy + 24);

        // Active Vehicle Pill Badge
        int vy = cy + 40;
        g2.setColor(new Color(15, 23, 42, 210));
        g2.fillRoundRect(x, vy, 190, 26, 13, 13);
        g2.setColor(new Color(56, 189, 248, 140));
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(x, vy, 190, 26, 13, 13);
        g2.setStroke(new BasicStroke(1));

        g2.setFont(new Font("Impact", Font.BOLD, 12));
        g2.setColor(new Color(251, 191, 36));
        g2.drawString("V", x + 10, vy + 18);
        g2.setColor(new Color(241, 245, 249));
        g2.drawString(vehicle.currentType.displayName, x + 30, vy + 18);

        // Pilot Name & Personal Best Pill Badge
        int dy = vy + 32;
        g2.setColor(new Color(15, 23, 42, 215));
        g2.fillRoundRect(x, dy, 205, 28, 14, 14);
        g2.setColor(new Color(251, 191, 36, 150));
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(x, dy, 205, 28, 14, 14);
        g2.setStroke(new BasicStroke(1));

        g2.setFont(new Font("Impact", Font.PLAIN, 12));
        g2.setColor(new Color(253, 224, 71));
        g2.drawString("PILOT:", x + 10, dy + 18);
        g2.setColor(Color.WHITE);
        String pName = (playerName != null && !playerName.isEmpty()) ? playerName : "Driver";
        if (pName.length() > 10) pName = pName.substring(0, 9) + "..";
        g2.drawString(pName, x + 46, dy + 18);

        g2.setColor(new Color(74, 222, 128));
        g2.drawString("BEST:", x + 114, dy + 18);
        g2.setColor(Color.WHITE);
        g2.drawString(String.format("%,d", bestScore), x + 146, dy + 18);
    }

    private static void drawStageBanner(Graphics2D g2, int screenW, int stage, double fuel) {
        int cx = screenW / 2;

        // "BONUS"
        g2.setColor(new Color(245, 158, 11));
        g2.setFont(new Font("Impact", Font.BOLD, 16));
        String bonusStr = "BONUS";
        FontMetrics fm1 = g2.getFontMetrics();
        g2.drawString(bonusStr, cx - fm1.stringWidth(bonusStr) / 2, 38);

        // "30"
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Impact", Font.BOLD, 32));
        String numStr = "30";
        FontMetrics fm2 = g2.getFontMetrics();
        g2.drawString(numStr, cx - fm2.stringWidth(numStr) / 2, 70);

        // Dynamic Stage Championship Banner with Difficulty Tier
        String stageStr;
        Color stageColor;
        if (stage == 1) {
            stageStr = "STAGE 1 - NORMAL";
            stageColor = new Color(74, 222, 128);
        } else if (stage == 2) {
            stageStr = "STAGE 2 - MEDIUM";
            stageColor = new Color(56, 189, 248);
        } else if (stage == 3) {
            stageStr = "STAGE 3 - HARD";
            stageColor = new Color(251, 146, 60);
        } else {
            stageStr = "STAGE 4 - EXTREME";
            stageColor = new Color(239, 68, 68);
        }

        g2.setFont(new Font("Impact", Font.BOLD, 36));
        FontMetrics fm3 = g2.getFontMetrics();
        int sx = cx - fm3.stringWidth(stageStr) / 2;
        int sy = 118;

        // Drop shadow
        g2.setColor(new Color(15, 23, 42));
        g2.drawString(stageStr, sx + 3, sy + 3);

        // Dynamic stage face color
        g2.setColor(stageColor);
        g2.drawString(stageStr, sx, sy);

        // Low fuel blinking warning
        if (fuel < 25.0) {
            boolean blink = (System.currentTimeMillis() / 280) % 2 == 0;
            if (blink) {
                g2.setColor(new Color(239, 68, 68));
                g2.setFont(new Font("Impact", Font.BOLD, 26));
                String lowFuel = "LOW FUEL!";
                FontMetrics fmFuel = g2.getFontMetrics();
                g2.drawString(lowFuel, cx - fmFuel.stringWidth(lowFuel) / 2, 152);
            }
        }
    }

    private static void drawMilestones(Graphics2D g2, int screenW, int distanceMeters, boolean isPaused) {
        int x = screenW - 175;
        int y = 20;

        // Checkered Flag milestone
        g2.setColor(new Color(15, 23, 42, 200));
        g2.fillRoundRect(x, y, 150, 36, 14, 14);
        g2.setColor(new Color(255, 255, 255, 40));
        g2.drawRoundRect(x, y, 150, 36, 14, 14);

        g2.setColor(new Color(251, 191, 36));
        g2.setFont(new Font("Impact", Font.BOLD, 15));
        g2.drawString((1000 - distanceMeters % 1000) + "m", x + 38, y + 23);
        g2.setColor(new Color(74, 222, 128));
        g2.setFont(new Font("Impact", Font.PLAIN, 12));
        g2.drawString("+10000", x + 96, y + 23);

        // Flag icon
        g2.setColor(Color.WHITE);
        g2.fillRect(x + 12, y + 9, 16, 12);
        g2.setColor(Color.BLACK);
        g2.fillRect(x + 12, y + 9, 8, 6);
        g2.fillRect(x + 20, y + 15, 8, 6);
        g2.setColor(new Color(148, 163, 184));
        g2.fillRect(x + 10, y + 9, 3, 19);

        // Mountain summit milestone
        int my = y + 44;
        g2.setColor(new Color(15, 23, 42, 200));
        g2.fillRoundRect(x, my, 150, 36, 14, 14);
        g2.setColor(new Color(255, 255, 255, 40));
        g2.drawRoundRect(x, my, 150, 36, 14, 14);

        g2.setColor(new Color(251, 191, 36));
        g2.setFont(new Font("Impact", Font.BOLD, 15));
        g2.drawString(distanceMeters + "m", x + 38, my + 23);
        g2.setColor(new Color(74, 222, 128));
        g2.setFont(new Font("Impact", Font.PLAIN, 12));
        g2.drawString("+1000", x + 96, my + 23);

        // Mountain icon
        g2.setColor(new Color(34, 197, 94));
        g2.fillPolygon(new int[]{x + 12, x + 20, x + 28}, new int[]{my + 26, my + 11, my + 26}, 3);

        // Row of 2 Action Buttons (Leaderboard & Pause) aligned symmetrically under the milestone pills:
        // Column bounds: x from (screenW - 175) to (screenW - 25), exactly 150px wide, zero overlap!
        int by = y + 88; // 20 + 88 = 108 (starts 8px below the second milestone pill which ends at 100)
        int btnW = 70;
        int btnH = 36;

        // Button 1: Leaderboard Trophy Button (Left)
        int tx = x; // screenW - 175
        int ty = by;
        g2.setColor(new Color(15, 23, 42, 215));
        g2.fillRoundRect(tx, ty, btnW, btnH, 12, 12);
        g2.setColor(new Color(251, 191, 36, 170));
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(tx, ty, btnW, btnH, 12, 12);
        g2.setStroke(new BasicStroke(1.0f));

        // Subtle top highlight
        g2.setColor(new Color(255, 255, 255, 30));
        g2.fillRoundRect(tx + 2, ty + 2, btnW - 4, btnH / 2 - 2, 10, 10);

        // Gold Trophy Icon (Centered left)
        int icx = tx + 10;
        int icy = ty + 9;
        g2.setColor(new Color(251, 191, 36));
        g2.fillRoundRect(icx + 3, icy, 12, 9, 3, 3);
        g2.fillRect(icx + 7, icy + 9, 4, 5);
        g2.fillRect(icx + 3, icy + 14, 12, 3);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawArc(icx, icy + 1, 4, 6, 90, 180);
        g2.drawArc(icx + 14, icy + 1, 4, 6, -90, 180);
        g2.setStroke(new BasicStroke(1.0f));

        // Label: "TOP"
        g2.setFont(new Font("Impact", Font.BOLD, 13));
        g2.setColor(new Color(253, 224, 71));
        g2.drawString("TOP", tx + 34, ty + 23);

        // Button 2: Pause / Play Button (Right)
        int px = x + btnW + 10; // screenW - 175 + 70 + 10 = screenW - 95
        int py = by;
        g2.setColor(new Color(15, 23, 42, 215));
        g2.fillRoundRect(px, py, btnW, btnH, 12, 12);
        g2.setColor(new Color(56, 189, 248, 150));
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(px, py, btnW, btnH, 12, 12);
        g2.setStroke(new BasicStroke(1.0f));

        // Subtle top highlight
        g2.setColor(new Color(255, 255, 255, 30));
        g2.fillRoundRect(px + 2, py + 2, btnW - 4, btnH / 2 - 2, 10, 10);

        if (!isPaused) {
            // "||" Pause Bars
            g2.setColor(Color.WHITE);
            g2.fillRect(px + 12, py + 10, 4, 16);
            g2.fillRect(px + 20, py + 10, 4, 16);
            g2.setFont(new Font("Impact", Font.BOLD, 12));
            g2.setColor(Color.WHITE);
            g2.drawString("PAUSE", px + 30, py + 23);
        } else {
            // ">" Play Triangle (Green)
            g2.setColor(new Color(74, 222, 128));
            Polygon playTriangle = new Polygon(
                new int[]{px + 12, px + 24, px + 12},
                new int[]{py + 10, py + 18, py + 26},
                3
            );
            g2.fillPolygon(playTriangle);
            g2.setFont(new Font("Impact", Font.BOLD, 12));
            g2.setColor(new Color(74, 222, 128));
            g2.drawString("RESUME", px + 28, py + 23);
        }
    }

    private static void drawPedals(Graphics2D g2, int screenW, int screenH, boolean brakePressed, boolean gasPressed) {
        int pedalW = 78;
        int pedalH = 112;
        int padY = screenH - pedalH - 24;

        // Left Pedal: BRAKE
        drawSinglePedal(g2, 35, padY, pedalW, pedalH, brakePressed, "BRAKE");

        // Right Pedal: GAS
        drawSinglePedal(g2, screenW - pedalW - 35, padY, pedalW, pedalH, gasPressed, "GAS");
    }

    private static void drawSinglePedal(Graphics2D g2, int x, int y, int w, int h, boolean pressed, String label) {
        int dy = pressed ? 9 : 0;

        // Cast shadow beneath pedal on floor
        g2.setColor(new Color(15, 23, 42, 90));
        g2.fillRoundRect(x - 3, y + 14, w + 6, h, 28, 28);

        // Brushed Aluminum Faceplate
        GradientPaint plateGrad = new GradientPaint(
            x, y + dy, new Color(160, 175, 195),
            x + w, y + h + dy, new Color(71, 85, 105)
        );
        g2.setPaint(plateGrad);
        g2.fillRoundRect(x, y + dy, w, h, 26, 26);

        // Shiny Chamfer Bevel Rim
        g2.setColor(new Color(226, 232, 240));
        g2.setStroke(new BasicStroke(2.0f));
        g2.drawRoundRect(x + 1, y + dy + 1, w - 2, h - 2, 24, 24);

        // Outer Dark Bezel
        g2.setColor(new Color(30, 41, 59));
        g2.setStroke(new BasicStroke(3.5f));
        g2.drawRoundRect(x, y + dy, w, h, 26, 26);
        g2.setStroke(new BasicStroke(1));

        // 6 Recessed Rubber Grip Inserts (High-traction studs)
        int[][] holes = {
            {x + 23, y + 26 + dy}, {x + 53, y + 26 + dy},
            {x + 23, y + 56 + dy}, {x + 53, y + 56 + dy},
            {x + 23, y + 86 + dy}, {x + 53, y + 86 + dy}
        };
        for (int[] hole : holes) {
            // Shadow recess
            g2.setColor(new Color(15, 23, 42));
            g2.fillOval(hole[0] - 8, hole[1] - 10, 16, 20);
            // Black rubber core
            g2.setColor(new Color(30, 41, 59));
            g2.fillOval(hole[0] - 6, hole[1] - 8, 12, 16);
        }

        // Label banner above pedal
        g2.setFont(new Font("Impact", Font.BOLD, 13));
        g2.setColor(pressed ? new Color(251, 191, 36) : new Color(203, 213, 225));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(label, x + (w - fm.stringWidth(label)) / 2, y + dy - 7);
    }

    private static void drawGauges(Graphics2D g2, int screenW, int screenH, Vehicle vehicle) {
        int cx = screenW / 2;
        int cy = screenH - 75;

        // Center Gauge: Main Fuel & Speedometer (Diameter 136px)
        drawFuelSpeedometer(g2, cx, cy - 10, 68, vehicle.fuel);

        // Left Gauge: RPM Tachometer (Diameter 96px)
        drawRpmGauge(g2, cx - 116, cy + 10, 48, vehicle.rpm);

        // Right Gauge: BOOST Gauge (Diameter 96px)
        drawBoostGauge(g2, cx + 116, cy + 10, 48, vehicle.boostGauge);
    }

    private static void drawFuelSpeedometer(Graphics2D g2, int cx, int cy, int radius, double fuelPercent) {
        // Outer Brushed Chrome Bezel
        GradientPaint bezelGrad = new GradientPaint(cx - radius - 7, cy - radius - 7, new Color(203, 213, 225), cx + radius + 7, cy + radius + 7, new Color(51, 65, 85));
        g2.setPaint(bezelGrad);
        g2.fillOval(cx - radius - 7, cy - radius - 7, (radius + 7) * 2, (radius + 7) * 2);

        // Dark recessed dial face
        GradientPaint dialGrad = new GradientPaint(cx, cy - radius, new Color(30, 41, 59), cx, cy + radius, new Color(15, 23, 42));
        g2.setPaint(dialGrad);
        g2.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);

        // Multi-color Arc (Red -> Orange -> Yellow -> Green)
        int arcR = radius - 14;
        g2.setStroke(new BasicStroke(10.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));

        g2.setColor(new Color(239, 68, 68)); // Red zone (0 - 25%)
        g2.drawArc(cx - arcR, cy - arcR, arcR * 2, arcR * 2, 140, 40);

        g2.setColor(new Color(249, 115, 22)); // Orange zone (25 - 50%)
        g2.drawArc(cx - arcR, cy - arcR, arcR * 2, arcR * 2, 100, 40);

        g2.setColor(new Color(234, 179, 8)); // Yellow zone (50 - 75%)
        g2.drawArc(cx - arcR, cy - arcR, arcR * 2, arcR * 2, 60, 40);

        g2.setColor(new Color(34, 197, 94)); // Green zone (75 - 100%)
        g2.drawArc(cx - arcR, cy - arcR, arcR * 2, arcR * 2, 0, 60);

        // Calibrated tick marks
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(1.5f));
        for (int a = 0; a <= 180; a += 30) {
            double rad = Math.toRadians(180 - a);
            int x1 = cx + (int) (Math.cos(rad) * (radius - 22));
            int y1 = cy - (int) (Math.sin(rad) * (radius - 22));
            int x2 = cx + (int) (Math.cos(rad) * (radius - 8));
            int y2 = cy - (int) (Math.sin(rad) * (radius - 8));
            g2.drawLine(x1, y1, x2, y2);
        }

        // Fuel pump icon in center
        g2.setColor(Color.WHITE);
        g2.fillRect(cx - 7, cy + 15, 14, 16);
        g2.setColor(new Color(15, 23, 42));
        g2.fillRect(cx - 4, cy + 18, 8, 6);
        g2.setColor(Color.WHITE);
        g2.drawArc(cx + 4, cy + 16, 7, 10, 0, 180);

        // Swinging Needle with drop shadow
        double needleAngle = Math.toRadians(180 - (fuelPercent / 100.0) * 180);
        int nx = cx + (int) (Math.cos(needleAngle) * (radius - 18));
        int ny = cy - (int) (Math.sin(needleAngle) * (radius - 18));

        // Needle shadow
        g2.setColor(new Color(15, 23, 42, 160));
        g2.setStroke(new BasicStroke(3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(cx + 2, cy + 2, nx + 2, ny + 2);

        // White needle face
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(cx, cy, nx, ny);
        g2.setStroke(new BasicStroke(1));

        // Convex Glass Lens Glare Arc (Gloss highlight)
        GradientPaint glassGrad = new GradientPaint(
            cx, cy - radius, new Color(255, 255, 255, 75),
            cx, cy, new Color(255, 255, 255, 0)
        );
        g2.setPaint(glassGrad);
        g2.fillOval(cx - radius + 5, cy - radius + 3, (radius - 5) * 2, radius);

        // Center hub cap with silver nut
        g2.setColor(new Color(203, 213, 225));
        g2.fillOval(cx - 9, cy - 9, 18, 18);
        g2.setColor(new Color(71, 85, 105));
        g2.drawOval(cx - 9, cy - 9, 18, 18);
    }

    private static void drawRpmGauge(Graphics2D g2, int cx, int cy, int radius, double rpm) {
        // Outer Brushed Chrome Bezel
        GradientPaint bezelGrad = new GradientPaint(cx - radius - 5, cy - radius - 5, new Color(203, 213, 225), cx + radius + 5, cy + radius + 5, new Color(51, 65, 85));
        g2.setPaint(bezelGrad);
        g2.fillOval(cx - radius - 5, cy - radius - 5, (radius + 5) * 2, (radius + 5) * 2);

        // Dial face
        g2.setColor(new Color(30, 41, 59));
        g2.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);

        // Tick marks around dial
        g2.setColor(new Color(203, 213, 225));
        g2.setStroke(new BasicStroke(1.8f));
        for (int a = 140; a <= 360; a += 35) {
            double rad = Math.toRadians(a);
            int x1 = cx + (int) (Math.cos(rad) * (radius - 12));
            int y1 = cy + (int) (Math.sin(rad) * (radius - 12));
            int x2 = cx + (int) (Math.cos(rad) * (radius - 4));
            int y2 = cy + (int) (Math.sin(rad) * (radius - 4));
            g2.drawLine(x1, y1, x2, y2);
        }

        // RPM label
        g2.setFont(new Font("Impact", Font.PLAIN, 13));
        g2.setColor(Color.WHITE);
        String label = "RPM";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(label, cx - fm.stringWidth(label) / 2, cy + 28);

        // Needle
        double normalizedRpm = Math.max(0, Math.min(1.0, (rpm - 1000) / 7000.0));
        double needleAngle = Math.toRadians(140 + normalizedRpm * 220);
        int nx = cx + (int) (Math.cos(needleAngle) * (radius - 14));
        int ny = cy + (int) (Math.sin(needleAngle) * (radius - 14));

        // Needle shadow
        g2.setColor(new Color(15, 23, 42, 140));
        g2.setStroke(new BasicStroke(2.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(cx + 1, cy + 1, nx + 1, ny + 1);

        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(cx, cy, nx, ny);
        g2.setStroke(new BasicStroke(1));

        // Glass reflection
        GradientPaint glassGrad = new GradientPaint(cx, cy - radius, new Color(255, 255, 255, 60), cx, cy, new Color(255, 255, 255, 0));
        g2.setPaint(glassGrad);
        g2.fillOval(cx - radius + 4, cy - radius + 2, (radius - 4) * 2, radius);

        // Hub cap
        g2.setColor(new Color(203, 213, 225));
        g2.fillOval(cx - 7, cy - 7, 14, 14);
    }

    private static void drawBoostGauge(Graphics2D g2, int cx, int cy, int radius, double boost) {
        // Outer Brushed Chrome Bezel
        GradientPaint bezelGrad = new GradientPaint(cx - radius - 5, cy - radius - 5, new Color(203, 213, 225), cx + radius + 5, cy + radius + 5, new Color(51, 65, 85));
        g2.setPaint(bezelGrad);
        g2.fillOval(cx - radius - 5, cy - radius - 5, (radius + 5) * 2, (radius + 5) * 2);

        // Dial face
        g2.setColor(new Color(30, 41, 59));
        g2.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);

        // Cyan Boost Arc
        int arcR = radius - 10;
        g2.setStroke(new BasicStroke(6.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(56, 189, 248));
        g2.drawArc(cx - arcR, cy - arcR, arcR * 2, arcR * 2, 140, (int) (boost / 100.0 * 220));

        // BOOST label
        g2.setFont(new Font("Impact", Font.PLAIN, 13));
        g2.setColor(new Color(56, 189, 248));
        String label = "BOOST";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(label, cx - fm.stringWidth(label) / 2, cy + 28);

        // Needle
        double needleAngle = Math.toRadians(140 + (boost / 100.0) * 220);
        int nx = cx + (int) (Math.cos(needleAngle) * (radius - 14));
        int ny = cy + (int) (Math.sin(needleAngle) * (radius - 14));

        // Needle shadow
        g2.setColor(new Color(15, 23, 42, 140));
        g2.setStroke(new BasicStroke(2.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(cx + 1, cy + 1, nx + 1, ny + 1);

        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(cx, cy, nx, ny);
        g2.setStroke(new BasicStroke(1));

        // Glass reflection
        GradientPaint glassGrad = new GradientPaint(cx, cy - radius, new Color(255, 255, 255, 60), cx, cy, new Color(255, 255, 255, 0));
        g2.setPaint(glassGrad);
        g2.fillOval(cx - radius + 4, cy - radius + 2, (radius - 4) * 2, radius);

        // Hub cap
        g2.setColor(new Color(203, 213, 225));
        g2.fillOval(cx - 7, cy - 7, 14, 14);
    }

    private static void drawStuntPopup(Graphics2D g2, int screenW, String currentStunt) {
        int cx = screenW / 2;
        int cy = 200;

        g2.setFont(new Font("Impact", Font.BOLD, 28));
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(currentStunt);

        // Glow badge background
        g2.setColor(new Color(15, 23, 42, 210));
        g2.fillRoundRect(cx - tw / 2 - 20, cy - 30, tw + 40, 42, 20, 20);
        g2.setColor(new Color(251, 191, 36));
        g2.setStroke(new BasicStroke(2.0f));
        g2.drawRoundRect(cx - tw / 2 - 20, cy - 30, tw + 40, 42, 20, 20);
        g2.setStroke(new BasicStroke(1));

        // Text
        g2.setColor(new Color(253, 224, 71));
        g2.drawString(currentStunt, cx - tw / 2, cy);
    }

    private static void drawControlsHint(Graphics2D g2, int screenW, int screenH) {
        g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        g2.setColor(new Color(15, 23, 42, 220));
        g2.drawString("CONTROLS: GAS (Right/D)  |  BRAKE (Left/A)  |  PAUSE (P/Esc)  |  BOOST (Space)  |  VEHICLE (V)  |  RESTART (R)", 20, screenH - 10);
    }

    public static void drawPauseOverlay(Graphics2D g2, int screenW, int screenH) {
        // Semi-transparent dark blur backdrop
        g2.setColor(new Color(15, 23, 42, 190));
        g2.fillRect(0, 0, screenW, screenH);

        // Center frosted glass card modal
        int mw = 420;
        int mh = 260;
        int mx = (screenW - mw) / 2;
        int my = (screenH - mh) / 2;

        // Modal shadow
        g2.setColor(new Color(0, 0, 0, 110));
        g2.fillRoundRect(mx + 6, my + 8, mw, mh, 24, 24);

        // Modal body gradient
        GradientPaint modalGrad = new GradientPaint(
            mx, my, new Color(30, 41, 59, 245),
            mx, my + mh, new Color(15, 23, 42, 245)
        );
        g2.setPaint(modalGrad);
        g2.fillRoundRect(mx, my, mw, mh, 24, 24);

        // Border glow
        g2.setColor(new Color(56, 189, 248, 140));
        g2.setStroke(new BasicStroke(2.0f));
        g2.drawRoundRect(mx, my, mw, mh, 24, 24);
        g2.setStroke(new BasicStroke(1.0f));

        // Title: GAME PAUSED
        g2.setFont(new Font("Impact", Font.BOLD, 36));
        g2.setColor(new Color(253, 224, 71));
        FontMetrics fm = g2.getFontMetrics();
        String title = "GAME PAUSED";
        g2.drawString(title, mx + (mw - fm.stringWidth(title)) / 2, my + 50);

        // Subtitle
        g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g2.setColor(new Color(203, 213, 225));
        fm = g2.getFontMetrics();
        String sub = "Game is currently suspended";
        g2.drawString(sub, mx + (mw - fm.stringWidth(sub)) / 2, my + 76);

        // Button 1: Resume Button inside modal
        int rw = 220;
        int rh = 44;
        int rx = mx + (mw - rw) / 2;
        int ry = my + 105;

        // Resume button gradient (Vibrant Emerald)
        GradientPaint btnGrad = new GradientPaint(
            rx, ry, new Color(34, 197, 94),
            rx, ry + rh, new Color(22, 163, 74)
        );
        g2.setPaint(btnGrad);
        g2.fillRoundRect(rx, ry, rw, rh, 16, 16);
        g2.setColor(new Color(255, 255, 255, 90));
        g2.setFont(new Font("Impact", Font.BOLD, 20));
        g2.setColor(Color.WHITE);
        fm = g2.getFontMetrics();
        String resumeTxt = "RESUME GAME";
        int rStrW = fm.stringWidth(resumeTxt);
        int rTotal = rStrW + 20;
        int rStartX = rx + (rw - rTotal) / 2;
        g2.drawString(resumeTxt, rStartX, ry + 29);

        // Vector Arrow
        int ax = rStartX + rStrW + 8;
        int ay = ry + 22;
        Polygon arrow = new Polygon(
            new int[]{ax, ax + 9, ax},
            new int[]{ay - 6, ay, ay + 6},
            3
        );
        g2.fillPolygon(arrow);

        // Button 2: Leaderboard Button inside pause modal
        int lw = 220;
        int lh = 38;
        int lx = mx + (mw - lw) / 2;
        int ly = my + 158;

        GradientPaint lGrad = new GradientPaint(
            lx, ly, new Color(217, 119, 6),
            lx, ly + lh, new Color(180, 83, 9)
        );
        g2.setPaint(lGrad);
        g2.fillRoundRect(lx, ly, lw, lh, 14, 14);
        g2.setColor(new Color(255, 255, 255, 60));
        g2.fillRoundRect(lx + 2, ly + 2, lw - 4, lh / 2 - 2, 12, 12);

        g2.setFont(new Font("Impact", Font.BOLD, 17));
        g2.setColor(Color.WHITE);
        fm = g2.getFontMetrics();
        String lTxt = "VIEW LEADERBOARD";
        int lStrW = fm.stringWidth(lTxt);
        int lTotal = lStrW + 24;
        int lStartX = lx + (lw - lTotal) / 2 + 18;
        g2.drawString(lTxt, lStartX, ly + 25);

        // Vector Gold Trophy Icon
        int tx = lx + (lw - lTotal) / 2;
        int ty = ly + 12;
        g2.setColor(new Color(253, 224, 71));
        g2.fillRoundRect(tx + 2, ty, 10, 8, 2, 2);
        g2.fillRect(tx + 5, ty + 8, 4, 4);
        g2.fillRect(tx + 2, ty + 12, 10, 2);
        g2.drawArc(tx, ty + 1, 3, 5, 90, 180);
        g2.drawArc(tx + 9, ty + 1, 3, 5, -90, 180);

        // Keyboard & Mouse hint
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2.setColor(new Color(148, 163, 184));
        fm = g2.getFontMetrics();
        String hint = "Click Resume button, Pause button, or Press [P] / [ESC]";
        g2.drawString(hint, mx + (mw - fm.stringWidth(hint)) / 2, my + 235);
    }

    public static void drawLoginScreen(Graphics2D g2, int screenW, int screenH) {
        // Subtle darkened tint over idling background scene
        g2.setColor(new Color(15, 23, 42, 170));
        g2.fillRect(0, 0, screenW, screenH);

        int mw = 520;
        int mh = 370;
        int mx = (screenW - mw) / 2;
        int my = (screenH - mh) / 2;

        // Shadow
        g2.setColor(new Color(0, 0, 0, 130));
        g2.fillRoundRect(mx + 8, my + 10, mw, mh, 28, 28);

        // Body
        GradientPaint modalGrad = new GradientPaint(
            mx, my, new Color(30, 41, 59, 250),
            mx, my + mh, new Color(15, 23, 42, 250)
        );
        g2.setPaint(modalGrad);
        g2.fillRoundRect(mx, my, mw, mh, 28, 28);

        // Border glow
        g2.setColor(new Color(56, 189, 248, 160));
        g2.setStroke(new BasicStroke(2.2f));
        g2.drawRoundRect(mx, my, mw, mh, 28, 28);
        g2.setStroke(new BasicStroke(1.0f));

        // Game Logo / Title
        g2.setFont(new Font("Impact", Font.BOLD, 36));
        g2.setColor(new Color(253, 224, 71));
        FontMetrics fm = g2.getFontMetrics();
        String title = "HILL CLIMB RACING 2";
        g2.drawString(title, mx + (mw - fm.stringWidth(title)) / 2, my + 52);

        // Subtitle badge
        g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g2.setColor(new Color(56, 189, 248));
        fm = g2.getFontMetrics();
        String sub = "COUNTRYSIDE STAGE 4 - KDU IT22013 DSA";
        g2.drawString(sub, mx + (mw - fm.stringWidth(sub)) / 2, my + 78);

        // Prompt
        g2.setFont(new Font("Impact", Font.PLAIN, 18));
        g2.setColor(Color.WHITE);
        fm = g2.getFontMetrics();
        String prompt = "ENTER DRIVER NAME TO BEGIN:";
        g2.drawString(prompt, mx + (mw - fm.stringWidth(prompt)) / 2, my + 128);

        // Button 1: START RACE
        int bw = 260;
        int bh = 46;
        int bx = mx + (mw - bw) / 2;
        int by = my + 215;

        GradientPaint startGrad = new GradientPaint(
            bx, by, new Color(34, 197, 94),
            bx, by + bh, new Color(22, 163, 74)
        );
        g2.setPaint(startGrad);
        g2.fillRoundRect(bx, by, bw, bh, 16, 16);
        g2.setColor(new Color(255, 255, 255, 90));
        g2.fillRoundRect(bx + 2, by + 2, bw - 4, bh / 2 - 2, 14, 14);

        g2.setFont(new Font("Impact", Font.BOLD, 22));
        g2.setColor(Color.WHITE);
        fm = g2.getFontMetrics();
        String startTxt = "START RACE";
        int sStrW = fm.stringWidth(startTxt);
        int sTotal = sStrW + 22;
        int sStartX = bx + (bw - sTotal) / 2;
        g2.drawString(startTxt, sStartX, by + 31);

        // Crisp Vector Arrow
        int sax = sStartX + sStrW + 9;
        int say = by + 23;
        Polygon sArrow = new Polygon(
            new int[]{sax, sax + 9, sax},
            new int[]{say - 7, say, say + 7},
            3
        );
        g2.fillPolygon(sArrow);

        // Button 2: LEADERBOARD
        int lw = 260;
        int lh = 38;
        int lx = mx + (mw - lw) / 2;
        int ly = my + 272;

        GradientPaint lGrad = new GradientPaint(
            lx, ly, new Color(217, 119, 6),
            lx, ly + lh, new Color(180, 83, 9)
        );
        g2.setPaint(lGrad);
        g2.fillRoundRect(lx, ly, lw, lh, 14, 14);
        g2.setColor(new Color(255, 255, 255, 60));
        g2.fillRoundRect(lx + 2, ly + 2, lw - 4, lh / 2 - 2, 12, 12);

        g2.setFont(new Font("Impact", Font.BOLD, 17));
        g2.setColor(Color.WHITE);
        fm = g2.getFontMetrics();
        String lTxt = "VIEW LEADERBOARD";
        int lbW = fm.stringWidth(lTxt);
        int lbTotal = lbW + 24;
        int lbStartX = lx + (lw - lbTotal) / 2 + 18;
        g2.drawString(lTxt, lbStartX, ly + 25);

        // Crisp Vector Gold Trophy Icon
        int ltx = lx + (lw - lbTotal) / 2;
        int lty = ly + 12;
        g2.setColor(new Color(253, 224, 71));
        g2.fillRoundRect(ltx + 2, lty, 10, 8, 2, 2);
        g2.fillRect(ltx + 5, lty + 8, 4, 4);
        g2.fillRect(ltx + 2, lty + 12, 10, 2);
        g2.drawArc(ltx, lty + 1, 3, 5, 90, 180);
        g2.drawArc(ltx + 9, lty + 1, 3, 5, -90, 180);

        // Footer Hint
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2.setColor(new Color(148, 163, 184));
        fm = g2.getFontMetrics();
        String hint = "Type your name and click START RACE or press ENTER";
        g2.drawString(hint, mx + (mw - fm.stringWidth(hint)) / 2, my + 342);
    }

    public static void drawVectorStar(Graphics2D g2, int cx, int cy, int outerR, int innerR, Color fill, Color stroke) {
        Polygon star = new Polygon();
        for (int i = 0; i < 10; i++) {
            double angle = -Math.PI / 2 + i * Math.PI / 5;
            int r = (i % 2 == 0) ? outerR : innerR;
            int x = (int) Math.round(cx + r * Math.cos(angle));
            int y = (int) Math.round(cy + r * Math.sin(angle));
            star.addPoint(x, y);
        }
        g2.setColor(fill);
        g2.fillPolygon(star);
        if (stroke != null) {
            g2.setColor(stroke);
            g2.drawPolygon(star);
        }
    }

    public static void drawScoreboardOverlay(Graphics2D g2, int screenW, int screenH, String currentPlayerName) {
        // Darkened glassmorphic backdrop
        g2.setColor(new Color(15, 23, 42, 230));
        g2.fillRect(0, 0, screenW, screenH);

        int mw = 700;
        int mh = 480;
        int mx = (screenW - mw) / 2;
        int my = (screenH - mh) / 2;

        // Outer Shadow
        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillRoundRect(mx + 8, my + 10, mw, mh, 28, 28);

        // Modal Body
        GradientPaint modalGrad = new GradientPaint(
            mx, my, new Color(30, 41, 59, 250),
            mx, my + mh, new Color(15, 23, 42, 250)
        );
        g2.setPaint(modalGrad);
        g2.fillRoundRect(mx, my, mw, mh, 28, 28);

        // Luminous Amber/Gold Border
        g2.setColor(new Color(251, 191, 36, 180));
        g2.setStroke(new BasicStroke(2.2f));
        g2.drawRoundRect(mx, my, mw, mh, 28, 28);
        g2.setStroke(new BasicStroke(1.0f));

        // Header Title with Dual Vector Gold Stars
        g2.setFont(new Font("Impact", Font.BOLD, 32));
        g2.setColor(new Color(253, 224, 71));
        FontMetrics fm = g2.getFontMetrics();
        String title = "ALL-TIME HIGH SCORE LEADERBOARD";
        int tStrW = fm.stringWidth(title);
        int titleX = mx + (mw - tStrW) / 2;
        g2.drawString(title, titleX, my + 44);

        // Left and Right Vector Gold Stars
        drawVectorStar(g2, titleX - 22, my + 33, 11, 5, new Color(251, 191, 36), new Color(217, 119, 6));
        drawVectorStar(g2, titleX + tStrW + 22, my + 33, 11, 5, new Color(251, 191, 36), new Color(217, 119, 6));

        // Subtitle: Algorithm mention for KDU DSA viva
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2.setColor(new Color(148, 163, 184));
        fm = g2.getFontMetrics();
        String sub = "Rankings sorted in real-time using Selection Sort O(n^2) - KDU IT22013 DSA";
        g2.drawString(sub, mx + (mw - fm.stringWidth(sub)) / 2, my + 68);

        // Table Header Bar
        int hy = my + 84;
        g2.setColor(new Color(51, 65, 85, 210));
        g2.fillRoundRect(mx + 25, hy, mw - 50, 32, 10, 10);
        g2.setColor(new Color(255, 255, 255, 30));
        g2.drawRoundRect(mx + 25, hy, mw - 50, 32, 10, 10);

        g2.setFont(new Font("Impact", Font.PLAIN, 14));
        g2.setColor(new Color(203, 213, 225));
        g2.drawString("RANK", mx + 45, hy + 21);
        g2.drawString("DRIVER", mx + 130, hy + 21);
        g2.drawString("BEST SCORE", mx + 310, hy + 21);
        g2.drawString("DISTANCE", mx + 440, hy + 21);
        g2.drawString("COINS", mx + 555, hy + 21);

        // List top records
        java.util.List<HillClimbAlgorithms.HighScoreRecord> top = ScoreboardManager.getTopRecords(7);
        int rowY = hy + 40;

        for (int i = 0; i < top.size(); i++) {
            HillClimbAlgorithms.HighScoreRecord rec = top.get(i);
            boolean isSelf = (currentPlayerName != null && rec.getPlayerName().equalsIgnoreCase(currentPlayerName.trim()));

            // Row background
            if (isSelf) {
                g2.setColor(new Color(56, 189, 248, 60));
                g2.fillRoundRect(mx + 25, rowY, mw - 50, 34, 8, 8);
                g2.setColor(new Color(56, 189, 248, 180));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(mx + 25, rowY, mw - 50, 34, 8, 8);
                g2.setStroke(new BasicStroke(1.0f));
            } else if (i % 2 == 0) {
                g2.setColor(new Color(30, 41, 59, 120));
                g2.fillRoundRect(mx + 25, rowY, mw - 50, 34, 8, 8);
            }

            // High-Contrast Clear Rank Badges
            int rx = mx + 40;
            int rw = 44;
            int rh = 24;
            int ry = rowY + 5;

            if (i == 0) {
                // 1st Gold Medal Badge
                GradientPaint gP = new GradientPaint(rx, ry, new Color(251, 191, 36), rx, ry + rh, new Color(217, 119, 6));
                g2.setPaint(gP);
                g2.fillRoundRect(rx, ry, rw, rh, 8, 8);
                g2.setColor(new Color(254, 240, 138));
                g2.drawRoundRect(rx, ry, rw, rh, 8, 8);
                g2.setColor(new Color(15, 23, 42));
                g2.setFont(new Font("Impact", Font.BOLD, 15));
                FontMetrics rfm = g2.getFontMetrics();
                String rTxt = "1st";
                g2.drawString(rTxt, rx + (rw - rfm.stringWidth(rTxt)) / 2, ry + 17);
            } else if (i == 1) {
                // 2nd Silver Medal Badge
                GradientPaint sP = new GradientPaint(rx, ry, new Color(241, 245, 249), rx, ry + rh, new Color(148, 163, 184));
                g2.setPaint(sP);
                g2.fillRoundRect(rx, ry, rw, rh, 8, 8);
                g2.setColor(Color.WHITE);
                g2.drawRoundRect(rx, ry, rw, rh, 8, 8);
                g2.setColor(new Color(15, 23, 42));
                g2.setFont(new Font("Impact", Font.BOLD, 15));
                FontMetrics rfm = g2.getFontMetrics();
                String rTxt = "2nd";
                g2.drawString(rTxt, rx + (rw - rfm.stringWidth(rTxt)) / 2, ry + 17);
            } else if (i == 2) {
                // 3rd Bronze Medal Badge
                GradientPaint bP = new GradientPaint(rx, ry, new Color(249, 115, 22), rx, ry + rh, new Color(154, 52, 18));
                g2.setPaint(bP);
                g2.fillRoundRect(rx, ry, rw, rh, 8, 8);
                g2.setColor(new Color(254, 215, 170));
                g2.drawRoundRect(rx, ry, rw, rh, 8, 8);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Impact", Font.BOLD, 15));
                FontMetrics rfm = g2.getFontMetrics();
                String rTxt = "3rd";
                g2.drawString(rTxt, rx + (rw - rfm.stringWidth(rTxt)) / 2, ry + 17);
            } else {
                g2.setColor(new Color(30, 41, 59, 180));
                g2.fillRoundRect(rx, ry, rw, rh, 8, 8);
                g2.setColor(new Color(148, 163, 184));
                g2.setFont(new Font("Impact", Font.PLAIN, 15));
                FontMetrics rfm = g2.getFontMetrics();
                String rTxt = "#" + (i + 1);
                g2.drawString(rTxt, rx + (rw - rfm.stringWidth(rTxt)) / 2, ry + 17);
            }

            // Driver Name
            g2.setFont(new Font("Segoe UI", isSelf ? Font.BOLD : Font.PLAIN, 15));
            g2.setColor(isSelf ? new Color(56, 189, 248) : Color.WHITE);
            String dName = rec.getPlayerName();
            if (isSelf) dName += " (YOU)";
            g2.drawString(dName, mx + 130, rowY + 23);

            // Score
            g2.setFont(new Font("Impact", Font.BOLD, 16));
            g2.setColor(new Color(253, 224, 71));
            g2.drawString(String.format("%,d", rec.getScore()), mx + 310, rowY + 23);

            // Distance
            g2.setFont(new Font("Impact", Font.PLAIN, 15));
            g2.setColor(new Color(74, 222, 128));
            g2.drawString(rec.getDistanceMeters() + "m", mx + 440, rowY + 23);

            // Coins
            g2.setColor(new Color(251, 146, 60));
            g2.drawString(String.format("%,d", rec.getTotalCoins()), mx + 555, rowY + 23);

            rowY += 38;
        }

        // Close / Back Button
        int bw = 240;
        int bh = 42;
        int bx = mx + (mw - bw) / 2;
        int by = my + mh - 58;

        GradientPaint closeGrad = new GradientPaint(
            bx, by, new Color(59, 130, 246),
            bx, by + bh, new Color(29, 78, 216)
        );
        g2.setPaint(closeGrad);
        g2.fillRoundRect(bx, by, bw, bh, 14, 14);
        g2.setColor(new Color(255, 255, 255, 80));
        g2.fillRoundRect(bx + 2, by + 2, bw - 4, bh / 2 - 2, 12, 12);

        g2.setFont(new Font("Impact", Font.BOLD, 18));
        g2.setColor(Color.WHITE);
        fm = g2.getFontMetrics();
        String closeTxt = "CLOSE LEADERBOARD";
        int cStrW = fm.stringWidth(closeTxt);
        int cTotal = cStrW + 18;
        int cStart = bx + (bw - cTotal) / 2;
        g2.drawString(closeTxt, cStart, by + 27);

        // Vector 'X' icon inside button
        int xx = cStart + cStrW + 6;
        int xy = by + 16;
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2.2f));
        g2.drawLine(xx, xy, xx + 8, xy + 8);
        g2.drawLine(xx + 8, xy, xx, xy + 8);
        g2.setStroke(new BasicStroke(1.0f));

        // Subtitle hint for keyboard
        g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        g2.setColor(new Color(148, 163, 184));
        fm = g2.getFontMetrics();
        String escHint = "Click button or press ESC to return";
        g2.drawString(escHint, mx + (mw - fm.stringWidth(escHint)) / 2, my + mh - 6);
    }

    public static void drawGameOverScreen(Graphics2D g2, int screenW, int screenH, String reason, 
                                          int score, int distanceMeters, int coins, 
                                          String playerName, int bestScore, boolean isNewBest) {
        // Darkened backdrop
        g2.setColor(new Color(15, 23, 42, 230));
        g2.fillRect(0, 0, screenW, screenH);

        int mw = 580;
        int mh = 390;
        int mx = (screenW - mw) / 2;
        int my = (screenH - mh) / 2;

        // Shadow & Body
        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillRoundRect(mx + 8, my + 10, mw, mh, 28, 28);

        GradientPaint cardGrad = new GradientPaint(
            mx, my, new Color(30, 41, 59, 250),
            mx, my + mh, new Color(15, 23, 42, 250)
        );
        g2.setPaint(cardGrad);
        g2.fillRoundRect(mx, my, mw, mh, 28, 28);

        g2.setColor(new Color(239, 68, 68, 180));
        g2.setStroke(new BasicStroke(2.2f));
        g2.drawRoundRect(mx, my, mw, mh, 28, 28);
        g2.setStroke(new BasicStroke(1.0f));

        // Crash Reason Title
        g2.setFont(new Font("Impact", Font.BOLD, 36));
        g2.setColor(new Color(239, 68, 68));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(reason, mx + (mw - fm.stringWidth(reason)) / 2, my + 50);

        // New Best Record Banner!
        int curY = my + 68;
        if (isNewBest) {
            g2.setColor(new Color(251, 191, 36));
            g2.fillRoundRect(mx + 60, curY, mw - 120, 32, 16, 16);
            g2.setFont(new Font("Impact", Font.BOLD, 17));
            g2.setColor(new Color(15, 23, 42));
            String nbStr = "NEW PERSONAL BEST SCORE RECORD!";
            fm = g2.getFontMetrics();
            g2.drawString(nbStr, mx + (mw - fm.stringWidth(nbStr)) / 2, curY + 22);
            curY += 44;
        } else {
            curY += 15;
        }

        // Stats Grid Box
        int gw = mw - 80;
        int gh = 96;
        int gx = mx + 40;
        int gy = curY;
        g2.setColor(new Color(15, 23, 42, 220));
        g2.fillRoundRect(gx, gy, gw, gh, 14, 14);
        g2.setColor(new Color(255, 255, 255, 30));
        g2.drawRoundRect(gx, gy, gw, gh, 14, 14);

        // Driver & Stats Left
        g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g2.setColor(new Color(148, 163, 184));
        g2.drawString("DRIVER: " + playerName, gx + 20, gy + 28);
        int rank = ScoreboardManager.getPlayerRank(playerName);
        String rankStr = (rank > 0) ? "#" + rank + " Overall" : "-";
        g2.drawString("RANK: " + rankStr, gx + 20, gy + 54);
        g2.drawString("COINS: " + String.format("%,d", coins), gx + 20, gy + 80);

        // Stats Right
        g2.setFont(new Font("Impact", Font.PLAIN, 16));
        g2.setColor(new Color(74, 222, 128));
        g2.drawString("DISTANCE: " + distanceMeters + "m", gx + 240, gy + 28);
        g2.setColor(Color.WHITE);
        g2.drawString("RUN SCORE: " + String.format("%,d", score), gx + 240, gy + 54);
        g2.setColor(new Color(253, 224, 71));
        g2.drawString("ALL-TIME BEST: " + String.format("%,d", bestScore), gx + 240, gy + 80);

        // Action Buttons:
        int bw = 155;
        int bh = 42;
        int by = my + mh - 65;
        int b1x = mx + 30;

        // PLAY AGAIN (R)
        GradientPaint pGrad = new GradientPaint(b1x, by, new Color(34, 197, 94), b1x, by + bh, new Color(22, 163, 74));
        g2.setPaint(pGrad);
        g2.fillRoundRect(b1x, by, bw, bh, 14, 14);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Impact", Font.BOLD, 15));
        fm = g2.getFontMetrics();
        String rTxt = "PLAY AGAIN (R)";
        g2.drawString(rTxt, b1x + (bw - fm.stringWidth(rTxt)) / 2, by + 27);

        // SCORES (L)
        int b2x = b1x + 175;
        GradientPaint lGrad = new GradientPaint(b2x, by, new Color(217, 119, 6), b2x, by + bh, new Color(180, 83, 9));
        g2.setPaint(lGrad);
        g2.fillRoundRect(b2x, by, bw, bh, 14, 14);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Impact", Font.BOLD, 15));
        fm = g2.getFontMetrics();
        String lTxt = "SCORES (L)";
        int lStrW = fm.stringWidth(lTxt);
        int lTotal = lStrW + 16;
        int lStartX = b2x + (bw - lTotal) / 2;
        g2.drawString(lTxt, lStartX, by + 27);

        // Vector Gold Trophy
        int tx = lStartX + lStrW + 5;
        int ty = by + 15;
        g2.setColor(new Color(253, 224, 71));
        g2.fillRoundRect(tx + 1, ty, 8, 6, 2, 2);
        g2.fillRect(tx + 3, ty + 6, 4, 3);
        g2.fillRect(tx + 1, ty + 9, 8, 2);

        // CHANGE DRIVER (C)
        int b3x = b2x + 175;
        GradientPaint cGrad = new GradientPaint(b3x, by, new Color(59, 130, 246), b3x, by + bh, new Color(29, 78, 216));
        g2.setPaint(cGrad);
        g2.fillRoundRect(b3x, by, bw, bh, 14, 14);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Impact", Font.BOLD, 15));
        fm = g2.getFontMetrics();
        String cTxt = "DRIVER (C)";
        int cStrW = fm.stringWidth(cTxt);
        int cTotal = cStrW + 16;
        int cStart = b3x + (bw - cTotal) / 2;
        g2.drawString(cTxt, cStart, by + 27);

        // Vector Driver Avatar Icon
        int ux = cStart + cStrW + 5;
        int uy = by + 14;
        g2.setColor(new Color(253, 224, 71));
        g2.fillOval(ux + 2, uy, 7, 7);
        g2.fillArc(ux, uy + 6, 11, 8, 0, 180);
    }
}
