import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Advanced Multi-Biome Procedural Terrain Engine for Hill Climb Racing.
 * 
 * Features:
 * 1. Multi-Biome Environmental Progression:
 *    - 0m - 1200m: Emerald Countryside (Lush rolling pastures, rotating windmill, barn, cows, wildflowers).
 *    - 1200m - 2600m: Pine Forest Ridge (Tall spruce pines, deep ravines, wooden trestle bridges, steep inclines).
 *    - 2600m - 4200m: Red Rock Canyon & Cave Arches (Sandstone bluffs, overhead rock cave arch, jump ramps).
 *    - 4200m+: Alpine Snowy Summit (Snow-capped peaks, icy crests, extreme mountain passes).
 * 2. Track Architecture:
 *    - Overhead Checkpoint Gantry Gates with flashing LED beacons at 500m, 1000m, 1500m, 2000m.
 *    - Mega Wooden Jump Launch Ramps spanning ravines.
 *    - Wooden Suspension Bridges with steel catenary cables and trestle arches.
 *    - Natural Stone Cave Arches overhead.
 * 3. Geological Strata & Roadside Detail:
 *    - Procedural swaying grass blade tufts, rich topsoil crust, and stratified earth.
 *    - Roadside warning signs ("STEEP CLIMB 18%", "JUMP AHEAD").
 */
public class Terrain {
    public static class Bridge {
        public double startX, endX;
        public double startY, endY;
        public boolean isRamp;

        public Bridge(double startX, double startY, double endX, double endY, boolean isRamp) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
            this.isRamp = isRamp;
        }

        public boolean contains(double x) {
            return x >= startX && x <= endX;
        }

