import java.awt.*;
import java.awt.geom.*;

/**
 * 2D Physics Vehicle Model with Ultra-Smooth Controls & Tuned Speed.
 * 
 * Enhanced Realistic Visuals:
 * - Knobby off-road competition tires with sidewalls and tread blocks.
 * - Drilled ventilated brake disc rotor & brake caliper inside the wheel assembly.
 * - Realistic 3D coiled spring suspension shock absorbers that flex with terrain.
 * - Gloss-sheen motocross body panels with tubular chassis frame and chrome heat-tinted exhaust.
 * - Animated racing driver with helmet visor reflections and dynamic torso leaning.
 * - Tuned, silky-smooth driving physics and aerial pitch stability.
 */
public class Vehicle {
    // Physical state
    public double x, y;
    public double vx, vy;
    public double angle; // Radians
    public double vAngle;

    // Wheel dimensions & spring suspension
    public double wheelRadius = 16.5;
    public double wheelbase = 48.0;
    public double wheelSpinAngle = 0;
    public double rearSuspensionY = 12.0;
    public double frontSuspensionY = 12.0;

    // Tuned Driving Parameters for Silky Smooth Control
    public double maxSpeed = 5.2; // Controllable cruising speed
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
        double gravity = 0.24;
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

            // Suspension springs rest naturally with compression dampening
            rearSuspensionY += (12.0 - rearSuspensionY) * 0.25;
            frontSuspensionY += (12.0 - frontSuspensionY) * 0.25;

        } else {
            rearGrounded = false;
            frontGrounded = false;
            wasAirborne = true;
            rearSuspensionY += (14.5 - rearSuspensionY) * 0.15;
            frontSuspensionY += (14.5 - frontSuspensionY) * 0.15;
        }

        // -------------------------------------------------------------
        // 3. ENGINE POWER & SMOOTH ACCELERATION
        // -------------------------------------------------------------
        double currentSlope = terrain.getSlopeAt(x);

        if (gasInput && fuel > 0) {
            fuel = Math.max(0, fuel - 0.028);
            rpm = Math.min(7600, rpm + 110);

            if (onGround) {
                double slopeResistance = -Math.sin(currentSlope) * 0.18;
                double effectiveForce = (isBoosting ? 0.26 : enginePower) + slopeResistance;

                if (vx < (isBoosting ? maxSpeed * 1.35 : maxSpeed)) {
                    vx += Math.cos(currentSlope) * Math.max(0.04, effectiveForce);
                    vy += Math.sin(currentSlope) * Math.max(0.04, effectiveForce);
                }

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
                vx *= 0.988;
                vx += Math.sin(currentSlope) * 0.08;
            }
        }

        // -------------------------------------------------------------
        // 4. BRAKE / REVERSE & AIR TILT FORWARD
        // -------------------------------------------------------------
        if (brakeInput) {
            rpm = Math.max(1000, rpm - 50);
            if (onGround) {
                if (vx > 0.3) {
                    vx -= brakePower;
                } else if (vx > maxReverseSpeed) {
                    vx -= 0.08;
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

        // 1. REAR & FRONT WHEEL ASSEMBLIES (Tires, Rims, Rotors)
        drawWheel(vg, -halfBase, rearSuspensionY, wheelRadius, wheelSpinAngle);
        drawWheel(vg, halfBase, frontSuspensionY, wheelRadius, wheelSpinAngle);

        // 2. REALISTIC COILED SUSPENSION SHOCKS
        drawShockAbsorber(vg, 0, 0, -halfBase, rearSuspensionY);
        drawShockAbsorber(vg, 8, -6, halfBase, frontSuspensionY);

        // 3. ENGINE BLOCK & HEAT-TINTED EXHAUST
        drawEngineAndExhaust(vg);

        // 4. CHASSIS BODYWORK (Metallic gloss & decals)
        drawChassisBody(vg);

        // 5. ANIMATED DRIVER (Helmet, Visor, Wrinkled Jacket)
        drawDriver(vg);

        vg.dispose();
    }

    private void drawShockAbsorber(Graphics2D g2, double x1, double y1, double x2, double y2) {
        // Upper mounting arm
        g2.setColor(new Color(71, 85, 105));
        g2.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine((int) x1, (int) y1, (int) x2, (int) y2);

        // Chrome damper piston rod
        g2.setColor(new Color(226, 232, 240));
        g2.setStroke(new BasicStroke(2.0f));
        g2.drawLine((int) (x1 * 0.7 + x2 * 0.3), (int) (y1 * 0.7 + y2 * 0.3), (int) (x1 * 0.3 + x2 * 0.7), (int) (y1 * 0.3 + y2 * 0.7));

        // Red coiled spring rings
        g2.setColor(new Color(239, 68, 68));
        g2.setStroke(new BasicStroke(2.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int coils = 4;
        for (int i = 0; i < coils; i++) {
            double t = 0.25 + (0.50 / coils) * i;
            double px = x1 + (x2 - x1) * t;
            double py = y1 + (y2 - y1) * t;
            g2.drawLine((int) (px - 4), (int) (py - 2), (int) (px + 4), (int) (py + 2));
        }
        g2.setStroke(new BasicStroke(1));
    }

    private void drawEngineAndExhaust(Graphics2D g2) {
        // Engine block with cooling fins
        g2.setColor(new Color(30, 41, 59));
        g2.fillRoundRect(-14, -6, 24, 16, 4, 4);
        g2.setColor(new Color(15, 23, 42));
        g2.drawLine(-12, -2, 8, -2);
        g2.drawLine(-12, 2, 8, 2);
        g2.drawLine(-12, 6, 8, 6);

        // Chrome exhaust header with blue/gold heat discoloration near cylinder
        g2.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        GradientPaint heatGrad = new GradientPaint(-10, 2, new Color(59, 130, 246), -26, -4, new Color(245, 158, 11));
        g2.setPaint(heatGrad);
        g2.drawLine(-10, 2, -26, -4);

        g2.setColor(new Color(203, 213, 225));
        g2.drawLine(-26, -4, -30, -10);
        g2.setStroke(new BasicStroke(1));

        // Nitro plume if active
        if (isBoosting) {
            // Radiant blue afterburner flame
            g2.setColor(new Color(56, 189, 248));
            g2.fillPolygon(new int[]{-30, -55, -30}, new int[]{-13, -10, -7}, 3);
            g2.setColor(Color.WHITE);
            g2.fillPolygon(new int[]{-30, -42, -30}, new int[]{-12, -10, -8}, 3);
        }
    }

    private void drawChassisBody(Graphics2D g2) {
        // Tubular frame struts
        g2.setColor(new Color(15, 23, 42));
        g2.setStroke(new BasicStroke(3.0f));
        g2.drawLine(-18, -6, 18, -6);
        g2.drawLine(-12, -16, 6, -16);
        g2.drawLine(-12, -16, -18, -6);
        g2.drawLine(6, -16, 18, -6);

        // Motocross body fairing (Gloss cyan gradient)
        GradientPaint bodyGrad = new GradientPaint(
            -25, -20, new Color(56, 189, 248),
            25, 0, new Color(2, 132, 199)
        );
        g2.setPaint(bodyGrad);

        Path2D.Double body = new Path2D.Double();
        body.moveTo(-28, -6);
        body.lineTo(-18, -18);
        body.lineTo(2, -20);
        body.lineTo(22, -14);
        body.lineTo(27, -6);
        body.lineTo(12, 0);
        body.lineTo(-20, 0);
        body.closePath();
        g2.fill(body);

        // White curved racing stripe / decal (#43)
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2.0f));
        g2.drawLine(-12, -16, 10, -12);

        // Specular gloss shine streak on upper rim
        g2.setColor(new Color(255, 255, 255, 200));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(-17, -17, 1, -19);

        // Black leather seat
        g2.setColor(new Color(15, 23, 42));
        g2.fillRoundRect(-21, -22, 19, 6, 3, 3);
        g2.setColor(new Color(51, 65, 85));
        g2.drawRoundRect(-21, -22, 19, 6, 3, 3);

        // Handlebars with rubber grip
        g2.setColor(new Color(203, 213, 225));
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawLine(16, -14, 20, -26);
        g2.setColor(new Color(15, 23, 42)); // Black grip
        g2.fillRect(17, -28, 7, 5);
        g2.setStroke(new BasicStroke(1));
    }

    private void drawDriver(Graphics2D g2) {
        double leanAngle = (gasInput ? 0.22 : (brakeInput ? -0.18 : 0));
        Graphics2D dg = (Graphics2D) g2.create();
        dg.translate(-10, -22);
        dg.rotate(leanAngle);

        // Red Torso Jacket with shading
        GradientPaint jacketGrad = new GradientPaint(-6, -22, new Color(244, 63, 94), 6, 0, new Color(190, 18, 60));
        dg.setPaint(jacketGrad);
        dg.fillRoundRect(-6, -22, 13, 22, 6, 6);

        // Arm stretching forward to handlebars
        dg.setColor(new Color(225, 29, 72));
        dg.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        dg.drawLine(0, -16, 28, -6);
        dg.setStroke(new BasicStroke(1));

        // Yellow rider gloves
        dg.setColor(new Color(251, 191, 36));
        dg.fillOval(26, -8, 6, 6);

        // Driver Head & Neck
        dg.setColor(new Color(254, 215, 170));
        dg.fillOval(-6, -34, 14, 14);
        dg.fillOval(4, -30, 8, 6); // Cartoon nose

        // Classic Off-Road Racing Helmet (Brown Cap with Visor)
        GradientPaint capGrad = new GradientPaint(-8, -38, new Color(180, 83, 9), 10, -28, new Color(120, 53, 15));
        dg.setPaint(capGrad);
        dg.fillArc(-8, -38, 18, 15, 0, 180);
        dg.fillRect(-4, -33, 16, 4);

        // Helmet Goggles / Visor (Reflective glass tint)
        dg.setColor(new Color(15, 23, 42));
        dg.fillOval(0, -32, 10, 7);
        dg.setColor(new Color(56, 189, 248));
        dg.fillOval(2, -31, 6, 4);
        dg.setColor(Color.WHITE);
        dg.fillOval(3, -31, 2, 2); // Glint

        dg.dispose();
    }

    private void drawWheel(Graphics2D g2, double wx, double wy, double radius, double spinAngle) {
        Graphics2D wg = (Graphics2D) g2.create();
        wg.translate(wx, wy);

        int r = (int) radius;

        // 1. Cast shadow behind wheel
        wg.setColor(new Color(15, 23, 42, 60));
        wg.fillOval(-r - 1, -r + 2, (r + 1) * 2, (r + 1) * 2);

        // 2. Drilled Ventilated Brake Rotor Disc (Stationary behind rotating wheel)
        int discR = r - 6;
        wg.setColor(new Color(148, 163, 184));
        wg.fillOval(-discR, -discR, discR * 2, discR * 2);
        // Perforation holes
        wg.setColor(new Color(51, 65, 85));
        for (int a = 0; a < 360; a += 60) {
            double rad = Math.toRadians(a);
            int hx = (int) (Math.cos(rad) * (discR - 3));
            int hy = (int) (Math.sin(rad) * (discR - 3));
            wg.fillOval(hx - 1, hy - 1, 2, 2);
        }
        // Red Brake Caliper clamp
        wg.setColor(new Color(220, 38, 38));
        wg.fillRoundRect(-discR - 1, -6, 6, 12, 2, 2);

        // 3. Rotating Tire & Spokes
        wg.rotate(spinAngle);

        // Knobby Off-Road Tire Rubber (Dark charcoal)
        GradientPaint tireGrad = new GradientPaint(-r, -r, new Color(51, 65, 85), r, r, new Color(15, 23, 42));
        wg.setPaint(tireGrad);
        wg.fillOval(-r, -r, r * 2, r * 2);

        // Knobby Deep Off-Road Treads (High-traction blocks)
        wg.setColor(new Color(15, 23, 42));
        wg.setStroke(new BasicStroke(3.2f));
        for (int a = 0; a < 360; a += 40) {
            double rad = Math.toRadians(a);
            int x1 = (int) (Math.cos(rad) * (r - 4));
            int y1 = (int) (Math.sin(rad) * (r - 4));
            int x2 = (int) (Math.cos(rad) * (r + 1));
            int y2 = (int) (Math.sin(rad) * (r + 1));
            wg.drawLine(x1, y1, x2, y2);
        }

        // 4. White Multi-Spoke Competition Alloy Rim
        int rimR = r - 5;
        wg.setColor(new Color(30, 41, 59));
        wg.fillOval(-rimR, -rimR, rimR * 2, rimR * 2);

        // 4 White Spokes with bevel
        wg.setColor(Color.WHITE);
        wg.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        wg.drawLine(-rimR + 2, 0, rimR - 2, 0);
        wg.drawLine(0, -rimR + 2, 0, rimR - 2);

        // Center Chrome Hub Cap with Gold Anodized Nut
        wg.setColor(new Color(203, 213, 225));
        wg.fillOval(-5, -5, 10, 10);
        wg.setColor(new Color(245, 158, 11));
        wg.fillOval(-3, -3, 6, 6);

        wg.dispose();
    }
}
