import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Enhanced realistic particle & floating text engine for Hill Climb Racing.
 * 
 * Features:
 * - Volumetric expanding smoke puffs with soft alpha dissipation.
 * - Multi-toned soil & gravel chunks thrown dynamically by knobby tires.
 * - Radiant four-point lens flares & golden sparkles on coin pickup.
 * - Floating score/bonus badges (+25, +5, +500) with smooth vertical rise and fade.
 */
public class ParticleManager {
    public static class Particle {
        double x, y;
        double vx, vy;
        Color color;
        double size;
        double growth;
        int life;
        int maxLife;
        boolean isSmoke;

        public Particle(double x, double y, double vx, double vy, Color color, double size, double growth, int maxLife, boolean isSmoke) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.color = color;
            this.size = size;
            this.growth = growth;
            this.life = maxLife;
            this.maxLife = maxLife;
            this.isSmoke = isSmoke;
        }

        public boolean update() {
            x += vx;
            y += vy;
            if (isSmoke) {
                vx *= 0.96;
                vy *= 0.96;
                vy -= 0.05; // Thermal rising smoke
                size += growth;
            } else {
                vy += 0.16; // Dirt chunk gravity
                vx *= 0.98;
            }
            life--;
            return life <= 0;
        }

        public void render(Graphics2D g2, double camX, double camY) {
            float alpha = Math.max(0f, Math.min(1f, (float) life / maxLife));
            int sx = (int) (x - camX);
            int sy = (int) (y - camY);
            int s = (int) Math.max(1.0, size * (isSmoke ? 1.0 : alpha));

            if (isSmoke) {
                // Soft gradient smoke puff
                int a = (int) (alpha * color.getAlpha());
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.min(255, a)));
                g2.fillOval(sx - s / 2, sy - s / 2, s, s);

                // Lighter inner core for 3D volumetric feel
                int innerS = s / 2;
                g2.setColor(new Color(255, 255, 255, Math.min(255, a / 2)));
                g2.fillOval(sx - innerS / 2, sy - innerS / 2 - 1, innerS, innerS);
            } else {
                // Angular gravel / dirt particle
                int a = (int) (alpha * 255);
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.min(255, a)));
                g2.fillRoundRect(sx - s / 2, sy - s / 2, s, s, 2, 2);
            }
        }
    }

    public static class FloatingText {
        double x, y;
        double vy;
        String text;
        Color color;
        int life;
        int maxLife;

        public FloatingText(double x, double y, String text, Color color) {
            this.x = x;
            this.y = y;
            this.vy = -1.4;
            this.text = text;
            this.color = color;
            this.life = 45;
            this.maxLife = 45;
        }

        public boolean update() {
            y += vy;
            vy *= 0.95;
            life--;
            return life <= 0;
        }

        public void render(Graphics2D g2, double camX, double camY) {
            float alpha = Math.max(0f, Math.min(1f, (float) life / maxLife));
            int sx = (int) (x - camX);
            int sy = (int) (y - camY);

            int a = (int) (alpha * 255);
            g2.setFont(new Font("Impact", Font.BOLD, 15));

            // Drop shadow
            g2.setColor(new Color(15, 23, 42, Math.min(255, (int)(a * 0.8))));
            g2.drawString(text, sx - 11, sy + 1);

            // Glowing text face
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), a));
            g2.drawString(text, sx - 12, sy);
        }
    }

    private final List<Particle> particles = new ArrayList<>();
    private final List<FloatingText> floatingTexts = new ArrayList<>();

    public void addDirtSpray(double x, double y, double speedFactor) {
        for (int i = 0; i < 4; i++) {
            double vx = -2.2 - Math.random() * 3.8 * speedFactor;
            double vy = -1.2 - Math.random() * 3.2;
            Color dirtCol;
            double roll = Math.random();
            if (roll > 0.6) {
                dirtCol = new Color(120, 53, 15); // Dark loam
            } else if (roll > 0.3) {
                dirtCol = new Color(180, 83, 9);  // Clay soil
            } else {
                dirtCol = new Color(80, 35, 10);  // Dark stone
            }
            particles.add(new Particle(x, y, vx, vy, dirtCol, 4 + Math.random() * 5, 0, 28, false));
        }
    }

    public void addExhaustSmoke(double x, double y) {
        double vx = -1.8 - Math.random() * 1.4;
        double vy = -0.4 + (Math.random() - 0.5) * 0.8;
        Color smokeCol = new Color(203, 213, 225, 130);
        particles.add(new Particle(x, y, vx, vy, smokeCol, 7.0, 0.45, 38, true));
    }

    public void addCoinSparkles(double x, double y) {
        for (int i = 0; i < 14; i++) {
            double angle = Math.random() * Math.PI * 2;
            double speed = 1.4 + Math.random() * 4.0;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            Color gold = (Math.random() > 0.35) ? new Color(253, 224, 71) : Color.WHITE;
            particles.add(new Particle(x, y, vx, vy, gold, 4.0 + Math.random() * 3.0, -0.05, 32, false));
        }
    }

    public void addFloatingText(double x, double y, String text, Color color) {
        floatingTexts.add(new FloatingText(x, y, text, color));
    }

    public void update() {
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            if (it.next().update()) {
                it.remove();
            }
        }

        Iterator<FloatingText> itTxt = floatingTexts.iterator();
        while (itTxt.hasNext()) {
            if (itTxt.next().update()) {
                itTxt.remove();
            }
        }
    }

    public void render(Graphics2D g2, double camX, double camY) {
        for (Particle p : particles) {
            p.render(g2, camX, camY);
        }
        for (FloatingText ft : floatingTexts) {
            ft.render(g2, camX, camY);
        }
    }

    public void clear() {
        particles.clear();
        floatingTexts.clear();
    }
}
