import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Advanced Physics, Damage & Accident Vehicle Engine for Hill Climb Racing.
 * 
 * Features:
 * 1. Realistic Crash / Accident Damage System:
 *    - Wheels detach and bounce/roll down hills dynamically with terrain gravity & friction.
 *    - Driver is ejected forward and ragdolls/tumbles to a stop on the ground.
 *    - Chassis suffers structural damage, grinding across terrain with spark showers and black smoke.
 * 2. Selectable Advanced Vehicles:
 *    - MOTOCROSS PRO BIKE (Cyan body, "MONSTER" & "FINGER" competition tire sidewalls, suspension forks).
 *    - 4x4 HILL CLIMBER JEEP (4WD live axles, headlights, roof foglights, spare tire).
 *    - MONSTER CRUSHER 4x4 (Giant tires, twin vertical flame exhaust stacks).
 * 3. Working Headlights casting dynamic forward lighting onto the terrain.
 */
public class Vehicle {
    public enum VehicleType {
        DIRT_BIKE("MOTOCROSS PRO BIKE", 15.5, 44.0, 5.6, 0.13, 0.17, 0.034),
        JEEP_4X4("4x4 HILL CLIMBER JEEP", 17.0, 52.0, 5.3, 0.12, 0.19, 0.022),
        MONSTER_TRUCK("MONSTER CRUSHER 4x4", 23.0, 56.0, 5.8, 0.14, 0.22, 0.020);

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

    // -------------------------------------------------------------
    // ACCIDENT & DETACHED PARTS PHYSICS
    // -------------------------------------------------------------
    public static class DetachedWheel {
        public double x, y;
        public double vx, vy;
        public double angle;
        public double vAngle;
        public double radius;
        public VehicleType type;

        public DetachedWheel(double x, double y, double vx, double vy, double radius, VehicleType type) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.radius = radius;
            this.type = type;
            this.angle = 0;
            this.vAngle = vx * 0.18;
        }

        public void update(Terrain terrain) {
            vy += 0.25; // Gravity
            vx *= 0.985; // Rolling friction
            x += vx;
            y += vy;
            angle += vAngle;

            double ground = terrain.getHeightAt(x);
            if (y + radius >= ground) {
                y = ground - radius;
                if (vy > 0.8) {
                    vy = -vy * 0.52; // Realistic bouncy rubber rebound
                } else {
                    vy = 0;
                }
                vAngle = vx * 0.16;
            }
        }
    }

    public static class RagdollDriver {
        public double x, y;
        public double vx, vy;
        public double angle;
        public double vAngle;

        public RagdollDriver(double x, double y, double vx, double vy) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.angle = 0;
            this.vAngle = (Math.random() - 0.5) * 0.25;
        }

