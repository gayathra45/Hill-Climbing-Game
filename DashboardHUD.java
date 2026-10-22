import java.awt.*;

/**
 * Realistic Hill Climb Racing Dashboard HUD.
 * 
 * Accurately reproduces all visual elements from the photo:
 * 1. Bottom Center: Triple analog gauge cluster (RPM Gauge, Main Fuel/Speed Gauge, Boost Gauge).
 * 2. Bottom Corners: Metallic Gas & Brake Pedals with interactive depressed states.
 * 3. Top Left: Score pill, "x2 HOT" multiplier, coin counter.
 * 4. Top Center: Stage banner & Bonus counter.
 * 5. Top Right: Checkered flag target distance & mountain summit markers.
 * 6. Center: Live Stunt announcements popped from CustomStack.
 */
public class DashboardHUD {

    public static void render(Graphics2D g2, int screenW, int screenH, Vehicle vehicle, 
                              int score, int coins, int stage, int distanceMeters, 
                              String currentStunt, boolean gasPressed, boolean brakePressed) {

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // -------------------------------------------------------------
        // 1. TOP LEFT: SCORE, MULTIPLIER, COINS
        // -------------------------------------------------------------
        drawScorePanel(g2, score, coins);

        // -------------------------------------------------------------
        // 2. TOP CENTER: STAGE & BONUS
        // -------------------------------------------------------------
        drawStageBanner(g2, screenW, stage, vehicle.fuel);

        // -------------------------------------------------------------
        // 3. TOP RIGHT: DISTANCE MILESTONES & PAUSE BUTTON
        // -------------------------------------------------------------
        drawMilestones(g2, screenW, distanceMeters);

        // -------------------------------------------------------------
        // 4. BOTTOM CORNERS: BRAKE & GAS PEDALS
        // -------------------------------------------------------------
        drawPedals(g2, screenW, screenH, brakePressed, gasPressed);

        // -------------------------------------------------------------
        // 5. BOTTOM CENTER: TRIPLE ANALOG GAUGES (RPM, FUEL, BOOST)
        // -------------------------------------------------------------
        drawGauges(g2, screenW, screenH, vehicle);

        // -------------------------------------------------------------
        // 6. STUNT POPUP BANNER (LIFO Stack Top Event)
        // -------------------------------------------------------------
        if (currentStunt != null && !currentStunt.isEmpty()) {
            drawStuntPopup(g2, screenW, currentStunt);
        }

        // -------------------------------------------------------------
        // 7. KEYBOARD CONTROLS HELPER OVERLAY
        // -------------------------------------------------------------
        drawControlsHint(g2, screenW, screenH);
    }

    private static void drawScorePanel(Graphics2D g2, int score, int coins) {
        int x = 20;
        int y = 20;

        // Dark pill background for score
        g2.setColor(new Color(15, 23, 42, 180));
        g2.fillRoundRect(x, y, 160, 48, 24, 24);

        g2.setFont(new Font("Impact", Font.PLAIN, 12));
        g2.setColor(new Color(148, 163, 184));
        g2.drawString("SCORE", x + 18, y + 18);

        g2.setFont(new Font("Impact", Font.BOLD, 22));
        g2.setColor(Color.WHITE);
        g2.drawString(String.valueOf(score), x + 18, y + 40);

        // Red "x2 HOT" multiplier badge (like in photo!)
        g2.setColor(new Color(239, 68, 68));
        g2.setFont(new Font("Impact", Font.BOLD, 14));
        g2.drawString("X2", x + 95, y + 26);
        g2.setColor(new Color(251, 146, 60));
        g2.setFont(new Font("Impact", Font.PLAIN, 11));
        g2.drawString("HOT", x + 115, y + 26);

        // Coin counter row below
        int cy = y + 56;
        g2.setColor(new Color(15, 23, 42, 180));
        g2.fillRoundRect(x, cy, 140, 32, 16, 16);

        // Gold coin icon
        g2.setColor(new Color(245, 158, 11));
        g2.fillOval(x + 8, cy + 6, 20, 20);
        g2.setColor(new Color(254, 240, 138));
        g2.fillOval(x + 10, cy + 8, 16, 16);
        g2.setColor(new Color(146, 64, 14));
        g2.setFont(new Font("Impact", Font.BOLD, 12));
        g2.drawString("$", x + 15, cy + 21);

        // Coin number
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Impact", Font.BOLD, 18));
        g2.drawString(String.format("%,d", coins), x + 35, cy + 22);
    }

