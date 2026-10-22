import java.awt.*;
import java.awt.geom.*;

/**
 * Advanced Physics & Multi-Vehicle Engine for Hill Climb Racing.
 * 
 * Selectable Vehicles (Press [V] to Switch):
 * 1. 4x4 HILL CLIMBER JEEP: Balanced 4WD, heavy live axles, roof lights, headlights, rear spare tire.
 * 2. MONSTER CRUSHER: Giant knobby tires, lifted twin-shock suspension, dual exhaust stacks, high clearance.
 * 3. MOTOCROSS PRO: Agile stunt bike, inverted forks, lightweight, rapid backflips and aerial rotation.
 * 
 * Features:
 * - Working headlights casting glowing illumination cones onto the terrain ahead.
 * - Dynamic independent front & rear suspension springs with compression and rebound dampening.
 * - Chassis pitch inertia (wheelies under hard acceleration, nose-dives under hard braking).
 * - Animated driver inside cabin holding steering wheel / handlebars with physics head bobbing.
 */
public class Vehicle {
    public enum VehicleType {
        JEEP_4X4("4x4 HILL CLIMBER JEEP", 17.0, 52.0, 5.3, 0.12, 0.19, 0.022),
        MONSTER_TRUCK("MONSTER CRUSHER 4x4", 23.0, 56.0, 5.8, 0.14, 0.22, 0.020),
        DIRT_BIKE("MOTOCROSS PRO BIKE", 15.5, 44.0, 5.6, 0.13, 0.17, 0.034);

        public final String displayName;
        public final double wheelRadius;
        public final double wheelbase;
        public final double maxSpeed;
        public final double enginePower;
        public final double brakePower;
        public final double airRotateTorque;

        VehicleType(String name, double r, double wb, double speed, double power, double brake, double torque) {
            this.displayName = name;
            this.wheelRadius = r;
            this.wheelbase = wb;
            this.maxSpeed = speed;
            this.enginePower = power;
            this.brakePower = brake;
            this.airRotateTorque = torque;
        }
    }

    // Current Vehicle Type
    public VehicleType currentType = VehicleType.JEEP_4X4;

    // Physical state
    public double x, y;
    public double vx, vy;
    public double angle; // Radians
    public double vAngle;

    // Wheel dimensions & spring suspension
    public double wheelRadius;
    public double wheelbase;
    public double wheelSpinAngle = 0;
    public double rearSuspensionY = 12.0;
    public double frontSuspensionY = 12.0;
    public double rearSuspensionVel = 0;
    public double frontSuspensionVel = 0;