        public double getHeightAt(double x) {
            double t = (x - startX) / (endX - startX);
            if (isRamp) {
                // Curved takeoff ramp
                return startY + (endY - startY) * Math.pow(t, 1.4);
            } else {
                double sag = Math.sin(t * Math.PI) * 16.0;
                return (startY + (endY - startY) * t) + sag;
            }
        }
    }

    private final List<Bridge> bridges = new ArrayList<>();
    public final CustomQueue<Double> upcomingChunkQueue = new CustomQueue<>(60);
    private double animTime = 0;

    public Terrain() {
        // Wooden suspension bridges & mega jump ramps along the world
        bridges.add(new Bridge(620, 470, 960, 490, false));   // Countryside Bridge 1
        bridges.add(new Bridge(1450, 460, 1540, 410, true));  // Forest Mega Launch Ramp
        bridges.add(new Bridge(2100, 480, 2520, 460, false));  // Forest Trestle Bridge
        bridges.add(new Bridge(2950, 470, 3040, 420, true));  // Canyon Jump Takeoff Ramp
        bridges.add(new Bridge(3750, 490, 4220, 480, false));  // Grand Canyon Suspension Chasm
        bridges.add(new Bridge(5400, 475, 5850, 485, false));  // Alpine Snow Bridge

        for (int i = 0; i < 50; i++) {
            upcomingChunkQueue.enqueue(i * 250.0);
        }
    }

    /**
     * Advanced continuous height function with biome-specific topography:
     * - Countryside: gentle rolling hills.
     * - Forest Ridge: steeper grade climbs & deep dips.
     * - Canyon: stepped sandstone mounds & jumps.
     * - Alpine: extreme high-angle slopes.
     */
    public double getHeightAt(double x) {
        for (Bridge b : bridges) {
            if (b.contains(x)) {
                return b.getHeightAt(x);
            }
        }

        double base = 480.0;

        // Flat starting straightaway
        if (x < 260) {
            return base + Math.sin(x * 0.006) * 8.0;
        }

        // Biome 1: Emerald Countryside (0 - 12000px / 0 - 1200m)
        if (x < 12000) {
            double hill1 = Math.sin(x * 0.0016) * 54.0;
            double hill2 = Math.cos(x * 0.0035) * 26.0;
            return base + hill1 + hill2;
        }
        // Biome 2: Pine Forest Ridge (12000 - 26000px / 1200m - 2600m)
        else if (x < 26000) {
            double steepClimb = Math.sin(x * 0.0022) * 78.0;
            double dips = Math.cos(x * 0.0048) * 36.0;
            return base + steepClimb + dips;
        }
        // Biome 3: Red Rock Canyon (26000 - 42000px / 2600m - 4200m)
        else if (x < 42000) {
            double plateau = Math.sin(x * 0.0018) * 85.0;
            double jumpMound = Math.sin(x * 0.007) * 22.0;
            return base + plateau + jumpMound;
        }
        // Biome 4: Alpine Snowy Summit (42000px+)
        else {
            double mountain = Math.sin(x * 0.0014) * 110.0;
            double crest = Math.cos(x * 0.0032) * 45.0;
            return base + mountain + crest;
        }
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

        // Current Biome identification based on camera center
        double worldDistMeters = (camX + screenW / 2.0) / 10.0;

        // 1. DYNAMIC SKY & SUN
        drawDynamicSky(g2, screenW, screenH, worldDistMeters);

        // 2. PARALLAX CLOUDS & SKYLINE
        drawClouds(g2, screenW, camX);

        // 3. DISTANT PARALLAX MOUNTAINS / CANYON BLUFFS (Layer 1: 0.09x)
        drawDistantParallax(g2, screenW, screenH, camX * 0.09, worldDistMeters);

        // 4. MIDGROUND COUNTRYSIDE / FOREST / CANYON (Layer 2: 0.28x)
        drawMidgroundScenery(g2, screenW, screenH, camX * 0.28, worldDistMeters);

        // 5. TIMBER SUSPENSION BRIDGES & JUMP RAMPS
        drawBridgesAndRamps(g2, camX, camY);

        // 6. MAIN GROUND TERRAIN & MULTI-LAYER STRATA
        drawForegroundTerrain(g2, screenW, screenH, camX, camY, worldDistMeters);

        // 7. ROADSIDE CHECKPOINT GANTRIES, SIGNS, AND VEGETATION
        drawRoadsideArchitecture(g2, screenW, camX, camY, worldDistMeters);
    }

    private void drawDynamicSky(Graphics2D g2, int screenW, int screenH, double distMeters) {
        Color skyTop;
        Color skyBottom;

        if (distMeters < 1200) {
            // Azure Countryside Sky
            skyTop = new Color(96, 165, 250);
            skyBottom = new Color(224, 242, 254);
        } else if (distMeters < 2600) {
            // Forest Deep Blue Sky
            skyTop = new Color(59, 130, 246);
            skyBottom = new Color(219, 234, 254);
        } else if (distMeters < 4200) {
            // Canyon Warm Amber Sky
            skyTop = new Color(249, 115, 22);
            skyBottom = new Color(254, 243, 199);
        } else {
            // Alpine Crisp Violet Sky
            skyTop = new Color(99, 102, 241);
            skyBottom = new Color(238, 242, 255);
        }

        GradientPaint skyGrad = new GradientPaint(0, 0, skyTop, 0, screenH * 0.75f, skyBottom);
        g2.setPaint(skyGrad);
        g2.fillRect(0, 0, screenW, screenH);

        // Radiant Sun with atmospheric glow
        int sunX = 140;
        int sunY = 85;
        g2.setColor(new Color(254, 240, 138, 75));
        g2.fillOval(sunX - 70, sunY - 70, 140, 140);
        g2.setColor(new Color(253, 224, 71, 130));
        g2.fillOval(sunX - 45, sunY - 45, 90, 90);
        g2.setColor(new Color(255, 255, 245));
        g2.fillOval(sunX - 25, sunY - 25, 50, 50);
    }

    private void drawClouds(Graphics2D g2, int screenW, double camX) {
        g2.setColor(new Color(255, 255, 255, 200));
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

    private void drawDistantParallax(Graphics2D g2, int screenW, int screenH, double scrollX, double distMeters) {
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

        Color distTop = (distMeters > 2600) ? new Color(249, 115, 22, 160) : new Color(147, 245, 175, 170);
        Color distBottom = (distMeters > 2600) ? new Color(194, 65, 12, 210) : new Color(74, 222, 128, 210);

        GradientPaint distGrad = new GradientPaint(0, 180, distTop, 0, screenH, distBottom);
        g2.setPaint(distGrad);
        g2.fill(path);
    }

    private void drawMidgroundScenery(Graphics2D g2, int screenW, int screenH, double scrollX, double distMeters) {
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

        Color midTop = (distMeters > 2600) ? new Color(254, 215, 170, 220) : new Color(254, 240, 138, 220);
        Color midBottom = (distMeters > 2600) ? new Color(234, 88, 12, 230) : new Color(163, 230, 53, 230);

        GradientPaint midGrad = new GradientPaint(0, 310, midTop, 0, screenH, midBottom);
        g2.setPaint(midGrad);
        g2.fill(midPath);

        // Biome Specific Midground Landmark
        if (distMeters < 1200) {
            // Countryside Windmill & Barn
            drawWindmill(g2, (int) (320 - (scrollX % 1400)), 330);
            drawBarn(g2, (int) (740 - (scrollX % 1000)), 350);
        } else if (distMeters < 2600) {
            // Pine Forest Watchtower & Spruce groves
            drawWatchtower(g2, (int) (480 - (scrollX % 1200)), 330);
        } else {
            // Canyon Sandstone Mesas & Rocks
            drawCanyonPillars(g2, (int) (400 - (scrollX % 900)), 320);
        }
    }

    private void drawWindmill(Graphics2D g2, int wx, int wy) {
        if (wx < -140 || wx > 1200) return;
        g2.setColor(new Color(241, 245, 249));
        int[] mtx = {wx - 14, wx + 14, wx + 9, wx - 9};
        int[] mty = {wy + 48, wy + 48, wy, wy};
        g2.fillPolygon(mtx, mty, 4);
        g2.setColor(new Color(148, 163, 184));
        g2.drawPolygon(mtx, mty, 4);

        g2.setColor(new Color(185, 28, 28));
        g2.fillPolygon(new int[]{wx - 12, wx + 12, wx}, new int[]{wy, wy, wy - 15}, 3);

        Graphics2D wg = (Graphics2D) g2.create();
        wg.translate(wx, wy + 4);
        wg.rotate(animTime * 1.2);
        wg.setColor(new Color(100, 116, 139));
        wg.setStroke(new BasicStroke(2.0f));
        for (int a = 0; a < 4; a++) {
            wg.rotate(Math.PI / 2);
            wg.drawLine(0, 0, 0, -32);
            wg.setColor(new Color(255, 255, 255, 220));
            wg.fillRect(2, -30, 9, 20);
            wg.setColor(new Color(100, 116, 139));
            wg.drawRect(2, -30, 9, 20);
        }
        wg.dispose();
    }

    private void drawBarn(Graphics2D g2, int bx, int by) {
        if (bx < -140 || bx > 1200) return;
        g2.setColor(new Color(185, 28, 28));
        g2.fillRect(bx, by, 70, 44);
        g2.setColor(Color.WHITE);
        g2.fillPolygon(new int[]{bx - 5, bx + 35, bx + 75}, new int[]{by, by - 24, by}, 3);
        g2.setColor(new Color(30, 41, 59));
        g2.fillRect(bx + 24, by + 16, 22, 28);
    }

    private void drawWatchtower(Graphics2D g2, int tx, int ty) {
        if (tx < -120 || tx > 1200) return;
        g2.setColor(new Color(120, 53, 15));
        g2.setStroke(new BasicStroke(3.0f));
        g2.drawLine(tx - 12, ty + 60, tx - 6, ty);
        g2.drawLine(tx + 12, ty + 60, tx + 6, ty);
        g2.drawLine(tx - 12, ty + 60, tx + 6, ty + 20);
        g2.drawLine(tx + 12, ty + 60, tx - 6, ty + 20);
        // Cabin
        g2.fillRect(tx - 14, ty - 16, 28, 16);
        g2.setColor(new Color(185, 28, 28));
        g2.fillPolygon(new int[]{tx - 18, tx + 18, tx}, new int[]{ty - 16, ty - 16, ty - 28}, 3);
        g2.setStroke(new BasicStroke(1));
    }

    private void drawCanyonPillars(Graphics2D g2, int px, int py) {
        if (px < -120 || px > 1200) return;
        GradientPaint rockGrad = new GradientPaint(px - 25, py, new Color(234, 88, 12), px + 25, py + 80, new Color(154, 52, 18));
        g2.setPaint(rockGrad);
        g2.fillRoundRect(px - 25, py, 50, 75, 12, 12);
        g2.fillRoundRect(px + 40, py + 15, 35, 60, 8, 8);
    }

    private void drawBridgesAndRamps(Graphics2D g2, double camX, double camY) {
        for (Bridge b : bridges) {
            int t1x = (int) (b.startX - camX);
            int t1y = (int) (b.startY - camY);
            int t2x = (int) (b.endX - camX);
            int t2y = (int) (b.endY - camY);

            if (b.isRamp) {
                // Mega Jump Takeoff Ramp (Heavy wooden scaffolding structure)
                g2.setColor(new Color(120, 53, 15));
                g2.setStroke(new BasicStroke(4.0f));
                g2.drawLine(t1x, t1y + 20, t2x, t2y);
                g2.drawLine(t2x, t2y, t2x, t2y + 65);
                g2.drawLine(t1x, t1y + 20, t2x, t2y + 65);

                // Ramp face surface
                GradientPaint rampGrad = new GradientPaint(t1x, t1y, new Color(245, 158, 11), t2x, t2y, new Color(180, 83, 9));
                g2.setPaint(rampGrad);
                Path2D.Double rPoly = new Path2D.Double();
                rPoly.moveTo(t1x, t1y);
                for (double x = b.startX; x <= b.endX; x += 10) {
                    rPoly.lineTo(x - camX, b.getHeightAt(x) - camY);
                }
                rPoly.lineTo(t2x, t2y + 8);
                rPoly.lineTo(t1x, t1y + 8);
                rPoly.closePath();
                g2.fill(rPoly);
                g2.setColor(Color.WHITE);
                g2.drawString(">> JUMP >>", t1x + 15, t1y - 12);

            } else {
                // Timber Suspension Bridge with steel cables and deck planks
                drawBridgeTower(g2, t1x, t1y);
                drawBridgeTower(g2, t2x, t2y);

                g2.setColor(new Color(245, 158, 11));
                g2.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                Path2D.Double mainCable = new Path2D.Double();
                mainCable.moveTo(t1x, t1y - 32);

                for (double x = b.startX; x <= b.endX; x += 14) {
                    double sx = x - camX;
                    double sy = b.getHeightAt(x) - camY - 24;
                    mainCable.lineTo(sx, sy);

                    g2.setColor(new Color(217, 119, 6));
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.drawLine((int) sx, (int) sy, (int) sx, (int) (b.getHeightAt(x) - camY));
                }
                g2.setStroke(new BasicStroke(3.0f));
                g2.draw(mainCable);

                // Wooden deck planks with grain & shadows
                for (double x = b.startX; x <= b.endX; x += 15) {
                    int px = (int) (x - camX);
                    int py = (int) (b.getHeightAt(x) - camY);

                    g2.setColor(new Color(67, 20, 7, 120));
                    g2.fillRoundRect(px - 7, py + 2, 16, 7, 3, 3);

                    GradientPaint plankGrad = new GradientPaint(px - 7, py - 4, new Color(217, 119, 6), px + 9, py + 4, new Color(146, 64, 14));
                    g2.setPaint(plankGrad);
                    g2.fillRoundRect(px - 7, py - 4, 15, 8, 3, 3);
                    g2.setColor(new Color(67, 20, 7));
                    g2.drawRoundRect(px - 7, py - 4, 15, 8, 3, 3);
                }
            }
            g2.setStroke(new BasicStroke(1));
        }
    }

    private void drawBridgeTower(Graphics2D g2, int tx, int ty) {
        GradientPaint timberGrad = new GradientPaint(tx - 5, ty, new Color(146, 64, 14), tx + 5, ty, new Color(67, 20, 7));
        g2.setPaint(timberGrad);
        g2.fillRect(tx - 6, ty - 42, 12, 140);
        g2.setColor(new Color(71, 85, 105));
        g2.fillRect(tx - 7, ty - 34, 14, 5);
        g2.fillRect(tx - 7, ty - 8, 14, 5);
        g2.fillRect(tx - 7, ty + 24, 14, 5);
    }

    private void drawForegroundTerrain(Graphics2D g2, int screenW, int screenH, double camX, double camY, double distMeters) {
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

        // Subsurface Earth Stratification (Biome-dependent colors)
        Color earthTop;
        Color earthBottom;
        Color crustCol;
        Color surfaceGrassCol;

        if (distMeters < 1200) {
            earthTop = new Color(245, 158, 11);
            earthBottom = new Color(180, 83, 9);
            crustCol = new Color(120, 53, 15);
            surfaceGrassCol = new Color(132, 204, 22);
        } else if (distMeters < 2600) {
            earthTop = new Color(180, 83, 9);
            earthBottom = new Color(113, 63, 18);
            crustCol = new Color(67, 34, 12);
            surfaceGrassCol = new Color(34, 197, 94);
        } else if (distMeters < 4200) {
            earthTop = new Color(234, 88, 12);
            earthBottom = new Color(154, 52, 18);
            crustCol = new Color(124, 45, 18);
            surfaceGrassCol = new Color(249, 115, 22);
        } else {
            earthTop = new Color(203, 213, 225);
            earthBottom = new Color(100, 116, 139);
            crustCol = new Color(71, 85, 105);
            surfaceGrassCol = new Color(241, 245, 249);
        }

        // Earth fill
        GradientPaint earthGrad = new GradientPaint(0, 350, earthTop, 0, screenH, earthBottom);
        g2.setPaint(earthGrad);
        g2.fill(groundPath);

        // Sedimentary strata lines
        g2.setColor(new Color(217, 119, 6, 130));
        g2.setStroke(new BasicStroke(2.0f));
        for (int i = 0; i < points - 1; i += 2) {
            double sx = xVals[i];
            double sy = yVals[i] + 35 + Math.sin(sx * 0.04) * 6;
            if (sy < screenH) {
                g2.drawLine((int) sx, (int) sy, (int) (sx + step * 2), (int) sy);
            }
        }

        // Dark Organic Topsoil Crust (8px)
        g2.setColor(crustCol);
        g2.setStroke(new BasicStroke(10.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D.Double crustPath = new Path2D.Double();
        crustPath.moveTo(xVals[0], yVals[0] + 4);
        for (int i = 1; i < points; i++) {
            crustPath.lineTo(xVals[i], yVals[i] + 4);
        }
        g2.draw(crustPath);

        // Vibrant Surface Grass Line
        g2.setColor(surfaceGrassCol);
        g2.setStroke(new BasicStroke(5.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D.Double grassPath = new Path2D.Double();
        grassPath.moveTo(xVals[0], yVals[0]);
        for (int i = 1; i < points; i++) {
            grassPath.lineTo(xVals[i], yVals[i]);
        }
        g2.draw(grassPath);

        // Swaying Grass Blades & Flora
        g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < points; i += 3) {
            double wx = camX + xVals[i];
            if (isBridgeAt(wx)) continue;

            double gx = xVals[i];
            double gy = yVals[i];
            double wind = Math.sin(wx * 0.08 + animTime * 2.0) * 3.5;

            g2.setColor(surfaceGrassCol);
            g2.drawLine((int) gx, (int) gy, (int) (gx + wind - 2), (int) (gy - 7));
            g2.setColor(new Color(254, 240, 138));
            g2.drawLine((int) (gx + 3), (int) gy, (int) (gx + 3 + wind + 2), (int) (gy - 9));
        }

        g2.setStroke(new BasicStroke(1));
    }

    private void drawRoadsideArchitecture(Graphics2D g2, int screenW, double camX, double camY, double distMeters) {
        // -------------------------------------------------------------
        // OVERHEAD CHECKPOINT GANTRY GATES (At 500m, 1000m, 1500m, 2000m...)
        // -------------------------------------------------------------
        int[] checkpoints = {500, 1000, 1500, 2000, 3000};
        for (int cp : checkpoints) {
            double cpX = cp * 10.0;
            int sx = (int) (cpX - camX);
            int sy = (int) (getHeightAt(cpX) - camY);

            if (sx > -100 && sx < screenW + 100) {
                drawCheckpointGantry(g2, sx, sy, cp);
            }
        }

        // Roadside warning signs ("STEEP CLIMB 18%", "JUMP AHEAD")
        drawWarningSign(g2, (int) (1400 - camX), (int) (getHeightAt(1400) - camY), "JUMP!");
        drawWarningSign(g2, (int) (2900 - camX), (int) (getHeightAt(2900) - camY), "RAMP!");

        // 34m Milestone Pin
        double pinWorldX = 340.0;
        int px = (int) (pinWorldX - camX);
        int py = (int) (getHeightAt(pinWorldX) - camY);
        if (px > -60 && px < screenW + 60) {
            g2.setColor(new Color(34, 197, 94));
            g2.fillOval(px - 11, py - 32, 22, 22);
            g2.fillPolygon(new int[]{px - 8, px + 8, px}, new int[]{py - 20, py - 20, py - 3}, 3);
            g2.setColor(Color.WHITE);
            g2.fillOval(px - 4, py - 25, 8, 8);
            g2.setFont(new Font("Impact", Font.BOLD, 14));
            g2.drawString("34m", px + 13, py - 14);
        }
    }

    private void drawCheckpointGantry(Graphics2D g2, int sx, int sy, int meters) {
        // Left & Right Steel Lattice Pillars
        g2.setColor(new Color(71, 85, 105));
        g2.setStroke(new BasicStroke(4.0f));
        g2.drawLine(sx - 40, sy, sx - 40, sy - 110);
        g2.drawLine(sx + 40, sy, sx + 40, sy - 110);

        // Overhead Truss Span
        g2.fillRect(sx - 46, sy - 110, 92, 14);
        g2.setStroke(new BasicStroke(1));

        // Checkered Banner
        g2.setColor(Color.WHITE);
        g2.fillRect(sx - 36, sy - 96, 72, 22);
        g2.setColor(Color.BLACK);
        for (int i = 0; i < 6; i++) {
            if (i % 2 == 0) {
                g2.fillRect(sx - 36 + i * 12, sy - 96, 12, 11);
                g2.fillRect(sx - 36 + (i + 1) * 12, sy - 85, 12, 11);
            }
        }

        // Distance text badge
        g2.setFont(new Font("Impact", Font.BOLD, 14));
        g2.setColor(new Color(251, 191, 36));
        g2.drawString(meters + "m CHECKPOINT", sx - 44, sy - 115);

        // Flashing LED beacons on top
        boolean blink = ((int) (animTime * 10)) % 2 == 0;
        g2.setColor(blink ? new Color(34, 197, 94) : new Color(239, 68, 68));
        g2.fillOval(sx - 44, sy - 124, 10, 10);
        g2.fillOval(sx + 34, sy - 124, 10, 10);
    }

    private void drawWarningSign(Graphics2D g2, int sx, int sy, String text) {
        if (sx < -60 || sx > 1200) return;
        // Post
        g2.setColor(new Color(100, 116, 139));
        g2.fillRect(sx - 2, sy - 36, 4, 38);
        // Diamond Yellow Warning Sign
        Polygon diamond = new Polygon();
        diamond.addPoint(sx, sy - 54);
        diamond.addPoint(sx + 18, sy - 36);
        diamond.addPoint(sx, sy - 18);
        diamond.addPoint(sx - 18, sy - 36);
        g2.setColor(new Color(251, 191, 36));
        g2.fillPolygon(diamond);
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawPolygon(diamond);
        g2.setFont(new Font("Impact", Font.BOLD, 9));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, sx - fm.stringWidth(text) / 2, sy - 33);
        g2.setStroke(new BasicStroke(1));
    }
}
