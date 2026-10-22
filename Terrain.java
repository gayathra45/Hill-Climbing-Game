import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Clean, Smooth Procedural Terrain Engine for Hill Climb Racing.
 * 
 * Design Principles:
 * - Simple, clean, elegant visuals (avoiding visual clutter).
 * - Ultra-smooth rolling hill waves (continuous gentle slopes without sharp kinks).
 * - Smooth wooden suspension bridges across gentle ravines.
 * - Clean multi-layer parallax countryside (Sky, Mountains, Meadow, Green Grass & Golden Earth).
 */
public class Terrain {
    public static class Bridge {
        public double startX, endX;
        public double startY, endY;

        public Bridge(double startX, double startY, double endX, double endY) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
        }

        public boolean contains(double x) {
            return x >= startX && x <= endX;
        }

        public double getHeightAt(double x) {
            double t = (x - startX) / (endX - startX);
            double sag = Math.sin(t * Math.PI) * 16.0; // Gentle bridge sag
            return (startY + (endY - startY) * t) + sag;
        }
    }

    private final List<Bridge> bridges = new ArrayList<>();
    public final CustomQueue<Double> upcomingChunkQueue = new CustomQueue<>(50);

    public Terrain() {
        // Bridges placed smoothly along the route
        bridges.add(new Bridge(600, 470, 950, 490));
        bridges.add(new Bridge(2100, 480, 2500, 460));
        bridges.add(new Bridge(3800, 490, 4250, 480));
        bridges.add(new Bridge(5600, 470, 6050, 490));

        for (int i = 0; i < 35; i++) {
            upcomingChunkQueue.enqueue(i * 250.0);
        }
    }

    /**
     * Ultra-smooth continuous mathematical height function.
     * Long wavelengths prevent any bumps or jitter, allowing vehicles to glide cleanly.
     */
    public double getHeightAt(double x) {
        // 1. Suspension bridge check
        for (Bridge b : bridges) {
            if (b.contains(x)) {
                return b.getHeightAt(x);
            }
        }

        // 2. Base elevation
        double base = 480.0;

        // Warmup flat section (0 - 250)
        if (x < 250) {
            return base + Math.sin(x * 0.006) * 10.0;
        }

        // Smooth, long rolling hill curves (long gentle slopes)
        double hill1 = Math.sin(x * 0.0016) * 52.0;  // Gentle rolling landscape
        double hill2 = Math.cos(x * 0.0036) * 26.0;  // Smooth secondary crests

        return base + hill1 + hill2;
    }

    /**
     * Calculates surface normal / tangent angle in radians for traction & slope gravity.
     */
    public double getSlopeAt(double x) {
        double delta = 6.0;
        double y1 = getHeightAt(x - delta);
        double y2 = getHeightAt(x + delta);
        return Math.atan2(y2 - y1, delta * 2.0);
    }

    public boolean isBridgeAt(double x) {
        for (Bridge b : bridges) {
            if (b.contains(x)) return true;
        }
        return false;
    }

    /**
     * Simple, clean rendering: Sky, Mountains, Meadow, Bridges, Ground.
     */
    public void render(Graphics2D g2, int screenW, int screenH, double camX, double camY) {
        // -------------------------------------------------------------
        // 1. SKY (Clean pastel gradient)
        // -------------------------------------------------------------
        GradientPaint skyGrad = new GradientPaint(0, 0, new Color(135, 206, 250), 0, screenH, new Color(224, 242, 254));
        g2.setPaint(skyGrad);
        g2.fillRect(0, 0, screenW, screenH);

        // Fluffy clouds
        drawClouds(g2, screenW, camX);

        // Subtle decorative background stars (clean & minimal)
        drawBackgroundStars(g2, screenW, camX);

        // -------------------------------------------------------------
        // 2. DISTANT BACKGROUND HILLS (Clean Parallax)
        // -------------------------------------------------------------
        drawDistantHills(g2, screenW, screenH, camX * 0.12);

        // -------------------------------------------------------------
        // 3. MIDGROUND COUNTRYSIDE (Red Barn, Gentle Pastures)
        // -------------------------------------------------------------
        drawMidgroundPasture(g2, screenW, screenH, camX * 0.32);

        // -------------------------------------------------------------
        // 4. CLEAN SUSPENSION BRIDGES
        // -------------------------------------------------------------
        drawSuspensionBridges(g2, camX, camY);

        // -------------------------------------------------------------
        // 5. MAIN FOREGROUND TERRAIN (Smooth, clean grass & soil)
        // -------------------------------------------------------------
        drawForegroundTerrain(g2, screenW, screenH, camX, camY);

        // -------------------------------------------------------------
        // 6. MINIMAL CHECKPOINT PINS & FENCES
        // -------------------------------------------------------------
        drawRoadsideDetails(g2, screenW, camX, camY);
    }

    private void drawClouds(Graphics2D g2, int screenW, double camX) {
        g2.setColor(new Color(255, 255, 255, 200));
        double cloudOffset = (camX * 0.05) % (screenW + 400);

        int[][] clouds = {
            {70, 50, 110, 42},
            {380, 85, 130, 46},
            {720, 45, 100, 38},
            {1040, 75, 120, 44}
        };

        for (int[] c : clouds) {
            int cx = (int) (c[0] - cloudOffset);
            if (cx < -160) cx += (screenW + 400);
            int cy = c[1];
            int cw = c[2];
            int ch = c[3];

            g2.fillRoundRect(cx, cy, cw, ch, 30, 30);
            g2.fillOval(cx + cw / 5, cy - ch / 3, cw / 2, ch);
            g2.fillOval(cx + cw / 2, cy - ch / 4, cw / 3, ch);
        }
    }

    private void drawBackgroundStars(Graphics2D g2, int screenW, double camX) {
        g2.setStroke(new BasicStroke(2.5f));
        int starCycle = 700;

        int sx1 = (int) (260 - ((camX * 0.2) % starCycle));
        if (sx1 < -100) sx1 += starCycle;
        g2.setColor(new Color(244, 114, 182, 140)); // Soft pink star
        drawStarShape(g2, sx1, 150, 55, 25, 5);

        int sx2 = (int) (620 - ((camX * 0.2) % starCycle));
        if (sx2 < -100) sx2 += starCycle;
        g2.setColor(new Color(192, 132, 252, 140)); // Soft purple star
        drawStarShape(g2, sx2, 120, 65, 30, 5);

        g2.setStroke(new BasicStroke(1));
    }

    private void drawStarShape(Graphics2D g2, int cx, int cy, int outerR, int innerR, int points) {
        double angleStep = Math.PI / points;
        int[] xPts = new int[points * 2];
        int[] yPts = new int[points * 2];
        double currentAngle = -Math.PI / 2;

        for (int i = 0; i < points * 2; i++) {
            int r = (i % 2 == 0) ? outerR : innerR;
            xPts[i] = cx + (int) (Math.cos(currentAngle) * r);
            yPts[i] = cy + (int) (Math.sin(currentAngle) * r);
            currentAngle += angleStep;
        }
        g2.drawPolygon(xPts, yPts, points * 2);
    }

    private void drawDistantHills(Graphics2D g2, int screenW, int screenH, double scrollX) {
        Path2D.Double hillPath = new Path2D.Double();
        hillPath.moveTo(0, screenH);

        for (int x = 0; x <= screenW + 40; x += 30) {
            double worldX = x + scrollX;
            double y = 280 + Math.sin(worldX * 0.0014) * 60;
            if (x == 0) hillPath.lineTo(0, y);
            else hillPath.lineTo(x, y);
        }
        hillPath.lineTo(screenW, screenH);
        hillPath.closePath();

        GradientPaint distGrad = new GradientPaint(0, 200, new Color(147, 245, 175, 160), 0, screenH, new Color(86, 222, 132, 200));
        g2.setPaint(distGrad);
        g2.fill(hillPath);
    }

    private void drawMidgroundPasture(Graphics2D g2, int screenW, int screenH, double scrollX) {
        Path2D.Double midPath = new Path2D.Double();
        midPath.moveTo(0, screenH);

        for (int x = 0; x <= screenW + 40; x += 25) {
            double worldX = x + scrollX;
            double y = 360 + Math.sin(worldX * 0.0028) * 42;
            if (x == 0) midPath.lineTo(0, y);
            else midPath.lineTo(x, y);
        }
        midPath.lineTo(screenW, screenH);
        midPath.closePath();

        GradientPaint midGrad = new GradientPaint(0, 300, new Color(254, 240, 138, 200), 0, screenH, new Color(163, 230, 53, 210));
        g2.setPaint(midGrad);
        g2.fill(midPath);

        // Simple Country Barn
        int barnCycle = 900;
        int bx = (int) (640 - (scrollX % barnCycle));
        if (bx < -120) bx += barnCycle;

        int by = 350;
        g2.setColor(new Color(185, 28, 28)); // Red barn
        g2.fillRect(bx, by, 65, 42);
        int[] rx = {bx - 4, bx + 32, bx + 69};
        int[] ry = {by, by - 22, by};
        g2.setColor(Color.WHITE); // White roof trim
        g2.fillPolygon(rx, ry, 3);
        g2.setColor(new Color(15, 23, 42));
        g2.fillRect(bx + 22, by + 16, 22, 26);
    }

    private void drawSuspensionBridges(Graphics2D g2, double camX, double camY) {
        for (Bridge b : bridges) {
            int t1x = (int) (b.startX - camX);
            int t1y = (int) (b.startY - camY);
            int t2x = (int) (b.endX - camX);
            int t2y = (int) (b.endY - camY);

            // Clean wooden pillars at ends
            g2.setColor(new Color(120, 53, 15));
            g2.setStroke(new BasicStroke(7.0f));
            g2.drawLine(t1x, t1y - 35, t1x, t1y + 90);
            g2.drawLine(t2x, t2y - 35, t2x, t2y + 90);

            // Suspension cables
            g2.setColor(new Color(217, 119, 6));
            g2.setStroke(new BasicStroke(3.0f));

            Path2D.Double rope = new Path2D.Double();
            rope.moveTo(t1x, t1y - 25);

            for (double x = b.startX; x <= b.endX; x += 15) {
                double sx = x - camX;
                double sy = b.getHeightAt(x) - camY - 20;
                rope.lineTo(sx, sy);

                // Vertical hanger ropes
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawLine((int) sx, (int) sy, (int) sx, (int) (b.getHeightAt(x) - camY));
            }
            g2.setStroke(new BasicStroke(2.5f));
            g2.draw(rope);

            // Clean wooden deck planks
            for (double x = b.startX; x <= b.endX; x += 16) {
                int px = (int) (x - camX);
                int py = (int) (b.getHeightAt(x) - camY);

                g2.setColor(new Color(180, 83, 9));
                g2.fillRoundRect(px - 7, py - 4, 15, 8, 3, 3);
                g2.setColor(new Color(67, 20, 7));
                g2.drawRoundRect(px - 7, py - 4, 15, 8, 3, 3);
            }
            g2.setStroke(new BasicStroke(1));
        }
    }

    private void drawForegroundTerrain(Graphics2D g2, int screenW, int screenH, double camX, double camY) {
        Path2D.Double groundPath = new Path2D.Double();
        groundPath.moveTo(0, screenH);

        int step = 6; // High density step for ultra-smooth rendering
        int points = (screenW / step) + 4;
        double[] xVals = new double[points];
        double[] yVals = new double[points];

        for (int i = 0; i < points; i++) {
            double worldX = camX + (i * step);
            double worldY = getHeightAt(worldX);
            xVals[i] = i * step;
            yVals[i] = worldY - camY;

            if (i == 0) groundPath.lineTo(xVals[i], yVals[i]);
            else groundPath.lineTo(xVals[i], yVals[i]);
        }

        groundPath.lineTo(screenW + 20, screenH);
        groundPath.closePath();

        // 1. Simple, clean golden earth body
        GradientPaint earthGrad = new GradientPaint(0, 360, new Color(245, 158, 11), 0, screenH, new Color(217, 119, 6));
        g2.setPaint(earthGrad);
        g2.fill(groundPath);

        // 2. Clean dark organic soil band (8px)
        g2.setColor(new Color(120, 53, 15));
        g2.setStroke(new BasicStroke(8.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D.Double crustPath = new Path2D.Double();
        crustPath.moveTo(xVals[0], yVals[0] + 3);
        for (int i = 1; i < points; i++) {
            crustPath.lineTo(xVals[i], yVals[i] + 3);
        }
        g2.draw(crustPath);

        // 3. Crisp vibrant green grass line on top (5px)
        g2.setColor(new Color(132, 204, 22));
        g2.setStroke(new BasicStroke(5.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D.Double grassPath = new Path2D.Double();
        grassPath.moveTo(xVals[0], yVals[0]);
        for (int i = 1; i < points; i++) {
            grassPath.lineTo(xVals[i], yVals[i]);
        }
        g2.draw(grassPath);

        g2.setStroke(new BasicStroke(1));
    }

    private void drawRoadsideDetails(Graphics2D g2, int screenW, double camX, double camY) {
        // Minimal wooden fence posts along pasture
        for (double wx = (int)(camX / 110) * 110; wx <= camX + screenW + 80; wx += 110) {
            if (isBridgeAt(wx)) continue;

            int sx = (int) (wx - camX);
            int sy = (int) (getHeightAt(wx) - camY);

            // Fence post
            g2.setColor(new Color(146, 64, 14));
            g2.fillRect(sx - 3, sy - 20, 6, 22);

            // Rail
            g2.setColor(new Color(180, 83, 9));
            g2.fillRect(sx - 55, sy - 15, 110, 4);
        }

        // Green Checkpoint Pin at 34m
        double pinWorldX = 340.0;
        int px = (int) (pinWorldX - camX);
        int py = (int) (getHeightAt(pinWorldX) - camY);

        if (px > -60 && px < screenW + 60) {
            g2.setColor(new Color(22, 163, 74));
            g2.fillOval(px - 10, py - 30, 20, 20);
            g2.fillPolygon(new int[]{px - 7, px + 7, px}, new int[]{py - 18, py - 18, py - 4}, 3);
            g2.setColor(Color.WHITE);
            g2.fillOval(px - 4, py - 24, 8, 8);

            g2.setFont(new Font("Impact", Font.BOLD, 14));
            g2.drawString("34m", px + 12, py - 14);
        }
    }
}
