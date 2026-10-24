package project.aoop.space_sandbox.physics;

import org.springframework.stereotype.Component;
import project.aoop.space_sandbox.config.PhysicsConfig;
import project.aoop.space_sandbox.entity.CelestialBody;
import project.aoop.space_sandbox.entity.Ship;
import project.aoop.space_sandbox.entity.Star;
import project.aoop.space_sandbox.service.LevelService;

import java.util.List;

@Component
public class GravitySystem {

    private final LevelService levelService;

    public GravitySystem(LevelService levelService) {
        this.levelService = levelService;
    }

    public void applyGravity(Ship ship, List<CelestialBody> bodies, double dt) {

        // Find closest active body (current level planet or Sun)
        // for orbit assist — same logic as before
        CelestialBody dominant    = null;
        double        closestDist = Double.MAX_VALUE;

        for (CelestialBody body : bodies) {
            double dx   = body.getX() - ship.getX();
            double dy   = body.getY() - ship.getY();
            double dist = Math.sqrt(dx*dx + dy*dy);

            if (dist > body.getGravityRadius()) continue;
            if (dist < body.getRadius())        continue;

            if (body instanceof Star) {
                // Sun — always raw gravity, check separately below
                continue;
            }

            String name = body.getName();

            if (levelService.isLocked(name)) {
                // Locked — apply strong unavoidable gravity immediately
                // Don't add to dominant — apply directly, full strength
                applyLockedGravity(ship, dx, dy, dist, body.getMass(), dt);

            } else if (levelService.isGravityActive(name)) {
                // Current level — orbit assist (gentle, escapable)
                if (dist < closestDist) {
                    closestDist = dist;
                    dominant    = body;
                }
            }
            // Mastered — no gravity at all, skip
        }

        // Apply orbit assist to closest current-level planet
        if (dominant != null) {
            double dx = dominant.getX() - ship.getX();
            double dy = dominant.getY() - ship.getY();
            applyOrbitAssist(ship, dx, dy, closestDist,
                    dominant.getMass(), dominant.getGravityRadius(), dt);
        }

        // Sun — raw gravity regardless of level
        for (CelestialBody body : bodies) {
            if (!(body instanceof Star)) continue;
            double dx   = body.getX() - ship.getX();
            double dy   = body.getY() - ship.getY();
            double dist = Math.sqrt(dx*dx + dy*dy);
            if (dist > body.getGravityRadius()) continue;
            if (dist < body.getRadius())        continue;
            applyRawGravity(ship, dx, dy, dist, body.getMass(), dt);
        }
    }

    // Locked planets: extremely strong pull — thrust can't escape
    // Ship spirals inward within seconds of entering the zone
    private void applyLockedGravity(Ship ship, double dx, double dy,
                                    double dist, double mass, double dt) {
        double force = PhysicsConfig.G * mass * PhysicsConfig.LOCKED_GRAVITY_MULT
                / (dist * dist);
        ship.setVx(ship.getVx() + (dx / dist) * force * dt);
        ship.setVy(ship.getVy() + (dy / dist) * force * dt);
    }

    // Sun: standard raw gravity
    private void applyRawGravity(Ship ship, double dx, double dy,
                                 double dist, double mass, double dt) {
        double force = PhysicsConfig.G * mass / (dist * dist);
        ship.setVx(ship.getVx() + (dx / dist) * force * dt);
        ship.setVy(ship.getVy() + (dy / dist) * force * dt);
    }

    // Current planet: gentle orbit assist
    private void applyOrbitAssist(Ship ship, double dx, double dy,
                                  double dist, double mass,
                                  double zoneRadius, double dt) {
        double rx = -dx / dist, ry = -dy / dist;
        double tx = -ry,        ty =  rx;

        double radial     = ship.getVx() * rx + ship.getVy() * ry;
        double tangential = ship.getVx() * tx + ship.getVy() * ty;

        double dir           = tangential >= 0 ? 1.0 : -1.0;
        double absTangential = Math.abs(tangential);
        double orbitalSpeed  = Math.sqrt(PhysicsConfig.G * mass / dist);

        double blendRate        = Math.min(PhysicsConfig.ORBIT_ASSIST_RATE * dt, 1.0);
        double newAbsTangential = absTangential
                + (orbitalSpeed - absTangential) * blendRate;
        double newRadial = radial * (1.0 - PhysicsConfig.RADIAL_DAMPEN_RATE * dt);

        // Edge retention
        double edgeThreshold = zoneRadius * 0.75;
        if (dist > edgeThreshold) {
            double edgeFraction = (dist - edgeThreshold)
                    / (zoneRadius - edgeThreshold);
            double pull = edgeFraction * PhysicsConfig.G * mass
                    / (dist * dist) * 0.5;
            newRadial -= pull * dt;
        }

        ship.setVx(newRadial * rx + dir * newAbsTangential * tx);
        ship.setVy(newRadial * ry + dir * newAbsTangential * ty);
    }
}