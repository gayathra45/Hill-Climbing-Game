import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * High-performance 2D particle engine for tire dirt spray, exhaust smoke, and coin sparkles.
 */
public class ParticleManager {
    public static class Particle {
        double x, y;
        double vx, vy;
        Color color;
        double size;
        int life;
        int maxLife;

        public Particle(double x, double y, double vx, double vy, Color color, double size, int maxLife) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.color = color;
            this.size = size;
            this.life = maxLife;
            this.maxLife = maxLife;
        }

        public boolean update() {
            x += vx;
            y += vy;
            vy += 0.08; // Gravity on dirt
            life--;
            return life <= 0;
        }

        public void render(Graphics2D g2, double camX, double camY) {
            float alpha = Math.max(0f, Math.min(1f, (float) life / maxLife));
            Color c = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (alpha * 255));
            g2.setColor(c);
            int sx = (int) (x - camX);
            int sy = (int) (y - camY);
            int s = (int) Math.max(1, size * alpha);
            g2.fillOval(sx - s / 2, sy - s / 2, s, s);
        }
    }

    private final List<Particle> particles = new ArrayList<>();

    public void addDirtSpray(double x, double y, double speedFactor) {
        for (int i = 0; i < 3; i++) {
            double vx = -2.0 - Math.random() * 3.0 * speedFactor;
            double vy = -1.0 - Math.random() * 2.5;
            Color dirtCol = (Math.random() > 0.5) ? new Color(180, 83, 9) : new Color(120, 53, 15);
            particles.add(new Particle(x, y, vx, vy, dirtCol, 4 + Math.random() * 4, 25));
        }
    }

    public void addExhaustSmoke(double x, double y) {
        double vx = -1.5 - Math.random() * 1.5;
        double vy = -0.5 + (Math.random() - 0.5) * 1.0;
        Color smokeCol = new Color(203, 213, 225, 180);
        particles.add(new Particle(x, y, vx, vy, smokeCol, 6 + Math.random() * 6, 35));
    }

    public void addCoinSparkles(double x, double y) {
        for (int i = 0; i < 12; i++) {
            double angle = Math.random() * Math.PI * 2;
            double speed = 1.5 + Math.random() * 3.5;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            Color gold = (Math.random() > 0.4) ? new Color(253, 224, 71) : Color.WHITE;
            particles.add(new Particle(x, y, vx, vy, gold, 4 + Math.random() * 3, 30));
        }
    }

    public void update() {
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            if (p.update()) {
                it.remove();
            }
        }
    }

    public void render(Graphics2D g2, double camX, double camY) {
        for (Particle p : particles) {
            p.render(g2, camX, camY);
        }
    }

    public void clear() {
        particles.clear();
    }
}
