import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Photorealistic Procedural Terrain Engine for Hill Climb Racing.
 * 
 * Visual Enhancements:
 * - Radiant sun flare & layered atmospheric parallax mountain vistas.
 * - Countryside midground featuring an animated rotating windmill, rustic red timber barn, and grazing dairy cattle.
 * - Heavy-duty suspension bridge with steel-banded timber pylons, tension cables, and grain-textured wooden deck planks.
 * - Subsurface geological strata: vibrant grass fringe with procedural blade tufts, rich organic topsoil crust, and layered golden earth with embedded sedimentary stones.
 * - Smooth rolling mathematical terrain profile allowing silky-smooth driving without physics jitters.
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
            double sag = Math.sin(t * Math.PI) * 16.0; // Gentle suspension sag
            return (startY + (endY - startY) * t) + sag;
        }
    }

    private final List<Bridge> bridges = new ArrayList<>();
    public final CustomQueue<Double> upcomingChunkQueue = new CustomQueue<>(50);
    private double animTime = 0;

    public Terrain() {
        // Wooden suspension bridges across scenic ravines
        bridges.add(new Bridge(600, 470, 950, 490));
        bridges.add(new Bridge(2100, 480, 2500, 460));
        bridges.add(new Bridge(3800, 490, 4250, 480));
        bridges.add(new Bridge(5600, 470, 6050, 490));
        bridges.add(new Bridge(7500, 485, 8000, 475));

        for (int i = 0; i < 45; i++) {
            upcomingChunkQueue.enqueue(i * 250.0);
        }
    }

    /**
     * Ultra-smooth continuous mathematical height function.
     * Prevents microscopic bumps so the car glides cleanly.
     */
    public double getHeightAt(double x) {
        for (Bridge b : bridges) {
            if (b.contains(x)) {
                return b.getHeightAt(x);
            }
        }

        double base = 480.0;
        if (x < 250) {
            return base + Math.sin(x * 0.006) * 10.0;
        }

        // Long wavelength sinusoidal waves for smooth rolling topography
        double hill1 = Math.sin(x * 0.0016) * 52.0;
        double hill2 = Math.cos(x * 0.0036) * 26.0;

        return base + hill1 + hill2;
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

    /**
     * Complete multi-layered rendering pass.
     */
    public void render(Graphics2D g2, int screenW, int screenH, double camX, double camY) {
        animTime += 0.03;

        // 1. SKY & RADIANT SUN
        drawSkyAndSun(g2, screenW, screenH, camX);

        // 2. PARALLAX CLOUDS & PASTEL STARS
        drawClouds(g2, screenW, camX);
        drawBackgroundStars(g2, screenW, camX);

        // 3. DISTANT MISTY MOUNTAINS (Parallax 0.10)
        drawDistantMountains(g2, screenW, screenH, camX * 0.10);

        // 4. MIDGROUND COUNTRYSIDE (Windmill, Barn, Cows) (Parallax 0.28)
        drawMidgroundCountryside(g2, screenW, screenH, camX * 0.28);

        // 5. TIMBER SUSPENSION BRIDGES
        drawSuspensionBridges(g2, camX, camY);

        // 6. MAIN GROUND TERRAIN & GEOLOGICAL STRATA
        drawForegroundTerrain(g2, screenW, screenH, camX, camY);

        // 7. ROADSIDE DETAILS (Fences, Grass tufts, 34m Pin)
        drawRoadsideDetails(g2, screenW, camX, camY);
    }

    private void drawSkyAndSun(Graphics2D g2, int screenW, int screenH, double camX) {
        // Rich azure sky gradient
        GradientPaint skyGrad = new GradientPaint(
            0, 0, new Color(110, 195, 255),
            0, screenH * 0.75f, new Color(224, 242, 254)
        );
        g2.setPaint(skyGrad);
        g2.fillRect(0, 0, screenW, screenH);

        // Radiant Sun with atmospheric glow
        int sunX = 140;
        int sunY = 90;

        // Soft outer corona
        g2.setColor(new Color(254, 240, 138, 80));
        g2.fillOval(sunX - 70, sunY - 70, 140, 140);
        g2.setColor(new Color(253, 224, 71, 140));
        g2.fillOval(sunX - 45, sunY - 45, 90, 90);

        // Brilliant white/gold sun disk
        g2.setColor(new Color(255, 255, 245));
        g2.fillOval(sunX - 26, sunY - 26, 52, 52);
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

            // Soft 3D cloud shading
            g2.fillRoundRect(cx, cy, cw, ch, 32, 32);
            g2.fillOval(cx + cw / 5, cy - ch / 3, cw / 2, ch);
            g2.fillOval(cx + cw / 2, cy - ch / 4, cw / 3, ch);

            // Subtle bottom shading for cloud depth
            g2.setColor(new Color(226, 232, 240, 160));
            g2.fillRoundRect(cx + 8, cy + ch - 12, cw - 16, 10, 8, 8);
            g2.setColor(new Color(255, 255, 255, 210));
        }
    }

    private void drawBackgroundStars(Graphics2D g2, int screenW, double camX) {
        g2.setStroke(new BasicStroke(2.5f));
        int starCycle = 750;

        int sx1 = (int) (260 - ((camX * 0.18) % starCycle));
        if (sx1 < -100) sx1 += starCycle;
        g2.setColor(new Color(244, 114, 182, 150)); // Soft pink star
        drawStarShape(g2, sx1, 140, 52, 24, 5);

        int sx2 = (int) (640 - ((camX * 0.18) % starCycle));
        if (sx2 < -100) sx2 += starCycle;
        g2.setColor(new Color(192, 132, 252, 150)); // Soft lilac star
        drawStarShape(g2, sx2, 115, 62, 28, 5);

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

    private void drawDistantMountains(Graphics2D g2, int screenW, int screenH, double scrollX) {
        Path2D.Double mountainPath = new Path2D.Double();
        mountainPath.moveTo(0, screenH);

        for (int x = 0; x <= screenW + 40; x += 30) {
            double worldX = x + scrollX;
            double y = 260 + Math.sin(worldX * 0.0012) * 65.0 + Math.cos(worldX * 0.0025) * 22.0;
            if (x == 0) mountainPath.lineTo(0, y);
            else mountainPath.lineTo(x, y);
        }
        mountainPath.lineTo(screenW, screenH);
        mountainPath.closePath();

        // Misty teal/sage mountain gradient with atmospheric perspective
        GradientPaint distGrad = new GradientPaint(
            0, 180, new Color(147, 245, 175, 170),
            0, screenH, new Color(74, 222, 128, 210)
        );
        g2.setPaint(distGrad);
        g2.fill(mountainPath);
    }

    private void drawMidgroundCountryside(Graphics2D g2, int screenW, int screenH, double scrollX) {
        // Rolling pasture layer
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

        // -------------------------------------------------------------
        // ANIMATED WINDMILL (Countryside icon!)
        // -------------------------------------------------------------
        int millCycle = 1350;
        int wx = (int) (320 - (scrollX % millCycle));
        if (wx < -140) wx += millCycle;
        int wy = 330;

        // Windmill stone tower
        g2.setColor(new Color(241, 245, 249));
        int[] mtx = {wx - 14, wx + 14, wx + 9, wx - 9};
        int[] mty = {wy + 48, wy + 48, wy, wy};
        g2.fillPolygon(mtx, mty, 4);
        g2.setColor(new Color(148, 163, 184));
        g2.drawPolygon(mtx, mty, 4);

        // Conical roof
        g2.setColor(new Color(185, 28, 28));
        g2.fillPolygon(new int[]{wx - 12, wx + 12, wx}, new int[]{wy, wy, wy - 15}, 3);

        // Rotating windmill sail blades
        Graphics2D wg = (Graphics2D) g2.create();
        wg.translate(wx, wy + 4);
        wg.rotate(animTime * 1.2);
        wg.setColor(new Color(100, 116, 139));
        wg.setStroke(new BasicStroke(2.0f));
        for (int a = 0; a < 4; a++) {
            wg.rotate(Math.PI / 2);
            wg.drawLine(0, 0, 0, -32);
            // Sail cloth lattice
            wg.setColor(new Color(255, 255, 255, 220));
            wg.fillRect(2, -30, 9, 20);
            wg.setColor(new Color(100, 116, 139));
            wg.drawRect(2, -30, 9, 20);
        }
        wg.dispose();

        // -------------------------------------------------------------
        // RED TIMBER COUNTRY BARN
        // -------------------------------------------------------------
        int barnCycle = 950;
        int bx = (int) (720 - (scrollX % barnCycle));
        if (bx < -140) bx += barnCycle;
        int by = 350;

        // Barn shadow
        g2.setColor(new Color(15, 23, 42, 60));
        g2.fillOval(bx - 8, by + 40, 85, 12);

        // Red wall siding
        g2.setColor(new Color(185, 28, 28));
        g2.fillRect(bx, by, 70, 44);

        // White Gambrel Roof
        int[] rx = {bx - 5, bx + 35, bx + 75};
        int[] ry = {by, by - 24, by};
        g2.setColor(Color.WHITE);
        g2.fillPolygon(rx, ry, 3);
        g2.setColor(new Color(226, 232, 240));
        g2.setStroke(new BasicStroke(2.0f));
        g2.drawPolygon(rx, ry, 3);
        g2.setStroke(new BasicStroke(1));

        // Dark barn doors with white X crossbeams
        g2.setColor(new Color(30, 41, 59));
        g2.fillRect(bx + 24, by + 16, 22, 28);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(bx + 24, by + 16, bx + 46, by + 44);
        g2.drawLine(bx + 46, by + 16, bx + 24, by + 44);
        g2.setStroke(new BasicStroke(1));

        // Hayloft window
        g2.setColor(new Color(254, 240, 138));
        g2.fillRect(bx + 30, by - 12, 10, 10);

        // -------------------------------------------------------------
        // GRAZING PASTURE COWS
        // -------------------------------------------------------------
        int cx = (bx + 110);
        int cy = by + 28;
        drawPastureCow(g2, cx, cy);
    }

    private void drawPastureCow(Graphics2D g2, int cx, int cy) {
        // Body (White with black Holstein patches)
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(cx, cy, 26, 16, 6, 6);
        g2.setColor(new Color(15, 23, 42)); // Black spots
        g2.fillOval(cx + 4, cy + 2, 8, 8);
        g2.fillOval(cx + 15, cy + 6, 7, 6);

        // Head
        g2.setColor(Color.WHITE);
        g2.fillOval(cx + 22, cy - 4, 12, 10);
        g2.setColor(new Color(15, 23, 42));
        g2.fillOval(cx + 28, cy - 2, 5, 5); // Eye & snout

        // Legs
        g2.setColor(Color.WHITE);
        g2.fillRect(cx + 3, cy + 15, 3, 9);
        g2.fillRect(cx + 9, cy + 15, 3, 9);
        g2.fillRect(cx + 17, cy + 15, 3, 9);
        g2.fillRect(cx + 22, cy + 15, 3, 9);
    }

    private void drawSuspensionBridges(Graphics2D g2, double camX, double camY) {
        for (Bridge b : bridges) {
            int t1x = (int) (b.startX - camX);
            int t1y = (int) (b.startY - camY);
            int t2x = (int) (b.endX - camX);
            int t2y = (int) (b.endY - camY);

            // Sturdy wooden pylons with steel bracket bands
            drawBridgeTower(g2, t1x, t1y);
            drawBridgeTower(g2, t2x, t2y);

            // Suspension steel catenary cables
            g2.setColor(new Color(180, 83, 9));
            g2.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            Path2D.Double mainCable = new Path2D.Double();
            mainCable.moveTo(t1x, t1y - 32);

            for (double x = b.startX; x <= b.endX; x += 14) {
                double sx = x - camX;
                double sy = b.getHeightAt(x) - camY - 24;
                mainCable.lineTo(sx, sy);

                // Steel vertical hanger wires
                g2.setColor(new Color(217, 119, 6));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawLine((int) sx, (int) sy, (int) sx, (int) (b.getHeightAt(x) - camY));
            }
            g2.setStroke(new BasicStroke(3.0f));
            g2.setColor(new Color(245, 158, 11));
            g2.draw(mainCable);

            // Wooden Deck Planks with realistic grain & drop shadows
            for (double x = b.startX; x <= b.endX; x += 15) {
                int px = (int) (x - camX);
                int py = (int) (b.getHeightAt(x) - camY);

                // Plank shadow
                g2.setColor(new Color(67, 20, 7, 120));
                g2.fillRoundRect(px - 7, py + 2, 16, 7, 3, 3);

                // Wooden plank face
                GradientPaint plankGrad = new GradientPaint(
                    px - 7, py - 4, new Color(217, 119, 6),
                    px + 9, py + 4, new Color(146, 64, 14)
                );
                g2.setPaint(plankGrad);
                g2.fillRoundRect(px - 7, py - 4, 15, 8, 3, 3);

                // Plank edge outline
                g2.setColor(new Color(67, 20, 7));
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(px - 7, py - 4, 15, 8, 3, 3);

                // Nail rivets
                g2.setColor(new Color(30, 41, 59));
                g2.fillOval(px - 4, py - 1, 2, 2);
                g2.fillOval(px + 4, py - 1, 2, 2);
            }
            g2.setStroke(new BasicStroke(1));
        }
    }

    private void drawBridgeTower(Graphics2D g2, int tx, int ty) {
        // Timber column
        GradientPaint timberGrad = new GradientPaint(
            tx - 5, ty, new Color(146, 64, 14),
            tx + 5, ty, new Color(67, 20, 7)
        );
        g2.setPaint(timberGrad);
        g2.fillRect(tx - 6, ty - 42, 12, 140);

        // Steel bracket bands
        g2.setColor(new Color(71, 85, 105));
        g2.fillRect(tx - 7, ty - 34, 14, 5);
        g2.fillRect(tx - 7, ty - 8, 14, 5);
        g2.fillRect(tx - 7, ty + 24, 14, 5);
    }

    private void drawForegroundTerrain(Graphics2D g2, int screenW, int screenH, double camX, double camY) {
        Path2D.Double groundPath = new Path2D.Double();
        groundPath.moveTo(0, screenH);

        int step = 5; // Ultra dense mesh for silky curved lines
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

        // -------------------------------------------------------------
        // 1. WARM GOLDEN/CLAY EARTH STRATA
        // -------------------------------------------------------------
        GradientPaint earthGrad = new GradientPaint(
            0, 350, new Color(245, 158, 11),
            0, screenH, new Color(180, 83, 9)
        );
        g2.setPaint(earthGrad);
        g2.fill(groundPath);

        // -------------------------------------------------------------
        // 2. SEDIMENTARY STRATA LINES & BURIED STONES
        // -------------------------------------------------------------
        g2.setColor(new Color(217, 119, 6, 140));
        g2.setStroke(new BasicStroke(2.0f));
        for (int i = 0; i < points - 1; i += 2) {
            double sx = xVals[i];
            double sy = yVals[i] + 35 + Math.sin(sx * 0.04) * 6;
            if (sy < screenH) {
                g2.drawLine((int) sx, (int) sy, (int) (sx + step * 2), (int) sy);
            }
        }

        // -------------------------------------------------------------
        // 3. ORGANIC TOPSOIL CRUST (Rich dark chocolate loam)
        // -------------------------------------------------------------
        g2.setColor(new Color(120, 53, 15));
        g2.setStroke(new BasicStroke(10.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D.Double crustPath = new Path2D.Double();
        crustPath.moveTo(xVals[0], yVals[0] + 4);
        for (int i = 1; i < points; i++) {
            crustPath.lineTo(xVals[i], yVals[i] + 4);
        }
        g2.draw(crustPath);

        // -------------------------------------------------------------
        // 4. CRISP LUSH GREEN GRASS EDGE
        // -------------------------------------------------------------
        g2.setColor(new Color(101, 163, 13)); // Deep meadow green
        g2.setStroke(new BasicStroke(6.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D.Double grassPath = new Path2D.Double();
        grassPath.moveTo(xVals[0], yVals[0] + 1);
        for (int i = 1; i < points; i++) {
            grassPath.lineTo(xVals[i], yVals[i] + 1);
        }
        g2.draw(grassPath);

        // Top sunlit lime green highlight
        g2.setColor(new Color(163, 230, 53));
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D.Double highlightPath = new Path2D.Double();
        highlightPath.moveTo(xVals[0], yVals[0]);
        for (int i = 1; i < points; i++) {
            highlightPath.lineTo(xVals[i], yVals[i]);
        }
        g2.draw(highlightPath);

        // -------------------------------------------------------------
        // 5. PROCEDURAL SWAYING GRASS TUFTS
        // -------------------------------------------------------------
        g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < points; i += 3) {
            double wx = camX + xVals[i];
            if (isBridgeAt(wx)) continue;

            double gx = xVals[i];
            double gy = yVals[i];
            double wind = Math.sin(wx * 0.08 + animTime * 2.0) * 3.5;

            // Two grass blade tufts
            g2.setColor(new Color(132, 204, 22));
            g2.drawLine((int) gx, (int) gy, (int) (gx + wind - 2), (int) (gy - 7));
            g2.setColor(new Color(163, 230, 53));
            g2.drawLine((int) (gx + 3), (int) gy, (int) (gx + 3 + wind + 2), (int) (gy - 9));
        }

        g2.setStroke(new BasicStroke(1));
    }

    private void drawRoadsideDetails(Graphics2D g2, int screenW, double camX, double camY) {
        // Wooden fence along pasture
        for (double wx = (int)(camX / 110) * 110; wx <= camX + screenW + 80; wx += 110) {
            if (isBridgeAt(wx)) continue;

            int sx = (int) (wx - camX);
            int sy = (int) (getHeightAt(wx) - camY);

            // Cast shadow
            g2.setColor(new Color(15, 23, 42, 45));
            g2.fillOval(sx - 4, sy + 1, 16, 6);

            // Fence post
            GradientPaint postGrad = new GradientPaint(sx - 3, sy - 22, new Color(180, 83, 9), sx + 3, sy, new Color(120, 53, 15));
            g2.setPaint(postGrad);
            g2.fillRect(sx - 3, sy - 22, 7, 24);

            // Horizontal rails
            g2.setColor(new Color(146, 64, 14));
            g2.fillRect(sx - 55, sy - 17, 110, 4);
            g2.setColor(new Color(180, 83, 9));
            g2.fillRect(sx - 55, sy - 9, 110, 4);
        }

        // Green Checkpoint Pin at 34m (matching the photo!)
        double pinWorldX = 340.0;
        int px = (int) (pinWorldX - camX);
        int py = (int) (getHeightAt(pinWorldX) - camY);

        if (px > -60 && px < screenW + 60) {
            // Shadow on ground
            g2.setColor(new Color(15, 23, 42, 60));
            g2.fillOval(px - 10, py + 2, 20, 6);

            // Pin marker body
            GradientPaint pinGrad = new GradientPaint(px - 10, py - 32, new Color(34, 197, 94), px + 10, py, new Color(21, 128, 61));
            g2.setPaint(pinGrad);
            g2.fillOval(px - 11, py - 32, 22, 22);
            g2.fillPolygon(new int[]{px - 8, px + 8, px}, new int[]{py - 20, py - 20, py - 3}, 3);

            // White reflective center dot
            g2.setColor(Color.WHITE);
            g2.fillOval(px - 4, py - 25, 8, 8);

            // Distance label
            g2.setFont(new Font("Impact", Font.BOLD, 14));
            g2.setColor(new Color(15, 23, 42));
            g2.drawString("34m", px + 14, py - 14);
            g2.setColor(new Color(240, 253, 244));
            g2.drawString("34m", px + 13, py - 15);
        }
    }
}