    private static void drawStageBanner(Graphics2D g2, int screenW, int stage, double fuel) {
        int cx = screenW / 2;

        // "BONUS"
        g2.setColor(new Color(245, 158, 11));
        g2.setFont(new Font("Impact", Font.BOLD, 16));
        String bonusStr = "BONUS";
        FontMetrics fm1 = g2.getFontMetrics();
        g2.drawString(bonusStr, cx - fm1.stringWidth(bonusStr) / 2, 40);

        // "30"
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Impact", Font.BOLD, 32));
        String numStr = "30";
        FontMetrics fm2 = g2.getFontMetrics();
        g2.drawString(numStr, cx - fm2.stringWidth(numStr) / 2, 72);

        // Big cyan "STAGE 4" banner (just like photo!)
        g2.setFont(new Font("Impact", Font.BOLD, 42));
        String stageStr = "STAGE " + stage;
        FontMetrics fm3 = g2.getFontMetrics();
        int sx = cx - fm3.stringWidth(stageStr) / 2;
        int sy = 120;

        // Dark outline shadow
        g2.setColor(new Color(15, 23, 42));
        g2.drawString(stageStr, sx + 3, sy + 3);

        // Cyan face
        g2.setColor(new Color(56, 189, 248));
        g2.drawString(stageStr, sx, sy);

        // Low fuel warning alarm if below 25%
        if (fuel < 25.0) {
            boolean blink = (System.currentTimeMillis() / 300) % 2 == 0;
            if (blink) {
                g2.setColor(new Color(239, 68, 68));
                g2.setFont(new Font("Impact", Font.BOLD, 26));
                String lowFuel = "LOW FUEL!";
                FontMetrics fmFuel = g2.getFontMetrics();
                g2.drawString(lowFuel, cx - fmFuel.stringWidth(lowFuel) / 2, 155);
            }
        }
    }

    private static void drawMilestones(Graphics2D g2, int screenW, int distanceMeters) {
        int x = screenW - 170;
        int y = 20;

        // Checkered Flag milestone (e.g. "966m  +10000")
        g2.setColor(new Color(15, 23, 42, 180));
        g2.fillRoundRect(x, y, 145, 36, 12, 12);

        g2.setColor(new Color(251, 191, 36));
        g2.setFont(new Font("Impact", Font.BOLD, 15));
        g2.drawString((1000 - distanceMeters % 1000) + "m", x + 35, y + 23);
        g2.setColor(new Color(74, 222, 128));
        g2.setFont(new Font("Impact", Font.PLAIN, 12));
        g2.drawString("+10000", x + 90, y + 23);

        // Flag icon
        g2.setColor(Color.WHITE);
        g2.fillRect(x + 10, y + 8, 16, 12);
        g2.setColor(Color.BLACK);
        g2.fillRect(x + 10, y + 8, 8, 6);
        g2.fillRect(x + 18, y + 14, 8, 6);
        g2.setColor(new Color(100, 116, 139));
        g2.fillRect(x + 8, y + 8, 3, 20);

        // Mountain summit milestone (e.g. "266m  +1000")
        int my = y + 42;
        g2.setColor(new Color(15, 23, 42, 180));
        g2.fillRoundRect(x, my, 145, 36, 12, 12);

        g2.setColor(new Color(251, 191, 36));
        g2.setFont(new Font("Impact", Font.BOLD, 15));
        g2.drawString(distanceMeters + "m", x + 35, my + 23);
        g2.setColor(new Color(74, 222, 128));
        g2.setFont(new Font("Impact", Font.PLAIN, 12));
        g2.drawString("+1000", x + 90, my + 23);

        // Mountain icon
        g2.setColor(new Color(34, 197, 94));
        g2.fillPolygon(new int[]{x + 10, x + 18, x + 26}, new int[]{my + 26, my + 10, my + 26}, 3);

        // Pause Button "||" at top right corner
        int px = screenW - 55;
        int py = 90;
        g2.setColor(new Color(15, 23, 42, 180));
        g2.fillRoundRect(px, py, 36, 36, 10, 10);
        g2.setColor(Color.WHITE);
        g2.fillRect(px + 10, py + 9, 5, 18);
        g2.fillRect(px + 21, py + 9, 5, 18);
    }

