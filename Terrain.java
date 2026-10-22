import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Authentic Countryside Stage 4 Terrain Engine for Hill Climb Racing.
 * 
 * Accurately matches the user's reference photo:
 * 1. Sky & Atmosphere:
 *    - Vibrant azure blue gradient sky with fluffy clouds.
 *    - Pastel neon star outlines (Pink, Green, Lilac) floating in the atmosphere.
 *    - Distant green rolling hills, red barn farmhouse with white trim, and grazing cows.
 * 2. Foreground Hills & Soil:
 *    - Warm golden earth with bubble circle texture pattern.
 *    - Thick dark organic loam soil band.
 *    - Clustered golden wheat grass tufts along the soil edge.
 * 3. Authentic Wooden Rope Suspension Bridge:
 *    - Timber deck with sagging rope suspension and diagonal rope truss lashings.
 *    - Heavy timber end pylon bound with rope windings.
 *    - Green "34m" checkpoint pin.
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
            double sag = Math.sin(t * Math.PI) * 18.0;
            return (startY + (endY - startY) * t) + sag;
        }
    }

    private final List<Bridge> bridges = new ArrayList<>();
    public final CustomQueue<Double> upcomingChunkQueue = new CustomQueue<>(60);
    private double animTime = 0;

    public Terrain() {
        // Dynamically initialize bridges to match advanced hill elevations seamlessly
        double b1s = 580, b1e = 960;
        bridges.add(new Bridge(b1s, getBaseHeight(b1s), b1e, getBaseHeight(b1e)));
        double b2s = 2200, b2e = 2600;
        bridges.add(new Bridge(b2s, getBaseHeight(b2s), b2e, getBaseHeight(b2e)));
        double b3s = 4100, b3e = 4550;
        bridges.add(new Bridge(b3s, getBaseHeight(b3s), b3e, getBaseHeight(b3e)));
        double b4s = 6200, b4e = 6680;
        bridges.add(new Bridge(b4s, getBaseHeight(b4s), b4e, getBaseHeight(b4e)));
        double b5s = 8500, b5e = 9000;
        bridges.add(new Bridge(b5s, getBaseHeight(b5s), b5e, getBaseHeight(b5e)));

        for (int i = 0; i < 50; i++) {
            upcomingChunkQueue.enqueue(i * 250.0);
        }
    }

    /**
     * Advanced continuous height function with dramatic topography:
     * - Flat starting straightaway.
     * - Thrilling rolling climbs, rollercoaster dips, launch crests, and steep hillclimb challenges.
     */
    public double getBaseHeight(double x) {
        double base = 480.0;

        // Flat starting straightaway
        if (x < 240) {
            return base + Math.sin(x * 0.006) * 5.0;
        }

        // Advanced undulating landscape with multi-tier elevation:
        double mountain = Math.sin(x * 0.00085) * 85.0;  // Grand elevation rise & fall
        double hillClimb = Math.sin(x * 0.0022) * 68.0; // Steep technical slopes
        double kicker = Math.cos(x * 0.0048) * 32.0;    // Launch kickers & whoops

        return base + mountain + hillClimb + kicker;
    }

    public double getHeightAt(double x) {
        for (Bridge b : bridges) {
            if (b.contains(x)) {
                return b.getHeightAt(x);
            }
        }
        return getBaseHeight(x);
    }

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

    public void render(Graphics2D g2, int screenW, int screenH, double camX, double camY) {
        animTime += 0.03;

        // 1. SKY & CLOUDS (Vibrant Azure Countryside Blue)
        drawSky(g2, screenW, screenH);
        drawClouds(g2, screenW, camX);

        // 2. PASTEL NEON STARS (Pink, Green, Lilac outline stars like in photo!)
        drawPastelStars(g2, screenW, camX);

        // 3. PARALLAX DISTANT GREEN HILLS (0.10x)
        drawDistantHills(g2, screenW, screenH, camX * 0.10);

        // 4. MIDGROUND COUNTRYSIDE (Red Barn, Cows, Windmill) (0.28x)
        drawMidgroundCountryside(g2, screenW, screenH, camX * 0.28);

        // 5. WOODEN ROPE SUSPENSION BRIDGES
        drawSuspensionBridges(g2, camX, camY);

        // 6. MAIN FOREGROUND HILLS (Golden Earth with circles, dark crust, wheat grass)
        drawForegroundHills(g2, screenW, screenH, camX, camY);

        // 7. ROADSIDE 34m PIN & TIMBER POSTS
        drawRoadsideDetails(g2, screenW, camX, camY);
    }

    private void drawSky(Graphics2D g2, int screenW, int screenH) {
        // Brilliant Azure Blue sky gradient matching reference photo
        GradientPaint skyGrad = new GradientPaint(
            0, 0, new Color(110, 195, 255),
            0, screenH * 0.70f, new Color(224, 242, 254)
        );
        g2.setPaint(skyGrad);
        g2.fillRect(0, 0, screenW, screenH);
    }

    private void drawClouds(Graphics2D g2, int screenW, double camX) {
        g2.setColor(new Color(255, 255, 255, 210));
        double cloudOffset = (camX * 0.05) % (screenW + 500);

        int[][] clouds = {
            {70, 48, 120, 44},
            {420, 75, 140, 48},
            {780, 40, 110, 40},
            {1150, 68, 130, 46}
        };

        for (int[] c : clouds) {
            int cx = (int) (c[0] - cloudOffset);
            if (cx < -180) cx += (screenW + 500);
            int cy = c[1];
            int cw = c[2];
            int ch = c[3];

            g2.fillRoundRect(cx, cy, cw, ch, 32, 32);
            g2.fillOval(cx + cw / 5, cy - ch / 3, cw / 2, ch);
            g2.fillOval(cx + cw / 2, cy - ch / 4, cw / 3, ch);
        }
    }

    private void drawPastelStars(Graphics2D g2, int screenW, double camX) {
        // Large outline stars floating in sky (Just like the reference photo!)
        int cycle = 850;

        // Big Pink Star
        int sx1 = (int) (220 - ((camX * 0.18) % cycle));
        if (sx1 < -120) sx1 += cycle;
        g2.setColor(new Color(244, 114, 182, 170));
        g2.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        drawStarShape(g2, sx1, 150, 65, 30, 5);

        // Big Lilac/Purple Star
        int sx2 = (int) (680 - ((camX * 0.18) % cycle));
        if (sx2 < -120) sx2 += cycle;
        g2.setColor(new Color(192, 132, 252, 170));
        drawStarShape(g2, sx2, 120, 75, 34, 5);

        // Small Lime Star
        int sx3 = (int) (980 - ((camX * 0.18) % cycle));
        if (sx3 < -120) sx3 += cycle;
        g2.setColor(new Color(163, 230, 53, 170));
        drawStarShape(g2, sx3, 220, 36, 16, 5);

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
        Path2D.Double path = new Path2D.Double();
        path.moveTo(0, screenH);

        for (int x = 0; x <= screenW + 40; x += 30) {
            double worldX = x + scrollX;
            double y = 260 + Math.sin(worldX * 0.0012) * 65.0 + Math.cos(worldX * 0.0026) * 25.0;
            if (x == 0) path.lineTo(0, y);
            else path.lineTo(x, y);
        }
        path.lineTo(screenW, screenH);
        path.closePath();

        GradientPaint distGrad = new GradientPaint(
            0, 180, new Color(147, 245, 175, 180),
            0, screenH, new Color(74, 222, 128, 220)
        );
        g2.setPaint(distGrad);
        g2.fill(path);
    }

    private void drawMidgroundCountryside(Graphics2D g2, int screenW, int screenH, double scrollX) {
        Path2D.Double midPath = new Path2D.Double();
        midPath.moveTo(0, screenH);

        for (int x = 0; x <= screenW + 40; x += 25) {
            double worldX = x + scrollX;
            double y = 355 + Math.sin(worldX * 0.0026) * 44.0;
            if (x == 0) midPath.lineTo(0, y);
            else midPath.lineTo(x, y);
        }
        midPath.lineTo(screenW, screenH);
        midPath.closePath();

        GradientPaint midGrad = new GradientPaint(
            0, 310, new Color(254, 240, 138, 220),
            0, screenH, new Color(163, 230, 53, 230)
        );
        g2.setPaint(midGrad);
        g2.fill(midPath);

        // Country Red Barn
        int barnCycle = 950;
        int bx = (int) (700 - (scrollX % barnCycle));
        if (bx < -140) bx += barnCycle;
        int by = 350;

        g2.setColor(new Color(185, 28, 28));
        g2.fillRect(bx, by, 65, 42);
        g2.setColor(Color.WHITE);
        g2.fillPolygon(new int[]{bx - 5, bx + 32, bx + 70}, new int[]{by, by - 22, by}, 3);
        g2.setColor(new Color(30, 41, 59));
        g2.fillRect(bx + 22, by + 16, 22, 26);

        // Grazing cows
        drawPastureCow(g2, bx + 105, by + 26);
        drawPastureCow(g2, bx + 135, by + 28);
    }

    private void drawPastureCow(Graphics2D g2, int cx, int cy) {
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(cx, cy, 24, 15, 5, 5);
        g2.setColor(new Color(15, 23, 42));
        g2.fillOval(cx + 4, cy + 2, 7, 7);
        g2.fillOval(cx + 14, cy + 5, 6, 6);
        g2.setColor(Color.WHITE);
        g2.fillOval(cx + 20, cy - 3, 10, 9);
        g2.setColor(new Color(15, 23, 42));
        g2.fillOval(cx + 25, cy - 1, 4, 4);
        g2.setColor(Color.WHITE);
        g2.fillRect(cx + 3, cy + 14, 3, 8);
        g2.fillRect(cx + 8, cy + 14, 3, 8);
        g2.fillRect(cx + 15, cy + 14, 3, 8);
        g2.fillRect(cx + 20, cy + 14, 3, 8);
    }

    private void drawSuspensionBridges(Graphics2D g2, double camX, double camY) {
        for (Bridge b : bridges) {
            int t1x = (int) (b.startX - camX);
            int t1y = (int) (b.startY - camY);
            int t2x = (int) (b.endX - camX);
            int t2y = (int) (b.endY - camY);

            // Right Timber Pylon with Rope Bindings (Matching photo!)
            drawRopeTimberPost(g2, t2x, t2y);

            // Sagging Rope Suspension Cables
            g2.setColor(new Color(217, 119, 6));
            g2.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            Path2D.Double mainRope = new Path2D.Double();
            mainRope.moveTo(t1x, t1y - 25);

            for (double x = b.startX; x <= b.endX; x += 14) {
                double sx = x - camX;
                double sy = b.getHeightAt(x) - camY - 22;
                mainRope.lineTo(sx, sy);

                // Diagonal rope lattice truss lashings (Exactly as seen in photo!)
                g2.setColor(new Color(180, 83, 9));
                g2.setStroke(new BasicStroke(1.4f));
                g2.drawLine((int) sx, (int) sy, (int) (sx + 10), (int) (b.getHeightAt(x) - camY));
                g2.drawLine((int) sx, (int) sy, (int) (sx - 10), (int) (b.getHeightAt(x) - camY));
            }
            g2.setStroke(new BasicStroke(3.0f));
            g2.setColor(new Color(245, 158, 11));
            g2.draw(mainRope);

            // Wooden Deck Planks
            for (double x = b.startX; x <= b.endX; x += 15) {
                int px = (int) (x - camX);
                int py = (int) (b.getHeightAt(x) - camY);

                g2.setColor(new Color(67, 20, 7, 110));
                g2.fillRoundRect(px - 7, py + 2, 16, 7, 3, 3);

                GradientPaint plankGrad = new GradientPaint(px - 7, py - 4, new Color(217, 119, 6), px + 9, py + 4, new Color(146, 64, 14));
                g2.setPaint(plankGrad);
                g2.fillRoundRect(px - 7, py - 4, 15, 8, 3, 3);
                g2.setColor(new Color(67, 20, 7));
                g2.drawRoundRect(px - 7, py - 4, 15, 8, 3, 3);
            }
            g2.setStroke(new BasicStroke(1));
        }
    }

    private void drawRopeTimberPost(Graphics2D g2, int tx, int ty) {
        // Thick weathered wooden post
        GradientPaint postGrad = new GradientPaint(tx - 6, ty - 50, new Color(180, 83, 9), tx + 8, ty + 40, new Color(120, 53, 15));
        g2.setPaint(postGrad);
        g2.fillRoundRect(tx - 6, ty - 52, 15, 85, 4, 4);
        g2.setColor(new Color(67, 20, 7));
        g2.drawRoundRect(tx - 6, ty - 52, 15, 85, 4, 4);

        // Rope coils wound around the top of the post (just like the photo!)
        g2.setColor(new Color(254, 240, 138));
        g2.setStroke(new BasicStroke(3.0f));
        g2.drawLine(tx - 7, ty - 38, tx + 9, ty - 38);
        g2.drawLine(tx - 7, ty - 34, tx + 9, ty - 34);
        g2.drawLine(tx - 7, ty - 30, tx + 9, ty - 30);
        g2.setStroke(new BasicStroke(1));
    }

    private void drawForegroundHills(Graphics2D g2, int screenW, int screenH, double camX, double camY) {
        Path2D.Double groundPath = new Path2D.Double();
        groundPath.moveTo(0, screenH);

        int step = 5;
        int points = (screenW / step) + 5;
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

        // 1. Golden Earth Body with gradient (Exact color from photo: warm golden amber)
        GradientPaint earthGrad = new GradientPaint(
            0, 350, new Color(245, 158, 11),
            0, screenH, new Color(217, 119, 6)
        );
        g2.setPaint(earthGrad);
        g2.fill(groundPath);

        // 2. Texture: Organic Circle Pattern inside the golden earth (Matching photo!)
        g2.setColor(new Color(234, 138, 8, 110));
        for (int i = 0; i < points - 1; i += 6) {
            double sx = xVals[i];
            double sy = yVals[i] + 45 + Math.sin(sx * 0.03) * 12;
            if (sy < screenH) {
                g2.fillOval((int) sx - 14, (int) sy - 14, 28, 28);
                g2.setColor(new Color(251, 191, 36, 120));
                g2.fillOval((int) sx - 6, (int) sy - 6, 12, 12);
                g2.setColor(new Color(234, 138, 8, 110));
            }
        }

        // 3. Thick Rich Dark Brown Organic Soil Band (12px)
        g2.setColor(new Color(101, 47, 15));
        g2.setStroke(new BasicStroke(11.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D.Double crustPath = new Path2D.Double();
        crustPath.moveTo(xVals[0], yVals[0] + 5);
        for (int i = 1; i < points; i++) {
            crustPath.lineTo(xVals[i], yVals[i] + 5);
        }
        g2.draw(crustPath);

        // Top edge dark outline
        g2.setColor(new Color(67, 20, 7));
        g2.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D.Double edgePath = new Path2D.Double();
        edgePath.moveTo(xVals[0], yVals[0]);
        for (int i = 1; i < points; i++) {
            edgePath.lineTo(xVals[i], yVals[i]);
        }
        g2.draw(edgePath);

        // 4. Clumps of Golden Wheat Grass Tufts (Warm golden meadow stalks like the photo!)
        for (int i = 0; i < points; i += 4) {
            double wx = camX + xVals[i];
            if (isBridgeAt(wx)) continue;

            double gx = xVals[i];
            double gy = yVals[i];
            double wind = Math.sin(wx * 0.08 + animTime * 2.0) * 2.5;

            // Clump of 3-4 golden wheat stalks
            g2.setColor(new Color(234, 179, 8));
            g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine((int) gx, (int) gy, (int) (gx + wind - 2), (int) (gy - 13));
            g2.drawLine((int) gx + 3, (int) gy, (int) (gx + 3 + wind + 2), (int) (gy - 17));

            g2.setColor(new Color(254, 240, 138));
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine((int) gx + 1, (int) gy, (int) (gx + 1 + wind), (int) (gy - 15));
        }

        g2.setStroke(new BasicStroke(1));
    }

    private void drawRoadsideDetails(Graphics2D g2, int screenW, double camX, double camY) {
        // Green Checkpoint Pin at 34m (Exact match to the reference photo!)
        double pinWorldX = 340.0;
        int px = (int) (pinWorldX - camX);
        int py = (int) (getHeightAt(pinWorldX) - camY);

        if (px > -60 && px < screenW + 60) {
            // Dark pin shadow
            g2.setColor(new Color(15, 23, 42, 70));
            g2.fillOval(px - 10, py + 2, 20, 6);

            // Green map marker body
            g2.setColor(new Color(22, 163, 74));
            g2.fillOval(px - 10, py - 30, 20, 20);
            g2.fillPolygon(new int[]{px - 7, px + 7, px}, new int[]{py - 18, py - 18, py - 3}, 3);

            // White dot
            g2.setColor(Color.WHITE);
            g2.fillOval(px - 4, py - 24, 8, 8);

            // Black outer border
            g2.setColor(new Color(15, 23, 42));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(px - 10, py - 30, 20, 20);
            g2.setStroke(new BasicStroke(1));

            // "34m" text
            g2.setFont(new Font("Impact", Font.BOLD, 15));
            g2.setColor(new Color(15, 23, 42));
            g2.drawString("34m", px + 15, py - 14);
            g2.setColor(Color.WHITE);
            g2.drawString("34m", px + 14, py - 15);
        }
    }
}