    // Driving parameters
    public double maxSpeed;
    public double maxReverseSpeed = -2.2;
    public double enginePower;
    public double brakePower;
    public double airRotateTorque;

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
    public CustomStack<String> stuntStack = new CustomStack<>(25);
    public double airTimeTimer = 0;
    public double totalAirRotation = 0;
    public double prevAngle = 0;
    public double wheelieTimer = 0;
    public double stoppieTimer = 0;
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
        applyVehicleType(VehicleType.JEEP_4X4);
    }

    public void applyVehicleType(VehicleType type) {
        this.currentType = type;
        this.wheelRadius = type.wheelRadius;
        this.wheelbase = type.wheelbase;
        this.maxSpeed = type.maxSpeed;
        this.enginePower = type.enginePower;
        this.brakePower = type.brakePower;
        this.airRotateTorque = type.airRotateTorque;
        this.rearSuspensionY = (type == VehicleType.MONSTER_TRUCK) ? 18.0 : 12.0;
        this.frontSuspensionY = (type == VehicleType.MONSTER_TRUCK) ? 18.0 : 12.0;
    }

    public void switchVehicle() {
        VehicleType[] types = VehicleType.values();
        int nextIndex = (currentType.ordinal() + 1) % types.length;
        applyVehicleType(types[nextIndex]);
        stuntStack.push("SWITCHED TO " + currentType.displayName + "!");
    }

    public void update(Terrain terrain, ParticleManager particles) {
        if (crashed) return;

        // -------------------------------------------------------------
        // 1. GRAVITY & AIR RESISTANCE
        // -------------------------------------------------------------
        double gravity = (currentType == VehicleType.MONSTER_TRUCK) ? 0.26 : 0.24;
        vy += gravity;

        vx *= 0.995;
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

        double rearWheelBottom = y + sinA * -halfBase + rearSuspensionY + wheelRadius;
        double frontWheelBottom = y + sinA * halfBase + frontSuspensionY + wheelRadius;

        rearGrounded = (rearWheelBottom >= groundRearY - 2.0);
        frontGrounded = (frontWheelBottom >= groundFrontY - 2.0);

        boolean onGround = rearGrounded || frontGrounded;

        // -------------------------------------------------------------
        // 3. SUSPENSION DYNAMICS & TERRAIN ALIGNMENT
        // -------------------------------------------------------------
        double targetRestY = (currentType == VehicleType.MONSTER_TRUCK) ? 18.0 : 12.0;

        if (onGround) {
            double avgGround = (groundRearY + groundFrontY) / 2.0;
            double targetY = avgGround - wheelRadius - targetRestY * 0.45;
            y += (targetY - y) * 0.38;
            if (vy > 0) vy *= 0.2;

            // Align chassis with terrain slope
            double targetSlope = terrain.getSlopeAt(x);
            double diff = targetSlope - angle;
            while (diff > Math.PI) diff -= Math.PI * 2;
            while (diff < -Math.PI) diff += Math.PI * 2;
            angle += diff * 0.15;
            vAngle *= 0.5;

            // Perfect landing detection
            if (wasAirborne && airTimeTimer > 0.8 && Math.abs(diff) < 0.35) {
                stuntStack.push("PERFECT LANDING! +500");
                particles.addFloatingText(x, y - 30, "PERFECT LANDING!", new Color(74, 222, 128));
            }
            wasAirborne = false;

            // Spring compression on ground
            if (rearGrounded) {
                rearSuspensionY += (targetRestY - 3.0 - rearSuspensionY) * 0.25;
            } else {
                rearSuspensionY += (targetRestY + 4.0 - rearSuspensionY) * 0.15;
            }

            if (frontGrounded) {
                frontSuspensionY += (targetRestY - 3.0 - frontSuspensionY) * 0.25;
            } else {
                frontSuspensionY += (targetRestY + 4.0 - frontSuspensionY) * 0.15;
            }

            // Wheelie & Stoppie Stunts
            if (rearGrounded && !frontGrounded && vx > 2.0) {
                wheelieTimer += 0.016;
                if (wheelieTimer > 0.8) {
                    stuntStack.push("WHEELIE BONUS! +250");
                    particles.addFloatingText(x, y - 25, "WHEELIE! +250", new Color(251, 191, 36));
                    wheelieTimer = 0;
                }
            } else {
                wheelieTimer = 0;
            }

            if (frontGrounded && !rearGrounded && vx > 2.0) {
                stoppieTimer += 0.016;
                if (stoppieTimer > 0.7) {
                    stuntStack.push("STOPPIE BONUS! +300");
                    particles.addFloatingText(x, y - 25, "STOPPIE! +300", new Color(244, 114, 182));
                    stoppieTimer = 0;
                }
            } else {
                stoppieTimer = 0;
            }

        } else {
            wasAirborne = true;
            // Shocks extend in mid-air
            rearSuspensionY += (targetRestY + 3.0 - rearSuspensionY) * 0.12;
            frontSuspensionY += (targetRestY + 3.0 - frontSuspensionY) * 0.12;
        }

        // -------------------------------------------------------------
        // 4. ENGINE POWER & TORQUE DYNAMICS
        // -------------------------------------------------------------
        double currentSlope = terrain.getSlopeAt(x);

        if (gasInput && fuel > 0) {
            fuel = Math.max(0, fuel - 0.026);
            rpm = Math.min(7800, rpm + 120);

            if (onGround) {
                double slopeResistance = -Math.sin(currentSlope) * 0.18;
                double effectiveForce = (isBoosting ? 0.28 : enginePower) + slopeResistance;
                double currentMax = isBoosting ? maxSpeed * 1.35 : maxSpeed;

                if (vx < currentMax) {
                    vx += Math.cos(currentSlope) * Math.max(0.04, effectiveForce);
                    vy += Math.sin(currentSlope) * Math.max(0.04, effectiveForce);
                }

                // Acceleration squats rear suspension and lifts front
                rearSuspensionY += 0.8;
                frontSuspensionY -= 0.6;

                // Tire dirt spray
                if (Math.abs(vx) > 1.0) {
                    particles.addDirtSpray(rearX, groundRearY, Math.abs(vx) * 0.25);
                }
            } else {
                // In-air pitch control (Lean Back)
                angle -= airRotateTorque;
            }

            // Exhaust smoke
            particles.addExhaustSmoke(rearX - cosA * 12, y + sinA * 12);

        } else {
            rpm = Math.max(1000, rpm - 85);
            if (onGround) {
                vx *= 0.988;
                vx += Math.sin(currentSlope) * 0.08;
            }
        }

        // -------------------------------------------------------------
        // 5. BRAKE & REVERSE DYNAMICS
        // -------------------------------------------------------------
        if (brakeInput) {
            rpm = Math.max(1000, rpm - 60);
            if (onGround) {
                if (vx > 0.3) {
                    vx -= brakePower;
                    // Braking compresses front suspension (nose dive)
                    frontSuspensionY += 0.9;
                    rearSuspensionY -= 0.6;
                } else if (vx > maxReverseSpeed) {
                    vx -= 0.09;
                }
            } else {
                // In-air pitch control (Lean Forward)
                angle += airRotateTorque;
            }
        }

        // -------------------------------------------------------------
        // 6. NITRO BOOST
        // -------------------------------------------------------------
        if (isBoosting && boostGauge > 0) {
            boostGauge = Math.max(0, boostGauge - 0.7);
            vx += Math.cos(angle) * 0.24;
            vy += Math.sin(angle) * 0.24;
            particles.addCoinSparkles(rearX - cosA * 18, y + 10);
        } else {
            isBoosting = false;
            boostGauge = Math.min(100.0, boostGauge + 0.16);
        }

        // -------------------------------------------------------------
        // 7. INTEGRATE VELOCITY & WHEEL ROTATION
        // -------------------------------------------------------------
        x += vx;
        y += vy;
        wheelSpinAngle += (vx / wheelRadius) * 2.2;

        // -------------------------------------------------------------
        // 8. AIR-TIME & FLIP ROTATION TRACKING
        // -------------------------------------------------------------
        if (!onGround) {
            airTimeTimer += 0.016;
            double dTheta = angle - prevAngle;
            totalAirRotation += dTheta;

            if (Math.abs(totalAirRotation) >= Math.PI * 1.85) {
                flipCount++;
                String stuntName = (totalAirRotation < 0) ? "BACKFLIP! +1000" : "FRONTFLIP! +1000";
                stuntStack.push(stuntName);
                particles.addFloatingText(x, y - 35, stuntName, new Color(253, 224, 71));
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
        // 9. HEAD IMPACT CRASH DETECTION
        // -------------------------------------------------------------
        double headX = x + sinA * 30.0;
        double headY = y - cosA * 30.0;
        double groundAtHead = terrain.getHeightAt(headX);

        if (headY >= groundAtHead && Math.abs(angle) > Math.PI * 0.52) {
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

        // 1. VEHICLE HEADLIGHT GLOW CONE (Casting forward light onto terrain)
        drawHeadlightBeam(vg);

        double halfBase = wheelbase / 2.0;

        // 2. WHEEL ASSEMBLIES (Dual Heavy-Duty Wheels)
        drawWheel(vg, -halfBase, rearSuspensionY, wheelRadius, wheelSpinAngle, currentType);
        drawWheel(vg, halfBase, frontSuspensionY, wheelRadius, wheelSpinAngle, currentType);

        // 3. CHASSIS, DRIVER & ACCENTS (Based on Vehicle Type)
        if (currentType == VehicleType.JEEP_4X4) {
            drawJeepChassis(vg, halfBase);
        } else if (currentType == VehicleType.MONSTER_TRUCK) {
            drawMonsterTruckChassis(vg, halfBase);
        } else {
            drawDirtBikeChassis(vg, halfBase);
        }

        vg.dispose();
    }

    private void drawHeadlightBeam(Graphics2D g2) {
        // Soft volumetric beam of yellow-white light illuminating the road
        int lx = (int) (wheelbase / 2.0 + 12);
        int ly = -4;

        Polygon beam = new Polygon();
        beam.addPoint(lx, ly);
        beam.addPoint(lx + 220, ly - 35);
        beam.addPoint(lx + 240, ly + 65);

        GradientPaint lightGrad = new GradientPaint(
            lx, ly, new Color(254, 240, 138, 70),
            lx + 230, ly, new Color(254, 240, 138, 0)
        );
        g2.setPaint(lightGrad);
        g2.fillPolygon(beam);
    }

    private void drawJeepChassis(Graphics2D g2, double halfBase) {
        // Shock absorbers & Live Axles
        drawShock(g2, -halfBase * 0.6, -4, -halfBase, rearSuspensionY, new Color(239, 68, 68));
        drawShock(g2, halfBase * 0.6, -4, halfBase, frontSuspensionY, new Color(239, 68, 68));

        // Heavy Drive Shaft & Differential
        g2.setColor(new Color(15, 23, 42));
        g2.setStroke(new BasicStroke(3.0f));
        g2.drawLine((int) -halfBase, (int) rearSuspensionY, (int) halfBase, (int) frontSuspensionY);
        g2.fillOval(-8, (int) ((rearSuspensionY + frontSuspensionY) / 2.0) - 6, 16, 12); // Differential

        // Exhaust pipe with smoke & backfire flames
        drawExhaust(g2, (int) -halfBase - 10, 4);

        // Heavy Red Jeep Body (Classic Hill Climber 4x4)
        GradientPaint bodyGrad = new GradientPaint(-28, -24, new Color(220, 38, 38), 28, 4, new Color(153, 27, 27));
        g2.setPaint(bodyGrad);

        Path2D.Double jeep = new Path2D.Double();
        jeep.moveTo(-28, 2);
        jeep.lineTo(-28, -14);
        jeep.lineTo(-6, -14);
        jeep.lineTo(8, -26);   // Windshield angle
        jeep.lineTo(24, -26);  // Hood
        jeep.lineTo(32, -10);  // Front grille
        jeep.lineTo(32, 2);
        jeep.lineTo(20, 2);
        jeep.lineTo(14, -4);
        jeep.lineTo(-14, -4);
        jeep.lineTo(-20, 2);
        jeep.closePath();
        g2.fill(jeep);

        // Body outline & bevel
        g2.setColor(new Color(127, 29, 29));
        g2.setStroke(new BasicStroke(2.0f));
        g2.draw(jeep);

        // Front Grille Slats (Classic 7-slot jeep grille)
        g2.setColor(new Color(15, 23, 42));
        g2.fillRect(28, -8, 4, 9);
        g2.setColor(Color.WHITE);
        g2.drawLine(29, -8, 29, 0);
        g2.drawLine(31, -8, 31, 0);

        // Front Headlight (Bright yellow lamp)
        g2.setColor(new Color(254, 240, 138));
        g2.fillOval(30, -14, 5, 5);
        g2.setColor(new Color(245, 158, 11));
        g2.drawOval(30, -14, 5, 5);

        // Windshield Frame & Glass
        g2.setColor(new Color(15, 23, 42));
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawLine(-6, -14, 8, -26);
        g2.drawLine(8, -26, 24, -26);
        g2.setColor(new Color(186, 230, 253, 160)); // Tinted glass
        g2.fillPolygon(new int[]{-5, 7, 7, -5}, new int[]{-13, -24, -13, -13}, 4);

        // Heavy Tubular Roll-Cage over cabin
        g2.setColor(new Color(30, 41, 59));
        g2.setStroke(new BasicStroke(3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(-26, -14, -14, -34);
        g2.drawLine(-14, -34, 4, -34);
        g2.drawLine(4, -34, 8, -26);
        g2.drawLine(-14, -34, -4, -14); // Cross bar

        // 4 Roof-Mounted Halogen Off-Road Fog Lights
        for (int i = 0; i < 4; i++) {
            int rx = -12 + (i * 5);
            g2.setColor(new Color(245, 158, 11));
            g2.fillOval(rx, -38, 4, 4);
            g2.setColor(new Color(254, 240, 138));
            g2.fillOval(rx + 1, -37, 2, 2);
        }

        // Rear Mounted Spare Tire & Jerrycan on tailgate
        g2.setColor(new Color(15, 23, 42));
        g2.fillOval(-36, -18, 16, 16);
        g2.setColor(new Color(71, 85, 105));
        g2.fillOval(-32, -14, 8, 8);
        // Jerrycan strapped to side
        g2.setColor(new Color(185, 28, 28));
        g2.fillRect(-22, -22, 7, 10);

        // Animated Driver inside Cockpit holding steering wheel
        drawDriverInCab(g2, 0, -16);
    }

    private void drawMonsterTruckChassis(Graphics2D g2, double halfBase) {
        // Enormous Lifted Dual Nitrogen Suspension Shocks
        drawShock(g2, -halfBase * 0.7, -10, -halfBase, rearSuspensionY, new Color(245, 158, 11));
        drawShock(g2, halfBase * 0.7, -10, halfBase, frontSuspensionY, new Color(245, 158, 11));

        // Heavy Ladder Frame Chassis (Yellow steel)
        g2.setColor(new Color(234, 179, 8));
        g2.setStroke(new BasicStroke(4.0f));
        g2.drawLine(-30, -6, 32, -6);
        g2.drawLine(-24, 0, 24, 0);
        g2.drawLine(-16, -6, -20, 0);
        g2.drawLine(16, -6, 20, 0);

        // Twin Vertical Chrome Exhaust Stacks
        g2.setColor(new Color(203, 213, 225));
        g2.setStroke(new BasicStroke(3.5f));
        g2.drawLine(-18, -6, -18, -38);
        g2.drawLine(-18, -38, -23, -42);
        if (gasInput || isBoosting) {
            // Hot flame bursts from vertical stacks
            g2.setColor(new Color(249, 115, 22));
            g2.fillPolygon(new int[]{-23, -33, -23}, new int[]{-44, -42, -40}, 3);
            g2.setColor(new Color(56, 189, 248));
            g2.fillPolygon(new int[]{-23, -29, -23}, new int[]{-43, -42, -41}, 3);
        }

        // Monster Truck Aggressive Body (Sleek Black & Golden Flame)
        GradientPaint bodyGrad = new GradientPaint(-28, -30, new Color(15, 23, 42), 30, -6, new Color(51, 65, 85));
        g2.setPaint(bodyGrad);

        Path2D.Double cab = new Path2D.Double();
        cab.moveTo(-28, -6);
        cab.lineTo(-28, -22);
        cab.lineTo(-12, -22);
        cab.lineTo(2, -34);
        cab.lineTo(20, -34);
        cab.lineTo(34, -20);
        cab.lineTo(34, -6);
        cab.closePath();
        g2.fill(cab);

        // Golden Flames on door
        g2.setColor(new Color(245, 158, 11));
        g2.fillPolygon(new int[]{-10, 10, -2, 6}, new int[]{-14, -14, -8, -8}, 4);

        // Monster Bull-Bar Bumper on front
        g2.setColor(new Color(203, 213, 225));
        g2.setStroke(new BasicStroke(3.0f));
        g2.drawRoundRect(32, -18, 8, 14, 3, 3);

        // Driver in cab
        drawDriverInCab(g2, 6, -20);
    }

    private void drawDirtBikeChassis(Graphics2D g2, double halfBase) {
        // Inverted Racing Forks & Monoshock
        drawShock(g2, 0, 0, -halfBase, rearSuspensionY, new Color(59, 130, 246));
        drawShock(g2, 8, -6, halfBase, frontSuspensionY, new Color(203, 213, 225));

        // Engine & Exhaust
        drawExhaust(g2, (int) -halfBase, 2);

        // Motocross body fairing (Gloss Cyan)
        GradientPaint bodyGrad = new GradientPaint(-25, -20, new Color(56, 189, 248), 25, 0, new Color(2, 132, 199));
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

        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(body);

        // Seat
        g2.setColor(new Color(15, 23, 42));
        g2.fillRoundRect(-21, -22, 19, 6, 3, 3);

        // Handlebars
        g2.setColor(new Color(203, 213, 225));
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawLine(16, -14, 20, -26);
        g2.setColor(new Color(15, 23, 42));
        g2.fillRect(17, -28, 7, 5);

        // Motocross Rider
        drawDirtBikeRider(g2);
    }

    private void drawShock(Graphics2D g2, double x1, double y1, double x2, double y2, Color springCol) {
        g2.setColor(new Color(71, 85, 105));
        g2.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine((int) x1, (int) y1, (int) x2, (int) y2);

        // Piston Rod
        g2.setColor(new Color(226, 232, 240));
        g2.setStroke(new BasicStroke(2.0f));
        g2.drawLine((int) (x1 * 0.7 + x2 * 0.3), (int) (y1 * 0.7 + y2 * 0.3), (int) (x1 * 0.3 + x2 * 0.7), (int) (y1 * 0.3 + y2 * 0.7));

        // Coiled Spring
        g2.setColor(springCol);
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

    private void drawExhaust(Graphics2D g2, int ex, int ey) {
        g2.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(203, 213, 225));
        g2.drawLine(ex + 12, ey, ex, ey - 4);
        g2.drawLine(ex, ey - 4, ex - 6, ey - 8);
        g2.setStroke(new BasicStroke(1));

        if (isBoosting) {
            g2.setColor(new Color(56, 189, 248));
            g2.fillPolygon(new int[]{ex - 6, ex - 28, ex - 6}, new int[]{ey - 10, ey - 8, ey - 6}, 3);
            g2.setColor(Color.WHITE);
            g2.fillPolygon(new int[]{ex - 6, ex - 18, ex - 6}, new int[]{ey - 9, ey - 8, ey - 7}, 3);
        }
    }

    private void drawDriverInCab(Graphics2D g2, int cx, int cy) {
        double headBob = (gasInput ? -2.0 : (brakeInput ? 2.0 : Math.sin(wheelSpinAngle * 0.5) * 1.0));

        // Red Shirt Driver Body
        g2.setColor(new Color(225, 29, 72));
        g2.fillRoundRect(cx - 7, cy - 8, 14, 16, 5, 5);

        // Arm holding Steering Wheel
        g2.setColor(new Color(225, 29, 72));
        g2.setStroke(new BasicStroke(3.0f));
        g2.drawLine(cx, cy, cx + 14, cy - 2);

        // Steering Wheel (3-spoke ring)
        g2.setColor(new Color(30, 41, 59));
        g2.setStroke(new BasicStroke(2.2f));
        g2.drawOval(cx + 12, cy - 8, 8, 12);
        g2.setStroke(new BasicStroke(1));

        // Head with Cap & Bobbing
        g2.setColor(new Color(254, 215, 170));
        g2.fillOval(cx - 5, (int) (cy - 19 + headBob), 12, 12);

        // Brown Cap
        g2.setColor(new Color(146, 64, 14));
        g2.fillArc(cx - 7, (int) (cy - 23 + headBob), 16, 13, 0, 180);
        g2.fillRect(cx - 4, (int) (cy - 18 + headBob), 15, 3); // Visor brim

        // Eye
        g2.setColor(Color.BLACK);
        g2.fillOval(cx + 2, (int) (cy - 16 + headBob), 3, 3);
    }

    private void drawDirtBikeRider(Graphics2D g2) {
        double leanAngle = (gasInput ? 0.22 : (brakeInput ? -0.18 : 0));
        Graphics2D dg = (Graphics2D) g2.create();
        dg.translate(-10, -22);
        dg.rotate(leanAngle);

        // Jacket
        GradientPaint jacketGrad = new GradientPaint(-6, -22, new Color(244, 63, 94), 6, 0, new Color(190, 18, 60));
        dg.setPaint(jacketGrad);
        dg.fillRoundRect(-6, -22, 13, 22, 6, 6);

        // Arm
        dg.setColor(new Color(225, 29, 72));
        dg.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        dg.drawLine(0, -16, 28, -6);

        // Gloves
        dg.setColor(new Color(251, 191, 36));
        dg.fillOval(26, -8, 6, 6);

        // Head
        dg.setColor(new Color(254, 215, 170));
        dg.fillOval(-6, -34, 14, 14);

        // Helmet
        GradientPaint capGrad = new GradientPaint(-8, -38, new Color(180, 83, 9), 10, -28, new Color(120, 53, 15));
        dg.setPaint(capGrad);
        dg.fillArc(-8, -38, 18, 15, 0, 180);
        dg.fillRect(-4, -33, 16, 4);

        // Goggles
        dg.setColor(new Color(15, 23, 42));
        dg.fillOval(0, -32, 10, 7);
        dg.setColor(new Color(56, 189, 248));
        dg.fillOval(2, -31, 6, 4);

        dg.dispose();
    }

    private void drawWheel(Graphics2D g2, double wx, double wy, double radius, double spinAngle, VehicleType type) {
        Graphics2D wg = (Graphics2D) g2.create();
        wg.translate(wx, wy);

        int r = (int) radius;

        // Ground contact shadow
        wg.setColor(new Color(15, 23, 42, 70));
        wg.fillOval(-r - 1, -r + 2, (r + 1) * 2, (r + 1) * 2);

        // Drilled Ventilated Brake Rotor Disc
        int discR = r - 5;
        wg.setColor(new Color(148, 163, 184));
        wg.fillOval(-discR, -discR, discR * 2, discR * 2);
        // Red Brake Caliper
        wg.setColor(new Color(220, 38, 38));
        wg.fillRoundRect(-discR - 1, -6, 6, 12, 2, 2);

        // Rotating Tire Assembly
        wg.rotate(spinAngle);

        // Knobby Rubber Tire
        GradientPaint tireGrad = new GradientPaint(-r, -r, new Color(51, 65, 85), r, r, new Color(15, 23, 42));
        wg.setPaint(tireGrad);
        wg.fillOval(-r, -r, r * 2, r * 2);

        // Deep Aggressive Off-Road Treads
        wg.setColor(new Color(15, 23, 42));
        int treadCount = (type == VehicleType.MONSTER_TRUCK) ? 12 : 9;
        wg.setStroke(new BasicStroke((type == VehicleType.MONSTER_TRUCK) ? 4.5f : 3.2f));
        for (int a = 0; a < 360; a += (360 / treadCount)) {
            double rad = Math.toRadians(a);
            int x1 = (int) (Math.cos(rad) * (r - 4));
            int y1 = (int) (Math.sin(rad) * (r - 4));
            int x2 = (int) (Math.cos(rad) * (r + 2));
            int y2 = (int) (Math.sin(rad) * (r + 2));
            wg.drawLine(x1, y1, x2, y2);
        }

        // Alloy Wheel Rim
        int rimR = r - ((type == VehicleType.MONSTER_TRUCK) ? 7 : 5);
        Color rimCol = (type == VehicleType.MONSTER_TRUCK) ? new Color(245, 158, 11) : Color.WHITE;
        wg.setColor(new Color(30, 41, 59));
        wg.fillOval(-rimR, -rimR, rimR * 2, rimR * 2);

        // Multi-Spokes
        wg.setColor(rimCol);
        wg.setStroke(new BasicStroke(3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int a = 0; a < 360; a += 90) {
            double rad = Math.toRadians(a);
            int sx = (int) (Math.cos(rad) * (rimR - 2));
            int sy = (int) (Math.sin(rad) * (rimR - 2));
            wg.drawLine(0, 0, sx, sy);
        }

        // Center Chrome Hub Cap with Anodized Nut
        wg.setColor(new Color(203, 213, 225));
        wg.fillOval(-5, -5, 10, 10);
        wg.setColor(new Color(239, 68, 68));
        wg.fillOval(-3, -3, 6, 6);

        wg.dispose();
    }
}
