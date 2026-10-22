import java.awt.*;

/**
 * Realistic Collectibles for Hill Climb Racing.
 * 
 * Features:
 * - 3D Gyroscopic Spinning Gold Coins with perspective width oscillation, milled bevel rim, and gleam.
 * - Stamped Heavy Steel Jerrycan with cross-embossed panels, metallic spout, and warning decal.
 * - Multi-faceted crystalline Bonus Star with vibrant chromatic glow.
 */
public class Collectible {
    public enum Type {
        COIN_5,
        COIN_25,
        FUEL,
        BONUS_STAR
    }

    private double x, y;
    private Type type;
    private int value;
    private boolean collected;
    private double animPhase;

    public Collectible(double x, double y, Type type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.collected = false;
        this.animPhase = Math.random() * Math.PI * 2;

        switch (type) {
            case COIN_5: this.value = 5; break;
            case COIN_25: this.value = 25; break;
            case BONUS_STAR: this.value = 100; break;
            case FUEL: this.value = 0; break;
        }
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public Type getType() { return type; }
    public int getValue() { return value; }
    public boolean isCollected() { return collected; }
    public void setCollected(boolean c) { this.collected = c; }

    public void update() {
        animPhase += 0.07;
    }

    public double getVisualY() {
        return y + Math.sin(animPhase) * 5.5;
    }

    public void render(Graphics2D g2, double camX, double camY) {
        if (collected) return;

        int sx = (int) (x - camX);
        int sy = (int) (getVisualY() - camY);

        if (type == Type.COIN_5 || type == Type.COIN_25) {
            render3DCoin(g2, sx, sy);
        } else if (type == Type.FUEL) {
            renderRealisticJerrycan(g2, sx, sy);
        } else if (type == Type.BONUS_STAR) {
            renderFacetedStar(g2, sx, sy);
        }
    }

    private void render3DCoin(Graphics2D g2, int sx, int sy) {
        int r = (type == Type.COIN_25) ? 17 : 14;

        // 3D spin rotation projection: scale width by cos(angle)
        double spinScale = Math.cos(animPhase * 1.6);
        double absScale = Math.max(0.18, Math.abs(spinScale));
        int coinW = (int) (r * 2 * absScale);
        int coinH = r * 2;

        // Outer warm bloom glow
        g2.setColor(new Color(254, 240, 138, 90));
        g2.fillOval(sx - r - 4, sy - r - 4, (r + 4) * 2, (r + 4) * 2);

        // Edge thickness (simulating 3D cylindrical coin edge)
        int edgeOffset = (spinScale >= 0) ? 3 : -3;
        g2.setColor(new Color(180, 83, 9));
        g2.fillOval(sx - coinW / 2 + edgeOffset, sy - coinH / 2, coinW, coinH);

        // Main coin face (Gold gradient)
        GradientPaint goldGrad = new GradientPaint(
            sx - coinW / 2, sy - coinH / 2, new Color(253, 224, 71),
            sx + coinW / 2, sy + coinH / 2, new Color(217, 119, 6)
        );
        g2.setPaint(goldGrad);
        g2.fillOval(sx - coinW / 2, sy - coinH / 2, coinW, coinH);

        // Dark golden rim ring
        g2.setColor(new Color(146, 64, 14));
        g2.setStroke(new BasicStroke(1.4f));
        g2.drawOval(sx - coinW / 2, sy - coinH / 2, coinW, coinH);

        // Inner face recess
        if (coinW > 12) {
            int innerW = coinW - 6;
            int innerH = coinH - 6;
            GradientPaint innerGrad = new GradientPaint(
                sx, sy - innerH / 2, new Color(254, 240, 138),
                sx, sy + innerH / 2, new Color(245, 158, 11)
            );
            g2.setPaint(innerGrad);
            g2.fillOval(sx - innerW / 2, sy - innerH / 2, innerW, innerH);

            // Embossed denomination number
            if (absScale > 0.45) {
                g2.setFont(new Font("Impact", Font.BOLD, (type == Type.COIN_25) ? 13 : 11));
                g2.setColor(new Color(120, 53, 15));
                String valStr = String.valueOf(value);
                FontMetrics fm = g2.getFontMetrics();
                int tx = sx - fm.stringWidth(valStr) / 2;
                int ty = sy + fm.getAscent() / 2 - 2;
                g2.drawString(valStr, tx, ty);
            }
        }

        // Specular sheen sweep across the face
        g2.setColor(new Color(255, 255, 255, 160));
        g2.fillOval(sx - coinW / 3, sy - coinH / 3, Math.max(3, coinW / 3), coinH / 4);

        g2.setStroke(new BasicStroke(1));
    }

    private void renderRealisticJerrycan(Graphics2D g2, int sx, int sy) {
        int w = 26;
        int h = 34;
        int x0 = sx - w / 2;
        int y0 = sy - h / 2;

        // Cast shadow beneath canister
        g2.setColor(new Color(15, 23, 42, 70));
        g2.fillOval(sx - 14, sy + h / 2 + 1, 28, 8);

        // Jerrycan Canister Red Body (Glossy finish)
        GradientPaint redGrad = new GradientPaint(x0, y0, new Color(239, 68, 68), x0 + w, y0 + h, new Color(185, 28, 28));
        g2.setPaint(redGrad);
        g2.fillRoundRect(x0, y0, w, h, 8, 8);

        // Dark edge border
        g2.setColor(new Color(127, 29, 29));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(x0, y0, w, h, 8, 8);

        // Stamped cross indentation on jerrycan panel (classic military fuel can)
        g2.setColor(new Color(153, 27, 27));
        g2.setStroke(new BasicStroke(2.0f));
        g2.drawLine(x0 + 6, y0 + 10, x0 + w - 6, y0 + h - 8);
        g2.drawLine(x0 + w - 6, y0 + 10, x0 + 6, y0 + h - 8);

        // Top carry handles
        g2.setColor(new Color(185, 28, 28));
        g2.fillRoundRect(sx - 9, y0 - 5, 18, 6, 2, 2);
        g2.setColor(new Color(127, 29, 29));
        g2.drawRoundRect(sx - 9, y0 - 5, 18, 6, 2, 2);

        // Metal screw cap & pour spout
        g2.setColor(new Color(203, 213, 225));
        g2.fillRect(sx + 3, y0 - 7, 7, 5);
        g2.setColor(new Color(71, 85, 105));
        g2.drawRect(sx + 3, y0 - 7, 7, 5);

        // White "GAS" badge with bold font
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Impact", Font.BOLD, 10));
        FontMetrics fm = g2.getFontMetrics();
        String lbl = "GAS";
        g2.drawString(lbl, sx - fm.stringWidth(lbl) / 2, sy + 4);

        g2.setStroke(new BasicStroke(1));
    }

    private void renderFacetedStar(Graphics2D g2, int sx, int sy) {
        int r = 21;

        // Radiant pulsating pink aura
        float pulse = (float)(0.6 + 0.4 * Math.sin(animPhase * 2.5));
        g2.setColor(new Color(244, 114, 182, (int)(90 * pulse)));
        g2.fillOval(sx - r - 6, sy - r - 6, (r + 6) * 2, (r + 6) * 2);

        // Faceted crystal star
        drawFacetedStarShape(g2, sx, sy, r, r / 2, 5);
    }

    private void drawFacetedStarShape(Graphics2D g2, int cx, int cy, int outerR, int innerR, int points) {
        double angleStep = Math.PI / points;
        double currentAngle = -Math.PI / 2;

        int[] xPts = new int[points * 2];
        int[] yPts = new int[points * 2];
        for (int i = 0; i < points * 2; i++) {
            int r = (i % 2 == 0) ? outerR : innerR;
            xPts[i] = cx + (int) (Math.cos(currentAngle) * r);
            yPts[i] = cy + (int) (Math.sin(currentAngle) * r);
            currentAngle += angleStep;
        }

        // Shaded fill
        GradientPaint pinkGrad = new GradientPaint(cx - outerR, cy - outerR, new Color(244, 114, 182), cx + outerR, cy + outerR, new Color(219, 39, 119));
        g2.setPaint(pinkGrad);
        g2.fillPolygon(xPts, yPts, points * 2);

        // Facet lines from center to each point
        g2.setColor(new Color(255, 255, 255, 170));
        g2.setStroke(new BasicStroke(1.2f));
        for (int i = 0; i < points * 2; i += 2) {
            g2.drawLine(cx, cy, xPts[i], yPts[i]);
        }

        // Crisp white border
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(1.8f));
        g2.drawPolygon(xPts, yPts, points * 2);
        g2.setStroke(new BasicStroke(1));
    }
}
