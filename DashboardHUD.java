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

    public static void render(Graphics2D g2, int screenW, int screenH, Vehicle vehicle, 
                              int score, int coins, int stage, int distanceMeters, 
                              String currentStunt, boolean gasPressed, boolean brakePressed) {

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 1. TOP LEFT: SCORE, HOT MULTIPLIER, COINS
        drawScorePanel(g2, score, coins);

        // 2. TOP CENTER: STAGE & BONUS
        drawStageBanner(g2, screenW, stage, vehicle.fuel);

        // 3. TOP RIGHT: DISTANCE MILESTONES & PAUSE BUTTON
        drawMilestones(g2, screenW, distanceMeters);

        // 4. BOTTOM CORNERS: REALISTIC METAL FOOT PEDALS
        drawPedals(g2, screenW, screenH, brakePressed, gasPressed);

        // 5. BOTTOM CENTER: TRIPLE ANALOG INSTRUMENT GAUGES
        drawGauges(g2, screenW, screenH, vehicle);

        // 6. STUNT POPUP BANNER (LIFO Stack Top Event)
        if (currentStunt != null && !currentStunt.isEmpty()) {
            drawStuntPopup(g2, screenW, currentStunt);
        }

        // 7. KEYBOARD CONTROLS HELPER OVERLAY
        drawControlsHint(g2, screenW, screenH);
    }

    private static void drawScorePanel(Graphics2D g2, int score, int coins) {
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

        // Big cyan "STAGE 4" championship banner
        g2.setFont(new Font("Impact", Font.BOLD, 42));
        String stageStr = "STAGE " + stage;
        FontMetrics fm3 = g2.getFontMetrics();
        int sx = cx - fm3.stringWidth(stageStr) / 2;
        int sy = 118;

        // Drop shadow
        g2.setColor(new Color(15, 23, 42));
        g2.drawString(stageStr, sx + 3, sy + 3);

        // Cyan face
        g2.setColor(new Color(56, 189, 248));
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

    private static void drawMilestones(Graphics2D g2, int screenW, int distanceMeters) {
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

        // Pause Button "||"
        int px = screenW - 55;
        int py = 92;
        g2.setColor(new Color(15, 23, 42, 200));
        g2.fillRoundRect(px, py, 36, 36, 10, 10);
        g2.setColor(Color.WHITE);
        g2.fillRect(px + 10, py + 9, 5, 18);
        g2.fillRect(px + 21, py + 9, 5, 18);
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
        g2.setFont(new Font("Impact", Font.PLAIN, 12));
        g2.setColor(new Color(15, 23, 42, 170));
        g2.drawString("CONTROLS: [RIGHT/D] GAS  |  [LEFT/A] BRAKE  |  [SPACE/UP] BOOST  |  [R] RESTART", 20, screenH - 12);
    }
}