        public void update(Terrain terrain) {
            vy += 0.24;
            vx *= 0.95;
            x += vx;
            y += vy;
            angle += vAngle;

            double ground = terrain.getHeightAt(x);
            if (y >= ground - 8.0) {
                y = ground - 8.0;
                vy = -vy * 0.25;
                vAngle *= 0.5;
            }
        }
    }

    // Current Vehicle Type
    public VehicleType currentType = VehicleType.DIRT_BIKE;

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

    // Accident & Crash Damage state
    public boolean crashed = false;
    public boolean accidentTriggered = false;
    public List<DetachedWheel> detachedWheels = new ArrayList<>();
    public RagdollDriver ragdollDriver = null;

    public Vehicle(double startX, double startY) {
        this.x = startX;
        this.y = startY;
        this.vx = 0;
        this.vy = 0;
        this.angle = 0;
        this.vAngle = 0;
        applyVehicleType(VehicleType.DIRT_BIKE);
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
        if (crashed) return;
        VehicleType[] types = VehicleType.values();
        int nextIndex = (currentType.ordinal() + 1) % types.length;
        applyVehicleType(types[nextIndex]);
        stuntStack.push("SWITCHED TO " + currentType.displayName + "!");
    }

    public void triggerAccident(Terrain terrain, ParticleManager particles) {
        if (accidentTriggered) return;
        accidentTriggered = true;
        crashed = true;

        double cosA = Math.cos(angle);
        double sinA = Math.sin(angle);
        double halfBase = wheelbase / 2.0;

        // 1. Detach Rear Wheel
        double rearX = x - cosA * halfBase;
        double rearY = y + rearSuspensionY;
        detachedWheels.add(new DetachedWheel(rearX, rearY, vx * 0.9 - 1.5, vy - 3.2, wheelRadius, currentType));

        // 2. Detach Front Wheel
        double frontX = x + cosA * halfBase;
        double frontY = y + frontSuspensionY;
        detachedWheels.add(new DetachedWheel(frontX, frontY, vx * 1.1 + 2.0, vy - 4.0, wheelRadius, currentType));

        // 3. Eject Driver
        ragdollDriver = new RagdollDriver(x, y - 20, vx + 2.5, vy - 3.5);

        // 4. Initial Crash Explosion FX
        particles.addCrashSparks(x, y);
        particles.addDarkCrashSmoke(x, y);
        particles.addFloatingText(x, y - 40, "ACCIDENT! DRIVER DOWN", new Color(239, 68, 68));
    }

    public void update(Terrain terrain, ParticleManager particles) {
        // -------------------------------------------------------------
        // IF CRASHED: RUN CRASH ACCIDENT DYNAMICS (WRECK SLIDING & PARTS BOUNCING)
        // -------------------------------------------------------------
        if (crashed) {
            if (!accidentTriggered) {
                triggerAccident(terrain, particles);
            }

            // Chassis grinds on ground with friction
            vy += 0.28;
            vx *= 0.94;
            x += vx;
            y += vy;

            double ground = terrain.getHeightAt(x);
            if (y >= ground - 8) {
                y = ground - 8;
                vy = 0;
                // Grinding sparks if sliding
                if (Math.abs(vx) > 0.6) {
                    particles.addCrashSparks(x, y);
                }
            }

            // Continuous dark oily smoke from wrecked engine
            if (Math.random() > 0.4) {
                particles.addDarkCrashSmoke(x, y - 10);
            }

            // Update detached wheels
            for (DetachedWheel w : detachedWheels) {
                w.update(terrain);
            }

            // Update ragdolled driver
            if (ragdollDriver != null) {
                ragdollDriver.update(terrain);
            }
            return;
        }

        // -------------------------------------------------------------
        // NORMAL ACTIVE DRIVING PHYSICS (HOOK'S LAW SUSPENSION & WEIGHT TRANSFER)
        // -------------------------------------------------------------
        double gravity = (currentType == VehicleType.MONSTER_TRUCK) ? 0.26 : 0.24;
        vy += gravity;

        vx *= 0.995;
        vy *= 0.994;

        double cosA = Math.cos(angle);
        double sinA = Math.sin(angle);
        double halfBase = wheelbase / 2.0;

        // Wheel positions in world space
        double rearX = x - cosA * halfBase;
        double frontX = x + cosA * halfBase;

        double groundRearY = terrain.getHeightAt(rearX);
        double groundFrontY = terrain.getHeightAt(frontX);

        double rearWheelBottom = y - sinA * halfBase + rearSuspensionY + wheelRadius;
        double frontWheelBottom = y + sinA * halfBase + frontSuspensionY + wheelRadius;

        // Ground contact penetrations
        double rearPenetration = rearWheelBottom - groundRearY;
        double frontPenetration = frontWheelBottom - groundFrontY;

        rearGrounded = (rearPenetration >= -2.0);
        frontGrounded = (frontPenetration >= -2.0);
        boolean onGround = rearGrounded || frontGrounded;

        double targetRestY = (currentType == VehicleType.MONSTER_TRUCK) ? 18.0 : 12.0;
        double springStiffness = (currentType == VehicleType.MONSTER_TRUCK) ? 0.42 : 0.46;
        double springDamping = 0.28;

        // 1. DYNAMIC SUSPENSION SPRING-DAMPER FORCES (Hooke's Law)
        if (rearGrounded && rearPenetration > 0) {
            double springForce = rearPenetration * springStiffness;
            y -= springForce * 0.55;
            if (vy > 0) vy *= (1.0 - springDamping);
            rearSuspensionY = Math.max(targetRestY - 6.0, rearSuspensionY - springForce * 0.45);
        } else {
            rearSuspensionY += (targetRestY - rearSuspensionY) * 0.18;
        }

        if (frontGrounded && frontPenetration > 0) {
            double springForce = frontPenetration * springStiffness;
            y -= springForce * 0.55;
            if (vy > 0) vy *= (1.0 - springDamping);
            frontSuspensionY = Math.max(targetRestY - 6.0, frontSuspensionY - springForce * 0.45);
        } else {
            frontSuspensionY += (targetRestY - frontSuspensionY) * 0.18;
        }

        // 2. CHASSIS GROUND SCRAPING / BOTTOMING OUT
        double groundAtChassis = terrain.getHeightAt(x);
        if (y + 4.0 >= groundAtChassis) {
            y = groundAtChassis - 4.0;
            if (vy > 0) vy = -vy * 0.25;
            vx *= 0.96;
            particles.addCrashSparks(x, y + 2.0);
        }

        // 3. TERRAIN ANGULAR ALIGNMENT & MOMENTUM
        if (onGround) {
            double targetSlope = terrain.getSlopeAt(x);
            double diff = targetSlope - angle;
            while (diff > Math.PI) diff -= Math.PI * 2;
            while (diff < -Math.PI) diff += Math.PI * 2;

            // Torque reaction when both wheels or one wheel is grounded
            double alignRate = (rearGrounded && frontGrounded) ? 0.16 : 0.08;
            angle += diff * alignRate;
            vAngle *= 0.70;

            if (wasAirborne && airTimeTimer > 0.8 && Math.abs(diff) < 0.35) {
                stuntStack.push("PERFECT LANDING! +500");
                particles.addFloatingText(x, y - 30, "PERFECT LANDING!", new Color(74, 222, 128));
            }
            wasAirborne = false;

            // Stunts: Wheelie & Stoppie
            if (rearGrounded && !frontGrounded && vx > 2.0) {
                wheelieTimer += 0.016;
                if (wheelieTimer > 0.8) {
                    stuntStack.push("WHEELIE BONUS! +250");
                    particles.addFloatingText(x, y - 25, "WHEELIE! +250", new Color(251, 191, 36));
                    wheelieTimer = 0;
                }
            } else wheelieTimer = 0;

            if (frontGrounded && !rearGrounded && vx > 2.0) {
                stoppieTimer += 0.016;
                if (stoppieTimer > 0.7) {
                    stuntStack.push("STOPPIE BONUS! +300");
                    particles.addFloatingText(x, y - 25, "STOPPIE! +300", new Color(244, 114, 182));
                    stoppieTimer = 0;
                }
            } else stoppieTimer = 0;

        } else {
            wasAirborne = true;
        }

        // 4. ENGINE POWER, TORQUE & WEIGHT TRANSFER (Natural Wheelie on Acceleration)
        double currentSlope = terrain.getSlopeAt(x);

        if (gasInput && fuel > 0) {
            fuel = Math.max(0, fuel - 0.026);
            rpm = Math.min(7800, rpm + 120);

            if (onGround) {
                double slopeResistance = -Math.sin(currentSlope) * 0.18;
                double effectiveForce = (isBoosting ? 0.30 : enginePower) + slopeResistance;
                double currentMax = isBoosting ? maxSpeed * 1.35 : maxSpeed;

                if (vx < currentMax) {
                    vx += Math.cos(currentSlope) * Math.max(0.04, effectiveForce);
                    vy += Math.sin(currentSlope) * Math.max(0.04, effectiveForce);
                }

                // Dynamic Weight Transfer: Accelerating squats rear suspension and lifts front
                rearSuspensionY = Math.min(targetRestY + 3.0, rearSuspensionY + 0.5);
                frontSuspensionY = Math.max(targetRestY - 4.0, frontSuspensionY - 0.4);

                // Rotational torque: Hard acceleration tips nose up
                if (rearGrounded && !frontGrounded) {
                    angle -= 0.014;
                }

                if (Math.abs(vx) > 1.0) {
                    particles.addDirtSpray(rearX, groundRearY, Math.abs(vx) * 0.25);
                }
            } else {
                angle -= airRotateTorque; // In-air backward pitch
            }

            particles.addExhaustSmoke(rearX - cosA * 12, y + sinA * 12);

        } else {
            rpm = Math.max(1000, rpm - 85);
            if (onGround) {
                vx *= 0.988;
                vx += Math.sin(currentSlope) * 0.08; // Natural rolling slope gravity
            }
        }

        // 5. BRAKE, STOPPIE NOSE-DIVE & AIR ROTATION
        if (brakeInput) {
            rpm = Math.max(1000, rpm - 60);
            if (onGround) {
                if (vx > 0.3) {
                    vx -= brakePower;
                    // Dynamic Weight Transfer: Braking compresses front suspension (nose dive)
                    frontSuspensionY = Math.min(targetRestY + 3.0, frontSuspensionY + 0.6);
                    rearSuspensionY = Math.max(targetRestY - 4.0, rearSuspensionY - 0.4);

                    // Rotational torque: Hard braking tips nose down
                    if (frontGrounded && !rearGrounded) {
                        angle += 0.014;
                    }
                } else if (vx > maxReverseSpeed) {
                    vx -= 0.09;
                }
            } else {
                angle += airRotateTorque; // In-air forward pitch
            }
        }

        // 6. NITRO BOOST
        if (isBoosting && boostGauge > 0) {
            boostGauge = Math.max(0, boostGauge - 0.7);
            vx += Math.cos(angle) * 0.24;
            vy += Math.sin(angle) * 0.24;
            particles.addCoinSparkles(rearX - cosA * 18, y + 10);
        } else {
            isBoosting = false;
            boostGauge = Math.min(100.0, boostGauge + 0.16);
        }

        x += vx;
        y += vy;
        wheelSpinAngle += (vx / wheelRadius) * 2.2;

        // 7. AERIAL FLIPS & STUNTS
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
        // ACCIDENT TRIGGER: HEAD GROUND IMPACT
        // -------------------------------------------------------------
        double headX = x + sinA * 28.0;
        double headY = y - cosA * 28.0;
        double groundAtHead = terrain.getHeightAt(headX);

        if (headY >= groundAtHead && Math.abs(angle) > Math.PI * 0.50) {
            triggerAccident(terrain, particles);
        }
    }

    public void render(Graphics2D g2, double camX, double camY) {
        // -------------------------------------------------------------
        // 1. RENDER DETACHED WHEELS IF ACCIDENT HAPPENED
        // -------------------------------------------------------------
        if (accidentTriggered) {
            for (DetachedWheel w : detachedWheels) {
                int wx = (int) (w.x - camX);
                int wy = (int) (w.y - camY);
                drawWheel(g2, wx, wy, w.radius, w.angle, w.type);
            }

            // Render Ragdolled Driver
            if (ragdollDriver != null) {
                int rx = (int) (ragdollDriver.x - camX);
                int ry = (int) (ragdollDriver.y - camY);
                Graphics2D rg = (Graphics2D) g2.create();
                rg.translate(rx, ry);
                rg.rotate(ragdollDriver.angle);
                // Body
                rg.setColor(new Color(225, 29, 72));
                rg.fillRoundRect(-6, -8, 12, 16, 4, 4);
                // Head with cap
                rg.setColor(new Color(254, 215, 170));
                rg.fillOval(-5, -18, 10, 10);
                rg.setColor(new Color(146, 64, 14));
                rg.fillArc(-7, -22, 14, 12, 0, 180);
                rg.dispose();
            }
        }

        // -------------------------------------------------------------
        // 2. RENDER VEHICLE CHASSIS
        // -------------------------------------------------------------
        int sx = (int) (x - camX);
        int sy = (int) (y - camY);

        Graphics2D vg = (Graphics2D) g2.create();
        vg.translate(sx, sy);
        vg.rotate(angle);

        if (!accidentTriggered) {
            // Headlight illumination beam
            drawHeadlightBeam(vg);

            double halfBase = wheelbase / 2.0;

            // Intact wheels
            drawWheel(vg, -halfBase, rearSuspensionY, wheelRadius, wheelSpinAngle, currentType);
            drawWheel(vg, halfBase, frontSuspensionY, wheelRadius, wheelSpinAngle, currentType);
        }

        double halfBase = wheelbase / 2.0;

        // Render Chassis
        if (currentType == VehicleType.DIRT_BIKE) {
            drawDirtBikeChassis(vg, halfBase);
        } else if (currentType == VehicleType.JEEP_4X4) {
            drawJeepChassis(vg, halfBase);
        } else {
            drawMonsterTruckChassis(vg, halfBase);
        }

        // Render crash fire / smoke if damaged
        if (accidentTriggered) {
            vg.setColor(new Color(239, 68, 68));
            vg.fillOval(-10, -12, 20, 12);
            vg.setColor(new Color(245, 158, 11));
            vg.fillOval(-6, -10, 12, 8);
        }

        vg.dispose();
    }

    private void drawHeadlightBeam(Graphics2D g2) {
        int lx = (int) (wheelbase / 2.0 + 10);
        int ly = -4;

        Polygon beam = new Polygon();
        beam.addPoint(lx, ly);
        beam.addPoint(lx + 230, ly - 35);
        beam.addPoint(lx + 250, ly + 65);

        GradientPaint lightGrad = new GradientPaint(
            lx, ly, new Color(254, 240, 138, 70),
            lx + 240, ly, new Color(254, 240, 138, 0)
        );
        g2.setPaint(lightGrad);
        g2.fillPolygon(beam);
    }

    private void drawDirtBikeChassis(Graphics2D g2, double halfBase) {
        if (!accidentTriggered) {
            // Suspension Forks
            drawShock(g2, 0, 0, -halfBase, rearSuspensionY, new Color(59, 130, 246));
            drawShock(g2, 8, -6, halfBase, frontSuspensionY, new Color(203, 213, 225));
        }

        // Exhaust pipe
        drawExhaust(g2, (int) -halfBase, 2);

        // Motocross Cyan Body Fairing (Exactly like photo!)
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

        // Black seat
        g2.setColor(new Color(15, 23, 42));
        g2.fillRoundRect(-21, -22, 19, 6, 3, 3);

        // Handlebars
        g2.setColor(new Color(203, 213, 225));
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawLine(16, -14, 20, -26);
        g2.setColor(new Color(15, 23, 42));
        g2.fillRect(17, -28, 7, 5);
        g2.setStroke(new BasicStroke(1));

        // Intact Driver (Only if NOT accident!)
        if (!accidentTriggered) {
            drawDirtBikeRider(g2);
        }
    }

    private void drawJeepChassis(Graphics2D g2, double halfBase) {
        if (!accidentTriggered) {
            drawShock(g2, -halfBase * 0.6, -4, -halfBase, rearSuspensionY, new Color(239, 68, 68));
            drawShock(g2, halfBase * 0.6, -4, halfBase, frontSuspensionY, new Color(239, 68, 68));
            g2.setColor(new Color(15, 23, 42));
            g2.setStroke(new BasicStroke(3.0f));
            g2.drawLine((int) -halfBase, (int) rearSuspensionY, (int) halfBase, (int) frontSuspensionY);
            g2.fillOval(-8, (int) ((rearSuspensionY + frontSuspensionY) / 2.0) - 6, 16, 12);
        }

        drawExhaust(g2, (int) -halfBase - 10, 4);

        // Red Jeep Body
        GradientPaint bodyGrad = new GradientPaint(-28, -24, new Color(220, 38, 38), 28, 4, new Color(153, 27, 27));
        g2.setPaint(bodyGrad);

        Path2D.Double jeep = new Path2D.Double();
        jeep.moveTo(-28, 2);
        jeep.lineTo(-28, -14);
        jeep.lineTo(-6, -14);
        jeep.lineTo(8, -26);
        jeep.lineTo(24, -26);
        jeep.lineTo(32, -10);
        jeep.lineTo(32, 2);
        jeep.lineTo(20, 2);
        jeep.lineTo(14, -4);
        jeep.lineTo(-14, -4);
        jeep.lineTo(-20, 2);
        jeep.closePath();
        g2.fill(jeep);

        // Windshield
        g2.setColor(new Color(186, 230, 253, 160));
        g2.fillPolygon(new int[]{-5, 7, 7, -5}, new int[]{-13, -24, -13, -13}, 4);

        // Roll cage
        g2.setColor(new Color(30, 41, 59));
        g2.setStroke(new BasicStroke(3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(-26, -14, -14, -34);
        g2.drawLine(-14, -34, 4, -34);
        g2.drawLine(4, -34, 8, -26);

        // Roof lights
        for (int i = 0; i < 4; i++) {
            g2.setColor(new Color(254, 240, 138));
            g2.fillOval(-12 + i * 5, -38, 4, 4);
        }

        if (!accidentTriggered) {
            drawDriverInCab(g2, 0, -16);
        }
    }

    private void drawMonsterTruckChassis(Graphics2D g2, double halfBase) {
        if (!accidentTriggered) {
            drawShock(g2, -halfBase * 0.7, -10, -halfBase, rearSuspensionY, new Color(245, 158, 11));
            drawShock(g2, halfBase * 0.7, -10, halfBase, frontSuspensionY, new Color(245, 158, 11));
        }

        // Vertical Stacks
        g2.setColor(new Color(203, 213, 225));
        g2.setStroke(new BasicStroke(3.5f));
        g2.drawLine(-18, -6, -18, -38);
        g2.drawLine(-18, -38, -23, -42);

        // Monster Cab
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

        // Flame decal
        g2.setColor(new Color(245, 158, 11));
        g2.fillPolygon(new int[]{-10, 10, -2, 6}, new int[]{-14, -14, -8, -8}, 4);

        if (!accidentTriggered) {
            drawDriverInCab(g2, 6, -20);
        }
    }

    private void drawShock(Graphics2D g2, double x1, double y1, double x2, double y2, Color springCol) {
        g2.setColor(new Color(71, 85, 105));
        g2.setStroke(new BasicStroke(3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine((int) x1, (int) y1, (int) x2, (int) y2);

        g2.setColor(springCol);
        g2.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
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
        g2.setColor(new Color(225, 29, 72));
        g2.fillRoundRect(cx - 7, cy - 8, 14, 16, 5, 5);
        g2.setColor(new Color(254, 215, 170));
        g2.fillOval(cx - 5, (int) (cy - 19 + headBob), 12, 12);
        g2.setColor(new Color(146, 64, 14));
        g2.fillArc(cx - 7, (int) (cy - 23 + headBob), 16, 13, 0, 180);
        g2.fillRect(cx - 4, (int) (cy - 18 + headBob), 15, 3);
    }

    private void drawDirtBikeRider(Graphics2D g2) {
        double leanAngle = (gasInput ? 0.22 : (brakeInput ? -0.18 : 0));
        Graphics2D dg = (Graphics2D) g2.create();
        dg.translate(-10, -22);
        dg.rotate(leanAngle);

        // Red Torso
        dg.setColor(new Color(225, 29, 72));
        dg.fillRoundRect(-6, -22, 13, 22, 6, 6);

        // Arm
        dg.setColor(new Color(225, 29, 72));
        dg.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        dg.drawLine(0, -16, 28, -6);

        // Glove
        dg.setColor(new Color(251, 191, 36));
        dg.fillOval(26, -8, 6, 6);

        // Head with pink nose
        dg.setColor(new Color(254, 215, 170));
        dg.fillOval(-6, -34, 14, 14);
        dg.setColor(new Color(244, 114, 182));
        dg.fillOval(5, -30, 8, 6); // Cartoon nose (like in photo!)

        // Brown Cap
        dg.setColor(new Color(146, 64, 14));
        dg.fillArc(-8, -38, 18, 15, 0, 180);
        dg.fillRect(-4, -33, 16, 4);

        // Eye
        dg.setColor(Color.BLACK);
        dg.fillOval(2, -31, 3, 3);

        dg.dispose();
    }

    private void drawWheel(Graphics2D g2, double wx, double wy, double radius, double spinAngle, VehicleType type) {
        Graphics2D wg = (Graphics2D) g2.create();
        wg.translate(wx, wy);

        int r = (int) radius;

        // Ground shadow
        wg.setColor(new Color(15, 23, 42, 65));
        wg.fillOval(-r - 1, -r + 2, (r + 1) * 2, (r + 1) * 2);

        // Wheel rotation
        wg.rotate(spinAngle);

        // Dark Tire Rubber
        wg.setColor(new Color(15, 23, 42));
        wg.fillOval(-r, -r, r * 2, r * 2);

        // Tire Treads
        wg.setColor(new Color(30, 41, 59));
        wg.setStroke(new BasicStroke(2.8f));
        for (int a = 0; a < 360; a += 40) {
            double rad = Math.toRadians(a);
            int x1 = (int) (Math.cos(rad) * (r - 3));
            int y1 = (int) (Math.sin(rad) * (r - 3));
            int x2 = (int) (Math.cos(rad) * (r + 1));
            int y2 = (int) (Math.sin(rad) * (r + 1));
            wg.drawLine(x1, y1, x2, y2);
        }

        // "MONSTER" and "FINGER" Sidewall White Text (Exactly like the photo!)
        wg.setColor(Color.WHITE);
        wg.setFont(new Font("Impact", Font.PLAIN, (int) Math.max(7, radius * 0.40)));
        wg.drawString("MONSTER", -r + 3, -2);
        wg.drawString("FINGER", -r + 5, 10);

        // Inner Rim
        int rimR = r - 5;
        wg.setColor(new Color(51, 65, 85));
        wg.fillOval(-rimR, -rimR, rimR * 2, rimR * 2);

        // 4-Spoke White / Silver Rim
        wg.setColor(Color.WHITE);
        wg.setStroke(new BasicStroke(2.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        wg.drawLine(-rimR + 2, 0, rimR - 2, 0);
        wg.drawLine(0, -rimR + 2, 0, rimR - 2);

        // Center Nut
        wg.setColor(new Color(245, 158, 11));
        wg.fillOval(-4, -4, 8, 8);

        wg.dispose();
    }
}
