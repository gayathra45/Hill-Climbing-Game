import java.awt.*;

/**
 * Collectible items scattered across the Hill Climb terrain:
 * - Coins (Value 5, 25, 100)
 * - Fuel Canisters (Restores tank)
 * - Bonus Stars
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
    private double bobPhase;

    public Collectible(double x, double y, Type type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.collected = false;
        this.bobPhase = Math.random() * Math.PI * 2;

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
        bobPhase += 0.08;
    }

    public double getVisualY() {
        return y + Math.sin(bobPhase) * 6;
    }

    public void render(Graphics2D g2, double camX, double camY) {
        if (collected) return;

        int sx = (int) (x - camX);
        int sy = (int) (getVisualY() - camY);

        if (type == Type.COIN_5 || type == Type.COIN_25) {
            // Gold Coin
            int r = (type == Type.COIN_25) ? 18 : 15;
            
            // Outer golden glow
            g2.setColor(new Color(253, 224, 71, 100));
            g2.fillOval(sx - r - 4, sy - r - 4, (r + 4) * 2, (r + 4) * 2);

            // Metallic rim
            GradientPaint rimGrad = new GradientPaint(sx - r, sy - r, new Color(245, 158, 11), sx + r, sy + r, new Color(180, 83, 9));
            g2.setPaint(rimGrad);
            g2.fillOval(sx - r, sy - r, r * 2, r * 2);

            // Coin inner face
            GradientPaint faceGrad = new GradientPaint(sx - r + 3, sy - r + 3, new Color(254, 240, 138), sx + r - 3, sy + r - 3, new Color(234, 179, 8));
            g2.setPaint(faceGrad);
            g2.fillOval(sx - r + 3, sy - r + 3, (r - 3) * 2, (r - 3) * 2);

            // Dark embossed text
            g2.setColor(new Color(146, 64, 14));
            g2.setFont(new Font("Impact", Font.BOLD, (type == Type.COIN_25) ? 14 : 12));
            String valStr = String.valueOf(value);
            FontMetrics fm = g2.getFontMetrics();
            int tx = sx - fm.stringWidth(valStr) / 2;
            int ty = sy + fm.getAscent() / 2 - 2;
            g2.drawString(valStr, tx, ty);

            // Shine specular highlight
            g2.setColor(new Color(255, 255, 255, 180));
            g2.fillOval(sx - r / 2, sy - r / 2, r / 2, r / 3);

        } else if (type == Type.FUEL) {
            // Red Fuel Canister
            int w = 24;
            int h = 30;
            g2.setColor(new Color(220, 38, 38)); // Crimson can
            g2.fillRoundRect(sx - w / 2, sy - h / 2, w, h, 6, 6);
            g2.setColor(Color.WHITE);
            g2.drawRoundRect(sx - w / 2, sy - h / 2, w, h, 6, 6);

            // Fuel spout / cap
            g2.setColor(new Color(30, 41, 59));
            g2.fillRect(sx - 4, sy - h / 2 - 6, 8, 6);

            // White "GAS" label
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
            g2.drawString("GAS", sx - 9, sy + 4);

        } else if (type == Type.BONUS_STAR) {
            // Star
            int r = 20;
            g2.setColor(new Color(236, 72, 153)); // Pink star outline (like the photo!)
            g2.setStroke(new BasicStroke(3.0f));
            drawStar(g2, sx, sy, r, r / 2, 5);
            g2.setStroke(new BasicStroke(1));
        }
    }

    private void drawStar(Graphics2D g2, int cx, int cy, int outerR, int innerR, int points) {
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
}
