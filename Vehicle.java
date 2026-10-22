import java.awt.*;
import java.awt.geom.*;

/**
 * 2D Physics Vehicle Model with Ultra-Smooth Controls & Tuned Speed.
 * 
 * Physics Tuning:
 * - Gentle, controlled cruising speed (Max 5.2 px/frame) avoiding excessive speed.
 * - Progressive acceleration and smooth slope gravity resistance.
 * - Silky smooth ground terrain alignment without jitter, snapping, or bouncing.
 * - Smooth in-air pitch tilting (Right Arrow = lean back, Left Arrow = lean forward).
 * - Animated driver and visual wheel spin.
 */
public class Vehicle {
    // Physical state
    public double x, y;
    public double vx, vy;
    public double angle; // Radians
    public double vAngle;

    // Wheel dimensions & spring suspension
    public double wheelRadius = 16.0;
    public double wheelbase = 46.0;
    public double wheelSpinAngle = 0;
    public double rearSuspensionY = 12.0;
    public double frontSuspensionY = 12.0;

    // Tuned Driving Parameters for Silky Smooth Control
    public double maxSpeed = 5.2; // Smooth, controllable cruising speed
    public double maxReverseSpeed = -2.2;
    public double enginePower = 0.11; // Gradual, gentle acceleration
    public double brakePower = 0.18;  // Smooth progressive braking
    public double airRotateTorque = 0.022; // Controlled air tilt

    // Fuel & Gauges
    public double fuel = 100.0;
    public double rpm = 1200;
    public double boostGauge = 100.0;
    public boolean isBoosting = false;

    // Inputs
    public boolean gasInput = false;
    public boolean brakeInput = false;

    // Contact state
    public boolean rearGrounded = false;
    public boolean frontGrounded = false;

    // Stunts & Combo System (DSA Stack)
    public CustomStack<String> stuntStack = new CustomStack<>(20);
    public double airTimeTimer = 0;
    public double totalAirRotation = 0;
    public double prevAngle = 0;
    public double wheelieTimer = 0;
    public boolean wasAirborne = false;
    public int flipCount = 0;
    public boolean crashed = false;

    public Vehicle(double startX, double startY) {
        this.x = startX;
        this.y = startY;
        this.vx = 0;
        this.vy = 0;
        this.angle = 0;
        this.vAngle = 0;
    }