    private static void drawPedals(Graphics2D g2, int screenW, int screenH, boolean brakePressed, boolean gasPressed) {
        int pedalW = 75;
        int pedalH = 110;
        int padY = screenH - pedalH - 25;

        // Left Pedal: BRAKE (Left Arrow / A)
        drawSinglePedal(g2, 35, padY, pedalW, pedalH, brakePressed, "BRAKE");

        // Right Pedal: GAS (Right Arrow / D)
        drawSinglePedal(g2, screenW - pedalW - 35, padY, pedalW, pedalH, gasPressed, "GAS");
    }

    private static void drawSinglePedal(Graphics2D g2, int x, int y, int w, int h, boolean pressed, String label) {
        int dy = pressed ? 8 : 0; // 3D push displacement

        // Pedal metallic grey plate with rounded corners
        GradientPaint plateGrad = new GradientPaint(x, y + dy, new Color(148, 163, 184), x + w, y + h + dy, new Color(71, 85, 105));
        g2.setPaint(plateGrad);
        g2.fillRoundRect(x, y + dy, w, h, 28, 28);

        // Dark bevel border
        g2.setColor(new Color(30, 41, 59));
        g2.setStroke(new BasicStroke(3.5f));
        g2.drawRoundRect(x, y + dy, w, h, 28, 28);
        g2.setStroke(new BasicStroke(1));

        // 6 Black Traction Grip Holes (Just like the photo!)
        g2.setColor(new Color(15, 23, 42));
        int[][] holes = {
            {x + 22, y + 25 + dy}, {x + 50, y + 25 + dy},
            {x + 22, y + 55 + dy}, {x + 50, y + 55 + dy},
            {x + 22, y + 85 + dy}, {x + 50, y + 85 + dy}
        };
        for (int[] hole : holes) {
            g2.fillOval(hole[0] - 7, hole[1] - 9, 14, 18);
        }

        // Label above pedal
        g2.setFont(new Font("Impact", Font.PLAIN, 12));
        g2.setColor(pressed ? new Color(251, 191, 36) : new Color(203, 213, 225));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(label, x + (w - fm.stringWidth(label)) / 2, y + dy - 6);
    }

    private static void drawGauges(Graphics2D g2, int screenW, int screenH, Vehicle vehicle) {
        int cx = screenW / 2;
        int cy = screenH - 75;

        // Center Gauge: Main Fuel & Speedometer (Diameter 130px)
        drawFuelSpeedometer(g2, cx, cy - 10, 68, vehicle.fuel);

        // Left Gauge: RPM Tachometer (Diameter 95px)
        drawRpmGauge(g2, cx - 115, cy + 10, 48, vehicle.rpm);

        // Right Gauge: BOOST Gauge (Diameter 95px)
        drawBoostGauge(g2, cx + 115, cy + 10, 48, vehicle.boostGauge);
    }

    private static void drawFuelSpeedometer(Graphics2D g2, int cx, int cy, int radius, double fuelPercent) {
        // Outer black bezel
        g2.setColor(new Color(15, 23, 42));
        g2.fillOval(cx - radius - 6, cy - radius - 6, (radius + 6) * 2, (radius + 6) * 2);

        // Dark metallic dial face
        GradientPaint dialGrad = new GradientPaint(cx, cy - radius, new Color(30, 41, 59), cx, cy + radius, new Color(15, 23, 42));
        g2.setPaint(dialGrad);
        g2.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);

        // Multi-color Arc (Red -> Orange -> Yellow -> Green) (Exactly like the photo!)
        int arcR = radius - 14;
        g2.setStroke(new BasicStroke(10.0f));

        // Red zone (0 - 25%)
        g2.setColor(new Color(239, 68, 68));
        g2.drawArc(cx - arcR, cy - arcR, arcR * 2, arcR * 2, 140, 40);

        // Orange zone (25 - 50%)
        g2.setColor(new Color(249, 115, 22));
        g2.drawArc(cx - arcR, cy - arcR, arcR * 2, arcR * 2, 100, 40);

        // Yellow zone (50 - 75%)
        g2.setColor(new Color(234, 179, 8));
        g2.drawArc(cx - arcR, cy - arcR, arcR * 2, arcR * 2, 60, 40);

        // Green zone (75 - 100%)
        g2.setColor(new Color(34, 197, 94));
        g2.drawArc(cx - arcR, cy - arcR, arcR * 2, arcR * 2, 0, 60);

        g2.setStroke(new BasicStroke(1));

        // Fuel pump icon in center
        g2.setColor(Color.WHITE);
        g2.fillRect(cx - 7, cy + 15, 14, 16);
        g2.setColor(new Color(15, 23, 42));
        g2.fillRect(cx - 4, cy + 18, 8, 6);
        g2.setColor(Color.WHITE);
        g2.drawArc(cx + 4, cy + 16, 7, 10, 0, 180); // Hose

        // Swinging Needle based on fuel percentage (From 180° to 0°)
        double needleAngle = Math.toRadians(180 - (fuelPercent / 100.0) * 180);
        int nx = cx + (int) (Math.cos(needleAngle) * (radius - 18));
        int ny = cy - (int) (Math.sin(needleAngle) * (radius - 18));

        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(3.0f));
        g2.drawLine(cx, cy, nx, ny);
        g2.setStroke(new BasicStroke(1));

        // Center hub cap
        g2.setColor(new Color(203, 213, 225));
        g2.fillOval(cx - 8, cy - 8, 16, 16);
    }

    private static void drawRpmGauge(Graphics2D g2, int cx, int cy, int radius, double rpm) {
        // Outer black bezel
        g2.setColor(new Color(15, 23, 42));
        g2.fillOval(cx - radius - 5, cy - radius - 5, (radius + 5) * 2, (radius + 5) * 2);

        // Dial face
        g2.setColor(new Color(30, 41, 59));
        g2.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);

        // Tick marks around dial
        g2.setColor(new Color(203, 213, 225));
        g2.setStroke(new BasicStroke(2.0f));
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

        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawLine(cx, cy, nx, ny);
        g2.setStroke(new BasicStroke(1));

        g2.setColor(new Color(203, 213, 225));
        g2.fillOval(cx - 6, cy - 6, 12, 12);
    }

    private static void drawBoostGauge(Graphics2D g2, int cx, int cy, int radius, double boost) {
        // Outer black bezel
        g2.setColor(new Color(15, 23, 42));
        g2.fillOval(cx - radius - 5, cy - radius - 5, (radius + 5) * 2, (radius + 5) * 2);

        // Dial face
        g2.setColor(new Color(30, 41, 59));
        g2.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);

        // Boost arc (Cyan & Amber)
        int arcR = radius - 10;
        g2.setColor(new Color(56, 189, 248));
        g2.setStroke(new BasicStroke(5.0f));
        g2.drawArc(cx - arcR, cy - arcR, arcR * 2, arcR * 2, 0, 180);
        g2.setStroke(new BasicStroke(1));

        // BOOST label
        g2.setFont(new Font("Impact", Font.PLAIN, 13));
        g2.setColor(Color.WHITE);
        String label = "BOOST";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(label, cx - fm.stringWidth(label) / 2, cy + 28);

        // Needle
        double needleAngle = Math.toRadians(180 - (boost / 100.0) * 180);
        int nx = cx + (int) (Math.cos(needleAngle) * (radius - 14));
        int ny = cy - (int) (Math.sin(needleAngle) * (radius - 14));

        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawLine(cx, cy, nx, ny);
        g2.setStroke(new BasicStroke(1));

        g2.setColor(new Color(203, 213, 225));
        g2.fillOval(cx - 6, cy - 6, 12, 12);
    }

    private static void drawStuntPopup(Graphics2D g2, int screenW, String stuntText) {
        int cx = screenW / 2;
        int cy = 180;

        g2.setFont(new Font("Impact", Font.BOLD, 28));
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(stuntText);

        g2.setColor(new Color(15, 23, 42, 210));
        g2.fillRoundRect(cx - tw / 2 - 16, cy - 28, tw + 32, 40, 12, 12);
        g2.setColor(new Color(251, 191, 36));
        g2.setStroke(new BasicStroke(2.0f));
        g2.drawRoundRect(cx - tw / 2 - 16, cy - 28, tw + 32, 40, 12, 12);

        g2.setColor(new Color(253, 224, 71));
        g2.drawString(stuntText, cx - tw / 2, cy);
        g2.setStroke(new BasicStroke(1));
    }

    private static void drawControlsHint(Graphics2D g2, int screenW, int screenH) {
        g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        g2.setColor(new Color(255, 255, 255, 160));
        String hint = "ARROW KEYS: [→] GAS / WHEELIE   |   [←] BRAKE / AIR LEAN   |   [SPACE] NITRO BOOST   |   [R] RESTART";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(hint, (screenW - fm.stringWidth(hint)) / 2, screenH - 8);
    }
}