    public void update(Terrain terrain, ParticleManager particles) {
        if (crashed) return;

        // -------------------------------------------------------------
        // 1. GRAVITY & DAMPING
        // -------------------------------------------------------------
        double gravity = 0.24; // Floatier, gentler gravity
        vy += gravity;

        vx *= 0.994;
        vy *= 0.994;

        // -------------------------------------------------------------
        // 2. WHEEL POSITIONING & TERRAIN CONTACT
        // -------------------------------------------------------------
        double cosA = Math.cos(angle);
        double sinA = Math.sin(angle);
        double halfBase = wheelbase / 2.0;

        double rearX = x - cosA * halfBase;
        double frontX = x + cosA * halfBase;

        double groundRearY = terrain.getHeightAt(rearX);
        double groundFrontY = terrain.getHeightAt(frontX);
        double avgGround = (groundRearY + groundFrontY) / 2.0;

        // Check if vehicle is resting on the ground
        boolean onGround = (y + wheelRadius >= avgGround - 2.5);

        if (onGround) {
            rearGrounded = true;
            frontGrounded = true;

            // Smoothly conform vertical position to ground without sudden snaps
            double targetY = avgGround - wheelRadius;
            y += (targetY - y) * 0.40;
            if (vy > 0) vy = 0;

            // Smoothly align vehicle angle to the terrain slope
            double targetSlope = terrain.getSlopeAt(x);
            double diff = targetSlope - angle;
            while (diff > Math.PI) diff -= Math.PI * 2;
            while (diff < -Math.PI) diff += Math.PI * 2;
            angle += diff * 0.14; // Silky smooth angular tracking
            vAngle *= 0.5;

            // Perfect landing check
            if (wasAirborne && airTimeTimer > 0.8 && Math.abs(diff) < 0.30) {
                stuntStack.push("PERFECT LANDING! +500");
            }
            wasAirborne = false;

            // Suspension springs rest naturally
            rearSuspensionY += (12.0 - rearSuspensionY) * 0.25;
            frontSuspensionY += (12.0 - frontSuspensionY) * 0.25;

        } else {
            rearGrounded = false;
            frontGrounded = false;
            wasAirborne = true;
            rearSuspensionY += (14.0 - rearSuspensionY) * 0.15;
            frontSuspensionY += (14.0 - frontSuspensionY) * 0.15;
        }

        // -------------------------------------------------------------
        // 3. ENGINE POWER & SMOOTH ACCELERATION
        // -------------------------------------------------------------
        double currentSlope = terrain.getSlopeAt(x);

        if (gasInput && fuel > 0) {
            fuel = Math.max(0, fuel - 0.028);
            rpm = Math.min(7600, rpm + 110);

            if (onGround) {
                // Slope gravity resistance: climbing hills requires steady momentum
                double slopeResistance = -Math.sin(currentSlope) * 0.18;
                double effectiveForce = (isBoosting ? 0.26 : enginePower) + slopeResistance;

                if (vx < (isBoosting ? maxSpeed * 1.35 : maxSpeed)) {
                    vx += Math.cos(currentSlope) * Math.max(0.04, effectiveForce);
                    vy += Math.sin(currentSlope) * Math.max(0.04, effectiveForce);
                }

                // Gentle tire spray
                if (Math.abs(vx) > 1.2) {
                    particles.addDirtSpray(rearX, groundRearY, Math.abs(vx) * 0.2);
                }
            } else {
                // In-air control: Gas tilts vehicle backward (Lean Back)
                angle -= airRotateTorque;
            }

            // Exhaust smoke
            particles.addExhaustSmoke(rearX - cosA * 10, y + sinA * 10);

        } else {
            rpm = Math.max(1000, rpm - 80);
            if (onGround) {
                vx *= 0.988; // Natural rolling friction
                vx += Math.sin(currentSlope) * 0.08; // Gentle coasting on slope
            }
        }

        // -------------------------------------------------------------
        // 4. BRAKE / REVERSE & AIR TILT FORWARD
        // -------------------------------------------------------------
        if (brakeInput) {
            rpm = Math.max(1000, rpm - 50);
            if (onGround) {
                if (vx > 0.3) {
                    vx -= brakePower; // Progressive brake
                } else if (vx > maxReverseSpeed) {
                    vx -= 0.08; // Smooth reverse
                }
            } else {
                // In-air control: Brake tilts vehicle forward (Lean Forward)
                angle += airRotateTorque;
            }
        }

        // -------------------------------------------------------------
        // 5. NITRO BOOST
        // -------------------------------------------------------------
        if (isBoosting && boostGauge > 0) {
            boostGauge = Math.max(0, boostGauge - 0.7);
            vx += Math.cos(angle) * 0.22;
            vy += Math.sin(angle) * 0.22;
            particles.addCoinSparkles(rearX - cosA * 16, y + 10);
        } else {
            isBoosting = false;
            boostGauge = Math.min(100.0, boostGauge + 0.15);
        }

        // -------------------------------------------------------------
        // 6. INTEGRATE POSITION & WHEEL ROTATION
        // -------------------------------------------------------------
        x += vx;
        y += vy;
        wheelSpinAngle += vx * 0.16;

        // -------------------------------------------------------------
        // 7. STUNTS & AIR-TIME TRACKING
        // -------------------------------------------------------------
        if (!onGround) {
            airTimeTimer += 0.016;
            double dTheta = angle - prevAngle;
            totalAirRotation += dTheta;

            if (Math.abs(totalAirRotation) >= Math.PI * 1.9) {
                flipCount++;
                String stuntName = (totalAirRotation < 0) ? "BACKFLIP! +1000" : "FRONTFLIP! +1000";
                stuntStack.push(stuntName);
                totalAirRotation = 0;
            }
        } else {
            if (airTimeTimer > 0.9) {
                int airPts = (int) (airTimeTimer * 250);
                stuntStack.push(String.format("AIR TIME (%.1fs) +%d", airTimeTimer, airPts));
            }
            airTimeTimer = 0;
            totalAirRotation = 0;
        }
        prevAngle = angle;

        // -------------------------------------------------------------
        // 8. HEAD CRASH DETECTION
        // -------------------------------------------------------------
        double headX = x + sinA * 28.0;
        double headY = y - cosA * 28.0;
        double groundAtHead = terrain.getHeightAt(headX);

        if (headY >= groundAtHead && Math.abs(angle) > Math.PI * 0.55) {
            crashed = true;
            stuntStack.push("DRIVER CRASHED!");
        }
    }

    public void render(Graphics2D g2, double camX, double camY) {
        int sx = (int) (x - camX);
        int sy = (int) (y - camY);

        Graphics2D vg = (Graphics2D) g2.create();
        vg.translate(sx, sy);
        vg.rotate(angle);

        double halfBase = wheelbase / 2.0;

        // 1. REAR & FRONT WHEELS (Smooth suspension positioning)
        drawWheel(vg, -halfBase, rearSuspensionY, wheelRadius, wheelSpinAngle);
        drawWheel(vg, halfBase, frontSuspensionY, wheelRadius, wheelSpinAngle);

        // 2. SUSPENSION FORKS
        vg.setColor(new Color(100, 116, 139));
        vg.setStroke(new BasicStroke(3.5f));
        vg.drawLine(0, 0, (int) -halfBase, (int) rearSuspensionY);
        vg.drawLine(8, -6, (int) halfBase, (int) frontSuspensionY);
        vg.setStroke(new BasicStroke(1));

        // 3. ENGINE BLOCK & EXHAUST
        vg.setColor(new Color(30, 41, 59));
        vg.fillRoundRect(-14, -6, 24, 16, 4, 4);

        // Chrome exhaust pipe
        vg.setColor(new Color(203, 213, 225));
        vg.setStroke(new BasicStroke(2.5f));
        vg.drawLine(-10, 2, -26, -4);
        vg.drawLine(-26, -4, -30, -10);
        vg.setStroke(new BasicStroke(1));

        // Nitro flame if boosting
        if (isBoosting) {
            vg.setColor(new Color(56, 189, 248));
            vg.fillPolygon(new int[]{-30, -50, -30}, new int[]{-12, -10, -8}, 3);
            vg.setColor(new Color(245, 158, 11));
            vg.fillPolygon(new int[]{-30, -42, -30}, new int[]{-14, -10, -6}, 3);
        }

        // 4. SKY-BLUE MOTOCROSS CHASSIS
        GradientPaint bodyGrad = new GradientPaint(-25, -20, new Color(56, 189, 248), 25, 0, new Color(2, 132, 199));
        vg.setPaint(bodyGrad);

        Path2D.Double body = new Path2D.Double();
        body.moveTo(-28, -6);
        body.lineTo(-18, -18);
        body.lineTo(2, -20);
        body.lineTo(22, -14);
        body.lineTo(26, -6);
        body.lineTo(12, 0);
        body.lineTo(-20, 0);
        body.closePath();
        vg.fill(body);

        vg.setColor(Color.WHITE);
        vg.draw(body);

        // Seat
        vg.setColor(new Color(15, 23, 42));
        vg.fillRoundRect(-20, -22, 18, 6, 3, 3);

        // Handlebars
        vg.setColor(new Color(203, 213, 225));
        vg.setStroke(new BasicStroke(2.5f));
        vg.drawLine(16, -14, 20, -26);
        vg.fillRect(18, -28, 6, 4);
        vg.setStroke(new BasicStroke(1));

        // 5. ANIMATED DRIVER
        double leanAngle = (gasInput ? 0.22 : (brakeInput ? -0.18 : 0));
        Graphics2D dg = (Graphics2D) vg.create();
        dg.translate(-10, -22);
        dg.rotate(leanAngle);

        // Red Torso
        dg.setColor(new Color(225, 29, 72));
        dg.fillRoundRect(-6, -22, 12, 22, 5, 5);

        // Arms
        dg.setColor(new Color(225, 29, 72));
        dg.setStroke(new BasicStroke(3.0f));
        dg.drawLine(0, -16, 28, -6);
        dg.setStroke(new BasicStroke(1));

        // Hands
        dg.setColor(new Color(251, 191, 36));
        dg.fillOval(26, -8, 6, 6);

        // Head & Face
        dg.setColor(new Color(254, 215, 170));
        dg.fillOval(-6, -34, 14, 14);
        dg.fillOval(4, -30, 8, 6); // Cartoon nose

        // Brown Cap
        dg.setColor(new Color(146, 64, 14));
        dg.fillArc(-8, -38, 18, 14, 0, 180);
        dg.fillRect(-4, -33, 16, 4);

        // Eye
        dg.setColor(Color.BLACK);
        dg.fillOval(2, -31, 3, 3);

        dg.dispose();
        vg.dispose();
    }

    private void drawWheel(Graphics2D g2, double wx, double wy, double radius, double spinAngle) {
        Graphics2D wg = (Graphics2D) g2.create();
        wg.translate(wx, wy);
        wg.rotate(spinAngle);

        int r = (int) radius;

        // Tire
        wg.setColor(new Color(15, 23, 42));
        wg.fillOval(-r, -r, r * 2, r * 2);

        // Treads
        wg.setColor(new Color(51, 65, 85));
        wg.setStroke(new BasicStroke(2.5f));
        for (int a = 0; a < 360; a += 45) {
            double rad = Math.toRadians(a);
            int x1 = (int) (Math.cos(rad) * (r - 4));
            int y1 = (int) (Math.sin(rad) * (r - 4));
            int x2 = (int) (Math.cos(rad) * r);
            int y2 = (int) (Math.sin(rad) * r);
            wg.drawLine(x1, y1, x2, y2);
        }

        // Inner rim
        int innerR = r - 5;
        wg.setColor(new Color(71, 85, 105));
        wg.fillOval(-innerR, -innerR, innerR * 2, innerR * 2);

        // White 4-spoke cross
        wg.setColor(Color.WHITE);
        wg.setStroke(new BasicStroke(2.5f));
        wg.drawLine(-innerR + 2, 0, innerR - 2, 0);
        wg.drawLine(0, -innerR + 2, 0, innerR - 2);

        // Hub nut
        wg.setColor(new Color(245, 158, 11));
        wg.fillOval(-4, -4, 8, 8);

        wg.dispose();
    }
}
